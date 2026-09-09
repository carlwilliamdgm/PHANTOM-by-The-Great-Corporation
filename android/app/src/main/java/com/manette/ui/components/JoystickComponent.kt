package com.manette.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.manette.ui.theme.GamepadButton
import com.manette.ui.theme.GamepadButtonPressed
import kotlin.math.*

@Composable
fun JoystickComponent(
    modifier: Modifier = Modifier,
    size: Int = 130,
    onMove: (Float, Float) -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val maxRadiusPx = with(density) { (size / 2 - 25).dp.toPx() }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Center stick
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), thumbOffset.y.roundToInt()) }
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    if (thumbOffset != Offset.Zero) GamepadButtonPressed else GamepadButton,
                    CircleShape
                )
                .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { thumbOffset = Offset.Zero },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = thumbOffset + dragAmount
                            val distance = sqrt(newOffset.x * newOffset.x + newOffset.y * newOffset.y)

                            thumbOffset = if (distance > maxRadiusPx) {
                                val angle = atan2(newOffset.y, newOffset.x)
                                Offset(cos(angle) * maxRadiusPx, sin(angle) * maxRadiusPx)
                            } else {
                                newOffset
                            }

                            val normX = (thumbOffset.x / maxRadiusPx).coerceIn(-1f, 1f)
                            // Invert Y so that pushing stick forward gives positive Y
                            val normY = -(thumbOffset.y / maxRadiusPx).coerceIn(-1f, 1f)
                            onMove(normX, normY)
                        },
                        onDragEnd = {
                            thumbOffset = Offset.Zero
                            onMove(0f, 0f)
                        },
                        onDragCancel = {
                            thumbOffset = Offset.Zero
                            onMove(0f, 0f)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
            )
        }
    }
}
