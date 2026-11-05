package com.miterangle.gauge.presentation.gap

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
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
import com.miterangle.gauge.core.ui.GlassCard
import com.miterangle.gauge.core.ui.HudField
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.core.ui.SecondaryButton
import com.miterangle.gauge.core.ui.SegmentedChips
import com.miterangle.gauge.domain.model.MeasureUnit

@Composable
fun GapCorrectorRoute(
    key: String,
    resultId: String?,
    onBack: () -> Unit,
    onApplied: () -> Unit,
    onMenu: () -> Unit
) {
    val gapViewModel: GapCorrectorViewModel = viewModel(
        key = key,
        factory = AppViewModelFactory.create(resultId = resultId.orEmpty())
    )
    val state by gapViewModel.state.collectAsStateWithLifecycle()

    GapCorrectorScreen(
        state = state,
        onBack = onBack,
        onGapWidthChange = gapViewModel::onGapWidthChange,
        onJointLengthChange = gapViewModel::onJointLengthChange,
        onGapSideChange = gapViewModel::onGapSideChange,
        onApply = {
            gapViewModel.onApply()
            onApplied()
        },
        onMenu = onMenu
    )
}

@Composable
private fun GapCorrectorScreen(
    state: GapUiState,
    onBack: () -> Unit,
    onGapWidthChange: (String) -> Unit,
    onJointLengthChange: (String) -> Unit,
    onGapSideChange: (Boolean) -> Unit,
    onApply: () -> Unit,
    onMenu: () -> Unit
) {
    val colors = HudTheme.colors
    val unitLabel = stringResource(
        if (state.unit == MeasureUnit.MM) R.string.unit_mm else R.string.unit_inch
    )

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.74f),
            colors.background.copy(alpha = 0.90f),
            colors.backgroundDeep.copy(alpha = 0.97f)
        ),
        title = stringResource(R.string.gap_title),
        onBack = onBack,
        grid = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawGapDiagram(
                        primary = colors.primary,
                        accent = colors.accent,
                        outline = colors.outline,
                        outside = state.gapAtOutside
                    )
                }
                Image(
                    painter = painterResource(R.drawable.sprite_5),
                    contentDescription = stringResource(R.string.cd_gauge),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(48.dp)
                )
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = colors.accent.copy(alpha = 0.45f),
                contentPadding = 14.dp
            ) {
                Text(
                    text = stringResource(R.string.gap_adjust),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
                Text(
                    text = stringResource(R.string.fmt_degrees, state.adjustmentText),
                    style = MaterialTheme.typography.displaySmall,
                    color = colors.accent,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                Text(
                    text = stringResource(state.directionRes),
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                if (state.hasSource && state.newSettingText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.fmt_gap_source,
                            stringResource(R.string.gap_new_setting),
                            stringResource(R.string.fmt_degrees, state.newSettingText)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HudField(
                    label = stringResource(R.string.gap_width),
                    value = state.gapWidth,
                    onValueChange = onGapWidthChange,
                    unit = unitLabel,
                    fieldHeight = 52.dp,
                    modifier = Modifier.weight(1f)
                )
                HudField(
                    label = stringResource(R.string.gap_joint_length),
                    value = state.jointLength,
                    onValueChange = onJointLengthChange,
                    unit = unitLabel,
                    fieldHeight = 52.dp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            SegmentedChips(
                options = listOf(
                    stringResource(R.string.gap_at_outside),
                    stringResource(R.string.gap_at_inside)
                ),
                selectedIndex = if (state.gapAtOutside) 0 else 1,
                onSelect = { index -> onGapSideChange(index == 0) },
                selectedStateDescription = stringResource(R.string.state_selected),
                unselectedStateDescription = stringResource(R.string.state_not_selected),
                accent = colors.accent
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(
                    if (state.hasSource) R.string.gap_hint else R.string.gap_no_source
                ),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            PrimaryButton(
                text = stringResource(R.string.cta_apply),
                onClick = onApply,
                enabled = state.hasSource && state.valid,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = stringResource(R.string.cta_menu),
                onClick = onMenu,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun DrawScope.drawGapDiagram(
    primary: Color,
    accent: Color,
    outline: Color,
    outside: Boolean
) {
    val width = size.width
    val height = size.height
    if (width <= 0f || height <= 0f) return
    val centerX = width / 2f
    val centerY = height / 2f
    val arm = (width * 0.33f).coerceAtMost(height * 0.9f)
    val thickness = arm * 0.26f
    val wedge = arm * 0.07f

    val left = Path().apply {
        moveTo(centerX - arm, centerY - thickness / 2f)
        lineTo(centerX - wedge, centerY - thickness / 2f)
        lineTo(centerX, centerY + thickness / 2f)
        lineTo(centerX - arm, centerY + thickness / 2f)
        close()
    }
    val right = Path().apply {
        moveTo(centerX + arm, centerY - thickness / 2f)
        lineTo(centerX + wedge, centerY - thickness / 2f)
        lineTo(centerX + wedge * 0.2f, centerY + thickness / 2f)
        lineTo(centerX + arm, centerY + thickness / 2f)
        close()
    }
    drawPath(left, color = primary.copy(alpha = 0.18f))
    drawPath(left, color = primary, style = Stroke(width = 1.5.dp.toPx()))
    drawPath(right, color = primary.copy(alpha = 0.18f))
    drawPath(right, color = primary, style = Stroke(width = 1.5.dp.toPx()))

    val gapY = if (outside) centerY - thickness / 2f else centerY + thickness / 2f
    drawLine(
        color = accent,
        start = Offset(centerX - wedge, gapY),
        end = Offset(centerX + wedge, gapY),
        strokeWidth = 3.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = accent.copy(alpha = 0.6f),
        start = Offset(centerX, gapY - 18.dp.toPx()),
        end = Offset(centerX, gapY + 18.dp.toPx()),
        strokeWidth = 1.dp.toPx()
    )
    drawLine(
        color = outline,
        start = Offset(centerX - arm, centerY + thickness),
        end = Offset(centerX + arm, centerY + thickness),
        strokeWidth = 1.dp.toPx()
    )
}
