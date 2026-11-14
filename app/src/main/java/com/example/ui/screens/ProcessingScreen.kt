package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.VideoItemStatus
import com.example.ui.AppStrings
import com.example.ui.ProcessingUiState
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.components.RealTikTokThumbnail
import com.example.ui.components.statusVisuals
import com.example.ui.theme.LocalGlassColors

@Composable
fun ProcessingScreen(
    strings: AppStrings,
    state: ProcessingUiState,
    onCancelProcess: () -> Unit,
    onOpenInTikTok: (String) -> Unit,
    onMarkItemDone: (String) -> Unit,
    onSaveAndFinish: () -> Unit
) {
    val glass = LocalGlassColors.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("processing_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = strings.processingTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        color = glass.textPrimary
                    )

                    LinearProgressIndicator(
                        progress = { state.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = glass.primary,
                        trackColor = glass.glassBorder
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProcessStatChip(
                            label = strings.totalVtProcessed,
                            value = state.processedCount.toString(),
                            color = glass.accent,
                            modifier = Modifier.weight(1f)
                        )
                        ProcessStatChip(
                            label = strings.totalSucceeded,
                            value = state.succeededCount.toString(),
                            color = glass.success,
                            modifier = Modifier.weight(1f)
                        )
                        ProcessStatChip(
                            label = strings.totalFailed,
                            value = state.failedCount.toString(),
                            color = glass.danger,
                            modifier = Modifier.weight(1f)
                        )
                        ProcessStatChip(
                            label = strings.totalRemaining,
                            value = state.remainingCount.toString(),
                            color = glass.warning,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    state.currentItem?.takeIf { state.isProcessing }?.let { active ->
                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            accentBorderColor = glass.accent,
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    RealTikTokThumbnail(
                                        thumbnailUrl = active.thumbnailUrl,
                                        contentDescription = active.creatorUsername,
                                        unavailableLabel = strings.thumbnailUnavailable,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "@${active.creatorUsername}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = glass.textPrimary
                                    )
                                    Text(
                                        text = "Checking TikTok API capability...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = glass.accent
                                    )
                                }
                            }
                        }
                    }

                    if (state.isProcessing) {
                        GlassOutlinedButton(
                            text = strings.cancelProcess,
                            icon = Icons.Rounded.Cancel,
                            tint = glass.danger,
                            onClick = onCancelProcess,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cancel_process_button")
                        )
                    } else {
                        PrimaryGlowButton(
                            text = strings.saveReportAndFinish,
                            icon = Icons.Rounded.Save,
                            onClick = onSaveAndFinish,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_report_button")
                        )
                    }
                }
            }
        }

        // Transparent Manual Action Guidance Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 22.dp,
                accentBorderColor = glass.warning
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TouchApp,
                        contentDescription = null,
                        tint = glass.warning,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = strings.manualActionGuideTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = glass.textPrimary
                        )
                        Text(
                            text = strings.manualActionGuideBody,
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                }
            }
        }

        items(state.queue, key = { it.id }) { item ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            RealTikTokThumbnail(
                                thumbnailUrl = item.thumbnailUrl,
                                contentDescription = item.creatorUsername,
                                unavailableLabel = strings.thumbnailUnavailable,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "@${item.creatorUsername}",
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.description.ifBlank { item.videoUrl },
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val (statusLabel, statusColor, statusIcon) = statusVisuals(item.status, strings, glass)
                            GlassStatusBadge(text = statusLabel, color = statusColor, icon = statusIcon)
                        }
                    }

                    if (item.status == VideoItemStatus.REQUIRES_MANUAL_ACTION) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassOutlinedButton(
                                text = strings.openInTikTok,
                                icon = Icons.AutoMirrored.Rounded.OpenInNew,
                                onClick = { onOpenInTikTok(item.videoUrl) },
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryGlowButton(
                                text = strings.markCleanedVerified,
                                icon = Icons.Rounded.CheckCircle,
                                containerColor = glass.success,
                                onClick = { onMarkItemDone(item.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mark_done_${item.id}")
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessStatChip(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        contentPadding = PaddingValues(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = glass.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
