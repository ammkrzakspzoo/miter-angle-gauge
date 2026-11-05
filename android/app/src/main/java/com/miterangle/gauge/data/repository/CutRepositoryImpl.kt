package com.miterangle.gauge.data.repository

import com.miterangle.gauge.data.local.GaugePreferences
import com.miterangle.gauge.data.sample.SampleData
import com.miterangle.gauge.domain.model.CalcOutcome
import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.SavedSetup
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.usecase.CalculateMiterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CutRepositoryImpl(
    private val preferences: GaugePreferences,
    private val calculate: CalculateMiterUseCase
) : CutRepository {

    private val _setups = MutableStateFlow<List<SavedSetup>>(emptyList())
    override val setups: StateFlow<List<SavedSetup>> = _setups.asStateFlow()

    private val _lastResult = MutableStateFlow<CutResult?>(null)
    override val lastResult: StateFlow<CutResult?> = _lastResult.asStateFlow()

    init {
        if (!preferences.isSeeded()) {
            preferences.writeSetups(seed())
            preferences.markSeeded()
        }
        _setups.value = preferences.readSetups()
        _lastResult.value = preferences.readLast()
    }

    override fun storeLast(result: CutResult) {
        _lastResult.value = result
        preferences.writeLast(result)
    }

    override fun find(id: String): CutResult? {
        _lastResult.value?.let { if (it.id == id) return it }
        return _setups.value.firstOrNull { it.result.id == id }?.result
    }

    override fun save(title: String, note: String, result: CutResult) {
        if (isSaved(result.id)) return
        val setup = SavedSetup(
            id = "setup_" + result.id,
            title = title,
            note = note,
            result = result
        )
        val updated = listOf(setup) + _setups.value
        _setups.value = updated
        preferences.writeSetups(updated)
    }

    override fun isSaved(resultId: String): Boolean =
        _setups.value.any { it.result.id == resultId }

    override fun delete(id: String) {
        val updated = _setups.value.filterNot { it.id == id }
        _setups.value = updated
        preferences.writeSetups(updated)
    }

    private fun seed(): List<SavedSetup> = SampleData.setups.mapIndexedNotNull { index, sample ->
        val outcome = calculate(sample.input, SampleData.SEED_TIMESTAMP + index)
        if (outcome is CalcOutcome.Success) {
            SavedSetup(
                id = sample.id,
                title = sample.title,
                note = sample.note,
                result = outcome.result
            )
        } else {
            null
        }
    }
}
