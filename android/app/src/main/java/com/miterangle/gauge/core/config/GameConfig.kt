package com.miterangle.gauge.core.config

object GameConfig {
    const val LOADER_DURATION_MS = 8000L
    const val DIAL_SPRING_DAMPING = 0.52f
    const val DIAL_SPRING_STIFFNESS = 520f
    const val FADE_MS = 320
    const val FADE_OUT_MS = 260
    const val SLIDE_MS = 260
    const val SIDE_FAN_MS_PER_SIDE = 90
    const val HAPTIC_MIN_INTERVAL_MS = 90L
    const val JOINT_SPIN_MS = 18000
    const val COMPUTE_SWEEP_MS = 280L
    const val SENSOR_PROBE_MS = 1500L
    const val STEP_REPEAT_DELAY_MS = 400L
    const val STEP_REPEAT_MS = 120L
}

object GaugeConfig {
    const val MIN_SIDES = 3
    const val MAX_SIDES = 24
    const val DEFAULT_SIDES = 6
    const val DEFAULT_RING_SEGMENTS = 12
    const val DEFAULT_KERF_MM = 3.0
    const val DEFAULT_SPRING_ANGLE_DEG = 38.0
    const val DEFAULT_STOCK_WIDTH_MM = 70.0
    const val DEFAULT_STOCK_THICKNESS_MM = 19.0
    const val DEFAULT_DIMENSION_MM = 300.0
    const val DEFAULT_RING_DIAMETER_MM = 240.0
    const val MM_PER_INCH = 25.4
    const val TILT_SMOOTHING = 0.88f
}
