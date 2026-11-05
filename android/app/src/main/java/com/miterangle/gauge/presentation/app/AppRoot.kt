package com.miterangle.gauge.presentation.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.core.config.GameConfig
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.navigation.Screen
import com.miterangle.gauge.core.navigation.depth
import com.miterangle.gauge.core.navigation.routeKey
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.presentation.corner.CornerMeasureRoute
import com.miterangle.gauge.presentation.game.GameRoute
import com.miterangle.gauge.presentation.gameover.GameOverRoute
import com.miterangle.gauge.presentation.gap.GapCorrectorRoute
import com.miterangle.gauge.presentation.menu.MenuRoute
import com.miterangle.gauge.presentation.saved.SavedSetupsRoute
import com.miterangle.gauge.presentation.settings.SettingsRoute
import com.miterangle.gauge.presentation.splash.SplashRoute

@Composable
fun AppRoot() {
    val appViewModel: AppViewModel = viewModel(factory = AppViewModelFactory.create())
    val screen by appViewModel.screen.collectAsStateWithLifecycle()
    val canGoBack by appViewModel.canGoBack.collectAsStateWithLifecycle()
    val colors = HudTheme.colors

    BackHandler(enabled = canGoBack) {
        appViewModel.back()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.backgroundDeep)
    ) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (targetState.depth > initialState.depth && initialState != Screen.Splash) {
                    (slideInHorizontally { full -> full / 6 } + fadeIn(tween(GameConfig.SLIDE_MS)))
                        .togetherWith(fadeOut(tween(200)))
                } else {
                    fadeIn(tween(GameConfig.FADE_MS))
                        .togetherWith(fadeOut(tween(GameConfig.FADE_OUT_MS)))
                }
            },
            label = "screenHost"
        ) { current ->
            when (current) {
                is Screen.Splash -> SplashRoute(
                    onFinished = { appViewModel.replaceWith(Screen.Menu) }
                )

                is Screen.Menu -> MenuRoute(
                    onStart = { mode -> appViewModel.navigate(Screen.Game(mode)) },
                    onCornerMeasure = { appViewModel.navigate(Screen.CornerMeasure) },
                    onGapCorrector = { appViewModel.navigate(Screen.GapCorrector()) },
                    onSavedSetups = { appViewModel.navigate(Screen.SavedSetups) },
                    onSettings = { appViewModel.navigate(Screen.Settings) },
                    onLastResult = { id -> appViewModel.navigate(Screen.GameOver(id)) }
                )

                is Screen.Game -> GameRoute(
                    key = current.routeKey,
                    mode = current.mode,
                    setupId = current.setupId,
                    cornerAngle = current.cornerAngle,
                    onBack = { appViewModel.back() },
                    onResult = { id -> appViewModel.navigate(Screen.GameOver(id)) }
                )

                is Screen.GameOver -> GameOverRoute(
                    key = current.routeKey,
                    resultId = current.resultId,
                    onBack = { appViewModel.back() },
                    onComputeAgain = { appViewModel.back() },
                    onGapCheck = { appViewModel.navigate(Screen.GapCorrector(current.resultId)) },
                    onMenu = { appViewModel.replaceWith(Screen.Menu) }
                )

                is Screen.CornerMeasure -> CornerMeasureRoute(
                    onBack = { appViewModel.back() },
                    onUseAngle = { angle ->
                        appViewModel.navigate(Screen.Game(CutMode.CORNER, cornerAngle = angle))
                    }
                )

                is Screen.GapCorrector -> GapCorrectorRoute(
                    key = current.routeKey,
                    resultId = current.resultId,
                    onBack = { appViewModel.back() },
                    onApplied = { appViewModel.back() },
                    onMenu = { appViewModel.replaceWith(Screen.Menu) }
                )

                is Screen.SavedSetups -> SavedSetupsRoute(
                    onBack = { appViewModel.back() },
                    onOpenSetup = { mode, setupId ->
                        appViewModel.navigate(Screen.Game(mode, setupId = setupId))
                    },
                    onStart = { appViewModel.navigate(Screen.Game(CutMode.POLYGON)) }
                )

                is Screen.Settings -> SettingsRoute(
                    onBack = { appViewModel.back() }
                )
            }
        }
    }
}
