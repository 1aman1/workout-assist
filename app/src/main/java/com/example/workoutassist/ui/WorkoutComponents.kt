package com.example.workoutassist.ui

import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.NumberPicker
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
            repsBySet.getOrNull(index)?.let { reps -> "x$reps" } ?: "-"
        }
    }
    val weightValues = remember(weightBySet, columnCount) {
        List(columnCount) { index ->
            weightBySet.getOrNull(index)?.trim()?.ifBlank { "-" } ?: "-"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(tableScroll)
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

@Composable
private fun ExerciseSetTableRow(
    label: String,
    values: List<String>,
    editable: Boolean,
    onValueClickAt: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                    .width(58.dp)
                    .clickable(enabled = editable, onClick = { onValueClickAt(index) })
                    .padding(vertical = 6.dp),
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
    // Hold a reference to the underlying NumberPicker so Save reads the wheel's actual
    // current value even if the change listener didn't fire for the final position
    // (e.g. tapping Save right after a fling / on a freshly added set).
    var pickerHolder by remember { mutableStateOf<NumberPicker?>(null) }
    val latestOnConfirm by rememberUpdatedState(onConfirm)
    val confirmSelected: () -> Unit = {
        val committedIndex = (pickerHolder?.value ?: selectedIndex).coerceIn(0, wheelValues.lastIndex)
        latestOnConfirm(wheelValues[committedIndex])
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
                        }.also { picker -> pickerHolder = picker }
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
