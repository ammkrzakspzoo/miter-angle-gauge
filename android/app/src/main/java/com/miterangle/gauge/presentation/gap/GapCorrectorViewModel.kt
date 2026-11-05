package com.miterangle.gauge.presentation.gap

import androidx.lifecycle.ViewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import com.miterangle.gauge.domain.usecase.GapCorrectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class GapUiState(
    val gapWidth: String = "",
    val jointLength: String = "",
    val gapAtOutside: Boolean = true,
    val unit: MeasureUnit = MeasureUnit.MM,
    val adjustmentText: String = "",
    val directionRes: Int = R.string.gap_decrease,
    val newSettingText: String = "",
    val currentMiterText: String = "",
    val hasSource: Boolean = false,
    val valid: Boolean = false
)

class GapCorrectorViewModel(
    private val resultId: String,
    private val cutRepository: CutRepository,
    private val settingsRepository: SettingsRepository,
    private val gapCorrection: GapCorrectionUseCase,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val source: CutResult? = resultId.takeIf { it.isNotEmpty() }?.let(cutRepository::find)

    private val _state = MutableStateFlow(GapUiState())
    val state: StateFlow<GapUiState> = _state.asStateFlow()

    init {
        val settings = settingsRepository.settings.value
        val jointLengthMm = source?.partLengthMm ?: 120.0
        _state.value = GapUiState(
            gapWidth = format.length(1.2, settings.unit, settings.decimals),
            jointLength = format.length(jointLengthMm, settings.unit, settings.decimals),
            unit = settings.unit,
            hasSource = source != null,
            currentMiterText = source?.let { format.angle(it.miterDeg, settings.decimals) }.orEmpty()
        )
        recompute()
    }

    fun onGapWidthChange(value: String) {
        _state.update { it.copy(gapWidth = value) }
        recompute()
    }

    fun onJointLengthChange(value: String) {
        _state.update { it.copy(jointLength = value) }
        recompute()
    }

    fun onGapSideChange(outside: Boolean) {
        _state.update { it.copy(gapAtOutside = outside) }
        recompute()
    }

    fun onApply() {
        val current = _state.value
        val original = source ?: return
        if (!current.valid) return
        val corrected = gapCorrection(
            gapMm = current.unit.toMillimetres(format.parse(current.gapWidth) ?: return),
            jointLengthMm = current.unit.toMillimetres(format.parse(current.jointLength) ?: return),
            gapAtOutside = current.gapAtOutside,
            currentMiterDeg = original.miterDeg
        ) ?: return
        val newMiter = corrected.correctedMiterDeg ?: return
        cutRepository.storeLast(original.copy(miterDeg = newMiter))
    }

    private fun recompute() {
        val current = _state.value
        val settings = settingsRepository.settings.value
        val gap = format.parse(current.gapWidth)
        val joint = format.parse(current.jointLength)
        if (gap == null || joint == null || joint <= 0.0 || gap < 0.0) {
            _state.update { it.copy(valid = false, adjustmentText = "", newSettingText = "") }
            return
        }
        val correction = gapCorrection(
            gapMm = current.unit.toMillimetres(gap),
            jointLengthMm = current.unit.toMillimetres(joint),
            gapAtOutside = current.gapAtOutside,
            currentMiterDeg = source?.miterDeg
        )
        if (correction == null) {
            _state.update { it.copy(valid = false, adjustmentText = "", newSettingText = "") }
            return
        }
        _state.update {
            it.copy(
                valid = true,
                adjustmentText = format.angle(correction.adjustmentDeg, settings.decimals),
                directionRes = if (correction.decrease) {
                    R.string.gap_decrease
                } else {
                    R.string.gap_increase
                },
                newSettingText = correction.correctedMiterDeg
                    ?.let { angle -> format.angle(angle, settings.decimals) }
                    .orEmpty()
            )
        }
    }
}
