package com.example.workoutassist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.WorkoutDayModel
import kotlinx.coroutines.launch

@Composable
private fun ScheduleDayCard(
    dayLabel: String,
    workoutName: String,
    exerciseCountText: String?,
    isToday: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    supportingText: String? = null,
    dateLabel: String? = null,
    leadingDateLabel: String? = null,
    dayLabelAlignEnd: Boolean = false
) {
    val cardHeight = when {
        isToday -> 132.dp
        !supportingText.isNullOrBlank() -> 102.dp
        else -> 86.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(cardHeight)
            .clickable { onClick() },
        shape = RoundedCornerShape(if (isToday) 20.dp else 18.dp),
        border = BorderStroke(
            width = if (isToday) 1.2.dp else 0.8.dp,
            color = if (isToday) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.34f)
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 6.dp else 2.dp
        ),
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
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (dateLabel.isNullOrBlank()) {
                    if (leadingDateLabel.isNullOrBlank()) {
                        Text(
                            text = dayLabel,
                            modifier = if (dayLabelAlignEnd) {
                                Modifier.fillMaxWidth()
                            } else {
                                Modifier
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = if (dayLabelAlignEnd) TextAlign.End else TextAlign.Start
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = leadingDateLabel,
                                modifier = Modifier.width(98.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = dayLabel,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Medium,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = workoutName,
                        style = if (isToday) {
                            MaterialTheme.typography.headlineSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        },
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    if (!supportingText.isNullOrBlank()) {
                        Text(
                            text = supportingText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = workoutName,
                            modifier = Modifier.weight(1f),
                            style = if (isToday) {
                                MaterialTheme.typography.headlineSmall
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!exerciseCountText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = exerciseCountText,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isCompleted) {
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Workout done",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SchedulePageSwitcher(
    scheduleLabel: String,
    infinityLabel: String,
    selectedPage: SchedulePage,
    onPageSelected: (SchedulePage) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SchedulePageSwitchItem(
                label = scheduleLabel,
                selected = selectedPage == SchedulePage.SCHEDULE,
                onClick = { onPageSelected(SchedulePage.SCHEDULE) },
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
            )

            SchedulePageSwitchItem(
                label = infinityLabel,
                selected = selectedPage == SchedulePage.INFINITY,
                onClick = { onPageSelected(SchedulePage.INFINITY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SchedulePageSwitchItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ScheduleScreen(
    days: List<WorkoutDayModel>,
    schedulePageLabel: String,
    infinityPageLabel: String,
    highlightedTodayDayNumber: Int?,
    completedSessionEpochDays: Set<Long>,
    onDaySelected: (Int) -> Unit
) {
    val orderedDays = remember(days) { days.sortedBy { it.dayNumber } }
    val dayCount = orderedDays.size
    val scheduleScope = rememberCoroutineScope()

    val scheduleListState = rememberLazyListState()
    val infinityListState = rememberLazyListState()
    var selectedPage by remember { mutableStateOf(SchedulePage.SCHEDULE) }
    val todayEpochDay = currentDateEpochDay()
    val infinityHalfCycles = 800
    val infinityTotalItems = if (dayCount == 0) 0 else dayCount * (infinityHalfCycles * 2 + 1)
    val defaultDayIndex = ((highlightedTodayDayNumber ?: 1) - 1).coerceIn(0, (dayCount - 1).coerceAtLeast(0))
    val infinityStartIndex = if (dayCount == 0) 0 else (infinityHalfCycles * dayCount) + defaultDayIndex
    val infinityTodayIndex = remember(
        orderedDays,
        dayCount,
        highlightedTodayDayNumber,
        todayEpochDay,
        infinityHalfCycles,
        infinityTotalItems,
        infinityStartIndex
    ) {
        if (dayCount == 0 || infinityTotalItems == 0) {
            return@remember 0
        }

        val highlightedIndex = highlightedTodayDayNumber
            ?.let { dayNumber -> orderedDays.indexOfFirst { it.dayNumber == dayNumber } }
            ?.takeIf { it >= 0 }
        if (highlightedIndex != null) {
            return@remember ((infinityHalfCycles * dayCount) + highlightedIndex)
                .coerceIn(0, infinityTotalItems - 1)
        }

        val mappedIndex = orderedDays.withIndex().firstNotNullOfOrNull { indexedDay ->
            val diff = todayEpochDay - indexedDay.value.plannedDateEpochDay
            if (diff % dayCount.toLong() != 0L) {
                null
            } else {
                val cycleOffset = (diff / dayCount.toLong())
                    .coerceIn(-infinityHalfCycles.toLong(), infinityHalfCycles.toLong())
                ((cycleOffset + infinityHalfCycles.toLong()) * dayCount + indexedDay.index).toInt()
            }
        }

        (mappedIndex ?: infinityStartIndex).coerceIn(0, infinityTotalItems - 1)
    }

    LaunchedEffect(selectedPage, infinityStartIndex, dayCount) {
        if (
            selectedPage == SchedulePage.INFINITY &&
            dayCount > 0 &&
            infinityListState.firstVisibleItemIndex == 0 &&
            infinityListState.firstVisibleItemScrollOffset == 0
        ) {
            infinityListState.scrollToItem(infinityStartIndex)
        }
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
                SchedulePageSwitcher(
                    scheduleLabel = schedulePageLabel,
                    infinityLabel = infinityPageLabel,
                    selectedPage = selectedPage,
                    onPageSelected = { selectedPage = it }
                )

                when (selectedPage) {
                    SchedulePage.SCHEDULE -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = scheduleListState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(orderedDays, key = { _, day -> day.dayNumber }) { _, day ->
                                    val isToday = day.dayNumber == highlightedTodayDayNumber
                                    ScheduleDayCard(
                                        dayLabel = "Day ${day.dayNumber}",
                                        workoutName = day.workoutName,
                                        exerciseCountText = null,
                                        leadingDateLabel = formatDateShort(day.plannedDateEpochDay),
                                        isToday = isToday,
                                        isCompleted = day.plannedDateEpochDay in completedSessionEpochDays,
                                        dayLabelAlignEnd = true,
                                        onClick = { onDaySelected(day.dayNumber) }
                                    )
                                }
                            }

                        }
                    }

                    SchedulePage.INFINITY -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            LazyColumn(
                                state = infinityListState,
                                modifier = Modifier.fillMaxSize(),
                                userScrollEnabled = true,
                                contentPadding = PaddingValues(bottom = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (dayCount > 0) {
                                    items(
                                        count = infinityTotalItems,
                                        key = { index -> index }
                                    ) { index ->
                                        val dayIndex = index % dayCount
                                        val cycleOffset = (index / dayCount) - infinityHalfCycles
                                        val day = orderedDays[dayIndex]
                                        val virtualDateEpochDay = day.plannedDateEpochDay +
                                            (cycleOffset.toLong() * dayCount.toLong())
                                        val isToday = virtualDateEpochDay == todayEpochDay
                                        val isCompletedForVirtualDate =
                                            virtualDateEpochDay in completedSessionEpochDays
                                        val cycleLabel = when {
                                            cycleOffset == 0 -> "Current cycle"
                                            cycleOffset > 0 -> "Cycle +$cycleOffset"
                                            else -> "Cycle $cycleOffset"
                                        }

                                        ScheduleDayCard(
                                            dayLabel = "Day ${day.dayNumber}",
                                            workoutName = day.workoutName,
                                            exerciseCountText = null,
                                            supportingText = cycleLabel,
                                            dateLabel = formatDateShort(virtualDateEpochDay),
                                            isToday = isToday,
                                            isCompleted = isCompletedForVirtualDate,
                                            onClick = { onDaySelected(day.dayNumber) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    scheduleScope.launch {
                                        infinityListState.animateScrollToItem(infinityTodayIndex)
                                    }
                                },
                                enabled = dayCount > 0,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 14.dp, bottom = 14.dp)
                                    .height(46.dp),
                                shape = RoundedCornerShape(999.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Text(
                                    text = "Today",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
}
