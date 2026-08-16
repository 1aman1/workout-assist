package com.example.workoutassist.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workoutassist.data.SetLogEntity
import com.example.workoutassist.data.WorkoutSessionEntity

private data class DatedValue(val epochDay: Long, val value: Float)

private val WEIGHT_NUMBER_REGEX = Regex("-?\\d+(?:\\.\\d+)?")

private fun parseWeightKg(raw: String): Float? =
    WEIGHT_NUMBER_REGEX.find(raw.trim())?.value?.toFloatOrNull()

private fun formatValue(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else String.format("%.1f", value)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WorkoutGraphsScreen(
    sessions: List<WorkoutSessionEntity>,
    setLogs: List<SetLogEntity>,
    title: String = "Progress Graphs",
    onBack: () -> Unit
) {
    val todayEpochDay = currentDateEpochDay()

    // Only finished sessions count — same source as Insights.
    val finishedSessionIds = remember(sessions) {
        sessions.asSequence().filter { it.finishedAt != null }.map { it.id }.toSet()
    }
    val completedDays = remember(sessions) {
        sessions.asSequence()
            .mapNotNull { it.finishedAt }
            .map { timestampMillisToEpochDay(it) }
            .toSet()
    }
    val trackedLogs = remember(setLogs, finishedSessionIds) {
        setLogs.filter { it.sessionId in finishedSessionIds }
    }

    // Consistency ---------------------------------------------------------
    val doneLast7 = remember(completedDays, todayEpochDay) {
        completedInWindow(completedDays, todayEpochDay, 7)
    }
    val doneLast30 = remember(completedDays, todayEpochDay) {
        completedInWindow(completedDays, todayEpochDay, 30)
    }

    // Weekly frequency (last 8 weeks, oldest -> newest) -------------------
    val weeklyFrequency = remember(completedDays, todayEpochDay) {
        (7 downTo 0).map { weeksAgo ->
            val weekEnd = todayEpochDay - 7L * weeksAgo
            val weekStart = weekEnd - 6L
            val count = completedDays.count { it in weekStart..weekEnd }
            count.toFloat()
        }
    }

    // Exercise selector ---------------------------------------------------
    val exerciseNames = remember(trackedLogs) {
        trackedLogs.asSequence()
            .map { it.exerciseName.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
            .toList()
    }
    var selectedExercise by remember(exerciseNames) {
        mutableStateOf(exerciseNames.firstOrNull())
    }

    val logsByDate = remember(trackedLogs, selectedExercise) {
        val name = selectedExercise
        if (name == null) {
            emptyMap()
        } else {
            trackedLogs
                .filter { it.exerciseName.trim() == name }
                .groupBy { timestampMillisToEpochDay(it.loggedAt) }
                .toSortedMap()
        }
    }
    val weightPoints = remember(logsByDate) {
        logsByDate.mapNotNull { (day, logs) ->
            val maxWeight = logs
                .mapNotNull { parseWeightKg(it.actualWeight.ifBlank { it.plannedWeight }) }
                .maxOrNull()
            if (maxWeight == null) null else DatedValue(day, maxWeight)
        }
    }
    val repsPoints = remember(logsByDate) {
        logsByDate.map { (day, logs) ->
            DatedValue(day, logs.sumOf { it.actualReps.coerceAtLeast(0) }.toFloat())
        }
    }

    val graphsGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.18f)
        )
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(graphsGradient)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ConsistencyCard(
                doneLast7 = doneLast7,
                doneLast30 = doneLast30
            )

            FrequencyCard(weeklyFrequency = weeklyFrequency)

            ExerciseProgressCard(
                exerciseNames = exerciseNames,
                selectedExercise = selectedExercise,
                onExerciseSelected = { selectedExercise = it },
                weightPoints = weightPoints,
                repsPoints = repsPoints
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ConsistencyCard(
    doneLast7: Int,
    doneLast30: Int
) {
    GraphCard(title = "Consistency") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatRing(
                fraction = doneLast7 / 7f,
                valueText = "$doneLast7/7",
                caption = "Last 7 days",
                ringColor = MaterialTheme.colorScheme.primary
            )
            StatRing(
                fraction = doneLast30 / 30f,
                valueText = "$doneLast30/30",
                caption = "Last 30 days",
                ringColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun FrequencyCard(weeklyFrequency: List<Float>) {
    GraphCard(title = "Workouts per week") {
        val hasData = weeklyFrequency.any { it > 0f }
        if (!hasData) {
            EmptyGraphHint("No finished workouts yet.")
            return@GraphCard
        }
        MiniBarChart(
            values = weeklyFrequency,
            barColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "8 weeks ago",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "this week",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "Days trained in each of the last 8 weeks.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExerciseProgressCard(
    exerciseNames: List<String>,
    selectedExercise: String?,
    onExerciseSelected: (String) -> Unit,
    weightPoints: List<DatedValue>,
    repsPoints: List<DatedValue>
) {
    GraphCard(title = "Exercise progress") {
        if (exerciseNames.isEmpty()) {
            EmptyGraphHint("Log some sets to see per-exercise trends.")
            return@GraphCard
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            exerciseNames.forEach { name ->
                FilterChip(
                    selected = name == selectedExercise,
                    onClick = { onExerciseSelected(name) },
                    label = { Text(name) }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Weight trend
        Text(
            text = "Top weight (kg)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (weightPoints.size < 1) {
            EmptyGraphHint("No weight recorded for this exercise.")
        } else {
            TrendBlock(
                points = weightPoints,
                lineColor = MaterialTheme.colorScheme.primary,
                fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Reps trend
        Text(
            text = "Total reps per session",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        if (repsPoints.isEmpty()) {
            EmptyGraphHint("No reps recorded for this exercise.")
        } else {
            TrendBlock(
                points = repsPoints,
                lineColor = MaterialTheme.colorScheme.tertiary,
                fillColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
            )
        }
    }
}

@Composable
private fun TrendBlock(
    points: List<DatedValue>,
    lineColor: Color,
    fillColor: Color
) {
    val values = points.map { it.value }
    val latest = values.lastOrNull() ?: 0f
    val minV = values.minOrNull() ?: 0f
    val maxV = values.maxOrNull() ?: 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "latest ${formatValue(latest)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = lineColor
        )
        Text(
            text = "min ${formatValue(minV)} · max ${formatValue(maxV)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    MiniLineChart(
        values = values,
        lineColor = lineColor,
        fillColor = fillColor,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = formatDateShort(points.first().epochDay),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatDateShort(points.last().epochDay),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GraphCard(
    title: String,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}

@Composable
private fun EmptyGraphHint(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatRing(
    fraction: Float,
    valueText: String,
    caption: String,
    ringColor: Color
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val clamped = fraction.coerceIn(0f, 1f)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(104.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 12.dp.toPx()
                val inset = strokeWidth / 2f
                val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                val topLeft = Offset(inset, inset)
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            Text(
                text = valueText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = caption,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MiniLineChart(
    values: List<Float>,
    lineColor: Color,
    fillColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxV = values.max()
        val minV = values.min()
        val range = (maxV - minV).takeIf { it > 0f } ?: 1f
        val n = values.size
        val padY = 10.dp.toPx()
        val usableHeight = size.height - padY * 2f
        val stepX = if (n > 1) size.width / (n - 1) else 0f

        fun pointFor(index: Int): Offset {
            val x = if (n > 1) stepX * index else size.width / 2f
            val norm = (values[index] - minV) / range
            val y = padY + usableHeight * (1f - norm)
            return Offset(x, y)
        }

        val linePath = Path()
        values.indices.forEach { index ->
            val point = pointFor(index)
            if (index == 0) linePath.moveTo(point.x, point.y) else linePath.lineTo(point.x, point.y)
        }

        if (n > 1) {
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(pointFor(n - 1).x, size.height)
                lineTo(pointFor(0).x, size.height)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(listOf(fillColor, Color.Transparent))
            )
        }

        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        values.indices.forEach { index ->
            drawCircle(color = lineColor, radius = 3.5.dp.toPx(), center = pointFor(index))
        }
    }
}

@Composable
private fun MiniBarChart(
    values: List<Float>,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (values.isEmpty()) return@Canvas
        val maxV = values.max().takeIf { it > 0f } ?: 1f
        val n = values.size
        val gap = 8.dp.toPx()
        val barWidth = ((size.width - gap * (n - 1)) / n).coerceAtLeast(1f)
        val radius = CornerRadius(5.dp.toPx(), 5.dp.toPx())

        values.indices.forEach { index ->
            val norm = (values[index] / maxV).coerceIn(0f, 1f)
            val barHeight = size.height * norm
            val left = index * (barWidth + gap)
            val top = size.height - barHeight
            // faint full-height track
            drawRoundRect(
                color = barColor.copy(alpha = 0.12f),
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = radius
            )
            if (barHeight > 0f) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = radius
                )
            }
        }
    }
}
