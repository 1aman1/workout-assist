package com.example.workoutassist.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import java.text.DateFormatSymbols

internal data class WidgetPalette(
    val done: Int,
    val frozen: Int,
    val pending: Int,
    val missed: Int = 0xFFDC2626.toInt(),
    val text: Int = 0xFF102028.toInt(),
    val mutedText: Int = 0xFF7A8791.toInt()
)

private const val CELL_RADIUS_FRACTION = 0.44f

// Draws a weekday header plus one row per week into a transparent bitmap sized to the
// widget's calendar area, so the cells fill the space exactly instead of letterboxing.
internal fun renderCalendarBitmap(
    widthPx: Int,
    heightPx: Int,
    density: Float,
    weeks: List<List<WidgetCalendarCell>>,
    firstDayOfWeek: Int,
    palette: WidgetPalette
): Bitmap {
    val bitmap = Bitmap.createBitmap(widthPx.coerceAtLeast(1), heightPx.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
    if (weeks.isEmpty()) return bitmap
    val canvas = Canvas(bitmap)
    val headerHeight = WIDGET_WEEKDAY_HEADER_DP * density
    val columnWidth = widthPx / 7f
    val rowHeight = (heightPx - headerHeight) / weeks.size
    val radius = minOf(columnWidth, rowHeight) * CELL_RADIUS_FRACTION

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = (radius * 0.16f).coerceAtLeast(1f)
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    drawWeekdayHeader(canvas, columnWidth, headerHeight, firstDayOfWeek, palette.mutedText, textPaint)

    val monthNames = DateFormatSymbols.getInstance().shortMonths
    weeks.forEachIndexed { weekIndex, week ->
        val centerY = headerHeight + rowHeight * weekIndex + rowHeight / 2f
        week.forEachIndexed { dayIndex, cell ->
            val centerX = columnWidth * dayIndex + columnWidth / 2f
            val fillColor = when (cell.status) {
                WidgetDayStatus.DONE -> palette.done
                WidgetDayStatus.FROZEN -> palette.frozen
                WidgetDayStatus.MISSED -> palette.missed
                else -> null
            }
            if (fillColor != null) {
                fillPaint.color = fillColor
                canvas.drawCircle(centerX, centerY, radius, fillPaint)
            }
            if (cell.status == WidgetDayStatus.TODAY) {
                ringPaint.color = palette.pending
                canvas.drawCircle(centerX, centerY, radius - ringPaint.strokeWidth / 2f, ringPaint)
            } else if (cell.isToday) {
                ringPaint.color = palette.text
                canvas.drawCircle(centerX, centerY, radius + ringPaint.strokeWidth / 2f, ringPaint)
            }

            // The first of a month shows the month name so a week spanning two months reads clearly.
            val isMonthStart = cell.dayOfMonth == 1
            val label = if (isMonthStart) monthNames[cell.monthIndex].take(3) else cell.dayOfMonth.toString()
            textPaint.textSize = radius * if (isMonthStart) 0.72f else 0.95f
            textPaint.color = when (cell.status) {
                WidgetDayStatus.DONE, WidgetDayStatus.FROZEN, WidgetDayStatus.MISSED -> 0xFFFFFFFF.toInt()
                WidgetDayStatus.TODAY -> palette.pending
                WidgetDayStatus.FUTURE -> palette.mutedText
                WidgetDayStatus.NEUTRAL -> palette.mutedText and 0x00FFFFFF or 0x99000000.toInt()
            }
            canvas.drawText(label, centerX, centerY - (textPaint.ascent() + textPaint.descent()) / 2f, textPaint)
        }
    }
    return bitmap
}

private fun drawWeekdayHeader(
    canvas: Canvas,
    columnWidth: Float,
    headerHeight: Float,
    firstDayOfWeek: Int,
    color: Int,
    paint: Paint
) {
    val weekdayNames = DateFormatSymbols.getInstance().shortWeekdays
    paint.color = color
    paint.textSize = headerHeight * 0.72f
    paint.typeface = Typeface.DEFAULT
    repeat(7) { column ->
        val calendarDay = (firstDayOfWeek - 1 + column) % 7 + 1
        val initial = weekdayNames[calendarDay].take(1)
        val baseline = headerHeight / 2f - (paint.ascent() + paint.descent()) / 2f
        canvas.drawText(initial, columnWidth * column + columnWidth / 2f, baseline, paint)
    }
    paint.typeface = Typeface.DEFAULT_BOLD
}
