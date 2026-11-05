package com.miterangle.gauge.presentation.corner

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.core.ui.BubbleLevel
import com.miterangle.gauge.core.ui.GlassCard
import com.miterangle.gauge.core.ui.HudField
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.core.ui.SecondaryButton
import com.miterangle.gauge.core.ui.SectionLabel
import com.miterangle.gauge.core.ui.StatItem
import com.miterangle.gauge.core.ui.StatRow

@Composable
fun CornerMeasureRoute(
    onBack: () -> Unit,
    onUseAngle: (Double) -> Unit
) {
    val cornerViewModel: CornerMeasureViewModel = viewModel(
        key = "corner",
        factory = AppViewModelFactory.create()
    )
    val state by cornerViewModel.state.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        cornerViewModel.eventFlow.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
    DisposableEffect(Unit) {
        onDispose { cornerViewModel.pause() }
    }

    CornerMeasureScreen(
        state = state,
        onBack = onBack,
        onTogglePause = cornerViewModel::toggleMeasuring,
        onCapture = cornerViewModel::onCapture,
        onReset = cornerViewModel::onReset,
        onManualInputChange = cornerViewModel::onManualInputChange,
        onUseAngle = { state.cornerAngle?.let(onUseAngle) }
    )
}

@Composable
private fun CornerMeasureScreen(
    state: CornerUiState,
    onBack: () -> Unit,
    onTogglePause: () -> Unit,
    onCapture: () -> Unit,
    onReset: () -> Unit,
    onManualInputChange: (String) -> Unit,
    onUseAngle: () -> Unit
) {
    val colors = HudTheme.colors

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.76f),
            colors.background.copy(alpha = 0.92f),
            colors.backgroundDeep.copy(alpha = 0.97f)
        ),
        title = stringResource(R.string.corner_title),
        onBack = onBack,
        grid = true,
        trailing = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onTogglePause() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.icon_pause),
                    contentDescription = stringResource(R.string.cd_pause),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.manual) {
                Image(
                    painter = painterResource(R.drawable.sprite_3),
                    contentDescription = stringResource(R.string.cd_corner),
                    contentScale = ContentScale.Fit,
                    alpha = 0.35f,
                    modifier = Modifier.size(120.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.corner_no_sensor),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.accent
                )
            } else {
                BubbleLevel(
                    angleDeg = state.liveAngle,
                    active = state.measuring,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.fmt_degrees, state.liveAngleText),
                style = MaterialTheme.typography.displayMedium,
                color = colors.accent,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Text(
                text = stringResource(
                    R.string.fmt_value_unit,
                    stringResource(R.string.corner_out_of_square),
                    stringResource(R.string.fmt_degrees, state.outOfSquareText)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.manual) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    HudField(
                        label = stringResource(R.string.label_corner_angle),
                        value = state.manualInput,
                        onValueChange = onManualInputChange,
                        unit = stringResource(R.string.label_degrees),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                SectionLabel(
                    text = stringResource(
                        when {
                            state.captured -> R.string.corner_ready
                            state.waitingForWallB -> R.string.corner_hold_b
                            else -> R.string.corner_hold_a
                        }
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                StatRow(
                    items = listOf(
                        StatItem(
                            label = stringResource(R.string.corner_wall_a),
                            value = state.wallAText,
                            accent = colors.primary
                        ),
                        StatItem(
                            label = stringResource(R.string.corner_wall_b),
                            value = state.wallBText,
                            accent = colors.accent
                        )
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                PrimaryButton(
                    text = stringResource(
                        if (state.waitingForWallB) R.string.cta_capture_b else R.string.cta_capture_a
                    ),
                    onClick = onCapture,
                    enabled = state.measuring && !state.captured,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.menu_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            PrimaryButton(
                text = stringResource(R.string.cta_use_angle),
                onClick = onUseAngle,
                enabled = state.cornerAngle != null,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = stringResource(R.string.cta_reset),
                    onClick = onReset,
                    enabled = !state.manual,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = stringResource(R.string.cta_menu),
                    onClick = onBack,
                    accent = colors.accent,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
