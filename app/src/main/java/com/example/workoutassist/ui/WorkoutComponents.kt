package com.example.workoutassist.ui

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.NumberPicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlin.math.abs

@Composable
internal fun ExerciseSetTable(
    repsBySet: List<Int>,
    weightBySet: List<String>,
    editable: Boolean,
    onEditRepsAt: (Int) -> Unit,
    onEditWeightAt: (Int) -> Unit
) {
    val tableScroll = rememberScrollState()
    val columnCount = maxOf(repsBySet.size, weightBySet.size, 1)
    val repsValues = remember(repsBySet, columnCount) {
        List(columnCount) { index ->
            repsBySet.getOrNull(index)?.toString() ?: "-"
        }
    }
    val weightValues = remember(weightBySet, columnCount) {
        List(columnCount) { index ->
            weightBySet.getOrNull(index)?.ifBlank { "-" } ?: "-"
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(tableScroll)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExerciseSetTableRow(
                    label = "reps",
                    values = repsValues,
                    editable = editable,
                    onValueClickAt = onEditRepsAt
                )
                ExerciseSetTableRow(
                    label = "wgt",
                    values = weightValues,
                    editable = editable,
                    onValueClickAt = onEditWeightAt
                )
            }
        }
    }
}

@Composable
private fun ExerciseSetTableRow(
    label: String,
    values: List<String>,
    editable: Boolean,
    onValueClickAt: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.width(44.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        values.forEachIndexed { index, value ->
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable(enabled = editable, onClick = { onValueClickAt(index) })
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
internal fun NumberWheelDialog(
    title: String,
    value: Int,
    range: IntRange,
    step: Int = 1,
    valueText: (Int) -> String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val stepSize = step.coerceAtLeast(1)
    val wheelValues = remember(range.first, range.last, stepSize) {
        generateSequence(range.first) { previous -> previous + stepSize }
            .takeWhile { it <= range.last }
            .toList()
            .ifEmpty { listOf(range.first) }
    }
    val initialIndex = remember(value, wheelValues) {
        wheelValues.indices.minByOrNull { index -> abs(wheelValues[index] - value) } ?: 0
    }

    var selectedIndex by remember(value, range.first, range.last, stepSize) {
        mutableIntStateOf(initialIndex)
    }
    val latestOnConfirm by rememberUpdatedState(onConfirm)
    val confirmSelected: () -> Unit = {
        latestOnConfirm(wheelValues[selectedIndex.coerceIn(0, wheelValues.lastIndex)])
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.width(188.dp),
                    factory = { context ->
                        NumberPicker(context).apply {
                            val doubleTapDetector = GestureDetector(
                                context,
                                object : GestureDetector.SimpleOnGestureListener() {
                                    override fun onDown(event: MotionEvent): Boolean {
                                        return true
                                    }

                                    override fun onDoubleTap(event: MotionEvent): Boolean {
                                        confirmSelected()
                                        return true
                                    }
                                }
                            )

                            minValue = 0
                            maxValue = wheelValues.lastIndex
                            wrapSelectorWheel = wheelValues.size > 1
                            descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                            setOnTouchListener { _, motionEvent ->
                                doubleTapDetector.onTouchEvent(motionEvent)
                                false
                            }
                            setOnValueChangedListener { _, _, newValue ->
                                selectedIndex = newValue
                            }
                        }
                    },
                    update = { picker ->
                        picker.displayedValues = null
                        picker.minValue = 0
                        picker.maxValue = wheelValues.lastIndex
                        picker.displayedValues = wheelValues.map(valueText).toTypedArray()
                        picker.wrapSelectorWheel = wheelValues.size > 1

                        val safeSelected = selectedIndex.coerceIn(0, wheelValues.lastIndex)
                        if (picker.value != safeSelected) {
                            picker.value = safeSelected
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = confirmSelected) {
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
