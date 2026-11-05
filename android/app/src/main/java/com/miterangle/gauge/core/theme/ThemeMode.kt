package com.miterangle.gauge.core.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ThemeMode {
    private val _bright = MutableStateFlow(false)
    val bright: StateFlow<Boolean> = _bright.asStateFlow()

    fun set(value: Boolean) {
        _bright.value = value
    }
}
