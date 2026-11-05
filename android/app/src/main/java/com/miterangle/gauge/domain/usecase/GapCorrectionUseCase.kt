package com.miterangle.gauge.domain.usecase

import com.miterangle.gauge.domain.model.GapCorrection
import kotlin.math.atan

class GapCorrectionUseCase {

    operator fun invoke(
        gapMm: Double,
        jointLengthMm: Double,
        gapAtOutside: Boolean,
        currentMiterDeg: Double?
    ): GapCorrection? {
        if (gapMm < 0.0 || jointLengthMm <= 0.0) return null
        val error = Math.toDegrees(atan(gapMm / jointLengthMm))
        val adjustment = error / 2.0
        val corrected = currentMiterDeg?.let {
            if (gapAtOutside) it - adjustment else it + adjustment
        }
        return GapCorrection(
            adjustmentDeg = adjustment,
            decrease = gapAtOutside,
            correctedMiterDeg = corrected?.coerceIn(0.0, 89.9)
        )
    }
}
