package com.miterangle.gauge.presentation.app

import androidx.lifecycle.ViewModel
import com.miterangle.gauge.core.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppViewModel : ViewModel() {

    private val backStack = ArrayDeque<Screen>()

    private val _screen = MutableStateFlow<Screen>(Screen.Splash)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    private val _forward = MutableStateFlow(true)
    val forward: StateFlow<Boolean> = _forward.asStateFlow()

    fun navigate(target: Screen) {
        val current = _screen.value
        if (current !is Screen.Splash) {
            backStack.addLast(current)
        }
        _forward.value = true
        _screen.value = target
        _canGoBack.value = backStack.isNotEmpty()
    }

    fun replaceWith(target: Screen) {
        backStack.clear()
        _forward.value = false
        _screen.value = target
        _canGoBack.value = false
    }

    fun back() {
        val previous = backStack.removeLastOrNull() ?: return
        _forward.value = false
        _screen.value = previous
        _canGoBack.value = backStack.isNotEmpty()
    }

    fun backTo(target: Screen) {
        while (backStack.isNotEmpty() && backStack.last() != target) {
            backStack.removeLast()
        }
        if (backStack.isEmpty()) {
            replaceWith(target)
        } else {
            back()
        }
    }
}
