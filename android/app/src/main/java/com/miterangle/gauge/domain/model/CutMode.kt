package com.miterangle.gauge.domain.model

enum class CutMode {
    POLYGON,
    SEGMENT_RING,
    CORNER;

    companion object {
        fun fromKey(key: String): CutMode = entries.firstOrNull { it.name == key } ?: POLYGON
    }
}
