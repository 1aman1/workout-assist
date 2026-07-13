package com.example.workoutassist.ui

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.workoutassist.data.ExerciseDraft
import com.example.workoutassist.data.ExerciseModel
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
    onRequestExport: () -> Unit,
    onBack: () -> Unit,
    onWorkoutActiveChange: (Boolean) -> Unit = {}
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

    fun updateSetWeightSelection(exercise: ExerciseModel, setIndex: Int, selectedWeightText: String) {
        val current = selectedSetWeightByExerciseId[exercise.id]
            ?: exercise.plannedWeightBySet
        if (setIndex !in current.indices) {
            return
        }
        val updated = current.toMutableList()
        updated[setIndex] = selectedWeightText
        selectedSetWeightByExerciseId = selectedSetWeightByExerciseId + (exercise.id to updated.toList())

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
        showFinishConfirm = false
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
                    if (workoutActive) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(end = 14.dp)
                        ) {
                            Text(
                                text = "${loggedExerciseIds.size}/${day.exercises.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = formatDateShort(viewedDateEpochDay),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
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

                        if (!workoutActive) {
                            Text(
                                text = formatDateShort(viewedDateEpochDay),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f)
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
                    Text(
                        text = focusedExercise.name,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )

                    val intervalLabel = focusedExercise.intervalSeconds.let { seconds ->
                        when {
                            seconds <= 0 -> ""
                            seconds < 60 -> "${seconds}s"
                            seconds % 60 == 0 -> "${seconds / 60}m"
                            else -> "${seconds / 60}m ${seconds % 60}s"
                        }
                    }
                    if (intervalLabel.isNotBlank()) {
                        Text(
                            text = "Rest $intervalLabel between sets",
                            modifier = Modifier.fillMaxWidth(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
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
                        val weightLabel = rawWeight.trim().ifBlank { "—" }
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

                TextButton(
                    onClick = onSkip,
                    enabled = focusedExercise != null && focusedExercise.id !in loggedExerciseIds,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Skip")
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
                    modifier = Modifier
                        .weight(1f)
                        .clickable { detailsExpanded = !detailsExpanded },
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
                    }
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
                        text = "interval",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${exercise.intervalSeconds}s",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
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
