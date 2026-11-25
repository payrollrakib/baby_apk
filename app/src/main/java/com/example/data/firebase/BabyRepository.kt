package com.example.data.firebase

import android.content.Context
import com.example.R
import com.example.data.content.BabyKnowledgeBase
import com.example.data.model.AgeStage
import com.example.data.model.BabyGender
import com.example.data.model.BabyNote
import com.example.data.model.BabyPhoto
import com.example.data.model.NoteCategory
import com.example.data.model.TodoCategory
import com.example.data.model.TodoItem
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class BabyRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String? = null): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("ব্যবহারকারী লগইন করা অবস্থায় নেই।")
        if (expectedUserId != null) {
            check(uid == expectedUserId) { "অনুমোদিত ব্যবহারকারীর সাথে মিল নেই।" }
        }
        return uid
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(
        userId: String,
        babyName: String,
        babyBirthDateMillis: Long,
        babyGender: BabyGender,
        babyAgeMonths: Int
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val cleanName = babyName.trim().ifBlank { "সোনামণি" }.take(60)
            val cleanAge = babyAgeMonths.coerceIn(0, 12).toLong()
            val cleanBirth = babyBirthDateMillis.coerceAtLeast(0L)
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "babyName" to cleanName,
                    "babyBirthDateMillis" to cleanBirth,
                    "babyGender" to babyGender.name,
                    "babyAgeMonths" to cleanAge,
                    "starterSeeded" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                docRef.update(
                    mapOf(
                        "babyName" to cleanName,
                        "babyBirthDateMillis" to cleanBirth,
                        "babyGender" to babyGender.name,
                        "babyAgeMonths" to cleanAge,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    fun observeTodos(userId: String): Flow<List<TodoItem>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/todos"
        emitAll(
            db.collection("users").document(uid).collection("todos")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        TodoItem::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun seedStarterTodos(userId: String): Result<Unit> {
        val basePath = "users/$userId"
        return try {
            val uid = requireUserId(userId)
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            val batch = db.batch()

            if (!userSnap.exists()) {
                batch.set(
                    userRef,
                    mapOf(
                        "userId" to uid,
                        "babyName" to "সোনামণি",
                        "babyBirthDateMillis" to System.currentTimeMillis(),
                        "babyGender" to BabyGender.UNSPECIFIED.name,
                        "babyAgeMonths" to 0L,
                        "starterSeeded" to true,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            } else {
                batch.update(
                    userRef,
                    mapOf(
                        "starterSeeded" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            val todosCollection = userRef.collection("todos")
            for (seed in BabyKnowledgeBase.starterTodos) {
                val docRef = todosCollection.document()
                batch.set(
                    docRef,
                    mapOf(
                        "userId" to uid,
                        "title" to seed.title.take(200),
                        "notes" to seed.notes.take(500),
                        "ageStage" to seed.ageStage.name,
                        "category" to seed.category.name,
                        "completed" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, basePath)
            Result.failure(e)
        }
    }

    suspend fun addTodo(
        userId: String,
        title: String,
        notes: String,
        ageStage: AgeStage,
        category: TodoCategory
    ): Result<Unit> {
        val basePath = "users/$userId/todos"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim().take(200)
            require(cleanTitle.isNotEmpty()) { "কাজের শিরোনাম লিখুন" }
            val cleanNotes = notes.trim().take(500)
            val docRef = db.collection("users").document(uid).collection("todos").document()
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "notes" to cleanNotes,
                    "ageStage" to ageStage.name,
                    "category" to category.name,
                    "completed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, basePath)
            Result.failure(e)
        }
    }

    suspend fun toggleTodo(userId: String, todoId: String, completed: Boolean): Result<Unit> {
        val docPath = "users/$userId/todos/$todoId"
        return try {
            val uid = requireUserId(userId)
            require(todoId.isNotBlank()) { "অবৈধ টাস্ক আইডি" }
            db.collection("users").document(uid).collection("todos").document(todoId)
                .update(
                    mapOf(
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateTodo(
        userId: String,
        todoId: String,
        title: String,
        notes: String,
        ageStage: AgeStage,
        category: TodoCategory,
        completed: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/todos/$todoId"
        return try {
            val uid = requireUserId(userId)
            require(todoId.isNotBlank()) { "অবৈধ টাস্ক আইডি" }
            val cleanTitle = title.trim().take(200)
            require(cleanTitle.isNotEmpty()) { "কাজের শিরোনাম লিখুন" }
            val cleanNotes = notes.trim().take(500)
            db.collection("users").document(uid).collection("todos").document(todoId)
                .update(
                    mapOf(
                        "title" to cleanTitle,
                        "notes" to cleanNotes,
                        "ageStage" to ageStage.name,
                        "category" to category.name,
                        "completed" to completed,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteTodo(userId: String, todoId: String): Result<Unit> {
        val docPath = "users/$userId/todos/$todoId"
        return try {
            val uid = requireUserId(userId)
            require(todoId.isNotBlank()) { "অবৈধ টাস্ক আইডি" }
            db.collection("users").document(uid).collection("todos").document(todoId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observePhotos(userId: String): Flow<List<BabyPhoto>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/photos"
        emitAll(
            db.collection("users").document(uid).collection("photos")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BabyPhoto::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addPhoto(
        userId: String,
        title: String,
        caption: String,
        ageMonth: Int,
        milestoneTag: String,
        localFileName: String,
        dateLabel: String
    ): Result<Unit> {
        val basePath = "users/$userId/photos"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "ছবির শিরোনাম লিখুন" }
            require(localFileName.isNotBlank()) { "ছবি সংরক্ষণ ব্যর্থ হয়েছে" }
            val docRef = db.collection("users").document(uid).collection("photos").document()
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "title" to cleanTitle,
                    "caption" to caption.trim().take(500),
                    "ageMonth" to ageMonth.coerceIn(0, 12).toLong(),
                    "milestoneTag" to milestoneTag.trim().take(60),
                    "localFileName" to localFileName.trim().take(160),
                    "dateLabel" to dateLabel.trim().take(40),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, basePath)
            Result.failure(e)
        }
    }

    suspend fun updatePhoto(
        userId: String,
        photoId: String,
        title: String,
        caption: String,
        ageMonth: Int,
        milestoneTag: String,
        dateLabel: String
    ): Result<Unit> {
        val docPath = "users/$userId/photos/$photoId"
        return try {
            val uid = requireUserId(userId)
            require(photoId.isNotBlank()) { "অবৈধ ছবি আইডি" }
            val cleanTitle = title.trim().take(120)
            require(cleanTitle.isNotEmpty()) { "ছবির শিরোনাম লিখুন" }
            db.collection("users").document(uid).collection("photos").document(photoId)
                .update(
                    mapOf(
                        "title" to cleanTitle,
                        "caption" to caption.trim().take(500),
                        "ageMonth" to ageMonth.coerceIn(0, 12).toLong(),
                        "milestoneTag" to milestoneTag.trim().take(60),
                        "dateLabel" to dateLabel.trim().take(40),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deletePhoto(userId: String, photoId: String): Result<Unit> {
        val docPath = "users/$userId/photos/$photoId"
        return try {
            val uid = requireUserId(userId)
            require(photoId.isNotBlank()) { "অবৈধ ছবি আইডি" }
            db.collection("users").document(uid).collection("photos").document(photoId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    fun observeNotes(userId: String): Flow<List<BabyNote>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/notes"
        emitAll(
            db.collection("users").document(uid).collection("notes")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        BabyNote::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val toLog = firestoreError ?: (error as? Exception)
                    if (toLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(toLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addNote(
        userId: String,
        title: String,
        content: String,
        category: NoteCategory,
        ageMonth: Int,
        pinned: Boolean,
        localImageFileName: String?
    ): Result<Unit> {
        val basePath = "users/$userId/notes"
        return try {
            val uid = requireUserId(userId)
            val cleanTitle = title.trim().take(150)
            val cleanContent = content.trim().take(3000)
            require(cleanTitle.isNotEmpty()) { "নোটের শিরোনাম লিখুন" }
            require(cleanContent.isNotEmpty()) { "নোটের বিবরণ লিখুন" }
            val cleanImage = localImageFileName?.trim()?.takeIf { it.isNotEmpty() }?.take(160)
            val payload = mapOf(
                "userId" to uid,
                "title" to cleanTitle,
                "content" to cleanContent,
                "category" to category.name,
                "ageMonth" to ageMonth.coerceIn(0, 12).toLong(),
                "pinned" to pinned,
                "localImageFileName" to cleanImage,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            ).filterValues { it != null } as Map<String, Any>

            db.collection("users").document(uid).collection("notes").document()
                .set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, basePath)
            Result.failure(e)
        }
    }

    suspend fun updateNote(
        userId: String,
        noteId: String,
        title: String,
        content: String,
        category: NoteCategory,
        ageMonth: Int,
        pinned: Boolean,
        localImageFileName: String?
    ): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "অবৈধ নোট আইডি" }
            val cleanTitle = title.trim().take(150)
            val cleanContent = content.trim().take(3000)
            require(cleanTitle.isNotEmpty()) { "নোটের শিরোনাম লিখুন" }
            require(cleanContent.isNotEmpty()) { "নোটের বিবরণ লিখুন" }
            val cleanImage = localImageFileName?.trim()?.takeIf { it.isNotEmpty() }?.take(160)
            val payload = mutableMapOf<String, Any>(
                "title" to cleanTitle,
                "content" to cleanContent,
                "category" to category.name,
                "ageMonth" to ageMonth.coerceIn(0, 12).toLong(),
                "pinned" to pinned,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (cleanImage != null) {
                payload["localImageFileName"] = cleanImage
            } else {
                payload["localImageFileName"] = FieldValue.delete()
            }
            db.collection("users").document(uid).collection("notes").document(noteId)
                .update(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleNotePin(userId: String, noteId: String, pinned: Boolean): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "অবৈধ নোট আইডি" }
            db.collection("users").document(uid).collection("notes").document(noteId)
                .update(
                    mapOf(
                        "pinned" to pinned,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        val docPath = "users/$userId/notes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "অবৈধ নোট আইডি" }
            db.collection("users").document(uid).collection("notes").document(noteId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
