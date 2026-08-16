package com.example.workoutassist.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.ExerciseDraft

@Composable
internal fun ExerciseEditorDialog(
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
internal fun RenameWorkoutDialog(
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
