package com.example.workoutassist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.workoutassist.data.ExerciseModel
import kotlin.math.roundToInt

@Composable
internal fun ExerciseRow(
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
                    Row {
                        Text(
                            text = "interval : ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${exercise.intervalSeconds}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                    Row {
                        Text(
                            text = "remarks : ",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = exercise.remarks.ifBlank { "No remarks" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                    }
                }

                if (isCurrent) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text("Current set: $currentSetNumber", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
