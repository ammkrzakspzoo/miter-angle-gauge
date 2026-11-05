package com.miterangle.gauge.domain.model

data class CutResult(
    val id: String,
    val mode: CutMode,
    val sides: Int,
    val dimensionMm: Double,
    val dimensionIsInside: Boolean,
    val stockWidthMm: Double,
    val stockThicknessMm: Double,
    val cornerAngleDeg: Double,
    val springAngleDeg: Double,
    val crown: Boolean,
    val miterDeg: Double,
    val bevelDeg: Double,
    val jointAngleDeg: Double,
    val partLengthMm: Double,
    val innerLengthMm: Double,
    val partCount: Int,
    val totalStockMm: Double,
    val kerfMm: Double,
    val unit: MeasureUnit,
    val decimals: Int,
    val createdAt: Long
)
