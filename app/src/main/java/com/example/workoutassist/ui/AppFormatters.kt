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

internal fun yearMonthDayToEpochDay(year: Int, month: Int, dayOfMonth: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(year, month, dayOfMonth, 0, 0, 0)
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}

internal fun epochDayToYearMonthDay(epochDay: Long): Triple<Int, Int, Int> {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.timeInMillis = epochDay * MILLIS_PER_DAY
    return Triple(
        utc.get(Calendar.YEAR),
        utc.get(Calendar.MONTH),
        utc.get(Calendar.DAY_OF_MONTH)
    )
}

internal fun formatDateShort(epochDay: Long): String {
    val formatter = SimpleDateFormat("dd-MMM", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
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
