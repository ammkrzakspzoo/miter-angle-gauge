package com.miterangle.gauge.core.di

import android.content.Context
import com.miterangle.gauge.data.local.GaugePreferences
import com.miterangle.gauge.data.repository.CutRepositoryImpl
import com.miterangle.gauge.data.repository.SettingsRepositoryImpl
import com.miterangle.gauge.data.sensor.TiltSensorDataSource
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.CalculateMiterUseCase
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import com.miterangle.gauge.domain.usecase.GapCorrectionUseCase

object ServiceLocator {

    private var started = false

    private var cuts: CutRepository? = null
    private var gaugeSettings: SettingsRepository? = null
    private var tilt: TiltSensorDataSource? = null

    val calculateMiter = CalculateMiterUseCase()
    val gapCorrection = GapCorrectionUseCase()
    val formatMeasurement = FormatMeasurementUseCase()

    val cutRepository: CutRepository
        get() = requireNotNull(cuts) { "ServiceLocator not initialised" }

    val settingsRepository: SettingsRepository
        get() = requireNotNull(gaugeSettings) { "ServiceLocator not initialised" }

    val tiltSensor: TiltSensorDataSource
        get() = requireNotNull(tilt) { "ServiceLocator not initialised" }

    fun init(context: Context) {
        if (started) return
        val preferences = GaugePreferences(context.applicationContext)
        gaugeSettings = SettingsRepositoryImpl(preferences)
        cuts = CutRepositoryImpl(preferences, calculateMiter)
        tilt = TiltSensorDataSource(context.applicationContext)
        started = true
    }
}
