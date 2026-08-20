package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.example.workoutassist.data.ExerciseDraft
import com.example.workoutassist.data.ExerciseModel
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class QuickEditField {
    SETS,
    REPS,
    WEIGHT,
    INTERVAL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutDayScreen(
    day: WorkoutDayModel,
    repository: WorkoutRepository,
    setLogs: List<SetLogEntity>,
    onRequestGoToSettings: () -> Unit,
    onBack: () -> Unit,
    onWorkoutActiveChange: (Boolean) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val viewedDateEpochDay = day.plannedDateEpochDay

    var editMode by remember(day.dayNumber) { mutableStateOf(false) }
    var workoutActive by remember(day.dayNumber) { mutableStateOf(false) }

    var showAddDialog by remember(day.dayNumber) { mutableStateOf(false) }
    var editExerciseTarget by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var showRenameWorkoutDialog by remember(day.dayNumber) { mutableStateOf(false) }
    var showAddSessionExerciseDialog by remember(day.dayNumber) { mutableStateOf(false) }

    var focusedExerciseId by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var selectedSetRepsByExerciseId by remember(day.dayNumber) {
        mutableStateOf<Map<Long, List<Int>>>(emptyMap())
    }
    var editedSetIndexesByExerciseId by remember(day.dayNumber) {
        mutableStateOf<Map<Long, Set<Int>>>(emptyMap())
    }
    var loggedExerciseIds by remember(day.dayNumber) { mutableStateOf<Set<Long>>(emptySet()) }
    var setPickerTarget by remember(day.dayNumber) { mutableStateOf<Pair<Long, Int>?>(null) }
    var selectedSetWeightByExerciseId by remember(day.dayNumber) {
        mutableStateOf<Map<Long, List<String>>>(emptyMap())
    }
    var weightPickerTarget by remember(day.dayNumber) { mutableStateOf<Pair<Long, Int>?>(null) }
    var removeSetTarget by remember(day.dayNumber) { mutableStateOf<Int?>(null) }

    var activeSessionId by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var sessionStartMessage by remember(day.dayNumber) { mutableStateOf<String?>(null) }

    var showExitWorkoutModeConfirm by remember(day.dayNumber) { mutableStateOf(false) }
    var showExportAfterEditPrompt by remember(day.dayNumber) { mutableStateOf(false) }
    var hasEditChangesPendingExport by remember(day.dayNumber) { mutableStateOf(false) }
    var showSaveEditsPrompt by remember(day.dayNumber) { mutableStateOf(false) }
    var editTemplateSnapshot by remember(day.dayNumber) { mutableStateOf<List<ExerciseModel>?>(null) }
    var quickEditExercise by remember(day.dayNumber) { mutableStateOf<ExerciseModel?>(null) }
    var quickEditField by remember(day.dayNumber) { mutableStateOf<QuickEditField?>(null) }
    var quickEditSetIndex by remember(day.dayNumber) { mutableStateOf<Int?>(null) }

    val listState = rememberLazyListState()
    var draggingExerciseId by remember(day.dayNumber) { mutableLongStateOf(-1L) }
    var dragOffsetY by remember(day.dayNumber) { mutableFloatStateOf(0f) }
    var editCollapseSignal by remember(day.dayNumber) { mutableIntStateOf(0) }

    // Stopwatches (not persisted): total workout time + rest interval since the last set log.
    var workoutStartMillis by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var intervalStartMillis by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var nowMillis by remember(day.dayNumber) { mutableLongStateOf(0L) }
    var intervalResetSignal by remember(day.dayNumber) { mutableIntStateOf(0) }
    var intervalFlash by remember(day.dayNumber) { mutableStateOf(false) }
    var intervalResetting by remember(day.dayNumber) { mutableStateOf(false) }

    // Tick once per half-second while a workout is active so both clocks stay current.
    LaunchedEffect(workoutActive) {
        while (workoutActive) {
            nowMillis = System.currentTimeMillis()
            delay(500)
        }
    }

    // On a set log, keep the final rest duration visible for 2s (blinking the rest timer
    // to warn it's about to reset), then refresh it and briefly pulse to confirm the reset.
    LaunchedEffect(intervalResetSignal) {
        if (intervalResetSignal == 0) return@LaunchedEffect
        intervalResetting = true
        delay(2000)
        intervalResetting = false
        intervalStartMillis = System.currentTimeMillis()
        nowMillis = intervalStartMillis
        intervalFlash = true
        delay(700)
        intervalFlash = false
    }

    val totalElapsedSeconds = if (workoutStartMillis > 0L) {
        ((nowMillis - workoutStartMillis) / 1000L).coerceAtLeast(0L).toInt()
    } else 0
    val intervalElapsedSeconds = if (intervalStartMillis > 0L) {
        ((nowMillis - intervalStartMillis) / 1000L).coerceAtLeast(0L).toInt()
    } else 0

    // Report active-session state up so the shell can hide the bottom nav during a
    // workout (prevents accidental tab taps from abandoning the session).
    LaunchedEffect(workoutActive) {
        onWorkoutActiveChange(workoutActive)
    }
    DisposableEffect(Unit) {
        onDispose { onWorkoutActiveChange(false) }
    }

    LaunchedEffect(workoutActive, sessionStartMessage) {
        val message = sessionStartMessage ?: return@LaunchedEffect
        if (!workoutActive) {
            return@LaunchedEffect
        }
        delay(5000)
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

    // Remark lives on the exercise template, so editing it (even mid-session) persists
    // and shows again next time this workout is opened.
    fun updateFocusedExerciseRemark(newRemark: String) {
        val focused = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId } ?: return
        scope.launch {
            repository.updateExercise(
                exercise = focused,
                draft = ExerciseDraft(
                    name = focused.name,
                    sets = focused.sets,
                    reps = focused.reps,
                    intervalSeconds = focused.intervalSeconds,
                    plannedWeight = focused.plannedWeight,
                    remarks = newRemark
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
        val startMillis = System.currentTimeMillis()
        workoutStartMillis = startMillis
        intervalStartMillis = startMillis
        nowMillis = startMillis
        intervalResetSignal = 0
        intervalFlash = false
        editMode = false
        focusedExerciseId = 0L
        selectedSetRepsByExerciseId = day.exercises.associate { exercise ->
            exercise.id to exercise.plannedRepsBySet
        }
        selectedSetWeightByExerciseId = day.exercises.associate { exercise ->
            exercise.id to exercise.plannedWeightBySet
        }
        editedSetIndexesByExerciseId = emptyMap()
        loggedExerciseIds = emptySet()
        setPickerTarget = null
        weightPickerTarget = null
        sessionStartMessage = WORKOUT_SESSION_START_MESSAGES.random()
    }

    fun updateSetRepsSelection(exercise: ExerciseModel, setIndex: Int, selectedReps: Int) {
        if (setIndex < 0) {
            return
        }
        val value = selectedReps.coerceIn(1, 50)
        val current = (selectedSetRepsByExerciseId[exercise.id] ?: exercise.plannedRepsBySet).toMutableList()
        val lastSetIndex = maxOf(setIndex, exercise.sets - 1)
        while (current.size <= lastSetIndex) {
            current.add(exercise.reps)
        }
        // Reps apply only to the tapped set. Unlike weight (which ladders down to every
        // later set), reps commonly vary per set, so we don't overwrite the rest.
        current[setIndex] = value
        selectedSetRepsByExerciseId = selectedSetRepsByExerciseId + (exercise.id to current.toList())

        val editedSetIndexes = editedSetIndexesByExerciseId[exercise.id].orEmpty() + setIndex
        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId + (exercise.id to editedSetIndexes)

        // Trigger the (delayed) rest-timer refresh; the reset itself happens 2s later.
        intervalResetSignal += 1
    }

    fun updateSetWeightSelection(exercise: ExerciseModel, setIndex: Int, selectedWeightText: String) {
        if (setIndex < 0) {
            return
        }
        val current = (selectedSetWeightByExerciseId[exercise.id] ?: exercise.plannedWeightBySet).toMutableList()
        val lastSetIndex = maxOf(setIndex, exercise.sets - 1)
        while (current.size <= lastSetIndex) {
            current.add(exercise.plannedWeight)
        }
        // Ladder fill-down: apply to this set and copy the value to every later set.
        for (index in setIndex..lastSetIndex) {
            current[index] = selectedWeightText
        }
        selectedSetWeightByExerciseId = selectedSetWeightByExerciseId + (exercise.id to current.toList())

        val editedSetIndexes = editedSetIndexesByExerciseId[exercise.id].orEmpty() + setIndex
        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId + (exercise.id to editedSetIndexes)

        // Trigger the (delayed) rest-timer refresh; the reset itself happens 2s later.
        intervalResetSignal += 1
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

        val selectedWeights = selectedSetWeightByExerciseId[focusedExercise.id]
            ?: focusedExercise.plannedWeightBySet
        val normalizedWeights = if (selectedWeights.size >= focusedExercise.sets) {
            selectedWeights.take(focusedExercise.sets)
        } else {
            selectedWeights + List(focusedExercise.sets - selectedWeights.size) { focusedExercise.plannedWeight }
        }

        scope.launch {
            repository.clearExerciseLogsForSession(activeSessionId, focusedExercise.id)
            normalizedReps.forEachIndexed { setIndex, reps ->
                repository.logSet(
                    sessionId = activeSessionId,
                    exercise = focusedExercise,
                    setNumber = setIndex + 1,
                    actualReps = reps,
                    actualWeight = normalizedWeights.getOrElse(setIndex) { "" }
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

    fun skipFocusedExercise() {
        val focusedExercise = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId } ?: return
        if (focusedExercise.id in loggedExerciseIds) {
            return
        }

        // Record the skip as a real save: log every set with 0 reps so it shows as
        // logged and can be undone/redone within the session by re-selecting it.
        val setCount = focusedExercise.sets
        if (setCount > 0) {
            selectedSetRepsByExerciseId = selectedSetRepsByExerciseId + (focusedExercise.id to List(setCount) { 0 })
        }
        if (activeSessionId != 0L) {
            val sessionId = activeSessionId
            val exerciseToSkip = focusedExercise
            scope.launch {
                repository.clearExerciseLogsForSession(sessionId, exerciseToSkip.id)
                repeat(setCount) { setIndex ->
                    repository.logSet(
                        sessionId = sessionId,
                        exercise = exerciseToSkip,
                        setNumber = setIndex + 1,
                        actualReps = 0,
                        actualWeight = ""
                    )
                }
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

    fun addSetToFocusedExercise() {
        val focusedExercise = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId } ?: return
        if (focusedExercise.id in loggedExerciseIds) {
            return
        }
        val newSets = (focusedExercise.sets + 1).coerceAtMost(8)
        if (newSets == focusedExercise.sets) {
            return
        }
        scope.launch {
            repository.updateExercise(
                exercise = focusedExercise,
                draft = ExerciseDraft(
                    name = focusedExercise.name,
                    sets = newSets,
                    reps = focusedExercise.reps,
                    intervalSeconds = focusedExercise.intervalSeconds,
                    plannedWeight = focusedExercise.plannedWeight,
                    remarks = focusedExercise.remarks
                )
            )
        }
    }

    fun removeSetFromFocusedExercise(setIndex: Int) {
        val focusedExercise = day.exercises.firstOrNull { exercise -> exercise.id == focusedExerciseId } ?: return
        if (focusedExercise.id in loggedExerciseIds) {
            return
        }
        if (focusedExercise.sets <= 1 || setIndex !in 0 until focusedExercise.sets) {
            return
        }

        // Keep the in-session selections aligned with the removed set.
        val currentReps = selectedSetRepsByExerciseId[focusedExercise.id]
            ?: List(focusedExercise.sets) { focusedExercise.reps }
        val currentWeights = selectedSetWeightByExerciseId[focusedExercise.id]
            ?: focusedExercise.plannedWeightBySet
        selectedSetRepsByExerciseId = selectedSetRepsByExerciseId +
            (focusedExercise.id to currentReps.filterIndexed { index, _ -> index != setIndex })
        selectedSetWeightByExerciseId = selectedSetWeightByExerciseId +
            (focusedExercise.id to currentWeights.filterIndexed { index, _ -> index != setIndex })
        val edited = editedSetIndexesByExerciseId[focusedExercise.id].orEmpty()
        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId +
            (
                focusedExercise.id to edited.mapNotNull { idx ->
                    when {
                        idx == setIndex -> null
                        idx > setIndex -> idx - 1
                        else -> idx
                    }
                }.toSet()
                )

        scope.launch {
            repository.removeExerciseSet(focusedExercise, setIndex)
        }
    }

    fun finishActiveSessionIfAny() {
        if (activeSessionId != 0L) {
            val sessionId = activeSessionId
            scope.launch {
                repository.finishSession(sessionId)
            }
        }
    }

    // Exiting without completing must not count as a workout: drop the started session
    // (and its logged sets) so only the Finish action records a completed workout.
    fun abandonActiveSessionIfAny() {
        if (activeSessionId != 0L) {
            val sessionId = activeSessionId
            scope.launch {
                repository.abandonSession(sessionId)
            }
        }
    }

    fun resetWorkoutModeState() {
        workoutActive = false
        editMode = false
        focusedExerciseId = 0L
        selectedSetRepsByExerciseId = emptyMap()
        selectedSetWeightByExerciseId = emptyMap()
        editedSetIndexesByExerciseId = emptyMap()
        loggedExerciseIds = emptySet()
        setPickerTarget = null
        weightPickerTarget = null
        sessionStartMessage = null
        activeSessionId = 0L
        showExitWorkoutModeConfirm = false
    }

    fun finishWorkout() {
        finishActiveSessionIfAny()
        scope.launch {
            repository.setWorkoutDone(
                dayNumber = day.dayNumber,
                plannedDateEpochDay = viewedDateEpochDay,
                isDone = true
            )
        }
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
        abandonActiveSessionIfAny()
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
                title = {
                    if (workoutActive) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TopBarStopwatch(
                                icon = Icons.Rounded.Timer,
                                iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                                timeText = formatStopwatch(totalElapsedSeconds),
                                flash = false,
                                modifier = Modifier.weight(0.4f)
                            )
                            VerticalDivider(
                                modifier = Modifier.height(26.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            TopBarStopwatch(
                                icon = Icons.Rounded.RestartAlt,
                                iconTint = MaterialTheme.colorScheme.primary,
                                timeText = formatStopwatch(intervalElapsedSeconds),
                                flash = intervalFlash,
                                blinking = intervalResetting,
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    } else {
                        Text("")
                    }
                },
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
                    if (!workoutActive) {
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
                                        editTemplateSnapshot = day.exercises
                                    } else if (hasEditChangesPendingExport) {
                                        // Edits persist live, so ask whether to keep them.
                                        showSaveEditsPrompt = true
                                    } else {
                                        editMode = false
                                        editTemplateSnapshot = null
                                    }
                                }
                            )
                        }
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
                if (!workoutActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
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
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            if (canEditTemplate) {
                                IconButton(onClick = { showRenameWorkoutDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Rename Workout",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        if (!workoutActive) {
                            Spacer(modifier = Modifier.height(10.dp))

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
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = workoutActive && !sessionStartMessage.isNullOrBlank(),
                    enter = fadeIn() + scaleIn(initialScale = 0.8f) + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        WorkoutSessionStartMessageCard(
                            message = sessionStartMessage.orEmpty(),
                            onDismiss = { sessionStartMessage = null }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (workoutActive) {
                    WorkoutActivePage(
                        day = day,
                        isSessionReady = activeSessionId != 0L,
                        focusedExerciseId = focusedExerciseId,
                        selectedSetRepsByExerciseId = selectedSetRepsByExerciseId,
                        selectedSetWeightByExerciseId = selectedSetWeightByExerciseId,
                        editedSetIndexesByExerciseId = editedSetIndexesByExerciseId,
                        loggedExerciseIds = loggedExerciseIds,
                        onFocusExercise = { exerciseId ->
                            // Re-selecting a logged/skipped exercise re-enables it for editing
                            // (undo a skip or fix a log without leaving the session).
                            if (exerciseId in loggedExerciseIds) {
                                loggedExerciseIds = loggedExerciseIds - exerciseId
                            }
                            focusedExerciseId = exerciseId
                        },
                        onSetTap = { exerciseId, setIndex ->
                            if (exerciseId !in loggedExerciseIds) {
                                setPickerTarget = exerciseId to setIndex
                            }
                        },
                        onWeightTap = { exerciseId, setIndex ->
                            if (exerciseId !in loggedExerciseIds) {
                                weightPickerTarget = exerciseId to setIndex
                            }
                        },
                        onLogFocusedExercise = { saveFocusedExerciseSets() },
                        onSkip = { skipFocusedExercise() },
                        onAddSet = { addSetToFocusedExercise() },
                        onRemoveSet = { setIndex -> removeSetTarget = setIndex },
                        onAddExercise = { showAddSessionExerciseDialog = true },
                        onFinish = { finishWorkout() },
                        onUpdateRemark = { newRemark -> updateFocusedExerciseRemark(newRemark) },
                        setLogs = setLogs,
                        activeSessionId = activeSessionId
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

    if (showAddSessionExerciseDialog) {
        RenameWorkoutDialog(
            dialogTitle = "Add Exercise",
            fieldLabel = "Exercise name",
            initialName = "",
            onDismiss = { showAddSessionExerciseDialog = false },
            onConfirm = { newName ->
                scope.launch {
                    repository.addExercise(
                        day.dayNumber,
                        ExerciseDraft(
                            name = newName,
                            sets = 1,
                            reps = 10,
                            intervalSeconds = 60,
                            plannedWeight = "",
                            remarks = ""
                        )
                    )
                }
                showAddSessionExerciseDialog = false
            }
        )
    }

    val activeRemoveSetIndex = removeSetTarget
    if (activeRemoveSetIndex != null) {
        AlertDialog(
            onDismissRequest = { removeSetTarget = null },
            title = { Text("Remove this set?") },
            text = { Text("Set ${activeRemoveSetIndex + 1} will be removed from this exercise.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        removeSetFromFocusedExercise(activeRemoveSetIndex)
                        removeSetTarget = null
                    }
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { removeSetTarget = null }) {
                    Text("Cancel")
                }
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
                    range = 0..8,
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

    weightPickerTarget?.let { (exerciseId, setIndex) ->
        val targetExercise = day.exercises.firstOrNull { exercise -> exercise.id == exerciseId }
        if (targetExercise != null) {
            val selectedForExercise = selectedSetWeightByExerciseId[exerciseId]
                ?: targetExercise.plannedWeightBySet
            val currentWeight = selectedForExercise.getOrElse(setIndex) { targetExercise.plannedWeight }
            val selectedHalfKg = ((parseWeightValue(currentWeight) ?: 20f) * 2f)
                .roundToInt()
                .coerceIn(0, 600)
            NumberWheelDialog(
                title = "${targetExercise.name} - Set ${setIndex + 1} weight",
                value = selectedHalfKg,
                range = 0..600,
                valueText = { halfKgStep -> "${formatHalfKgValue(halfKgStep)} kg" },
                onDismiss = { weightPickerTarget = null },
                onConfirm = { selectedHalfKgValue ->
                    val selectedKg = selectedHalfKgValue / 2f
                    updateSetWeightSelection(
                        exercise = targetExercise,
                        setIndex = setIndex,
                        selectedWeightText = "${formatKgValue(selectedKg)} kg"
                    )
                    weightPickerTarget = null
                }
            )
        } else {
            weightPickerTarget = null
        }
    }

    if (showSaveEditsPrompt) {
        AlertDialog(
            onDismissRequest = { showSaveEditsPrompt = false },
            title = { Text("Save changes?") },
            text = { Text("Keep the changes you made to this workout template?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveEditsPrompt = false
                        editMode = false
                        editTemplateSnapshot = null
                        showExportAfterEditPrompt = true
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSaveEditsPrompt = false
                        editMode = false
                        hasEditChangesPendingExport = false
                        val snapshot = editTemplateSnapshot
                        editTemplateSnapshot = null
                        if (snapshot != null) {
                            scope.launch {
                                repository.restoreDayExercises(day.dayNumber, snapshot)
                            }
                        }
                    }
                ) {
                    Text("Discard")
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
            title = { Text("Back up your changes?") },
            text = { Text("You changed the workout template. Go to Settings to export a backup now?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportAfterEditPrompt = false
                        hasEditChangesPendingExport = false
                        onRequestGoToSettings()
                    }
                ) {
                    Text("Go to settings")
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
            text = { Text("Press and hold the Exit button to end the current session. This prevents accidental exits from a stray touch.") },
            confirmButton = {
                HoldToConfirmButton(
                    text = "Hold to exit",
                    onConfirm = { exitWorkoutModeAndLeave() }
                )
            },
            dismissButton = {
                TextButton(onClick = { showExitWorkoutModeConfirm = false }) {
                    Text("Stay")
                }
            }
        )
    }
}

