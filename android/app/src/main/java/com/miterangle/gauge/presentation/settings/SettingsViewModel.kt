package com.miterangle.gauge.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.data.sensor.TiltSensorDataSource
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val unit: MeasureUnit = MeasureUnit.MM,
    val decimals: Int = 1,
    val kerfInput: String = "",
    val bright: Boolean = false,
    val haptics: Boolean = true,
    val sensorAvailable: Boolean = false,
    val calibrationText: String = "",
    val calibrating: Boolean = false
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val tiltSensor: TiltSensorDataSource,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update {
                    it.copy(
                        unit = settings.unit,
                        decimals = settings.decimals,
                        kerfInput = if (it.kerfInput.isEmpty()) {
                            format.length(settings.kerfMm, settings.unit, settings.decimals)
                        } else {
                            it.kerfInput
                        },
                        bright = settings.bright,
                        haptics = settings.haptics,
                        sensorAvailable = tiltSensor.available,
                        calibrationText = format.angle(
                            settings.tiltZeroOffset.toDouble(),
                            settings.decimals
                        )
                    )
                }
            }
        }
    }

    fun onUnitChange(unit: MeasureUnit) {
        val currentKerf = settingsRepository.settings.value.kerfMm
        settingsRepository.update { it.copy(unit = unit) }
        _state.update {
            it.copy(kerfInput = format.length(currentKerf, unit, it.decimals))
        }
    }

    fun onDecimalsChange(delta: Int) {
        settingsRepository.update { it.copy(decimals = it.decimals + delta) }
    }

    fun onKerfChange(value: String) {
        _state.update { it.copy(kerfInput = value) }
        val parsed = format.parse(value) ?: return
        val unit = settingsRepository.settings.value.unit
        settingsRepository.update { it.copy(kerfMm = unit.toMillimetres(parsed)) }
    }

    fun onBrightChange(enabled: Boolean) {
        settingsRepository.update { it.copy(bright = enabled) }
    }

    fun onHapticsChange(enabled: Boolean) {
        settingsRepository.update { it.copy(haptics = enabled) }
    }

    fun onCalibrate() {
        if (!tiltSensor.available || _state.value.calibrating) return
        _state.update { it.copy(calibrating = true) }
        viewModelScope.launch {
            val sample = tiltSensor.tiltDegrees().first()
            settingsRepository.update { it.copy(tiltZeroOffset = sample) }
            _state.update { it.copy(calibrating = false) }
        }
    }

    fun onResetCalibration() {
        settingsRepository.update { it.copy(tiltZeroOffset = 0f) }
    }
}
