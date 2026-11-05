package com.miterangle.gauge.data.sample

import com.miterangle.gauge.core.config.GaugeConfig
import com.miterangle.gauge.domain.model.CutInput
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.MeasureUnit

data class SampleSetup(
    val id: String,
    val title: String,
    val note: String,
    val input: CutInput
)

object SampleData {

    val setups: List<SampleSetup> = listOf(
        SampleSetup(
            id = "setup_hex_planter",
            title = "HEX PLANTER FRAME",
            note = "CEDAR · GLUE AND BRAD NAILS",
            input = CutInput(
                mode = CutMode.POLYGON,
                sides = 6,
                dimensionMm = 300.0,
                dimensionIsInside = true,
                stockWidthMm = 70.0,
                stockThicknessMm = 19.0,
                kerfMm = GaugeConfig.DEFAULT_KERF_MM,
                unit = MeasureUnit.MM,
                decimals = 1
            )
        ),
        SampleSetup(
            id = "setup_bowl_ring",
            title = "SEGMENTED BOWL RING",
            note = "WALNUT AND MAPLE · 12 STAVES",
            input = CutInput(
                mode = CutMode.SEGMENT_RING,
                sides = 12,
                dimensionMm = 240.0,
                dimensionIsInside = false,
                stockWidthMm = 38.0,
                stockThicknessMm = 25.0,
                kerfMm = GaugeConfig.DEFAULT_KERF_MM,
                unit = MeasureUnit.MM,
                decimals = 1
            )
        ),
        SampleSetup(
            id = "setup_door_casing",
            title = "GARAGE DOOR CASING",
            note = "WALL MEASURED OUT OF SQUARE",
            input = CutInput(
                mode = CutMode.CORNER,
                sides = 2,
                dimensionMm = 2100.0,
                dimensionIsInside = true,
                stockWidthMm = 90.0,
                stockThicknessMm = 18.0,
                cornerAngleDeg = 87.4,
                kerfMm = GaugeConfig.DEFAULT_KERF_MM,
                unit = MeasureUnit.MM,
                decimals = 1
            )
        )
    )

    const val SEED_TIMESTAMP = 1762300000000L
}
