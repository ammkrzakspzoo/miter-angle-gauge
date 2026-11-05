package com.miterangle.gauge.domain.repository

import com.miterangle.gauge.domain.model.GaugeSettings
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val settings: StateFlow<GaugeSettings>
    fun update(transform: (GaugeSettings) -> GaugeSettings)
}
