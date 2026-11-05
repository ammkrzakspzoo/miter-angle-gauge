package com.miterangle.gauge.core.navigation

import com.miterangle.gauge.domain.model.CutMode

sealed interface Screen {
    data object Splash : Screen
    data object Menu : Screen
    data class Game(
        val mode: CutMode,
        val setupId: String? = null,
        val cornerAngle: Double? = null
    ) : Screen
    data class GameOver(val resultId: String) : Screen
    data object CornerMeasure : Screen
    data class GapCorrector(val resultId: String? = null) : Screen
    data object SavedSetups : Screen
    data object Settings : Screen
}

val Screen.routeKey: String
    get() = when (this) {
        is Screen.Splash -> "splash"
        is Screen.Menu -> "menu"
        is Screen.Game -> "game-" + mode.name + "-" + (setupId ?: "") + "-" + (cornerAngle?.toString() ?: "")
        is Screen.GameOver -> "gameover-" + resultId
        is Screen.CornerMeasure -> "corner"
        is Screen.GapCorrector -> "gap-" + (resultId ?: "")
        is Screen.SavedSetups -> "saved"
        is Screen.Settings -> "settings"
    }

val Screen.depth: Int
    get() = when (this) {
        is Screen.Splash -> 0
        is Screen.Menu -> 1
        is Screen.Game -> 2
        is Screen.CornerMeasure -> 2
        is Screen.SavedSetups -> 2
        is Screen.Settings -> 2
        is Screen.GapCorrector -> 3
        is Screen.GameOver -> 3
    }
