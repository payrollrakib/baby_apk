package com.example

import com.example.data.content.BabyKnowledgeBase
import com.example.data.model.AgeStage
import com.example.data.model.toBengaliDigits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BabyKnowledgeBaseTest {

    @Test
    fun `bengali digit conversion handles 0 to 12`() {
        assertEquals("০", 0.toBengaliDigits())
        assertEquals("৬", 6.toBengaliDigits())
        assertEquals("১২", 12.toBengaliDigits())
        assertEquals("০–১২ মাস", "0–12 মাস".toBengaliDigits())
    }

    @Test
    fun `age stage maps months 0 to 12 accurately`() {
        assertEquals(AgeStage.M0_3, AgeStage.fromMonths(0))
        assertEquals(AgeStage.M0_3, AgeStage.fromMonths(3))
        assertEquals(AgeStage.M4_6, AgeStage.fromMonths(4))
        assertEquals(AgeStage.M4_6, AgeStage.fromMonths(6))
        assertEquals(AgeStage.M7_9, AgeStage.fromMonths(7))
        assertEquals(AgeStage.M7_9, AgeStage.fromMonths(9))
        assertEquals(AgeStage.M10_12, AgeStage.fromMonths(10))
        assertEquals(AgeStage.M10_12, AgeStage.fromMonths(12))
    }

    @Test
    fun `starter todos and guidelines cover all 0 to 12 month stages`() {
        val todoStages = BabyKnowledgeBase.starterTodos.map { it.ageStage }.toSet()
        assertTrue(todoStages.containsAll(listOf(AgeStage.M0_3, AgeStage.M4_6, AgeStage.M7_9, AgeStage.M10_12)))

        val guideStages = BabyKnowledgeBase.guidelineTopics.map { it.ageStage }.toSet()
        assertTrue(guideStages.containsAll(listOf(AgeStage.M0_3, AgeStage.M4_6, AgeStage.M7_9, AgeStage.M10_12)))
        assertTrue(BabyKnowledgeBase.vaccineSchedule.isNotEmpty())
        assertTrue(BabyKnowledgeBase.dangerSigns.isNotEmpty())
    }
}
