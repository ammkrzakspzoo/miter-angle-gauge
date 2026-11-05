package com.miterangle.gauge.presentation.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MenuViewModel(
    private val cutRepository: CutRepository,
    private val settingsRepository: SettingsRepository,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MenuUiState())
    val state: StateFlow<MenuUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cutRepository.setups,
                cutRepository.lastResult,
                settingsRepository.settings
            ) { setups, last, settings ->
                MenuUiState(
                    unit = settings.unit,
                    savedCount = setups.size,
                    lastResultId = last?.id,
                    lastAngle = last?.let { format.angle(it.miterDeg, settings.decimals) }.orEmpty(),
                    lastSides = last?.sides ?: 0
                )
            }.collect { _state.value = it }
        }
    }

    fun selectUnit(unit: MeasureUnit) {
        settingsRepository.update { it.copy(unit = unit) }
    }
}
