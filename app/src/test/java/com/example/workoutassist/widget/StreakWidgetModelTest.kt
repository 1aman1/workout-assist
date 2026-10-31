package com.example.workoutassist.widget

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakWidgetModelTest {
    // 2026-10-31, a Saturday.
    private val today = 20757L

    @Test
    fun calendarDayOfWeek_matchesKnownDates() {
        assertEquals(Calendar.THURSDAY, calendarDayOfWeek(0L))
        assertEquals(Calendar.SATURDAY, calendarDayOfWeek(today))
    }

    @Test
    fun stats_streakIncludingToday() {
        val stats = computeWidgetStats(setOf(today - 2, today - 1, today), emptySet(), today)
        assertEquals(3, stats.streakDays)
        assertEquals(0, stats.gapDays)
        assertEquals(0, stats.breaksThisMonth)
        assertFalse(stats.isOnGap)
    }

    @Test
    fun stats_unloggedTodayDoesNotBreakStreak() {
        val stats = computeWidgetStats(setOf(today - 2, today - 1), emptySet(), today)
        assertEquals(2, stats.streakDays)
        assertEquals(0, stats.gapDays)
    }

    @Test
    fun stats_gapCountsConfirmedMissedDaysAndOneBreak() {
        val stats = computeWidgetStats(setOf(today - 5), emptySet(), today)
        assertEquals(0, stats.streakDays)
        assertEquals(4, stats.gapDays)
        assertEquals(1, stats.breaksThisMonth)
        assertTrue(stats.isOnGap)
    }

    @Test
    fun stats_frozenDayBridgesStreak() {
        val stats = computeWidgetStats(setOf(today - 3, today - 1), setOf(today - 2), today)
        assertEquals(3, stats.streakDays)
        assertEquals(0, stats.breaksThisMonth)
    }

    @Test
    fun stats_noHistoryIsAllZero() {
        val stats = computeWidgetStats(emptySet(), emptySet(), today)
        assertEquals(WidgetStats(0, 0, 0), stats)
    }

    @Test
    fun weeks_endWithCurrentWeekAndStartOnConfiguredDay() {
        val weeks = buildWidgetWeeks(setOf(today - 3), emptySet(), today, weekCount = 2, firstDayOfWeek = Calendar.MONDAY)
        assertEquals(2, weeks.size)
        assertTrue(weeks.all { it.size == 7 })
        // Monday of the previous week is 12 days before Saturday 2026-10-31 (Oct 19).
        assertEquals(19, weeks[0][0].dayOfMonth)
        assertEquals(WidgetDayStatus.TODAY, weeks[1][5].status)
        assertEquals(WidgetDayStatus.FUTURE, weeks[1][6].status)
    }

    @Test
    fun weeks_statusesRelativeToFirstActiveDay() {
        val weeks = buildWidgetWeeks(setOf(today - 3), emptySet(), today, weekCount = 2, firstDayOfWeek = Calendar.MONDAY)
        assertEquals(WidgetDayStatus.NEUTRAL, weeks[1][1].status)
        assertEquals(WidgetDayStatus.DONE, weeks[1][2].status)
        assertEquals(WidgetDayStatus.MISSED, weeks[1][3].status)
    }

    @Test
    fun weekCount_growsWithHeightWithinBounds() {
        assertEquals(2, widgetWeekCountForHeight(40f))
        assertEquals(2, widgetWeekCountForHeight(71f))
        assertEquals(5, widgetWeekCountForHeight(150f))
        assertEquals(6, widgetWeekCountForHeight(400f))
    }
}
