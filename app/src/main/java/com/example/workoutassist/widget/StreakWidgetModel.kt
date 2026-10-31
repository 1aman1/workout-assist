package com.example.workoutassist.widget

import com.example.workoutassist.ui.MILLIS_PER_DAY
import com.example.workoutassist.ui.computeRoutineStreak
import com.example.workoutassist.ui.startOfMonthEpochDay
import com.example.workoutassist.ui.streakBreakDays
import java.util.Calendar
import java.util.TimeZone

internal enum class WidgetDayStatus { DONE, FROZEN, MISSED, TODAY, FUTURE, NEUTRAL }

internal data class WidgetCalendarCell(
    val dayOfMonth: Int,
    val monthIndex: Int,
    val status: WidgetDayStatus,
    val isToday: Boolean
)

// Exactly one of streak/gap is non-zero while history exists: a gap only starts once
// yesterday is confirmed missed, mirroring how the streak graph treats a pending today.
internal data class WidgetStats(val streakDays: Int, val gapDays: Int, val breaksThisMonth: Int) {
    val isOnGap: Boolean get() = streakDays == 0 && gapDays > 0
}

internal const val WIDGET_MIN_WEEKS = 2
internal const val WIDGET_MAX_WEEKS = 6
internal const val WIDGET_WEEKDAY_HEADER_DP = 14f
private const val WIDGET_MIN_WEEK_ROW_DP = 24f

// Always at least two week rows so the current week plus the whole previous week (and thus
// the last few days, even on the first day of a week) stay visible; grows with height.
internal fun widgetWeekCountForHeight(calendarHeightDp: Float): Int {
    val rows = ((calendarHeightDp - WIDGET_WEEKDAY_HEADER_DP) / WIDGET_MIN_WEEK_ROW_DP).toInt()
    return rows.coerceIn(WIDGET_MIN_WEEKS, WIDGET_MAX_WEEKS)
}

internal fun computeWidgetStats(
    completedDays: Set<Long>,
    frozenDays: Set<Long>,
    todayEpochDay: Long
): WidgetStats {
    val activeDays = completedDays + frozenDays
    val streak = computeRoutineStreak(activeDays, todayEpochDay)
    val lastActiveDay = activeDays.maxOrNull()
    val gap = if (streak == 0 && lastActiveDay != null) {
        (todayEpochDay - 1L - lastActiveDay).toInt().coerceAtLeast(0)
    } else {
        0
    }
    val monthStart = startOfMonthEpochDay(todayEpochDay, 0)
    val breaksThisMonth = streakBreakDays(activeDays, todayEpochDay).count { it in monthStart..todayEpochDay }
    return WidgetStats(streakDays = streak, gapDays = gap, breaksThisMonth = breaksThisMonth)
}

// Week rows (oldest first) ending with the week that contains today.
internal fun buildWidgetWeeks(
    completedDays: Set<Long>,
    frozenDays: Set<Long>,
    todayEpochDay: Long,
    weekCount: Int,
    firstDayOfWeek: Int
): List<List<WidgetCalendarCell>> {
    val firstActiveDay = (completedDays + frozenDays).minOrNull()
    val offsetInWeek = Math.floorMod(calendarDayOfWeek(todayEpochDay) - firstDayOfWeek, 7)
    val currentWeekStart = todayEpochDay - offsetInWeek
    return (0 until weekCount).map { weekIndex ->
        val weekStart = currentWeekStart - 7L * (weekCount - 1 - weekIndex)
        (0 until 7).map { dayIndex ->
            val epochDay = weekStart + dayIndex
            val calendar = utcCalendar(epochDay)
            WidgetCalendarCell(
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                monthIndex = calendar.get(Calendar.MONTH),
                status = widgetDayStatus(epochDay, completedDays, frozenDays, todayEpochDay, firstActiveDay),
                isToday = epochDay == todayEpochDay
            )
        }
    }
}

private fun widgetDayStatus(
    epochDay: Long,
    completedDays: Set<Long>,
    frozenDays: Set<Long>,
    todayEpochDay: Long,
    firstActiveDay: Long?
): WidgetDayStatus = when {
    epochDay > todayEpochDay -> WidgetDayStatus.FUTURE
    epochDay in completedDays -> WidgetDayStatus.DONE
    epochDay in frozenDays -> WidgetDayStatus.FROZEN
    epochDay == todayEpochDay -> WidgetDayStatus.TODAY
    firstActiveDay != null && epochDay > firstActiveDay -> WidgetDayStatus.MISSED
    else -> WidgetDayStatus.NEUTRAL
}

// java.util.Calendar numbering (Sunday = 1 ... Saturday = 7); epoch day 0 was a Thursday.
internal fun calendarDayOfWeek(epochDay: Long): Int = Math.floorMod(epochDay + 4L, 7L).toInt() + 1

private fun utcCalendar(epochDay: Long): Calendar =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        timeInMillis = epochDay * MILLIS_PER_DAY
    }
