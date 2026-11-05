package com.miterangle.gauge.domain.repository

import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.SavedSetup
import kotlinx.coroutines.flow.StateFlow

interface CutRepository {
    val setups: StateFlow<List<SavedSetup>>
    val lastResult: StateFlow<CutResult?>
    fun storeLast(result: CutResult)
    fun find(id: String): CutResult?
    fun save(title: String, note: String, result: CutResult)
    fun isSaved(resultId: String): Boolean
    fun delete(id: String)
}
