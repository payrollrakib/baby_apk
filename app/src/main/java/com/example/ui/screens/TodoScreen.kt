package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.AgeStage
import com.example.data.model.TodoCategory
import com.example.data.model.TodoItem
import com.example.data.model.UserProfile
import com.example.data.model.toBengaliDigits
import com.example.ui.viewmodel.UiState

private enum class TodoStatusFilter(val labelBn: String) {
    ALL("সব কাজ"),
    PENDING("বাকি আছে"),
    DONE("সম্পন্ন")
}

@Composable
fun TodoScreen(
    profileState: UiState<UserProfile?>,
    todosState: UiState<List<TodoItem>>,
    isSaving: Boolean,
    onOpenProfile: () -> Unit,
    onSeedStarter: () -> Unit,
    onAddTodo: (title: String, notes: String, ageStage: AgeStage, category: TodoCategory) -> Unit,
    onToggleTodo: (TodoItem) -> Unit,
    onUpdateTodo: (todoId: String, title: String, notes: String, ageStage: AgeStage, category: TodoCategory, completed: Boolean) -> Unit,
    onDeleteTodo: (String) -> Unit
) {
    var selectedStage by remember { mutableStateOf(AgeStage.ALL) }
    var statusFilter by remember { mutableStateOf(TodoStatusFilter.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }
    var deletingTodo by remember { mutableStateOf<TodoItem?>(null) }

    val profile = (profileState as? UiState.Success)?.data
    val todosList = (todosState as? UiState.Success)?.data.orEmpty()

    val filteredTodos = remember(todosList, selectedStage, statusFilter) {
        todosList.filter { item ->
            val stageMatch = selectedStage == AgeStage.ALL ||
                item.ageStage == selectedStage.name ||
                item.ageStage == AgeStage.ALL.name
            val statusMatch = when (statusFilter) {
                TodoStatusFilter.ALL -> true
                TodoStatusFilter.PENDING -> !item.completed
                TodoStatusFilter.DONE -> item.completed
            }
            stageMatch && statusMatch
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_todo_fab")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_todo_label)
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
                BabyHeroSummaryCard(
                    profile = profile,
                    totalCount = todosList.size,
                    completedCount = todosList.count { it.completed },
                    onEditProfile = onOpenProfile
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AgeStage.entries.forEach { stage ->
                            FilterChip(
                                selected = selectedStage == stage,
                                onClick = { selectedStage = stage },
                                label = { Text(stage.shortLabelBn) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TodoStatusFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = statusFilter == filter,
                                onClick = { statusFilter = filter },
                                label = { Text(filter.labelBn) }
                            )
                        }
                    }
                }
            }

            when (todosState) {
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
                        ErrorBannerCard(message = todosState.message)
                    }
                }

                is UiState.Success -> {
                    if (todosList.isEmpty()) {
                        item {
                            StarterChecklistPromptCard(
                                isSaving = isSaving,
                                onSeedStarter = onSeedStarter,
                                onAddCustom = { showAddDialog = true }
                            )
                        }
                    } else {
                        if (profile?.starterSeeded != true) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "০–১২ মাসের আদর্শ তালিকা যোগ করবেন?",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "টিকা, খাবার ও যত্নের ১৮টি জরুরি কাজ একসাথে যোগ করুন।",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                        TextButton(
                                            onClick = onSeedStarter,
                                            enabled = !isSaving,
                                            modifier = Modifier.testTag("seed_starter_inline_btn")
                                        ) {
                                            Text("যোগ করুন")
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredTodos.isEmpty()) {
                            item {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "এই ফিল্টারে কোনো করণীয় কাজ পাওয়া যায়নি। নতুন কাজ যোগ করতে '+' বাটনে ট্যাপ করুন।",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(20.dp)
                                    )
                                }
                            }
                        } else {
                            items(filteredTodos, key = { it.id }) { item ->
                                TodoItemCard(
                                    item = item,
                                    onToggle = { onToggleTodo(item) },
                                    onEdit = { editingTodo = item },
                                    onDelete = { deletingTodo = item }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TodoEditorDialog(
            initialItem = null,
            defaultStage = if (selectedStage == AgeStage.ALL) {
                AgeStage.fromMonths((profile?.babyAgeMonths ?: 0L).toInt())
            } else {
                selectedStage
            },
            isSaving = isSaving,
            onDismiss = { showAddDialog = false },
            onSave = { title, notes, stage, category ->
                onAddTodo(title, notes, stage, category)
                showAddDialog = false
            }
        )
    }

    editingTodo?.let { target ->
        TodoEditorDialog(
            initialItem = target,
            defaultStage = AgeStage.fromRaw(target.ageStage),
            isSaving = isSaving,
            onDismiss = { editingTodo = null },
            onSave = { title, notes, stage, category ->
                onUpdateTodo(target.id, title, notes, stage, category, target.completed)
                editingTodo = null
            }
        )
    }

    deletingTodo?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingTodo = null },
            title = { Text("কাজটি মুছে ফেলবেন?") },
            text = { Text("“${target.title}” তালিকা থেকে স্থায়ীভাবে মুছে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTodo(target.id)
                        deletingTodo = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingTodo = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun BabyHeroSummaryCard(
    profile: UserProfile?,
    totalCount: Int,
    completedCount: Int,
    onEditProfile: () -> Unit
) {
    val babyName = profile?.babyName?.takeIf { it.isNotBlank() } ?: "সোনামণি"
    val ageMonths = (profile?.babyAgeMonths ?: 0L).toInt().coerceIn(0, 12)
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = babyName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "বয়স: ${ageMonths.toBengaliDigits()} মাস (${AgeStage.fromMonths(ageMonths).labelBn})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    Surface(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        modifier = Modifier.testTag("edit_baby_profile_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = stringResource(R.string.edit_baby_profile),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "প্রোফাইল",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "অগ্রগতি",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${totalCount.toBengaliDigits()}টির মধ্যে ${completedCount.toBengaliDigits()}টি সম্পন্ন",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StarterChecklistPromptCard(
    isSaving: Boolean,
    onSeedStarter: () -> Unit,
    onAddCustom: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_hero_baby),
                contentDescription = "সোনামণি করণীয়",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Text(
                text = "০ থেকে ১ বছরের সম্পূর্ণ চেকলিস্ট শুরু করুন",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "টিকা, মায়ের দুধ ও বাড়তি খাবার, নাভির যত্ন এবং প্রতিটি মাসের জরুরি কাজগুলো এক ক্লিকেই আপনার তালিকায় যুক্ত করুন।",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onSeedStarter,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("seed_starter_todos_button")
            ) {
                Icon(imageVector = Icons.Rounded.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("আদর্শ ১৮টি করণীয় যোগ করুন")
            }
            OutlinedButton(
                onClick = onAddCustom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("নিজের মতো নতুন কাজ লিখুন")
            }
        }
    }
}

@Composable
private fun TodoItemCard(
    item: TodoItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stage = AgeStage.fromRaw(item.ageStage)
    val category = TodoCategory.fromRaw(item.category)
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (item.completed) {
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.65f)
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("todo_item_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (item.completed) {
                            Icons.Rounded.CheckCircle
                        } else {
                            Icons.Rounded.RadioButtonUnchecked
                        },
                        contentDescription = if (item.completed) "অসম্পন্ন করুন" else "সম্পন্ন করুন",
                        tint = if (item.completed) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 6.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (item.completed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        textDecoration = if (item.completed) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = category.labelBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = stage.shortLabelBn,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = expanded && item.notes.isNotBlank()) {
                        Text(
                            text = item.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Column {
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
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodoEditorDialog(
    initialItem: TodoItem?,
    defaultStage: AgeStage,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, notes: String, stage: AgeStage, category: TodoCategory) -> Unit
) {
    var title by remember(initialItem) { mutableStateOf(initialItem?.title ?: "") }
    var notes by remember(initialItem) { mutableStateOf(initialItem?.notes ?: "") }
    var selectedStage by remember(initialItem) { mutableStateOf(defaultStage) }
    var selectedCategory by remember(initialItem) {
        mutableStateOf(TodoCategory.fromRaw(initialItem?.category))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "নতুন করণীয় যোগ করুন" else "করণীয় সম্পাদনা করুন",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 200) title = it },
                    label = { Text("কাজের নাম *") },
                    placeholder = { Text("যেমন: ১০ সপ্তাহের টিকা নেওয়া") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todo_title_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 500) notes = it },
                    label = { Text("বিস্তারিত বা মনে রাখার কথা (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: টিকা কার্ড ও রুমাল সাথে নিতে হবে") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("todo_notes_input")
                )

                Text("বয়সের ধাপ", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AgeStage.entries.forEach { stage ->
                        FilterChip(
                            selected = selectedStage == stage,
                            onClick = { selectedStage = stage },
                            label = { Text(stage.shortLabelBn) }
                        )
                    }
                }

                Text("ধরন / ক্যাটাগরি", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TodoCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.labelBn) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, notes, selectedStage, selectedCategory) },
                enabled = title.trim().isNotEmpty() && !isSaving,
                modifier = Modifier.testTag("save_todo_button")
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
fun ErrorBannerCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(16.dp)
        )
    }
}
