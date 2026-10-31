package com.example.workoutassist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import com.example.workoutassist.MainActivity
import com.example.workoutassist.R
import com.example.workoutassist.data.WorkoutDatabase
import com.example.workoutassist.ui.PREFS_NAME
import com.example.workoutassist.ui.currentDateEpochDay
import com.example.workoutassist.ui.readFrozenStreakDays
import com.example.workoutassist.ui.readWidgetThemeColors
import com.example.workoutassist.ui.timestampMillisToEpochDay
import java.util.Calendar
import kotlin.math.sqrt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "StreakWidget"
private const val DEFAULT_WIDGET_WIDTH_DP = 250
private const val DEFAULT_WIDGET_HEIGHT_DP = 110
private const val WIDGET_PADDING_HORIZONTAL_DP = 8
private const val WIDGET_PADDING_VERTICAL_DP = 6
// Top (calendar) share of the widget's content height; the rest is the stats strip.
private const val CALENDAR_HEIGHT_FRACTION = 0.7f
// Keeps the calendar bitmap well inside the RemoteViews binder memory budget.
private const val MAX_CALENDAR_BITMAP_PIXELS = 1_000_000f

/** Home-screen widget: recent-weeks calendar on top (70%), streak/gap and monthly breaks below. */
class StreakCalendarWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAsync(context, appWidgetManager, appWidgetIds)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        updateAsync(context, appWidgetManager, intArrayOf(appWidgetId))
    }

    private fun updateAsync(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                appWidgetIds.forEach { appWidgetId ->
                    manager.updateAppWidget(appWidgetId, buildRemoteViews(context, manager, appWidgetId))
                }
            } catch (error: Exception) {
                Log.e(TAG, "Streak widget update failed", error)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        /** Asks every placed widget to redraw from the latest data (no-op when none are placed). */
        fun requestUpdate(context: Context) {
            val appContext = context.applicationContext
            val appWidgetIds = AppWidgetManager.getInstance(appContext)
                .getAppWidgetIds(ComponentName(appContext, StreakCalendarWidgetProvider::class.java))
            if (appWidgetIds.isEmpty()) return
            val intent = Intent(appContext, StreakCalendarWidgetProvider::class.java)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            appContext.sendBroadcast(intent)
        }
    }
}

private suspend fun buildRemoteViews(
    context: Context,
    manager: AppWidgetManager,
    appWidgetId: Int
): RemoteViews {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val sessions = WorkoutDatabase.getInstance(context).workoutDao().getAllSessions()
    val completedDays = sessions
        .mapNotNull { it.finishedAt }
        .map { timestampMillisToEpochDay(it) }
        .toSet()
    val frozenDays = readFrozenStreakDays(prefs)
    val todayEpochDay = currentDateEpochDay()
    val stats = computeWidgetStats(completedDays, frozenDays, todayEpochDay)
    val colors = readWidgetThemeColors(prefs)
    val palette = WidgetPalette(
        done = colors.done.toArgb(),
        frozen = colors.frozen.toArgb(),
        pending = colors.pending.toArgb()
    )

    val options = manager.getAppWidgetOptions(appWidgetId)
    val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, DEFAULT_WIDGET_WIDTH_DP)
        .takeIf { it > 0 } ?: DEFAULT_WIDGET_WIDTH_DP
    val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, DEFAULT_WIDGET_HEIGHT_DP)
        .takeIf { it > 0 } ?: DEFAULT_WIDGET_HEIGHT_DP
    val calendarWidthDp = (widthDp - 2 * WIDGET_PADDING_HORIZONTAL_DP).coerceAtLeast(1)
    val calendarHeightDp = (heightDp - 2 * WIDGET_PADDING_VERTICAL_DP) * CALENDAR_HEIGHT_FRACTION

    val firstDayOfWeek = Calendar.getInstance().firstDayOfWeek
    val weeks = buildWidgetWeeks(
        completedDays = completedDays,
        frozenDays = frozenDays,
        todayEpochDay = todayEpochDay,
        weekCount = widgetWeekCountForHeight(calendarHeightDp),
        firstDayOfWeek = firstDayOfWeek
    )

    val density = context.resources.displayMetrics.density
    val rawWidthPx = calendarWidthDp * density
    val rawHeightPx = calendarHeightDp * density
    val scale = minOf(1f, sqrt(MAX_CALENDAR_BITMAP_PIXELS / (rawWidthPx * rawHeightPx).coerceAtLeast(1f)))
    val bitmap = renderCalendarBitmap(
        widthPx = (rawWidthPx * scale).toInt(),
        heightPx = (rawHeightPx * scale).toInt(),
        density = density * scale,
        weeks = weeks,
        firstDayOfWeek = firstDayOfWeek,
        palette = palette
    )

    val views = RemoteViews(context.packageName, R.layout.widget_streak_calendar)
    views.setImageViewBitmap(R.id.widget_calendar, bitmap)
    views.setTextViewText(R.id.widget_streak_value, (if (stats.isOnGap) stats.gapDays else stats.streakDays).toString())
    views.setTextColor(R.id.widget_streak_value, if (stats.isOnGap) palette.missed else palette.done)
    views.setTextViewText(R.id.widget_streak_label, context.getString(if (stats.isOnGap) R.string.widget_gap_label else R.string.widget_streak_label))
    views.setTextViewText(R.id.widget_breaks_value, stats.breaksThisMonth.toString())
    views.setTextViewText(R.id.widget_breaks_label, context.getString(R.string.widget_breaks_label))

    val openApp = PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )
    views.setOnClickPendingIntent(R.id.widget_root, openApp)
    return views
}
