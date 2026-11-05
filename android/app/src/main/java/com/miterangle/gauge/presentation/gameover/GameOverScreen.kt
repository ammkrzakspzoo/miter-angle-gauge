package com.miterangle.gauge.presentation.gameover

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
import com.miterangle.gauge.core.ui.JointPreview
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.core.ui.SecondaryButton
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.MeasureUnit

@Composable
fun GameOverRoute(
    key: String,
    resultId: String,
    onBack: () -> Unit,
    onComputeAgain: () -> Unit,
    onGapCheck: () -> Unit,
    onMenu: () -> Unit
) {
    val gameOverViewModel: GameOverViewModel = viewModel(
        key = key,
        factory = AppViewModelFactory.create(resultId = resultId)
    )
    val state by gameOverViewModel.state.collectAsStateWithLifecycle()
    val modeLabel = stringResource(
        when (state.mode) {
            CutMode.POLYGON -> R.string.mode_polygon
            CutMode.SEGMENT_RING -> R.string.mode_ring
            CutMode.CORNER -> R.string.mode_corner
        }
    )
    val savedTitle = stringResource(
        R.string.fmt_saved_title,
        modeLabel,
        stringResource(R.string.fmt_degrees, state.miter)
    )
    val savedNote = stringResource(R.string.fmt_sided, state.sides)

    GameOverScreen(
        state = state,
        modeLabel = modeLabel,
        onBack = onBack,
        onSave = { gameOverViewModel.onSave(savedTitle, savedNote) },
        onComputeAgain = onComputeAgain,
        onGapCheck = onGapCheck,
        onMenu = onMenu
    )
}

@Composable
private fun GameOverScreen(
    state: GameOverUiState,
    modeLabel: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onComputeAgain: () -> Unit,
    onGapCheck: () -> Unit,
    onMenu: () -> Unit
) {
    val colors = HudTheme.colors
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        revealed = true
    }
    val heroScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 480f),
        label = "heroScale"
    )
    val unitLabel = stringResource(
        if (state.unit == MeasureUnit.MM) R.string.unit_mm else R.string.unit_inch
    )
    val longUnitLabel = stringResource(
        if (state.unit == MeasureUnit.MM) R.string.unit_m else R.string.unit_ft
    )

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.80f),
            colors.surface.copy(alpha = 0.94f),
            colors.backgroundDeep.copy(alpha = 0.98f)
        ),
        title = stringResource(R.string.cut_card_title),
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    colors.accent.copy(alpha = 0.22f),
                                    colors.accent.copy(alpha = 0f)
                                )
                            )
                        )
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.fmt_degrees, state.miter),
                        style = MaterialTheme.typography.displayLarge,
                        color = colors.accent,
                        modifier = Modifier
                            .scale(heroScale)
                            .alpha(if (revealed) 1f else 0f)
                            .semantics { liveRegion = LiveRegionMode.Polite }
                    )
                    Text(
                        text = stringResource(R.string.cut_card_caption),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = colors.primary.copy(alpha = 0.4f),
                contentPadding = 14.dp
            ) {
                SheetRow(
                    label = stringResource(R.string.label_shape),
                    value = if (state.mode == CutMode.CORNER) {
                        modeLabel
                    } else {
                        stringResource(R.string.fmt_sided, state.sides)
                    }
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_miter),
                    value = stringResource(R.string.fmt_degrees, state.miter),
                    highlight = true
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_bevel),
                    value = stringResource(R.string.fmt_degrees, state.bevel)
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_part_length),
                    value = stringResource(R.string.fmt_value_unit, state.partLength, unitLabel),
                    highlight = true
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_parts),
                    value = stringResource(R.string.fmt_count, state.parts)
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_total_stock),
                    value = stringResource(R.string.fmt_value_unit, state.totalStock, longUnitLabel)
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_kerf),
                    value = stringResource(R.string.fmt_value_unit, state.kerf, unitLabel)
                )
                SheetDivider()
                SheetRow(
                    label = stringResource(R.string.label_joint_angle),
                    value = stringResource(R.string.fmt_degrees, state.jointAngle)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                JointPreview(
                    sides = if (state.mode == CutMode.CORNER) 4 else state.sides,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(
                        when (state.mode) {
                            CutMode.POLYGON -> R.drawable.sprite_1
                            CutMode.SEGMENT_RING -> R.drawable.sprite_2
                            CutMode.CORNER -> R.drawable.sprite_3
                        }
                    ),
                    contentDescription = modeLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(44.dp)
                )
            }

            Text(
                text = stringResource(R.string.cut_card_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            PrimaryButton(
                text = stringResource(R.string.cta_again),
                onClick = onComputeAgain,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SecondaryButton(
                    text = stringResource(
                        if (state.saved) R.string.cta_saved else R.string.cta_save
                    ),
                    onClick = onSave,
                    enabled = state.available && !state.saved,
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = stringResource(R.string.cta_gap_check),
                    onClick = onGapCheck,
                    accent = colors.accent,
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(
                onClick = onMenu,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = stringResource(R.string.cta_menu),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun SheetRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val colors = HudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary,
            maxLines = 1
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (highlight) colors.accent else colors.textPrimary,
            maxLines = 1
        )
    }
}

@Composable
private fun SheetDivider() {
    val colors = HudTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.surfaceVariant)
    )
}
