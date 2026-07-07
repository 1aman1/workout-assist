package com.example.workoutassist.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.NumberPicker
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import com.example.workoutassist.data.BackupSnapshot
import com.example.workoutassist.data.ExerciseDraft
import com.example.workoutassist.data.ExerciseEntity
import com.example.workoutassist.data.ExerciseModel
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.TemplateDayEntity
import com.example.workoutassist.data.WorkoutDatabase
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import com.example.workoutassist.data.WorkoutSessionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class AppScreen {
    SCHEDULE,
    DAY_DETAIL
}

private enum class RootTab(
    val icon: ImageVector
) {
    WORKOUT(icon = Icons.Rounded.FitnessCenter),
    INSIGHTS(icon = Icons.Rounded.CheckCircle),
    SETTINGS(icon = Icons.Rounded.Settings)
}

private data class ThemeColorOption(
    val id: String,
    val label: String,
    val color: Color
)

private enum class SettingsView {
    ROOT,
    THEME_OPTIONS,
    LABEL_OPTIONS
}

private enum class SchedulePage {
    SCHEDULE,
    INFINITY
}

private enum class SettingsFeedbackKind {
    SUCCESS,
    FAILURE
}

private data class SettingsFeedback(
    val kind: SettingsFeedbackKind,
    val title: String,
    val message: String
)

private data class AppPageCommand(
    val command: String,
    val description: String
)

@Composable
private fun ExerciseMetricPill(
    shortLabel: String,
    value: String,
    emphasized: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (emphasized) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
    }
    val borderColor = if (emphasized) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
    }

    Row(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(999.dp))
            .border(1.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = shortLabel,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExerciseMetricStrip(
    exercise: ExerciseModel,
    canQuickEdit: Boolean,
    isCurrent: Boolean,
    currentSetNumber: Int,
    onQuickEditSets: () -> Unit,
    onQuickEditReps: () -> Unit,
    onQuickEditWeight: () -> Unit,
    onQuickEditInterval: () -> Unit
) {
    val rowScroll = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rowScroll),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExerciseMetricPill(
            shortLabel = "S",
            value = "${exercise.sets}",
            emphasized = true,
            enabled = canQuickEdit,
            onClick = onQuickEditSets
        )
        ExerciseMetricPill(
            shortLabel = "R",
            value = "${exercise.reps}",
            emphasized = true,
            enabled = canQuickEdit,
            onClick = onQuickEditReps
        )
        ExerciseMetricPill(
            shortLabel = "W",
            value = if (exercise.plannedWeight.isBlank()) "-" else exercise.plannedWeight,
            emphasized = false,
            enabled = canQuickEdit,
            onClick = onQuickEditWeight
        )
        ExerciseMetricPill(
            shortLabel = "Rest",
            value = "${exercise.intervalSeconds}s",
            emphasized = false,
            enabled = canQuickEdit,
            onClick = onQuickEditInterval
        )
        if (isCurrent) {
            ExerciseMetricPill(
                shortLabel = "Set",
                value = "$currentSetNumber/${exercise.sets}",
                emphasized = true,
                enabled = false,
                onClick = {}
            )
        }
    }
}

@Composable
private fun ExerciseSetTable(
    repsBySet: List<Int>,
    weightBySet: List<String>,
    editable: Boolean,
    onEditRepsAt: (Int) -> Unit,
    onEditWeightAt: (Int) -> Unit
) {
    val tableScroll = rememberScrollState()
    val columnCount = maxOf(repsBySet.size, weightBySet.size, 1)
    val repsValues = remember(repsBySet, columnCount) {
        List(columnCount) { index ->
            repsBySet.getOrNull(index)?.toString() ?: "-"
        }
    }
    val weightValues = remember(weightBySet, columnCount) {
        List(columnCount) { index ->
            weightBySet.getOrNull(index)?.ifBlank { "-" } ?: "-"
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(tableScroll)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExerciseSetTableRow(
                    label = "reps",
                    values = repsValues,
                    editable = editable,
                    onValueClickAt = onEditRepsAt
                )
                ExerciseSetTableRow(
                    label = "wgt",
                    values = weightValues,
                    editable = editable,
                    onValueClickAt = onEditWeightAt
                )
            }
        }
    }
}

@Composable
private fun ExerciseSetTableRow(
    label: String,
    values: List<String>,
    editable: Boolean,
    onValueClickAt: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(40.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        values.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .width(58.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = editable, onClick = { onValueClickAt(index) })
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NumberWheelDialog(
    title: String,
    value: Int,
    range: IntRange,
    step: Int = 1,
    valueText: (Int) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val stepSize = step.coerceAtLeast(1)
    val wheelValues = remember(range.first, range.last, stepSize) {
        generateSequence(range.first) { previous -> previous + stepSize }
            .takeWhile { it <= range.last }
            .toList()
            .ifEmpty { listOf(range.first) }
    }
    val initialIndex = remember(value, wheelValues) {
        wheelValues.indices.minByOrNull { index -> abs(wheelValues[index] - value) } ?: 0
    }

    var selectedIndex by remember(value, range.first, range.last, stepSize) {
        mutableIntStateOf(initialIndex)
    }
    val latestOnConfirm by rememberUpdatedState(onConfirm)
    val confirmSelected: () -> Unit = {
        latestOnConfirm(wheelValues[selectedIndex.coerceIn(0, wheelValues.lastIndex)])
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.width(188.dp),
                    factory = { context ->
                        NumberPicker(context).apply {
                            val doubleTapDetector = GestureDetector(
                                context,
                                object : GestureDetector.SimpleOnGestureListener() {
                                    override fun onDown(event: MotionEvent): Boolean {
                                        return true
                                    }

                                    override fun onDoubleTap(event: MotionEvent): Boolean {
                                        confirmSelected()
                                        return true
                                    }
                                }
                            )

                            minValue = 0
                            maxValue = wheelValues.lastIndex
                            wrapSelectorWheel = wheelValues.size > 1
                            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                            setOnTouchListener { _, motionEvent ->
                                doubleTapDetector.onTouchEvent(motionEvent)
                                false
                            }
                            setOnValueChangedListener { _, _, newValue ->
                                selectedIndex = newValue
                            }
                        }
                    },
                    update = { picker ->
                        picker.displayedValues = null
                        picker.minValue = 0
                        picker.maxValue = wheelValues.lastIndex
                        picker.displayedValues = wheelValues.map(valueText).toTypedArray()
                        picker.wrapSelectorWheel = wheelValues.size > 1

                        val safeSelected = selectedIndex.coerceIn(0, wheelValues.lastIndex)
                        if (picker.value != safeSelected) {
                            picker.value = safeSelected
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = confirmSelected) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private enum class QuickEditField {
    SETS,
    REPS,
    WEIGHT,
    INTERVAL
}

private const val DEFAULT_SCHEDULE_TITLE = "Workout Schedule"
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
private const val KEY_PRODUCTION_RESET_20260707_DONE = "production_reset_20260707_done"
private const val DEFAULT_PAGE_LABEL_SCHEDULE = "Schedule"
private const val DEFAULT_PAGE_LABEL_INFINITY = "Infinity"
private const val DEFAULT_TAB_LABEL_WORKOUT = "Workout"
private const val DEFAULT_TAB_LABEL_INSIGHTS = "Insights"
private const val DEFAULT_TAB_LABEL_SETTINGS = "Settings"
private const val DEFAULT_THEME_BACKGROUND_ID = "white"
private const val DEFAULT_THEME_STATUS_ID = "turquoise"
private const val DEFAULT_THEME_DONE_ID = "green"
private const val DEFAULT_THEME_BACKGROUND_CUSTOM_HEX = "#FFFFFF"
private const val DEFAULT_THEME_STATUS_CUSTOM_HEX = "#1CCBCB"
private const val DEFAULT_THEME_DONE_CUSTOM_HEX = "#1E9E58"
private const val CUSTOM_THEME_OPTION_ID = "custom"
private const val LATEST_DESIGN_VERSION = "1.86"

private val WORKOUT_SESSION_START_MESSAGES = listOf(
    "Lift weights and come back!",
    "This is something you won't regret!",
    "hustle for that muscle!",
    "mind plays tricks like exhaustion to skip next rep-but pain is not one of them",
    "No need to stop when you're tired, stop when you're done!",
    "Last time you lifted more with less sweat."
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

private val PAGE_COMMAND_NAMES = listOf(
    AppPageCommand(command = "workout.schedule", description = "Workout tab schedule list"),
    AppPageCommand(command = "workout.infinity", description = "Workout tab infinity list"),
    AppPageCommand(command = "workout.day", description = "Workout day detail"),
    AppPageCommand(command = "workout.session", description = "Active workout session"),
    AppPageCommand(command = "insights.home", description = "Insights tab"),
    AppPageCommand(command = "settings.home", description = "Settings root"),
    AppPageCommand(command = "settings.theme", description = "Settings theme options"),
    AppPageCommand(command = "settings.labels", description = "Settings label options")
)

private val LATEST_VERSION_HIGHLIGHTS = listOf(
    "Insights workout history selector now uses a dropdown instead of chips for a cleaner compact control.",
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
    val todayDateEpochDay = currentDateEpochDay()
    val highlightedTodayDayNumber = days
        .firstOrNull { it.plannedDateEpochDay == todayDateEpochDay }
        ?.dayNumber
    var scheduleTitle by remember {
        mutableStateOf(prefs.getString(KEY_SCHEDULE_TITLE, DEFAULT_SCHEDULE_TITLE) ?: DEFAULT_SCHEDULE_TITLE)
    }
    var selectedDayNumber by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf(AppScreen.SCHEDULE) }
    var selectedTab by remember { mutableStateOf(RootTab.WORKOUT) }
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
        ) { innerPadding ->
            val layoutDirection = LocalLayoutDirection.current
            val suppressRootTopInset =
                selectedTab == RootTab.SETTINGS ||
                    (selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.DAY_DETAIL)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        top = if (suppressRootTopInset) 0.dp else innerPadding.calculateTopPadding(),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )
            ) {
                when (selectedTab) {
                    RootTab.WORKOUT -> {
                        if (days.isEmpty()) {
                            LoadingScreen()
                        } else {
                            when (currentScreen) {
                                AppScreen.SCHEDULE -> {
                                    ScheduleScreen(
                                        days = days,
                                        schedulePageLabel = schedulePageLabel,
                                        infinityPageLabel = infinityPageLabel,
                                        highlightedTodayDayNumber = highlightedTodayDayNumber,
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
                                        onRequestExport = { requestBackupExport() },
                                        onBack = { currentScreen = AppScreen.SCHEDULE }
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
                                setLogs = setLogs
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
                            backgroundThemeOptions = backgroundThemeOptions,
                            statusThemeOptions = statusThemeOptions,
                            doneThemeOptions = doneThemeOptions,
                            backgroundCustomColor = backgroundThemeCustomColor,
                            statusCustomColor = statusThemeCustomColor,
                            doneCustomColor = doneThemeCustomColor,
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
                            schedulePageLabel = schedulePageLabel,
                            infinityPageLabel = infinityPageLabel,
                            workoutTabLabel = workoutTabLabel,
                            insightsTabLabel = insightsTabLabel,
                            settingsTabLabel = settingsTabLabel,
                            onLabelsSaved = { scheduleLabel, infinityLabel, workoutLabel, insightsLabel, settingsLabel ->
                                val cleanSchedule = scheduleLabel.trim().ifEmpty { DEFAULT_PAGE_LABEL_SCHEDULE }
                                val cleanInfinity = infinityLabel.trim().ifEmpty { DEFAULT_PAGE_LABEL_INFINITY }
                                val cleanWorkoutTab = workoutLabel.trim().ifEmpty { DEFAULT_TAB_LABEL_WORKOUT }
                                val cleanInsightsTab = insightsLabel.trim().ifEmpty { DEFAULT_TAB_LABEL_INSIGHTS }
                                val cleanSettingsTab = settingsLabel.trim().ifEmpty { DEFAULT_TAB_LABEL_SETTINGS }
                                schedulePageLabel = cleanSchedule
                                infinityPageLabel = cleanInfinity
                                workoutTabLabel = cleanWorkoutTab
                                insightsTabLabel = cleanInsightsTab
                                settingsTabLabel = cleanSettingsTab
                                prefs.edit()
                                    .putString(KEY_PAGE_LABEL_SCHEDULE, cleanSchedule)
                                    .putString(KEY_PAGE_LABEL_INFINITY, cleanInfinity)
                                    .putString(KEY_TAB_LABEL_WORKOUT, cleanWorkoutTab)
                                    .putString(KEY_TAB_LABEL_INSIGHTS, cleanInsightsTab)
                                    .putString(KEY_TAB_LABEL_SETTINGS, cleanSettingsTab)
                                    .apply()
                            },
                            onExportBackup = {
                                requestBackupExport()
                            },
                            onImportBackup = {
                                importBackupLauncher.launch(arrayOf("application/json", "text/plain"))
                            }
                        )
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
        Text(
            text = "Preparing workout template...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.86f)
        )
    }
}

private data class FinishedWorkoutSessionSnapshot(
    val sessionId: Long,
    val workoutName: String,
    val epochDay: Long,
    val finishedAtMillis: Long
)

private data class WorkoutHistorySnapshot(
    val epochDay: Long,
    val setEntries: List<String>
)

private fun buildSetEntryLines(logs: List<SetLogEntity>): List<String> {
    val repsByWeight = linkedMapOf<String, MutableList<Int>>()

    logs
        .sortedWith(compareBy<SetLogEntity> { it.setNumber }.thenBy { it.loggedAt })
        .forEach { log ->
            val weightLabel = log.actualWeight
                .trim()
                .ifBlank { log.plannedWeight.trim() }
                .ifBlank { "-" }

            repsByWeight
                .getOrPut(weightLabel) { mutableListOf() }
                .add(log.actualReps.coerceAtLeast(0))
        }

    return repsByWeight.map { (weight, repsList) ->
        val repsText = repsList.joinToString(separator = "x") { reps -> reps.toString() }
        "$weight x$repsText"
    }
}

@Composable
private fun InsightsScreen(
    sessions: List<WorkoutSessionEntity>,
    setLogs: List<SetLogEntity>
) {
    val scope = rememberCoroutineScope()
    val todayEpochDay = currentDateEpochDay()
    var refreshNonce by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }

    val refreshRotation by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 550),
        label = "insightsRefreshRotation"
    )

    val finishedSessionSamples = remember(sessions, refreshNonce) {
        sessions
            .asSequence()
            .mapNotNull { session ->
                val finishedAt = session.finishedAt ?: return@mapNotNull null
                val resolvedWorkoutName = session.workoutName
                    .trim()
                    .ifBlank { "Day ${session.dayNumber}" }

                FinishedWorkoutSessionSnapshot(
                    sessionId = session.id,
                    workoutName = resolvedWorkoutName,
                    epochDay = timestampMillisToEpochDay(finishedAt),
                    finishedAtMillis = finishedAt
                )
            }
            .sortedByDescending { sample -> sample.finishedAtMillis }
            .toList()
    }

    val completedSessionEpochDays = remember(finishedSessionSamples, refreshNonce) {
        finishedSessionSamples
            .asSequence()
            .map { sample -> sample.epochDay }
            .toSet()
    }

    val (todayYear, todayMonth, _) = remember(todayEpochDay, refreshNonce) {
        epochDayToYearMonthDay(todayEpochDay)
    }
    val monthStartEpochDay = remember(todayYear, todayMonth, refreshNonce) {
        yearMonthDayToEpochDay(todayYear, todayMonth, 1)
    }
    val nextMonthStartEpochDay = remember(todayYear, todayMonth, refreshNonce) {
        yearMonthDayToEpochDay(todayYear, todayMonth + 1, 1)
    }

    val doneLast7 = remember(completedSessionEpochDays, todayEpochDay, refreshNonce) {
        val startDay = todayEpochDay - 6L
        completedSessionEpochDays.count { day -> day in startDay..todayEpochDay }
    }
    val doneThisMonth = remember(completedSessionEpochDays, monthStartEpochDay, todayEpochDay, refreshNonce) {
        completedSessionEpochDays.count { day -> day in monthStartEpochDay..todayEpochDay }
    }
    val thisMonthWindowDays = remember(monthStartEpochDay, nextMonthStartEpochDay, refreshNonce) {
        (nextMonthStartEpochDay - monthStartEpochDay).toInt().coerceAtLeast(1)
    }

    val setLogsBySessionId = remember(setLogs, refreshNonce) {
        setLogs.groupBy { log -> log.sessionId }
    }

    val finishedSessionIds = remember(finishedSessionSamples, refreshNonce) {
        finishedSessionSamples
            .map { sample -> sample.sessionId }
            .toSet()
    }

    val trackedExerciseNames = remember(setLogs, finishedSessionIds, refreshNonce) {
        val latestLoggedAtByExercise = mutableMapOf<String, Long>()

        setLogs.forEach { log ->
            if (log.sessionId !in finishedSessionIds) {
                return@forEach
            }

            val exerciseName = log.exerciseName.trim().ifBlank { "Exercise" }
            val existing = latestLoggedAtByExercise[exerciseName] ?: Long.MIN_VALUE
            if (log.loggedAt > existing) {
                latestLoggedAtByExercise[exerciseName] = log.loggedAt
            }
        }

        latestLoggedAtByExercise
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }
    var selectedExerciseName by remember { mutableStateOf("") }

    LaunchedEffect(trackedExerciseNames) {
        selectedExerciseName = when {
            trackedExerciseNames.isEmpty() -> ""
            selectedExerciseName in trackedExerciseNames -> selectedExerciseName
            else -> trackedExerciseNames.first()
        }
    }

    val selectedExerciseHistory = remember(
        selectedExerciseName,
        finishedSessionSamples,
        setLogsBySessionId,
        refreshNonce
    ) {
        if (selectedExerciseName.isBlank()) {
            emptyList()
        } else {
            finishedSessionSamples
                .asSequence()
                .mapNotNull { sample ->
                    val logs = setLogsBySessionId[sample.sessionId]
                        .orEmpty()
                        .filter { log ->
                            log.exerciseName.trim().ifBlank { "Exercise" } == selectedExerciseName
                        }

                    if (logs.isEmpty()) {
                        null
                    } else {
                        WorkoutHistorySnapshot(
                            epochDay = sample.epochDay,
                            setEntries = buildSetEntryLines(logs)
                        )
                    }
                }
                .take(8)
                .toList()
        }
    }

    val insightsGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(insightsGradient)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = {
                                refreshNonce += 1
                                scope.launch {
                                    isRefreshing = true
                                    delay(560)
                                    isRefreshing = false
                                }
                            },
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh Stats",
                                modifier = Modifier.graphicsLayer(rotationZ = refreshRotation)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Refresh Stats")
                        }
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "$doneLast7/7",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$doneThisMonth/$thisMonthWindowDays",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = "Finished sessions logged: ${finishedSessionSamples.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            WorkoutSpecificInsightsCard(
                exerciseNames = trackedExerciseNames,
                selectedExerciseName = selectedExerciseName,
                onExerciseSelected = { exerciseName -> selectedExerciseName = exerciseName },
                history = selectedExerciseHistory
            )
        }
    }
}

@Composable
private fun WorkoutSpecificInsightsCard(
    workoutNames: List<String>,
    selectedWorkoutName: String,
    onWorkoutSelected: (String) -> Unit,
    history: List<WorkoutHistorySnapshot>
) {
    val historyGridScroll = rememberScrollState()
    var workoutSelectorExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Workout Insights",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Select a workout to review the last 4-8 same workouts before you start.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (workoutNames.isEmpty()) {
                Text(
                    text = "Finish some workouts to unlock workout-specific history.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { workoutSelectorExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = selectedWorkoutName,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Start
                        )
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = "Open workout selection"
                        )
                    }

                    DropdownMenu(
                        expanded = workoutSelectorExpanded,
                        onDismissRequest = { workoutSelectorExpanded = false }
                    ) {
                        workoutNames.forEach { workoutName ->
                            DropdownMenuItem(
                                text = { Text(workoutName) },
                                onClick = {
                                    onWorkoutSelected(workoutName)
                                    workoutSelectorExpanded = false
                                }
                            )
                        }
                    }
                }

                if (history.isEmpty()) {
                    Text(
                        text = "No completed history for selected workout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Last ${history.size} sessions • ${selectedWorkoutName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(historyGridScroll)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            WorkoutHistoryHeaderRow(history = history)
                            WorkoutHistoryMetricRow(
                                label = "sets",
                                values = history.map { item -> item.setsLogged.toString() }
                            )
                            WorkoutHistoryMetricRow(
                                label = "ex",
                                values = history.map { item -> item.exercisesLogged.toString() }
                            )
                            WorkoutHistoryMetricRow(
                                label = "reps",
                                values = history.map { item -> "${item.actualReps}/${item.plannedReps}" }
                            )
                        }
                    }

                    Text(
                        text = "Columns are chronological dates (oldest to latest).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutHistoryHeaderRow(
    history: List<WorkoutHistorySnapshot>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "date",
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        history.forEach { item ->
            WorkoutHistoryGridCell(
                text = formatDateShort(item.epochDay),
                emphasized = true
            )
        }
    }
}

@Composable
private fun WorkoutHistoryMetricRow(
    label: String,
    values: List<String>
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        values.forEach { value ->
            WorkoutHistoryGridCell(
                text = value,
                emphasized = false
            )
        }
    }
}

@Composable
private fun WorkoutHistoryGridCell(
    text: String,
    emphasized: Boolean
) {
    val cellColor = if (emphasized) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    } else {
        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
    }

    Box(
        modifier = Modifier
            .width(86.dp)
            .background(cellColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (emphasized) FontWeight.SemiBold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    statusFeedback: SettingsFeedback?,
    onDismissStatusFeedback: () -> Unit,
    backgroundThemeOptionId: String,
    statusThemeOptionId: String,
    doneThemeOptionId: String,
    onBackgroundThemeOptionChanged: (String) -> Unit,
    onStatusThemeOptionChanged: (String) -> Unit,
    onDoneThemeOptionChanged: (String) -> Unit,
    backgroundThemeOptions: List<ThemeColorOption>,
    statusThemeOptions: List<ThemeColorOption>,
    doneThemeOptions: List<ThemeColorOption>,
    backgroundCustomColor: Color,
    statusCustomColor: Color,
    doneCustomColor: Color,
    onBackgroundCustomColorChanged: (Color) -> Unit,
    onStatusCustomColorChanged: (Color) -> Unit,
    onDoneCustomColorChanged: (Color) -> Unit,
    schedulePageLabel: String,
    infinityPageLabel: String,
    workoutTabLabel: String,
    insightsTabLabel: String,
    settingsTabLabel: String,
    onLabelsSaved: (String, String, String, String, String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = remember(context) { currentAppVersionName(context) }
    var showVersionDetails by remember { mutableStateOf(false) }
    var settingsView by remember { mutableStateOf(SettingsView.ROOT) }
    val settingsScrollState = rememberScrollState()
    var scheduleLabelInput by remember(schedulePageLabel) { mutableStateOf(schedulePageLabel) }
    var infinityLabelInput by remember(infinityPageLabel) { mutableStateOf(infinityPageLabel) }
    var workoutTabLabelInput by remember(workoutTabLabel) { mutableStateOf(workoutTabLabel) }
    var insightsTabLabelInput by remember(insightsTabLabel) { mutableStateOf(insightsTabLabel) }
    var settingsTabLabelInput by remember(settingsTabLabel) { mutableStateOf(settingsTabLabel) }

    BackHandler(enabled = settingsView != SettingsView.ROOT) {
        settingsView = SettingsView.ROOT
    }

    val settingsGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f)
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = {
                    Text(
                        text = when (settingsView) {
                            SettingsView.ROOT -> "Settings"
                            SettingsView.THEME_OPTIONS -> "Theme"
                            SettingsView.LABEL_OPTIONS -> "Labels"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (settingsView != SettingsView.ROOT) {
                        IconButton(onClick = { settingsView = SettingsView.ROOT }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(settingsGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(settingsScrollState)
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (settingsView == SettingsView.ROOT) {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Labels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Rename workout page and bottom tab labels.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { settingsView = SettingsView.LABEL_OPTIONS },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Options")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Page Command Names",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Use these stable names for quick commands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PAGE_COMMAND_NAMES.forEach { page ->
                                Text(
                                    text = "${page.command} -> ${page.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Configure app colors by role.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { settingsView = SettingsView.THEME_OPTIONS },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Options")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Export saves your full local state to a JSON file. Import restores that state.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onExportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Export to file")
                            }
                            OutlinedButton(
                                onClick = onImportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Import from file")
                            }
                        }
                    }

                    if (statusFeedback != null) {
                        SettingsFeedbackCard(
                            feedback = statusFeedback,
                            onDismiss = onDismissStatusFeedback
                        )
                    }
                } else {
                    when (settingsView) {
                        SettingsView.THEME_OPTIONS -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Theme Options",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Choose color roles for background, status cards, and done/actions.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    ThemeColorSelector(
                                        title = "Background",
                                        options = backgroundThemeOptions,
                                        selectedOptionId = backgroundThemeOptionId,
                                        onSelected = onBackgroundThemeOptionChanged
                                    )
                                    ThemeRgbColorPicker(
                                        title = "Background custom",
                                        color = backgroundCustomColor,
                                        onColorChanged = onBackgroundCustomColorChanged
                                    )

                                    ThemeColorSelector(
                                        title = "Status (Exercise cards)",
                                        options = statusThemeOptions,
                                        selectedOptionId = statusThemeOptionId,
                                        onSelected = onStatusThemeOptionChanged
                                    )
                                    ThemeRgbColorPicker(
                                        title = "Status custom",
                                        color = statusCustomColor,
                                        onColorChanged = onStatusCustomColorChanged
                                    )

                                    ThemeColorSelector(
                                        title = "Done / Actions",
                                        options = doneThemeOptions,
                                        selectedOptionId = doneThemeOptionId,
                                        onSelected = onDoneThemeOptionChanged
                                    )
                                    ThemeRgbColorPicker(
                                        title = "Done / Actions custom",
                                        color = doneCustomColor,
                                        onColorChanged = onDoneCustomColorChanged
                                    )
                                }
                            }
                        }

                        SettingsView.LABEL_OPTIONS -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Label Options",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Rename page buttons and bottom tabs.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    OutlinedTextField(
                                        value = scheduleLabelInput,
                                        onValueChange = { scheduleLabelInput = it },
                                        label = { Text("Schedule button") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = infinityLabelInput,
                                        onValueChange = { infinityLabelInput = it },
                                        label = { Text("Infinity button") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = workoutTabLabelInput,
                                        onValueChange = { workoutTabLabelInput = it },
                                        label = { Text("Workout tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = insightsTabLabelInput,
                                        onValueChange = { insightsTabLabelInput = it },
                                        label = { Text("Insights tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = settingsTabLabelInput,
                                        onValueChange = { settingsTabLabelInput = it },
                                        label = { Text("Settings tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            onLabelsSaved(
                                                scheduleLabelInput,
                                                infinityLabelInput,
                                                workoutTabLabelInput,
                                                insightsTabLabelInput,
                                                settingsTabLabelInput
                                            )
                                        },
                                        enabled = scheduleLabelInput.isNotBlank() &&
                                            infinityLabelInput.isNotBlank() &&
                                            workoutTabLabelInput.isNotBlank() &&
                                            insightsTabLabelInput.isNotBlank() &&
                                            settingsTabLabelInput.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Save labels")
                                    }
                                }
                            }
                        }

                        SettingsView.ROOT -> Unit
                    }
                }
            }

            TextButton(
                onClick = { showVersionDetails = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "v$appVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showVersionDetails) {
        AlertDialog(
            onDismissRequest = { showVersionDetails = false },
            title = { Text("Version v$appVersion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Latest design version: v$LATEST_DESIGN_VERSION",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LATEST_VERSION_HIGHLIGHTS.forEach { line ->
                        Text(
                            text = "- $line",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDetails = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun ThemeColorSelector(
    title: String,
    options: List<ThemeColorOption>,
    selectedOptionId: String,
    onSelected: (String) -> Unit
) {
    val rowScrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rowScrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option.id == selectedOptionId,
                    onClick = { onSelected(option.id) },
                    label = { Text(option.label) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(option.color, CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeRgbColorPicker(
    title: String,
    color: Color,
    onColorChanged: (Color) -> Unit
) {
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .background(color, CircleShape)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
            )
            Text(
                text = "Hex ${colorToHexRgb(color)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "(Auto-uses Custom)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(text = "R: $red", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = red.toFloat(),
            onValueChange = { nextRed ->
                onColorChanged(
                    colorFromRgb(
                        red = nextRed.roundToInt().coerceIn(0, 255),
                        green = green,
                        blue = blue
                    )
                )
            },
            valueRange = 0f..255f
        )

        Text(text = "G: $green", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = green.toFloat(),
            onValueChange = { nextGreen ->
                onColorChanged(
                    colorFromRgb(
                        red = red,
                        green = nextGreen.roundToInt().coerceIn(0, 255),
                        blue = blue
                    )
                )
            },
            valueRange = 0f..255f
        )

        Text(text = "B: $blue", style = MaterialTheme.typography.bodySmall)
        Slider(
            value = blue.toFloat(),
            onValueChange = { nextBlue ->
                onColorChanged(
                    colorFromRgb(
                        red = red,
                        green = green,
                        blue = nextBlue.roundToInt().coerceIn(0, 255)
                    )
                )
            },
            valueRange = 0f..255f
        )
    }
}

@Composable
private fun SettingsFeedbackCard(
    feedback: SettingsFeedback,
    onDismiss: () -> Unit
) {
    val accentColor = when (feedback.kind) {
        SettingsFeedbackKind.SUCCESS -> MaterialTheme.colorScheme.primary
        SettingsFeedbackKind.FAILURE -> MaterialTheme.colorScheme.error
    }
    val containerColor = when (feedback.kind) {
        SettingsFeedbackKind.SUCCESS -> mixWithWhite(accentColor, 0.88f)
        SettingsFeedbackKind.FAILURE -> mixWithWhite(accentColor, 0.9f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.38f)),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    tint = accentColor
                )
                Text(
                    text = feedback.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }

            Text(
                text = feedback.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ScheduleDayCard(
    dayLabel: String,
    workoutName: String,
    exerciseCountText: String?,
    isToday: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    dateLabel: String? = null,
    leadingDateLabel: String? = null,
    dayLabelAlignEnd: Boolean = false
) {
    val cardHeight = when {
        isToday -> 132.dp
        !supportingText.isNullOrBlank() -> 102.dp
        else -> 86.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(if (isToday) 24.dp else 18.dp),
        border = BorderStroke(
            width = if (isToday) 1.2.dp else 0.8.dp,
            color = if (isToday) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (dateLabel.isNullOrBlank()) {
                    if (leadingDateLabel.isNullOrBlank()) {
                        Text(
                            text = dayLabel,
                            modifier = if (dayLabelAlignEnd) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = if (dayLabelAlignEnd) TextAlign.End else TextAlign.Start
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = leadingDateLabel,
                                modifier = Modifier.width(98.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dayLabel,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = workoutName,
                        style = if (isToday) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workoutName,
                            modifier = Modifier.weight(1f),
                            style = if (isToday) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!exerciseCountText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exerciseCountText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!supportingText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isCompleted) {
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Workout done",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SchedulePageSwitcher(
    scheduleLabel: String,
    infinityLabel: String,
    selectedPage: SchedulePage,
    onPageSelected: (SchedulePage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SchedulePageSwitchItem(
                label = scheduleLabel,
                selected = selectedPage == SchedulePage.SCHEDULE,
                onClick = { onPageSelected(SchedulePage.SCHEDULE) },
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            )

            SchedulePageSwitchItem(
                label = infinityLabel,
                selected = selectedPage == SchedulePage.INFINITY,
                onClick = { onPageSelected(SchedulePage.INFINITY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SchedulePageSwitchItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreen(
    days: List<WorkoutDayModel>,
    schedulePageLabel: String,
    infinityPageLabel: String,
    highlightedTodayDayNumber: Int?,
    onDaySelected: (Int) -> Unit
) {
    val orderedDays = remember(days) { days.sortedBy { it.dayNumber } }
    val dayCount = orderedDays.size
    val scheduleScope = rememberCoroutineScope()

    val scheduleListState = rememberLazyListState()
    val infinityListState = rememberLazyListState()
    var selectedPage by remember { mutableStateOf(SchedulePage.SCHEDULE) }
    val todayEpochDay = currentDateEpochDay()
    val infinityHalfCycles = 800
    val infinityTotalItems = if (dayCount == 0) 0 else dayCount * (infinityHalfCycles * 2 + 1)
    val defaultDayIndex = ((highlightedTodayDayNumber ?: 1) - 1).coerceIn(0, (dayCount - 1).coerceAtLeast(0))
    val infinityStartIndex = if (dayCount == 0) 0 else (infinityHalfCycles * dayCount) + defaultDayIndex
    val infinityTodayIndex = remember(
        orderedDays,
        dayCount,
        highlightedTodayDayNumber,
        todayEpochDay,
        infinityHalfCycles,
        infinityTotalItems,
        infinityStartIndex
    ) {
        if (dayCount == 0 || infinityTotalItems == 0) {
            return@remember 0
        }

        val highlightedIndex = highlightedTodayDayNumber
            ?.let { dayNumber -> orderedDays.indexOfFirst { it.dayNumber == dayNumber } }
            ?.takeIf { it >= 0 }
        if (highlightedIndex != null) {
            return@remember ((infinityHalfCycles * dayCount) + highlightedIndex)
                .coerceIn(0, infinityTotalItems - 1)
        }

        val mappedIndex = orderedDays.withIndex().firstNotNullOfOrNull { indexedDay ->
            val diff = todayEpochDay - indexedDay.value.plannedDateEpochDay
            if (diff % dayCount.toLong() != 0L) {
                null
            } else {
                val cycleOffset = (diff / dayCount.toLong())
                    .coerceIn(-infinityHalfCycles.toLong(), infinityHalfCycles.toLong())
                ((cycleOffset + infinityHalfCycles.toLong()) * dayCount + indexedDay.index).toInt()
            }
        }

        (mappedIndex ?: infinityStartIndex).coerceIn(0, infinityTotalItems - 1)
    }

    LaunchedEffect(selectedPage, infinityStartIndex, dayCount) {
        if (
            selectedPage == SchedulePage.INFINITY &&
            dayCount > 0 &&
            infinityListState.firstVisibleItemIndex == 0 &&
            infinityListState.firstVisibleItemScrollOffset == 0
        ) {
            infinityListState.scrollToItem(infinityStartIndex)
        }
    }

    val scheduleGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheduleGradient)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SchedulePageSwitcher(
                    scheduleLabel = schedulePageLabel,
                    infinityLabel = infinityPageLabel,
                    selectedPage = selectedPage,
                    onPageSelected = { selectedPage = it }
                )

                when (selectedPage) {
                    SchedulePage.SCHEDULE -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = scheduleListState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(orderedDays, key = { _, day -> day.dayNumber }) { _, day ->
                                    val isToday = day.dayNumber == highlightedTodayDayNumber
                                    ScheduleDayCard(
                                        dayLabel = "Day ${day.dayNumber}",
                                        workoutName = day.workoutName,
                                        exerciseCountText = null,
                                        leadingDateLabel = formatDateShort(day.plannedDateEpochDay),
                                        isToday = isToday,
                                        isCompleted = day.isCompleted,
                                        dayLabelAlignEnd = true,
                                        onClick = { onDaySelected(day.dayNumber) }
                                    )
                                }
                            }

                        }
                    }

                    SchedulePage.INFINITY -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = infinityListState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (dayCount > 0) {
                                    items(
                                        count = infinityTotalItems,
                                        key = { index -> index }
                                    ) { index ->
                                        val dayIndex = index % dayCount
                                        val cycleOffset = (index / dayCount) - infinityHalfCycles
                                        val day = orderedDays[dayIndex]
                                        val virtualDateEpochDay = day.plannedDateEpochDay +
                                            (cycleOffset.toLong() * dayCount.toLong())
                                        val isToday = virtualDateEpochDay == todayEpochDay
                                        val isCompletedForVirtualDate =
                                            day.completedForDateEpochDay == virtualDateEpochDay
                                        val cycleLabel = when {
                                            cycleOffset == 0 -> "Current cycle"
                                            cycleOffset > 0 -> "Cycle +$cycleOffset"
                                            else -> "Cycle $cycleOffset"
                                        }

                                        ScheduleDayCard(
                                            dayLabel = "Day ${day.dayNumber}",
                                            workoutName = day.workoutName,
                                            exerciseCountText = null,
                                            supportingText = cycleLabel,
                                            dateLabel = formatDateShort(virtualDateEpochDay),
                                            isToday = isToday,
                                            isCompleted = isCompletedForVirtualDate,
                                            onClick = { onDaySelected(day.dayNumber) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    scheduleScope.launch {
                                        infinityListState.animateScrollToItem(infinityTodayIndex)
                                    }
                                },
                                enabled = dayCount > 0,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 14.dp)
                                    .height(46.dp),
                                shape = RoundedCornerShape(999.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDayScreen(
    day: WorkoutDayModel,
    repository: WorkoutRepository,
    onRequestExport: () -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val viewedDateEpochDay = day.plannedDateEpochDay
    val viewedDateIsCompleted = day.completedForDateEpochDay == viewedDateEpochDay
    val canToggleExerciseDone = viewedDateEpochDay <= currentDateEpochDay()

    var editMode by remember(day.dayNumber) { mutableStateOf(false) }
    var workoutActive by remember(day.dayNumber) { mutableStateOf(false) }

    var showAddDialog by remember(day.dayNumber) { mutableStateOf(false) }
    var editExerciseTarget by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var showRenameWorkoutDialog by remember(day.dayNumber) { mutableStateOf(false) }

    var focusedExerciseId by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var selectedSetRepsByExerciseId by remember(day.dayNumber) {
        mutableStateOf<Map<Long, List<Int>>>(emptyMap())
    }
    var editedSetIndexesByExerciseId by remember(day.dayNumber) {
        mutableStateOf<Map<Long, Set<Int>>>(emptyMap())
    }
    var loggedExerciseIds by remember(day.dayNumber) { mutableStateOf<Set<Long>>(emptySet()) }
    var setPickerTarget by remember(day.dayNumber) { mutableStateOf<Pair<Long, Int>?>(null) }

    var activeSessionId by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var sessionStartMessage by remember(day.dayNumber) { mutableStateOf<String?>(null) }

    var showFinishConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showAchievementPopup by remember(day.dayNumber) { mutableStateOf(false) }
    var showExitWorkoutModeConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showExportAfterEditPrompt by remember(day.dayNumber) { mutableStateOf(false) }
    var hasEditChangesPendingExport by remember(day.dayNumber) { mutableStateOf(false) }
    var quickEditExercise by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var quickEditField by remember(day.dayNumber) { mutableStateOf<QuickEditField?>(null) }
    var quickEditSetIndex by remember(day.dayNumber) { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    var draggingExerciseId by remember(day.dayNumber) { mutableLongStateOf(-1L) }
    var dragOffsetY by remember(day.dayNumber) { mutableFloatStateOf(0f) }
    var editCollapseSignal by remember(day.dayNumber) { mutableIntStateOf(0) }

    LaunchedEffect(workoutActive, sessionStartMessage) {
        val message = sessionStartMessage ?: return@LaunchedEffect
        if (!workoutActive) {
            return@LaunchedEffect
        }
        delay(2000)
        if (sessionStartMessage == message) {
            sessionStartMessage = null
        }
    }

    val canEditTemplate = editMode && !workoutActive
    val exerciseListBottomPadding = if (canEditTemplate) 96.dp else 16.dp
    val dayDetailGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.18f)
        )
    )

    fun openDatePicker() {
        val (year, month, dayOfMonth) = epochDayToYearMonthDay(day.plannedDateEpochDay)
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val newDate = yearMonthDayToEpochDay(selectedYear, selectedMonth, selectedDay)
                scope.launch {
                    repository.updateDayDateAndPushForward(day.dayNumber, newDate)
                }
                hasEditChangesPendingExport = true
            },
            year,
            month,
            dayOfMonth
        ).show()
    }

    fun toggleWorkoutDone() {
        scope.launch {
            repository.setWorkoutDone(
                dayNumber = day.dayNumber,
                plannedDateEpochDay = viewedDateEpochDay,
                isDone = !viewedDateIsCompleted
            )
        }
        hasEditChangesPendingExport = true
    }

    fun openQuickEditDialog(exercise: ExerciseModel, field: QuickEditField, setIndex: Int? = null) {
        if (!canEditTemplate) {
            return
        }
        quickEditExercise = exercise
        quickEditField = field
        quickEditSetIndex = setIndex
    }

    fun dismissQuickEditDialog() {
        quickEditExercise = null
        quickEditField = null
        quickEditSetIndex = null
    }

    fun updateExerciseQuick(
        exercise: ExerciseModel,
        sets: Int = exercise.sets,
        reps: Int = exercise.reps,
        intervalSeconds: Int = exercise.intervalSeconds,
        plannedWeight: String = exercise.plannedWeight
    ) {
        hasEditChangesPendingExport = true
        scope.launch {
            repository.updateExercise(
                exercise = exercise,
                draft = ExerciseDraft(
                    name = exercise.name,
                    sets = sets,
                    reps = reps,
                    intervalSeconds = intervalSeconds,
                    plannedWeight = plannedWeight,
                    remarks = exercise.remarks
                )
            )
        }
    }

    fun onDragStart(exerciseId: Long) {
        draggingExerciseId = exerciseId
        dragOffsetY = 0f
    }

    fun onDrag(dragAmountY: Float) {
        if (draggingExerciseId < 0L) {
            return
        }
        dragOffsetY += dragAmountY

        val fromIndex = day.exercises.indexOfFirst { it.id == draggingExerciseId }
        if (fromIndex < 0) {
            return
        }

        val currentVisibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == fromIndex }
        val rowHeight = currentVisibleItem?.size?.toFloat() ?: 1f
        val threshold = rowHeight * 0.55f

        when {
            dragOffsetY > threshold -> {
                if (fromIndex < day.exercises.lastIndex) {
                    val movingId = draggingExerciseId
                    scope.launch {
                        repository.moveExercise(day.dayNumber, movingId, moveBy = 1)
                    }
                    hasEditChangesPendingExport = true
                    dragOffsetY -= rowHeight
                }
            }

            dragOffsetY < -threshold -> {
                if (fromIndex > 0) {
                    val movingId = draggingExerciseId
                    scope.launch {
                        repository.moveExercise(day.dayNumber, movingId, moveBy = -1)
                    }
                    hasEditChangesPendingExport = true
                    dragOffsetY += rowHeight
                }
            }
        }
    }

    fun onDragEnd() {
        draggingExerciseId = -1L
        dragOffsetY = 0f
    }

    fun startWorkout() {
        if (day.exercises.isEmpty()) {
            return
        }

        activeSessionId = 0L
        scope.launch {
            activeSessionId = repository.startSession(day)
        }

        workoutActive = true
        editMode = false
        focusedExerciseId = 0L
        selectedSetRepsByExerciseId = day.exercises.associate { exercise ->
            exercise.id to exercise.plannedRepsBySet
        }
        editedSetIndexesByExerciseId = emptyMap()
        loggedExerciseIds = emptySet()
        setPickerTarget = null
        sessionStartMessage = WORKOUT_SESSION_START_MESSAGES.random()
    }

    fun updateSetRepsSelection(exercise: ExerciseModel, setIndex: Int, selectedReps: Int) {
        val current = selectedSetRepsByExerciseId[exercise.id]
            ?: exercise.plannedRepsBySet
        if (setIndex !in current.indices) {
            return
        }
        val updated = current.toMutableList()
        updated[setIndex] = selectedReps.coerceIn(1, 50)
        selectedSetRepsByExerciseId = selectedSetRepsByExerciseId + (exercise.id to updated.toList())

        val editedSetIndexes = editedSetIndexesByExerciseId[exercise.id].orEmpty() + setIndex
        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId + (exercise.id to editedSetIndexes)
    }

    fun saveFocusedExerciseSets() {
        val focusedExercise = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId } ?: return
        if (activeSessionId == 0L) {
            return
        }
        if (focusedExercise.id in loggedExerciseIds) {
            return
        }

        val selectedReps = selectedSetRepsByExerciseId[focusedExercise.id]
            ?: focusedExercise.plannedRepsBySet
        val normalizedReps = if (selectedReps.size >= focusedExercise.sets) {
            selectedReps.take(focusedExercise.sets)
        } else {
            selectedReps + List(focusedExercise.sets - selectedReps.size) { focusedExercise.reps }
        }

        scope.launch {
            normalizedReps.forEachIndexed { setIndex, reps ->
                repository.logSet(
                    sessionId = activeSessionId,
                    exercise = focusedExercise,
                    setNumber = setIndex + 1,
                    actualReps = reps,
                    actualWeight = ""
                )
            }
        }

        val updatedLogged = loggedExerciseIds + focusedExercise.id
        loggedExerciseIds = updatedLogged

        val focusedIndex = day.exercises.indexOfFirst { exercise -> exercise.id == focusedExercise.id }
        val nextUnloggedExercise = day.exercises
            .drop((focusedIndex + 1).coerceAtLeast(0))
            .firstOrNull { exercise -> exercise.id !in updatedLogged }
            ?: day.exercises.firstOrNull { exercise -> exercise.id !in updatedLogged }

        focusedExerciseId = nextUnloggedExercise?.id ?: 0L
        setPickerTarget = null
    }

    fun finishActiveSessionIfAny() {
        if (activeSessionId != 0L) {
            val sessionId = activeSessionId
            scope.launch {
                repository.finishSession(sessionId)
            }
        }
    }

    fun resetWorkoutModeState() {
        workoutActive = false
        editMode = false
        focusedExerciseId = 0L
        selectedSetRepsByExerciseId = emptyMap()
        editedSetIndexesByExerciseId = emptyMap()
        loggedExerciseIds = emptySet()
        setPickerTarget = null
        sessionStartMessage = null
        activeSessionId = 0L
        showFinishConfirm = false
        showExitWorkoutModeConfirm = false
    }

    fun finishWorkout() {
        finishActiveSessionIfAny()
        resetWorkoutModeState()
    }

    fun requestBackNavigation() {
        if (editMode && !workoutActive) {
            return
        }
        if (workoutActive) {
            showExitWorkoutModeConfirm = true
            return
        }
        onBack()
    }

    fun exitWorkoutModeAndLeave() {
        finishActiveSessionIfAny()
        resetWorkoutModeState()
        onBack()
    }

    BackHandler(enabled = editMode && !workoutActive) {
        // Intentionally consume back while editing to avoid accidental screen exit.
    }

    BackHandler(enabled = workoutActive) {
        showExitWorkoutModeConfirm = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = { requestBackNavigation() },
                        enabled = !editMode
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRenameWorkoutDialog = true },
                        enabled = canEditTemplate
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Rename Workout")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = editMode,
                            enabled = !workoutActive,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    editMode = true
                                    editCollapseSignal += 1
                                    hasEditChangesPendingExport = false
                                    showExportAfterEditPrompt = false
                                } else {
                                    editMode = false
                                    if (hasEditChangesPendingExport) {
                                        showExportAfterEditPrompt = true
                                    }
                                }
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (canEditTemplate) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Exercise")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(dayDetailGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = day.workoutName,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (editMode) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (!workoutActive) {
                                        TextButton(
                                            onClick = { openDatePicker() },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(formatDateShort(viewedDateEpochDay))
                                        }
                                    } else {
                                        Text(
                                            text = formatDateShort(viewedDateEpochDay),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(onClick = { toggleWorkoutDone() }) {
                                        Icon(
                                            imageVector = if (viewedDateIsCompleted) {
                                                Icons.Rounded.CheckCircle
                                            } else {
                                                Icons.Rounded.RadioButtonUnchecked
                                            },
                                            contentDescription = if (viewedDateIsCompleted) {
                                                "Mark workout not done"
                                            } else {
                                                "Mark workout done"
                                            },
                                            tint = if (viewedDateIsCompleted) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = formatDateShort(viewedDateEpochDay),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!workoutActive) {
                            Button(
                                onClick = { startWorkout() },
                                enabled = day.exercises.isNotEmpty() && !editMode,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start Workout")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (workoutActive && !sessionStartMessage.isNullOrBlank()) {
                    WorkoutSessionStartMessageCard(
                        message = sessionStartMessage.orEmpty(),
                        onDismiss = { sessionStartMessage = null }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (workoutActive) {
                    WorkoutActivePage(
                        day = day,
                        isSessionReady = activeSessionId != 0L,
                        focusedExerciseId = focusedExerciseId,
                        selectedSetRepsByExerciseId = selectedSetRepsByExerciseId,
                        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId,
                        loggedExerciseIds = loggedExerciseIds,
                        onFocusExercise = { exerciseId ->
                            if (exerciseId !in loggedExerciseIds) {
                                focusedExerciseId = exerciseId
                            }
                        },
                        onSetTap = { exerciseId, setIndex ->
                            if (exerciseId !in loggedExerciseIds) {
                                setPickerTarget = exerciseId to setIndex
                            }
                        },
                        onLogFocusedExercise = { saveFocusedExerciseSets() },
                        onFinish = { showFinishConfirm = true }
                    )
                } else {
                    if (day.exercises.isEmpty()) {
                        Card(shape = RoundedCornerShape(14.dp)) {
                            Text(
                                text = "No exercises yet. Turn on Edit mode and tap + to add one.",
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = exerciseListBottomPadding)
                    ) {
                        itemsIndexed(day.exercises, key = { _, exercise -> exercise.id }) { index, exercise ->
                            key(exercise.id, exercise.isDone) {
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = { targetValue ->
                                        when (targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> {
                                                if (canToggleExerciseDone) {
                                                    val toggledDone = !exercise.isDone
                                                    val allExercisesDoneAfterToggle = day.exercises.all { item ->
                                                        if (item.id == exercise.id) toggledDone else item.isDone
                                                    }
                                                    scope.launch {
                                                        repository.setExerciseDone(exercise.id, toggledDone)
                                                        if (allExercisesDoneAfterToggle && !viewedDateIsCompleted) {
                                                            repository.setWorkoutDone(
                                                                dayNumber = day.dayNumber,
                                                                plannedDateEpochDay = viewedDateEpochDay,
                                                                isDone = true
                                                            )
                                                            showAchievementPopup = true
                                                        }
                                                    }
                                                }
                                                false
                                            }

                                            SwipeToDismissBoxValue.EndToStart -> false

                                            SwipeToDismissBoxValue.Settled -> true
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = !editMode && canToggleExerciseDone,
                                    enableDismissFromEndToStart = false,
                                    backgroundContent = {
                                        SwipeHintBackground(
                                            targetValue = dismissState.targetValue,
                                            exerciseDone = exercise.isDone
                                        )
                                    }
                                ) {
                                    ExerciseRow(
                                        exercise = exercise,
                                        index = index,
                                        editMode = editMode,
                                        canEditTemplate = canEditTemplate,
                                        canDelete = editMode && canEditTemplate,
                                        isCurrent = false,
                                        currentSetNumber = 1,
                                        canQuickEdit = canEditTemplate,
                                        collapseSignal = editCollapseSignal,
                                        isDragging = draggingExerciseId == exercise.id,
                                        dragOffsetY = if (draggingExerciseId == exercise.id) dragOffsetY else 0f,
                                        onDragStart = { onDragStart(exercise.id) },
                                        onDrag = { deltaY -> onDrag(deltaY) },
                                        onDragEnd = { onDragEnd() },
                                        onEdit = { editExerciseTarget = exercise },
                                        onQuickEditRepsForSet = { setIndex ->
                                            openQuickEditDialog(exercise, QuickEditField.REPS, setIndex)
                                        },
                                        onQuickEditWeightForSet = { setIndex ->
                                            openQuickEditDialog(exercise, QuickEditField.WEIGHT, setIndex)
                                        },
                                        onDelete = {
                                            scope.launch {
                                                repository.deleteExercise(exercise)
                                            }
                                            hasEditChangesPendingExport = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
        }
        }
    }

    if (showAddDialog) {
        ExerciseEditorDialog(
            title = "Add Exercise",
            initialDraft = ExerciseDraft(
                name = "",
                sets = 3,
                reps = 12,
                intervalSeconds = 90,
                plannedWeight = "",
                remarks = ""
            ),
            onDismiss = { showAddDialog = false },
            onSave = { draft ->
                scope.launch {
                    repository.addExercise(day.dayNumber, draft)
                }
                hasEditChangesPendingExport = true
                showAddDialog = false
            }
        )
    }

    editExerciseTarget?.let { target ->
        ExerciseEditorDialog(
            title = "Edit Exercise",
            initialDraft = ExerciseDraft(
                name = target.name,
                sets = target.sets,
                reps = target.reps,
                intervalSeconds = target.intervalSeconds,
                plannedWeight = target.plannedWeight,
                remarks = target.remarks
            ),
            onDismiss = { editExerciseTarget = null },
            onSave = { draft ->
                scope.launch {
                    repository.updateExercise(target, draft)
                }
                hasEditChangesPendingExport = true
                editExerciseTarget = null
            }
        )
    }

    if (showRenameWorkoutDialog) {
        RenameWorkoutDialog(
            initialName = day.workoutName,
            onDismiss = { showRenameWorkoutDialog = false },
            onConfirm = { newName ->
                scope.launch {
                    repository.renameWorkout(day.dayNumber, newName)
                }
                hasEditChangesPendingExport = true
                showRenameWorkoutDialog = false
            }
        )
    }

    val activeQuickEditExercise = quickEditExercise
    val activeQuickEditField = quickEditField
    val activeQuickEditSetIndex = quickEditSetIndex
    if (activeQuickEditExercise != null && activeQuickEditField != null) {
        when (activeQuickEditField) {
            QuickEditField.SETS -> {
                NumberWheelDialog(
                    title = "Sets",
                    value = activeQuickEditExercise.sets,
                    range = 1..8,
                    valueText = { "$it" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selected ->
                        updateExerciseQuick(activeQuickEditExercise, sets = selected)
                        dismissQuickEditDialog()
                    }
                )
            }

            QuickEditField.REPS -> {
                val maxSetIndex = (activeQuickEditExercise.sets - 1).coerceAtLeast(0)
                val targetSetIndex = activeQuickEditSetIndex
                    ?.coerceIn(0, maxSetIndex)
                    ?: 0
                val currentValue = activeQuickEditExercise.plannedRepsBySet
                    .getOrElse(targetSetIndex) { activeQuickEditExercise.reps }

                NumberWheelDialog(
                    title = "Reps (Set ${targetSetIndex + 1})",
                    value = currentValue,
                    range = 1..50,
                    valueText = { "$it" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selected ->
                        hasEditChangesPendingExport = true
                        scope.launch {
                            repository.updateExerciseSetPlan(
                                exercise = activeQuickEditExercise,
                                setIndex = targetSetIndex,
                                plannedReps = selected
                            )
                        }
                        dismissQuickEditDialog()
                    }
                )
            }

            QuickEditField.WEIGHT -> {
                val maxSetIndex = (activeQuickEditExercise.sets - 1).coerceAtLeast(0)
                val targetSetIndex = activeQuickEditSetIndex
                    ?.coerceIn(0, maxSetIndex)
                    ?: 0
                val currentWeight = activeQuickEditExercise.plannedWeightBySet
                    .getOrElse(targetSetIndex) { activeQuickEditExercise.plannedWeight }
                val selectedHalfKg = ((parseWeightValue(currentWeight) ?: 20f) * 2f)
                    .roundToInt()
                    .coerceIn(0, 600)

                NumberWheelDialog(
                    title = "Weight (Set ${targetSetIndex + 1})",
                    value = selectedHalfKg,
                    range = 0..600,
                    valueText = { halfKgStep -> "${formatHalfKgValue(halfKgStep)} kg" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selectedHalfKgValue ->
                        val selectedKg = selectedHalfKgValue / 2f
                        hasEditChangesPendingExport = true
                        scope.launch {
                            repository.updateExerciseSetPlan(
                                exercise = activeQuickEditExercise,
                                setIndex = targetSetIndex,
                                plannedWeight = "${formatKgValue(selectedKg)} kg"
                            )
                        }
                        dismissQuickEditDialog()
                    }
                )
            }

            QuickEditField.INTERVAL -> {
                NumberWheelDialog(
                    title = "Interval",
                    value = activeQuickEditExercise.intervalSeconds,
                    range = 0..600,
                    step = 15,
                    valueText = { "$it sec" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selected ->
                        updateExerciseQuick(activeQuickEditExercise, intervalSeconds = selected)
                        dismissQuickEditDialog()
                    }
                )
            }
        }
    }

    setPickerTarget?.let { (exerciseId, setIndex) ->
        val targetExercise = day.exercises.firstOrNull { exercise -> exercise.id == exerciseId }
        if (targetExercise != null) {
            val selectedForExercise = selectedSetRepsByExerciseId[exerciseId]
                ?: targetExercise.plannedRepsBySet
            val initialValue = selectedForExercise.getOrElse(setIndex) { targetExercise.reps }
            NumberWheelDialog(
                title = "${targetExercise.name} - Set ${setIndex + 1}",
                value = initialValue,
                range = 1..50,
                valueText = { "$it reps" },
                onDismiss = { setPickerTarget = null },
                onConfirm = { selected ->
                    updateSetRepsSelection(
                        exercise = targetExercise,
                        setIndex = setIndex,
                        selectedReps = selected
                    )
                    setPickerTarget = null
                }
            )
        } else {
            setPickerTarget = null
        }
    }

    if (showAchievementPopup) {
        AlertDialog(
            onDismissRequest = { showAchievementPopup = false },
            title = { Text("Achievement") },
            text = { Text("All exercises are done. Workout marked complete.") },
            confirmButton = {
                TextButton(onClick = { showAchievementPopup = false }) {
                    Text("Nice")
                }
            }
        )
    }

    if (showExportAfterEditPrompt) {
        AlertDialog(
            onDismissRequest = {
                showExportAfterEditPrompt = false
                hasEditChangesPendingExport = false
            },
            title = { Text("Export changes?") },
            text = { Text("You changed workout template values. Export a backup now?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportAfterEditPrompt = false
                        hasEditChangesPendingExport = false
                        onRequestExport()
                    }
                ) {
                    Text("Export")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExportAfterEditPrompt = false
                        hasEditChangesPendingExport = false
                    }
                ) {
                    Text("Later")
                }
            }
        )
    }

    if (showExitWorkoutModeConfirm) {
        AlertDialog(
            onDismissRequest = { showExitWorkoutModeConfirm = false },
            title = { Text("Exit workout mode?") },
            text = { Text("This will end the current workout session and return to schedule.") },
            confirmButton = {
                TextButton(onClick = { exitWorkoutModeAndLeave() }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitWorkoutModeConfirm = false }) {
                    Text("Stay")
                }
            }
        )
    }

    if (showFinishConfirm) {
        AlertDialog(
            onDismissRequest = { showFinishConfirm = false },
            title = { Text("Finish day workout?") },
            text = { Text("This will close the active workout session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFinishConfirm = false
                        finishWorkout()
                    }
                ) {
                    Text("Finish")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFinishConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun WorkoutSessionStartMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.64f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss message",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun WorkoutActivePage(
    day: WorkoutDayModel,
    isSessionReady: Boolean,
    focusedExerciseId: Long,
    selectedSetRepsByExerciseId: Map<Long, List<Int>>,
    editedSetIndexesByExerciseId: Map<Long, Set<Int>>,
    loggedExerciseIds: Set<Long>,
    onFocusExercise: (Long) -> Unit,
    onSetTap: (Long, Int) -> Unit,
    onLogFocusedExercise: () -> Unit,
    onFinish: () -> Unit
) {
    val focusedExercise = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId }
    val exerciseStripState = rememberLazyListState()
    val focusedExerciseIndex = remember(day.exercises, focusedExerciseId) {
        day.exercises.indexOfFirst { exercise -> exercise.id == focusedExerciseId }
    }

    LaunchedEffect(focusedExerciseIndex, day.dayNumber) {
        if (focusedExerciseIndex >= 0) {
            exerciseStripState.animateScrollToItem(focusedExerciseIndex)
        }
    }

    val canLogFocusedExercise =
        isSessionReady && focusedExercise != null && focusedExercise.id !in loggedExerciseIds
    val loggedSummaryText = "${loggedExerciseIds.size}/${day.exercises.size} Done"
    var showSessionActions by remember(day.dayNumber) { mutableStateOf(false) }
    var showFocusedExerciseRemark by remember(day.dayNumber) { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        shape = RoundedCornerShape(999.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                        )
                    ) {
                        Text(
                            text = loggedSummaryText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    LazyRow(
                        state = exerciseStripState,
                        modifier = Modifier
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(day.exercises, key = { _, exercise -> exercise.id }) { _, exercise ->
                            val isFocused = focusedExerciseId == exercise.id
                            val isLogged = exercise.id in loggedExerciseIds
                            FilterChip(
                                selected = isFocused,
                                enabled = !isLogged,
                                onClick = { onFocusExercise(exercise.id) },
                                label = {
                                    Text(
                                        if (isLogged) {
                                            "${exercise.name} Done"
                                        } else {
                                            exercise.name
                                        },
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = { showFocusedExerciseRemark = true },
                        enabled = focusedExercise != null
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "View selected exercise remark"
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (focusedExercise == null) {
                    Text(
                        text = "Select any exercise chip above to start logging reps.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = focusedExercise.name,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    WorkoutDataRowReadOnly(
                        label = "Planned reps",
                        value = "${focusedExercise.reps}"
                    )

                    val selectedSetReps = selectedSetRepsByExerciseId[focusedExercise.id]
                        ?: List(focusedExercise.sets) { focusedExercise.reps }
                    val editedSetIndexes = editedSetIndexesByExerciseId[focusedExercise.id].orEmpty()

                    repeat(focusedExercise.sets) { setIndex ->
                        val selectedValue = selectedSetReps.getOrElse(setIndex) { focusedExercise.reps }
                        WorkoutDataRowEditable(
                            label = "Set ${setIndex + 1}",
                            value = "$selectedValue reps",
                            isEdited = setIndex in editedSetIndexes,
                            enabled = focusedExercise.id !in loggedExerciseIds,
                            onClick = { onSetTap(focusedExercise.id, setIndex) }
                        )
                    }
                }
            }
        }

        if (showFocusedExerciseRemark && focusedExercise != null) {
            AlertDialog(
                onDismissRequest = { showFocusedExerciseRemark = false },
                title = { Text("${focusedExercise.name} remark") },
                text = {
                    Text(
                        focusedExercise.remarks.ifBlank {
                            "No remark added for this exercise."
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showFocusedExerciseRemark = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f, fill = true))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLogFocusedExercise,
                    enabled = canLogFocusedExercise,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                ) {
                    Text(if (isSessionReady) "Log Exercise" else "Starting...")
                }

                TextButton(
                    onClick = { showSessionActions = !showSessionActions },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(if (showSessionActions) "Hide Session Actions" else "Show Session Actions")
                }

                if (showSessionActions) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .combinedClickable(onClick = { }, onLongClick = onFinish)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "Long Press to Finish Workout",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutDataRowReadOnly(
    label: String,
    value: String
) {
    val mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.86f)

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = mutedTextColor
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = mutedTextColor
            )
        }
    }
}

@Composable
private fun WorkoutDataRowEditable(
    label: String,
    value: String,
    isEdited: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isEdited) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
    }
    val containerColor = if (isEdited) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isEdited) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: ExerciseModel,
    index: Int,
    editMode: Boolean,
    canEditTemplate: Boolean,
    canDelete: Boolean,
    isCurrent: Boolean,
    currentSetNumber: Int,
    canQuickEdit: Boolean,
    collapseSignal: Int,
    isDragging: Boolean,
    dragOffsetY: Float,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onQuickEditRepsForSet: (Int) -> Unit,
    onQuickEditWeightForSet: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var detailsExpanded by remember(exercise.id) { mutableStateOf(false) }

    // Collapse all cards when edit mode is explicitly enabled.
    LaunchedEffect(collapseSignal) {
        detailsExpanded = false
    }

    Card(
        modifier = Modifier
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .zIndex(if (isDragging) 1f else 0f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isCurrent || isDragging) 1.1.dp else 0.7.dp,
            color = when {
                isDragging -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.62f)
                isCurrent -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = when {
                isDragging -> 8.dp
                isCurrent -> 4.dp
                else -> 1.dp
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                exercise.isDone || isCurrent -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (editMode) {
                    Icon(
                        imageVector = Icons.Outlined.DragHandle,
                        contentDescription = "Drag to reorder",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .pointerInput(canEditTemplate, exercise.id) {
                                if (!canEditTemplate) {
                                    return@pointerInput
                                }
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        onDragStart()
                                    },
                                    onDragCancel = {
                                        onDragEnd()
                                    },
                                    onDragEnd = {
                                        onDragEnd()
                                    },
                                    onDrag = { _, dragAmount ->
                                        onDrag(dragAmount.y)
                                    }
                                )
                            }
                    )
                }

                Text(
                    text = exercise.name,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.SemiBold
                )

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit details") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            enabled = canEditTemplate,
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        if (canDelete) {
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDelete()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("More options soon") },
                            enabled = false,
                            onClick = { menuExpanded = false }
                        )
                    }
                }

                IconButton(onClick = { detailsExpanded = !detailsExpanded }) {
                    Icon(
                        imageVector = if (detailsExpanded) {
                            Icons.Rounded.ExpandMore
                        } else {
                            Icons.AutoMirrored.Rounded.KeyboardArrowRight
                        },
                        contentDescription = if (detailsExpanded) {
                            "Collapse exercise details"
                        } else {
                            "Expand exercise details"
                        }
                    )
                }
            }

            if (detailsExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ExerciseSetTable(
                        repsBySet = exercise.plannedRepsBySet,
                        weightBySet = exercise.plannedWeightBySet,
                        editable = canQuickEdit,
                        onEditRepsAt = onQuickEditRepsForSet,
                        onEditWeightAt = onQuickEditWeightForSet
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                    Text(
                        text = "remarks",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = exercise.remarks.ifBlank { "No remarks" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }

                if (isCurrent) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Current set: $currentSetNumber", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SwipeHintBackground(
    targetValue: SwipeToDismissBoxValue,
    exerciseDone: Boolean
) {
    val backgroundColor = when (targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }
    val labelColor = when (targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimary
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onSurfaceVariant
        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = when (targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> if (exerciseDone) "Mark not done" else "Mark done"
        SwipeToDismissBoxValue.EndToStart -> ""
        SwipeToDismissBoxValue.Settled -> if (exerciseDone) "Swipe right to undo" else "Swipe right"
    }
    val alignment = when (targetValue) {
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = labelColor
        )
    }
}

@Composable
private fun ExerciseEditorDialog(
    title: String,
    initialDraft: ExerciseDraft,
    onDismiss: () -> Unit,
    onSave: (ExerciseDraft) -> Unit
) {
    var name by remember(initialDraft.name) { mutableStateOf(initialDraft.name) }
    var sets by remember(initialDraft.sets) { mutableStateOf(initialDraft.sets.toString()) }
    var reps by remember(initialDraft.reps) { mutableStateOf(initialDraft.reps.toString()) }
    var interval by remember(initialDraft.intervalSeconds) { mutableStateOf(initialDraft.intervalSeconds.toString()) }
    var plannedWeight by remember(initialDraft.plannedWeight) { mutableStateOf(initialDraft.plannedWeight) }
    var remarks by remember(initialDraft.remarks) { mutableStateOf(initialDraft.remarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Exercise name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sets,
                    onValueChange = { sets = it.filter(Char::isDigit).take(1) },
                    label = { Text("Sets (1-8)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = reps,
                    onValueChange = { reps = it.filter(Char::isDigit).take(2) },
                    label = { Text("Reps (1-50)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = interval,
                    onValueChange = { interval = it.filter(Char::isDigit).take(3) },
                    label = { Text("Interval (sec)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = plannedWeight,
                    onValueChange = { plannedWeight = it },
                    label = { Text("Planned weight") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val cleanName = name.trim()
                    if (cleanName.isBlank()) {
                        return@TextButton
                    }
                    onSave(
                        ExerciseDraft(
                            name = cleanName,
                            sets = (sets.toIntOrNull() ?: 3).coerceIn(1, 8),
                            reps = (reps.toIntOrNull() ?: 12).coerceIn(1, 50),
                            intervalSeconds = (interval.toIntOrNull() ?: 90).coerceAtLeast(0),
                            plannedWeight = plannedWeight.trim(),
                            remarks = remarks.trim()
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RenameWorkoutDialog(
    dialogTitle: String = "Rename Workout",
    fieldLabel: String = "Workout name",
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var workoutName by remember(initialName) { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(dialogTitle) },
        text = {
            OutlinedTextField(
                value = workoutName,
                onValueChange = { workoutName = it },
                label = { Text(fieldLabel) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clean = workoutName.trim()
                    if (clean.isNotEmpty()) {
                        onConfirm(clean)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L

private fun currentDateEpochDay(): Long {
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

private fun yearMonthDayToEpochDay(year: Int, month: Int, dayOfMonth: Int): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.clear()
    utc.set(year, month, dayOfMonth, 0, 0, 0)
    utc.set(Calendar.MILLISECOND, 0)
    return utc.timeInMillis / MILLIS_PER_DAY
}

private fun epochDayToYearMonthDay(epochDay: Long): Triple<Int, Int, Int> {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    utc.timeInMillis = epochDay * MILLIS_PER_DAY
    return Triple(
        utc.get(Calendar.YEAR),
        utc.get(Calendar.MONTH),
        utc.get(Calendar.DAY_OF_MONTH)
    )
}

private fun formatDateShort(epochDay: Long): String {
    val formatter = SimpleDateFormat("dd-MMM", Locale.ENGLISH)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(epochDay * MILLIS_PER_DAY))
}

private fun timestampMillisToEpochDay(timestampMillis: Long): Long {
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

private fun parseWeightValue(text: String): Float? {
    return Regex("\\d+(?:\\.\\d+)?")
        .find(text)
        ?.value
        ?.toFloatOrNull()
}

@Suppress("DEPRECATION")
private fun currentAppVersionName(context: Context): String {
    return runCatching {
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
    }
        .getOrDefault("")
        .ifBlank { LATEST_DESIGN_VERSION }
}

private fun resolveThemeColorOption(
    options: List<ThemeColorOption>,
    selectedId: String,
    fallbackId: String
): ThemeColorOption {
    return options.firstOrNull { it.id == selectedId }
        ?: options.firstOrNull { it.id == fallbackId }
        ?: options.first()
}

private fun parseThemeHexColorOrDefault(hexValue: String?, fallback: Color): Color {
    return parseThemeHexColor(hexValue) ?: fallback
}

private fun parseThemeHexColor(hexValue: String?): Color? {
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

private fun colorFromRgb(red: Int, green: Int, blue: Int): Color {
    return Color(
        red = red.coerceIn(0, 255) / 255f,
        green = green.coerceIn(0, 255) / 255f,
        blue = blue.coerceIn(0, 255) / 255f,
        alpha = 1f
    )
}

private fun colorToHexRgb(color: Color): String {
    val red = (color.red * 255f).roundToInt().coerceIn(0, 255)
    val green = (color.green * 255f).roundToInt().coerceIn(0, 255)
    val blue = (color.blue * 255f).roundToInt().coerceIn(0, 255)
    return String.format(Locale.ENGLISH, "#%02X%02X%02X", red, green, blue)
}

private fun mixWithWhite(base: Color, whiteAmount: Float): Color {
    val t = whiteAmount.coerceIn(0f, 1f)
    return Color(
        red = base.red + (1f - base.red) * t,
        green = base.green + (1f - base.green) * t,
        blue = base.blue + (1f - base.blue) * t,
        alpha = 1f
    )
}

private fun contrastColor(color: Color): Color {
    return if (color.luminance() > 0.52f) {
        Color(0xFF0F1720)
    } else {
        Color(0xFFFFFFFF)
    }
}

private fun formatHalfKgValue(halfKgStep: Int): String {
    return formatKgValue(halfKgStep / 2f)
}

private fun formatKgValue(weightKg: Float): String {
    val roundedToSingleDecimal = (weightKg * 10f).roundToInt() / 10f
    return if (roundedToSingleDecimal % 1f == 0f) {
        roundedToSingleDecimal.toInt().toString()
    } else {
        String.format(Locale.ENGLISH, "%.1f", roundedToSingleDecimal)
    }
}

private const val BACKUP_FORMAT_VERSION = 1

private data class ImportedAppState(
    val scheduleTitle: String
)

private fun generateBackupFileName(): String {
    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(Date())
    return "workout-assist-backup-$timestamp.json"
}

private suspend fun exportBackupToUri(
    context: Context,
    repository: WorkoutRepository,
    scheduleTitle: String,
    outputUri: Uri
) {
    val snapshot = repository.exportBackupSnapshot()
    val payload = buildBackupJson(scheduleTitle = scheduleTitle, snapshot = snapshot)

    withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openOutputStream(outputUri, "wt")
            ?: error("Unable to open destination file")
        stream.bufferedWriter().use { writer ->
            writer.write(payload)
        }
    }
}

private suspend fun importBackupFromUri(
    context: Context,
    repository: WorkoutRepository,
    inputUri: Uri
): ImportedAppState {
    val text = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(inputUri)
            ?: error("Unable to open selected file")
        stream.bufferedReader().use { reader ->
            reader.readText()
        }
    }

    val parsed = parseBackupJson(text)
    if (parsed.snapshot.days.isEmpty()) {
        error("Backup does not include workout days")
    }

    repository.importBackupSnapshot(parsed.snapshot)
    return ImportedAppState(scheduleTitle = parsed.scheduleTitle)
}

private data class BackupPayload(
    val scheduleTitle: String,
    val snapshot: BackupSnapshot
)

private fun buildBackupJson(scheduleTitle: String, snapshot: BackupSnapshot): String {
    return JSONObject()
        .put("formatVersion", BACKUP_FORMAT_VERSION)
        .put("scheduleTitle", scheduleTitle)
        .put("exportedAt", System.currentTimeMillis())
        .put(
            "templateDays",
            JSONArray().apply {
                snapshot.days.forEach { day ->
                    put(
                        JSONObject()
                            .put("dayNumber", day.dayNumber)
                            .put("workoutName", day.workoutName)
                            .put("plannedDateEpochDay", day.plannedDateEpochDay)
                            .put("completedForDateEpochDay", day.completedForDateEpochDay)
                    )
                }
            }
        )
        .put(
            "exercises",
            JSONArray().apply {
                snapshot.exercises.forEach { exercise ->
                    put(
                        JSONObject()
                            .put("id", exercise.id)
                            .put("dayNumber", exercise.dayNumber)
                            .put("name", exercise.name)
                            .put("sets", exercise.sets)
                            .put("reps", exercise.reps)
                            .put("intervalSeconds", exercise.intervalSeconds)
                            .put("plannedWeight", exercise.plannedWeight)
                            .put("plannedRepsBySetJson", exercise.plannedRepsBySetJson)
                            .put("plannedWeightBySetJson", exercise.plannedWeightBySetJson)
                            .put("remarks", exercise.remarks)
                            .put("position", exercise.position)
                            .put("isDone", exercise.isDone)
                    )
                }
            }
        )
        .put(
            "workoutSessions",
            JSONArray().apply {
                snapshot.sessions.forEach { session ->
                    put(
                        JSONObject()
                            .put("id", session.id)
                            .put("dayNumber", session.dayNumber)
                            .put("workoutName", session.workoutName)
                            .put("startedAt", session.startedAt)
                            .put("finishedAt", session.finishedAt)
                    )
                }
            }
        )
        .put(
            "setLogs",
            JSONArray().apply {
                snapshot.logs.forEach { log ->
                    put(
                        JSONObject()
                            .put("id", log.id)
                            .put("sessionId", log.sessionId)
                            .put("exerciseId", log.exerciseId)
                            .put("exerciseName", log.exerciseName)
                            .put("setNumber", log.setNumber)
                            .put("plannedReps", log.plannedReps)
                            .put("actualReps", log.actualReps)
                            .put("plannedWeight", log.plannedWeight)
                            .put("actualWeight", log.actualWeight)
                            .put("loggedAt", log.loggedAt)
                    )
                }
            }
        )
        .toString(2)
}

private fun parseBackupJson(text: String): BackupPayload {
    val root = JSONObject(text)
    val formatVersion = root.optInt("formatVersion", 0)
    if (formatVersion <= 0) {
        error("Unsupported backup format")
    }

    val scheduleTitle = root.optString("scheduleTitle", DEFAULT_SCHEDULE_TITLE)

    val days = root.optJSONArray("templateDays").toTemplateDays()
    val exercises = root.optJSONArray("exercises").toExercises()
    val sessions = root.optJSONArray("workoutSessions").toSessions()
    val logs = root.optJSONArray("setLogs").toSetLogs()

    return BackupPayload(
        scheduleTitle = scheduleTitle,
        snapshot = BackupSnapshot(
            days = days,
            exercises = exercises,
            sessions = sessions,
            logs = logs
        )
    )
}

private fun JSONArray?.toTemplateDays(): List<TemplateDayEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val day = getJSONObject(index)
            add(
                TemplateDayEntity(
                    dayNumber = day.getInt("dayNumber"),
                    workoutName = day.optString("workoutName", "Day ${day.optInt("dayNumber", index + 1)} Workout"),
                    plannedDateEpochDay = day.getLong("plannedDateEpochDay"),
                    completedForDateEpochDay = if (day.isNull("completedForDateEpochDay")) {
                        null
                    } else {
                        day.getLong("completedForDateEpochDay")
                    }
                )
            )
        }
    }
}

private fun JSONArray?.toExercises(): List<ExerciseEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val exercise = getJSONObject(index)
            val sets = exercise.optInt("sets", 3)
            val reps = exercise.optInt("reps", 12)
            val plannedWeight = exercise.optString("plannedWeight", "")
            val plannedRepsBySetJson = exercise.optString("plannedRepsBySetJson", "")
                .ifBlank { buildRepsBySetJsonFallback(sets = sets, reps = reps) }
            val plannedWeightBySetJson = exercise.optString("plannedWeightBySetJson", "")
                .ifBlank { buildWeightBySetJsonFallback(sets = sets, plannedWeight = plannedWeight) }

            add(
                ExerciseEntity(
                    id = exercise.optLong("id", 0L),
                    dayNumber = exercise.getInt("dayNumber"),
                    name = exercise.optString("name", "Exercise"),
                    sets = sets,
                    reps = reps,
                    intervalSeconds = exercise.optInt("intervalSeconds", 90),
                    plannedWeight = plannedWeight,
                    plannedRepsBySetJson = plannedRepsBySetJson,
                    plannedWeightBySetJson = plannedWeightBySetJson,
                    remarks = exercise.optString("remarks", ""),
                    position = exercise.optInt("position", index + 1),
                    isDone = exercise.optBoolean("isDone", false)
                )
            )
        }
    }
}

private fun buildRepsBySetJsonFallback(sets: Int, reps: Int): String {
    val safeSets = sets.coerceAtLeast(1)
    return JSONArray().apply {
        repeat(safeSets) {
            put(reps)
        }
    }.toString()
}

private fun buildWeightBySetJsonFallback(sets: Int, plannedWeight: String): String {
    val safeSets = sets.coerceAtLeast(1)
    return JSONArray().apply {
        repeat(safeSets) {
            put(plannedWeight)
        }
    }.toString()
}

private fun JSONArray?.toSessions(): List<WorkoutSessionEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val session = getJSONObject(index)
            add(
                WorkoutSessionEntity(
                    id = session.optLong("id", 0L),
                    dayNumber = session.getInt("dayNumber"),
                    workoutName = session.optString("workoutName", "Workout"),
                    startedAt = session.optLong("startedAt", System.currentTimeMillis()),
                    finishedAt = if (session.isNull("finishedAt")) {
                        null
                    } else {
                        session.getLong("finishedAt")
                    }
                )
            )
        }
    }
}

private fun JSONArray?.toSetLogs(): List<SetLogEntity> {
    if (this == null) {
        return emptyList()
    }
    return buildList {
        for (index in 0 until length()) {
            val log = getJSONObject(index)
            add(
                SetLogEntity(
                    id = log.optLong("id", 0L),
                    sessionId = log.getLong("sessionId"),
                    exerciseId = log.optLong("exerciseId", 0L),
                    exerciseName = log.optString("exerciseName", "Exercise"),
                    setNumber = log.optInt("setNumber", 1),
                    plannedReps = log.optInt("plannedReps", 0),
                    actualReps = log.optInt("actualReps", 0),
                    plannedWeight = log.optString("plannedWeight", ""),
                    actualWeight = log.optString("actualWeight", ""),
                    loggedAt = log.optLong("loggedAt", System.currentTimeMillis())
                )
            )
        }
    }
}
