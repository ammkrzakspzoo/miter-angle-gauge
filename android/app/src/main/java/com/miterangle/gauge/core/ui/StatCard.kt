package com.miterangle.gauge.core.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.theme.HudTheme

data class StatItem(
    val label: String,
    val value: String,
    val unit: String? = null,
    val accent: Color? = null
)

@Composable
fun StatCard(
    item: StatItem,
    modifier: Modifier = Modifier
) {
    val colors = HudTheme.colors
    val tint = item.accent ?: colors.primary
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.72f), RoundedCornerShape(8.dp))
            .border(1.dp, colors.outline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(tint, CircleShape)
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = item.value,
                style = MaterialTheme.typography.labelMedium,
                color = tint,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            if (item.unit != null) {
                Text(
                    text = item.unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatRow(
    items: List<StatItem>,
    modifier: Modifier = Modifier
) {
    val visible = items.filter { it.value.isNotBlank() && !isZero(it.value) }
    if (visible.size < 2) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        visible.forEach { item ->
            StatCard(item = item, modifier = Modifier.weight(1f))
        }
    }
}

private fun isZero(value: String): Boolean {
    val numeric = value.trim().toDoubleOrNull() ?: return false
    return numeric == 0.0
}
