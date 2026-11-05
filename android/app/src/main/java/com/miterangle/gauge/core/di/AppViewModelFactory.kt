package com.miterangle.gauge.core.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.presentation.app.AppViewModel
import com.miterangle.gauge.presentation.corner.CornerMeasureViewModel
import com.miterangle.gauge.presentation.game.GameArgs
import com.miterangle.gauge.presentation.game.GameViewModel
import com.miterangle.gauge.presentation.gameover.GameOverViewModel
import com.miterangle.gauge.presentation.gap.GapCorrectorViewModel
import com.miterangle.gauge.presentation.menu.MenuViewModel
import com.miterangle.gauge.presentation.saved.SavedSetupsViewModel
import com.miterangle.gauge.presentation.settings.SettingsViewModel
import com.miterangle.gauge.presentation.splash.SplashViewModel

object AppViewModelFactory {

    fun create(
        gameArgs: GameArgs = GameArgs(CutMode.POLYGON),
        resultId: String = ""
    ): ViewModelProvider.Factory = viewModelFactory {
        initializer {
            AppViewModel()
        }
        initializer {
            SplashViewModel()
        }
        initializer {
            MenuViewModel(
                ServiceLocator.cutRepository,
                ServiceLocator.settingsRepository,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            GameViewModel(
                gameArgs,
                ServiceLocator.cutRepository,
                ServiceLocator.settingsRepository,
                ServiceLocator.calculateMiter,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            GameOverViewModel(
                resultId,
                ServiceLocator.cutRepository,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            CornerMeasureViewModel(
                ServiceLocator.tiltSensor,
                ServiceLocator.settingsRepository,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            GapCorrectorViewModel(
                resultId,
                ServiceLocator.cutRepository,
                ServiceLocator.settingsRepository,
                ServiceLocator.gapCorrection,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            SavedSetupsViewModel(
                ServiceLocator.cutRepository,
                ServiceLocator.settingsRepository,
                ServiceLocator.formatMeasurement
            )
        }
        initializer {
            SettingsViewModel(
                ServiceLocator.settingsRepository,
                ServiceLocator.tiltSensor,
                ServiceLocator.formatMeasurement
            )
        }
    }
}
