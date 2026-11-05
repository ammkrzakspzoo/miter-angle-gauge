package com.miterangle.gauge.domain.model

data class GaugeSettings(
    val unit: MeasureUnit = MeasureUnit.MM,
    val decimals: Int = 1,
    val kerfMm: Double = 3.0,
    val bright: Boolean = false,
    val haptics: Boolean = true,
    val tiltZeroOffset: Float = 0f
)
