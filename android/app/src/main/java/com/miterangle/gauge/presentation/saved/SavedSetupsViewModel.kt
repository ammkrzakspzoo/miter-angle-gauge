package com.miterangle.gauge.presentation.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedRow(
    val id: String,
    val title: String,
    val note: String,
    val angleText: String,
    val lengthText: String,
    val sides: Int,
    val mode: CutMode
)

data class SavedSetupsUiState(
    val rows: List<SavedRow> = emptyList(),
    val pendingDeleteId: String? = null,
    val loading: Boolean = true
)

class SavedSetupsViewModel(
    private val cutRepository: CutRepository,
    private val settingsRepository: SettingsRepository,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SavedSetupsUiState())
    val state: StateFlow<SavedSetupsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cutRepository.setups,
                settingsRepository.settings
            ) { setups, settings ->
                setups.map { setup ->
                    SavedRow(
                        id = setup.id,
                        title = setup.title,
                        note = setup.note,
                        angleText = format.angle(setup.result.miterDeg, settings.decimals),
                        lengthText = format.length(
                            setup.result.partLengthMm,
                            settings.unit,
                            settings.decimals
                        ),
                        sides = setup.result.sides,
                        mode = setup.result.mode
                    )
                }
            }.collect { rows ->
                _state.update { it.copy(rows = rows, loading = false) }
            }
        }
    }

    fun onDeleteRequest(id: String) {
        _state.update { it.copy(pendingDeleteId = id) }
    }

    fun onDeleteCancel() {
        _state.update { it.copy(pendingDeleteId = null) }
    }

    fun onDeleteConfirm() {
        val id = _state.value.pendingDeleteId ?: return
        cutRepository.delete(id)
        _state.update { it.copy(pendingDeleteId = null) }
    }
}
