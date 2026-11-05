package com.miterangle.gauge.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.theme.HudTheme

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    brackets: Boolean = true,
    borderColor: Color? = null,
    contentPadding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(10.dp)
    val stroke = borderColor ?: colors.outline
    Column(
        modifier = modifier
            .background(colors.surface.copy(alpha = if (colors.bright) 0.96f else 0.90f), shape)
            .border(1.dp, stroke, shape)
            .then(if (brackets) Modifier.drawBehind { drawBrackets(colors.primary) } else Modifier)
            .padding(contentPadding),
        content = content
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBrackets(color: Color) {
    val arm = 18.dp.toPx()
    val inset = 6.dp.toPx()
    val width = 1.5.dp.toPx()
    val tint = color.copy(alpha = 0.7f)
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset
    val segments = listOf(
        Offset(left, top) to Offset(left + arm, top),
        Offset(left, top) to Offset(left, top + arm),
        Offset(right, top) to Offset(right - arm, top),
        Offset(right, top) to Offset(right, top + arm),
        Offset(left, bottom) to Offset(left + arm, bottom),
        Offset(left, bottom) to Offset(left, bottom - arm),
        Offset(right, bottom) to Offset(right - arm, bottom),
        Offset(right, bottom) to Offset(right, bottom - arm)
    )
    segments.forEach { segment ->
        drawLine(
            color = tint,
            start = segment.first,
            end = segment.second,
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}
