package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.content.BabyKnowledgeBase
import com.example.data.model.BabyPhoto
import com.example.data.model.toBengaliDigits
import com.example.ui.viewmodel.UiState
import java.io.File
import kotlin.math.roundToInt

@Composable
fun PhotosScreen(
    photosState: UiState<List<BabyPhoto>>,
    defaultAgeMonth: Int,
    isSaving: Boolean,
    resolvePhotoFile: (String?) -> File?,
    onAddPhoto: (uri: Uri, title: String, caption: String, ageMonth: Int, milestoneTag: String, dateLabel: String) -> Unit,
    onUpdatePhoto: (photoId: String, title: String, caption: String, ageMonth: Int, milestoneTag: String, dateLabel: String) -> Unit,
    onDeletePhoto: (BabyPhoto) -> Unit
) {
    var selectedMonthFilter by remember { mutableStateOf<Int?>(null) }
    var pickedUriForNew by remember { mutableStateOf<Uri?>(null) }
    var viewingPhoto by remember { mutableStateOf<BabyPhoto?>(null) }
    var editingPhoto by remember { mutableStateOf<BabyPhoto?>(null) }
    var deletingPhoto by remember { mutableStateOf<BabyPhoto?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                pickedUriForNew = uri
            }
        }
    )

    val launchPhotoPicker = {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    val photosList = (photosState as? UiState.Success)?.data.orEmpty()
    val filteredPhotos = remember(photosList, selectedMonthFilter) {
        if (selectedMonthFilter == null) photosList
        else photosList.filter { it.ageMonth.toInt() == selectedMonthFilter }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = launchPhotoPicker,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.AddAPhoto,
                        contentDescription = null
                    )
                },
                text = { Text(stringResource(R.string.add_photo_label)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_photo_fab")
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val columns = if (maxWidth >= 600.dp) 3 else 2

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(span = { GridItemSpan(columns) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "সোনামণির ছবির অ্যালবাম",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "০ থেকে ১২ মাসের প্রতিটি মিষ্টি মুহূর্ত ও মাইলস্টোনের ছবি সংরক্ষণ করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedMonthFilter == null,
                                onClick = { selectedMonthFilter = null },
                                label = { Text("সব (${photosList.size.toBengaliDigits()})") }
                            )
                            (0..12).forEach { month ->
                                FilterChip(
                                    selected = selectedMonthFilter == month,
                                    onClick = { selectedMonthFilter = month },
                                    label = { Text("${month.toBengaliDigits()} মাস") }
                                )
                            }
                        }
                    }
                }

                when (photosState) {
                    is UiState.Loading -> {
                        item(span = { GridItemSpan(columns) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    is UiState.Error -> {
                        item(span = { GridItemSpan(columns) }) {
                            ErrorBannerCard(message = photosState.message)
                        }
                    }

                    is UiState.Success -> {
                        if (filteredPhotos.isEmpty()) {
                            item(span = { GridItemSpan(columns) }) {
                                EmptyPhotosCard(
                                    isFiltered = selectedMonthFilter != null,
                                    onAddPhoto = launchPhotoPicker
                                )
                            }
                        } else {
                            items(filteredPhotos, key = { it.id }) { photo ->
                                PhotoMemoryCard(
                                    photo = photo,
                                    imageFile = resolvePhotoFile(photo.localFileName),
                                    onClick = { viewingPhoto = photo }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pickedUriForNew?.let { uri ->
        PhotoMemoryEditorDialog(
            previewModel = uri,
            initialPhoto = null,
            defaultAgeMonth = defaultAgeMonth,
            isSaving = isSaving,
            onDismiss = { pickedUriForNew = null },
            onSave = { title, caption, ageMonth, tag, dateLabel ->
                onAddPhoto(uri, title, caption, ageMonth, tag, dateLabel)
                pickedUriForNew = null
            }
        )
    }

    editingPhoto?.let { target ->
        PhotoMemoryEditorDialog(
            previewModel = resolvePhotoFile(target.localFileName),
            initialPhoto = target,
            defaultAgeMonth = target.ageMonth.toInt(),
            isSaving = isSaving,
            onDismiss = { editingPhoto = null },
            onSave = { title, caption, ageMonth, tag, dateLabel ->
                onUpdatePhoto(target.id, title, caption, ageMonth, tag, dateLabel)
                editingPhoto = null
            }
        )
    }

    viewingPhoto?.let { photo ->
        PhotoViewerDialog(
            photo = photo,
            imageFile = resolvePhotoFile(photo.localFileName),
            onDismiss = { viewingPhoto = null },
            onEdit = {
                viewingPhoto = null
                editingPhoto = photo
            },
            onDelete = {
                viewingPhoto = null
                deletingPhoto = photo
            }
        )
    }

    deletingPhoto?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingPhoto = null },
            title = { Text("ছবিটি মুছে ফেলবেন?") },
            text = { Text("“${target.title}” স্মৃতির অ্যালবাম থেকে মুছে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePhoto(target)
                        deletingPhoto = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingPhoto = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyPhotosCard(
    isFiltered: Boolean,
    onAddPhoto: () -> Unit
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_memories),
                contentDescription = "সোনামণির স্মৃতি অ্যালবাম",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Text(
                text = if (isFiltered) "এই মাসের কোনো ছবি এখনো যোগ করা হয়নি" else "প্রথম মিষ্টি স্মৃতিটি যোগ করুন",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "ফোনের গ্যালারি থেকে সোনামণির ছবি বেছে নিয়ে বয়স, মাইলস্টোন ও ছোট্ট গল্প লিখে রাখুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onAddPhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("empty_add_photo_button")
            ) {
                Icon(imageVector = Icons.Rounded.AddAPhoto, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_photo_label))
            }
        }
    }
}

@Composable
private fun PhotoMemoryCard(
    photo: BabyPhoto,
    imageFile: File?,
    onClick: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("photo_memory_card")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (imageFile != null) {
                    AsyncImage(
                        model = imageFile,
                        contentDescription = photo.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BrokenImage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "ছবিটি মূল ডিভাইসে রয়েছে",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "${photo.ageMonth.toInt().toBengaliDigits()} মাস",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = photo.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (photo.milestoneTag.isNotBlank()) {
                    Text(
                        text = "#${photo.milestoneTag}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PhotoMemoryEditorDialog(
    previewModel: Any?,
    initialPhoto: BabyPhoto?,
    defaultAgeMonth: Int,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, caption: String, ageMonth: Int, milestoneTag: String, dateLabel: String) -> Unit
) {
    var title by remember(initialPhoto) { mutableStateOf(initialPhoto?.title ?: "") }
    var caption by remember(initialPhoto) { mutableStateOf(initialPhoto?.caption ?: "") }
    var milestoneTag by remember(initialPhoto) { mutableStateOf(initialPhoto?.milestoneTag ?: "প্রথম হাসি") }
    var dateLabel by remember(initialPhoto) { mutableStateOf(initialPhoto?.dateLabel ?: "") }
    var ageFloat by remember(initialPhoto) {
        mutableFloatStateOf(
            (initialPhoto?.ageMonth?.toInt() ?: defaultAgeMonth).coerceIn(0, 12).toFloat()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialPhoto == null) "নতুন ছবির স্মৃতি" else "ছবির তথ্য সম্পাদনা",
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
                if (previewModel != null) {
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "নির্বাচিত ছবি",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 120) title = it },
                    label = { Text("ছবির শিরোনাম *") },
                    placeholder = { Text("যেমন: প্রথমবার মিষ্টি হাসি") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_title_input")
                )

                val ageInt = ageFloat.roundToInt()
                Text(
                    text = "শিশুর বয়স: ${ageInt.toBengaliDigits()} মাস",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = ageFloat,
                    onValueChange = { ageFloat = it },
                    valueRange = 0f..12f,
                    steps = 11
                )

                Text("মাইলস্টোন ট্যাগ", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BabyKnowledgeBase.milestoneTagsBn.forEach { tag ->
                        FilterChip(
                            selected = milestoneTag == tag,
                            onClick = { milestoneTag = tag },
                            label = { Text(tag) }
                        )
                    }
                }

                OutlinedTextField(
                    value = dateLabel,
                    onValueChange = { if (it.length <= 40) dateLabel = it },
                    label = { Text("তারিখ বা সময় (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ১২ নভেম্বর, ২০২৬") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = caption,
                    onValueChange = { if (it.length <= 500) caption = it },
                    label = { Text("স্মৃতির ছোট্ট গল্প (ঐচ্ছিক)") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("photo_caption_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        title,
                        caption,
                        ageFloat.roundToInt().coerceIn(0, 12),
                        milestoneTag,
                        dateLabel
                    )
                },
                enabled = title.trim().isNotEmpty() && !isSaving,
                modifier = Modifier.testTag("save_photo_button")
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

@Composable
private fun PhotoViewerDialog(
    photo: BabyPhoto,
    imageFile: File?,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (imageFile != null) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = photo.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(290.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.BrokenImage,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(50)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "বন্ধ করুন"
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = photo.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${photo.ageMonth.toInt().toBengaliDigits()} মাস বয়স",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        if (photo.milestoneTag.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = photo.milestoneTag,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (photo.dateLabel.isNotBlank()) {
                        Text(
                            text = "তারিখ: ${photo.dateLabel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (photo.caption.isNotBlank()) {
                        Text(
                            text = photo.caption,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = onEdit) {
                            Icon(imageVector = Icons.Rounded.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.edit))
                        }
                    }
                }
            }
        }
    }
}
