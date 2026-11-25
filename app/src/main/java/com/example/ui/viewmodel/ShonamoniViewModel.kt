package com.example.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.BabyRepository
import com.example.data.firebase.PhotoStorageHelper
import com.example.data.model.AgeStage
import com.example.data.model.BabyGender
import com.example.data.model.BabyNote
import com.example.data.model.BabyPhoto
import com.example.data.model.NoteCategory
import com.example.data.model.TodoCategory
import com.example.data.model.TodoItem
import com.example.data.model.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ShonamoniViewModel(
    private val repository: BabyRepository,
    private val photoStorage: PhotoStorageHelper,
    private val currentUserId: String
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents = _snackbarEvents.asSharedFlow()

    val profileState: StateFlow<UiState<UserProfile?>> = repository.observeUserProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile?>> { UiState.Success(it) }
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing profile", root)
            emit(UiState.Error(root.localizedMessage ?: "শিশুর প্রোফাইল লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val todosState: StateFlow<UiState<List<TodoItem>>> = repository.observeTodos(currentUserId)
        .map<List<TodoItem>, UiState<List<TodoItem>>> { list ->
            val sorted = list.sortedWith(
                compareBy<TodoItem> { it.completed }
                    .thenByDescending { it.createdAt ?: Timestamp(0, 0) }
            )
            UiState.Success(sorted)
        }
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing todos", root)
            emit(UiState.Error(root.localizedMessage ?: "করণীয় তালিকা লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val photosState: StateFlow<UiState<List<BabyPhoto>>> = repository.observePhotos(currentUserId)
        .map<List<BabyPhoto>, UiState<List<BabyPhoto>>> { list ->
            val sorted = list.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            UiState.Success(sorted)
        }
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing photos", root)
            emit(UiState.Error(root.localizedMessage ?: "ছবির তালিকা লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    val notesState: StateFlow<UiState<List<BabyNote>>> = repository.observeNotes(currentUserId)
        .map<List<BabyNote>, UiState<List<BabyNote>>> { list ->
            val sorted = list.sortedWith(
                compareByDescending<BabyNote> { it.pinned }
                    .thenByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            )
            UiState.Success(sorted)
        }
        .catch { error ->
            val root = unwrapFirestoreError(error)
            Log.w(TAG, "Error observing notes", root)
            emit(UiState.Error(root.localizedMessage ?: "নোট তালিকা লোড করা যায়নি।"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = UiState.Loading
        )

    fun resolvePhotoFile(fileName: String?): File? = photoStorage.resolvePhotoFile(fileName)

    fun saveBabyProfile(
        babyName: String,
        babyBirthDateMillis: Long,
        babyGender: BabyGender,
        babyAgeMonths: Int
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.saveUserProfile(
                userId = currentUserId,
                babyName = babyName,
                babyBirthDateMillis = babyBirthDateMillis,
                babyGender = babyGender,
                babyAgeMonths = babyAgeMonths
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("শিশুর তথ্য সংরক্ষিত হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "তথ্য সংরক্ষণ করা যায়নি")
            }
        }
    }

    fun seedStarterChecklist() {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.seedStarterTodos(currentUserId)
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("০–১২ মাসের আদর্শ করণীয় তালিকা যোগ করা হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "তালিকা যোগ করা যায়নি")
            }
        }
    }

    fun addTodo(
        title: String,
        notes: String,
        ageStage: AgeStage,
        category: TodoCategory,
        successMessage: String = "নতুন কাজ যোগ করা হয়েছে"
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.addTodo(
                userId = currentUserId,
                title = title,
                notes = notes,
                ageStage = ageStage,
                category = category
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit(successMessage)
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "কাজ যোগ করা যায়নি")
            }
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            val result = repository.toggleTodo(currentUserId, todo.id, !todo.completed)
            result.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "অবস্থা পরিবর্তন করা যায়নি")
            }
        }
    }

    fun updateTodo(
        todoId: String,
        title: String,
        notes: String,
        ageStage: AgeStage,
        category: TodoCategory,
        completed: Boolean
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateTodo(
                userId = currentUserId,
                todoId = todoId,
                title = title,
                notes = notes,
                ageStage = ageStage,
                category = category,
                completed = completed
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("কাজ আপডেট করা হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "আপডেট ব্যর্থ হয়েছে")
            }
        }
    }

    fun deleteTodo(todoId: String) {
        viewModelScope.launch {
            val result = repository.deleteTodo(currentUserId, todoId)
            result.onSuccess {
                _snackbarEvents.tryEmit("কাজটি মুছে ফেলা হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "মুছে ফেলা যায়নি")
            }
        }
    }

    fun addPhotoMemory(
        imageUri: Uri,
        title: String,
        caption: String,
        ageMonth: Int,
        milestoneTag: String,
        dateLabel: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val savedFileResult = photoStorage.savePickedPhoto(imageUri, prefix = "memory")
            val fileName = savedFileResult.getOrElse { err ->
                _isSaving.value = false
                _snackbarEvents.tryEmit(err.localizedMessage ?: "ছবি প্রসেস করা যায়নি")
                return@launch
            }
            val result = repository.addPhoto(
                userId = currentUserId,
                title = title,
                caption = caption,
                ageMonth = ageMonth,
                milestoneTag = milestoneTag,
                localFileName = fileName,
                dateLabel = dateLabel
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("ছবির স্মৃতি সংরক্ষিত হয়েছে")
            }.onFailure { err ->
                photoStorage.deletePhotoFile(fileName)
                _snackbarEvents.tryEmit(err.localizedMessage ?: "ছবি সংরক্ষণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun updatePhotoMemory(
        photoId: String,
        title: String,
        caption: String,
        ageMonth: Int,
        milestoneTag: String,
        dateLabel: String
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updatePhoto(
                userId = currentUserId,
                photoId = photoId,
                title = title,
                caption = caption,
                ageMonth = ageMonth,
                milestoneTag = milestoneTag,
                dateLabel = dateLabel
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("ছবির তথ্য আপডেট হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "আপডেট ব্যর্থ হয়েছে")
            }
        }
    }

    fun deletePhotoMemory(photo: BabyPhoto) {
        viewModelScope.launch {
            val result = repository.deletePhoto(currentUserId, photo.id)
            result.onSuccess {
                photoStorage.deletePhotoFile(photo.localFileName)
                _snackbarEvents.tryEmit("ছবিটি মুছে ফেলা হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "ছবি মুছে ফেলা যায়নি")
            }
        }
    }

    fun addNote(
        title: String,
        content: String,
        category: NoteCategory,
        ageMonth: Int,
        pinned: Boolean,
        pickedImageUri: Uri?
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            var savedFileName: String? = null
            if (pickedImageUri != null) {
                val imgRes = photoStorage.savePickedPhoto(pickedImageUri, prefix = "note")
                savedFileName = imgRes.getOrElse { err ->
                    _isSaving.value = false
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "নোটের ছবি সংরক্ষণ করা যায়নি")
                    return@launch
                }
            }
            val result = repository.addNote(
                userId = currentUserId,
                title = title,
                content = content,
                category = category,
                ageMonth = ageMonth,
                pinned = pinned,
                localImageFileName = savedFileName
            )
            _isSaving.value = false
            result.onSuccess {
                _snackbarEvents.tryEmit("নতুন নোট সংরক্ষিত হয়েছে")
            }.onFailure { err ->
                if (savedFileName != null) photoStorage.deletePhotoFile(savedFileName)
                _snackbarEvents.tryEmit(err.localizedMessage ?: "নোট সংরক্ষণ ব্যর্থ হয়েছে")
            }
        }
    }

    fun updateNote(
        existingNote: BabyNote,
        title: String,
        content: String,
        category: NoteCategory,
        ageMonth: Int,
        pinned: Boolean,
        newPickedImageUri: Uri?,
        removeExistingImage: Boolean
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            var newFileName: String? = existingNote.localImageFileName
            var newlyCreatedFile: String? = null

            if (newPickedImageUri != null) {
                val imgRes = photoStorage.savePickedPhoto(newPickedImageUri, prefix = "note")
                newlyCreatedFile = imgRes.getOrElse { err ->
                    _isSaving.value = false
                    _snackbarEvents.tryEmit(err.localizedMessage ?: "নোটের ছবি আপডেট করা যায়নি")
                    return@launch
                }
                newFileName = newlyCreatedFile
            } else if (removeExistingImage) {
                newFileName = null
            }

            val result = repository.updateNote(
                userId = currentUserId,
                noteId = existingNote.id,
                title = title,
                content = content,
                category = category,
                ageMonth = ageMonth,
                pinned = pinned,
                localImageFileName = newFileName
            )
            _isSaving.value = false
            result.onSuccess {
                if ((newPickedImageUri != null || removeExistingImage) &&
                    !existingNote.localImageFileName.isNullOrBlank()
                ) {
                    photoStorage.deletePhotoFile(existingNote.localImageFileName)
                }
                _snackbarEvents.tryEmit("নোট আপডেট করা হয়েছে")
            }.onFailure { err ->
                if (newlyCreatedFile != null) photoStorage.deletePhotoFile(newlyCreatedFile)
                _snackbarEvents.tryEmit(err.localizedMessage ?: "নোট আপডেট ব্যর্থ হয়েছে")
            }
        }
    }

    fun toggleNotePin(note: BabyNote) {
        viewModelScope.launch {
            val result = repository.toggleNotePin(currentUserId, note.id, !note.pinned)
            result.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "পিন পরিবর্তন করা যায়নি")
            }
        }
    }

    fun deleteNote(note: BabyNote) {
        viewModelScope.launch {
            val result = repository.deleteNote(currentUserId, note.id)
            result.onSuccess {
                if (!note.localImageFileName.isNullOrBlank()) {
                    photoStorage.deletePhotoFile(note.localImageFileName)
                }
                _snackbarEvents.tryEmit("নোটটি মুছে ফেলা হয়েছে")
            }.onFailure { err ->
                _snackbarEvents.tryEmit(err.localizedMessage ?: "নোট মুছে ফেলা যায়নি")
            }
        }
    }

    private fun unwrapFirestoreError(error: Throwable): Throwable {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        return firestoreError ?: error
    }

    private companion object {
        const val TAG = "ShonamoniVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
