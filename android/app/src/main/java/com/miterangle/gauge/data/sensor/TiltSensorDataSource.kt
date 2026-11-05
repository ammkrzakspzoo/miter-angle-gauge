package com.miterangle.gauge.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.miterangle.gauge.core.config.GaugeConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.abs
import kotlin.math.atan2

class TiltSensorDataSource(context: Context) {

    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    val available: Boolean
        get() = manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    fun tiltDegrees(): Flow<Float> = callbackFlow {
        val sensorManager = manager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || sensor == null) {
            close()
            return@callbackFlow
        }
        var smoothed = Float.NaN
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                if (abs(x) < 0.0001f && abs(y) < 0.0001f) return
                val raw = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
                smoothed = if (smoothed.isNaN()) {
                    raw
                } else {
                    smoothed * GaugeConfig.TILT_SMOOTHING + raw * (1f - GaugeConfig.TILT_SMOOTHING)
                }
                trySend(smoothed)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}
