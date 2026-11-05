package com.miterangle.gauge.domain.usecase

import com.miterangle.gauge.domain.model.CalcError
import com.miterangle.gauge.domain.model.CalcOutcome
import com.miterangle.gauge.domain.model.CutInput
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.CutResult
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

class CalculateMiterUseCase {

    operator fun invoke(input: CutInput, idSeed: Long): CalcOutcome {
        if (input.dimensionMm <= 0.0) return CalcOutcome.Failure(CalcError.DIMENSION)
        if (input.stockWidthMm <= 0.0) return CalcOutcome.Failure(CalcError.STOCK)
        return when (input.mode) {
            CutMode.POLYGON -> polygon(input, idSeed)
            CutMode.SEGMENT_RING -> ring(input, idSeed)
            CutMode.CORNER -> corner(input, idSeed)
        }
    }

    private fun polygon(input: CutInput, idSeed: Long): CalcOutcome {
        val n = input.sides
        if (n < MIN_SIDES || n > MAX_SIDES) return CalcOutcome.Failure(CalcError.SIDES)
        val half = 180.0 / n
        val t = tan(Math.toRadians(half))
        val inner: Double
        val outer: Double
        if (input.dimensionIsInside) {
            inner = input.dimensionMm * t
            outer = inner + 2.0 * input.stockWidthMm * t
        } else {
            outer = input.dimensionMm * t
            inner = outer - 2.0 * input.stockWidthMm * t
        }
        return CalcOutcome.Success(
            result(input, idSeed, half, 0.0, (n - 2) * 180.0 / n, outer, inner, n)
        )
    }

    private fun ring(input: CutInput, idSeed: Long): CalcOutcome {
        val n = input.sides
        if (n < MIN_SIDES || n > MAX_SIDES) return CalcOutcome.Failure(CalcError.SIDES)
        val half = 180.0 / n
        val t = tan(Math.toRadians(half))
        val outer = input.dimensionMm * t
        val inner = outer - 2.0 * input.stockWidthMm * t
        if (inner <= 0.0) return CalcOutcome.Failure(CalcError.RING_TOO_SMALL)
        return CalcOutcome.Success(
            result(input, idSeed, half, 0.0, (n - 2) * 180.0 / n, outer, inner, n)
        )
    }

    private fun corner(input: CutInput, idSeed: Long): CalcOutcome {
        val theta = input.cornerAngleDeg
        if (theta <= 0.0 || theta >= 180.0) return CalcOutcome.Failure(CalcError.CORNER)
        val base = (180.0 - theta) / 2.0
        val baseRad = Math.toRadians(base)
        val miter: Double
        val bevel: Double
        if (input.crown) {
            val spring = Math.toRadians(input.springAngleDeg.coerceIn(1.0, 89.0))
            miter = Math.toDegrees(atan(sin(spring) * tan(baseRad)))
            bevel = Math.toDegrees(atan(cos(spring) * sin(baseRad)))
        } else {
            miter = base
            bevel = 0.0
        }
        val t = tan(baseRad)
        val inner = input.dimensionMm * t
        val outer = inner + 2.0 * input.stockWidthMm * t
        return CalcOutcome.Success(
            result(input, idSeed, miter, bevel, theta, outer, inner, 2)
        )
    }

    private fun result(
        input: CutInput,
        idSeed: Long,
        miter: Double,
        bevel: Double,
        joint: Double,
        outer: Double,
        inner: Double,
        count: Int
    ): CutResult = CutResult(
        id = "cut_" + idSeed.toString(36) + "_" + abs(input.hashCode()).toString(36),
        mode = input.mode,
        sides = input.sides,
        dimensionMm = input.dimensionMm,
        dimensionIsInside = input.dimensionIsInside,
        stockWidthMm = input.stockWidthMm,
        stockThicknessMm = input.stockThicknessMm,
        cornerAngleDeg = input.cornerAngleDeg,
        springAngleDeg = input.springAngleDeg,
        crown = input.crown,
        miterDeg = miter,
        bevelDeg = bevel,
        jointAngleDeg = joint,
        partLengthMm = outer,
        innerLengthMm = inner.coerceAtLeast(0.0),
        partCount = count,
        totalStockMm = count * (outer + input.kerfMm),
        kerfMm = input.kerfMm,
        unit = input.unit,
        decimals = input.decimals,
        createdAt = idSeed
    )

    companion object {
        const val MIN_SIDES = 3
        const val MAX_SIDES = 24
    }
}
