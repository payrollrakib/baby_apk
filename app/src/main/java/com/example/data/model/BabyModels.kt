package com.example.data.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class BabyGender(val labelBn: String) {
    BOY("ছেলে"),
    GIRL("মেয়ে"),
    UNSPECIFIED("উল্লেখ নেই");

    companion object {
        fun fromRaw(raw: String?): BabyGender =
            entries.firstOrNull { it.name == raw } ?: UNSPECIFIED
    }
}

enum class AgeStage(val labelBn: String, val shortLabelBn: String) {
    ALL("সব বয়স (০–১২ মাস)", "সব"),
    M0_3("০–৩ মাস", "০–৩ মাস"),
    M4_6("৪–৬ মাস", "৪–৬ মাস"),
    M7_9("৭–৯ মাস", "৭–৯ মাস"),
    M10_12("১০–১২ মাস", "১০–১২ মাস");

    companion object {
        fun fromRaw(raw: String?): AgeStage =
            entries.firstOrNull { it.name == raw } ?: ALL

        fun fromMonths(months: Int): AgeStage = when (months) {
            in 0..3 -> M0_3
            in 4..6 -> M4_6
            in 7..9 -> M7_9
            else -> M10_12
        }
    }
}

enum class TodoCategory(val labelBn: String) {
    VACCINE("টিকা"),
    FEEDING("খাবার"),
    HEALTH("স্বাস্থ্য"),
    CARE("দৈনন্দিন যত্ন"),
    SHOPPING("প্রয়োজনীয় জিনিস");

    companion object {
        fun fromRaw(raw: String?): TodoCategory =
            entries.firstOrNull { it.name == raw } ?: CARE
    }
}

enum class NoteCategory(val labelBn: String) {
    DOCTOR("ডাক্তার ও ওষুধ"),
    FEEDING("খাবার"),
    SLEEP("ঘুম ও রুটিন"),
    MILESTONE("মাইলস্টোন"),
    GENERAL("সাধারণ");

    companion object {
        fun fromRaw(raw: String?): NoteCategory =
            entries.firstOrNull { it.name == raw } ?: GENERAL
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val babyName: String = "সোনামণি",
    val babyBirthDateMillis: Long = 0L,
    val babyGender: String = BabyGender.UNSPECIFIED.name,
    val babyAgeMonths: Long = 0L,
    val starterSeeded: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class TodoItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val notes: String = "",
    val ageStage: String = AgeStage.ALL.name,
    val category: String = TodoCategory.CARE.name,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BabyPhoto(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val caption: String = "",
    val ageMonth: Long = 0L,
    val milestoneTag: String = "",
    val localFileName: String = "",
    val dateLabel: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class BabyNote(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = NoteCategory.GENERAL.name,
    val ageMonth: Long = 0L,
    val pinned: Boolean = false,
    val localImageFileName: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class StarterTodoSeed(
    val title: String,
    val notes: String,
    val ageStage: AgeStage,
    val category: TodoCategory
)

data class GuidelineTopic(
    val id: String,
    val titleBn: String,
    val summaryBn: String,
    val ageStage: AgeStage,
    val pointsBn: List<String>,
    val doListBn: List<String>,
    val avoidListBn: List<String>,
    val quickTodoTitleBn: String? = null,
    val quickTodoCategory: TodoCategory = TodoCategory.CARE
)

data class VaccineEntry(
    val ageBn: String,
    val vaccineNameBn: String,
    val diseaseBn: String,
    val noteBn: String,
    val ageStage: AgeStage
)

data class DangerSignItem(
    val titleBn: String,
    val detailBn: String
)

fun Int.toBengaliDigits(): String = toString().toBengaliDigits()

fun String.toBengaliDigits(): String {
    val bengali = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
    val sb = StringBuilder(length)
    for (ch in this) {
        if (ch in '0'..'9') {
            sb.append(bengali[ch - '0'])
        } else {
            sb.append(ch)
        }
    }
    return sb.toString()
}
