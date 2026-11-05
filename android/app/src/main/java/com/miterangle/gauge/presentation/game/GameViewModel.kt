package com.miterangle.gauge.presentation.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miterangle.gauge.R
import com.miterangle.gauge.core.config.GameConfig
import com.miterangle.gauge.core.config.GaugeConfig
import com.miterangle.gauge.domain.model.CalcError
import com.miterangle.gauge.domain.model.CalcOutcome
import com.miterangle.gauge.domain.model.CutInput
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.CutResult
import com.miterangle.gauge.domain.model.MeasureUnit
import com.miterangle.gauge.domain.repository.CutRepository
import com.miterangle.gauge.domain.repository.SettingsRepository
import com.miterangle.gauge.domain.usecase.CalculateMiterUseCase
import com.miterangle.gauge.domain.usecase.FormatMeasurementUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(
    private val args: GameArgs,
    private val cutRepository: CutRepository,
    private val settingsRepository: SettingsRepository,
    private val calculate: CalculateMiterUseCase,
    private val format: FormatMeasurementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(GameUiState())
    val state: StateFlow<GameUiState> = _state.asStateFlow()

    private val events = Channel<GameEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    private var stepJob: Job? = null
    private var computeJob: Job? = null

    init {
        val settings = settingsRepository.settings.value
        val stored = args.setupId?.let { id ->
            cutRepository.setups.value.firstOrNull { it.id == id }?.result
        }
        val mode = stored?.mode ?: args.mode
        val defaultSides = when {
            stored != null -> stored.sides
            mode == CutMode.SEGMENT_RING -> GaugeConfig.DEFAULT_RING_SEGMENTS
            mode == CutMode.CORNER -> 2
            else -> GaugeConfig.DEFAULT_SIDES
        }
        val defaultDimension = when {
            stored != null -> stored.dimensionMm
            mode == CutMode.SEGMENT_RING -> GaugeConfig.DEFAULT_RING_DIAMETER_MM
            else -> GaugeConfig.DEFAULT_DIMENSION_MM
        }
        val corner = args.cornerAngle ?: stored?.cornerAngleDeg ?: 90.0
        _state.value = GameUiState(
            mode = mode,
            sides = defaultSides,
            dimension = format.length(defaultDimension, settings.unit, settings.decimals),
            dimensionIsInside = stored?.dimensionIsInside ?: (mode != CutMode.SEGMENT_RING),
            stockWidth = format.length(
                stored?.stockWidthMm ?: GaugeConfig.DEFAULT_STOCK_WIDTH_MM,
                settings.unit,
                settings.decimals
            ),
            stockThickness = format.length(
                stored?.stockThicknessMm ?: GaugeConfig.DEFAULT_STOCK_THICKNESS_MM,
                settings.unit,
                settings.decimals
            ),
            cornerAngle = format.angle(corner, settings.decimals),
            springAngle = format.angle(
                stored?.springAngleDeg ?: GaugeConfig.DEFAULT_SPRING_ANGLE_DEG,
                settings.decimals
            ),
            crown = stored?.crown ?: false,
            unit = settings.unit,
            decimals = settings.decimals
        )
        recompute()
    }

    fun onDimensionChange(value: String) {
        _state.update { it.copy(dimension = value) }
        recompute()
    }

    fun onStockWidthChange(value: String) {
        _state.update { it.copy(stockWidth = value) }
        recompute()
    }

    fun onStockThicknessChange(value: String) {
        _state.update { it.copy(stockThickness = value) }
        recompute()
    }

    fun onCornerAngleChange(value: String) {
        _state.update { it.copy(cornerAngle = value) }
        recompute()
    }

    fun onSpringAngleChange(value: String) {
        _state.update { it.copy(springAngle = value) }
        recompute()
    }

    fun onInsideOutsideChange(inside: Boolean) {
        _state.update { it.copy(dimensionIsInside = inside) }
        recompute()
    }

    fun onCrownChange(crown: Boolean) {
        _state.update { it.copy(crown = crown) }
        recompute()
    }

    fun onStepStart(delta: Int) {
        stepJob?.cancel()
        applyStep(delta)
        stepJob = viewModelScope.launch {
            delay(GameConfig.STEP_REPEAT_DELAY_MS)
            while (isActive) {
                applyStep(delta)
                delay(GameConfig.STEP_REPEAT_MS)
            }
        }
    }

    fun onStepStop() {
        stepJob?.cancel()
        stepJob = null
    }

    fun onCompute() {
        if (computeJob?.isActive == true) return
        val outcome = buildOutcome() ?: return
        if (outcome !is CalcOutcome.Success) return
        computeJob = viewModelScope.launch {
            _state.update { it.copy(computing = true) }
            delay(GameConfig.COMPUTE_SWEEP_MS)
            cutRepository.storeLast(outcome.result)
            _state.update { it.copy(computing = false) }
            events.send(GameEvent.OpenResult(outcome.result.id))
        }
    }

    fun pause() {
        stepJob?.cancel()
        stepJob = null
    }

    override fun onCleared() {
        pause()
        computeJob?.cancel()
        super.onCleared()
    }

    private fun applyStep(delta: Int) {
        _state.update {
            it.copy(
                sides = (it.sides + delta).coerceIn(GaugeConfig.MIN_SIDES, GaugeConfig.MAX_SIDES)
            )
        }
        recompute()
    }

    private fun buildOutcome(): CalcOutcome? {
        val current = _state.value
        val settings = settingsRepository.settings.value
        val dimension = format.parse(current.dimension)
        val stockWidth = format.parse(current.stockWidth)
        val stockThickness = format.parse(current.stockThickness) ?: 0.0
        val corner = format.parse(current.cornerAngle) ?: 90.0
        val spring = format.parse(current.springAngle) ?: GaugeConfig.DEFAULT_SPRING_ANGLE_DEG
        if (dimension == null || dimension <= 0.0) return null
        if (stockWidth == null || stockWidth <= 0.0) return null
        val input = CutInput(
            mode = current.mode,
            sides = current.sides,
            dimensionMm = current.unit.toMillimetres(dimension),
            dimensionIsInside = current.dimensionIsInside,
            stockWidthMm = current.unit.toMillimetres(stockWidth),
            stockThicknessMm = current.unit.toMillimetres(stockThickness),
            cornerAngleDeg = corner,
            springAngleDeg = spring,
            crown = current.crown,
            kerfMm = settings.kerfMm,
            unit = current.unit,
            decimals = current.decimals
        )
        return calculate(input, System.currentTimeMillis())
    }

    private fun recompute() {
        val current = _state.value
        val dimension = format.parse(current.dimension)
        if (dimension == null || dimension <= 0.0) {
            _state.update {
                it.copy(
                    valid = false,
                    dimensionErrorRes = R.string.err_dimension,
                    generalErrorRes = null
                )
            }
            return
        }
        val stockWidth = format.parse(current.stockWidth)
        if (stockWidth == null || stockWidth <= 0.0) {
            _state.update {
                it.copy(
                    valid = false,
                    dimensionErrorRes = null,
                    generalErrorRes = R.string.err_stock
                )
            }
            return
        }
        when (val outcome = buildOutcome()) {
            is CalcOutcome.Success -> applyPreview(outcome.result)
            is CalcOutcome.Failure -> _state.update {
                it.copy(
                    valid = false,
                    dimensionErrorRes = null,
                    generalErrorRes = errorMessage(outcome.error)
                )
            }

            null -> _state.update {
                it.copy(
                    valid = false,
                    dimensionErrorRes = R.string.err_dimension,
                    generalErrorRes = null
                )
            }
        }
    }

    private fun applyPreview(result: CutResult) {
        _state.update {
            it.copy(
                valid = true,
                dimensionErrorRes = null,
                generalErrorRes = null,
                dialAngle = result.miterDeg.toFloat(),
                miterText = format.angle(result.miterDeg, it.decimals),
                bevelText = format.angle(result.bevelDeg, it.decimals),
                jointText = format.angle(result.jointAngleDeg, it.decimals),
                partLengthText = format.length(result.partLengthMm, it.unit, it.decimals),
                totalStockText = format.longLength(result.totalStockMm, it.unit),
                partCount = result.partCount
            )
        }
    }

    private fun errorMessage(error: CalcError): Int = when (error) {
        CalcError.SIDES -> R.string.err_sides
        CalcError.DIMENSION -> R.string.err_dimension
        CalcError.STOCK -> R.string.err_stock
        CalcError.RING_TOO_SMALL -> R.string.err_ring_small
        CalcError.CORNER -> R.string.err_corner
    }
}
