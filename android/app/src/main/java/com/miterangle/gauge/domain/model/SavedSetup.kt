package com.miterangle.gauge.domain.model

data class SavedSetup(
    val id: String,
    val title: String,
    val note: String,
    val result: CutResult
)
