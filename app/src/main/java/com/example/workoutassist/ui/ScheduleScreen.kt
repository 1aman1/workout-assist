package com.example.workoutassist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.WorkoutDayModel

private enum class DayStatus { DONE, DUE, FUTURE, MISSED }

private data class ScheduleEntry(
    val epochDay: Long,
    val dayNumber: Int,
    val workoutName: String,
    val status: DayStatus
)

// Rows rendered in the list. In compact mode, runs of missed days between two shown
// dates are collapsed into a thin "domino" strip (one red pip per missed day).
private sealed interface ScheduleRow {
    data class Entry(val entry: ScheduleEntry) : ScheduleRow
    data class Gap(val afterEpochDay: Long, val count: Int) : ScheduleRow
}

// The merged schedule is a single factual + projected timeline:
//  - DONE:    a workout was logged on that date (past or today).
//  - DUE:     today, no workout logged yet (the next day in the cycle).
//  - MISSED:  a past date with no workout (only shown in the expanded/calendar view).
//  - FUTURE:  projected upcoming days of the CURRENT cycle, up to the last day (day 7).
// The current cycle is always extended to its final day, but never beyond it.
private fun buildMergedTimeline(
    cycle: List<WorkoutDayModel>,
    completedDays: Set<Long>,
    completedWorkoutByDate: Map<Long, String>,
    completedDayNumberByDate: Map<Long, Int>,
    nextDueDay: WorkoutDayModel?,
    todayEpochDay: Long
): List<ScheduleEntry> {
    if (cycle.isEmpty()) return emptyList()
    val dayCount = cycle.size
    val todayDone = todayEpochDay in completedDays

    // Past window starts at the first logged workout (so we don't render endless
    // empty history), capped to ~180 days back.
    val earliestDone = completedDays.minOrNull()
    val windowStart = if (earliestDone != null) {
        earliestDone.coerceAtLeast(todayEpochDay - 180L)
    } else {
        todayEpochDay
    }

    val result = mutableListOf<ScheduleEntry>()

    // Past + today (factual record).
    var date = windowStart
    while (date <= todayEpochDay) {
        when {
            date in completedDays -> result.add(
                ScheduleEntry(
                    epochDay = date,
                    dayNumber = completedDayNumberByDate[date] ?: 0,
                    workoutName = completedWorkoutByDate[date].orEmpty(),
                    status = DayStatus.DONE
                )
            )

            date == todayEpochDay -> result.add(
                ScheduleEntry(
                    epochDay = date,
                    dayNumber = nextDueDay?.dayNumber ?: 0,
                    workoutName = nextDueDay?.workoutName.orEmpty(),
                    status = DayStatus.DUE
                )
            )

            else -> result.add(
                ScheduleEntry(
                    epochDay = date,
                    dayNumber = 0,
                    workoutName = "",
                    status = DayStatus.MISSED
                )
            )
        }
        date += 1L
    }

    // Future days of the current cycle, projected forward to the last day number.
    val startDn = nextDueDay?.dayNumber ?: 1
    val firstFutureDn = if (todayDone) startDn else startDn + 1
    var projectedDate = todayEpochDay + 1L
    for (dn in firstFutureDn..dayCount) {
        val day = cycle.firstOrNull { it.dayNumber == dn } ?: continue
        result.add(
            ScheduleEntry(
                epochDay = projectedDate,
                dayNumber = dn,
                workoutName = day.workoutName,
                status = DayStatus.FUTURE
            )
        )
        projectedDate += 1L
    }

    return result
}

// The next day due = the immediate next day in the cycle after the most recently
// completed workout, including rest days (so a rest day can be "up next").
private fun computeNextDueDay(
    cycle: List<WorkoutDayModel>,
    lastCompletedDayNumber: Int?
): WorkoutDayModel? {
    if (cycle.isEmpty()) return null
    val n = cycle.size
    val startIndex = lastCompletedDayNumber
        ?.let { dayNumber -> cycle.indexOfFirst { it.dayNumber == dayNumber } }
        ?: -1
    val nextIndex = (((startIndex + 1) % n) + n) % n
    return cycle[nextIndex]
}

@Composable
private fun ScheduleEntryCard(
    entry: ScheduleEntry,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    isRestDay: Boolean = false,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)? = null
) {
    val isDone = entry.status == DayStatus.DONE
    val isFuture = entry.status == DayStatus.FUTURE
    val dayLabelColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onDoubleClick = onDoubleClick),
        shape = RoundedCornerShape(if (isToday) 20.dp else 16.dp),
        border = BorderStroke(
            width = if (isToday) 1.2.dp else 0.8.dp,
            color = if (isToday) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 6.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (entry.dayNumber > 0) "Day ${entry.dayNumber}" else "Day –",
                modifier = Modifier.width(64.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = dayLabelColor
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatDateShort(entry.epochDay),
                    style = if (isToday) {
                        MaterialTheme.typography.titleMedium
                    } else {
                        MaterialTheme.typography.titleSmall
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isFuture) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                if (entry.workoutName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = entry.workoutName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isFuture) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                if (entry.status == DayStatus.DUE) {
                    Text(
                        text = if (isRestDay) "Rest day · tap to mark done" else "Today · tap to start",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (isDone) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Done",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MissedDayCard(
    entry: ScheduleEntry,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBF360C))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDateShort(entry.epochDay),
                modifier = Modifier.width(72.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Missed · tap to add",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun GapDominoStrip(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .height(5.dp)
                    .background(
                        color = Color(0xFFBF360C),
                        shape = RoundedCornerShape(3.dp)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleScreen(
    days: List<WorkoutDayModel>,
    planTitle: String,
    schedulePageLabel: String,
    infinityPageLabel: String,
    lastCompletedDayNumber: Int?,
    completedSessionEpochDays: Set<Long>,
    completedWorkoutByDate: Map<Long, String>,
    completedDayNumberByDate: Map<Long, Int>,
    onLogBackdatedWorkout: (Int, Long) -> Unit,
    onRemoveWorkoutOnDate: (Long) -> Unit,
    onDaySelected: (Int) -> Unit
) {
    val orderedDays = remember(days) { days.sortedBy { it.dayNumber } }
    val todayEpochDay = currentDateEpochDay()
    val listState = rememberLazyListState()

    var expanded by remember { mutableStateOf(false) }
    var editDateTarget by remember { mutableStateOf<Long?>(null) }
    var removeConfirmDate by remember { mutableStateOf<Long?>(null) }
    // Set when the user toggles Compact/Calendar, so the transition can reveal the
    // change (scroll up to show missed days on expand, ease back to today on collapse).
    var justToggled by remember { mutableStateOf(false) }

    val nextDueDay = remember(orderedDays, lastCompletedDayNumber) {
        computeNextDueDay(orderedDays, lastCompletedDayNumber)
    }
    val timeline = remember(
        orderedDays,
        completedSessionEpochDays,
        completedWorkoutByDate,
        completedDayNumberByDate,
        nextDueDay,
        todayEpochDay
    ) {
        buildMergedTimeline(
            cycle = orderedDays,
            completedDays = completedSessionEpochDays,
            completedWorkoutByDate = completedWorkoutByDate,
            completedDayNumberByDate = completedDayNumberByDate,
            nextDueDay = nextDueDay,
            todayEpochDay = todayEpochDay
        )
    }
    val displayEntries = remember(timeline, expanded) {
        if (expanded) timeline else timeline.filter { it.status != DayStatus.MISSED }
    }
    // In compact mode, collapse missed-day runs between shown dates into a domino strip.
    val renderRows = remember(displayEntries, expanded) {
        if (expanded) {
            displayEntries.map { ScheduleRow.Entry(it) }
        } else {
            buildList<ScheduleRow> {
                displayEntries.forEachIndexed { index, entry ->
                    add(ScheduleRow.Entry(entry))
                    val next = displayEntries.getOrNull(index + 1)
                    if (next != null) {
                        val missed = (next.epochDay - entry.epochDay - 1L).toInt()
                        if (missed > 0) {
                            add(ScheduleRow.Gap(afterEpochDay = entry.epochDay, count = missed))
                        }
                    }
                }
            }
        }
    }

    // Initial positioning: land a couple of recent entries above today.
    LaunchedEffect(Unit) {
        if (renderRows.isNotEmpty()) {
            val todayIndex = renderRows.indexOfFirst {
                it is ScheduleRow.Entry && it.entry.epochDay == todayEpochDay
            }
            val target = if (todayIndex >= 0) {
                (todayIndex - 2).coerceAtLeast(0)
            } else {
                (renderRows.size - 1).coerceAtLeast(0)
            }
            listState.scrollToItem(target)
        }
    }

    // Reveal-on-expand: after a toggle, animate-scroll so the change is visible.
    // Expanding (Calendar) scrolls up to reveal the newly inserted missed-day cards
    // above today; collapsing (Compact) eases back to today's resting position.
    LaunchedEffect(expanded) {
        if (!justToggled) return@LaunchedEffect
        justToggled = false
        val todayIndex = renderRows.indexOfFirst {
            it is ScheduleRow.Entry && it.entry.epochDay == todayEpochDay
        }
        if (todayIndex < 0) return@LaunchedEffect
        val target = if (expanded) {
            (todayIndex - 8).coerceAtLeast(0)
        } else {
            (todayIndex - 2).coerceAtLeast(0)
        }
        listState.animateScrollToItem(target)
    }

    val scheduleGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.24f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scheduleGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = planTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = {
                        justToggled = true
                        expanded = !expanded
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(if (expanded) schedulePageLabel else infinityPageLabel)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (displayEntries.isEmpty()) {
                    Text(
                        text = "No history yet. Start today's workout, or switch to Calendar to backfill a past day.",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        contentPadding = PaddingValues(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = renderRows,
                            key = { row ->
                                when (row) {
                                    is ScheduleRow.Entry -> "e-${row.entry.epochDay}"
                                    is ScheduleRow.Gap -> "g-${row.afterEpochDay}"
                                }
                            }
                        ) { row ->
                            when (row) {
                                is ScheduleRow.Gap -> GapDominoStrip(
                                    count = row.count,
                                    modifier = Modifier.animateItem(fadeOutSpec = null)
                                )

                                is ScheduleRow.Entry -> {
                                    val entry = row.entry
                                    val entryIsRest = orderedDays
                                        .firstOrNull { it.dayNumber == entry.dayNumber }
                                        ?.exercises?.isEmpty() == true
                                    when (entry.status) {
                                        DayStatus.MISSED -> MissedDayCard(
                                            entry = entry,
                                            modifier = Modifier.animateItem(fadeOutSpec = null),
                                            onClick = { editDateTarget = entry.epochDay }
                                        )

                                        else -> ScheduleEntryCard(
                                            entry = entry,
                                            isToday = entry.epochDay == todayEpochDay,
                                            isRestDay = entryIsRest,
                                            modifier = Modifier.animateItem(fadeOutSpec = null),
                                            onClick = {
                                                when (entry.status) {
                                                    DayStatus.DUE -> if (entryIsRest) {
                                                        onLogBackdatedWorkout(entry.dayNumber, todayEpochDay)
                                                    } else {
                                                        onDaySelected(entry.dayNumber)
                                                    }
                                                    DayStatus.DONE -> if (entry.dayNumber > 0) onDaySelected(entry.dayNumber)
                                                    DayStatus.FUTURE -> if (entry.dayNumber > 0) onDaySelected(entry.dayNumber)
                                                    else -> Unit
                                                }
                                            },
                                            onDoubleClick = if (entry.status == DayStatus.DONE) {
                                                { removeConfirmDate = entry.epochDay }
                                            } else {
                                                null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val removeDate = removeConfirmDate
            if (removeDate != null) {
                val loggedName = completedWorkoutByDate[removeDate] ?: "workout"
                AlertDialog(
                    onDismissRequest = { removeConfirmDate = null },
                    title = { Text("Remove workout?") },
                    text = { Text("Remove \"$loggedName\" logged on ${formatDateShort(removeDate)}?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onRemoveWorkoutOnDate(removeDate)
                                removeConfirmDate = null
                            }
                        ) {
                            Text("Remove")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { removeConfirmDate = null }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            val editDate = editDateTarget
            if (editDate != null) {
                val doneName = completedWorkoutByDate[editDate]
                AlertDialog(
                    onDismissRequest = { editDateTarget = null },
                    title = { Text(formatDateShort(editDate)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (doneName != null) {
                                Text(
                                    text = "Logged: $doneName",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = "Mark a workout you did on this day:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                orderedDays
                                    .forEach { day ->
                                        OutlinedButton(
                                            onClick = {
                                                onLogBackdatedWorkout(day.dayNumber, editDate)
                                                editDateTarget = null
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(day.workoutName)
                                        }
                                    }
                            }
                        }
                    },
                    confirmButton = {
                        if (doneName != null) {
                            TextButton(
                                onClick = {
                                    onRemoveWorkoutOnDate(editDate)
                                    editDateTarget = null
                                }
                            ) {
                                Text("Remove")
                            }
                        } else {
                            TextButton(onClick = { editDateTarget = null }) {
                                Text("Cancel")
                            }
                        }
                    },
                    dismissButton = {
                        if (doneName != null) {
                            TextButton(onClick = { editDateTarget = null }) {
                                Text("Cancel")
                            }
                        }
                    }
                )
            }
        }
    }
}
