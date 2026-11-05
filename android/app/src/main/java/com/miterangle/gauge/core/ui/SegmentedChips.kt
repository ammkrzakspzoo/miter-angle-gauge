package com.miterangle.gauge.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.theme.HudTheme

@Composable
fun SegmentedChips(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    selectedStateDescription: String,
    unselectedStateDescription: String,
    modifier: Modifier = Modifier,
    accent: Color? = null
) {
    val colors = HudTheme.colors
    val tint = accent ?: colors.primary
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEachIndexed { index, option ->
            val active = index == selectedIndex
            val shape = RoundedCornerShape(6.dp)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .background(
                        if (active) tint.copy(alpha = 0.14f) else Color.Transparent,
                        shape
                    )
                    .border(1.dp, if (active) tint else colors.outline, shape)
                    .clickable { onSelect(index) }
                    .semantics {
                        stateDescription =
                            if (active) selectedStateDescription else unselectedStateDescription
                    }
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (active) colors.textPrimary else colors.textSecondary
                )
            }
        }
    }
}
