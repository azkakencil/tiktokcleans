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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.CleaningSessionItemDetail
import com.example.domain.model.CleaningSessionSummary
import com.example.ui.AppStrings
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.RealTikTokThumbnail
import com.example.ui.components.statusVisuals
import com.example.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    strings: AppStrings,
    sessions: List<CleaningSessionSummary>,
    onViewDetails: (Long) -> Unit,
    onStartAnalysis: () -> Unit
) {
    val glass = LocalGlassColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevated = true
            ) {
                Column {
                    Text(
                        text = strings.cleaningHistory,
                        style = MaterialTheme.typography.headlineSmall,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${sessions.size} reports",
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary
                    )
                }
            }
        }

        if (sessions.isEmpty()) {
            item {
                GlassEmptyState(
                    title = strings.emptyHistoryTitle,
                    subtitle = strings.emptyHistorySubtitle,
                    primaryActionText = strings.smartDeleteAnalysis,
                    primaryActionIcon = Icons.Rounded.AutoAwesome,
                    onPrimaryAction = onStartAnalysis
                )
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                SessionSummaryCard(
                    session = session,
                    strings = strings,
                    onViewDetails = { onViewDetails(session.id) }
                )
            }
        }
    }
}

@Composable
fun HistoryDetailScreen(
    strings: AppStrings,
    session: CleaningSessionSummary?,
    items: List<CleaningSessionItemDetail>,
    hideThumbnails: Boolean,
    onBack: () -> Unit,
    onOpenInTikTok: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevated = true
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("history_detail_back")) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = glass.textPrimary
                            )
                        }
                        Text(
                            text = strings.cleaningReport,
                            style = MaterialTheme.typography.headlineSmall,
                            color = glass.textPrimary
                        )
                    }

                    session?.let { s ->
                        val dateLabel = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
                            .format(Date(s.createdAtMs))
                        Text(
                            text = "${s.category.name} • $dateLabel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )
                        Text(
                            text = "${strings.totalVtFound}: ${s.totalAnalyzed}  |  ${strings.totalVtSelected}: ${s.totalSelected}",
                            style = MaterialTheme.typography.labelLarge,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${strings.totalSucceeded}: ${s.totalSucceeded}  |  Manual: ${s.totalManualAction}  |  ${strings.totalFailed}: ${s.totalFailed}",
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                    }
                }
            }
        }

        items(items, key = { it.id }) { item ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                contentPadding = PaddingValues(12.dp)
            ) {
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
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (hideThumbnails) Modifier.blur(16.dp) else Modifier)
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
                            text = item.description.ifBlank { item.activityDateLabel },
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val (statusLabel, statusColor, statusIcon) = statusVisuals(item.finalStatus, strings, glass)
                        GlassStatusBadge(text = statusLabel, color = statusColor, icon = statusIcon)
                    }
                    IconButton(onClick = { onOpenInTikTok(item.videoUrl) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = strings.openInTikTok,
                            tint = glass.accent
                        )
                    }
                }
            }
        }
    }
}
