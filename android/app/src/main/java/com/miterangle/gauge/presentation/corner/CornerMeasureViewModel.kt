package com.miterangle.gauge.presentation.corner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.core.config.GameConfig
import com.miterangle.gauge.data.sensor.TiltSensorDataSource
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class CornerUiState(
    val manual: Boolean = false,
    val measuring: Boolean = true,
    val liveAngle: Float = 0f,
    val manualInput: String = "",
    val liveAngleText: String = "",
    val outOfSquareText: String = "",
    val wallAText: String = "",
    val wallBText: String = "",
    val cornerAngle: Double? = null,
    val waitingForWallB: Boolean = false,
    val captured: Boolean = false
)

object CornerHapticTick

class CornerMeasureViewModel(
    private val tiltSensor: TiltSensorDataSource,
    private val settingsRepository: SettingsRepository,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CornerUiState())
    val state: StateFlow<CornerUiState> = _state.asStateFlow()

    private val events = Channel<CornerHapticTick>(Channel.CONFLATED)
    val eventFlow = events.receiveAsFlow()

    private var sensorJob: Job? = null
    private var watchdogJob: Job? = null
    private var lastHapticAt = 0L
    private var lastDegree = Int.MIN_VALUE
    private var sampleSeen = false

    private val decimals: Int
        get() = settingsRepository.settings.value.decimals

    init {
        _state.value = CornerUiState(
            manualInput = format.angle(90.0, decimals),
            liveAngleText = format.angle(90.0, decimals),
            outOfSquareText = format.angle(0.0, decimals)
        )
        start()
    }

    fun start() {
        if (sensorJob?.isActive == true) return
        _state.update { it.copy(measuring = true) }
        if (!tiltSensor.available) {
            switchToManual()
            return
        }
        sensorJob = viewModelScope.launch {
            tiltSensor.tiltDegrees().collect { reading ->
                sampleSeen = true
                onReading(reading)
            }
        }
        watchdogJob = viewModelScope.launch {
            delay(GameConfig.SENSOR_PROBE_MS)
            if (!sampleSeen) {
                switchToManual()
            }
        }
    }

    fun pause() {
        sensorJob?.cancel()
        sensorJob = null
        watchdogJob?.cancel()
        watchdogJob = null
        _state.update { it.copy(measuring = false) }
    }

    fun toggleMeasuring() {
        if (_state.value.measuring) pause() else start()
    }

    fun onManualInputChange(value: String) {
        val parsed = format.parse(value)
        _state.update {
            it.copy(
                manualInput = value,
                cornerAngle = parsed?.takeIf { angle -> angle > 0.0 && angle < 180.0 },
                liveAngleText = parsed?.let { angle -> format.angle(angle, decimals) }
                    ?: it.liveAngleText,
                outOfSquareText = parsed?.let { angle -> format.angle(angle - 90.0, decimals) }
                    ?: it.outOfSquareText
            )
        }
    }

    fun onCapture() {
        val current = _state.value
        if (current.manual) return
        val reading = current.liveAngle
        if (!current.waitingForWallB) {
            _state.update {
                it.copy(
                    wallAText = format.angle(reading.toDouble(), decimals),
                    waitingForWallB = true,
                    captured = false,
                    cornerAngle = null
                )
            }
            wallA = reading
        } else {
            val corner = foldCorner(abs(reading - wallA))
            _state.update {
                it.copy(
                    wallBText = format.angle(reading.toDouble(), decimals),
                    waitingForWallB = false,
                    captured = true,
                    cornerAngle = corner,
                    liveAngleText = format.angle(corner, decimals),
                    outOfSquareText = format.angle(corner - 90.0, decimals)
                )
            }
        }
    }

    fun onReset() {
        wallA = 0f
        _state.update {
            it.copy(
                wallAText = "",
                wallBText = "",
                waitingForWallB = false,
                captured = false,
                cornerAngle = null
            )
        }
    }

    override fun onCleared() {
        pause()
        super.onCleared()
    }

    private var wallA: Float = 0f

    private fun switchToManual() {
        sensorJob?.cancel()
        sensorJob = null
        val parsed = format.parse(_state.value.manualInput) ?: 90.0
        _state.update {
            it.copy(
                manual = true,
                measuring = false,
                cornerAngle = parsed,
                liveAngleText = format.angle(parsed, decimals),
                outOfSquareText = format.angle(parsed - 90.0, decimals)
            )
        }
    }

    private fun onReading(reading: Float) {
        val current = _state.value
        if (current.captured) return
        val degree = reading.roundToInt()
        if (degree != lastDegree) {
            lastDegree = degree
            val now = System.currentTimeMillis()
            if (settingsRepository.settings.value.haptics &&
                now - lastHapticAt >= GameConfig.HAPTIC_MIN_INTERVAL_MS
            ) {
                lastHapticAt = now
                events.trySend(CornerHapticTick)
            }
        }
        val shown = foldCorner(abs(reading.toDouble()))
        _state.update {
            it.copy(
                liveAngle = reading,
                liveAngleText = format.angle(shown, decimals),
                outOfSquareText = format.angle(shown - 90.0, decimals)
            )
        }
    }

    private fun foldCorner(raw: Double): Double {
        var value = abs(raw) % 360.0
        if (value > 180.0) value = 360.0 - value
        if (value < 1.0) value = 90.0
        return value
    }

    private fun foldCorner(raw: Float): Double = foldCorner(raw.toDouble())
}
