package com.example.workoutassist.ui

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.WorkoutDatabase
import com.example.workoutassist.data.WorkoutRepository
import kotlinx.coroutines.launch

private enum class AppScreen {
    SCHEDULE,
    DAY_DETAIL
}

private enum class RootTab(
    val icon: ImageVector
) {
    WORKOUT(icon = Icons.Rounded.FitnessCenter),
    INSIGHTS(icon = Icons.Rounded.BarChart),
    SETTINGS(icon = Icons.Rounded.Settings)
}

internal data class ThemeColorOption(
    val id: String,
    val label: String,
    val color: Color
)

internal enum class SettingsView {
    ROOT,
    THEME_OPTIONS,
    LABEL_OPTIONS,
    STREAK_GRAPH_OPTIONS,
    PAGE_COMMANDS
}

internal enum class SettingsFeedbackKind {
    SUCCESS,
    FAILURE
}

internal data class SettingsFeedback(
    val kind: SettingsFeedbackKind,
    val title: String,
    val message: String
)

internal data class AppPageCommand(
    val name: String,
    val command: String,
    val description: String
)

internal data class AppLabels(
    val planTitle: String,
    val compactButton: String,
    val calendarButton: String,
    val workoutTab: String,
    val insightsTab: String,
    val settingsTab: String,
    val insightsTitle: String,
    val workoutInsightsTitle: String,
    val graphsTitle: String,
    val themeTitle: String,
    val labelsTitle: String,
    val pageCommandsTitle: String,
    val missedBannerText: String,
    val routineTitle: String,
    val streakTitle: String,
    val daysToRoutineText: String,
    val onRoutineText: String
)

internal const val DEFAULT_SCHEDULE_TITLE = "Your plan"
private const val PREFS_NAME = "gudhealth_prefs"
private const val KEY_SCHEDULE_TITLE = "schedule_title"
private const val KEY_PAGE_LABEL_SCHEDULE = "page_label_schedule"
private const val KEY_PAGE_LABEL_INFINITY = "page_label_infinity"
private const val KEY_TAB_LABEL_WORKOUT = "tab_label_workout"
private const val KEY_TAB_LABEL_INSIGHTS = "tab_label_insights"
private const val KEY_TAB_LABEL_SETTINGS = "tab_label_settings"
private const val KEY_THEME_BACKGROUND = "theme_background"
private const val KEY_THEME_STATUS = "theme_status"
private const val KEY_THEME_DONE = "theme_done"
private const val KEY_THEME_BACKGROUND_CUSTOM_HEX = "theme_background_custom_hex"
private const val KEY_THEME_STATUS_CUSTOM_HEX = "theme_status_custom_hex"
private const val KEY_THEME_DONE_CUSTOM_HEX = "theme_done_custom_hex"
private const val KEY_THEME_BANNER = "theme_banner"
private const val KEY_THEME_BANNER_CUSTOM_HEX = "theme_banner_custom_hex"
private const val KEY_PRODUCTION_RESET_20260707_DONE = "production_reset_20260707_done"
private const val KEY_HISTORY_PREFILL_20260708_DONE = "history_prefill_20260708_done"
private const val DEFAULT_PAGE_LABEL_SCHEDULE = "Compact"
private const val DEFAULT_PAGE_LABEL_INFINITY = "Calendar"
private const val DEFAULT_TAB_LABEL_WORKOUT = "Workout"
private const val DEFAULT_TAB_LABEL_INSIGHTS = "Insights"
private const val DEFAULT_TAB_LABEL_SETTINGS = "Settings"
private const val KEY_TITLE_INSIGHTS = "title_insights"
private const val KEY_TITLE_WORKOUT_INSIGHTS = "title_workout_insights"
private const val KEY_TITLE_GRAPHS = "title_graphs"
private const val KEY_TITLE_THEME = "title_theme"
private const val KEY_TITLE_LABELS = "title_labels"
private const val KEY_TITLE_PAGE_COMMANDS = "title_page_commands"
private const val DEFAULT_TITLE_INSIGHTS = "Insights"
private const val DEFAULT_TITLE_WORKOUT_INSIGHTS = "Workout Insights"
private const val DEFAULT_TITLE_GRAPHS = "Progress Graphs"
private const val DEFAULT_TITLE_THEME = "Theme"
private const val DEFAULT_TITLE_LABELS = "Labels"
private const val DEFAULT_TITLE_PAGE_COMMANDS = "Page Commands"
private const val KEY_TITLE_MISSED_BANNER = "title_missed_banner"
private const val DEFAULT_TITLE_MISSED_BANNER = "Missed · tap to add"
private const val KEY_TITLE_ROUTINE = "title_routine"
private const val DEFAULT_TITLE_ROUTINE = "routine"
private const val KEY_TITLE_STREAK = "title_streak"
private const val DEFAULT_TITLE_STREAK = "Streak momentum"
private const val KEY_TEXT_DAYS_TO_ROUTINE = "text_days_to_routine"
private const val DEFAULT_TEXT_DAYS_TO_ROUTINE = "days to get back on routine"
private const val KEY_TEXT_ON_ROUTINE = "text_on_routine"
private const val DEFAULT_TEXT_ON_ROUTINE = "You're on routine"
private const val KEY_INSIGHTS_SHORT_WINDOW = "insights_short_window"
private const val DEFAULT_INSIGHTS_SHORT_WINDOW = 7
private const val MIN_INSIGHTS_SHORT_WINDOW = 5
private const val MAX_INSIGHTS_SHORT_WINDOW = 15
private const val KEY_ROUTINE_WINDOW = "insights_routine_window"
private const val KEY_DEFAULT_SCHEDULE_CALENDAR = "default_schedule_calendar"
private const val KEY_CLASSIC_STREAK_GRAPH = "classic_streak_graph"
private const val KEY_MOMENTUM_STOCK_MODE = "momentum_stock_mode"
private const val DEFAULT_THEME_BACKGROUND_ID = "white"
private const val DEFAULT_THEME_STATUS_ID = "turquoise"
private const val DEFAULT_THEME_DONE_ID = "green"
private const val DEFAULT_THEME_BACKGROUND_CUSTOM_HEX = "#FFFFFF"
private const val DEFAULT_THEME_STATUS_CUSTOM_HEX = "#1CCBCB"
private const val DEFAULT_THEME_DONE_CUSTOM_HEX = "#1E9E58"
private const val DEFAULT_THEME_BANNER_ID = "flame"
private const val DEFAULT_THEME_BANNER_CUSTOM_HEX = "#BF360C"
internal const val CUSTOM_THEME_OPTION_ID = "custom"
internal const val LATEST_DESIGN_VERSION = "1.105"

internal val WORKOUT_SESSION_START_MESSAGES = listOf(
    "Lift weights and come back !",
    "This is something you won't regret !",
    "hustle for that muscle !",
    "mind plays tricks like exhaustion to skip next rep-but pain is not one of them",
    "No need to stop when you're tired, stop when you're Done !",
    "Last time you lifted more with less sweat !"
)

private val BACKGROUND_THEME_OPTIONS = listOf(
    ThemeColorOption(id = "white", label = "White", color = Color(0xFFFFFFFF)),
    ThemeColorOption(id = "mist", label = "Mist", color = Color(0xFFF3F7F9)),
    ThemeColorOption(id = "paper", label = "Paper", color = Color(0xFFFAF8F3))
)

private val STATUS_THEME_OPTIONS = listOf(
    ThemeColorOption(id = "turquoise", label = "Turquoise", color = Color(0xFF1CCBCB)),
    ThemeColorOption(id = "ocean", label = "Ocean", color = Color(0xFF2FA6D9)),
    ThemeColorOption(id = "teal", label = "Teal", color = Color(0xFF2CB8A0))
)

private val DONE_THEME_OPTIONS = listOf(
    ThemeColorOption(id = "green", label = "Green", color = Color(0xFF1E9E58)),
    ThemeColorOption(id = "forest", label = "Forest", color = Color(0xFF228B52)),
    ThemeColorOption(id = "blue", label = "Blue", color = Color(0xFF1F7AE0))
)

private val BANNER_THEME_OPTIONS = listOf(
    ThemeColorOption(id = "flame", label = "Flame", color = Color(0xFFBF360C)),
    ThemeColorOption(id = "crimson", label = "Crimson", color = Color(0xFFC62828)),
    ThemeColorOption(id = "amber", label = "Amber", color = Color(0xFFEF6C00))
)

internal val PAGE_COMMAND_NAMES = listOf(
    AppPageCommand(name = "Schedule", command = "workout.schedule", description = "Workout tab: merged plan/history (Compact default, Calendar toggle)"),
    AppPageCommand(name = "Day Detail", command = "workout.day", description = "Workout day detail (start/edit a day)"),
    AppPageCommand(name = "Workout Session", command = "workout.session", description = "Active workout session (focus mode)"),
    AppPageCommand(name = "Exercise History Peek", command = "workout.session.history", description = "Active session: double-tap the exercise title -> confirm -> temporary past-sessions overlay (back/close returns to the session)"),
    AppPageCommand(name = "Insights", command = "insights.home", description = "Insights tab (ratios + open Workout Insights + Progress Graphs)"),
    AppPageCommand(name = "Streak Graph", command = "insights.routine", description = "Insights: streak momentum graph (Bars/Stocks toggle, double-tap to inspect); Settings > Streak graph switches to the classic triangle"),
    AppPageCommand(name = "Adherence Ratios", command = "insights.ratios", description = "Insights: recent-days + last-30-day ratio bars (tap short bar to change window 5-15)"),
    AppPageCommand(name = "Workout Insights", command = "insights.workout", description = "Insights > Workout Insights (per-workout exercise history)"),
    AppPageCommand(name = "Progress Graphs", command = "graphs.progress", description = "Progress Graphs (beta), opened from Insights"),
    AppPageCommand(name = "Consistency Rings", command = "graphs.consistency", description = "Progress Graphs: last-7 / last-30 consistency rings"),
    AppPageCommand(name = "Weekly Frequency", command = "graphs.frequency", description = "Progress Graphs: weekly-frequency bars"),
    AppPageCommand(name = "Exercise Trends", command = "graphs.exercise", description = "Progress Graphs: per-exercise weight/reps line charts"),
    AppPageCommand(name = "Settings", command = "settings.home", description = "Settings root (Appearance: labels, theme, default schedule view, streak graph; Data; Advanced)"),
    AppPageCommand(name = "Backup & Restore", command = "settings.backup", description = "Settings > Data: export/import a JSON backup"),
    AppPageCommand(name = "Theme", command = "settings.theme", description = "Settings > Theme (colors + custom picker)"),
    AppPageCommand(name = "Labels", command = "settings.labels", description = "Settings > Labels (titles, toggle, tabs, routine texts)"),
    AppPageCommand(name = "Page Commands", command = "settings.pagecommands", description = "Settings > Page command names (this list)"),
    AppPageCommand(name = "About & What's New", command = "settings.about", description = "Settings: version badge -> details + What's new highlights")
)

internal val LATEST_VERSION_HIGHLIGHTS = listOf(
    "The Insights streak view is now a scrollable Streak Momentum graph: it climbs 1 per workout day and drops to 0 when you miss a day, and opens on today (right-most). Double-tap it to open a taller, scrollable inspector that shows your best streak. Switch the style (momentum or classic triangle) and look (line or stock-market candles) under Settings > Streak graph.",
    "Exiting a workout mid-session no longer counts as completed - only the Hold-to-Finish action records a done workout, so your streaks stay honest.",
    "Pick which schedule view opens by default (Compact or Calendar) in Settings > Default schedule view.",
    "Logging a set now fills weight down to later sets but keeps each set's reps independent (reps usually vary per set).",
    "Polish: the workout day title is centered, the set-table headers read REPS / WGT, and the active-session rest shows as 'Rest 1m30s' under the exercise name.",
    "Today's pending workout now nudges you with a pulsing angry red emoji instead of a flame, and your streak triangle shows bigger flames for each day done.",
    "Editing the recent-days ratio and the streak target is now double-tap (both), the past-sessions peek shows a clean reps/wgt table, and weights drop the 'kg' clutter.",
    "Peek at your history mid-workout: double-tap the exercise title, confirm, and a temporary overlay shows what you did for that exercise in past sessions — close it (or back) to drop right back into your session.",
    "The routine streak graph's target is adjustable too — long-press the triangle to set 5–15 days.",
    "Cleaner active workout: the focused exercise card is now a solid color (no shadow behind the title), the rest timer blinks before it resets, and the rest duration is bigger.",
    "Tidier exercise details: interval and remarks show inline, and reps/weight show as plain 'x6' / '60 kg' text instead of boxes.",
    "The 'Last 7 days' ratio is now tap-to-change: pick any window from 5 to 15 days (it's remembered).",
    "After you log a set, the rest timer now waits ~2 seconds before resetting, so you can see how long the rest actually was.",
    "Tidied the active workout: the rest time shows as a compact 'rest 1m30s' to the left of the exercise name, and the Total/Rest timers now use a 40:60 width split.",
    "Logging a set now fills down: set the reps/weight on set 1 and the later sets copy it automatically (a ladder), so you only change the sets that differ.",
    "The last-7-day and last-30-day bars now show their empty part in the missed-banner color (matching the routine bar), so done vs. remaining reads consistently.",
    "Switching tabs now slides: swipe left/right (or tap the bottom bar) to move between Workout, Insights, and Settings, and the screen slides in the direction you're going.",
    "You can now edit an exercise's remark during a workout (the 'i' note dialog is editable). Remarks stick to the exercise, so your note is there again next time you do that workout.",
    "The Workout home screen now shows your routine streak as a compact strip of bricks under the title — filled bricks are days you've kept the streak, at a glance.",
    "The Insights routine triangle now shows a little fire icon above each completed day's edge (the same flame as today's pending workout), so your streak lights up as you keep it going.",
    "Progress Graphs (Beta) now opens from the bottom of the Insights page instead of Settings — all your analytics live together in one place.",
    "The Insights routine triangle now has a numbered bottom axis (7 6 5 4 3 2 1) so you can read off how many days are left to get back on routine at a glance.",
    "Turning off Edit mode now asks 'Save changes?' — pick Save to keep your edits, or Discard to roll the workout template back to how it was before you started editing.",
    "After you save template edits, the follow-up prompt now says 'Go to settings' (instead of exporting straight away) and takes you to Settings, where you can export a backup.",
    "The Insights 'routine' stat is now a single smooth triangle: the primary-colored part is your current streak and the rest of the triangle (in the missed-banner color) is how many days are left to get back on routine — both shown at once.",
    "The two workout timers now tell themselves apart by icon (a clock for total, a reset icon for the rest timer) instead of 'Total'/'Rest' labels, leaving room for a bigger, easier-to-read time.",
    "The active-workout top bar is now just the two stopwatches (Total and Rest) — the session date was removed from the top-right for a cleaner header.",
    "Finishing a workout now uses a press-and-hold on the bottom 'Hold to Finish Workout' button itself (it fills as you hold, like exit) and finishes when full — no confirm dialog, and a stray tap no longer ends the session.",
    "Schedule cards now show Day, Date, and workout name on a single line, with Day and Date at fixed widths so the workout names all start at the same spot for a cleaner, aligned list.",
    "An active workout now shows two stopwatches in the top bar (50/50 split): 'Total' counts the whole session, and 'Rest' counts the interval since your last set log and resets (with a brief flash) each time you save a set's reps or weight. Neither is saved.",
    "Completed workouts now show a gold medal badge (instead of a plain tick) for an achievement feel; auto-logged rest days show a calm muted moon so the medal stays meaningful.",
    "Exiting a workout mid-session now requires a deliberate press-and-hold (a filling 'Hold to exit' button) instead of a tap, so a stray/pocket touch can't end your session; Back opens the same hold confirm.",
    "The Back-to-routine texts are now editable in Settings > Labels: the stat title, the 'days to get back on routine' text, and the 'You're on routine' text.",
    "Insights adds a Back-to-routine stat: an on-plan streak (consecutive days with a session) toward one cycle; shows days left to get back on routine, or 'You're on routine' once the streak reaches a full cycle.",
    "The missed-day banner text (\"Missed - tap to add\") is now editable in Settings > Labels.",
    "Theme has a 4th role, Banner: it sets the color of the missed-day cards and domino pips (with the same swatch + custom picker).",
    "Workout Insights is now its own page: the Insights tab shows ratios plus an Open button that navigates to the per-workout history (with back).",
    "Settings > Labels now edits all page/subpage titles too: Insights, Workout Insights, Progress Graphs, Theme, Labels, and Page Commands (plus the existing plan title, toggle, and tabs).",
    "Export/Import feedback now shows at the top of Settings and auto-scrolls into view (was appearing far down the page).",
    "Insights ratios are shown as horizontal battery-style step bars (7 steps for last-7 days, 30 for last-30).",
    "Workout Insights dropdown now prefixes each workout with its day number (Day N - Name).",
    "Removed the right-swipe-to-mark-done gesture on the workout day page (and its now-dead code).",
    "Removed the date shown on the workout day page header (where exercise cards and Start Workout are).",
    "Page Command Names moved behind an Options button in Settings > Advanced (new Page Commands view); list refreshed.",
    "Single-tap a done workout to open it in the workout page; double-tap still removes it (confirm).",
    "Backfill (tap a missed day in Calendar) now also offers the rest day (day 7).",
    "Compact view shows missed days between two dates as tiny red domino pips (one per missed day); Calendar shows full red missed-day cards.",
    "Compact/Calendar transition animates (~1.5s) and pivots on today; the toggle button labels are renamable in Settings > Labels.",
    "Removed the top-left Today status on the Workout tab; on the workout day page removed the non-working date/mark-done edit controls and moved the rename pencil next to the title.",
    "Schedule and Infinity are merged into one view: a single Day n - Date timeline showing the whole Day 1-7 cycle (past cycles included), skipping missed days by default.",
    "The timeline always extends the current cycle through its last day (day 7) with projected upcoming dates; it never shows more than the current cycle ahead.",
    "Top-right Calendar/Compact toggle: Calendar expands to show every day including missed days (red) and adds the workout name (Day n - Date - Workout).",
    "Done days keep the tick and color coding; today stays highlighted; double-tap a done day to remove it; tap a missed day (Calendar) to backfill.",
    "Adding a set mid-session now accepts wheel-picker input for the new set (previously ignored).",
    "Infinity: a logged workout is removed by a deliberate double-tap + confirm (no more accidental removal by holding); single-tap does nothing.",
    "Schedule Up next no longer skips rest days (after the last training day, the next day - even a rest day - is Up next).",
    "Schedule shows a today indicator: workout done or not logged yet.",
    "Schedule tab is a Day 1-7 plan/cycle view with position tracking (Up next, passed ticks); one-time gaps via long-press a day, removed by long-press a gap.",
    "Infinity tab is a factual history calendar (oldest to today, today highlighted); tap a past day to backfill or remove a workout.",
    "Done is unified: a finished session on a date drives Insights ratios, Progress Graphs, and Schedule/Infinity ticks.",
    "Progress Graphs (Beta) page added under Settings > Analytics: consistency rings, weekly bars, per-exercise weight/reps line charts (native Canvas).",
    "Settings grouped into Appearance, Data, Advanced, Analytics sections.",
    "Insights: removed Refresh Stats, renamed Delete Record to Delete Set, monthly ratio now rolling last-30-days, exercise chips ordered by workout sequence.",
    "Active session: Skip logs 0 reps and stays re-selectable to undo; rest interval shown; scrollable so Log/Skip stay reachable; long-press a set to remove; sets can be 0; bottom nav hidden (focus mode).",
    "Workout-day table weight column shows wt(kg) with plain numbers; seed weights numeric only; Treadmill added to every training day.",
    "Insights selector is now workout-level first (day-style), then exercise-level inside that workout.",
    "Insights date-wise history now renders as stacked cards from newest to oldest for clearer scanability.",
    "Insights exercise history now supports pointed set edit and set delete actions.",
    "Insights date-level delete removes only the selected exercise entry for that specific date, keeping same-workout data on other dates untouched.",
    "Insights now shows date-wise exercise history as weight x reps entries (for example 50 x6, 60 x6x6), replacing the old summary grid.",
    "Insights exercise history selector uses a dropdown for compact selection.",
    "Insights top ratios now show only compact values (for example 2/7 and 10/31) without extra labels.",
    "Workout-day expanded reps/wgt table edits are now truly per-set and persisted independently (editing set 2 only updates set 2).",
    "Exercise template model now stores per-set planned reps/weight arrays and set logs read planned values by set number.",
    "Backup export/import now includes per-set planned arrays with backward-compatible fallback for older backups.",
    "One-time production reset now flushes stored workout data and reseeds template dates from today on first launch after update.",
    "Room destructive migration fallback is removed to protect existing user data in future schema updates.",
    "Insights now keeps ratio metrics and adds workout-specific history grid (last 4-8 same workouts with date columns).",
    "Removed extra Insights trend cards so workout-specific history is the main deep view.",
    "Exercise-card metric chips are removed; expanded table remains the primary detail UI with editable reps/wgt cells via wheel picker.",
    "Expanded workout-day exercise details now render a set-wise table with reps and weight rows (interval removed from the table).",
    "Workout day no longer supports cycle swipe on header; it is back to fixed-date day view.",
    "Dropped workout-day cycle swipe + Today-return idea is now tracked in docs/DROPPED_FEATURES.md for future revisit.",
    "Settings now includes stable page command names for quick navigation/edit references.",
    "After logging an exercise, active session now auto-focuses and auto-scrolls to the next unfinished exercise chip.",
    "Number wheel dialogs now support double-tap directly on the scroll value area to confirm selection.",
    "Insights now drops session-duration trend and keeps top 7-day/month ratios as plain counts without percent.",
    "Insights now uses logged sessions and set logs to show trend cards (consistency and rep adherence).",
    "Infinity Today quick-jump now uses a higher-contrast filled button style for stronger visibility.",
    "Workout strip now includes a fixed i button to open remarks for the selected exercise.",
    "System back on Insights/Settings now returns to Workout home first.",
    "Back on Workout home now shows an exit confirmation dialog.",
    "Workout exercise strip labels now use larger text, along with the pinned 1/n Done strip.",
    "Pinned 1/n Done strip now uses larger text for better readability.",
    "Active workout 1/n Done progress is pinned beside the exercise strip and stays visible while chips scroll.",
    "Infinity quick-jump Today button text is now bold for stronger visibility.",
    "Dropped Template Frozen lock toggle and status label from active workout view; template edits remain blocked during active workout.",
    "Edited set rows in workout session now keep visual highlight styling without showing an explicit Edited text badge.",
    "Focused exercise name in workout session data card is now centered for clearer visual hierarchy.",
    "Rolled back forced zero-inset overrides to restore touchable top content across pages.",
    "Edited set rows in workout session are visually highlighted after confirmation to reduce accidental re-edits.",
    "Removed residual top empty space across pages by aligning Scaffold and TopAppBar insets.",
    "Starting a workout now shows a random 2-second motivational message with an X to close early.",
    "Finish Workout now requires long-press (inside explicit Session Actions), and remains available regardless of logged exercise count.",
    "Workout session now keeps Finish hidden until explicit Session Actions reveal; Log Exercise is wider and bottom-anchored for easier thumb reach.",
    "This Month ratio now uses done sessions divided by total days in current month.",
    "Insights now has a Refresh Stats button with circular refresh action and only 7-day + this-month ratios.",
    "Finishing a workout session now closes directly without showing summary stats popup.",
    "Workout planned-reps row is now visually grayed to indicate read-only state.",
    "Workout start page now hides helper headings for a cleaner focus layout.",
    "Back navigation is disabled while Edit mode is active on workout screen.",
    "Workout start mode now opens a dedicated focus flow with per-set wheel input rows.",
    "Theme settings now include a per-role RGB color picker with custom persistence.",
    "Insights now leads with My Ratio (last 7 days, session-based).",
    "Insights now shows rolling done-session ratios for 7 and 31 days.",
    "Added Insights tab between Workout and Settings.",
    "Tab labels (Workout, Insights, Settings) are now renamable.",
    "Moved label rename controls into a dedicated Settings -> Labels options view.",
    "Infinity done state now matches only the exact completed date instance.",
    "Workout list bottom reserve now adapts to FAB visibility to cut extra empty strip.",
    "Workout exercise list now fills remaining height to avoid lower blank strip.",
    "Workout day top app bar now uses zero top inset to remove leading gap.",
    "Removed extra top spacing in workout day screen content.",
    "Removed schedule top pencil action to keep only two page buttons.",
    "Removed custom right-edge scroll indicator from Schedule and Infinity lists.",
    "Removed schedule header section; page switcher is now the top focus.",
    "Settings can rename both page labels (left and right switch segments).",
    "Schedule/Infinity selector now uses a larger 50-50 segmented switch.",
    "Infinity page now includes a Today quick-jump button.",
    "Infinity page now free-scrolls repeated schedule cycles above and below.",
    "Schedule/Infinity top switch keeps Schedule as default.",
    "Import/export now shows styled success and failure feedback cards.",
    "Exercise cards include remarks in expanded details only.",
    "Added Settings color-role customization options.",
    "Added prompt to export backup when exiting edit mode after making changes.",
    "Settings keeps small version badge with tap-to-open details dialog.",
    "Displayed version is sourced from app package version metadata."
)

@Composable
fun WorkoutAssistApp() {
    val context = LocalContext.current
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val repository = remember {
        WorkoutRepository(WorkoutDatabase.getInstance(context).workoutDao())
    }
    val prefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        val prefillAlreadyDone = prefs.getBoolean(KEY_HISTORY_PREFILL_20260708_DONE, false)
        if (!prefillAlreadyDone) {
            val prefillResult = runCatching {
                importBackupFromAsset(
                    context = context,
                    repository = repository,
                    assetName = "workout-history-prefill.json"
                )
            }
            if (prefillResult.isSuccess) {
                // One-time load succeeded; mark done so it never runs again and never
                // overwrites entries the user makes later in the app.
                prefs.edit()
                    .putBoolean(KEY_HISTORY_PREFILL_20260708_DONE, true)
                    .putBoolean(KEY_PRODUCTION_RESET_20260707_DONE, true)
                    .apply()
                return@LaunchedEffect
            }
        }

        val resetAlreadyDone = prefs.getBoolean(KEY_PRODUCTION_RESET_20260707_DONE, false)
        if (!resetAlreadyDone) {
            repository.resetAllDataAndSeedFromToday()
            prefs.edit().putBoolean(KEY_PRODUCTION_RESET_20260707_DONE, true).apply()
        } else {
            repository.ensureSeedData()
        }
    }

    val days by repository.observeDays().collectAsState(initial = emptyList())
    val sessions by repository.observeSessions().collectAsState(initial = emptyList())
    val setLogs by repository.observeSetLogs().collectAsState(initial = emptyList())
    val completedSessionEpochDays = remember(sessions) {
        sessions.asSequence()
            .mapNotNull { it.finishedAt }
            .map { timestampMillisToEpochDay(it) }
            .toSet()
    }
    val completedWorkoutByDate = remember(sessions) {
        sessions.asSequence()
            .filter { it.finishedAt != null }
            .sortedBy { it.finishedAt }
            .associate { timestampMillisToEpochDay(it.finishedAt!!) to it.workoutName }
    }
    val completedDayNumberByDate = remember(sessions) {
        sessions.asSequence()
            .filter { it.finishedAt != null }
            .sortedBy { it.finishedAt }
            .associate { timestampMillisToEpochDay(it.finishedAt!!) to it.dayNumber }
    }
    val lastCompletedDayNumber = remember(sessions) {
        sessions.asSequence()
            .filter { it.finishedAt != null }
            .maxByOrNull { it.finishedAt!! }
            ?.dayNumber
    }
    val lastCompletedEpochDay = remember(completedSessionEpochDays) {
        completedSessionEpochDays.maxOrNull()
    }
    val todayDateEpochDay = currentDateEpochDay()

    // Auto-advance past a skipped rest day: if the next due day is a rest day and its
    // scheduled date has already passed, log an (empty) rest session for it so the
    // cycle rolls forward instead of the rest day showing as "missed" indefinitely.
    LaunchedEffect(days, lastCompletedDayNumber, lastCompletedEpochDay, todayDateEpochDay) {
        if (days.isEmpty()) return@LaunchedEffect
        val lastDate = lastCompletedEpochDay ?: return@LaunchedEffect
        val cycle = days.sortedBy { it.dayNumber }
        val startIndex = lastCompletedDayNumber
            ?.let { dayNumber -> cycle.indexOfFirst { it.dayNumber == dayNumber } }
            ?: return@LaunchedEffect
        if (startIndex < 0) return@LaunchedEffect
        val n = cycle.size
        val nextDue = cycle[(((startIndex + 1) % n) + n) % n]
        val restDueDate = lastDate + 1L
        if (nextDue.exercises.isEmpty() &&
            todayDateEpochDay > restDueDate &&
            restDueDate !in completedSessionEpochDays
        ) {
            repository.logBackdatedWorkout(nextDue, restDueDate)
        }
    }
    val highlightedTodayDayNumber = days
        .firstOrNull { it.plannedDateEpochDay == todayDateEpochDay }
        ?.dayNumber
    var scheduleTitle by remember {
        mutableStateOf(prefs.getString(KEY_SCHEDULE_TITLE, DEFAULT_SCHEDULE_TITLE) ?: DEFAULT_SCHEDULE_TITLE)
    }
    var selectedDayNumber by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf(AppScreen.SCHEDULE) }
    var selectedTab by remember { mutableStateOf(RootTab.WORKOUT) }
    var showGraphsPage by remember { mutableStateOf(false) }
    var isWorkoutSessionActive by remember { mutableStateOf(false) }
    var showExitAppConfirm by remember { mutableStateOf(false) }
    var settingsFeedback by remember { mutableStateOf<SettingsFeedback?>(null) }
    var importResultFeedback by remember { mutableStateOf<SettingsFeedback?>(null) }
    var schedulePageLabel by remember {
        mutableStateOf(
            prefs.getString(KEY_PAGE_LABEL_SCHEDULE, DEFAULT_PAGE_LABEL_SCHEDULE) ?: DEFAULT_PAGE_LABEL_SCHEDULE
        )
    }
    var infinityPageLabel by remember {
        mutableStateOf(
            prefs.getString(KEY_PAGE_LABEL_INFINITY, DEFAULT_PAGE_LABEL_INFINITY) ?: DEFAULT_PAGE_LABEL_INFINITY
        )
    }
    var workoutTabLabel by remember {
        mutableStateOf(
            prefs.getString(KEY_TAB_LABEL_WORKOUT, DEFAULT_TAB_LABEL_WORKOUT) ?: DEFAULT_TAB_LABEL_WORKOUT
        )
    }
    var insightsTabLabel by remember {
        mutableStateOf(
            prefs.getString(KEY_TAB_LABEL_INSIGHTS, DEFAULT_TAB_LABEL_INSIGHTS) ?: DEFAULT_TAB_LABEL_INSIGHTS
        )
    }
    var settingsTabLabel by remember {
        mutableStateOf(
            prefs.getString(KEY_TAB_LABEL_SETTINGS, DEFAULT_TAB_LABEL_SETTINGS) ?: DEFAULT_TAB_LABEL_SETTINGS
        )
    }
    var insightsTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_INSIGHTS, DEFAULT_TITLE_INSIGHTS) ?: DEFAULT_TITLE_INSIGHTS)
    }
    var workoutInsightsTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_WORKOUT_INSIGHTS, DEFAULT_TITLE_WORKOUT_INSIGHTS) ?: DEFAULT_TITLE_WORKOUT_INSIGHTS)
    }
    var graphsTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_GRAPHS, DEFAULT_TITLE_GRAPHS) ?: DEFAULT_TITLE_GRAPHS)
    }
    var themeTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_THEME, DEFAULT_TITLE_THEME) ?: DEFAULT_TITLE_THEME)
    }
    var labelsTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_LABELS, DEFAULT_TITLE_LABELS) ?: DEFAULT_TITLE_LABELS)
    }
    var pageCommandsTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_PAGE_COMMANDS, DEFAULT_TITLE_PAGE_COMMANDS) ?: DEFAULT_TITLE_PAGE_COMMANDS)
    }
    var missedBannerTextLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_MISSED_BANNER, DEFAULT_TITLE_MISSED_BANNER) ?: DEFAULT_TITLE_MISSED_BANNER)
    }
    var routineTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_ROUTINE, DEFAULT_TITLE_ROUTINE) ?: DEFAULT_TITLE_ROUTINE)
    }
    var streakTitleLabel by remember {
        mutableStateOf(prefs.getString(KEY_TITLE_STREAK, DEFAULT_TITLE_STREAK) ?: DEFAULT_TITLE_STREAK)
    }
    var daysToRoutineTextLabel by remember {
        mutableStateOf(prefs.getString(KEY_TEXT_DAYS_TO_ROUTINE, DEFAULT_TEXT_DAYS_TO_ROUTINE) ?: DEFAULT_TEXT_DAYS_TO_ROUTINE)
    }
    var onRoutineTextLabel by remember {
        mutableStateOf(prefs.getString(KEY_TEXT_ON_ROUTINE, DEFAULT_TEXT_ON_ROUTINE) ?: DEFAULT_TEXT_ON_ROUTINE)
    }
    var insightsShortWindow by remember {
        mutableStateOf(prefs.getInt(KEY_INSIGHTS_SHORT_WINDOW, DEFAULT_INSIGHTS_SHORT_WINDOW))
    }
    var routineWindowOverride by remember {
        mutableStateOf(prefs.getInt(KEY_ROUTINE_WINDOW, 0))
    }
    var defaultScheduleCalendar by remember {
        mutableStateOf(prefs.getBoolean(KEY_DEFAULT_SCHEDULE_CALENDAR, false))
    }
    var useClassicStreakGraph by remember {
        mutableStateOf(prefs.getBoolean(KEY_CLASSIC_STREAK_GRAPH, false))
    }
    var momentumStockMode by remember {
        mutableStateOf(prefs.getBoolean(KEY_MOMENTUM_STOCK_MODE, false))
    }
    var backgroundThemeOptionId by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_BACKGROUND, DEFAULT_THEME_BACKGROUND_ID) ?: DEFAULT_THEME_BACKGROUND_ID
        )
    }
    var statusThemeOptionId by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_STATUS, DEFAULT_THEME_STATUS_ID) ?: DEFAULT_THEME_STATUS_ID
        )
    }
    var doneThemeOptionId by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_DONE, DEFAULT_THEME_DONE_ID) ?: DEFAULT_THEME_DONE_ID
        )
    }
    var bannerThemeOptionId by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_BANNER, DEFAULT_THEME_BANNER_ID) ?: DEFAULT_THEME_BANNER_ID
        )
    }
    var backgroundThemeCustomHex by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_BACKGROUND_CUSTOM_HEX, DEFAULT_THEME_BACKGROUND_CUSTOM_HEX)
                ?: DEFAULT_THEME_BACKGROUND_CUSTOM_HEX
        )
    }
    var statusThemeCustomHex by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_STATUS_CUSTOM_HEX, DEFAULT_THEME_STATUS_CUSTOM_HEX)
                ?: DEFAULT_THEME_STATUS_CUSTOM_HEX
        )
    }
    var doneThemeCustomHex by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_DONE_CUSTOM_HEX, DEFAULT_THEME_DONE_CUSTOM_HEX)
                ?: DEFAULT_THEME_DONE_CUSTOM_HEX
        )
    }
    var bannerThemeCustomHex by remember {
        mutableStateOf(
            prefs.getString(KEY_THEME_BANNER_CUSTOM_HEX, DEFAULT_THEME_BANNER_CUSTOM_HEX)
                ?: DEFAULT_THEME_BANNER_CUSTOM_HEX
        )
    }

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            runCatching {
                exportBackupToUri(
                    context = context,
                    repository = repository,
                    scheduleTitle = scheduleTitle,
                    outputUri = uri
                )
            }
                .onSuccess {
                    settingsFeedback = SettingsFeedback(
                        kind = SettingsFeedbackKind.SUCCESS,
                        title = "Export Complete",
                        message = "Backup exported successfully."
                    )
                }
                .onFailure { error ->
                    settingsFeedback = SettingsFeedback(
                        kind = SettingsFeedbackKind.FAILURE,
                        title = "Export Failed",
                        message = error.message ?: "Unknown error while exporting backup."
                    )
                }
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            runCatching {
                importBackupFromUri(
                    context = context,
                    repository = repository,
                    inputUri = uri
                )
            }
                .onSuccess { imported ->
                    scheduleTitle = imported.scheduleTitle
                    prefs.edit().putString(KEY_SCHEDULE_TITLE, imported.scheduleTitle).apply()
                    selectedTab = RootTab.WORKOUT
                    currentScreen = AppScreen.SCHEDULE
                    selectedDayNumber = 0
                    val feedback = SettingsFeedback(
                        kind = SettingsFeedbackKind.SUCCESS,
                        title = "Import Complete",
                        message = "Backup imported successfully and workout data was refreshed."
                    )
                    settingsFeedback = feedback
                    importResultFeedback = feedback
                }
                .onFailure { error ->
                    val feedback = SettingsFeedback(
                        kind = SettingsFeedbackKind.FAILURE,
                        title = "Import Failed",
                        message = error.message ?: "Invalid or unsupported backup file."
                    )
                    settingsFeedback = feedback
                    importResultFeedback = feedback
                }
        }
    }

    fun requestBackupExport() {
        exportBackupLauncher.launch(generateBackupFileName())
    }

    val baseScheme = MaterialTheme.colorScheme
    val backgroundThemeCustomColor = parseThemeHexColorOrDefault(
        hexValue = backgroundThemeCustomHex,
        fallback = Color(0xFFFFFFFF)
    )
    val statusThemeCustomColor = parseThemeHexColorOrDefault(
        hexValue = statusThemeCustomHex,
        fallback = Color(0xFF1CCBCB)
    )
    val doneThemeCustomColor = parseThemeHexColorOrDefault(
        hexValue = doneThemeCustomHex,
        fallback = Color(0xFF1E9E58)
    )
    val bannerThemeCustomColor = parseThemeHexColorOrDefault(
        hexValue = bannerThemeCustomHex,
        fallback = Color(0xFFBF360C)
    )
    val backgroundThemeOptions = remember(backgroundThemeCustomColor) {
        BACKGROUND_THEME_OPTIONS + ThemeColorOption(
            id = CUSTOM_THEME_OPTION_ID,
            label = "Custom",
            color = backgroundThemeCustomColor
        )
    }
    val statusThemeOptions = remember(statusThemeCustomColor) {
        STATUS_THEME_OPTIONS + ThemeColorOption(
            id = CUSTOM_THEME_OPTION_ID,
            label = "Custom",
            color = statusThemeCustomColor
        )
    }
    val doneThemeOptions = remember(doneThemeCustomColor) {
        DONE_THEME_OPTIONS + ThemeColorOption(
            id = CUSTOM_THEME_OPTION_ID,
            label = "Custom",
            color = doneThemeCustomColor
        )
    }
    val bannerThemeOptions = remember(bannerThemeCustomColor) {
        BANNER_THEME_OPTIONS + ThemeColorOption(
            id = CUSTOM_THEME_OPTION_ID,
            label = "Custom",
            color = bannerThemeCustomColor
        )
    }
    val backgroundThemeColor = resolveThemeColorOption(
        options = backgroundThemeOptions,
        selectedId = backgroundThemeOptionId,
        fallbackId = DEFAULT_THEME_BACKGROUND_ID
    ).color
    val statusThemeColor = resolveThemeColorOption(
        options = statusThemeOptions,
        selectedId = statusThemeOptionId,
        fallbackId = DEFAULT_THEME_STATUS_ID
    ).color
    val doneThemeColor = resolveThemeColorOption(
        options = doneThemeOptions,
        selectedId = doneThemeOptionId,
        fallbackId = DEFAULT_THEME_DONE_ID
    ).color
    val bannerThemeColor = resolveThemeColorOption(
        options = bannerThemeOptions,
        selectedId = bannerThemeOptionId,
        fallbackId = DEFAULT_THEME_BANNER_ID
    ).color

    val secondaryContainerColor = mixWithWhite(statusThemeColor, 0.72f)
    val primaryContainerColor = mixWithWhite(doneThemeColor, 0.72f)
    val themedColorScheme = baseScheme.copy(
        background = backgroundThemeColor,
        primary = doneThemeColor,
        onPrimary = contrastColor(doneThemeColor),
        primaryContainer = primaryContainerColor,
        onPrimaryContainer = contrastColor(primaryContainerColor),
        secondary = statusThemeColor,
        onSecondary = contrastColor(statusThemeColor),
        secondaryContainer = secondaryContainerColor,
        onSecondaryContainer = contrastColor(secondaryContainerColor),
        tertiary = statusThemeColor,
        onTertiary = contrastColor(statusThemeColor),
        tertiaryContainer = mixWithWhite(statusThemeColor, 0.82f),
        onTertiaryContainer = contrastColor(mixWithWhite(statusThemeColor, 0.82f))
    )

    MaterialTheme(colorScheme = themedColorScheme) {
        BackHandler(enabled = selectedTab == RootTab.INSIGHTS || selectedTab == RootTab.SETTINGS) {
            selectedTab = RootTab.WORKOUT
            currentScreen = AppScreen.SCHEDULE
            showExitAppConfirm = false
        }

        BackHandler(enabled = selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.DAY_DETAIL) {
            currentScreen = AppScreen.SCHEDULE
        }

        BackHandler(enabled = selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.SCHEDULE) {
            showExitAppConfirm = true
        }

        BackHandler(enabled = showGraphsPage) {
            showGraphsPage = false
        }

        LaunchedEffect(days, todayDateEpochDay) {
            if (days.isEmpty()) {
                return@LaunchedEffect
            }
            if (days.any { it.dayNumber == selectedDayNumber }) {
                return@LaunchedEffect
            }

            selectedDayNumber = highlightedTodayDayNumber
                ?: days.first().dayNumber
        }

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!isWorkoutSessionActive) {
                    NavigationBar {
                        RootTab.entries.forEach { tab ->
                            val tabLabel = when (tab) {
                                RootTab.WORKOUT -> workoutTabLabel
                                RootTab.INSIGHTS -> insightsTabLabel
                                RootTab.SETTINGS -> settingsTabLabel
                            }

                            NavigationBarItem(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tabLabel
                                    )
                                },
                                label = { Text(tabLabel) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val suppressRootTopInset =
                selectedTab == RootTab.SETTINGS ||
                    (selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.DAY_DETAIL)

            // Horizontal swipe switches between the root tabs (Workout <-> Insights <-> Settings)
            // from their home screens. Disabled during a session, in day detail, and over the
            // graphs overlay, which have their own horizontal interactions.
            val tabSwipeEnabled = !isWorkoutSessionActive &&
                !(selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.DAY_DETAIL) &&
                !showGraphsPage

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        top = if (suppressRootTopInset) 0.dp else innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                    .then(
                        if (tabSwipeEnabled) {
                            Modifier.pointerInput(selectedTab) {
                                var totalDrag = 0f
                                val threshold = 72.dp.toPx()
                                detectHorizontalDragGestures(
                                    onDragStart = { totalDrag = 0f },
                                    onDragEnd = {
                                        val order = RootTab.entries
                                        val index = selectedTab.ordinal
                                        if (totalDrag <= -threshold && index < order.lastIndex) {
                                            selectedTab = order[index + 1]
                                        } else if (totalDrag >= threshold && index > 0) {
                                            selectedTab = order[index - 1]
                                        }
                                    },
                                    onHorizontalDrag = { _, dragAmount ->
                                        totalDrag += dragAmount
                                    }
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val forward = targetState.ordinal > initialState.ordinal
                        if (forward) {
                            (slideInHorizontally(animationSpec = tween(280)) { width -> width } + fadeIn()) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280)) { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally(animationSpec = tween(280)) { width -> -width } + fadeIn()) togetherWith
                                (slideOutHorizontally(animationSpec = tween(280)) { width -> width } + fadeOut())
                        }
                    },
                    label = "tabSwitch"
                ) { animatedTab ->
                when (animatedTab) {
                    RootTab.WORKOUT -> {
                        if (days.isEmpty()) {
                            LoadingScreen()
                        } else {
                            when (currentScreen) {
                                AppScreen.SCHEDULE -> {
                                    ScheduleScreen(
                                        days = days,
                                        planTitle = scheduleTitle,
                                        schedulePageLabel = schedulePageLabel,
                                        infinityPageLabel = infinityPageLabel,
                                        missedBannerText = missedBannerTextLabel,
                                        bannerColor = bannerThemeColor,
                                        defaultCalendarView = defaultScheduleCalendar,
                                        lastCompletedDayNumber = lastCompletedDayNumber,
                                        completedSessionEpochDays = completedSessionEpochDays,
                                        completedWorkoutByDate = completedWorkoutByDate,
                                        completedDayNumberByDate = completedDayNumberByDate,
                                        onLogBackdatedWorkout = { dayNumber, epochDay ->
                                            val day = days.firstOrNull { it.dayNumber == dayNumber }
                                            if (day != null) {
                                                scope.launch { repository.logBackdatedWorkout(day, epochDay) }
                                            }
                                        },
                                        onRemoveWorkoutOnDate = { epochDay ->
                                            scope.launch { repository.removeWorkoutOnDate(epochDay) }
                                        },
                                        onDaySelected = { dayNumber ->
                                            selectedDayNumber = dayNumber
                                            currentScreen = AppScreen.DAY_DETAIL
                                        }
                                    )
                                }

                                AppScreen.DAY_DETAIL -> {
                                    val selectedDay = days.firstOrNull { it.dayNumber == selectedDayNumber }
                                        ?: days.first()

                                    WorkoutDayScreen(
                                        day = selectedDay,
                                        repository = repository,
                                        setLogs = setLogs,
                                        onRequestGoToSettings = {
                                            currentScreen = AppScreen.SCHEDULE
                                            selectedTab = RootTab.SETTINGS
                                        },
                                        onBack = { currentScreen = AppScreen.SCHEDULE },
                                        onWorkoutActiveChange = { active -> isWorkoutSessionActive = active }
                                    )
                                }
                            }
                        }
                    }

                    RootTab.INSIGHTS -> {
                        if (days.isEmpty()) {
                            LoadingScreen()
                        } else {
                            InsightsScreen(
                                sessions = sessions,
                                setLogs = setLogs,
                                days = days,
                                repository = repository,
                                insightsTitle = insightsTitleLabel,
                                workoutInsightsTitle = workoutInsightsTitleLabel,
                                routineTitle = routineTitleLabel,
                                streakTitle = streakTitleLabel,
                                daysToRoutineText = daysToRoutineTextLabel,
                                onRoutineText = onRoutineTextLabel,
                                bannerColor = bannerThemeColor,
                                shortWindowDays = insightsShortWindow,
                                onShortWindowChange = { newWindow ->
                                    val clamped = newWindow.coerceIn(
                                        MIN_INSIGHTS_SHORT_WINDOW,
                                        MAX_INSIGHTS_SHORT_WINDOW
                                    )
                                    insightsShortWindow = clamped
                                    prefs.edit().putInt(KEY_INSIGHTS_SHORT_WINDOW, clamped).apply()
                                },
                                routineWindowOverride = routineWindowOverride,
                                onRoutineWindowChange = { newTarget ->
                                    val clamped = newTarget.coerceIn(
                                        MIN_INSIGHTS_SHORT_WINDOW,
                                        MAX_INSIGHTS_SHORT_WINDOW
                                    )
                                    routineWindowOverride = clamped
                                    prefs.edit().putInt(KEY_ROUTINE_WINDOW, clamped).apply()
                                },
                                useClassicStreakGraph = useClassicStreakGraph,
                                stockMode = momentumStockMode,
                                onOpenGraphs = { showGraphsPage = true }
                            )
                        }
                    }

                    RootTab.SETTINGS -> {
                        SettingsScreen(
                            statusFeedback = settingsFeedback,
                            onDismissStatusFeedback = { settingsFeedback = null },
                            backgroundThemeOptionId = backgroundThemeOptionId,
                            statusThemeOptionId = statusThemeOptionId,
                            doneThemeOptionId = doneThemeOptionId,
                            bannerThemeOptionId = bannerThemeOptionId,
                            onBackgroundThemeOptionChanged = { selectedId ->
                                backgroundThemeOptionId = selectedId
                                prefs.edit().putString(KEY_THEME_BACKGROUND, selectedId).apply()
                            },
                            onStatusThemeOptionChanged = { selectedId ->
                                statusThemeOptionId = selectedId
                                prefs.edit().putString(KEY_THEME_STATUS, selectedId).apply()
                            },
                            onDoneThemeOptionChanged = { selectedId ->
                                doneThemeOptionId = selectedId
                                prefs.edit().putString(KEY_THEME_DONE, selectedId).apply()
                            },
                            onBannerThemeOptionChanged = { selectedId ->
                                bannerThemeOptionId = selectedId
                                prefs.edit().putString(KEY_THEME_BANNER, selectedId).apply()
                            },
                            backgroundThemeOptions = backgroundThemeOptions,
                            statusThemeOptions = statusThemeOptions,
                            doneThemeOptions = doneThemeOptions,
                            bannerThemeOptions = bannerThemeOptions,
                            backgroundCustomColor = backgroundThemeCustomColor,
                            statusCustomColor = statusThemeCustomColor,
                            doneCustomColor = doneThemeCustomColor,
                            bannerCustomColor = bannerThemeCustomColor,
                            onBackgroundCustomColorChanged = { selectedColor ->
                                val hex = colorToHexRgb(selectedColor)
                                backgroundThemeCustomHex = hex
                                backgroundThemeOptionId = CUSTOM_THEME_OPTION_ID
                                prefs.edit()
                                    .putString(KEY_THEME_BACKGROUND_CUSTOM_HEX, hex)
                                    .putString(KEY_THEME_BACKGROUND, CUSTOM_THEME_OPTION_ID)
                                    .apply()
                            },
                            onStatusCustomColorChanged = { selectedColor ->
                                val hex = colorToHexRgb(selectedColor)
                                statusThemeCustomHex = hex
                                statusThemeOptionId = CUSTOM_THEME_OPTION_ID
                                prefs.edit()
                                    .putString(KEY_THEME_STATUS_CUSTOM_HEX, hex)
                                    .putString(KEY_THEME_STATUS, CUSTOM_THEME_OPTION_ID)
                                    .apply()
                            },
                            onDoneCustomColorChanged = { selectedColor ->
                                val hex = colorToHexRgb(selectedColor)
                                doneThemeCustomHex = hex
                                doneThemeOptionId = CUSTOM_THEME_OPTION_ID
                                prefs.edit()
                                    .putString(KEY_THEME_DONE_CUSTOM_HEX, hex)
                                    .putString(KEY_THEME_DONE, CUSTOM_THEME_OPTION_ID)
                                    .apply()
                            },
                            onBannerCustomColorChanged = { selectedColor ->
                                val hex = colorToHexRgb(selectedColor)
                                bannerThemeCustomHex = hex
                                bannerThemeOptionId = CUSTOM_THEME_OPTION_ID
                                prefs.edit()
                                    .putString(KEY_THEME_BANNER_CUSTOM_HEX, hex)
                                    .putString(KEY_THEME_BANNER, CUSTOM_THEME_OPTION_ID)
                                    .apply()
                            },
                            labels = AppLabels(
                                planTitle = scheduleTitle,
                                compactButton = schedulePageLabel,
                                calendarButton = infinityPageLabel,
                                workoutTab = workoutTabLabel,
                                insightsTab = insightsTabLabel,
                                settingsTab = settingsTabLabel,
                                insightsTitle = insightsTitleLabel,
                                workoutInsightsTitle = workoutInsightsTitleLabel,
                                graphsTitle = graphsTitleLabel,
                                themeTitle = themeTitleLabel,
                                labelsTitle = labelsTitleLabel,
                                pageCommandsTitle = pageCommandsTitleLabel,
                                missedBannerText = missedBannerTextLabel,
                                routineTitle = routineTitleLabel,
                                streakTitle = streakTitleLabel,
                                daysToRoutineText = daysToRoutineTextLabel,
                                onRoutineText = onRoutineTextLabel
                            ),
                            onLabelsSaved = { updated ->
                                val cleanPlanTitle = updated.planTitle.trim().ifEmpty { DEFAULT_SCHEDULE_TITLE }
                                val cleanSchedule = updated.compactButton.trim().ifEmpty { DEFAULT_PAGE_LABEL_SCHEDULE }
                                val cleanInfinity = updated.calendarButton.trim().ifEmpty { DEFAULT_PAGE_LABEL_INFINITY }
                                val cleanWorkoutTab = updated.workoutTab.trim().ifEmpty { DEFAULT_TAB_LABEL_WORKOUT }
                                val cleanInsightsTab = updated.insightsTab.trim().ifEmpty { DEFAULT_TAB_LABEL_INSIGHTS }
                                val cleanSettingsTab = updated.settingsTab.trim().ifEmpty { DEFAULT_TAB_LABEL_SETTINGS }
                                val cleanInsightsTitle = updated.insightsTitle.trim().ifEmpty { DEFAULT_TITLE_INSIGHTS }
                                val cleanWorkoutInsightsTitle = updated.workoutInsightsTitle.trim().ifEmpty { DEFAULT_TITLE_WORKOUT_INSIGHTS }
                                val cleanGraphsTitle = updated.graphsTitle.trim().ifEmpty { DEFAULT_TITLE_GRAPHS }
                                val cleanThemeTitle = updated.themeTitle.trim().ifEmpty { DEFAULT_TITLE_THEME }
                                val cleanLabelsTitle = updated.labelsTitle.trim().ifEmpty { DEFAULT_TITLE_LABELS }
                                val cleanPageCommandsTitle = updated.pageCommandsTitle.trim().ifEmpty { DEFAULT_TITLE_PAGE_COMMANDS }
                                val cleanMissedBanner = updated.missedBannerText.trim().ifEmpty { DEFAULT_TITLE_MISSED_BANNER }
                                val cleanRoutineTitle = updated.routineTitle.trim().ifEmpty { DEFAULT_TITLE_ROUTINE }
                                val cleanStreakTitle = updated.streakTitle.trim().ifEmpty { DEFAULT_TITLE_STREAK }
                                val cleanDaysToRoutine = updated.daysToRoutineText.trim().ifEmpty { DEFAULT_TEXT_DAYS_TO_ROUTINE }
                                val cleanOnRoutine = updated.onRoutineText.trim().ifEmpty { DEFAULT_TEXT_ON_ROUTINE }
                                scheduleTitle = cleanPlanTitle
                                schedulePageLabel = cleanSchedule
                                infinityPageLabel = cleanInfinity
                                workoutTabLabel = cleanWorkoutTab
                                insightsTabLabel = cleanInsightsTab
                                settingsTabLabel = cleanSettingsTab
                                insightsTitleLabel = cleanInsightsTitle
                                workoutInsightsTitleLabel = cleanWorkoutInsightsTitle
                                graphsTitleLabel = cleanGraphsTitle
                                themeTitleLabel = cleanThemeTitle
                                labelsTitleLabel = cleanLabelsTitle
                                pageCommandsTitleLabel = cleanPageCommandsTitle
                                missedBannerTextLabel = cleanMissedBanner
                                routineTitleLabel = cleanRoutineTitle
                                streakTitleLabel = cleanStreakTitle
                                daysToRoutineTextLabel = cleanDaysToRoutine
                                onRoutineTextLabel = cleanOnRoutine
                                prefs.edit()
                                    .putString(KEY_SCHEDULE_TITLE, cleanPlanTitle)
                                    .putString(KEY_PAGE_LABEL_SCHEDULE, cleanSchedule)
                                    .putString(KEY_PAGE_LABEL_INFINITY, cleanInfinity)
                                    .putString(KEY_TAB_LABEL_WORKOUT, cleanWorkoutTab)
                                    .putString(KEY_TAB_LABEL_INSIGHTS, cleanInsightsTab)
                                    .putString(KEY_TAB_LABEL_SETTINGS, cleanSettingsTab)
                                    .putString(KEY_TITLE_INSIGHTS, cleanInsightsTitle)
                                    .putString(KEY_TITLE_WORKOUT_INSIGHTS, cleanWorkoutInsightsTitle)
                                    .putString(KEY_TITLE_GRAPHS, cleanGraphsTitle)
                                    .putString(KEY_TITLE_THEME, cleanThemeTitle)
                                    .putString(KEY_TITLE_LABELS, cleanLabelsTitle)
                                    .putString(KEY_TITLE_PAGE_COMMANDS, cleanPageCommandsTitle)
                                    .putString(KEY_TITLE_MISSED_BANNER, cleanMissedBanner)
                                    .putString(KEY_TITLE_ROUTINE, cleanRoutineTitle)
                                    .putString(KEY_TITLE_STREAK, cleanStreakTitle)
                                    .putString(KEY_TEXT_DAYS_TO_ROUTINE, cleanDaysToRoutine)
                                    .putString(KEY_TEXT_ON_ROUTINE, cleanOnRoutine)
                                    .apply()
                            },
                            onExportBackup = {
                                requestBackupExport()
                            },
                            onImportBackup = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                            },
                            defaultScheduleCalendar = defaultScheduleCalendar,
                            onDefaultScheduleCalendarChanged = { useCalendar ->
                                defaultScheduleCalendar = useCalendar
                                prefs.edit().putBoolean(KEY_DEFAULT_SCHEDULE_CALENDAR, useCalendar).apply()
                            },
                            useClassicStreakGraph = useClassicStreakGraph,
                            onUseClassicStreakGraphChanged = { enabled ->
                                useClassicStreakGraph = enabled
                                prefs.edit().putBoolean(KEY_CLASSIC_STREAK_GRAPH, enabled).apply()
                            },
                            momentumStockMode = momentumStockMode,
                            onMomentumStockModeChanged = { enabled ->
                                momentumStockMode = enabled
                                prefs.edit().putBoolean(KEY_MOMENTUM_STOCK_MODE, enabled).apply()
                            }
                        )
                    }
                }
                }
            }
        }

        if (showExitAppConfirm) {
            AlertDialog(
                onDismissRequest = { showExitAppConfirm = false },
                title = { Text("Exit app?") },
                text = { Text("Do you want to exit?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showExitAppConfirm = false
                            activity?.finish()
                        }
                    ) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitAppConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        importResultFeedback?.let { feedback ->
            AlertDialog(
                onDismissRequest = { importResultFeedback = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (feedback.kind == SettingsFeedbackKind.SUCCESS) {
                                Icons.Rounded.CheckCircle
                            } else {
                                Icons.Rounded.Warning
                            },
                            contentDescription = null,
                            tint = if (feedback.kind == SettingsFeedbackKind.SUCCESS) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                        Text(feedback.title)
                    }
                },
                text = { Text(feedback.message) },
                confirmButton = {
                    TextButton(onClick = { importResultFeedback = null }) {
                        Text("Close")
                    }
                }
            )
        }

        if (showGraphsPage) {
            WorkoutGraphsScreen(
                sessions = sessions,
                setLogs = setLogs,
                title = graphsTitleLabel,
                onBack = { showGraphsPage = false }
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    val loadingGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.42f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(loadingGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Preparing workout template...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f)
            )
        }
    }
}
