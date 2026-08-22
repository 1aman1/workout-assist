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
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import com.example.workoutassist.data.WorkoutSessionEntity
import kotlinx.coroutines.launch

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
    val momentumSeries = remember(momentumEntries) { momentumEntries.map { it.value } }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = workoutInsightsTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = { showWorkoutInsights = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Progress Graphs (Beta)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(
                            onClick = onOpenGraphs,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Open")
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null
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
        // Keep the candles from getting too tall; the chart's horizontal length (one candle
        // per day) is unchanged and scrolls to reveal the whole timeline.
        val inspectorHeight = 200.dp
        val inspectorHScroll = rememberScrollState()
        LaunchedEffect(stockMode, inspectorHScroll.maxValue) {
            inspectorHScroll.scrollTo(inspectorHScroll.maxValue)
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
        Dialog(onDismissRequest = { showMomentumInspector = false }) {
            Card(
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
                    val inspectorAxisMax = (momentumSeries.maxOrNull() ?: 1).coerceAtLeast(1)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MomentumYAxis(maxValue = inspectorAxisMax, height = inspectorHeight)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(inspectorHScroll)
                        ) {
                            Column {
                                if (stockMode) {
                                    MomentumCandleChart(
                                        values = momentumSeries,
                                        upColor = Color(0xFF16A34A),
                                        downColor = Color(0xFFDC2626),
                                        pendingColor = Color(0xFF2563EB),
                                        pendingLast = momentumPendingToday,
                                        modifier = Modifier.height(inspectorHeight)
                                    )
                                } else {
                                    MomentumLineChart(
                                        values = momentumSeries,
                                        lineColor = MaterialTheme.colorScheme.primary,
                                        pendingColor = Color(0xFF2563EB),
                                        pendingLast = momentumPendingToday,
                                        modifier = Modifier.height(inspectorHeight)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // X-axis: the date (day-of-month) under each bar/candle
                                // (candles are day-over-day, so drop the leading baseline).
                                if (stockMode) {
                                    MomentumValueAxis(
                                        values = momentumDayLabels.drop(1),
                                        cellWidth = 12.dp,
                                        gap = 10.dp
                                    )
                                } else {
                                    MomentumValueAxis(
                                        values = momentumDayLabels,
                                        cellWidth = 14.dp,
                                        gap = 6.dp
                                    )
                                }
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
    pendingToday: Boolean,
    onInspect: () -> Unit
) {
    val chartScroll = rememberScrollState()
    // Anchor the view on the latest (right-most) entry so today's trend shows first;
    // the user can still scroll left through history.
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
            val axisMax = (momentumSeries.maxOrNull() ?: 1).coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Top
            ) {
                MomentumYAxis(maxValue = axisMax, height = chartHeight)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(chartScroll)
                ) {
                    Column {
                        if (stockMode) {
                            MomentumCandleChart(
                                values = momentumSeries,
                                upColor = Color(0xFF16A34A),
                                downColor = Color(0xFFDC2626),
                                pendingColor = Color(0xFF2563EB),
                                pendingLast = pendingToday,
                                modifier = Modifier.height(chartHeight)
                            )
                        } else {
                            MomentumLineChart(
                                values = momentumSeries,
                                lineColor = MaterialTheme.colorScheme.primary,
                                pendingColor = Color(0xFF2563EB),
                                pendingLast = pendingToday,
                                modifier = Modifier.height(chartHeight)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        if (stockMode) {
                            MomentumValueAxis(values = dayLabels.drop(1), cellWidth = 12.dp, gap = 10.dp)
                        } else {
                            MomentumValueAxis(values = dayLabels, cellWidth = 14.dp, gap = 6.dp)
                        }
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
// horizontal scroll so the scale stays visible while the chart scrolls.
@Composable
private fun MomentumYAxis(maxValue: Int, height: Dp, modifier: Modifier = Modifier) {
    val m = maxValue.coerceAtLeast(1)
    val ticks = remember(m) { momentumYTicks(m) }
    val textHeight = 14.dp
    Box(
        modifier = modifier
            .height(height)
            .width(22.dp)
    ) {
        ticks.forEach { tick ->
            val frac = tick.toFloat() / m
            val y = (height * (1f - frac) - textHeight * 0.5f).coerceIn(0.dp, height - textHeight)
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

// Evenly spaced streak values (max .. 0) used as Y-axis ticks. Small ranges show every
// integer; larger ranges are thinned to about five labels.
private fun momentumYTicks(maxValue: Int): List<Int> {
    val m = maxValue.coerceAtLeast(1)
    if (m <= 6) return (m downTo 0).toList()
    val step = kotlin.math.ceil(m / 5.0).toInt().coerceAtLeast(1)
    val ticks = sortedSetOf(0, m)
    var v = step
    while (v < m) {
        ticks.add(v)
        v += step
    }
    return ticks.toList().sortedDescending()
}

// Numeric x-axis for the inspector: the streak value under each bar/candle, using the
// same per-item width and gap as the chart so the labels line up.
@Composable
private fun MomentumValueAxis(
    values: List<Int>,
    cellWidth: Dp,
    gap: Dp,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) {
        values.forEach { value ->
            Box(modifier = Modifier.width(cellWidth), contentAlignment = Alignment.Center) {
                Text(
                    text = value.toString(),
                    modifier = Modifier.wrapContentWidth(unbounded = true),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

// Stock-market gimmick: draw each day-over-day change in streak as a candle. A climb
// (green) rises one step; a break (red) drops from the streak peak all the way to zero.
// The very last candle can be "pending" (today, not yet logged) — drawn in a distinct
// color instead of red so it doesn't look like a confirmed miss.
@Composable
private fun MomentumCandleChart(
    values: List<Int>,
    upColor: Color,
    downColor: Color,
    pendingColor: Color,
    pendingLast: Boolean,
    modifier: Modifier = Modifier
) {
    if (values.size < 2) return
    val maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val candleCount = values.size - 1
    val candleWidth = 12.dp
    val gap = 10.dp
    val chartWidth = candleWidth * candleCount + gap * (candleCount - 1).coerceAtLeast(0)
    Canvas(modifier = modifier.width(chartWidth)) {
        val cw = candleWidth.toPx()
        val g = gap.toPx()
        val radius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
        val minBody = 3.dp.toPx()
        for (i in 1 until values.size) {
            val open = values[i - 1]
            val close = values[i]
            val isPending = pendingLast && i == values.size - 1
            // A zero means a missed day: draw it red. The first miss after a run drops from
            // the streak peak; a continuing miss shows a 1-unit red tick so it stays visible.
            val isMiss = close == 0 && !isPending
            val color = if (isPending) pendingColor else if (isMiss) downColor else upColor
            val topValue = if (isPending || isMiss) maxOf(open, 1) else maxOf(open, close)
            val bottomValue = if (isPending || isMiss) 0 else minOf(open, close)
            val topY = size.height * (1f - topValue.toFloat() / maxValue)
            val bottomY = size.height * (1f - bottomValue.toFloat() / maxValue)
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
        }
    }
}

@Composable
private fun MomentumLineChart(
    values: List<Int>,
    lineColor: Color,
    pendingColor: Color,
    pendingLast: Boolean,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return
    val maxValue = (values.maxOrNull() ?: 1).coerceAtLeast(1)
    val barWidth = 14.dp
    val gap = 6.dp
    val chartWidth = barWidth * values.size + gap * (values.size - 1).coerceAtLeast(0)
    Canvas(modifier = modifier.width(chartWidth)) {
        val stride = (barWidth + gap).toPx()
        val half = barWidth.toPx() / 2f
        val points = values.mapIndexed { index, value ->
            val x = index * stride + half
            val y = size.height * (1f - value.toFloat() / maxValue)
            Offset(x, y)
        }
        // Soft fill under the line.
        val fill = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
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
        // A dot at each streak value; today's dot is pending-colored while unlogged.
        points.forEachIndexed { index, p ->
            val isPending = pendingLast && index == points.size - 1
            drawCircle(color = if (isPending) pendingColor else lineColor, radius = 3.dp.toPx(), center = p)
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
