package com.miterangle.gauge.domain.usecase

import com.miterangle.gauge.domain.model.MeasureUnit
import java.util.Locale

class FormatMeasurementUseCase {

    fun angle(degrees: Double, decimals: Int): String =
        String.format(Locale.US, "%." + clampDecimals(decimals) + "f", degrees)

    fun length(valueMm: Double, unit: MeasureUnit, decimals: Int): String =
        String.format(Locale.US, "%." + clampDecimals(decimals) + "f", unit.fromMillimetres(valueMm))

    fun longLength(valueMm: Double, unit: MeasureUnit): String = when (unit) {
        MeasureUnit.MM -> String.format(Locale.US, "%.2f", valueMm / 1000.0)
        MeasureUnit.INCH -> String.format(Locale.US, "%.2f", valueMm / MeasureUnit.MM_PER_INCH / 12.0)
    }

    fun parse(raw: String): Double? = raw.trim().replace(',', '.').toDoubleOrNull()

    private fun clampDecimals(decimals: Int): Int = decimals.coerceIn(1, 3)
}
