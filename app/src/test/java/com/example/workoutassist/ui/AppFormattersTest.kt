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
        // runs of 4, 3, 4 consecutive days separated by gaps. today = last active day.
        val days = setOf(1L, 2L, 3L, 4L, 10L, 11L, 12L, 20L, 21L, 22L, 23L)
        assertEquals(
            listOf(0, 1, 2, 3, 4, 0, 1, 2, 3, 0, 1, 2, 3, 4),
            buildStreakMomentumSeries(days, todayEpochDay = 23L)
        )
    }

    @Test
    fun buildStreakMomentumSeries_singleDayIsZeroThenOne() {
        assertEquals(listOf(0, 1), buildStreakMomentumSeries(setOf(5L), todayEpochDay = 5L))
    }

    @Test
    fun buildStreakMomentumSeries_emptyIsEmpty() {
        assertEquals(emptyList<Int>(), buildStreakMomentumSeries(emptySet(), todayEpochDay = 0L))
    }

    @Test
    fun buildStreakMomentumSeries_sortsUnorderedInput() {
        assertEquals(listOf(0, 1, 2, 3), buildStreakMomentumSeries(setOf(3L, 1L, 2L), todayEpochDay = 3L))
    }

    @Test
    fun buildStreakMomentumSeries_appendsBreakWhenRecentDayMissed() {
        // last active day 3, today 5 (day 4 = yesterday was missed) -> trailing 0 (red drop).
        assertEquals(listOf(0, 1, 2, 3, 0), buildStreakMomentumSeries(setOf(1L, 2L, 3L), todayEpochDay = 5L))
    }

    @Test
    fun buildStreakMomentumSeries_noBreakWhileTodayOrYesterdayActive() {
        // today == last active day (ongoing).
        assertEquals(listOf(0, 1, 2, 3), buildStreakMomentumSeries(setOf(1L, 2L, 3L), todayEpochDay = 3L))
        // yesterday active, today pending (today - last == 1) -> not a break yet.
        assertEquals(listOf(0, 1, 2, 3), buildStreakMomentumSeries(setOf(1L, 2L, 3L), todayEpochDay = 4L))
    }

    @Test
    fun streakRunLengths_groupsConsecutiveDays() {
        // runs of 3 (1..3), 1 (5), 2 (7..8).
        assertEquals(listOf(3, 1, 2), streakRunLengths(setOf(1L, 2L, 3L, 5L, 7L, 8L)))
    }

    @Test
    fun streakRunLengths_emptyIsEmpty() {
        assertEquals(emptyList<Int>(), streakRunLengths(emptySet()))
    }

    @Test
    fun streakLengthHistogram_countsPerBucket() {
        // runs: 1, 1, 2, 3 -> two 1-day, one 2-day, one 3-day.
        val days = setOf(1L, 3L, 5L, 6L, 8L, 9L, 10L)
        val histogram = streakLengthHistogram(days, maxBucket = 7)
        assertEquals(2, histogram[0]) // 1-day streaks
        assertEquals(1, histogram[1]) // 2-day streaks
        assertEquals(1, histogram[2]) // 3-day streaks
        assertEquals(0, histogram[3]) // 4-day streaks
    }

    @Test
    fun streakLengthHistogram_lastBucketAccumulatesLongRuns() {
        // a single 9-day run lands in the 7+ bucket (index 6).
        val days = (1L..9L).toSet()
        val histogram = streakLengthHistogram(days, maxBucket = 7)
        assertEquals(1, histogram[6])
        assertEquals(0, histogram[0])
    }

    @Test
    fun streakBreakDays_marksDayAfterEachEndedRun() {
        // runs 1..3 (break at 4) and 6..7 (break at 8 since today > 7).
        assertEquals(listOf(4L, 8L), streakBreakDays(setOf(1L, 2L, 3L, 6L, 7L), 10L))
    }

    @Test
    fun streakBreakDays_ongoingStreakTodayHasNoBreak() {
        assertEquals(emptyList<Long>(), streakBreakDays(setOf(5L), 5L))
        // yesterday active, today pending -> not a break yet.
        assertEquals(emptyList<Long>(), streakBreakDays(setOf(5L), 6L))
        // yesterday (6) missed -> break at 6.
        assertEquals(listOf(6L), streakBreakDays(setOf(5L), 7L))
    }

    @Test
    fun longestStreakGap_findsBiggestInnerGap() {
        assertEquals(3, longestStreakGap(setOf(1L, 5L, 6L), 6L))
    }

    @Test
    fun longestStreakGap_includesTrailingGap() {
        assertEquals(7, longestStreakGap(setOf(1L, 2L, 3L), 10L))
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
