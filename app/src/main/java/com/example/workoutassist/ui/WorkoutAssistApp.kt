package com.example.workoutassist.ui

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.widget.NumberPicker
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    val label: String,
    val icon: ImageVector
) {
    WORKOUT(label = "Workout", icon = Icons.Rounded.FitnessCenter),
    SETTINGS(label = "Settings", icon = Icons.Rounded.Settings)
}

private data class ThemeColorOption(
    val id: String,
    val label: String,
    val color: Color
)

private enum class SettingsView {
    ROOT,
    THEME_OPTIONS
}

@Composable
private fun ExerciseAttributeField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Text(
        text = "$label : $value",
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 3.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

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
                            minValue = 0
                            maxValue = wheelValues.lastIndex
                            wrapSelectorWheel = wheelValues.size > 1
                            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
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
            TextButton(onClick = { onConfirm(wheelValues[selectedIndex.coerceIn(0, wheelValues.lastIndex)]) }) {
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
private const val KEY_THEME_BACKGROUND = "theme_background"
private const val KEY_THEME_STATUS = "theme_status"
private const val KEY_THEME_DONE = "theme_done"
private const val DEFAULT_THEME_BACKGROUND_ID = "white"
private const val DEFAULT_THEME_STATUS_ID = "turquoise"
private const val DEFAULT_THEME_DONE_ID = "green"
private const val LATEST_DESIGN_VERSION = "1.27"

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

private val LATEST_VERSION_HIGHLIGHTS = listOf(
    "Added Settings color-role customization options.",
    "Added prompt to export backup when exiting edit mode after making changes.",
    "Settings keeps small version badge with tap-to-open details dialog.",
    "Displayed version is sourced from app package version metadata."
)

@Composable
fun WorkoutAssistApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember {
        WorkoutRepository(WorkoutDatabase.getInstance(context).workoutDao())
    }
    val prefs = remember(context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    LaunchedEffect(Unit) {
        repository.ensureSeedData()
    }

    val days by repository.observeDays().collectAsState(initial = emptyList())
    val todayDateEpochDay = currentDateEpochDay()
    val highlightedTodayDayNumber = days
        .firstOrNull { it.plannedDateEpochDay == todayDateEpochDay }
        ?.dayNumber
    var scheduleTitle by remember {
        mutableStateOf(prefs.getString(KEY_SCHEDULE_TITLE, DEFAULT_SCHEDULE_TITLE) ?: DEFAULT_SCHEDULE_TITLE)
    }
    var showScheduleRenameDialog by remember { mutableStateOf(false) }
    var selectedDayNumber by remember { mutableIntStateOf(0) }
    var currentScreen by remember { mutableStateOf(AppScreen.SCHEDULE) }
    var selectedTab by remember { mutableStateOf(RootTab.WORKOUT) }
    var settingsStatusMessage by remember { mutableStateOf<String?>(null) }
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
                    settingsStatusMessage = "Backup exported successfully."
                }
                .onFailure { error ->
                    settingsStatusMessage = "Export failed: ${error.message ?: "Unknown error"}"
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
                    showScheduleRenameDialog = false
                    settingsStatusMessage = "Backup imported successfully."
                }
                .onFailure { error ->
                    settingsStatusMessage = "Import failed: ${error.message ?: "Invalid file"}"
                }
        }
    }

    fun requestBackupExport() {
        exportBackupLauncher.launch(generateBackupFileName())
    }

    val baseScheme = MaterialTheme.colorScheme
    val backgroundThemeColor = resolveThemeColorOption(
        options = BACKGROUND_THEME_OPTIONS,
        selectedId = backgroundThemeOptionId,
        fallbackId = DEFAULT_THEME_BACKGROUND_ID
    ).color
    val statusThemeColor = resolveThemeColorOption(
        options = STATUS_THEME_OPTIONS,
        selectedId = statusThemeOptionId,
        fallbackId = DEFAULT_THEME_STATUS_ID
    ).color
    val doneThemeColor = resolveThemeColorOption(
        options = DONE_THEME_OPTIONS,
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
        BackHandler(enabled = selectedTab == RootTab.SETTINGS) {
            selectedTab = RootTab.WORKOUT
        }

        BackHandler(enabled = selectedTab == RootTab.WORKOUT && currentScreen == AppScreen.DAY_DETAIL) {
            currentScreen = AppScreen.SCHEDULE
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
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                                        scheduleTitle = scheduleTitle,
                                        highlightedTodayDayNumber = highlightedTodayDayNumber,
                                        onRenameTitle = { showScheduleRenameDialog = true },
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

                    RootTab.SETTINGS -> {
                        SettingsScreen(
                            statusMessage = settingsStatusMessage,
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

        if (showScheduleRenameDialog) {
            RenameWorkoutDialog(
                dialogTitle = "Rename Schedule",
                fieldLabel = "Schedule title",
                initialName = scheduleTitle,
                onDismiss = { showScheduleRenameDialog = false },
                onConfirm = { newName ->
                    scheduleTitle = newName
                    prefs.edit().putString(KEY_SCHEDULE_TITLE, newName).apply()
                    showScheduleRenameDialog = false
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    statusMessage: String?,
    backgroundThemeOptionId: String,
    statusThemeOptionId: String,
    doneThemeOptionId: String,
    onBackgroundThemeOptionChanged: (String) -> Unit,
    onStatusThemeOptionChanged: (String) -> Unit,
    onDoneThemeOptionChanged: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = remember(context) { currentAppVersionName(context) }
    var showVersionDetails by remember { mutableStateOf(false) }
    var settingsView by remember { mutableStateOf(SettingsView.ROOT) }

    BackHandler(enabled = settingsView == SettingsView.THEME_OPTIONS) {
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
                        text = if (settingsView == SettingsView.ROOT) "Settings" else "Theme",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (settingsView == SettingsView.THEME_OPTIONS) {
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
                    .padding(16.dp),
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

                    if (!statusMessage.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = statusMessage,
                                modifier = Modifier.padding(14.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
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
                                options = BACKGROUND_THEME_OPTIONS,
                                selectedOptionId = backgroundThemeOptionId,
                                onSelected = onBackgroundThemeOptionChanged
                            )

                            ThemeColorSelector(
                                title = "Status (Exercise cards)",
                                options = STATUS_THEME_OPTIONS,
                                selectedOptionId = statusThemeOptionId,
                                onSelected = onStatusThemeOptionChanged
                            )

                            ThemeColorSelector(
                                title = "Done / Actions",
                                options = DONE_THEME_OPTIONS,
                                selectedOptionId = doneThemeOptionId,
                                onSelected = onDoneThemeOptionChanged
                            )
                        }
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
private fun ScheduleScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = listState.layoutInfo
    val visibleItems = layoutInfo.visibleItemsInfo
    val totalItems = layoutInfo.totalItemsCount
    val canScroll = listState.canScrollBackward || listState.canScrollForward
    if (!canScroll || totalItems == 0 || visibleItems.isEmpty()) {
        return
    }

    val visibleCount = visibleItems.size.coerceAtLeast(1)
    val visibleFraction = (visibleCount.toFloat() / totalItems.toFloat()).coerceIn(0.14f, 1f)

    val averageItemSizePx = visibleItems
        .map { it.size }
        .average()
        .toFloat()
        .coerceAtLeast(1f)
    val firstVisibleProgress = listState.firstVisibleItemIndex +
        (listState.firstVisibleItemScrollOffset / averageItemSizePx)
    val maxStartIndex = (totalItems - visibleCount).coerceAtLeast(1)
    val scrollFraction = (firstVisibleProgress / maxStartIndex.toFloat()).coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .width(5.dp)
            .fillMaxHeight()
            .background(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                shape = RoundedCornerShape(999.dp)
            )
    ) {
        val minThumbHeight = if (maxHeight > 28.dp) 28.dp else maxHeight
        val thumbHeight = (maxHeight * visibleFraction)
            .coerceAtLeast(minThumbHeight)
            .coerceAtMost(maxHeight)
        val maxOffset = (maxHeight - thumbHeight).coerceAtLeast(0.dp)
        val thumbOffset = maxOffset * scrollFraction

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = thumbOffset)
                .fillMaxWidth()
                .height(thumbHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(999.dp)
                )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleScreen(
    days: List<WorkoutDayModel>,
    scheduleTitle: String,
    highlightedTodayDayNumber: Int?,
    onRenameTitle: () -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val scheduleListState = rememberLazyListState()
    val scheduleGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
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
                        text = scheduleTitle,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    IconButton(onClick = onRenameTitle) {
                        Icon(Icons.Rounded.Edit, contentDescription = "Rename Schedule")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(scheduleGradient)
        ) {
            LazyColumn(
                state = scheduleListState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = true,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(days, key = { _, day -> day.dayNumber }) { _, day ->
                    val isToday = day.dayNumber == highlightedTodayDayNumber
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isToday) 132.dp else 86.dp)
                            .clickable { onDaySelected(day.dayNumber) },
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
                                Text(
                                    text = if (isToday) {
                                        "Today • ${formatDateShort(day.plannedDateEpochDay)}"
                                    } else {
                                        "Day ${day.dayNumber} • ${formatDateShort(day.plannedDateEpochDay)}"
                                    },
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = day.workoutName,
                                    style = if (isToday) {
                                        MaterialTheme.typography.headlineSmall
                                    } else {
                                        MaterialTheme.typography.titleMedium
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                                if (isToday) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "${day.exercises.size} exercises",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            if (day.isCompleted) {
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
            }

            ScheduleScrollIndicator(
                listState = scheduleListState,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp, top = 18.dp, bottom = 18.dp)
            )
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
    val canToggleExerciseDone = day.plannedDateEpochDay <= currentDateEpochDay()

    var editMode by remember(day.dayNumber) { mutableStateOf(false) }
    var workoutActive by remember(day.dayNumber) { mutableStateOf(false) }
    var freezeMode by remember(day.dayNumber) { mutableStateOf(true) }

    var showAddDialog by remember(day.dayNumber) { mutableStateOf(false) }
    var editExerciseTarget by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var showRenameWorkoutDialog by remember(day.dayNumber) { mutableStateOf(false) }

    var currentExerciseIndex by remember(day.dayNumber) { mutableIntStateOf(0) }
    var currentSetNumber by remember(day.dayNumber) { mutableIntStateOf(1) }
    var actualRepsInput by remember(day.dayNumber) { mutableStateOf("") }
    var actualWeightInput by remember(day.dayNumber) { mutableStateOf("") }

    var activeSessionId by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var loggedSetCount by remember(day.dayNumber) { mutableIntStateOf(0) }

    var showFinishConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showSummary by remember(day.dayNumber) { mutableStateOf(false) }
    var showAchievementPopup by remember(day.dayNumber) { mutableStateOf(false) }
    var showExitWorkoutModeConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showExportAfterEditPrompt by remember(day.dayNumber) { mutableStateOf(false) }
    var hasEditChangesPendingExport by remember(day.dayNumber) { mutableStateOf(false) }
    var nextExercisePrompt by remember(day.dayNumber) { mutableStateOf<String?>(null) }
    var quickEditExercise by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var quickEditField by remember(day.dayNumber) { mutableStateOf<QuickEditField?>(null) }

    val listState = rememberLazyListState()
    var draggingExerciseId by remember(day.dayNumber) { mutableLongStateOf(-1L) }
    var dragOffsetY by remember(day.dayNumber) { mutableFloatStateOf(0f) }
    var editCollapseSignal by remember(day.dayNumber) { mutableIntStateOf(0) }

    val canEditTemplate = editMode && (!workoutActive || !freezeMode)
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
                plannedDateEpochDay = day.plannedDateEpochDay,
                isDone = !day.isCompleted
            )
        }
        hasEditChangesPendingExport = true
    }

    fun openQuickEditDialog(exercise: ExerciseModel, field: QuickEditField) {
        if (!canEditTemplate) {
            return
        }
        quickEditExercise = exercise
        quickEditField = field
    }

    fun dismissQuickEditDialog() {
        quickEditExercise = null
        quickEditField = null
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
                    plannedWeight = plannedWeight
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

    LaunchedEffect(day.exercises.size) {
        if (day.exercises.isEmpty()) {
            currentExerciseIndex = 0
            currentSetNumber = 1
        } else if (currentExerciseIndex > day.exercises.lastIndex) {
            currentExerciseIndex = day.exercises.lastIndex
            currentSetNumber = 1
        }
    }

    fun startWorkout() {
        if (day.exercises.isEmpty()) {
            return
        }

        scope.launch {
            activeSessionId = repository.startSession(day)
        }

        workoutActive = true
        freezeMode = true
        editMode = false
        currentExerciseIndex = 0
        currentSetNumber = 1
        actualRepsInput = ""
        actualWeightInput = ""
        loggedSetCount = 0
    }

    fun saveCurrentSet() {
        val currentExercise = day.exercises.getOrNull(currentExerciseIndex) ?: return
        val actualReps = actualRepsInput.toIntOrNull()?.coerceIn(1, 50) ?: return
        if (activeSessionId == 0L) {
            return
        }

        scope.launch {
            repository.logSet(
                sessionId = activeSessionId,
                exercise = currentExercise,
                setNumber = currentSetNumber,
                actualReps = actualReps,
                actualWeight = actualWeightInput
            )
        }

        loggedSetCount += 1
        actualRepsInput = ""
        actualWeightInput = ""

        if (currentSetNumber < currentExercise.sets) {
            currentSetNumber += 1
            return
        }

        if (currentExerciseIndex < day.exercises.lastIndex) {
            currentExerciseIndex += 1
            currentSetNumber = 1
            nextExercisePrompt = day.exercises[currentExerciseIndex].name
            return
        }

        showFinishConfirm = true
    }

    fun finishActiveSessionIfAny() {
        if (activeSessionId != 0L) {
            val sessionId = activeSessionId
            scope.launch {
                repository.finishSession(sessionId)
            }
        }
    }

    fun resetWorkoutModeState(showSummaryDialog: Boolean) {
        workoutActive = false
        freezeMode = true
        editMode = false
        activeSessionId = 0L
        showFinishConfirm = false
        showSummary = false
        showExitWorkoutModeConfirm = false
        showSummary = showSummaryDialog
    }

    fun finishWorkout() {
        finishActiveSessionIfAny()
        resetWorkoutModeState(showSummaryDialog = true)
    }

    fun requestBackNavigation() {
        if (workoutActive) {
            showExitWorkoutModeConfirm = true
            return
        }
        onBack()
    }

    fun exitWorkoutModeAndLeave() {
        finishActiveSessionIfAny()
        resetWorkoutModeState(showSummaryDialog = false)
        onBack()
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
                    IconButton(onClick = { requestBackNavigation() }) {
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

                    if (workoutActive) {
                        IconButton(onClick = { freezeMode = !freezeMode }) {
                            Icon(
                                imageVector = if (freezeMode) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = if (freezeMode) "Freeze On" else "Freeze Off"
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = editMode,
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
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                            Text(formatDateShort(day.plannedDateEpochDay))
                                        }
                                    } else {
                                        Text(
                                            text = formatDateShort(day.plannedDateEpochDay),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(onClick = { toggleWorkoutDone() }) {
                                        Icon(
                                            imageVector = if (day.isCompleted) {
                                                Icons.Rounded.CheckCircle
                                            } else {
                                                Icons.Rounded.RadioButtonUnchecked
                                            },
                                            contentDescription = if (day.isCompleted) {
                                                "Mark workout not done"
                                            } else {
                                                "Mark workout done"
                                            },
                                            tint = if (day.isCompleted) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }

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
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(onClick = { showFinishConfirm = true }) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Finish")
                                }
                                Text(
                                    text = if (freezeMode) "Template Frozen" else "Template Unlocked",
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            if (workoutActive) {
                CurrentSetLogger(
                    day = day,
                    currentExerciseIndex = currentExerciseIndex,
                    currentSetNumber = currentSetNumber,
                    actualRepsInput = actualRepsInput,
                    actualWeightInput = actualWeightInput,
                    onActualRepsChanged = { actualRepsInput = it.filter(Char::isDigit).take(2) },
                    onActualWeightChanged = { actualWeightInput = it },
                    onSaveSet = { saveCurrentSet() }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

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
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
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
                                                if (allExercisesDoneAfterToggle && !day.isCompleted) {
                                                    repository.setWorkoutDone(
                                                        dayNumber = day.dayNumber,
                                                        plannedDateEpochDay = day.plannedDateEpochDay,
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
                                isCurrent = workoutActive && index == currentExerciseIndex,
                                currentSetNumber = currentSetNumber,
                                canQuickEdit = canEditTemplate,
                                collapseSignal = editCollapseSignal,
                                isDragging = draggingExerciseId == exercise.id,
                                dragOffsetY = if (draggingExerciseId == exercise.id) dragOffsetY else 0f,
                                onDragStart = { onDragStart(exercise.id) },
                                onDrag = { deltaY -> onDrag(deltaY) },
                                onDragEnd = { onDragEnd() },
                                onEdit = { editExerciseTarget = exercise },
                                onQuickEditSets = {
                                    openQuickEditDialog(exercise, QuickEditField.SETS)
                                },
                                onQuickEditReps = {
                                    openQuickEditDialog(exercise, QuickEditField.REPS)
                                },
                                onQuickEditWeight = {
                                    openQuickEditDialog(exercise, QuickEditField.WEIGHT)
                                },
                                onQuickEditInterval = {
                                    openQuickEditDialog(exercise, QuickEditField.INTERVAL)
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

    if (showAddDialog) {
        ExerciseEditorDialog(
            title = "Add Exercise",
            initialDraft = ExerciseDraft(
                name = "",
                sets = 3,
                reps = 12,
                intervalSeconds = 90,
                plannedWeight = ""
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
                plannedWeight = target.plannedWeight
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
                NumberWheelDialog(
                    title = "Reps",
                    value = activeQuickEditExercise.reps,
                    range = 1..50,
                    valueText = { "$it" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selected ->
                        updateExerciseQuick(activeQuickEditExercise, reps = selected)
                        dismissQuickEditDialog()
                    }
                )
            }

            QuickEditField.WEIGHT -> {
                val selectedHalfKg = ((parseWeightValue(activeQuickEditExercise.plannedWeight) ?: 20f) * 2f)
                    .roundToInt()
                    .coerceIn(0, 600)

                NumberWheelDialog(
                    title = "Weight",
                    value = selectedHalfKg,
                    range = 0..600,
                    valueText = { halfKgStep -> "${formatHalfKgValue(halfKgStep)} kg" },
                    onDismiss = { dismissQuickEditDialog() },
                    onConfirm = { selectedHalfKgValue ->
                        val selectedKg = selectedHalfKgValue / 2f
                        updateExerciseQuick(
                            activeQuickEditExercise,
                            plannedWeight = "${formatKgValue(selectedKg)} kg"
                        )
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

    nextExercisePrompt?.let { nextName ->
        AlertDialog(
            onDismissRequest = { nextExercisePrompt = null },
            title = { Text("Next Exercise") },
            text = { Text("Move to $nextName") },
            confirmButton = {
                TextButton(onClick = { nextExercisePrompt = null }) {
                    Text("Continue")
                }
            }
        )
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
            text = { Text("This will close the active session and show summary.") },
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

    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = { Text("Workout Summary") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Workout: ${day.workoutName}")
                    Text("Exercises: ${day.exercises.size}")
                    Text("Planned sets: ${day.exercises.sumOf { it.sets }}")
                    Text("Logged sets: $loggedSetCount")
                }
            },
            confirmButton = {
                TextButton(onClick = { showSummary = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun CurrentSetLogger(
    day: WorkoutDayModel,
    currentExerciseIndex: Int,
    currentSetNumber: Int,
    actualRepsInput: String,
    actualWeightInput: String,
    onActualRepsChanged: (String) -> Unit,
    onActualWeightChanged: (String) -> Unit,
    onSaveSet: () -> Unit
) {
    val currentExercise = day.exercises.getOrNull(currentExerciseIndex) ?: return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Current Set",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(currentExercise.name)
            Text("Set $currentSetNumber/${currentExercise.sets} - Planned ${currentExercise.reps} reps")
            if (currentExercise.plannedWeight.isNotBlank()) {
                Text("Planned weight: ${currentExercise.plannedWeight}")
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = actualRepsInput,
                onValueChange = onActualRepsChanged,
                label = { Text("Actual reps") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = actualWeightInput,
                onValueChange = onActualWeightChanged,
                label = { Text("Actual weight (optional)") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onSaveSet) {
                Text("Save Set")
            }
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
    onQuickEditSets: () -> Unit,
    onQuickEditReps: () -> Unit,
    onQuickEditWeight: () -> Unit,
    onQuickEditInterval: () -> Unit,
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

            Spacer(modifier = Modifier.height(6.dp))
            ExerciseMetricStrip(
                exercise = exercise,
                canQuickEdit = canQuickEdit,
                isCurrent = isCurrent,
                currentSetNumber = currentSetNumber,
                onQuickEditSets = onQuickEditSets,
                onQuickEditReps = onQuickEditReps,
                onQuickEditWeight = onQuickEditWeight,
                onQuickEditInterval = onQuickEditInterval
            )

            if (detailsExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Column {
                    ExerciseAttributeField(
                        label = "set",
                        value = "${exercise.sets}",
                        enabled = canQuickEdit,
                        onClick = onQuickEditSets
                    )
                    ExerciseAttributeField(
                        label = "reps",
                        value = "${exercise.reps}",
                        enabled = canQuickEdit,
                        onClick = onQuickEditReps
                    )
                    ExerciseAttributeField(
                        label = "weight",
                        value = if (exercise.plannedWeight.isBlank()) "-" else exercise.plannedWeight,
                        enabled = canQuickEdit,
                        onClick = onQuickEditWeight
                    )
                    ExerciseAttributeField(
                        label = "interval",
                        value = "${exercise.intervalSeconds} sec",
                        enabled = canQuickEdit,
                        onClick = onQuickEditInterval
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
                            plannedWeight = plannedWeight.trim()
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
            add(
                ExerciseEntity(
                    id = exercise.optLong("id", 0L),
                    dayNumber = exercise.getInt("dayNumber"),
                    name = exercise.optString("name", "Exercise"),
                    sets = exercise.optInt("sets", 3),
                    reps = exercise.optInt("reps", 12),
                    intervalSeconds = exercise.optInt("intervalSeconds", 90),
                    plannedWeight = exercise.optString("plannedWeight", ""),
                    position = exercise.optInt("position", index + 1),
                    isDone = exercise.optBoolean("isDone", false)
                )
            )
        }
    }
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
