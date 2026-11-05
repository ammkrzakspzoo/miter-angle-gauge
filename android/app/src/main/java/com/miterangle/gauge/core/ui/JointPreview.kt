package com.miterangle.gauge.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.config.GameConfig
import com.miterangle.gauge.core.theme.HudTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun JointPreview(
    sides: Int,
    modifier: Modifier = Modifier,
    squash: Float = 0.35f
) {
    val colors = HudTheme.colors
    val transition = rememberInfiniteTransition(label = "jointSpin")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = GameConfig.JOINT_SPIN_MS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "jointSpinValue"
    )
    Canvas(modifier = modifier.fillMaxSize()) {
        drawJoint(
            sides = sides.coerceAtLeast(3),
            spin = spin,
            squash = squash,
            primary = colors.primary,
            accent = colors.accent
        )
    }
}

private fun DrawScope.drawJoint(
    sides: Int,
    spin: Float,
    squash: Float,
    primary: Color,
    accent: Color
) {
    val radius = min(size.width, size.height) / 2f * 0.78f
    val points = (0 until sides).map { index ->
        val radians = Math.toRadians(spin + index * 360.0 / sides)
        Offset(
            center.x + (cos(radians) * radius).toFloat(),
            center.y + (sin(radians) * radius * squash).toFloat()
        )
    }
    val path = Path()
    path.moveTo(points[0].x, points[0].y)
    for (index in 1 until sides) {
        path.lineTo(points[index].x, points[index].y)
    }
    path.close()
    drawPath(
        path = path,
        color = primary.copy(alpha = 0.75f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    points.forEach { point ->
        drawLine(
            color = accent,
            start = Offset(point.x, point.y - 6.dp.toPx()),
            end = Offset(point.x, point.y + 6.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    drawCircle(
        color = primary.copy(alpha = 0.18f),
        radius = radius * 0.2f,
        center = center
    )
}
