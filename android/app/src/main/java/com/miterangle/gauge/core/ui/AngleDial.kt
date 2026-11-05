package com.miterangle.gauge.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.R
import com.miterangle.gauge.core.config.GameConfig
import com.miterangle.gauge.core.theme.HudTheme
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

@Composable
fun AngleDial(
    angleDeg: Float,
    sides: Int,
    ring: Boolean,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    backdrop: Boolean = true,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val colors = HudTheme.colors
    val sweep by animateFloatAsState(
        targetValue = angleDeg,
        animationSpec = spring(
            dampingRatio = GameConfig.DIAL_SPRING_DAMPING,
            stiffness = GameConfig.DIAL_SPRING_STIFFNESS
        ),
        label = "dialSweep"
    )
    val drawnSides = remember { Animatable(sides.toFloat()) }
    LaunchedEffect(sides) {
        drawnSides.snapTo(0f)
        drawnSides.animateTo(
            targetValue = sides.toFloat(),
            animationSpec = tween(
                durationMillis = GameConfig.SIDE_FAN_MS_PER_SIDE * sides,
                easing = FastOutSlowInEasing
            )
        )
    }
    Box(
        modifier = modifier.alpha(if (dimmed) 0.45f else 1f),
        contentAlignment = Alignment.Center
    ) {
        if (backdrop) {
            Image(
                painter = painterResource(R.drawable.sprite_4),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                alpha = 0.07f,
                modifier = Modifier.fillMaxWidth(0.72f)
            )
        }
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = min(size.width, size.height) / 2f
            drawBezel(colors.outline, colors.primary, radius)
            rotate(degrees = -sweep) {
                drawTicks(colors.primary, colors.outline, radius)
            }
            drawPolygon(
                primary = colors.primary,
                accent = colors.accent,
                radius = radius,
                sides = sides,
                drawn = drawnSides.value,
                ring = ring
            )
            drawNeedle(colors.accent, radius)
        }
        Box(contentAlignment = Alignment.Center, content = centerContent)
    }
}

private fun DrawScope.drawBezel(outline: Color, primary: Color, radius: Float) {
    drawCircle(
        color = outline,
        radius = radius - 1.dp.toPx(),
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawCircle(
        color = primary.copy(alpha = 0.45f),
        radius = radius - 10.dp.toPx(),
        style = Stroke(width = 1.dp.toPx())
    )
}

private fun DrawScope.drawTicks(primary: Color, outline: Color, radius: Float) {
    val outer = radius - 12.dp.toPx()
    val majorLength = 16.dp.toPx()
    val minorLength = 8.dp.toPx()
    for (index in 0 until 180) {
        val degrees = index * 2.0
        val major = index % 5 == 0
        val length = if (major) majorLength else minorLength
        val radians = Math.toRadians(degrees - 90.0)
        val cosine = cos(radians).toFloat()
        val sine = sin(radians).toFloat()
        val start = Offset(
            center.x + cosine * outer,
            center.y + sine * outer
        )
        val end = Offset(
            center.x + cosine * (outer - length),
            center.y + sine * (outer - length)
        )
        drawLine(
            color = if (major) primary else outline,
            start = start,
            end = end,
            strokeWidth = if (major) 1.5.dp.toPx() else 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawPolygon(
    primary: Color,
    accent: Color,
    radius: Float,
    sides: Int,
    drawn: Float,
    ring: Boolean
) {
    if (sides < 3) return
    val shapeRadius = radius * 0.42f
    val vertices = (0 until sides).map { index ->
        val radians = Math.toRadians(-90.0 + index * 360.0 / sides)
        Offset(
            center.x + (cos(radians) * shapeRadius).toFloat(),
            center.y + (sin(radians) * shapeRadius).toFloat()
        )
    }
    val complete = floor(drawn).toInt().coerceIn(0, sides)
    val fraction = (drawn - complete).coerceIn(0f, 1f)
    val path = Path()
    if (complete > 0) {
        path.moveTo(vertices[0].x, vertices[0].y)
        for (index in 1..complete) {
            val point = vertices[index % sides]
            path.lineTo(point.x, point.y)
        }
        drawPath(
            path = path,
            color = primary.copy(alpha = 0.7f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
    if (fraction > 0f && complete < sides) {
        val from = vertices[complete % sides]
        val to = vertices[(complete + 1) % sides]
        drawLine(
            color = accent,
            start = from,
            end = Offset(
                from.x + (to.x - from.x) * fraction,
                from.y + (to.y - from.y) * fraction
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
    if (ring) {
        drawCircle(
            color = primary.copy(alpha = 0.35f),
            radius = shapeRadius * 1.35f,
            style = Stroke(
                width = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(6.dp.toPx(), 6.dp.toPx())
                )
            )
        )
    }
}

private fun DrawScope.drawNeedle(accent: Color, radius: Float) {
    val top = center.y - radius + 6.dp.toPx()
    drawLine(
        color = accent,
        start = Offset(center.x, center.y),
        end = Offset(center.x, top + 10.dp.toPx()),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )
    val head = Path().apply {
        moveTo(center.x, top)
        lineTo(center.x - 7.dp.toPx(), top + 13.dp.toPx())
        lineTo(center.x + 7.dp.toPx(), top + 13.dp.toPx())
        close()
    }
    drawPath(path = head, color = accent)
    drawCircle(color = accent, radius = 4.dp.toPx(), center = center)
}
