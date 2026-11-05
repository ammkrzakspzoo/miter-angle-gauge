package com.miterangle.gauge.presentation.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.core.ui.AngleDial
import com.miterangle.gauge.core.ui.HudField
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.core.ui.SegmentedChips
import com.miterangle.gauge.core.ui.StatItem
import com.miterangle.gauge.core.ui.StatRow
import com.miterangle.gauge.core.ui.StepperRow
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.MeasureUnit

@Composable
fun GameRoute(
    key: String,
    mode: CutMode,
    setupId: String?,
    cornerAngle: Double?,
    onBack: () -> Unit,
    onResult: (String) -> Unit
) {
    val gameViewModel: GameViewModel = viewModel(
        key = key,
        factory = AppViewModelFactory.create(
            gameArgs = GameArgs(mode = mode, setupId = setupId, cornerAngle = cornerAngle)
        )
    )
    val state by gameViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        gameViewModel.eventFlow.collect { event ->
            when (event) {
                is GameEvent.OpenResult -> onResult(event.resultId)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { gameViewModel.pause() }
    }

    GameScreen(
        state = state,
        onBack = onBack,
        onStepStart = gameViewModel::onStepStart,
        onStepStop = gameViewModel::onStepStop,
        onDimensionChange = gameViewModel::onDimensionChange,
        onStockWidthChange = gameViewModel::onStockWidthChange,
        onStockThicknessChange = gameViewModel::onStockThicknessChange,
        onCornerAngleChange = gameViewModel::onCornerAngleChange,
        onSpringAngleChange = gameViewModel::onSpringAngleChange,
        onInsideOutsideChange = gameViewModel::onInsideOutsideChange,
        onCrownChange = gameViewModel::onCrownChange,
        onCompute = gameViewModel::onCompute
    )
}

@Composable
private fun GameScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onStepStart: (Int) -> Unit,
    onStepStop: () -> Unit,
    onDimensionChange: (String) -> Unit,
    onStockWidthChange: (String) -> Unit,
    onStockThicknessChange: (String) -> Unit,
    onCornerAngleChange: (String) -> Unit,
    onSpringAngleChange: (String) -> Unit,
    onInsideOutsideChange: (Boolean) -> Unit,
    onCrownChange: (Boolean) -> Unit,
    onCompute: () -> Unit
) {
    val colors = HudTheme.colors
    val unitLabel = stringResource(
        if (state.unit == MeasureUnit.MM) R.string.unit_mm else R.string.unit_inch
    )
    val title = stringResource(
        when (state.mode) {
            CutMode.POLYGON -> R.string.mode_polygon
            CutMode.SEGMENT_RING -> R.string.mode_ring
            CutMode.CORNER -> R.string.mode_corner
        }
    )

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.78f),
            colors.background.copy(alpha = 0.92f),
            colors.backgroundDeep.copy(alpha = 0.97f)
        ),
        title = title,
        onBack = onBack,
        trailing = {
            Text(
                text = if (state.valid) {
                    stringResource(R.string.fmt_degrees, state.miterText)
                } else {
                    stringResource(R.string.label_saw)
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.accent,
                maxLines = 1
            )
        }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            val side = min(min(maxWidth, maxHeight), 320.dp)
            AngleDial(
                angleDeg = state.dialAngle,
                sides = state.sides,
                ring = state.isRing,
                dimmed = !state.valid,
                modifier = Modifier.size(side)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.miterText,
                        style = MaterialTheme.typography.displaySmall,
                        color = colors.accent,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                        }
                    )
                    Text(
                        text = stringResource(R.string.label_degrees),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.fmt_value_unit,
                            stringResource(R.string.label_bevel),
                            stringResource(R.string.fmt_degrees, state.bevelText)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.primary
                    )
                }
            }
        }

        StatRow(
            items = listOf(
                StatItem(
                    label = stringResource(R.string.label_saw),
                    value = state.miterText,
                    accent = colors.accent
                ),
                StatItem(
                    label = stringResource(R.string.label_part),
                    value = state.partLengthText,
                    unit = unitLabel,
                    accent = colors.primary
                ),
                StatItem(
                    label = stringResource(R.string.label_stock),
                    value = state.totalStockText,
                    unit = stringResource(
                        if (state.unit == MeasureUnit.MM) R.string.unit_m else R.string.unit_ft
                    ),
                    accent = colors.textPrimary
                )
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        ControlsPanel(
            state = state,
            unitLabel = unitLabel,
            onStepStart = onStepStart,
            onStepStop = onStepStop,
            onDimensionChange = onDimensionChange,
            onStockWidthChange = onStockWidthChange,
            onStockThicknessChange = onStockThicknessChange,
            onCornerAngleChange = onCornerAngleChange,
            onSpringAngleChange = onSpringAngleChange,
            onInsideOutsideChange = onInsideOutsideChange,
            onCrownChange = onCrownChange,
            onCompute = onCompute
        )
    }
}

@Composable
private fun ControlsPanel(
    state: GameUiState,
    unitLabel: String,
    onStepStart: (Int) -> Unit,
    onStepStop: () -> Unit,
    onDimensionChange: (String) -> Unit,
    onStockWidthChange: (String) -> Unit,
    onStockThicknessChange: (String) -> Unit,
    onCornerAngleChange: (String) -> Unit,
    onSpringAngleChange: (String) -> Unit,
    onInsideOutsideChange: (Boolean) -> Unit,
    onCrownChange: (Boolean) -> Unit,
    onCompute: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface.copy(alpha = 0.94f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(colors.outline)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            if (state.isCorner) {
                HudField(
                    label = stringResource(R.string.label_corner_angle),
                    value = state.cornerAngle,
                    onValueChange = onCornerAngleChange,
                    unit = stringResource(R.string.label_degrees),
                    modifier = Modifier.weight(1f)
                )
                HudField(
                    label = stringResource(R.string.label_spring_angle),
                    value = state.springAngle,
                    onValueChange = onSpringAngleChange,
                    unit = stringResource(R.string.label_degrees),
                    modifier = Modifier.weight(1f)
                )
            } else {
                StepperRow(
                    label = stringResource(
                        if (state.isRing) R.string.label_segments else R.string.label_sides
                    ),
                    value = state.sides.toString(),
                    minusLabel = stringResource(R.string.step_minus),
                    plusLabel = stringResource(R.string.step_plus),
                    minusDescription = stringResource(R.string.step_minus),
                    plusDescription = stringResource(R.string.step_plus),
                    stateDescriptionText = state.sides.toString(),
                    canDecrease = state.canDecrease,
                    canIncrease = state.canIncrease,
                    onPressStart = onStepStart,
                    onPressStop = onStepStop,
                    modifier = Modifier.weight(1f)
                )
                HudField(
                    label = stringResource(
                        when {
                            state.isRing -> R.string.label_ring_diameter
                            state.dimensionIsInside -> R.string.label_inside_width
                            else -> R.string.label_outside_width
                        }
                    ),
                    value = state.dimension,
                    onValueChange = onDimensionChange,
                    unit = unitLabel,
                    error = state.dimensionErrorRes?.let { stringResource(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (state.isCorner) {
            HudField(
                label = stringResource(R.string.label_inside_width),
                value = state.dimension,
                onValueChange = onDimensionChange,
                unit = unitLabel,
                error = state.dimensionErrorRes?.let { stringResource(it) },
                modifier = Modifier.fillMaxWidth()
            )
            SegmentedChips(
                options = listOf(
                    stringResource(R.string.label_miter),
                    stringResource(R.string.label_crown)
                ),
                selectedIndex = if (state.crown) 1 else 0,
                onSelect = { index -> onCrownChange(index == 1) },
                selectedStateDescription = stringResource(R.string.state_selected),
                unselectedStateDescription = stringResource(R.string.state_not_selected),
                accent = colors.accent
            )
        } else if (!state.isRing) {
            SegmentedChips(
                options = listOf(
                    stringResource(R.string.label_inside),
                    stringResource(R.string.label_outside)
                ),
                selectedIndex = if (state.dimensionIsInside) 0 else 1,
                onSelect = { index -> onInsideOutsideChange(index == 0) },
                selectedStateDescription = stringResource(R.string.state_selected),
                unselectedStateDescription = stringResource(R.string.state_not_selected)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HudField(
                label = stringResource(R.string.label_stock_width),
                value = state.stockWidth,
                onValueChange = onStockWidthChange,
                unit = unitLabel,
                fieldHeight = 48.dp,
                modifier = Modifier.weight(1f)
            )
            HudField(
                label = stringResource(R.string.label_stock_thickness),
                value = state.stockThickness,
                onValueChange = onStockThicknessChange,
                unit = unitLabel,
                fieldHeight = 48.dp,
                modifier = Modifier.weight(1f)
            )
        }

        state.generalErrorRes?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.danger,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }

        PrimaryButton(
            text = stringResource(R.string.cta_compute),
            onClick = onCompute,
            enabled = state.valid,
            busy = state.computing,
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_build),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
