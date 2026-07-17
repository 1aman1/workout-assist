package com.example.workoutassist.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    statusFeedback: SettingsFeedback?,
    onDismissStatusFeedback: () -> Unit,
    backgroundThemeOptionId: String,
    statusThemeOptionId: String,
    doneThemeOptionId: String,
    onBackgroundThemeOptionChanged: (String) -> Unit,
    onStatusThemeOptionChanged: (String) -> Unit,
    onDoneThemeOptionChanged: (String) -> Unit,
    backgroundThemeOptions: List<ThemeColorOption>,
    statusThemeOptions: List<ThemeColorOption>,
    doneThemeOptions: List<ThemeColorOption>,
    backgroundCustomColor: Color,
    statusCustomColor: Color,
    doneCustomColor: Color,
    onBackgroundCustomColorChanged: (Color) -> Unit,
    onStatusCustomColorChanged: (Color) -> Unit,
    onDoneCustomColorChanged: (Color) -> Unit,
    planTitle: String,
    schedulePageLabel: String,
    infinityPageLabel: String,
    workoutTabLabel: String,
    insightsTabLabel: String,
    settingsTabLabel: String,
    onLabelsSaved: (String, String, String, String, String, String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onOpenGraphs: () -> Unit
) {
    val context = LocalContext.current
    val appVersion = remember(context) { currentAppVersionName(context) }
    var showVersionDetails by remember { mutableStateOf(false) }
    var settingsView by remember { mutableStateOf(SettingsView.ROOT) }
    var selectedThemeRole by remember { mutableStateOf(ThemeRole.BACKGROUND) }
    val settingsScrollState = rememberScrollState()
    var planTitleInput by remember(planTitle) { mutableStateOf(planTitle) }
    var scheduleLabelInput by remember(schedulePageLabel) { mutableStateOf(schedulePageLabel) }
    var infinityLabelInput by remember(infinityPageLabel) { mutableStateOf(infinityPageLabel) }
    var workoutTabLabelInput by remember(workoutTabLabel) { mutableStateOf(workoutTabLabel) }
    var insightsTabLabelInput by remember(insightsTabLabel) { mutableStateOf(insightsTabLabel) }
    var settingsTabLabelInput by remember(settingsTabLabel) { mutableStateOf(settingsTabLabel) }

    BackHandler(enabled = settingsView != SettingsView.ROOT) {
        settingsView = SettingsView.ROOT
    }

    val settingsGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.22f)
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
                title = {
                    Text(
                        text = when (settingsView) {
                            SettingsView.ROOT -> "Settings"
                            SettingsView.THEME_OPTIONS -> "Theme"
                            SettingsView.LABEL_OPTIONS -> "Labels"
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    if (settingsView != SettingsView.ROOT) {
                        IconButton(onClick = { settingsView = SettingsView.ROOT }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(settingsGradient)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(settingsScrollState)
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 44.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (settingsView == SettingsView.ROOT) {
                    SettingsSectionHeader("Appearance")
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Labels",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Rename the workout view toggle and bottom tabs.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedButton(
                                onClick = { settingsView = SettingsView.LABEL_OPTIONS },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Options")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    Card(
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
                                text = "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Configure app colors by role.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(
                                onClick = { settingsView = SettingsView.THEME_OPTIONS },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Options")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                    contentDescription = null
                                )
                            }
                        }
                    }

                    SettingsSectionHeader("Data")
                    Card(
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
                                text = "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Export saves your full local state to a JSON file. Import restores that state.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onExportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Export to file")
                            }
                            OutlinedButton(
                                onClick = onImportBackup,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Import from file")
                            }
                        }
                    }

                    SettingsSectionHeader("Advanced")
                    Card(
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
                                text = "Page Command Names",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Use these stable names for quick commands.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            PAGE_COMMAND_NAMES.forEach { page ->
                                Text(
                                    text = "${page.command} -> ${page.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (statusFeedback != null) {
                        SettingsFeedbackCard(
                            feedback = statusFeedback,
                            onDismiss = onDismissStatusFeedback
                        )
                    }

                    SettingsSectionHeader("Analytics")
                    Card(
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
                                text = "Progress Graphs (Beta)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Visualize your Insights data as charts — consistency, weekly frequency, and per-exercise trends.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onOpenGraphs,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Open Progress Graphs")
                            }
                        }
                    }
                } else {
                    when (settingsView) {
                        SettingsView.THEME_OPTIONS -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Theme Options",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Pick a role, then tap a color. The last swatch opens a custom color picker.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    ThemeRoleSelector(
                                        selectedRole = selectedThemeRole,
                                        onRoleSelected = { selectedThemeRole = it }
                                    )

                                    when (selectedThemeRole) {
                                        ThemeRole.BACKGROUND -> ThemeSwatchPicker(
                                            options = backgroundThemeOptions,
                                            selectedOptionId = backgroundThemeOptionId,
                                            customColor = backgroundCustomColor,
                                            onOptionSelected = onBackgroundThemeOptionChanged,
                                            onCustomColorChanged = onBackgroundCustomColorChanged
                                        )

                                        ThemeRole.STATUS -> ThemeSwatchPicker(
                                            options = statusThemeOptions,
                                            selectedOptionId = statusThemeOptionId,
                                            customColor = statusCustomColor,
                                            onOptionSelected = onStatusThemeOptionChanged,
                                            onCustomColorChanged = onStatusCustomColorChanged
                                        )

                                        ThemeRole.DONE -> ThemeSwatchPicker(
                                            options = doneThemeOptions,
                                            selectedOptionId = doneThemeOptionId,
                                            customColor = doneCustomColor,
                                            onOptionSelected = onDoneThemeOptionChanged,
                                            onCustomColorChanged = onDoneCustomColorChanged
                                        )
                                    }
                                }
                            }
                        }

                        SettingsView.LABEL_OPTIONS -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Label Options",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Rename the workout view toggle and bottom tabs.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    OutlinedTextField(
                                        value = planTitleInput,
                                        onValueChange = { planTitleInput = it },
                                        label = { Text("Plan title") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = scheduleLabelInput,
                                        onValueChange = { scheduleLabelInput = it },
                                        label = { Text("Compact view button") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = infinityLabelInput,
                                        onValueChange = { infinityLabelInput = it },
                                        label = { Text("Calendar view button") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = workoutTabLabelInput,
                                        onValueChange = { workoutTabLabelInput = it },
                                        label = { Text("Workout tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = insightsTabLabelInput,
                                        onValueChange = { insightsTabLabelInput = it },
                                        label = { Text("Insights tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    OutlinedTextField(
                                        value = settingsTabLabelInput,
                                        onValueChange = { settingsTabLabelInput = it },
                                        label = { Text("Settings tab") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            onLabelsSaved(
                                                planTitleInput,
                                                scheduleLabelInput,
                                                infinityLabelInput,
                                                workoutTabLabelInput,
                                                insightsTabLabelInput,
                                                settingsTabLabelInput
                                            )
                                        },
                                        enabled = planTitleInput.isNotBlank() &&
                                            scheduleLabelInput.isNotBlank() &&
                                            infinityLabelInput.isNotBlank() &&
                                            workoutTabLabelInput.isNotBlank() &&
                                            insightsTabLabelInput.isNotBlank() &&
                                            settingsTabLabelInput.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Save labels")
                                    }
                                }
                            }
                        }

                        SettingsView.ROOT -> Unit
                    }
                }
            }

            TextButton(
                onClick = { showVersionDetails = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 8.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "v$appVersion",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showVersionDetails) {
        AlertDialog(
            onDismissRequest = { showVersionDetails = false },
            title = { Text("Version v$appVersion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Latest design version: v$LATEST_DESIGN_VERSION",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LATEST_VERSION_HIGHLIGHTS.forEach { line ->
                        Text(
                            text = "- $line",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersionDetails = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private enum class ThemeRole(val title: String) {
    BACKGROUND("Background"),
    STATUS("Status (Exercise cards)"),
    DONE("Done / Actions")
}

@Composable
private fun ThemeRoleSelector(
    selectedRole: ThemeRole,
    onRoleSelected: (ThemeRole) -> Unit
) {
    val rowScrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rowScrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ThemeRole.entries.forEach { role ->
            FilterChip(
                selected = role == selectedRole,
                onClick = { onRoleSelected(role) },
                label = { Text(role.title) }
            )
        }
    }
}

@Composable
private fun ThemeSwatchPicker(
    options: List<ThemeColorOption>,
    selectedOptionId: String,
    customColor: Color,
    onOptionSelected: (String) -> Unit,
    onCustomColorChanged: (Color) -> Unit
) {
    var showCustomDialog by remember { mutableStateOf(false) }
    val rowScrollState = rememberScrollState()
    val presets = options.filter { it.id != CUSTOM_THEME_OPTION_ID }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rowScrollState),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        presets.forEach { option ->
            ThemeSwatch(
                color = option.color,
                selected = option.id == selectedOptionId,
                onClick = { onOptionSelected(option.id) }
            )
        }
        ThemeSwatch(
            color = customColor,
            selected = selectedOptionId == CUSTOM_THEME_OPTION_ID,
            isCustom = true,
            onClick = {
                onOptionSelected(CUSTOM_THEME_OPTION_ID)
                showCustomDialog = true
            }
        )
    }

    if (showCustomDialog) {
        ThemeCustomColorDialog(
            color = customColor,
            onColorChanged = onCustomColorChanged,
            onDismiss = { showCustomDialog = false }
        )
    }
}

@Composable
private fun ThemeSwatch(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    isCustom: Boolean = false
) {
    val contrast = if (color.luminance() > 0.5f) Color.Black else Color.White
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                },
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isCustom -> Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Custom color",
                tint = contrast
            )

            selected -> Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = "Selected",
                tint = contrast
            )
        }
    }
}

@Composable
private fun ThemeCustomColorDialog(
    color: Color,
    onColorChanged: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color, CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = "Hex ${colorToHexRgb(color)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GradientColorPicker(
                    color = color,
                    onColorChanged = onColorChanged
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun GradientColorPicker(
    color: Color,
    onColorChanged: (Color) -> Unit
) {
    val initialHsv = remember {
        FloatArray(3).also { android.graphics.Color.colorToHSV(color.toArgb(), it) }
    }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var value by remember { mutableFloatStateOf(initialHsv[2]) }

    fun emit() {
        onColorChanged(
            Color.hsv(
                hue = hue.coerceIn(0f, 360f),
                saturation = saturation.coerceIn(0f, 1f),
                value = value.coerceIn(0f, 1f)
            )
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Saturation (x) / value (y) gradient box.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        saturation = (offset.x / size.width).coerceIn(0f, 1f)
                        value = (1f - offset.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        saturation = (change.position.x / size.width).coerceIn(0f, 1f)
                        value = (1f - change.position.y / size.height).coerceIn(0f, 1f)
                        emit()
                    }
                }
                .drawBehind {
                    val hueColor = Color.hsv(hue.coerceIn(0f, 360f), 1f, 1f)
                    drawRect(Brush.horizontalGradient(listOf(Color.White, hueColor)))
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
                    val cx = saturation.coerceIn(0f, 1f) * size.width
                    val cy = (1f - value.coerceIn(0f, 1f)) * size.height
                    drawCircle(
                        color = Color.Black,
                        radius = 8.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )

        // Hue strip.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp)
                .clip(RoundedCornerShape(11.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        hue = ((offset.x / size.width) * 360f).coerceIn(0f, 360f)
                        emit()
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        hue = ((change.position.x / size.width) * 360f).coerceIn(0f, 360f)
                        emit()
                    }
                }
                .drawBehind {
                    val hueColors = listOf(0f, 60f, 120f, 180f, 240f, 300f, 360f)
                        .map { Color.hsv(it, 1f, 1f) }
                    drawRect(Brush.horizontalGradient(hueColors))
                    val x = (hue.coerceIn(0f, 360f) / 360f) * size.width
                    drawLine(
                        color = Color.White,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = 3.dp.toPx()
                    )
                }
        )
    }
}

@Composable
private fun SettingsFeedbackCard(
    feedback: SettingsFeedback,
    onDismiss: () -> Unit
) {
    val accentColor = when (feedback.kind) {
        SettingsFeedbackKind.SUCCESS -> MaterialTheme.colorScheme.primary
        SettingsFeedbackKind.FAILURE -> MaterialTheme.colorScheme.error
    }
    val containerColor = when (feedback.kind) {
        SettingsFeedbackKind.SUCCESS -> mixWithWhite(accentColor, 0.88f)
        SettingsFeedbackKind.FAILURE -> mixWithWhite(accentColor, 0.9f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.38f)),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (feedback.kind == SettingsFeedbackKind.SUCCESS) {
                        Icons.Rounded.CheckCircle
                    } else {
                        Icons.Rounded.Warning
                    },
                    contentDescription = null,
                    tint = accentColor
                )
                Text(
                    text = feedback.title,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }

            Text(
                text = feedback.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
