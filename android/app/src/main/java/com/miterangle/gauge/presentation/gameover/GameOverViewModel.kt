package com.miterangle.gauge.presentation.gameover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameOverUiState(
    val available: Boolean = false,
    val mode: CutMode = CutMode.POLYGON,
    val sides: Int = 0,
    val unit: MeasureUnit = MeasureUnit.MM,
    val miter: String = "",
    val bevel: String = "",
    val jointAngle: String = "",
    val partLength: String = "",
    val innerLength: String = "",
    val parts: Int = 0,
    val totalStock: String = "",
    val kerf: String = "",
    val saved: Boolean = false
)

class GameOverViewModel(
    private val resultId: String,
    private val cutRepository: CutRepository,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameOverUiState())
    val state: StateFlow<GameOverUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                cutRepository.lastResult,
                cutRepository.setups
            ) { last, setups ->
                if (last != null && last.id == resultId) {
                    last
                } else {
                    setups.firstOrNull { it.result.id == resultId }?.result
                }
            }.collect { result ->
                if (result != null) {
                    _state.value = map(result)
                }
            }
        }
    }

    fun onSave(title: String, note: String) {
        val result = cutRepository.find(resultId) ?: return
        cutRepository.save(title, note, result)
        _state.update { it.copy(saved = true) }
    }

    private fun map(result: CutResult): GameOverUiState = GameOverUiState(
        available = true,
        mode = result.mode,
        sides = result.sides,
        unit = result.unit,
        miter = format.angle(result.miterDeg, result.decimals),
        bevel = format.angle(result.bevelDeg, result.decimals),
        jointAngle = format.angle(result.jointAngleDeg, result.decimals),
        partLength = format.length(result.partLengthMm, result.unit, result.decimals),
        innerLength = format.length(result.innerLengthMm, result.unit, result.decimals),
        parts = result.partCount,
        totalStock = format.longLength(result.totalStockMm, result.unit),
        kerf = format.length(result.kerfMm, result.unit, result.decimals),
        saved = cutRepository.isSaved(result.id)
    )
}
