package com.miterangle.gauge.presentation.menu

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.core.ui.BlueprintGrid
import com.miterangle.gauge.core.ui.GhostIconButton
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.SecondaryButton
import com.miterangle.gauge.core.ui.SegmentedChips
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.domain.model.MeasureUnit

@Composable
fun MenuRoute(
    onStart: (CutMode) -> Unit,
    onCornerMeasure: () -> Unit,
    onGapCorrector: () -> Unit,
    onSavedSetups: () -> Unit,
    onSettings: () -> Unit,
    onLastResult: (String) -> Unit
) {
    val menuViewModel: MenuViewModel = viewModel(
        key = "menu",
        factory = AppViewModelFactory.create()
    )
    val state by menuViewModel.state.collectAsStateWithLifecycle()
    MenuScreen(
        state = state,
        onSelectUnit = menuViewModel::selectUnit,
        onStart = onStart,
        onCornerMeasure = onCornerMeasure,
        onGapCorrector = onGapCorrector,
        onSavedSetups = onSavedSetups,
        onSettings = onSettings,
        onLastResult = onLastResult
    )
}

@Composable
private fun MenuScreen(
    state: MenuUiState,
    onSelectUnit: (MeasureUnit) -> Unit,
    onStart: (CutMode) -> Unit,
    onCornerMeasure: () -> Unit,
    onGapCorrector: () -> Unit,
    onSavedSetups: () -> Unit,
    onSettings: () -> Unit,
    onLastResult: (String) -> Unit
) {
    val colors = HudTheme.colors
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        revealed = true
    }
    val reveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(durationMillis = 420),
        label = "menuReveal"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_game),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.background.copy(alpha = 0.70f),
                            colors.background.copy(alpha = 0.90f),
                            colors.backgroundDeep.copy(alpha = 0.96f)
                        )
                    )
                )
        )
        BlueprintGrid(color = colors.primary)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp)
                .alpha(reveal)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.icon_home),
                        contentDescription = stringResource(R.string.cd_home),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.menu_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                }
                GhostIconButton(
                    icon = ImageVector.vectorResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.cd_settings),
                    onClick = onSettings
                )
            }

            SegmentedChips(
                options = listOf(
                    stringResource(R.string.unit_mm),
                    stringResource(R.string.unit_inch)
                ),
                selectedIndex = if (state.unit == MeasureUnit.MM) 0 else 1,
                onSelect = { index ->
                    onSelectUnit(if (index == 0) MeasureUnit.MM else MeasureUnit.INCH)
                },
                selectedStateDescription = stringResource(R.string.state_selected),
                unselectedStateDescription = stringResource(R.string.state_not_selected)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1.7f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroTile(
                        modifier = Modifier
                            .weight(0.62f)
                            .fillMaxHeight(),
                        onStart = { onStart(CutMode.POLYGON) }
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.38f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LastCutTile(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            state = state,
                            onClick = { state.lastResultId?.let(onLastResult) }
                        )
                        SavedTile(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            count = state.savedCount,
                            onClick = onSavedSetups
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModeTile(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        painter = painterResource(R.drawable.sprite_2),
                        contentDescription = stringResource(R.string.cd_ring),
                        title = stringResource(R.string.mode_ring),
                        badge = null,
                        onClick = { onStart(CutMode.SEGMENT_RING) }
                    )
                    ModeTile(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        painter = painterResource(R.drawable.sprite_3),
                        contentDescription = stringResource(R.string.cd_corner),
                        title = stringResource(R.string.mode_corner),
                        badge = stringResource(R.string.badge_tilt),
                        onClick = onCornerMeasure
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            SecondaryButton(
                text = stringResource(R.string.cta_gap_corrector),
                onClick = onGapCorrector,
                accent = colors.accent,
                leadingPainter = painterResource(R.drawable.sprite_5),
                leadingDescription = stringResource(R.string.cd_gauge),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.menu_hint),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
private fun HeroTile(
    modifier: Modifier,
    onStart: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Column(
        modifier = modifier
            .shadow(
                elevation = 18.dp,
                shape = shape,
                ambientColor = colors.primary,
                spotColor = colors.primary
            )
            .background(colors.surface.copy(alpha = 0.92f), shape)
            .border(1.dp, colors.primary.copy(alpha = 0.55f), shape)
            .drawBehind { drawCornerBrackets(colors.primary) }
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.sprite_1),
            contentDescription = stringResource(R.string.cd_polygon),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.mode_polygon),
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary
        )
        Text(
            text = stringResource(R.string.mode_polygon_caption),
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(10.dp))
        PrimaryButton(
            text = stringResource(R.string.cta_start),
            onClick = onStart,
            leadingIcon = ImageVector.vectorResource(R.drawable.ic_play_arrow),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LastCutTile(
    modifier: Modifier,
    state: MenuUiState,
    onClick: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(10.dp)
    val hasResult = state.lastResultId != null
    Column(
        modifier = modifier
            .background(colors.surface.copy(alpha = if (hasResult) 0.88f else 0.4f), shape)
            .border(1.dp, colors.outline, shape)
            .then(if (hasResult) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.tile_last_cut),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        if (hasResult) {
            Text(
                text = stringResource(R.string.fmt_degrees, state.lastAngle),
                style = MaterialTheme.typography.titleLarge,
                color = colors.accent,
                maxLines = 1
            )
            Text(
                text = stringResource(
                    R.string.fmt_last_cut,
                    state.lastSides,
                    stringResource(R.string.label_sides),
                    stringResource(
                        if (state.unit == MeasureUnit.MM) R.string.unit_mm else R.string.unit_inch
                    )
                ),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1
            )
        } else {
            Text(
                text = stringResource(R.string.tile_no_cuts),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun SavedTile(
    modifier: Modifier,
    count: Int,
    onClick: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.88f), shape)
            .border(1.dp, colors.outline, shape)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.tile_saved),
            style = MaterialTheme.typography.labelSmall,
            color = colors.textSecondary
        )
        Box(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = stringResource(R.string.fmt_count, count),
                style = MaterialTheme.typography.bodySmall,
                color = colors.primary
            )
        }
    }
}

@Composable
private fun ModeTile(
    modifier: Modifier,
    painter: Painter,
    contentDescription: String,
    title: String,
    badge: String?,
    onClick: () -> Unit
) {
    val colors = HudTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .background(colors.surface.copy(alpha = 0.86f), shape)
            .border(1.dp, colors.outline, shape)
            .drawBehind { drawCornerBrackets(colors.outline) }
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                maxLines = 1
            )
        }
        if (badge != null) {
            Text(
                text = badge,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .background(colors.accent.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCornerBrackets(color: Color) {
    val arm = 18.dp.toPx()
    val inset = 7.dp.toPx()
    val width = 1.5.dp.toPx()
    val tint = color.copy(alpha = 0.75f)
    val left = inset
    val top = inset
    val right = size.width - inset
    val bottom = size.height - inset
    val segments = listOf(
        Offset(left, top) to Offset(left + arm, top),
        Offset(left, top) to Offset(left, top + arm),
        Offset(right, top) to Offset(right - arm, top),
        Offset(right, top) to Offset(right, top + arm),
        Offset(left, bottom) to Offset(left + arm, bottom),
        Offset(left, bottom) to Offset(left, bottom - arm),
        Offset(right, bottom) to Offset(right - arm, bottom),
        Offset(right, bottom) to Offset(right, bottom - arm)
    )
    segments.forEach { segment ->
        drawLine(
            color = tint,
            start = segment.first,
            end = segment.second,
            strokeWidth = width,
            cap = StrokeCap.Round
        )
    }
}
