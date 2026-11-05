package com.miterangle.gauge.presentation.menu

import com.miterangle.gauge.domain.model.MeasureUnit

data class MenuUiState(
    val unit: MeasureUnit = MeasureUnit.MM,
    val savedCount: Int = 0,
    val lastResultId: String? = null,
    val lastAngle: String = "",
    val lastSides: Int = 0
)
