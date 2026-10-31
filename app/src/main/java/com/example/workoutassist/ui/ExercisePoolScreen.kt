package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.ExerciseModel
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import kotlinx.coroutines.launch

// Which side of the split is the current editing/focus zone. Toggling this also reverses
// the left/right pane widths; moving a day up/down is only allowed while Day is in focus.
private enum class FocusSide { DAY, EXERCISE }

// Restores days to a target workout-name order by swapping slot content back (dayNumber
// slots themselves never move - only workoutName/exercises/history per swapDays). Used to
// discard a pending day reallocation when the user exits without saving.
private suspend fun restoreDayOrder(
    repository: WorkoutRepository,
    currentDays: List<WorkoutDayModel>,
    targetNames: List<String>
) {
    val slots = currentDays.sortedBy { it.dayNumber }.map { it.dayNumber }
    val nameBySlot = currentDays.associate { it.dayNumber to it.workoutName }.toMutableMap()
    for (i in targetNames.indices) {
        if (nameBySlot[slots[i]] == targetNames[i]) continue
        val matchIndex = (i + 1 until slots.size).firstOrNull { nameBySlot[slots[it]] == targetNames[i] } ?: continue
        repository.swapDays(slots[i], slots[matchIndex])
        val swapped = nameBySlot.getValue(slots[i])
        nameBySlot[slots[i]] = nameBySlot.getValue(slots[matchIndex])
        nameBySlot[slots[matchIndex]] = swapped
    }
}

// Manage-assignments screen: days on the left, exercises on the right. Picking a day
// shows checkboxes on the right for whether each exercise belongs to that day; fully
// unassigned exercises (zero days) can also be permanently removed from the pool.
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ExercisePoolScreen(
    repository: WorkoutRepository,
    days: List<WorkoutDayModel>,
    onBack: () -> Unit,
    onOpenDay: (Int) -> Unit = {},
    onPendingChangesChanged: (Boolean) -> Unit = {},
    pendingExternalExitAction: (() -> Unit)? = null,
    onPendingExternalExitActionConsumed: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val exercises by repository.observeAllExercises().collectAsState(initial = emptyList())
    val orderedDays = remember(days) { days.sortedBy { it.dayNumber } }
    // Keyed on just the day numbers (not the full day models, which embed each day's
    // exercise list) so ticking a checkbox doesn't reset the selected day.
    val dayNumbers = remember(orderedDays) { orderedDays.map { it.dayNumber } }
    var selectedDayNumber by remember(dayNumbers) {
        mutableStateOf(orderedDays.firstOrNull()?.dayNumber ?: 0)
    }
    val selectedDay = remember(orderedDays, selectedDayNumber) {
        orderedDays.firstOrNull { it.dayNumber == selectedDayNumber }
    }
    // Three groups: allocated to the selected day (in that day's workout sequence), then
    // allocated only to other days (alphabetical), then unallocated (alphabetical).
    val orderedExercises = remember(exercises, selectedDayNumber, selectedDay) {
        val positionInDay = selectedDay?.exercises
            ?.withIndex()
            ?.associate { (index, exercise) -> exercise.id to index }
            .orEmpty()
        fun group(exercise: ExerciseModel) = when {
            selectedDayNumber in exercise.dayNumbers -> 0
            exercise.dayNumbers.isNotEmpty() -> 1
            else -> 2
        }
        exercises.sortedWith(
            compareBy<ExerciseModel> { group(it) }
                .thenBy { if (group(it) == 0) positionInDay[it.id] ?: Int.MAX_VALUE else 0 }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { if (group(it) == 0) "" else it.name }
        )
    }
    var deleteTarget by remember { mutableStateOf<ExerciseModel?>(null) }
    var showAddDayDialog by remember { mutableStateOf(false) }

    // Default focus is Exercise (right side, current 32/68 split); toggling to Day flips
    // the split (~70/30) and turns the days list into an editing/reorder zone.
    var focusSide by remember { mutableStateOf(FocusSide.EXERCISE) }
    val daysWeight = if (focusSide == FocusSide.DAY) 0.7f else 0.32f
    val exercisesWeight = if (focusSide == FocusSide.DAY) 0.3f else 0.68f
    // Day focus mirrors the selected day's own workout page: only its exercises, in the
    // user-set sequence (orderedExercises already lists them first in that order).
    val visibleExercises = if (focusSide == FocusSide.DAY) {
        orderedExercises.filter { selectedDayNumber in it.dayNumbers }
    } else {
        orderedExercises
    }

    // Snapshot of each exercise's day assignments, taken once the real data loads, so
    // unsaved changes can be discarded by restoring it.
    var assignmentSnapshot by remember { mutableStateOf<Map<Long, Set<Int>>?>(null) }
    LaunchedEffect(exercises) {
        if (assignmentSnapshot == null && exercises.isNotEmpty()) {
            assignmentSnapshot = exercises.associate { it.id to it.dayNumbers }
        }
    }
    val hasAssignmentChanges = remember(exercises, assignmentSnapshot) {
        val snapshot = assignmentSnapshot
        snapshot != null && exercises.any { exercise -> snapshot[exercise.id] != exercise.dayNumbers }
    }
    // Snapshot of the day order (by workout name, since dayNumber slots stay fixed and
    // reordering swaps content between them), so a reallocation can also be discarded.
    var dayOrderSnapshot by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(orderedDays) {
        if (dayOrderSnapshot == null && orderedDays.isNotEmpty()) {
            dayOrderSnapshot = orderedDays.map { it.workoutName }
        }
    }
    val hasDayOrderChanges = remember(orderedDays, dayOrderSnapshot) {
        val snapshot = dayOrderSnapshot
        snapshot != null && snapshot != orderedDays.map { it.workoutName }
    }
    val hasPendingChanges = hasAssignmentChanges || hasDayOrderChanges
    var showSaveChangesPrompt by remember { mutableStateOf(false) }
    // The action to perform once the save/discard prompt resolves - defaults to the
    // screen's own back navigation, but an external caller (e.g. a bottom-nav tab switch)
    // can route its own navigation through the same prompt instead of bypassing it.
    var resolvedExitAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    fun requestExit(action: () -> Unit) {
        if (hasPendingChanges) {
            resolvedExitAction = action
            showSaveChangesPrompt = true
        } else {
            action()
        }
    }
    fun requestBack() = requestExit(onBack)

    LaunchedEffect(hasPendingChanges) { onPendingChangesChanged(hasPendingChanges) }
    LaunchedEffect(pendingExternalExitAction) {
        val action = pendingExternalExitAction ?: return@LaunchedEffect
        if (hasPendingChanges) {
            resolvedExitAction = action
            showSaveChangesPrompt = true
        } else {
            action()
        }
        onPendingExternalExitActionConsumed()
    }

    // Takes priority over the schedule-tab back handler below while there are unsaved
    // changes, so the hardware/gesture back also asks to save instead of silently leaving.
    BackHandler(enabled = hasPendingChanges) {
        resolvedExitAction = onBack
        showSaveChangesPrompt = true
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                title = { Text("Exercise Pool") },
                navigationIcon = {
                    IconButton(onClick = { requestBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    FocusToggle(
                        focusSide = focusSide,
                        onFocusSideChange = { focusSide = it },
                        modifier = Modifier.padding(end = 12.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(daysWeight)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(orderedDays, key = { _, day -> day.dayNumber }) { index, day ->
                    DayListItem(
                        day = day,
                        selected = day.dayNumber == selectedDayNumber,
                        showReorderControls = focusSide == FocusSide.DAY,
                        canMoveUp = index > 0,
                        canMoveDown = index < orderedDays.lastIndex,
                        onClick = { selectedDayNumber = day.dayNumber },
                        onOpenDay = { requestExit { onOpenDay(day.dayNumber) } },
                        onMoveUp = { scope.launch { repository.moveDay(day.dayNumber, -1) } },
                        onMoveDown = { scope.launch { repository.moveDay(day.dayNumber, 1) } }
                    )
                }
                item {
                    AddDayRow(onClick = { showAddDayDialog = true })
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            if (visibleExercises.isEmpty()) {
                Text(
                    text = if (focusSide == FocusSide.DAY) "No exercises in this day." else "No exercises yet.",
                    modifier = Modifier
                        .weight(exercisesWeight)
                        .padding(16.dp)
                )
            } else {
                val poolListState = rememberLazyListState()
                Box(
                    modifier = Modifier
                        .weight(exercisesWeight)
                        .fillMaxHeight()
                ) {
                    LazyColumn(
                        state = poolListState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        items(visibleExercises, key = { it.id }) { exercise ->
                            ExercisePoolRow(
                                exercise = exercise,
                                selectedDayNumber = selectedDayNumber,
                                onToggleSelectedDay = { assigned ->
                                    scope.launch {
                                        if (assigned) {
                                            repository.assignExerciseToDay(exercise.id, selectedDayNumber)
                                        } else {
                                            repository.unassignExerciseFromDay(selectedDayNumber, exercise.id)
                                        }
                                    }
                                },
                                onDelete = { deleteTarget = exercise }
                            )
                        }
                    }
                    PoolScrollbar(
                        listState = poolListState,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxHeight()
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }

    if (showAddDayDialog) {
        RenameWorkoutDialog(
            dialogTitle = "Add Day",
            fieldLabel = "Workout name",
            initialName = "",
            onDismiss = { showAddDayDialog = false },
            onConfirm = { name ->
                scope.launch {
                    val newDayNumber = repository.addDay(name)
                    selectedDayNumber = newDayNumber
                }
                showAddDayDialog = false
            }
        )
    }

    if (showSaveChangesPrompt) {
        AlertDialog(
            onDismissRequest = { showSaveChangesPrompt = false },
            title = { Text("Save changes?") },
            text = { Text("Keep the day assignment and reorder changes you made?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveChangesPrompt = false
                    (resolvedExitAction ?: onBack)()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveChangesPrompt = false
                    val assignmentSnapshotCopy = assignmentSnapshot
                    val dayOrderSnapshotCopy = dayOrderSnapshot
                    // Navigate from within the same coroutine, after the restore writes
                    // complete, so leaving the screen can't cancel them mid-flight.
                    scope.launch {
                        if (assignmentSnapshotCopy != null) {
                            exercises.forEach { exercise ->
                                val original = assignmentSnapshotCopy[exercise.id] ?: emptySet()
                                if (original != exercise.dayNumbers) {
                                    repository.setExerciseDayAssignments(exercise.id, original)
                                }
                            }
                        }
                        if (dayOrderSnapshotCopy != null) {
                            restoreDayOrder(repository, orderedDays, dayOrderSnapshotCopy)
                        }
                        (resolvedExitAction ?: onBack)()
                    }
                }) {
                    Text("Discard")
                }
            }
        )
    }

    deleteTarget?.let { exercise ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Exercise") },
            text = { Text("Permanently delete \"${exercise.name}\" from the pool? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.deleteExercisePermanently(exercise.id) }
                    deleteTarget = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PoolScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val showScrollbar by remember {
        derivedStateOf { listState.layoutInfo.totalItemsCount > listState.layoutInfo.visibleItemsInfo.size }
    }
    if (!showScrollbar) return

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
    val thumbColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    Canvas(
        modifier = modifier.width(4.dp)
    ) {
        val totalCount = listState.layoutInfo.totalItemsCount
        val visibleCount = listState.layoutInfo.visibleItemsInfo.size
        val thumbHeight = (size.height * visibleCount / totalCount).coerceAtLeast(size.width * 6)
        val maxOffset = size.height - thumbHeight
        val scrollFraction = listState.firstVisibleItemIndex.toFloat() / (totalCount - visibleCount).coerceAtLeast(1)
        val thumbOffset = (maxOffset * scrollFraction).coerceIn(0f, maxOffset)

        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(size.width / 2)
        )
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(0f, thumbOffset),
            size = Size(size.width, thumbHeight),
            cornerRadius = CornerRadius(size.width / 2)
        )
    }
}

@Composable
private fun AddDayRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Add Day",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun FocusToggle(
    focusSide: FocusSide,
    onFocusSideChange: (FocusSide) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                RoundedCornerShape(20.dp)
            )
            .padding(2.dp)
    ) {
        FocusToggleSegment(
            label = "Day",
            selected = focusSide == FocusSide.DAY,
            onClick = { onFocusSideChange(FocusSide.DAY) }
        )
        FocusToggleSegment(
            label = "Exercise",
            selected = focusSide == FocusSide.EXERCISE,
            onClick = { onFocusSideChange(FocusSide.EXERCISE) }
        )
    }
}

@Composable
private fun FocusToggleSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun DayListItem(
    day: WorkoutDayModel,
    selected: Boolean,
    showReorderControls: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onClick: () -> Unit,
    onOpenDay: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Day ${day.dayNumber}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = day.workoutName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }
            if (showReorderControls) {
                IconButton(onClick = onOpenDay, modifier = Modifier.width(40.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Open ${day.workoutName} workout page"
                    )
                }
                Column {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Move day up"
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.width(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Move day down"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExercisePoolRow(
    exercise: ExerciseModel,
    selectedDayNumber: Int,
    onToggleSelectedDay: (assigned: Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val assignedToSelectedDay = selectedDayNumber in exercise.dayNumbers
    val otherDays = exercise.dayNumbers - selectedDayNumber

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = assignedToSelectedDay,
            onCheckedChange = onToggleSelectedDay
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            val subtitle = when {
                otherDays.isNotEmpty() -> "Also on: ${otherDays.sorted().joinToString { "Day $it" }}"
                exercise.dayNumbers.isEmpty() -> "Unassigned - not on any day"
                else -> null
            }
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (exercise.dayNumbers.isEmpty()) {
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete permanently",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
