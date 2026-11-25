package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.content.BabyKnowledgeBase
import com.example.data.model.AgeStage
import com.example.data.model.DangerSignItem
import com.example.data.model.GuidelineTopic
import com.example.data.model.TodoCategory
import com.example.data.model.VaccineEntry

private enum class GuidelineTab(val labelBn: String) {
    AGE_GUIDE("বয়সভিত্তিক গাইড"),
    VACCINES("টিকা সূচি"),
    DANGER_SIGNS("জরুরি সতর্কতা")
}

@Composable
fun GuidelineScreen(
    onAddQuickTodo: (title: String, notes: String, stage: AgeStage, category: TodoCategory) -> Unit
) {
    var selectedSection by remember { mutableStateOf(GuidelineTab.AGE_GUIDE) }
    var stageFilter by remember { mutableStateOf(AgeStage.ALL) }

    val filteredGuides = remember(stageFilter) {
        if (stageFilter == AgeStage.ALL) BabyKnowledgeBase.guidelineTopics
        else BabyKnowledgeBase.guidelineTopics.filter { it.ageStage == stageFilter }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "প্যারেন্টিং গাইডলাইন (০–১ বছর)",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "নবজাতক থেকে ১২ মাস পর্যন্ত শিশুর খাবার, ঘুম, বিকাশ, টিকা ও সতর্কতা সম্পর্কে নির্ভরযোগ্য বাংলা নির্দেশিকা।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GuidelineTab.entries.forEach { tab ->
                        FilterChip(
                            selected = selectedSection == tab,
                            onClick = { selectedSection = tab },
                            label = { Text(tab.labelBn) },
                            modifier = Modifier.testTag("guideline_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }

        when (selectedSection) {
            GuidelineTab.AGE_GUIDE -> {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AgeStage.entries.forEach { stage ->
                            FilterChip(
                                selected = stageFilter == stage,
                                onClick = { stageFilter = stage },
                                label = { Text(stage.shortLabelBn) }
                            )
                        }
                    }
                }

                items(filteredGuides, key = { it.id }) { topic ->
                    GuidelineTopicCard(
                        topic = topic,
                        onAddQuickTodo = { title ->
                            onAddQuickTodo(
                                title,
                                topic.summaryBn,
                                topic.ageStage,
                                topic.quickTodoCategory
                            )
                        }
                    )
                }
            }

            GuidelineTab.VACCINES -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Vaccines,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "শিশুর টিকা কার্ড সবসময় যত্ন করে রাখুন এবং যেকোনো ডোজ মিস হলে নিকটস্থ স্বাস্থ্যকেন্দ্রে যোগাযোগ করুন।",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                items(BabyKnowledgeBase.vaccineSchedule) { entry ->
                    VaccineScheduleCard(
                        entry = entry,
                        onAddVaccineTodo = {
                            onAddQuickTodo(
                                "${entry.ageBn}: ${entry.vaccineNameBn}",
                                "রোগ প্রতিরোধ: ${entry.diseaseBn}। ${entry.noteBn}",
                                entry.ageStage,
                                TodoCategory.VACCINE
                            )
                        }
                    )
                }
            }

            GuidelineTab.DANGER_SIGNS -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.MedicalServices,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "নিচের যেকোনো একটি বিপদ চিহ্ন দেখা দিলে দেরি না করে দ্রুত শিশু বিশেষজ্ঞ বা হাসপাতালে নিন।",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                items(BabyKnowledgeBase.dangerSigns) { item ->
                    DangerSignCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun GuidelineTopicCard(
    topic: GuidelineTopic,
    onAddQuickTodo: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("guideline_card_${topic.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = topic.ageStage.shortLabelBn,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = topic.titleBn,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "সংকুচিত করুন" else "বিস্তারিত দেখুন"
                )
            }

            Text(
                text = topic.summaryBn,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topic.pointsBn.forEach { point ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "যা অবশ্যই করবেন",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            topic.doListBn.forEach { doItem ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = doItem,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "যা এড়িয়ে চলবেন",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            topic.avoidListBn.forEach { avoidItem ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Rounded.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = avoidItem,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    topic.quickTodoTitleBn?.let { quickTodo ->
                        OutlinedButton(
                            onClick = { onAddQuickTodo(quickTodo) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.AddTask, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("করণীয় তালিকায় যোগ করুন")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VaccineScheduleCard(
    entry: VaccineEntry,
    onAddVaccineTodo: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = entry.ageBn,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Text(
                text = entry.vaccineNameBn,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "সুরক্ষা: ${entry.diseaseBn}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "পরামর্শ: ${entry.noteBn}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedButton(
                onClick = onAddVaccineTodo,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.Rounded.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("টিকা করণীয়তে যোগ করুন")
            }
        }
    }
}

@Composable
private fun DangerSignCard(item: DangerSignItem) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.ReportProblem,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.titleBn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.detailBn,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
