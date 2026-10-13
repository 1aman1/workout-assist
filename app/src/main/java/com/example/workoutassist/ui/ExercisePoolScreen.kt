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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.ExerciseModel
import com.example.workoutassist.data.WorkoutDayModel
import com.example.workoutassist.data.WorkoutRepository
import kotlinx.coroutines.launch

// Manage-assignments screen: days on the left, exercises on the right. Picking a day
// shows checkboxes on the right for whether each exercise belongs to that day; fully
// unassigned exercises (zero days) can also be permanently removed from the pool.
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ExercisePoolScreen(
    repository: WorkoutRepository,
    days: List<WorkoutDayModel>,
    onBack: () -> Unit
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
    // Exercises already assigned to the selected day float to the top as a group.
    val orderedExercises = remember(exercises, selectedDayNumber) {
        exercises.sortedByDescending { selectedDayNumber in it.dayNumbers }
    }
    var deleteTarget by remember { mutableStateOf<ExerciseModel?>(null) }
    var showAddDayDialog by remember { mutableStateOf(false) }

    // Snapshot of each exercise's day assignments, taken once the real data loads, so
    // unsaved changes can be discarded by restoring it.
    var assignmentSnapshot by remember { mutableStateOf<Map<Long, Set<Int>>?>(null) }
    LaunchedEffect(exercises) {
        if (assignmentSnapshot == null && exercises.isNotEmpty()) {
            assignmentSnapshot = exercises.associate { it.id to it.dayNumbers }
        }
    }
    val hasPendingChanges = remember(exercises, assignmentSnapshot) {
        val snapshot = assignmentSnapshot
        snapshot != null && exercises.any { exercise -> snapshot[exercise.id] != exercise.dayNumbers }
    }
    var showSaveChangesPrompt by remember { mutableStateOf(false) }
    fun requestBack() {
        if (hasPendingChanges) {
            showSaveChangesPrompt = true
        } else {
            onBack()
        }
    }

    // Takes priority over the schedule-tab back handler below while there are unsaved
    // changes, so the hardware/gesture back also asks to save instead of silently leaving.
    BackHandler(enabled = hasPendingChanges) { showSaveChangesPrompt = true }

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
                    .weight(0.32f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(orderedDays, key = { it.dayNumber }) { day ->
                    DayListItem(
                        day = day,
                        selected = day.dayNumber == selectedDayNumber,
                        onClick = { selectedDayNumber = day.dayNumber }
                    )
                }
                item {
                    AddDayRow(onClick = { showAddDayDialog = true })
                }
            }

            VerticalDivider(modifier = Modifier.fillMaxHeight())

            if (orderedExercises.isEmpty()) {
                Text(
                    text = "No exercises yet.",
                    modifier = Modifier
                        .weight(0.68f)
                        .padding(16.dp)
                )
            } else {
                val poolListState = rememberLazyListState()
                Box(
                    modifier = Modifier
                        .weight(0.68f)
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
                        items(orderedExercises, key = { it.id }) { exercise ->
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
            text = { Text("Keep the day assignment changes you made?") },
            confirmButton = {
                TextButton(onClick = {
                    showSaveChangesPrompt = false
                    onBack()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSaveChangesPrompt = false
                    val snapshot = assignmentSnapshot
                    // Navigate from within the same coroutine, after the restore writes
                    // complete, so leaving the screen can't cancel them mid-flight.
                    scope.launch {
                        if (snapshot != null) {
                            exercises.forEach { exercise ->
                                val original = snapshot[exercise.id] ?: emptySet()
                                if (original != exercise.dayNumbers) {
                                    repository.setExerciseDayAssignments(exercise.id, original)
                                }
                            }
                        }
                        onBack()
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
private fun DayListItem(
    day: WorkoutDayModel,
    selected: Boolean,
    onClick: () -> Unit
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
        Column {
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
