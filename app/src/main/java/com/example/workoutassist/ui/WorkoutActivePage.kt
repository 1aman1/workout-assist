package com.example.workoutassist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.WorkoutDayModel
import kotlinx.coroutines.launch

private data class PastSessionPeek(
    val epochDay: Long,
    val latestMillis: Long,
    val repsBySet: List<Int>,
    val weightBySet: List<String>
)

@Composable
internal fun WorkoutActivePage(
    day: WorkoutDayModel,
    isSessionReady: Boolean,
    focusedExerciseId: Long,
    selectedSetRepsByExerciseId: Map<Long, List<Int>>,
    selectedSetWeightByExerciseId: Map<Long, List<String>>,
    editedSetIndexesByExerciseId: Map<Long, Set<Int>>,
    loggedExerciseIds: Set<Long>,
    onFocusExercise: (Long) -> Unit,
    onSetTap: (Long, Int) -> Unit,
    onWeightTap: (Long, Int) -> Unit,
    onLogFocusedExercise: () -> Unit,
    onSkip: () -> Unit,
    onAddSet: () -> Unit,
    onRemoveSet: (Int) -> Unit,
    onAddExercise: () -> Unit,
    onFinish: () -> Unit,
    onUpdateRemark: (String) -> Unit,
    setLogs: List<SetLogEntity>,
    activeSessionId: Long
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
    var showSessionActions by remember(day.dayNumber) { mutableStateOf(false) }
    var showFocusedExerciseRemark by remember(day.dayNumber) { mutableStateOf(false) }
    var showHistoryConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showHistoryPeek by remember(day.dayNumber) { mutableStateOf(false) }
    val focusedExerciseHistory = remember(setLogs, focusedExercise?.name, activeSessionId) {
        val name = focusedExercise?.name?.trim().orEmpty()
        if (name.isEmpty()) {
            emptyList()
        } else {
            setLogs.asSequence()
                .filter { log -> log.sessionId != activeSessionId }
                .filter { log -> log.exerciseName.trim().equals(name, ignoreCase = true) }
                .groupBy { log -> log.sessionId }
                .map { (_, logs) ->
                    val latestMillis = logs.maxOf { it.loggedAt }
                    val sorted = logs.sortedBy { it.setNumber }
                    PastSessionPeek(
                        epochDay = timestampMillisToEpochDay(latestMillis),
                        latestMillis = latestMillis,
                        repsBySet = sorted.map { log -> log.actualReps.coerceAtLeast(0) },
                        weightBySet = sorted.map { log -> log.actualWeight.trim().ifBlank { log.plannedWeight.trim() } }
                    )
                }
                .sortedByDescending { it.latestMillis }
                .take(8)
        }
    }

    val finishHoldScope = rememberCoroutineScope()
    val finishHoldProgress = remember(day.dayNumber) { Animatable(0f) }
    var finishHolding by remember(day.dayNumber) { mutableStateOf(false) }

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
                                enabled = true,
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

                    IconButton(onClick = onAddExercise) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add exercise"
                        )
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (focusedExercise != null) {
                    val intervalLabel = focusedExercise.intervalSeconds.let { seconds ->
                        when {
                            seconds <= 0 -> ""
                            seconds < 60 -> "${seconds}s"
                            seconds % 60 == 0 -> "${seconds / 60}m"
                            else -> "${seconds / 60}m${seconds % 60}s"
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = focusedExercise.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {},
                                    onDoubleClick = { showHistoryConfirm = true }
                                ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        if (intervalLabel.isNotBlank()) {
                            Text(
                                text = "Rest $intervalLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    val selectedSetReps = selectedSetRepsByExerciseId[focusedExercise.id]
                        ?: List(focusedExercise.sets) { focusedExercise.reps }
                    val selectedSetWeights = selectedSetWeightByExerciseId[focusedExercise.id]
                        ?: focusedExercise.plannedWeightBySet
                    val editedSetIndexes = editedSetIndexesByExerciseId[focusedExercise.id].orEmpty()

                    val canRemoveSet = focusedExercise.id !in loggedExerciseIds && focusedExercise.sets > 1
                    repeat(focusedExercise.sets) { setIndex ->
                        val selectedValue = selectedSetReps.getOrElse(setIndex) { focusedExercise.reps }
                        val rawWeight = selectedSetWeights.getOrElse(setIndex) { focusedExercise.plannedWeight }
                        val weightLabel = stripWeightUnit(rawWeight).ifBlank { "—" }
                        WorkoutSetEditRow(
                            label = "Set ${setIndex + 1}",
                            weightText = weightLabel,
                            repsText = "$selectedValue reps",
                            isEdited = setIndex in editedSetIndexes,
                            enabled = focusedExercise.id !in loggedExerciseIds,
                            onWeightClick = { onWeightTap(focusedExercise.id, setIndex) },
                            onRepsClick = { onSetTap(focusedExercise.id, setIndex) },
                            onLongClick = { if (canRemoveSet) onRemoveSet(setIndex) }
                        )
                    }

                    if (focusedExercise.id !in loggedExerciseIds) {
                        TextButton(
                            onClick = onAddSet,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("+ Add set")
                        }
                    }
                }
            }
        }

        if (showFocusedExerciseRemark && focusedExercise != null) {
            var remarkDraft by remember(focusedExercise.id, showFocusedExerciseRemark) {
                mutableStateOf(focusedExercise.remarks)
            }
            AlertDialog(
                onDismissRequest = { showFocusedExerciseRemark = false },
                title = { Text("${focusedExercise.name} remark") },
                text = {
                    OutlinedTextField(
                        value = remarkDraft,
                        onValueChange = { remarkDraft = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Add a remark for this exercise") },
                        minLines = 2
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUpdateRemark(remarkDraft.trim())
                            showFocusedExerciseRemark = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showFocusedExerciseRemark = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showHistoryConfirm && focusedExercise != null) {
            AlertDialog(
                onDismissRequest = { showHistoryConfirm = false },
                title = { Text("View past sessions?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showHistoryConfirm = false
                            showHistoryPeek = true
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showHistoryConfirm = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showHistoryPeek && focusedExercise != null) {
            Dialog(onDismissRequest = { showHistoryPeek = false }) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Past sessions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = focusedExercise.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { showHistoryPeek = false }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Close history"
                                )
                            }
                        }
                        HorizontalDivider()
                        if (focusedExerciseHistory.isEmpty()) {
                            Text(
                                text = "No past sessions logged for this exercise yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .heightIn(max = 360.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                focusedExerciseHistory.forEach { entry ->
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = formatDateShort(entry.epochDay),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        ExerciseSetTable(
                                            repsBySet = entry.repsBySet,
                                            weightBySet = entry.weightBySet,
                                            editable = false,
                                            onEditRepsAt = {},
                                            onEditWeightAt = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSkip,
                        enabled = focusedExercise != null && focusedExercise.id !in loggedExerciseIds,
                        modifier = Modifier.weight(0.2f)
                    ) {
                        Text("Skip")
                    }

                    TextButton(
                        onClick = { showSessionActions = !showSessionActions },
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text(if (showSessionActions) "Hide Session Actions" else "Show Session Actions")
                    }
                }

                if (showSessionActions) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.45f),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        finishHolding = true
                                        val holdJob = finishHoldScope.launch {
                                            finishHoldProgress.animateTo(
                                                targetValue = 1f,
                                                animationSpec = tween(
                                                    durationMillis = 1200,
                                                    easing = LinearEasing
                                                )
                                            )
                                            finishHolding = false
                                            onFinish()
                                            finishHoldProgress.snapTo(0f)
                                        }
                                        tryAwaitRelease()
                                        if (holdJob.isActive) {
                                            holdJob.cancel()
                                            finishHolding = false
                                            finishHoldScope.launch { finishHoldProgress.snapTo(0f) }
                                        }
                                    }
                                )
                            }
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .fillMaxHeight()
                                .fillMaxWidth(finishHoldProgress.value)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.18f))
                        )
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
                                text = if (finishHolding) "Keep holding to finish…" else "Hold to Finish Workout",
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
private fun WorkoutSetEditRow(
    label: String,
    weightText: String,
    repsText: String,
    isEdited: Boolean,
    enabled: Boolean,
    onWeightClick: () -> Unit,
    onRepsClick: () -> Unit,
    onLongClick: () -> Unit = {}
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
            .combinedClickable(enabled = enabled, onClick = {}, onLongClick = onLongClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            WorkoutSetValuePill(
                value = weightText,
                enabled = enabled,
                onClick = onWeightClick
            )
            Text(
                text = "×",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WorkoutSetValuePill(
                value = repsText,
                enabled = enabled,
                onClick = onRepsClick
            )
        }
    }
}

@Composable
private fun WorkoutSetValuePill(
    value: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
