package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    routineTitle: String = "Back to routine",
    daysToRoutineText: String = "days to get back on routine",
    onRoutineText: String = "You're on routine"
) {
    val scope = rememberCoroutineScope()
    val todayEpochDay = currentDateEpochDay()
    var showWorkoutInsights by remember { mutableStateOf(false) }

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

    val doneLast7 = remember(completedSessionEpochDays, todayEpochDay) {
        val startDay = todayEpochDay - 6L
        completedSessionEpochDays.count { day -> day in startDay..todayEpochDay }
    }
    val doneLast30 = remember(completedSessionEpochDays, todayEpochDay) {
        val startDay = todayEpochDay - 29L
        completedSessionEpochDays.count { day -> day in startDay..todayEpochDay }
    }
    // Back-to-routine metric: consecutive most-recent days that each have a session
    // (a rest day auto-logs, so a day only breaks the streak if a scheduled workout is
    // missed). Today counts once it has a session; while today is still unlogged the
    // streak is measured up to yesterday so the in-progress day isn't penalized.
    val cycleLength = remember(days) { days.size.takeIf { it > 0 } ?: 7 }
    val routineStreak = remember(completedSessionEpochDays, todayEpochDay) {
        var cursor = if (todayEpochDay in completedSessionEpochDays) {
            todayEpochDay
        } else {
            todayEpochDay - 1L
        }
        var streak = 0
        while (cursor in completedSessionEpochDays) {
            streak++
            cursor -= 1L
        }
        streak
    }
    val onRoutine = routineStreak >= cycleLength
    val daysToRoutine = (cycleLength - routineStreak).coerceAtLeast(0)

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

                        RatioBatteryBar(
                            label = routineTitle,
                            filled = routineStreak,
                            total = cycleLength
                        )
                        if (onRoutine) {
                            Text(
                                text = onRoutineText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            RatioBatteryBar(
                                label = daysToRoutineText,
                                filled = daysToRoutine,
                                total = cycleLength
                            )
                        }

                        HorizontalDivider()

                        RatioBatteryBar(
                            label = "Last 7 days",
                            filled = doneLast7,
                            total = 7
                        )
                        RatioBatteryBar(
                            label = "Last 30 days",
                            filled = doneLast30,
                            total = 30
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
            }
        }
    }
}

@Composable
private fun RatioBatteryBar(
    label: String,
    filled: Int,
    total: Int
) {
    val safeTotal = total.coerceAtLeast(1)
    val safeFilled = filled.coerceIn(0, safeTotal)
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
