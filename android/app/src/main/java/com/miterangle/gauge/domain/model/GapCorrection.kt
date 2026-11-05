package com.miterangle.gauge.domain.model

data class GapCorrection(
    val adjustmentDeg: Double,
    val decrease: Boolean,
    val correctedMiterDeg: Double?
)
