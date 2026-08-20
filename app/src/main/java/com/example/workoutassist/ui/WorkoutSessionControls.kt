package com.example.workoutassist.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
internal fun TopBarStopwatch(
    icon: ImageVector,
    iconTint: Color,
    timeText: String,
    flash: Boolean,
    modifier: Modifier = Modifier,
    blinking: Boolean = false
) {
    val background by animateColorAsState(
        targetValue = if (flash) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 250),
        label = "stopwatchFlash"
    )
    val blinkTransition = rememberInfiniteTransition(label = "stopwatchBlink")
    val blinkAlpha by blinkTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "stopwatchBlinkAlpha"
    )
    val contentAlpha = if (blinking) blinkAlpha else 1f
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .alpha(contentAlpha),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = timeText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
internal fun HoldToConfirmButton(
    text: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    holdMillis: Int = 1200
) {
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var holding by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .width(176.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.error)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holding = true
                        val animJob = scope.launch {
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(durationMillis = holdMillis, easing = LinearEasing)
                            )
                            holding = false
                            onConfirm()
                            progress.snapTo(0f)
                        }
                        tryAwaitRelease()
                        if (animJob.isActive) {
                            animJob.cancel()
                            holding = false
                            scope.launch { progress.snapTo(0f) }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.value)
                .background(Color.White.copy(alpha = 0.28f))
        )
        Text(
            text = if (holding) "Keep holding…" else text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onError,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
internal fun WorkoutSessionStartMessageCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.64f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Dismiss message",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
