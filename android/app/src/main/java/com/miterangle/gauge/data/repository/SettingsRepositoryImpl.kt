package com.miterangle.gauge.data.repository

import com.miterangle.gauge.core.theme.ThemeMode
import com.miterangle.gauge.data.local.GaugePreferences
import com.miterangle.gauge.domain.model.GaugeSettings
import com.miterangle.gauge.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepositoryImpl(
    private val preferences: GaugePreferences
) : SettingsRepository {

    private val _settings = MutableStateFlow(preferences.readSettings())
    override val settings: StateFlow<GaugeSettings> = _settings.asStateFlow()

    init {
        ThemeMode.set(_settings.value.bright)
    }

    override fun update(transform: (GaugeSettings) -> GaugeSettings) {
        val next = transform(_settings.value).let {
            it.copy(decimals = it.decimals.coerceIn(1, 3), kerfMm = it.kerfMm.coerceIn(0.0, 20.0))
        }
        _settings.value = next
        preferences.writeSettings(next)
        ThemeMode.set(next.bright)
    }
}
