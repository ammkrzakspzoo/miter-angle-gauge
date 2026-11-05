package com.miterangle.gauge.domain.model

enum class CalcError {
    SIDES,
    DIMENSION,
    STOCK,
    RING_TOO_SMALL,
    CORNER
}

sealed interface CalcOutcome {
    data class Success(val result: CutResult) : CalcOutcome
    data class Failure(val error: CalcError) : CalcOutcome
}
