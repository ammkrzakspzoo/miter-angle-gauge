package com.miterangle.gauge.presentation.game

import com.miterangle.gauge.core.config.GaugeConfig
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.MeasureUnit

data class GameArgs(
    val mode: CutMode,
    val setupId: String? = null,
    val cornerAngle: Double? = null
)

data class GameUiState(
    val mode: CutMode = CutMode.POLYGON,
    val sides: Int = GaugeConfig.DEFAULT_SIDES,
    val dimension: String = "",
    val dimensionIsInside: Boolean = true,
    val stockWidth: String = "",
    val stockThickness: String = "",
    val cornerAngle: String = "",
    val springAngle: String = "",
    val crown: Boolean = false,
    val unit: MeasureUnit = MeasureUnit.MM,
    val decimals: Int = 1,
    val dialAngle: Float = 45f,
    val miterText: String = "",
    val bevelText: String = "",
    val jointText: String = "",
    val partLengthText: String = "",
    val totalStockText: String = "",
    val partCount: Int = 0,
    val valid: Boolean = false,
    val computing: Boolean = false,
    val dimensionErrorRes: Int? = null,
    val generalErrorRes: Int? = null
) {
    val canDecrease: Boolean get() = sides > GaugeConfig.MIN_SIDES
    val canIncrease: Boolean get() = sides < GaugeConfig.MAX_SIDES
    val isRing: Boolean get() = mode == CutMode.SEGMENT_RING
    val isCorner: Boolean get() = mode == CutMode.CORNER
}

sealed interface GameEvent {
    data class OpenResult(val resultId: String) : GameEvent
}
