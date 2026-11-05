package com.miterangle.gauge.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.miterangle.gauge.core.theme.HudTheme

@Composable
fun StepperRow(
    label: String,
    value: String,
    minusLabel: String,
    plusLabel: String,
    minusDescription: String,
    plusDescription: String,
    stateDescriptionText: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onPressStart: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onPressStop: () -> Unit = {}
) {
    val colors = HudTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { stateDescription = stateDescriptionText },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepperButton(
                text = minusLabel,
                description = minusDescription,
                enabled = canDecrease,
                onPressStart = { onPressStart(-1) },
                onPressStop = onPressStop
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            StepperButton(
                text = plusLabel,
                description = plusDescription,
                enabled = canIncrease,
                onPressStart = { onPressStart(1) },
                onPressStop = onPressStop
            )
        }
    }
}

@Composable
private fun StepperButton(
    text: String,
    description: String,
    enabled: Boolean,
    onPressStart: () -> Unit,
    onPressStop: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(52.dp)
            .background(
                if (enabled) colors.surfaceVariant.copy(alpha = 0.6f) else Color.Transparent,
                shape
            )
            .border(1.dp, if (enabled) colors.outline else colors.outline.copy(alpha = 0.4f), shape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressStop()
                    }
                )
            }
            .semantics {
                role = Role.Button
                contentDescription = description
            }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = if (enabled) colors.primary else colors.textMuted
        )
    }
}
