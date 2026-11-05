package com.miterangle.gauge.core.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.theme.HudTheme

@Composable
fun BubbleLevel(
    angleDeg: Float,
    modifier: Modifier = Modifier,
    active: Boolean = true
) {
    val colors = HudTheme.colors
    val offsetFraction by animateFloatAsState(
        targetValue = (angleDeg / 30f).coerceIn(-1f, 1f),
        animationSpec = tween(durationMillis = 120),
        label = "bubbleOffset"
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        val inset = 12.dp.toPx()
        val capsuleHeight = 56.dp.toPx()
        val top = (size.height - capsuleHeight) / 2f
        val width = size.width - inset * 2f
        val radius = CornerRadius(capsuleHeight / 2f, capsuleHeight / 2f)
        val tint = if (active) colors.primary else colors.textMuted
        drawRoundRect(
            color = colors.surface.copy(alpha = 0.7f),
            topLeft = Offset(inset, top),
            size = Size(width, capsuleHeight),
            cornerRadius = radius
        )
        drawRoundRect(
            color = colors.outline,
            topLeft = Offset(inset, top),
            size = Size(width, capsuleHeight),
            cornerRadius = radius,
            style = Stroke(width = 1.dp.toPx())
        )
        val centerX = inset + width / 2f
        for (step in -6..6) {
            val x = centerX + step * (width / 14f)
            val tall = step % 3 == 0
            val length = if (tall) 14.dp.toPx() else 8.dp.toPx()
            drawLine(
                color = if (tall) tint.copy(alpha = 0.8f) else colors.outline,
                start = Offset(x, top + capsuleHeight / 2f - length / 2f),
                end = Offset(x, top + capsuleHeight / 2f + length / 2f),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
        drawLine(
            color = colors.accent,
            start = Offset(centerX, top - 6.dp.toPx()),
            end = Offset(centerX, top + capsuleHeight + 6.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        val bubbleX = centerX + offsetFraction * (width / 2f - capsuleHeight / 2f - 4.dp.toPx())
        drawCircle(
            color = tint.copy(alpha = 0.25f),
            radius = capsuleHeight / 2f - 6.dp.toPx(),
            center = Offset(bubbleX, top + capsuleHeight / 2f)
        )
        drawCircle(
            color = tint,
            radius = capsuleHeight / 2f - 6.dp.toPx(),
            center = Offset(bubbleX, top + capsuleHeight / 2f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

