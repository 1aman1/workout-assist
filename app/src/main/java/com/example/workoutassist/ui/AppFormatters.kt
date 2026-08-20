package com.example.workoutassist.ui

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToInt

internal const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

internal fun currentDateEpochDay(): Long {
    val local = Calendar.getInstance()
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}

internal fun formatDateShort(epochDay: Long): String {
    val formatter = SimpleDateFormat("dd-MMM", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
}

internal fun formatStopwatch(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    return "%d:%02d".format(safe / 60, safe % 60)
}

internal fun timestampMillisToEpochDay(timestampMillis: Long): Long {
    val local = Calendar.getInstance().apply {
        timeInMillis = timestampMillis
    }
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(
        local.get(Calendar.YEAR),
        local.get(Calendar.MONTH),
        local.get(Calendar.DAY_OF_MONTH),
        0,
        0,
        0
    )
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}

internal fun stripWeightUnit(raw: String): String =
    raw.trim().replace(Regex("\\s*kg\\s*$", RegexOption.IGNORE_CASE), "").trim()

// Count completed days within the last [windowDays] days, inclusive of today.
internal fun completedInWindow(
    completedDays: Set<Long>,
    todayEpochDay: Long,
    windowDays: Int
): Int {
    val startDay = todayEpochDay - (windowDays - 1).toLong()
    return completedDays.count { day -> day in startDay..todayEpochDay }
}

// Consecutive most-recent days that each have a completed session. Today counts if
// present; otherwise the streak is measured up to yesterday so an in-progress day
// isn't penalized.
internal fun computeRoutineStreak(completedDays: Set<Long>, todayEpochDay: Long): Int {
    var cursor = if (todayEpochDay in completedDays) todayEpochDay else todayEpochDay - 1L
    var streak = 0
    while (cursor in completedDays) {
        streak++
        cursor -= 1L
    }
    return streak
}

// Streak-momentum series: each maximal run of consecutive completed days is emitted as
// a 0 (the break/reset) followed by 1..runLength (the climb), so missed-day gaps read as
// drops to zero. Example: runs of 4, 3, 4 days -> [0,1,2,3,4, 0,1,2,3, 0,1,2,3,4].
internal fun buildStreakMomentumSeries(completedDays: Set<Long>, todayEpochDay: Long): List<Int> {
    if (completedDays.isEmpty()) return emptyList()
    val sortedDays = completedDays.toSortedSet().toList()
    val series = mutableListOf<Int>()
    var runLength = 0
    var previousDay: Long? = null
    for (day in sortedDays) {
        val continuesRun = previousDay != null && day == previousDay + 1L
        if (continuesRun) {
            runLength += 1
        } else {
            series.add(0)
            runLength = 1
        }
        series.add(runLength)
        previousDay = day
    }
    // If the most recent run has already broken (a full day was missed before today, e.g.
    // yesterday), append the drop to zero so the latest break shows as a red tick. Today
    // being unlogged (last day == today or yesterday) is not yet a break.
    if (todayEpochDay - sortedDays.last() >= 2L) {
        series.add(0)
    }
    return series
}

// Lengths of each maximal run of consecutive completed days, e.g. days {1,2,3, 5} -> [3, 1].
internal fun streakRunLengths(completedDays: Set<Long>): List<Int> {
    if (completedDays.isEmpty()) return emptyList()
    val sortedDays = completedDays.toSortedSet().toList()
    val runs = mutableListOf<Int>()
    var runLength = 1
    for (i in 1 until sortedDays.size) {
        if (sortedDays[i] == sortedDays[i - 1] + 1L) {
            runLength++
        } else {
            runs.add(runLength)
            runLength = 1
        }
    }
    runs.add(runLength)
    return runs
}

// Histogram of streak run lengths bucketed 1..maxBucket; the last bucket accumulates runs
// of length >= maxBucket. Returns counts where index i corresponds to a run length of i + 1.
internal fun streakLengthHistogram(completedDays: Set<Long>, maxBucket: Int = 7): List<Int> {
    val buckets = maxBucket.coerceAtLeast(1)
    val counts = IntArray(buckets)
    streakRunLengths(completedDays).forEach { length ->
        val index = length.coerceIn(1, buckets) - 1
        counts[index] += 1
    }
    return counts.toList()
}

// Epoch days on which a streak break occurred: the first missed day after each run that
// has already ended. A run whose last day is today or yesterday has not broken yet (today
// may still be logged), so its trailing gap is only counted once yesterday is confirmed missed.
internal fun streakBreakDays(completedDays: Set<Long>, todayEpochDay: Long): List<Long> {
    if (completedDays.isEmpty()) return emptyList()
    val sortedDays = completedDays.toSortedSet().toList()
    val breaks = mutableListOf<Long>()
    for (i in 1 until sortedDays.size) {
        if (sortedDays[i] != sortedDays[i - 1] + 1L) {
            breaks.add(sortedDays[i - 1] + 1L)
        }
    }
    val lastDay = sortedDays.last()
    if (lastDay < todayEpochDay - 1L) {
        breaks.add(lastDay + 1L)
    }
    return breaks
}

// The longest gap (in days) between streaks, including any ongoing gap since the last
// active day up to today.
internal fun longestStreakGap(completedDays: Set<Long>, todayEpochDay: Long): Int {
    if (completedDays.isEmpty()) return 0
    val sortedDays = completedDays.toSortedSet().toList()
    var longest = 0
    for (i in 1 until sortedDays.size) {
        val gap = (sortedDays[i] - sortedDays[i - 1] - 1L).toInt()
        if (gap > longest) longest = gap
    }
    val trailingGap = (todayEpochDay - sortedDays.last()).toInt()
    if (trailingGap > longest) longest = trailingGap
    return longest.coerceAtLeast(0)
}

// The epoch day of the first of the month, [monthsBack] calendar months before the month
// containing [epochDay]. Used to bucket events into calendar-month windows.
internal fun startOfMonthEpochDay(epochDay: Long, monthsBack: Int = 0): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.timeInMillis = epochDay * MILLIS_PER_DAY
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.add(Calendar.MONTH, -monthsBack.coerceAtLeast(0))
    return cal.timeInMillis / MILLIS_PER_DAY
}

internal fun parseWeightValue(text: String): Float? {
    return Regex("\\d+(?:\\.\\d+)?")
        .find(text)
        ?.value
        ?.toFloatOrNull()
}

@Suppress("DEPRECATION")
internal fun currentAppVersionName(context: Context): String {
    return runCatching {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
    }
        .getOrDefault("")
        .ifBlank { LATEST_DESIGN_VERSION }
}

internal fun resolveThemeColorOption(
    options: List<ThemeColorOption>,
    selectedId: String,
    fallbackId: String
): ThemeColorOption {
    return options.firstOrNull { it.id == selectedId }
        ?: options.firstOrNull { it.id == fallbackId }
        ?: options.first()
}

internal fun parseThemeHexColorOrDefault(hexValue: String?, fallback: Color): Color {
    return parseThemeHexColor(hexValue) ?: fallback
}

internal fun parseThemeHexColor(hexValue: String?): Color? {
    val normalized = hexValue
        ?.trim()
        ?.removePrefix("#")
        ?: return null

    val rawValue = normalized.toLongOrNull(16) ?: return null
    return when (normalized.length) {
        6 -> {
            val red = ((rawValue shr 16) and 0xFF).toInt()
            val green = ((rawValue shr 8) and 0xFF).toInt()
            val blue = (rawValue and 0xFF).toInt()
            colorFromRgb(red = red, green = green, blue = blue)
        }

        8 -> {
            val alpha = ((rawValue shr 24) and 0xFF).toInt()
            val red = ((rawValue shr 16) and 0xFF).toInt()
            val green = ((rawValue shr 8) and 0xFF).toInt()
            val blue = (rawValue and 0xFF).toInt()
            Color(
                red = red / 255f,
                green = green / 255f,
                blue = blue / 255f,
                alpha = alpha / 255f
            )
        }

        else -> null
    }
}

internal fun colorFromRgb(red: Int, green: Int, blue: Int): Color {
    return Color(
        red = red.coerceIn(0, 255) / 255f,
        green = green.coerceIn(0, 255) / 255f,
        blue = blue.coerceIn(0, 255) / 255f,
        alpha = 1f
    )
}

internal fun colorToHexRgb(color: Color): String {
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format(Locale.ENGLISH, "#%02X%02X%02X", red, green, blue)
}

internal fun mixWithWhite(base: Color, whiteAmount: Float): Color {
    val t = whiteAmount.coerceIn(0f, 1f)
    return Color(
        red = base.red + (1f - base.red) * t,
        green = base.green + (1f - base.green) * t,
        blue = base.blue + (1f - base.blue) * t,
        alpha = 1f
    )
}

internal fun contrastColor(color: Color): Color {
    return if (color.luminance() > 0.52f) {
        Color(0xFF0F1720)
    } else {
        Color(0xFFFFFFFF)
    }
}

internal fun formatHalfKgValue(halfKgStep: Int): String {
    return formatKgValue(halfKgStep / 2f)
}

internal fun formatKgValue(weightKg: Float): String {
    val roundedToSingleDecimal = (weightKg * 10f).roundToInt() / 10f
    return if (roundedToSingleDecimal % 1f == 0f) {
        roundedToSingleDecimal.toInt().toString()
    } else {
        String.format(Locale.ENGLISH, "%.1f", roundedToSingleDecimal)
    }
}
