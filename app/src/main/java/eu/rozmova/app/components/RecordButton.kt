package eu.rozmova.app.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun RecordButton(
    onRecord: () -> Unit,
    onStop: () -> Unit,
    isRecording: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1.7f,
        targetValue = 1.9f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(850),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "",
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1.9f,
        targetValue = 2.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(800), // Different duration
                repeatMode = RepeatMode.Reverse,
            ),
        label = "",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (isRecording) {
            // First animation
            Box(
                Modifier
                    .size(48.dp)
                    .scale(scale1)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        CircleShape,
                    ),
            )
            // Second animation
            Box(
                Modifier
                    .size(48.dp)
                    .scale(scale2)
                    .background(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        CircleShape,
                    ),
            )
        }

        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    onStop()
                } else {
                    onRecord()
                }
            },
            containerColor =
                if (isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
            )
        }
    }
}
