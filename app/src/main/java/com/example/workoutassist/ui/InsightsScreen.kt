package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import com.example.workoutassist.data.WorkoutSessionEntity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class FinishedWorkoutSessionSnapshot(
    val sessionId: Long,
    val dayNumber: Int,
    val workoutName: String,
    val epochDay: Long,
    val finishedAtMillis: Long
)

private data class WorkoutHistorySnapshot(
    val sessionId: Long,
    val exerciseId: Long,
    val epochDay: Long,
    val setLogs: List<SetLogEntity>
)

private fun formatSetLogEntry(log: SetLogEntity): String {
    val weightLabel = log.actualWeight
        .trim()
        .ifBlank { log.plannedWeight.trim() }
        .ifBlank { "-" }
    val reps = log.actualReps.coerceAtLeast(0)
    return "$weightLabel x$reps"
}

@Composable
internal fun InsightsScreen(
    sessions: List<WorkoutSessionEntity>,
    setLogs: List<SetLogEntity>,
    days: List<WorkoutDayModel>,
    repository: WorkoutRepository,
    insightsTitle: String = "Insights",
    workoutInsightsTitle: String = "Workout Insights",
    routineTitle: String = "routine",
    streakTitle: String = "Streak momentum",
    daysToRoutineText: String = "days to get back on routine",
    onRoutineText: String = "You're on routine",
    bannerColor: Color = Color(0xFFBF360C),
    shortWindowDays: Int = 7,
    onShortWindowChange: (Int) -> Unit = {},
    routineWindowOverride: Int = 0,
    onRoutineWindowChange: (Int) -> Unit = {},
    useClassicStreakGraph: Boolean = false,
    stockMode: Boolean = false,
    crashMode: Boolean = false,
    pendingColor: Color = Color(0xFF2563EB),
    onOpenGraphs: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val todayEpochDay = currentDateEpochDay()
    var showWorkoutInsights by remember { mutableStateOf(false) }
    var showWindowPicker by remember { mutableStateOf(false) }
    var showRoutineWindowPicker by remember { mutableStateOf(false) }
    // Full-screen inspector so a tall streak graph can be examined with 2D scrolling.
    var showMomentumInspector by remember { mutableStateOf(false) }

    val finishedSessionSamples = remember(sessions) {
        sessions
            .asSequence()
            .mapNotNull { session ->
                val finishedAt = session.finishedAt ?: return@mapNotNull null
                val resolvedWorkoutName = session.workoutName
                    .trim()
                    .ifBlank { "Day ${session.dayNumber}" }

                FinishedWorkoutSessionSnapshot(
                    sessionId = session.id,
                    dayNumber = session.dayNumber,
                    workoutName = resolvedWorkoutName,
                    epochDay = timestampMillisToEpochDay(finishedAt),
                    finishedAtMillis = finishedAt
                )
            }
            .sortedByDescending { sample -> sample.finishedAtMillis }
            .toList()
    }

    val completedSessionEpochDays = remember(finishedSessionSamples) {
        finishedSessionSamples
            .asSequence()
            .map { sample -> sample.epochDay }
            .toSet()
    }

    val doneShort = remember(completedSessionEpochDays, todayEpochDay, shortWindowDays) {
        completedInWindow(completedSessionEpochDays, todayEpochDay, shortWindowDays.coerceIn(5, 15))
    }
    val doneLast30 = remember(completedSessionEpochDays, todayEpochDay) {
        completedInWindow(completedSessionEpochDays, todayEpochDay, 30)
    }
    // Back-to-routine metric: consecutive most-recent days that each have a session
    // (a rest day auto-logs, so a day only breaks the streak if a scheduled workout is
    // missed). Today counts once it has a session; while today is still unlogged the
    // streak is measured up to yesterday so the in-progress day isn't penalized.
    val cycleLength = remember(days, routineWindowOverride) {
        routineWindowOverride.takeIf { it in 5..15 } ?: (days.size.takeIf { it > 0 } ?: 7)
    }
    val routineStreak = remember(completedSessionEpochDays, todayEpochDay) {
        computeRoutineStreak(completedSessionEpochDays, todayEpochDay)
    }
    val onRoutine = routineStreak >= cycleLength

    // Momentum: streak length per calendar day. Completed days climb 1,2,3...; each missed
    // day is a 0 so consecutive misses are all visible. Dates drive the graph's x-axis.
    val momentumEntries = remember(completedSessionEpochDays, todayEpochDay) {
        buildMomentumEntries(completedSessionEpochDays, todayEpochDay)
    }
    // Crash mode ("gimmick"): miss runs fall progressively below zero instead of
    // flatlining at 0, like a stock crashing further each day it doesn't recover.
    val displayMomentumEntries = remember(momentumEntries, crashMode) {
        if (crashMode) applyMissCrashDepth(momentumEntries) else momentumEntries
    }
    val momentumSeries = remember(displayMomentumEntries) { displayMomentumEntries.map { it.value } }
    val momentumDayLabels = remember(momentumEntries) {
        momentumEntries.map { epochDayToDayOfMonth(it.epochDay) }
    }
    // Today shows as a distinct "still pending" color until it's logged done or the day
    // passes unlogged (at which point it becomes a normal miss on the next render).
    val momentumPendingToday = momentumEntries.lastOrNull()?.status == MomentumDayStatus.PENDING
    val bestStreak = remember(completedSessionEpochDays) {
        streakRunLengths(completedSessionEpochDays).maxOrNull() ?: 0
    }

    // Streak breaks (missed scheduled days). Lifted to the top level so the summary
    // can be shown both on the main card and inside the inspector.
    val breakDays = remember(completedSessionEpochDays, todayEpochDay) {
        streakBreakDays(completedSessionEpochDays, todayEpochDay)
    }
    val breaksThisMonth = remember(breakDays, todayEpochDay) {
        val start = startOfMonthEpochDay(todayEpochDay, 0)
        breakDays.count { it in start..todayEpochDay }
    }

    val setLogsBySessionId = remember(setLogs) {
        setLogs.groupBy { log -> log.sessionId }
    }

    val trackedWorkoutNames = remember(finishedSessionSamples) {
        finishedSessionSamples
            .map { sample -> sample.workoutName }
            .distinct()
    }
    val dayNumberByWorkoutName = remember(finishedSessionSamples) {
        finishedSessionSamples.associate { sample -> sample.workoutName to sample.dayNumber }
    }
    var selectedWorkoutName by remember { mutableStateOf("") }

    LaunchedEffect(trackedWorkoutNames) {
        selectedWorkoutName = when {
            trackedWorkoutNames.isEmpty() -> ""
            selectedWorkoutName in trackedWorkoutNames -> selectedWorkoutName
            else -> trackedWorkoutNames.first()
        }
    }

    val exerciseNamesForSelectedWorkout = remember(
        selectedWorkoutName,
        finishedSessionSamples,
        setLogsBySessionId,
        days
    ) {
        if (selectedWorkoutName.isBlank()) {
            emptyList()
        } else {
            val earliestLoggedAtByExercise = mutableMapOf<String, Long>()

            finishedSessionSamples
                .asSequence()
                .filter { sample -> sample.workoutName == selectedWorkoutName }
                .forEach { sample ->
                    setLogsBySessionId[sample.sessionId].orEmpty().forEach { log ->
                        val exerciseName = log.exerciseName.trim().ifBlank { "Exercise" }
                        val existing = earliestLoggedAtByExercise[exerciseName] ?: Long.MAX_VALUE
                        if (log.loggedAt < existing) {
                            earliestLoggedAtByExercise[exerciseName] = log.loggedAt
                        }
                    }
                }

            // Order by the workout's template exercise sequence (position); anything not
            // in the current template falls back to first-logged time, then name.
            val positionByExerciseName = days
                .firstOrNull { it.workoutName == selectedWorkoutName }
                ?.exercises
                ?.associate { exercise -> exercise.name.trim() to exercise.position }
                .orEmpty()

            earliestLoggedAtByExercise.keys.sortedWith(
                compareBy(
                    { positionByExerciseName[it] ?: Int.MAX_VALUE },
                    { earliestLoggedAtByExercise[it] ?: Long.MAX_VALUE },
                    { it }
                )
            )
        }
    }
    var selectedExerciseName by remember { mutableStateOf("") }

    LaunchedEffect(selectedWorkoutName, exerciseNamesForSelectedWorkout) {
        selectedExerciseName = when {
            exerciseNamesForSelectedWorkout.isEmpty() -> ""
            selectedExerciseName in exerciseNamesForSelectedWorkout -> selectedExerciseName
            else -> exerciseNamesForSelectedWorkout.first()
        }
    }

    val selectedExerciseHistory = remember(
        selectedWorkoutName,
        selectedExerciseName,
        finishedSessionSamples,
        setLogsBySessionId
    ) {
        if (selectedWorkoutName.isBlank() || selectedExerciseName.isBlank()) {
            emptyList()
        } else {
            finishedSessionSamples
                .asSequence()
                .filter { sample -> sample.workoutName == selectedWorkoutName }
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
                            sessionId = sample.sessionId,
                            exerciseId = logs.first().exerciseId,
                            epochDay = sample.epochDay,
                            setLogs = logs.sortedWith(
                                compareBy<SetLogEntity> { it.setNumber }
                                    .thenBy { it.loggedAt }
                            )
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

    BackHandler(enabled = showWorkoutInsights) {
        showWorkoutInsights = false
    }

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
            if (showWorkoutInsights) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = { showWorkoutInsights = false }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Text(
                        text = workoutInsightsTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                WorkoutSpecificInsightsCard(
                    workoutNames = trackedWorkoutNames,
                    dayNumberByWorkoutName = dayNumberByWorkoutName,
                    selectedWorkoutName = selectedWorkoutName,
                    onWorkoutSelected = { workoutName -> selectedWorkoutName = workoutName },
                    exerciseNames = exerciseNamesForSelectedWorkout,
                    selectedExerciseName = selectedExerciseName,
                    onExerciseSelected = { exerciseName -> selectedExerciseName = exerciseName },
                    history = selectedExerciseHistory,
                    onUpdateSetLog = { logId, actualReps, actualWeight ->
                        scope.launch {
                            repository.updateSetLogEntry(
                                logId = logId,
                                actualReps = actualReps,
                                actualWeight = actualWeight
                            )
                        }
                    },
                    onDeleteSetLog = { logId, sessionId ->
                        scope.launch {
                            repository.deleteSetLogEntry(
                                logId = logId,
                                sessionId = sessionId
                            )
                        }
                    },
                    onDeleteDateEntry = { sessionId, exerciseId ->
                        scope.launch {
                            repository.deleteExerciseHistoryForSession(
                                sessionId = sessionId,
                                exerciseId = exerciseId
                            )
                        }
                    }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = insightsTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        HorizontalDivider()

                        if (useClassicStreakGraph) {
                            RoutineBatteryBar(
                                label = routineTitle,
                                streak = routineStreak,
                                total = cycleLength,
                                remainingColor = bannerColor,
                                onDoubleTap = { showRoutineWindowPicker = true }
                            )
                        } else {
                            StreakMomentumGraph(
                                momentumSeries = momentumSeries,
                                dayLabels = momentumDayLabels,
                                title = streakTitle,
                                stockMode = stockMode,
                                crashMode = crashMode,
                                pendingColor = pendingColor,
                                pendingToday = momentumPendingToday,
                                onInspect = { showMomentumInspector = true }
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StreakSummaryChip(
                                label = "Current streak",
                                value = "$routineStreak ${if (routineStreak == 1) "day" else "days"}"
                            )
                            StreakSummaryChip(
                                label = "Best streak",
                                value = "$bestStreak ${if (bestStreak == 1) "day" else "days"}"
                            )
                            StreakSummaryChip(
                                label = "Breaks this month",
                                value = breaksThisMonth.toString()
                            )
                        }
                        if (onRoutine) {
                            Text(
                                text = onRoutineText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        HorizontalDivider()

                        RatioBatteryBar(
                            label = "Last $shortWindowDays days",
                            filled = doneShort,
                            total = shortWindowDays,
                            remainingColor = bannerColor,
                            onDoubleTap = { showWindowPicker = true }
                        )
                        RatioBatteryBar(
                            label = "Last 30 days",
                            filled = doneLast30,
                            total = 30,
                            remainingColor = bannerColor
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workoutInsightsTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { showWorkoutInsights = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Open $workoutInsightsTitle"
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progress Graphs (Beta)",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = onOpenGraphs) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "Open Progress Graphs"
                            )
                        }
                    }
                }
            }
        }
    }

    if (showWindowPicker) {
        NumberWheelDialog(
            title = "Recent-days window",
            value = shortWindowDays,
            range = 5..15,
            valueText = { days -> "$days days" },
            onDismiss = { showWindowPicker = false },
            onConfirm = { newWindow ->
                onShortWindowChange(newWindow)
                showWindowPicker = false
            }
        )
    }

    if (showRoutineWindowPicker) {
        NumberWheelDialog(
            title = "Routine target days",
            value = cycleLength,
            range = 5..15,
            valueText = { days -> "$days days" },
            onDismiss = { showRoutineWindowPicker = false },
            onConfirm = { newTarget ->
                onRoutineWindowChange(newTarget)
                showRoutineWindowPicker = false
            }
        )
    }

    if (showMomentumInspector && momentumSeries.isNotEmpty()) {
        // Unlike the compact card (which auto-zooms the Y-axis to squeeze the whole range
        // into one view), the inspector uses a fixed dp-per-streak-unit scale, like a stock
        // chart, and pans in both directions: horizontally through time (unchanged) and
        // vertically through the value range, instead of shrinking everything to fit.
        val inspectorViewportHeight = 340.dp
        val baseInspectorUnitHeight = 26.dp
        val baseInspectorCandleWidth = 12.dp
        val baseInspectorCandleGap = 10.dp
        val baseInspectorBarWidth = 14.dp
        val baseInspectorBarGap = 6.dp
        val inspectorZoomMin = 0.5f
        val inspectorZoomMax = 2.5f
        val inspectorZoomStep = 0.25f
        var inspectorZoom by remember { mutableStateOf(1f) }
        val inspectorHScroll = rememberScrollState()
        val inspectorVScroll = rememberScrollState()
        // Re-anchor on today whenever the data, mode, or zoom level changes (zooming resizes
        // the content, so without this the view could land somewhere other than today) - the
        // same "always anchored on today" guarantee as the compact card, just pannable here.
        LaunchedEffect(stockMode, inspectorZoom, inspectorHScroll.maxValue) {
            inspectorHScroll.scrollTo(inspectorHScroll.maxValue)
        }
        LaunchedEffect(momentumSeries, stockMode, inspectorZoom, inspectorVScroll.maxValue) {
            // Land on the bottom of the value range (today's baseline + the date axis),
            // same "anchored on now" feel as the horizontal scroll; scroll up to see peaks.
            inspectorVScroll.scrollTo(inspectorVScroll.maxValue)
        }
        // How many streaks were 1, 2, ... 7+ days long (a length-frequency histogram).
        val streakHistogram = remember(completedSessionEpochDays) {
            streakLengthHistogram(completedSessionEpochDays, maxBucket = 7)
        }
        val streakRuns = remember(completedSessionEpochDays) {
            streakRunLengths(completedSessionEpochDays)
        }
        val breaksLast3Months = remember(breakDays, todayEpochDay) {
            val start = startOfMonthEpochDay(todayEpochDay, 2)
            breakDays.count { it in start..todayEpochDay }
        }
        val longestGap = remember(completedSessionEpochDays, todayEpochDay) {
            longestStreakGap(completedSessionEpochDays, todayEpochDay)
        }
        val avgStreak = if (streakRuns.isEmpty()) 0.0 else streakRuns.average()
        Dialog(
            onDismissRequest = { showMomentumInspector = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .padding(vertical = 24.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = streakTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { showMomentumInspector = false }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                    // Zoom the fixed chart scale in/out (both axes together) instead of
                    // relying on auto-fit; pinned above the pannable chart area.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { inspectorZoom = (inspectorZoom - inspectorZoomStep).coerceAtLeast(inspectorZoomMin) },
                            enabled = inspectorZoom > inspectorZoomMin
                        ) {
                            Icon(imageVector = Icons.Rounded.ZoomOut, contentDescription = "Zoom out")
                        }
                        Text(
                            text = "${(inspectorZoom * 100).roundToInt()}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = { inspectorZoom = (inspectorZoom + inspectorZoomStep).coerceAtMost(inspectorZoomMax) },
                            enabled = inspectorZoom < inspectorZoomMax
                        ) {
                            Icon(imageVector = Icons.Rounded.ZoomIn, contentDescription = "Zoom in")
                        }
                    }
                    val inspectorAxisMax = (momentumSeries.maxOrNull() ?: 1).coerceAtLeast(1)
                    val inspectorAxisMin = (momentumSeries.minOrNull() ?: 0).coerceAtMost(0)
                    val inspectorRange = (inspectorAxisMax - inspectorAxisMin).coerceAtLeast(1)
                    // Fixed scale (times the current zoom level): content grows taller/wider
                    // as the range/zoom grows, instead of being squashed into a fixed view.
                    val inspectorContentHeight = baseInspectorUnitHeight * inspectorZoom * inspectorRange
                    val inspectorCandleWidth = baseInspectorCandleWidth * inspectorZoom
                    val inspectorCandleGap = baseInspectorCandleGap * inspectorZoom
                    val inspectorBarWidth = baseInspectorBarWidth * inspectorZoom
                    val inspectorBarGap = baseInspectorBarGap * inspectorZoom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(inspectorViewportHeight)
                            .verticalScroll(inspectorVScroll),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MomentumYAxis(
                            minValue = inspectorAxisMin,
                            maxValue = inspectorAxisMax,
                            height = inspectorContentHeight,
                            verticalInset = MomentumDateLabelGutter
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(inspectorHScroll)
                        ) {
                            // Below/at 100% zoom, candles/points pack in tight enough that
                            // their date labels would overlap each other, so hide the dates
                            // until the user zooms in past 100% for room to show them again.
                            val inspectorShowDates = inspectorZoom > 1f
                            if (stockMode) {
                                MomentumCandleChart(
                                    values = momentumSeries,
                                    dayLabels = momentumDayLabels.drop(1),
                                    upColor = Color(0xFF16A34A),
                                    downColor = Color(0xFFDC2626),
                                    pendingColor = pendingColor,
                                    pendingLast = momentumPendingToday,
                                    crashMode = crashMode,
                                    candleWidth = inspectorCandleWidth,
                                    gap = inspectorCandleGap,
                                    contentHeight = inspectorContentHeight,
                                    showDateLabels = inspectorShowDates
                                )
                            } else {
                                MomentumLineChart(
                                    values = momentumSeries,
                                    dayLabels = momentumDayLabels,
                                    lineColor = MaterialTheme.colorScheme.primary,
                                    pendingColor = pendingColor,
                                    pendingLast = momentumPendingToday,
                                    barWidth = inspectorBarWidth,
                                    gap = inspectorBarGap,
                                    contentHeight = inspectorContentHeight,
                                    showDateLabels = inspectorShowDates
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    Text(
                        text = "Consistency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        MetricRow(
                            label = "Current streak",
                            value = "$routineStreak ${if (routineStreak == 1) "day" else "days"}"
                        )
                        MetricRow(
                            label = "Best streak",
                            value = "$bestStreak ${if (bestStreak == 1) "day" else "days"}"
                        )
                        MetricRow("Breaks this month", breaksThisMonth.toString())
                        MetricRow("Breaks last 3 months", breaksLast3Months.toString())
                        MetricRow("Active days", completedSessionEpochDays.size.toString())
                        MetricRow("Streaks", streakRuns.size.toString())
                        MetricRow("Avg streak", "${"%.1f".format(avgStreak)} days")
                        MetricRow(
                            label = "Longest gap",
                            value = "$longestGap ${if (longestGap == 1) "day" else "days"}"
                        )
                    }
                    HorizontalDivider()
                    Text(
                        text = "Streak lengths",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        streakHistogram.forEachIndexed { index, count ->
                            val days = index + 1
                            val label = when {
                                days >= 7 -> "7+ days"
                                days == 1 -> "1 day"
                                else -> "$days days"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// The routine streak visualization (default). A horizontally scrollable momentum graph
// with a Bars/Stocks toggle; the classic triangle graph is shown instead via Settings.
@Composable
private fun StreakMomentumGraph(
    momentumSeries: List<Int>,
    dayLabels: List<Int>,
    title: String,
    stockMode: Boolean,
    crashMode: Boolean,
    pendingColor: Color,
    pendingToday: Boolean,
    onInspect: () -> Unit
) {
    val chartScroll = rememberScrollState()
    // The compact card is always anchored on today (right-most) and isn't user-draggable,
    // so it never drifts away and "gets lost" scrolled into old history; the chevron opens
    // the inspector, which is the dedicated place to pan/zoom through the full timeline.
    LaunchedEffect(momentumSeries, stockMode, chartScroll.maxValue) {
        chartScroll.scrollTo(chartScroll.maxValue)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (momentumSeries.isNotEmpty()) {
                IconButton(onClick = onInspect) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Expand streak graph"
                    )
                }
            }
        }
        if (momentumSeries.isEmpty()) {
            Text(
                text = "Finish a few workouts to see your streak momentum build up here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val chartHeight = 180.dp
            // The compact card no longer auto-fits its y-scale to the full data range (that
            // squashed everything down whenever a big streak or crash appeared); it now uses
            // a fixed -5..+5 window, matching the inspector's "fixed scale, scroll to see
            // more" feel, and stays anchored on today (right-most) via `chartScroll` above.
            val compactRange = -5..5
            val axisMin = compactRange.first
            val axisMax = compactRange.last
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                MomentumYAxis(
                    minValue = axisMin,
                    maxValue = axisMax,
                    height = chartHeight,
                    verticalInset = MomentumDateLabelGutter
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(chartScroll, enabled = false)
                ) {
                    if (stockMode) {
                        MomentumCandleChart(
                            values = momentumSeries,
                            dayLabels = dayLabels.drop(1),
                            upColor = Color(0xFF16A34A),
                            downColor = Color(0xFFDC2626),
                            pendingColor = pendingColor,
                            pendingLast = pendingToday,
                            crashMode = crashMode,
                            contentHeight = chartHeight,
                            fixedRange = compactRange
                        )
                    } else {
                        MomentumLineChart(
                            values = momentumSeries,
                            dayLabels = dayLabels,
                            lineColor = MaterialTheme.colorScheme.primary,
                            pendingColor = pendingColor,
                            pendingLast = pendingToday,
                            contentHeight = chartHeight,
                            fixedRange = compactRange
                        )
                    }
                }
            }
        }
    }
}

// Compact highlighted stat shown on the main streak card (Best streak / Breaks this month).
@Composable
private fun RowScope.StreakSummaryChip(label: String, value: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// A label : value row for the inspector's streak metrics.
@Composable
private fun MetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// Fixed left value-axis (streak counts) for the momentum charts. Sits outside the
// horizontal scroll so the scale stays visible while the chart scrolls. `verticalInset`
// must match the chart's own top/bottom label gutter so the tick marks line up with the
// actual candle/point positions.
@Composable
private fun MomentumYAxis(
    minValue: Int,
    maxValue: Int,
    height: Dp,
    verticalInset: Dp = 0.dp,
    modifier: Modifier = Modifier
) {
    val lo = minValue.coerceAtMost(0)
    val hi = maxValue.coerceAtLeast(lo + 1)
    val range = (hi - lo).toFloat()
    val ticks = remember(lo, hi) { momentumYTicks(lo, hi) }
    val textHeight = 14.dp
    val totalHeight = height + verticalInset * 2
    Box(
        modifier = modifier
            .height(totalHeight)
            .width(22.dp)
    ) {
        ticks.forEach { tick ->
            val frac = (tick - lo) / range
            val y = (verticalInset + height * (1f - frac) - textHeight * 0.5f).coerceIn(0.dp, totalHeight - textHeight)
            Text(
                text = tick.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = y)
            )
        }
    }
}

// Evenly spaced streak values (max .. min) used as Y-axis ticks. Small ranges show every
// integer; larger ranges are thinned to about five labels. Zero is always included so the
// baseline reads clearly once crash mode pushes misses below it.
private fun momentumYTicks(minValue: Int, maxValue: Int): List<Int> {
    val lo = minValue.coerceAtMost(0)
    val hi = maxValue.coerceAtLeast(lo + 1)
    val range = hi - lo
    if (range <= 6) return (hi downTo lo).toList()
    val step = kotlin.math.ceil(range / 5.0).toInt().coerceAtLeast(1)
    val ticks = sortedSetOf(lo, hi, 0)
    var v = lo + step
    while (v < hi) {
        ticks.add(v)
        v += step
    }
    return ticks.toList().sortedDescending()
}

// Space reserved above/below the zero axis inside the chart canvas for the date labels
// that now sit directly on the axis line (see `MomentumCandleChart`/`MomentumLineChart`).
private val MomentumDateLabelGutter = 16.dp

// Stock-market gimmick: draw each day-over-day change in streak as a candle. A climb
// (green) rises one step; a break (red) drops from the streak peak all the way to zero.
// The very last candle can be "pending" (today, not yet logged) — drawn in a distinct
// color instead of red so it doesn't look like a confirmed miss.
@Composable
private fun MomentumCandleChart(
    values: List<Int>,
    dayLabels: List<Int>,
    upColor: Color,
    downColor: Color,
    pendingColor: Color,
    pendingLast: Boolean,
    contentHeight: Dp,
    crashMode: Boolean = false,
    candleWidth: Dp = 12.dp,
    gap: Dp = 10.dp,
    labelGutter: Dp = MomentumDateLabelGutter,
    // When set, pins the y-scale to this range instead of auto-fitting to the data (values
    // outside it are clamped/cropped, like scrolling a stock chart at a fixed price scale).
    fixedRange: IntRange? = null,
    // At low zoom, candles get too narrow for their date labels to fit without overlapping
    // their neighbors, so callers hide the labels below a zoom threshold.
    showDateLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) return
    val maxValue = fixedRange?.last ?: (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val minValue = fixedRange?.first ?: (values.minOrNull() ?: 0).coerceAtMost(0)
    val range = (maxValue - minValue).coerceAtLeast(1)
    val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val textMeasurer = rememberTextMeasurer()
    val candleCount = values.size - 1
    val chartWidth = candleWidth * candleCount + gap * (candleCount - 1).coerceAtLeast(0)
    Canvas(modifier = modifier.height(contentHeight + labelGutter * 2).width(chartWidth)) {
        val cw = candleWidth.toPx()
        val g = gap.toPx()
        val gutterPx = labelGutter.toPx()
        val innerHeight = size.height - gutterPx * 2
        val radius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        val minBody = 3.dp.toPx()
        val labelGapPx = 2.dp.toPx()
        fun toY(value: Int) = gutterPx + innerHeight * (1f - (value - minValue).toFloat() / range)
        val zeroY = toY(0)
        if (minValue < 0) {
            drawLine(
                color = baselineColor,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }
        for (i in 1 until values.size) {
            val open = values[i - 1]
            val close = values[i]
            val isPending = pendingLast && i == values.size - 1
            // A non-positive close means a missed day: draw it red (or crash-mode negative).
            // The first miss after a run drops from the streak peak; a continuing miss (in
            // classic mode) shows a 1-unit red tick so it stays visible.
            val isMiss = close <= 0 && !isPending
            val color = if (isPending) pendingColor else if (isMiss) downColor else upColor
            var topValue: Int
            var bottomValue: Int
            if (isPending && crashMode) {
                // Show the possibility of recovery: stretch from the deepest point the
                // crash reached so far up to +1 (a fresh streak start), so a long miss
                // run reads as a long blue candle with room to climb back out.
                topValue = 1
                bottomValue = minOf(open, close)
            } else if (!crashMode && (isPending || isMiss)) {
                topValue = maxOf(open, 1)
                bottomValue = 0
            } else {
                topValue = maxOf(open, close)
                bottomValue = minOf(open, close)
            }
            if (fixedRange != null) {
                topValue = topValue.coerceIn(minValue, maxValue)
                bottomValue = bottomValue.coerceIn(minValue, maxValue)
            }
            val topY = toY(topValue)
            val bottomY = toY(bottomValue)
            val left = (i - 1) * (cw + g)
            var bodyTop = topY
            var bodyHeight = bottomY - topY
            if (bodyHeight < minBody) {
                bodyTop = ((topY + bottomY) / 2f) - minBody / 2f
                bodyHeight = minBody
            }
            drawRoundRect(
                color = color,
                topLeft = Offset(left, bodyTop),
                size = Size(cw, bodyHeight),
                cornerRadius = radius
            )
            // The date sits right on the zero axis, under the candle by default; if the
            // candle dips below zero (crash mode) it flips above the axis instead, so the
            // label never overlaps the body.
            val label = dayLabels.getOrNull(i - 1)?.toString()
            if (showDateLabels && label != null) {
                val layout = textMeasurer.measure(label, style = labelStyle)
                val labelAbove = bottomValue < 0
                val labelX = (left + cw / 2f - layout.size.width / 2f)
                    .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
                val labelY = if (labelAbove) {
                    zeroY - labelGapPx - layout.size.height
                } else {
                    zeroY + labelGapPx
                }
                drawText(layout, topLeft = Offset(labelX, labelY))
            }
        }
    }
}

@Composable
private fun MomentumLineChart(
    values: List<Int>,
    dayLabels: List<Int>,
    lineColor: Color,
    pendingColor: Color,
    pendingLast: Boolean,
    contentHeight: Dp,
    barWidth: Dp = 14.dp,
    gap: Dp = 6.dp,
    labelGutter: Dp = MomentumDateLabelGutter,
    // When set, pins the y-scale to this range instead of auto-fitting to the data (values
    // outside it are clamped/cropped, like scrolling a stock chart at a fixed price scale).
    fixedRange: IntRange? = null,
    // At low zoom, points get too close together for their date labels to fit without
    // overlapping their neighbors, so callers hide the labels below a zoom threshold.
    showDateLabels: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return
    val maxValue = fixedRange?.last ?: (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val minValue = fixedRange?.first ?: (values.minOrNull() ?: 0).coerceAtMost(0)
    val range = (maxValue - minValue).coerceAtLeast(1)
    val baselineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val textMeasurer = rememberTextMeasurer()
    val chartWidth = barWidth * values.size + gap * (values.size - 1).coerceAtLeast(0)
    Canvas(modifier = modifier.height(contentHeight + labelGutter * 2).width(chartWidth)) {
        val gutterPx = labelGutter.toPx()
        val innerHeight = size.height - gutterPx * 2
        val innerBottom = gutterPx + innerHeight
        val labelGapPx = 2.dp.toPx()
        val stride = (barWidth + gap).toPx()
        val half = barWidth.toPx() / 2f
        val points = values.mapIndexed { index, value ->
            val clamped = if (fixedRange != null) value.coerceIn(minValue, maxValue) else value
            val x = index * stride + half
            val y = gutterPx + innerHeight * (1f - (clamped - minValue).toFloat() / range)
            Offset(x, y)
        }
        val zeroY = gutterPx + innerHeight * (1f - (0 - minValue).toFloat() / range)
        if (minValue < 0) {
            drawLine(
                color = baselineColor,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.dp.toPx()
            )
        }
        // Soft fill under the line.
        val fill = Path().apply {
            moveTo(points.first().x, innerBottom)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, innerBottom)
            close()
        }
        drawPath(path = fill, color = lineColor.copy(alpha = 0.15f))
        // The connecting line. The last segment (into today) is drawn pending-colored
        // when today hasn't been logged yet.
        for (i in 1 until points.size) {
            val isPending = pendingLast && i == points.size - 1
            drawLine(
                color = if (isPending) pendingColor else lineColor,
                start = points[i - 1],
                end = points[i],
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        // A dot at each streak value; today's dot is pending-colored while unlogged. The
        // date sits on the zero axis, under the point by default, flipping above it when
        // the value dips below zero so the label never overlaps the line/dot.
        points.forEachIndexed { index, p ->
            val isPending = pendingLast && index == points.size - 1
            drawCircle(color = if (isPending) pendingColor else lineColor, radius = 3.dp.toPx(), center = p)
            val label = dayLabels.getOrNull(index)?.toString()
            if (showDateLabels && label != null) {
                val layout = textMeasurer.measure(label, style = labelStyle)
                val pointValue = if (fixedRange != null) values[index].coerceIn(minValue, maxValue) else values[index]
                val labelAbove = pointValue < 0
                val labelX = (p.x - layout.size.width / 2f)
                    .coerceIn(0f, (size.width - layout.size.width).coerceAtLeast(0f))
                val labelY = if (labelAbove) {
                    zeroY - labelGapPx - layout.size.height
                } else {
                    zeroY + labelGapPx
                }
                drawText(layout, topLeft = Offset(labelX, labelY))
            }
        }
    }
}

@Composable
private fun RoutineBatteryBar(
    label: String,
    streak: Int,
    total: Int,
    remainingColor: Color,
    onDoubleTap: (() -> Unit)? = null
) {
    val safeTotal = total.coerceAtLeast(1)
    val safeStreak = streak.coerceIn(0, safeTotal)
    Column(
        modifier = if (onDoubleTap != null) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
        } else {
            Modifier
        },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        val streakColor = MaterialTheme.colorScheme.primary
        val fireColor = Color(0xFFFF6D00)
        val fraction = safeStreak.toFloat() / safeTotal
        val triangleHeight = 96.dp
        val fireHeadroom = 22.dp
        val fireIconSize = 18.dp
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(triangleHeight + fireHeadroom)
        ) {
            val barWidth = maxWidth
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(triangleHeight)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height
                val splitX = w * fraction
                val ySplit = h * (1f - fraction)
                if (fraction > 0f) {
                    val streakPath = Path().apply {
                        moveTo(0f, h)
                        lineTo(splitX, h)
                        lineTo(splitX, ySplit)
                        close()
                    }
                    drawPath(path = streakPath, color = streakColor)
                }
                if (fraction < 1f) {
                    val remainingPath = Path().apply {
                        moveTo(splitX, h)
                        lineTo(w, h)
                        lineTo(w, 0f)
                        lineTo(splitX, ySplit)
                        close()
                    }
                    drawPath(path = remainingPath, color = remainingColor)
                }
            }
            // A fire marker just above the green hypotenuse for each completed day.
            for (i in 0 until safeStreak) {
                val centerXFrac = (i + 0.5f) / safeTotal
                val edgeYFrac = 1f - centerXFrac
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = fireColor,
                    modifier = Modifier
                        .size(fireIconSize)
                        .offset(
                            x = barWidth * centerXFrac - fireIconSize / 2f,
                            y = fireHeadroom + triangleHeight * edgeYFrac - fireIconSize - 2.dp
                        )
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            for (n in safeTotal downTo 1) {
                Text(
                    text = n.toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RatioBatteryBar(
    label: String,
    filled: Int,
    total: Int,
    remainingColor: Color,
    onDoubleTap: (() -> Unit)? = null
) {
    val safeTotal = total.coerceAtLeast(1)
    val safeFilled = filled.coerceIn(0, safeTotal)
    Column(
        modifier = if (onDoubleTap != null) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onDoubleTap() })
            }
        } else {
            Modifier
        },
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$safeFilled/$safeTotal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(safeTotal) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(
                            color = if (index < safeFilled) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                remainingColor
                            },
                            shape = RoundedCornerShape(3.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}

@Composable
private fun WorkoutSpecificInsightsCard(
    workoutNames: List<String>,
    dayNumberByWorkoutName: Map<String, Int>,
    selectedWorkoutName: String,
    onWorkoutSelected: (String) -> Unit,
    exerciseNames: List<String>,
    selectedExerciseName: String,
    onExerciseSelected: (String) -> Unit,
    history: List<WorkoutHistorySnapshot>,
    onUpdateSetLog: (logId: Long, actualReps: Int, actualWeight: String) -> Unit,
    onDeleteSetLog: (logId: Long, sessionId: Long) -> Unit,
    onDeleteDateEntry: (sessionId: Long, exerciseId: Long) -> Unit
) {
    var workoutSelectorExpanded by remember { mutableStateOf(false) }
    val exerciseScrollState = rememberScrollState()
    var editSetTarget by remember { mutableStateOf<SetLogEntity?>(null) }
    var editRepsInput by remember { mutableStateOf("") }
    var editWeightInput by remember { mutableStateOf("") }
    var deleteSetTarget by remember { mutableStateOf<SetLogEntity?>(null) }
    var deleteDateTarget by remember { mutableStateOf<WorkoutHistorySnapshot?>(null) }
    val parsedReps = editRepsInput.toIntOrNull()?.coerceIn(1, 50)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
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

            if (workoutNames.isEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Finish and log workouts to unlock workout history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { workoutSelectorExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = dayNumberByWorkoutName[selectedWorkoutName]
                                ?.let { "Day $it · $selectedWorkoutName" }
                                ?: selectedWorkoutName,
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
                                text = {
                                    Text(
                                        dayNumberByWorkoutName[workoutName]
                                            ?.let { "Day $it · $workoutName" }
                                            ?: workoutName
                                    )
                                },
                                onClick = {
                                    onWorkoutSelected(workoutName)
                                    workoutSelectorExpanded = false
                                }
                            )
                        }
                    }
                }

                if (exerciseNames.isEmpty()) {
                    Text(
                        text = "No logged exercises found for selected workout.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(exerciseScrollState),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        exerciseNames.forEach { exerciseName ->
                            FilterChip(
                                selected = exerciseName == selectedExerciseName,
                                onClick = { onExerciseSelected(exerciseName) },
                                label = { Text(exerciseName) }
                            )
                        }
                    }

                    if (history.isEmpty()) {
                        Text(
                            text = "No date history found for selected exercise in this workout.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = "Last ${history.size} dates • ${selectedWorkoutName} • ${selectedExerciseName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            history.forEach { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = formatDateShort(item.epochDay),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )

                                            TextButton(
                                                onClick = { deleteDateTarget = item },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Delete Set")
                                            }
                                        }

                                        item.setLogs.forEach { setLog ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        editSetTarget = setLog
                                                        editRepsInput = setLog.actualReps.toString()
                                                        editWeightInput = setLog.actualWeight
                                                            .trim()
                                                            .ifBlank { setLog.plannedWeight.trim() }
                                                    }
                                                    .background(
                                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Set ${setLog.setNumber}",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = formatSetLogEntry(setLog),
                                                    modifier = Modifier.weight(1f),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Medium
                                                )

                                                Icon(
                                                    imageVector = Icons.Rounded.Delete,
                                                    contentDescription = "Delete set entry",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .clickable { deleteSetTarget = setLog }
                                                        .padding(4.dp)
                                                        .size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Tap a set to edit it. Delete Set removes this date's entry for the selected workout and exercise.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    val activeEditSet = editSetTarget
    if (activeEditSet != null) {
        AlertDialog(
            onDismissRequest = { editSetTarget = null },
            title = { Text("Edit Set Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editRepsInput,
                        onValueChange = { updated ->
                            editRepsInput = updated.filter { char -> char.isDigit() }.take(2)
                        },
                        label = { Text("Reps") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editWeightInput,
                        onValueChange = { updated -> editWeightInput = updated },
                        label = { Text("Weight") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = parsedReps != null,
                    onClick = {
                        onUpdateSetLog(
                            activeEditSet.id,
                            parsedReps ?: activeEditSet.actualReps,
                            editWeightInput
                        )
                        editSetTarget = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editSetTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val activeDeleteSet = deleteSetTarget
    if (activeDeleteSet != null) {
        AlertDialog(
            onDismissRequest = { deleteSetTarget = null },
            title = { Text("Delete Set Entry?") },
            text = { Text("This deletes only the selected set entry.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSetLog(activeDeleteSet.id, activeDeleteSet.sessionId)
                        deleteSetTarget = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteSetTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val activeDeleteDate = deleteDateTarget
    if (activeDeleteDate != null) {
        AlertDialog(
            onDismissRequest = { deleteDateTarget = null },
            title = { Text("Delete Date Entry?") },
            text = {
                Text(
                    "This deletes ${selectedWorkoutName} > ${selectedExerciseName} history for ${formatDateShort(activeDeleteDate.epochDay)} only. Other dates stay unchanged."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteDateEntry(activeDeleteDate.sessionId, activeDeleteDate.exerciseId)
                        deleteDateTarget = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDateTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
