package com.miterangle.gauge.presentation.saved

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miterangle.gauge.R
import com.miterangle.gauge.core.di.AppViewModelFactory
import com.miterangle.gauge.core.theme.HudTheme
import com.miterangle.gauge.core.ui.GhostIconButton
import com.miterangle.gauge.core.ui.GlassCard
import com.miterangle.gauge.core.ui.PrimaryButton
import com.miterangle.gauge.core.ui.ScreenScaffold
import com.miterangle.gauge.domain.model.CutMode
import com.miterangle.gauge.presentation.dialog.ConfirmDeleteDialog

@Composable
fun SavedSetupsRoute(
    onBack: () -> Unit,
    onOpenSetup: (CutMode, String) -> Unit,
    onStart: () -> Unit
) {
    val savedViewModel: SavedSetupsViewModel = viewModel(
        key = "saved",
        factory = AppViewModelFactory.create()
    )
    val state by savedViewModel.state.collectAsStateWithLifecycle()

    SavedSetupsScreen(
        state = state,
        onBack = onBack,
        onOpenSetup = onOpenSetup,
        onStart = onStart,
        onDeleteRequest = savedViewModel::onDeleteRequest,
        onDeleteCancel = savedViewModel::onDeleteCancel,
        onDeleteConfirm = savedViewModel::onDeleteConfirm
    )
}

@Composable
private fun SavedSetupsScreen(
    state: SavedSetupsUiState,
    onBack: () -> Unit,
    onOpenSetup: (CutMode, String) -> Unit,
    onStart: () -> Unit,
    onDeleteRequest: (String) -> Unit,
    onDeleteCancel: () -> Unit,
    onDeleteConfirm: () -> Unit
) {
    val colors = HudTheme.colors

    ScreenScaffold(
        overlay = listOf(
            colors.background.copy(alpha = 0.76f),
            colors.background.copy(alpha = 0.92f),
            colors.backgroundDeep.copy(alpha = 0.97f)
        ),
        title = stringResource(R.string.saved_title),
        onBack = onBack
    ) {
        if (state.rows.isEmpty() && !state.loading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.sprite_4),
                    contentDescription = stringResource(R.string.cd_blade),
                    contentScale = ContentScale.Fit,
                    alpha = 0.3f,
                    modifier = Modifier.size(96.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.saved_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.saved_empty_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    text = stringResource(R.string.cta_start),
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = state.rows, key = { row -> row.id }) { row ->
                    SetupRow(
                        row = row,
                        onOpen = { onOpenSetup(row.mode, row.id) },
                        onDelete = { onDeleteRequest(row.id) }
                    )
                }
            }
        }
    }

    if (state.pendingDeleteId != null) {
        ConfirmDeleteDialog(
            onConfirm = onDeleteConfirm,
            onDismiss = onDeleteCancel
        )
    }
}

@Composable
private fun SetupRow(
    row: SavedRow,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = HudTheme.colors
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        brackets = false,
        contentPadding = 12.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(
                    when (row.mode) {
                        CutMode.POLYGON -> R.drawable.sprite_1
                        CutMode.SEGMENT_RING -> R.drawable.sprite_2
                        CutMode.CORNER -> R.drawable.sprite_3
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(36.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = row.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.fmt_degrees, row.angleText),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.accent
                )
            }
            GhostIconButton(
                icon = ImageVector.vectorResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.cd_delete),
                onClick = onDelete,
                tint = colors.textSecondary
            )
        }
    }
}
