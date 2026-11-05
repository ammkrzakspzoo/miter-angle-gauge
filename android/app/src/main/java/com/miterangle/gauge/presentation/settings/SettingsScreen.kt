package com.miterangle.gauge.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.core.ui.GlassCard
import com.miterangle.gauge.core.ui.HudField
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.core.ui.SecondaryButton
import com.miterangle.gauge.core.ui.SectionLabel
import com.miterangle.gauge.core.ui.SegmentedChips
import com.miterangle.gauge.core.ui.StepperRow
import com.miterangle.gauge.domain.model.MeasureUnit

@Composable
fun SettingsRoute(onBack: () -> Unit) {
    val settingsViewModel: SettingsViewModel = viewModel(
        key = "settings",
        factory = AppViewModelFactory.create()
    )
    val state by settingsViewModel.state.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onBack = onBack,
        onUnitChange = settingsViewModel::onUnitChange,
        onDecimalsChange = settingsViewModel::onDecimalsChange,
        onKerfChange = settingsViewModel::onKerfChange,
        onBrightChange = settingsViewModel::onBrightChange,
        onHapticsChange = settingsViewModel::onHapticsChange,
        onCalibrate = settingsViewModel::onCalibrate,
        onResetCalibration = settingsViewModel::onResetCalibration
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onUnitChange: (MeasureUnit) -> Unit,
    onDecimalsChange: (Int) -> Unit,
    onKerfChange: (String) -> Unit,
    onBrightChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onCalibrate: () -> Unit,
    onResetCalibration: () -> Unit
) {
    val colors = HudTheme.colors
    val unitLabel = stringResource(
        if (state.unit == MeasureUnit.MM) R.string.unit_mm else R.string.unit_inch
    )
    val onText = stringResource(R.string.state_on)
    val offText = stringResource(R.string.state_off)

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.78f),
            colors.background.copy(alpha = 0.92f),
            colors.backgroundDeep.copy(alpha = 0.97f)
        ),
        title = stringResource(R.string.settings_title),
        onBack = onBack,
        grid = true
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), brackets = false) {
                    SectionLabel(text = stringResource(R.string.settings_units))
                    Spacer(modifier = Modifier.height(10.dp))
                    SegmentedChips(
                        options = listOf(
                            stringResource(R.string.unit_mm),
                            stringResource(R.string.unit_inch)
                        ),
                        selectedIndex = if (state.unit == MeasureUnit.MM) 0 else 1,
                        onSelect = { index ->
                            onUnitChange(if (index == 0) MeasureUnit.MM else MeasureUnit.INCH)
                        },
                        selectedStateDescription = stringResource(R.string.state_selected),
                        unselectedStateDescription = stringResource(R.string.state_not_selected)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    StepperRow(
                        label = stringResource(R.string.settings_decimals),
                        value = state.decimals.toString(),
                        minusLabel = stringResource(R.string.step_minus),
                        plusLabel = stringResource(R.string.step_plus),
                        minusDescription = stringResource(R.string.step_minus),
                        plusDescription = stringResource(R.string.step_plus),
                        stateDescriptionText = state.decimals.toString(),
                        canDecrease = state.decimals > 1,
                        canIncrease = state.decimals < 3,
                        onPressStart = onDecimalsChange,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    HudField(
                        label = stringResource(R.string.label_kerf),
                        value = state.kerfInput,
                        onValueChange = onKerfChange,
                        unit = unitLabel,
                        fieldHeight = 52.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), brackets = false) {
                    SwitchRow(
                        label = stringResource(R.string.settings_bright),
                        checked = state.bright,
                        onCheckedChange = onBrightChange,
                        onText = onText,
                        offText = offText
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SwitchRow(
                        label = stringResource(R.string.settings_haptics),
                        checked = state.haptics,
                        onCheckedChange = onHapticsChange,
                        onText = onText,
                        offText = offText
                    )
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), brackets = false) {
                    SectionLabel(text = stringResource(R.string.settings_calibration))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.fmt_degrees, state.calibrationText),
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.accent
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = stringResource(R.string.settings_calibrate),
                            onClick = onCalibrate,
                            enabled = state.sensorAvailable && !state.calibrating,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = stringResource(R.string.settings_reset),
                            onClick = onResetCalibration,
                            accent = colors.accent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                GlassCard(modifier = Modifier.fillMaxWidth(), brackets = false) {
                    Text(
                        text = stringResource(R.string.settings_footer),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onText: String,
    offText: String
) {
    val colors = HudTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics { stateDescription = if (checked) onText else offText },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.backgroundDeep,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceVariant,
                uncheckedBorderColor = colors.outline
            )
        )
    }
}
