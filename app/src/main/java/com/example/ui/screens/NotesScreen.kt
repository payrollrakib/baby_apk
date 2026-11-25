package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BabyNote
import com.example.data.model.NoteCategory
import com.example.data.model.toBengaliDigits
import com.example.ui.viewmodel.UiState
import java.io.File
import kotlin.math.roundToInt

@Composable
fun NotesScreen(
    notesState: UiState<List<BabyNote>>,
    defaultAgeMonth: Int,
    isSaving: Boolean,
    resolvePhotoFile: (String?) -> File?,
    onAddNote: (title: String, content: String, category: NoteCategory, ageMonth: Int, pinned: Boolean, pickedUri: Uri?) -> Unit,
    onUpdateNote: (existing: BabyNote, title: String, content: String, category: NoteCategory, ageMonth: Int, pinned: Boolean, newUri: Uri?, removeImage: Boolean) -> Unit,
    onTogglePin: (BabyNote) -> Unit,
    onDeleteNote: (BabyNote) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<NoteCategory?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<BabyNote?>(null) }
    var deletingNote by remember { mutableStateOf<BabyNote?>(null) }

    val notesList = (notesState as? UiState.Success)?.data.orEmpty()
    val filteredNotes = remember(notesList, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        notesList.filter { note ->
            val catMatch = selectedCategory == null || note.category == selectedCategory?.name
            val queryMatch = q.isEmpty() ||
                note.title.lowercase().contains(q) ||
                note.content.lowercase().contains(q)
            catMatch && queryMatch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_note_label)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "শিশুর ডায়েরি ও জরুরি নোট",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        leadingIcon = {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(imageVector = Icons.Rounded.Close, contentDescription = "মুছুন")
                                }
                            }
                        },
                        placeholder = { Text("ডাক্তার, ওষুধ, খাবার বা নোট খুঁজুন...") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_notes_input")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("সব (${notesList.size.toBengaliDigits()})") }
                        )
                        NoteCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.labelBn) }
                            )
                        }
                    }
                }
            }

            when (notesState) {
                is UiState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }

                is UiState.Error -> {
                    item {
                        ErrorBannerCard(message = notesState.message)
                    }
                }

                is UiState.Success -> {
                    if (filteredNotes.isEmpty()) {
                        item {
                            EmptyNotesCard(
                                hasAnyNotes = notesList.isNotEmpty(),
                                onCreateNote = { showCreateDialog = true }
                            )
                        }
                    } else {
                        items(filteredNotes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                imageFile = resolvePhotoFile(note.localImageFileName),
                                onTogglePin = { onTogglePin(note) },
                                onEdit = { editingNote = note },
                                onDelete = { deletingNote = note }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        NoteEditorDialog(
            initialNote = null,
            existingImageFile = null,
            defaultAgeMonth = defaultAgeMonth,
            isSaving = isSaving,
            onDismiss = { showCreateDialog = false },
            onSave = { title, content, category, ageMonth, pinned, newUri, _ ->
                onAddNote(title, content, category, ageMonth, pinned, newUri)
                showCreateDialog = false
            }
        )
    }

    editingNote?.let { target ->
        NoteEditorDialog(
            initialNote = target,
            existingImageFile = resolvePhotoFile(target.localImageFileName),
            defaultAgeMonth = target.ageMonth.toInt(),
            isSaving = isSaving,
            onDismiss = { editingNote = null },
            onSave = { title, content, category, ageMonth, pinned, newUri, removeExisting ->
                onUpdateNote(target, title, content, category, ageMonth, pinned, newUri, removeExisting)
                editingNote = null
            }
        )
    }

    deletingNote?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingNote = null },
            title = { Text("নোটটি মুছে ফেলবেন?") },
            text = { Text("“${target.title}” স্থায়ীভাবে মুছে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(target)
                        deletingNote = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingNote = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyNotesCard(
    hasAnyNotes: Boolean,
    onCreateNote: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!hasAnyNotes) {
                Image(
                    painter = painterResource(id = R.drawable.img_empty_memories),
                    contentDescription = "শিশুর নোটবুক",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
            }
            Text(
                text = if (hasAnyNotes) "খোঁজা নোটটি পাওয়া যায়নি" else "ডাক্তারের পরামর্শ ও প্রতিদিনের নোট রাখুন",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = if (hasAnyNotes) {
                    "অন্য শব্দ দিয়ে খুঁজুন অথবা সব ক্যাটাগরি নির্বাচন করুন।"
                } else {
                    "শিশুর ওজন, ওষুধের ডোজ, ঘুমের সময়, খাবারের পছন্দ কিংবা মাইলস্টোন—প্রয়োজনীয় সব নোট ছবি সহ লিখে রাখুন।"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!hasAnyNotes) {
                Button(
                    onClick = onCreateNote,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("empty_add_note_button")
                ) {
                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.add_note_label))
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: BabyNote,
    imageFile: File?,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = NoteCategory.fromRaw(note.category)

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (note.pinned) {
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("note_item_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = category.labelBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${note.ageMonth.toInt().toBengaliDigits()} মাস",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = if (note.pinned) "আনপিন করুন" else "পিন করুন",
                            tint = if (note.pinned) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            if (imageFile != null) {
                AsyncImage(
                    model = imageFile,
                    contentDescription = note.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorDialog(
    initialNote: BabyNote?,
    existingImageFile: File?,
    defaultAgeMonth: Int,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        content: String,
        category: NoteCategory,
        ageMonth: Int,
        pinned: Boolean,
        newPickedUri: Uri?,
        removeExisting: Boolean
    ) -> Unit
) {
    var title by remember(initialNote) { mutableStateOf(initialNote?.title ?: "") }
    var content by remember(initialNote) { mutableStateOf(initialNote?.content ?: "") }
    var selectedCategory by remember(initialNote) {
        mutableStateOf(NoteCategory.fromRaw(initialNote?.category))
    }
    var ageFloat by remember(initialNote) {
        mutableFloatStateOf(
            (initialNote?.ageMonth?.toInt() ?: defaultAgeMonth).coerceIn(0, 12).toFloat()
        )
    }
    var pinned by remember(initialNote) { mutableStateOf(initialNote?.pinned ?: false) }
    var pickedUri by remember(initialNote) { mutableStateOf<Uri?>(null) }
    var removeExistingImage by remember(initialNote) { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                pickedUri = uri
                removeExistingImage = false
            }
        }
    )

    val displayModel: Any? = pickedUri ?: if (!removeExistingImage) existingImageFile else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialNote == null) "নতুন নোট লিখুন" else "নোট সম্পাদনা করুন",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 150) title = it },
                    label = { Text("শিরোনাম *") },
                    placeholder = { Text("যেমন: শিশুর ওজন ও ডাক্তারের পরামর্শ") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 3000) content = it },
                    label = { Text("বিস্তারিত নোট *") },
                    placeholder = { Text("প্রয়োজনীয় তথ্য, ওষুধের নিয়ম বা মাইলস্টোন লিখুন...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_content_input")
                )

                Text("বিভাগ", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NoteCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.labelBn) }
                        )
                    }
                }

                val ageInt = ageFloat.roundToInt()
                Text(
                    text = "বয়স: ${ageInt.toBengaliDigits()} মাস",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = ageFloat,
                    onValueChange = { ageFloat = it },
                    valueRange = 0f..12f,
                    steps = 11
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "উপরে পিন করে রাখুন",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = pinned,
                        onCheckedChange = { pinned = it }
                    )
                }

                if (displayModel != null) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = displayModel,
                            contentDescription = "নোটের সংযুক্ত ছবি",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(MaterialTheme.shapes.small)
                        )
                        IconButton(
                            onClick = {
                                pickedUri = null
                                removeExistingImage = true
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "ছবি সরান",
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }

                OutlinedButton(
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("attach_note_photo_button")
                ) {
                    Icon(imageVector = Icons.Rounded.AddPhotoAlternate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (displayModel == null) "ছবি যুক্ত করুন (ঐচ্ছিক)" else "ছবি পরিবর্তন করুন")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        content,
                        selectedCategory,
                        ageFloat.roundToInt().coerceIn(0, 12),
                        pinned,
                        pickedUri,
                        removeExistingImage
                    )
                },
                enabled = title.trim().isNotEmpty() && content.trim().isNotEmpty() && !isSaving,
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
