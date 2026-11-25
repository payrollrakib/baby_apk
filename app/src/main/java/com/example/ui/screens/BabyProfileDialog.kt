package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.BabyGender
import com.example.data.model.UserProfile
import com.example.data.model.toBengaliDigits
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BabyProfileDialog(
    profile: UserProfile?,
    userEmail: String?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, birthDateMillis: Long, gender: BabyGender, ageMonths: Int) -> Unit,
    onSignOut: () -> Unit
) {
    var babyName by remember(profile) {
        mutableStateOf(profile?.babyName?.takeIf { it.isNotBlank() } ?: "সোনামণি")
    }
    var selectedGender by remember(profile) {
        mutableStateOf(BabyGender.fromRaw(profile?.babyGender))
    }
    var ageMonthsFloat by remember(profile) {
        mutableFloatStateOf((profile?.babyAgeMonths ?: 0L).coerceIn(0L, 12L).toFloat())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.ChildCare,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "সোনামণির প্রোফাইল",
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
                    value = babyName,
                    onValueChange = { if (it.length <= 60) babyName = it },
                    label = { Text("শিশুর নাম বা ডাকনাম") },
                    placeholder = { Text("যেমন: আয়ান, তাহিয়া, সোনামণি") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("baby_name_input")
                )

                val ageInt = ageMonthsFloat.roundToInt()
                Text(
                    text = "বর্তমান বয়স: ${ageInt.toBengaliDigits()} মাস",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Slider(
                    value = ageMonthsFloat,
                    onValueChange = { ageMonthsFloat = it },
                    valueRange = 0f..12f,
                    steps = 11,
                    modifier = Modifier.testTag("baby_age_slider")
                )

                Text(
                    text = "শিশুর পরিচয়",
                    style = MaterialTheme.typography.labelLarge
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BabyGender.entries.forEach { gender ->
                        FilterChip(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender },
                            label = { Text(gender.labelBn) }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                if (!userEmail.isNullOrBlank()) {
                    Text(
                        text = "লগইন অ্যাকাউন্ট: $userEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.sign_out))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val ageInt = ageMonthsFloat.roundToInt().coerceIn(0, 12)
                    val birthApprox = profile?.babyBirthDateMillis?.takeIf { it > 0L }
                        ?: (System.currentTimeMillis() - ageInt * 30L * 24L * 60L * 60L * 1000L)
                    onSave(
                        babyName.trim().ifBlank { "সোনামণি" },
                        birthApprox,
                        selectedGender,
                        ageInt
                    )
                    onDismiss()
                },
                enabled = !isSaving,
                modifier = Modifier.testTag("save_profile_button")
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
