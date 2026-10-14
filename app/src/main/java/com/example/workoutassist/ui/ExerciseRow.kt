package com.example.workoutassist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.ExerciseModel

@Composable
internal fun ExerciseRow(
    exercise: ExerciseModel,
    editMode: Boolean,
    canEditTemplate: Boolean,
    canDelete: Boolean,
    isCurrent: Boolean,
    currentSetNumber: Int,
    canQuickEdit: Boolean,
    collapseSignal: Int,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
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
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isCurrent) 1.1.dp else 0.7.dp,
            color = when {
                isCurrent -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.42f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrent) 4.dp else 1.dp
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(enabled = canEditTemplate && canMoveUp) { onMoveUp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            contentDescription = "Move up",
                            tint = if (canEditTemplate && canMoveUp) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                }

                Text(
                    text = exercise.name,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { detailsExpanded = !detailsExpanded },
                    fontWeight = FontWeight.SemiBold
                )

                if (editMode) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(enabled = canEditTemplate && canMoveDown) { onMoveDown() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = "Move down",
                            tint = if (canEditTemplate && canMoveDown) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
                    }
                }

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
