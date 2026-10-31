package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
    bannerColor: Color = Color(0xFFBF360C),
    shortWindowDays: Int = 7,
    onShortWindowChange: (Int) -> Unit = {},
    routineWindowOverride: Int = 0,
    onRoutineWindowChange: (Int) -> Unit = {},
    lookbackWeeks: Int = 4,
    onLookbackWeeksChange: (Int) -> Unit = {},
    lookbackTitle: String = "Retro",
    flatlineMisses: Boolean = true,
    pendingColor: Color = Color(0xFF2563EB),
    frozenColor: Color = FreezeBlue,
    frozenDays: Set<Long> = emptySet(),
    onOpenGraphs: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val todayEpochDay = currentDateEpochDay()
    var showWorkoutInsights by remember { mutableStateOf(false) }
    var showWindowPicker by remember { mutableStateOf(false) }
    var showRoutineWindowPicker by remember { mutableStateOf(false) }
    var showLookbackWeeksPicker by remember { mutableStateOf(false) }
    var showLookbackInspector by remember { mutableStateOf(false) }
    // Full-screen inspector so a tall streak graph can be examined with 2D scrolling.
    var showMomentumInspector by remember { mutableStateOf(false) }
    // Nested popup for the inspector's consistency/streak-length metrics, kept out of the
    // main inspector to leave more room for the chart itself.
    var showMomentumStats by remember { mutableStateOf(false) }

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

    // Rest days are template days with no exercises; they auto-log a session but aren't a
    // real activity. The "Last N days" counts should reflect only activity days: a gym
    // workout or a custom activity.
    val restDayNumbers = remember(days) {
        days.asSequence().filter { it.exercises.isEmpty() }.map { it.dayNumber }.toSet()
    }
    val activitySessionEpochDays = remember(sessions, restDayNumbers) {
        sessions
            .asSequence()
            .mapNotNull { session ->
                val finishedAt = session.finishedAt ?: return@mapNotNull null
                val isActivity = session.isCustomActivity || session.dayNumber !in restDayNumbers
                if (isActivity) timestampMillisToEpochDay(finishedAt) else null
            }
            .toSet()
    }

    val doneShort = remember(activitySessionEpochDays, todayEpochDay, shortWindowDays) {
        completedInWindow(activitySessionEpochDays, todayEpochDay, shortWindowDays.coerceIn(5, 15))
    }
    val doneLast30 = remember(activitySessionEpochDays, todayEpochDay) {
        completedInWindow(activitySessionEpochDays, todayEpochDay, 30)
    }
    // Back-to-routine metric: consecutive most-recent days that each have a session
    // (a rest day auto-logs, so a day only breaks the streak if a scheduled workout is
    // missed). Today counts once it has a session; while today is still unlogged the
    // streak is measured up to yesterday so the in-progress day isn't penalized.
    val cycleLength = remember(days, routineWindowOverride) {
        routineWindowOverride.takeIf { it in 5..15 } ?: (days.size.takeIf { it > 0 } ?: 7)
    }
    val routineStreak = remember(completedSessionEpochDays, frozenDays, todayEpochDay) {
        computeRoutineStreak(completedSessionEpochDays + frozenDays, todayEpochDay)
    }

    // Momentum: streak length per calendar day. Completed days climb 1,2,3...; each missed
    // day is a 0 so consecutive misses are all visible. Dates drive the graph's x-axis.
    val momentumEntries = remember(completedSessionEpochDays, frozenDays, todayEpochDay) {
        buildMomentumEntries(completedSessionEpochDays, todayEpochDay, frozenDays = frozenDays)
    }
    // Crash mode ("gimmick"): miss runs fall progressively below zero instead of
    // flatlining at 0, like a stock crashing further each day it doesn't recover.
    val displayMomentumEntries = remember(momentumEntries, flatlineMisses) {
        if (flatlineMisses) momentumEntries else applyMissCrashDepth(momentumEntries)
    }
    val momentumSeries = remember(displayMomentumEntries) { displayMomentumEntries.map { it.value } }
    val momentumStatuses = remember(displayMomentumEntries) { displayMomentumEntries.map { it.status } }
    val momentumReversalLabels = remember(momentumEntries) { momentumReversalRunLengths(momentumEntries) }
    val momentumDayLabels = remember(momentumEntries) {
        momentumEntries.map { epochDayToDayOfMonth(it.epochDay) }
    }
    // Today shows as a distinct "still pending" color until it's logged done or the day
    // passes unlogged (at which point it becomes a normal miss on the next render).
    val momentumPendingToday = momentumEntries.lastOrNull()?.status == MomentumDayStatus.PENDING
    val bestStreak = remember(completedSessionEpochDays, frozenDays) {
        streakRunLengths(completedSessionEpochDays + frozenDays).maxOrNull() ?: 0
    }

    // Streak freezes: earned from sustained real runs, spent from the Schedule tab by
    // tapping a missed day. These stats are shown read-only in the inspector below.
    val earnedFreezes = remember(completedSessionEpochDays) {
        earnedStreakFreezes(completedSessionEpochDays)
    }
    val freezesUsed = remember(frozenDays, todayEpochDay) {
        frozenDays.count { it <= todayEpochDay }
    }
    val freezesAvailable = (earnedFreezes - freezesUsed).coerceAtLeast(0)

    // Streak breaks (missed scheduled days). Lifted to the top level so the summary
    // can be shown both on the main card and inside the inspector. Frozen days bridge the
    // streak, so they no longer count as breaks.
    val breakDays = remember(completedSessionEpochDays, frozenDays, todayEpochDay) {
        streakBreakDays(completedSessionEpochDays + frozenDays, todayEpochDay)
    }
    val breaksThisMonth = remember(breakDays, todayEpochDay) {
        val start = startOfMonthEpochDay(todayEpochDay, 0)
        breakDays.count { it in start..todayEpochDay }
    }
    // Lifted to the top level (alongside breaksThisMonth) so both the main card's summary
    // chips and the inspector's detailed stats can share the same computation.
    val streakRuns = remember(completedSessionEpochDays) {
        streakRunLengths(completedSessionEpochDays)
    }
    val avgStreak = if (streakRuns.isEmpty()) 0.0 else streakRuns.average()

    // Workout lookback grid: which prelisted template day was last done in each of the
    // last [lookbackWeeks] rolling cycles. A "cycle" is the plan's current day count, not a
    // hardcoded 7 - so this stays meaningful if the plan ever has more/fewer than 7 days.
    // Custom activities and frozen days are not sessions tied to a template day, so they
    // never appear here - only real workouts do.
    val lookbackWorkoutDays = remember(days) { days.sortedBy { it.dayNumber } }
    val lookbackCycleLength = remember(days) { days.size.takeIf { it > 0 } ?: 7 }
    val lookbackWeekRanges = remember(todayEpochDay, lookbackWeeks, lookbackCycleLength) {
        val safeWeeks = lookbackWeeks.coerceIn(2, 10)
        val cycleDays = lookbackCycleLength.toLong()
        (0 until safeWeeks).map { weekIndex ->
            val end = todayEpochDay - weekIndex * cycleDays
            (end - (cycleDays - 1L))..end
        }
    }
    val lookbackEpochDaysByDayNumber = remember(sessions) {
        sessions
            .asSequence()
            .filter { session -> !session.isCustomActivity }
            .mapNotNull { session ->
                session.finishedAt?.let { finishedAt ->
                    session.dayNumber to timestampMillisToEpochDay(finishedAt)
                }
            }
            .groupBy({ (dayNumber, _) -> dayNumber }) { (_, epochDay) -> epochDay }
    }
    val lookbackCellsByDayNumber = remember(
        lookbackWorkoutDays,
        lookbackEpochDaysByDayNumber,
        lookbackWeekRanges
    ) {
        lookbackWorkoutDays.associate { day ->
            val epochDays = lookbackEpochDaysByDayNumber[day.dayNumber].orEmpty()
            day.dayNumber to lookbackWeekRanges.map { range -> epochDays.filter { it in range }.maxOrNull() }
        }
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

                        // Graph itself only lives in the expand popup now; main page just
                        // shows the title + expand affordance. Double-tap to set the
                        // routine-length cycle used by the streak/consistency stats.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(onDoubleTap = { showRoutineWindowPicker = true })
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = streakTitle,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (momentumSeries.isNotEmpty()) {
                                TextButton(
                                    onClick = { showMomentumInspector = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Expand")
                                    Spacer(modifier = Modifier.width(2.dp))
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
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StreakSummaryChip(
                                label = "Current",
                                value = "$routineStreak ${if (routineStreak == 1) "day" else "days"}"
                            )
                            StreakSummaryChip(
                                label = "Best",
                                value = "$bestStreak ${if (bestStreak == 1) "day" else "days"}"
                            )
                            StreakSummaryChip(
                                label = "Avg",
                                value = "${"%.1f".format(avgStreak)} days"
                            )
                            StreakSummaryChip(
                                label = "Breaks",
                                value = breaksThisMonth.toString()
                            )
                        }
                        HorizontalDivider()

                        RatioBatteryBar(
                            label = "Activity last $shortWindowDays days",
                            filled = doneShort,
                            total = shortWindowDays,
                            remainingColor = bannerColor,
                            onDoubleTap = { showWindowPicker = true }
                        )
                        RatioBatteryBar(
                            label = "Activity last 30 days",
                            filled = doneLast30,
                            total = 30,
                            remainingColor = bannerColor
                        )
                    }
                }

                if (lookbackWorkoutDays.isNotEmpty()) {
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = lookbackTitle,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                TextButton(
                                    onClick = { showLookbackInspector = true },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Text("Expand")
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                        contentDescription = "Expand $lookbackTitle"
                                    )
                                }
                            }
                            HorizontalDivider()
                            WorkoutLookbackGraph(
                                workoutDays = lookbackWorkoutDays,
                                weekRanges = lookbackWeekRanges,
                                cellsByDayNumber = lookbackCellsByDayNumber,
                                compact = true,
                                onDoubleTap = { showLookbackWeeksPicker = true },
                                onExpand = { showLookbackInspector = true }
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
            title = "Insights \u2192 Activity window",
            value = shortWindowDays,
            range = 5..15,
            valueText = { days -> "Last $days days" },
            onDismiss = { showWindowPicker = false },
            onConfirm = { newWindow ->
                onShortWindowChange(newWindow)
                showWindowPicker = false
            }
        )
    }

    if (showRoutineWindowPicker) {
        NumberWheelDialog(
            title = "Insights \u2192 Routine length",
            value = cycleLength,
            range = 5..15,
            valueText = { days -> "$days-day cycle" },
            onDismiss = { showRoutineWindowPicker = false },
            onConfirm = { newTarget ->
                onRoutineWindowChange(newTarget)
                showRoutineWindowPicker = false
            }
        )
    }

    if (showLookbackWeeksPicker) {
        NumberWheelDialog(
            title = "Insights \u2192 Workout lookback",
            value = lookbackWeeks,
            range = 2..10,
            step = 2,
            valueText = { weeks -> "Last $weeks cycles" },
            onDismiss = { showLookbackWeeksPicker = false },
            onConfirm = { newWeeks ->
                onLookbackWeeksChange(newWeeks)
                showLookbackWeeksPicker = false
            }
        )
    }

    if (showLookbackInspector && lookbackWorkoutDays.isNotEmpty()) {
        Dialog(
            onDismissRequest = { showLookbackInspector = false },
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
                            text = lookbackTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = { showLookbackInspector = false }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close"
                            )
                        }
                    }
                    WorkoutLookbackGraph(
                        workoutDays = lookbackWorkoutDays,
                        weekRanges = lookbackWeekRanges,
                        cellsByDayNumber = lookbackCellsByDayNumber,
                        compact = false,
                        onDoubleTap = { showLookbackWeeksPicker = true }
                    )
                }
            }
        }
    }

    if (showMomentumInspector && momentumSeries.isNotEmpty()) {
        // Unlike the compact card (which auto-zooms the Y-axis to squeeze the whole range
        // into one view), the inspector uses a fixed dp-per-streak-unit scale, like a stock
        // chart, and pans in both directions: horizontally through time (unchanged) and
        // vertically through the value range, instead of shrinking everything to fit.
        val inspectorViewportHeight = 520.dp
        val baseInspectorUnitHeight = 26.dp
        val baseInspectorCandleWidth = 12.dp
        val baseInspectorCandleGap = 10.dp
        val inspectorZoomMin = 0.5f
        val inspectorZoomMax = 2.5f
        val inspectorZoomStep = 0.25f
        // Default to the minimum (50%) zoom so a long streak history fits on open without
        // the user needing to zoom out manually first.
        var inspectorZoom by remember { mutableStateOf(inspectorZoomMin) }
        val inspectorHScroll = rememberScrollState()
        val inspectorVScroll = rememberScrollState()
        // Re-anchor on today whenever the data, mode, or zoom level changes (zooming resizes
        // the content, so without this the view could land somewhere other than today) - the
        // same "always anchored on today" guarantee as the compact card, just pannable here.
        LaunchedEffect(inspectorZoom, inspectorHScroll.maxValue) {
            inspectorHScroll.scrollTo(inspectorHScroll.maxValue)
        }
        LaunchedEffect(momentumSeries, inspectorZoom, inspectorVScroll.maxValue) {
            // Land on the bottom of the value range (today's baseline + the date axis),
            // same "anchored on now" feel as the horizontal scroll; scroll up to see peaks.
            inspectorVScroll.scrollTo(inspectorVScroll.maxValue)
        }
        // How many streaks were 1, 2, ... 7+ days long (a length-frequency histogram).
        val streakHistogram = remember(completedSessionEpochDays) {
            streakLengthHistogram(completedSessionEpochDays, maxBucket = 7)
        }
        val breaksLast3Months = remember(breakDays, todayEpochDay) {
            val start = startOfMonthEpochDay(todayEpochDay, 2)
            breakDays.count { it in start..todayEpochDay }
        }
        val longestGap = remember(completedSessionEpochDays, todayEpochDay) {
            longestStreakGap(completedSessionEpochDays, todayEpochDay)
        }
        Dialog(
            onDismissRequest = { showMomentumInspector = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.96f)
                    .padding(vertical = 8.dp),
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
                            // Below 100% zoom, candles/points pack in tight enough that
                            // their date labels would overlap each other, so hide the dates
                            // until the user zooms back to 100% (the default) or beyond.
                            val inspectorShowDates = inspectorZoom >= 1f
                            MomentumCandleChart(
                                values = momentumSeries,
                                statuses = momentumStatuses,
                                reversalLabels = momentumReversalLabels,
                                dayLabels = momentumDayLabels.drop(1),
                                upColor = Color(0xFF16A34A),
                                downColor = Color(0xFFDC2626),
                                pendingColor = pendingColor,
                                frozenColor = frozenColor,
                                pendingLast = momentumPendingToday,
                                fallingMode = !flatlineMisses,
                                candleWidth = inspectorCandleWidth,
                                gap = inspectorCandleGap,
                                contentHeight = inspectorContentHeight,
                                showDateLabels = inspectorShowDates
                            )
                        }
                    }
                    HorizontalDivider()
                    OutlinedButton(
                        onClick = { showMomentumStats = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View streak stats")
                    }
                }
            }
        }

        if (showMomentumStats) {
            Dialog(onDismissRequest = { showMomentumStats = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = "Streak stats",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            IconButton(onClick = { showMomentumStats = false }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close"
                                )
                            }
                        }
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
                            MetricRow(
                                label = "Longest gap",
                                value = "$longestGap ${if (longestGap == 1) "day" else "days"}"
                            )
                            MetricRow("Avg streak", "${"%.1f".format(avgStreak)} days")
                            MetricRow("Breaks this month", breaksThisMonth.toString())
                            MetricRow("Breaks last 3 months", breaksLast3Months.toString())
                            MetricRow("Active days", completedSessionEpochDays.size.toString())
                            MetricRow("Streaks", streakRuns.size.toString())
                            MetricRow("Freezes earned", earnedFreezes.toString())
                            MetricRow("Freezes available", freezesAvailable.toString())
                            MetricRow("Freezes used", freezesUsed.toString())
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
}

// Compact highlighted stat shown on the main streak card (Current / Best / Avg / Breaks).
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
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically(tween(220)) { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically(tween(220)) { height -> -height } + fadeOut())
                },
                label = "streakChipValue"
            ) { animatedValue ->
                Text(
                    text = animatedValue,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
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
                    .align(Alignment.TopStart)
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
// that now sit directly on the axis line (see `MomentumCandleChart`).
private val MomentumDateLabelGutter = 16.dp

// Color for a frozen (freeze-bridged) day on the momentum graph - a distinct blue so it
// reads as neither a real workout (green) nor a confirmed miss (red).
private val FreezeBlue = Color(0xFF0EA5E9)

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
    statuses: List<MomentumDayStatus> = emptyList(),
    reversalLabels: List<Int?> = emptyList(),
    frozenColor: Color = FreezeBlue,
    fallingMode: Boolean = false,
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
    val reversalLabelStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
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
            val isFrozen = statuses.getOrNull(i) == MomentumDayStatus.FROZEN
            val color = when {
                isPending -> pendingColor
                isFrozen -> frozenColor
                isMiss -> downColor
                else -> upColor
            }
            var topValue: Int
            var bottomValue: Int
            if (isFrozen) {
                // A streak-freeze day is drawn as a full column from the zero axis up to its
                // value (not just the usual open-to-close sliver), so the freeze reads as a
                // distinct, prominent bar. This is purely a per-day rendering choice driven by
                // the live FROZEN status - if that day is ever no longer frozen, it falls
                // through to the ordinary candle shape below with no extra state to unwind.
                topValue = maxOf(open, close, 0)
                bottomValue = 0
            } else if (isPending && fallingMode && close < 0) {
                // Only stretch into the "recovery" shape once the crash depth has actually
                // gone negative (i.e. we're mid-crash). Gating on the depth itself (not just
                // the crash-mode toggle) keeps an ordinary ongoing streak's pending candle on
                // the "continue the climb" branch below instead of collapsing to a 1-unit tick.
                // Show the possibility of recovery: stretch from the deepest point the
                // crash reached so far up to +1 (a fresh streak start), so a long miss
                // run reads as a long blue candle with room to climb back out.
                topValue = 1
                bottomValue = minOf(open, close)
            } else if (isPending) {
                // Not confirmed as a miss yet: tentatively continue the climb by one unit
                // from wherever it currently stands (or start a fresh one at +1 if it's
                // already at zero), instead of snapping down to a flat peak-to-zero "miss"
                // shape before the day has actually passed unlogged.
                topValue = open + 1
                bottomValue = open
            } else if (!fallingMode && isMiss) {
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
            // When this candle is the confirmed last day of a run right before it reverses
            // (a streak breaking or a miss gap ending with a workout), stamp its run length
            // right on the candle: above the body for a broken streak (count sits near the
            // peak it reached), below the body for an ended gap (count sits near where the
            // miss run bottomed out), in the candle's own color so it reads as an annotation.
            val reversalCount = reversalLabels.getOrNull(i)
            if (reversalCount != null) {
                val countLayout = textMeasurer.measure(
                    reversalCount.toString(),
                    style = reversalLabelStyle.copy(color = color)
                )
                val countX = (left + cw / 2f - countLayout.size.width / 2f)
                    .coerceIn(0f, (size.width - countLayout.size.width).coerceAtLeast(0f))
                val countY = if (isMiss) {
                    bodyTop + bodyHeight + labelGapPx
                } else {
                    bodyTop - labelGapPx - countLayout.size.height
                }
                drawText(countLayout, topLeft = Offset(countX, countY))
            }
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
            AnimatedContent(
                targetState = "$safeFilled/$safeTotal",
                transitionSpec = {
                    (slideInVertically(tween(220)) { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically(tween(220)) { height -> -height } + fadeOut())
                },
                label = "ratioBarValue"
            ) { animatedValue ->
                Text(
                    text = animatedValue,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            repeat(safeTotal) { index ->
                val boxColor by animateColorAsState(
                    targetValue = if (index < safeFilled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        remainingColor
                    },
                    animationSpec = tween(220),
                    label = "ratioBarSegment"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(16.dp)
                        .background(
                            color = boxColor,
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

// Compact "which workouts are lagging" view: one bar per day, height = weeks done out of
// the weeks shown, scaled to a fixed per-week unit so the overall bar height still grows
// or shrinks with the selected week range (min 3, max 8). A shorter bar reads as behind
// pace at a glance, without needing the full week-by-week matrix.
@Composable
private fun WorkoutLookbackColumnChart(
    workoutDays: List<WorkoutDayModel>,
    totalWeeks: Int,
    cellsByDayNumber: Map<Int, List<Long?>>,
    onPaceColor: Color,
    partialColor: Color,
    laggingColor: Color
) {
    val barWidth = 28.dp
    // Y-axis ticks in multiples of 2 up to the ceiling (rounded up from the weeks shown;
    // default is 4 -> 4, 2), so the bars read against a scale instead of floating free.
    val axisCeiling = (kotlin.math.ceil(totalWeeks.coerceAtLeast(1) / 2.0).toInt() * 2).coerceAtLeast(4)
    val axisTicks = (axisCeiling downTo 2 step 2).toList()
    val axisTextHeight = 14.dp
    // Fixed chart height regardless of how many weeks are selected (previously it scaled
    // with totalWeeks and became a short sliver at 2 weeks); bars are normalized against
    // axisCeiling instead so the y-axis scale and bar fill always agree.
    val barAreaHeight = 128.dp
    val unitHeight = barAreaHeight / axisCeiling

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(barAreaHeight)
        ) {
            axisTicks.forEach { tick ->
                val y = (barAreaHeight * (1f - tick.toFloat() / axisCeiling) - axisTextHeight / 2)
                    .coerceIn(0.dp, barAreaHeight - axisTextHeight)
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
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            workoutDays.forEach { day ->
                val hits = cellsByDayNumber[day.dayNumber].orEmpty().count { it != null }
                val paceColor = when {
                    totalWeeks == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                    hits == totalWeeks -> onPaceColor
                    hits == 0 -> laggingColor
                    else -> partialColor
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(barWidth)
                            .height(barAreaHeight),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(unitHeight * hits)
                                .background(
                                    color = paceColor,
                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                )
                        )
                    }
                    Text(
                        text = "D${day.dayNumber}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = paceColor
                    )
                }
            }
        }
    }
}

// Grid: rows are rolling cycles (most recent first, each cycle spanning the plan's current
// day count rather than a hardcoded 7 days), columns are the template's prelisted workout
// days (1..N). A cell shows the day-of-month it was last done that cycle, or a dash if
// skipped - making a lagging vs. on-pace workout obvious from its column's dash/fill ratio.
// In compact mode (main card) the left-side date labels and the legend are hidden to reduce
// clutter; the expanded popup shows both.
@Composable
private fun WorkoutLookbackGraph(
    workoutDays: List<WorkoutDayModel>,
    weekRanges: List<LongRange>,
    cellsByDayNumber: Map<Int, List<Long?>>,
    compact: Boolean,
    onDoubleTap: () -> Unit,
    onExpand: () -> Unit = {}
) {
    val onPaceColor = Color(0xFF2E7D32)
    val partialColor = Color(0xFFFF8F00)
    val laggingColor = MaterialTheme.colorScheme.error
    val weekLabelWidth = 76.dp
    val cellWidth = 52.dp
    val gridWidth = (if (compact) 0.dp else weekLabelWidth) + cellWidth * workoutDays.size

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { if (compact) onExpand() },
                    onDoubleTap = { onDoubleTap() }
                )
            },
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Description only shown in the expanded inspector, not the compact main-page card.
        if (!compact) {
            Text(
                text = "When each workout was last done \u00b7 last ${weekRanges.size} cycles \u00b7 double-tap to change",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (compact) {
            // Lagging-at-a-glance view: one bar per day, height = weeks done out of weeks
            // shown, so a shorter bar reads as behind pace without needing the full matrix.
            WorkoutLookbackColumnChart(
                workoutDays = workoutDays,
                totalWeeks = weekRanges.size,
                cellsByDayNumber = cellsByDayNumber,
                onPaceColor = onPaceColor,
                partialColor = partialColor,
                laggingColor = laggingColor
            )
        } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!compact) {
                    Spacer(modifier = Modifier.width(weekLabelWidth))
                }
                workoutDays.forEach { day ->
                    val hits = cellsByDayNumber[day.dayNumber].orEmpty().count { it != null }
                    val total = weekRanges.size
                    val paceColor = when {
                        total == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                        hits == total -> onPaceColor
                        hits == 0 -> laggingColor
                        else -> partialColor
                    }
                    Text(
                        text = "D${day.dayNumber}",
                        modifier = Modifier.width(cellWidth),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = paceColor
                    )
                }
            }
            weekRanges.forEachIndexed { rowIndex, range ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!compact) {
                        Text(
                            text = formatMonthDay(range.first),
                            modifier = Modifier.width(weekLabelWidth),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    workoutDays.forEach { day ->
                        val epochDay = cellsByDayNumber[day.dayNumber]?.getOrNull(rowIndex)
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(32.dp)
                                .padding(2.dp)
                                .background(
                                    color = if (epochDay != null) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = epochDay?.let { epochDayToDayOfMonth(it).toString() } ?: "–",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (epochDay != null) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (epochDay != null) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                }
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.width(gridWidth),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            // Totals row: k/n where k = weeks this workout was done, n = weeks shown.
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!compact) {
                    Text(
                        text = "Done",
                        modifier = Modifier.width(weekLabelWidth),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                workoutDays.forEach { day ->
                    val hits = cellsByDayNumber[day.dayNumber].orEmpty().count { it != null }
                    val total = weekRanges.size
                    val paceColor = when {
                        total == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                        hits == total -> onPaceColor
                        hits == 0 -> laggingColor
                        else -> partialColor
                    }
                    Text(
                        text = "$hits/$total",
                        modifier = Modifier.width(cellWidth),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = paceColor
                    )
                }
            }
        }
        if (!compact) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                workoutDays.forEach { day ->
                    Text(
                        text = "D${day.dayNumber} · ${day.workoutName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
