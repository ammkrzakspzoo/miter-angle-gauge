package com.miterangle.gauge.presentation.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SplashRoute(onFinished: () -> Unit) {
    val splashViewModel: SplashViewModel = viewModel(
        key = "splash",
        factory = AppViewModelFactory.create()
    )
    val finished by splashViewModel.finished.collectAsStateWithLifecycle()
    LaunchedEffect(finished) {
        if (finished) {
            onFinished()
        }
    }
    SplashScreen()
}

@Composable
private fun SplashScreen() {
    val colors = HudTheme.colors
    val transition = rememberInfiniteTransition(label = "splash")
    val ringSpin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringSpin"
    )
    val needleSwing by transition.animateFloat(
        initialValue = -52f,
        targetValue = 52f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "needleSwing"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
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
                            colors.backgroundDeep.copy(alpha = 0.90f),
                            colors.backgroundDeep.copy(alpha = 0.95f),
                            Color.Black.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(196.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(ringSpin)
                ) {
                    val radius = min(size.width, size.height) / 2f
                    drawCircle(
                        color = colors.outline,
                        radius = radius - 2.dp.toPx(),
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                    for (index in 0 until 72) {
                        val major = index % 6 == 0
                        val length = if (major) 14.dp.toPx() else 7.dp.toPx()
                        val radians = Math.toRadians(index * 5.0 - 90.0)
                        val cosine = cos(radians).toFloat()
                        val sine = sin(radians).toFloat()
                        val outer = radius - 6.dp.toPx()
                        drawLine(
                            color = if (major) colors.primary else colors.outline,
                            start = Offset(center.x + cosine * outer, center.y + sine * outer),
                            end = Offset(
                                center.x + cosine * (outer - length),
                                center.y + sine * (outer - length)
                            ),
                            strokeWidth = if (major) 1.5.dp.toPx() else 1.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = min(size.width, size.height) / 2f
                    drawCircle(
                        color = colors.primary.copy(alpha = glowAlpha),
                        radius = radius - 22.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(needleSwing)
                ) {
                    val radius = min(size.width, size.height) / 2f
                    drawLine(
                        color = colors.accent,
                        start = center,
                        end = Offset(center.x, center.y - radius + 20.dp.toPx()),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = stringResource(R.string.cd_logo),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(148.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
            Text(
                text = stringResource(R.string.splash_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary
            )
            Text(
                text = stringResource(R.string.splash_title_2),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.primary
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.splash_subtitle),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(34.dp))
            LinearProgressIndicator(
                color = colors.primary,
                trackColor = colors.surfaceVariant,
                strokeCap = StrokeCap.Round,
                modifier = Modifier
                    .size(width = 168.dp, height = 3.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.splash_status),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 32.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.outline)
                    .alpha(0.8f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.splash_footer),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}
