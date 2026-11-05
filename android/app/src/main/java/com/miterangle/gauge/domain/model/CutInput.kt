package com.miterangle.gauge.domain.model

data class CutInput(
    val mode: CutMode,
    val sides: Int,
    val dimensionMm: Double,
    val dimensionIsInside: Boolean = true,
    val stockWidthMm: Double,
    val stockThicknessMm: Double,
    val cornerAngleDeg: Double = 90.0,
    val springAngleDeg: Double = 38.0,
    val crown: Boolean = false,
    val kerfMm: Double = 3.0,
    val unit: MeasureUnit = MeasureUnit.MM,
    val decimals: Int = 1
)
