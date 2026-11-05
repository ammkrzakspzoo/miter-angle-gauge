package com.miterangle.gauge.domain.model

enum class MeasureUnit {
    MM,
    INCH;

    fun toMillimetres(value: Double): Double = if (this == INCH) value * MM_PER_INCH else value

    fun fromMillimetres(valueMm: Double): Double = if (this == INCH) valueMm / MM_PER_INCH else valueMm

    companion object {
        const val MM_PER_INCH = 25.4

        fun fromKey(key: String): MeasureUnit = entries.firstOrNull { it.name == key } ?: MM
    }
}
