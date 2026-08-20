package com.example.workoutassist.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the pure formatter/calculation helpers in AppFormatters.kt.
 * These run on the host JVM (no Android framework needed).
 */
class AppFormattersTest {

    @Test
    fun stripWeightUnit_removesKgSuffixAndTrims() {
        assertEquals("60", stripWeightUnit("60 kg"))
        assertEquals("60", stripWeightUnit("60kg"))
        assertEquals("60", stripWeightUnit("  60 KG "))
        assertEquals("12.5", stripWeightUnit("12.5 Kg"))
    }

    @Test
    fun stripWeightUnit_leavesValuesWithoutUnit() {
        assertEquals("60", stripWeightUnit("60"))
        assertEquals("", stripWeightUnit(""))
        assertEquals("bodyweight", stripWeightUnit("bodyweight"))
    }

    @Test
    fun stripWeightUnit_onlyStripsTrailingKg() {
        // "kg" not at the end is preserved.
        assertEquals("kg press 40", stripWeightUnit("kg press 40"))
    }

    @Test
    fun completedInWindow_countsDaysInsideInclusiveWindow() {
        val today = 100L
        val done = setOf(100L, 99L, 98L)
        assertEquals(3, completedInWindow(done, today, 7))
    }

    @Test
    fun completedInWindow_windowOfOneCountsOnlyToday() {
        val today = 100L
        assertEquals(1, completedInWindow(setOf(100L, 99L), today, 1))
        assertEquals(0, completedInWindow(setOf(99L), today, 1))
    }

    @Test
    fun completedInWindow_includesStartBoundaryExcludesOutside() {
        val today = 100L
        // window 7 => start day = 94, inclusive.
        assertEquals(1, completedInWindow(setOf(94L), today, 7))
        assertEquals(0, completedInWindow(setOf(93L), today, 7))
    }

    @Test
    fun completedInWindow_emptySetIsZero() {
        assertEquals(0, completedInWindow(emptySet(), 100L, 30))
    }

    @Test
    fun computeRoutineStreak_countsConsecutiveEndingToday() {
        assertEquals(3, computeRoutineStreak(setOf(100L, 99L, 98L), 100L))
    }

    @Test
    fun computeRoutineStreak_startsYesterdayWhenTodayMissing() {
        // Today not logged yet: streak is measured from yesterday backwards.
        assertEquals(2, computeRoutineStreak(setOf(99L, 98L), 100L))
    }

    @Test
    fun computeRoutineStreak_gapBreaksStreak() {
        // 98 is missing, so only 100 and 99 count.
        assertEquals(2, computeRoutineStreak(setOf(100L, 99L, 97L), 100L))
    }

    @Test
    fun computeRoutineStreak_zeroWhenTodayAndYesterdayMissing() {
        assertEquals(0, computeRoutineStreak(setOf(98L, 97L), 100L))
        assertEquals(0, computeRoutineStreak(emptySet(), 100L))
    }

    @Test
    fun computeRoutineStreak_onlyTodayIsOne() {
        assertEquals(1, computeRoutineStreak(setOf(100L), 100L))
    }

    @Test
    fun buildStreakMomentumSeries_emitsZeroThenClimbPerRun() {
        // runs of 4, 3, 4 consecutive days separated by gaps.
        val days = setOf(1L, 2L, 3L, 4L, 10L, 11L, 12L, 20L, 21L, 22L, 23L)
        assertEquals(
            listOf(0, 1, 2, 3, 4, 0, 1, 2, 3, 0, 1, 2, 3, 4),
            buildStreakMomentumSeries(days)
        )
    }

    @Test
    fun buildStreakMomentumSeries_singleDayIsZeroThenOne() {
        assertEquals(listOf(0, 1), buildStreakMomentumSeries(setOf(5L)))
    }

    @Test
    fun buildStreakMomentumSeries_emptyIsEmpty() {
        assertEquals(emptyList<Int>(), buildStreakMomentumSeries(emptySet()))
    }

    @Test
    fun buildStreakMomentumSeries_sortsUnorderedInput() {
        assertEquals(listOf(0, 1, 2, 3), buildStreakMomentumSeries(setOf(3L, 1L, 2L)))
    }

    @Test
    fun parseWeightValue_extractsLeadingNumber() {
        assertEquals(60f, parseWeightValue("60 kg"))
        assertEquals(12.5f, parseWeightValue("12.5 kg"))
        assertEquals(30.25f, parseWeightValue("weight 30.25 kg"))
    }

    @Test
    fun parseWeightValue_nullWhenNoNumber() {
        assertNull(parseWeightValue("bodyweight"))
        assertNull(parseWeightValue(""))
    }

    @Test
    fun formatStopwatch_formatsMinutesAndSeconds() {
        assertEquals("0:00", formatStopwatch(0))
        assertEquals("0:05", formatStopwatch(5))
        assertEquals("1:05", formatStopwatch(65))
        assertEquals("10:00", formatStopwatch(600))
    }

    @Test
    fun formatStopwatch_clampsNegativeToZero() {
        assertEquals("0:00", formatStopwatch(-10))
    }
}
