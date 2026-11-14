package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.R
import com.example.data.remote.TikTokOwnVideo
import com.example.domain.model.ActivityCategory
import com.example.domain.model.CleaningSessionSummary
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.TikTokUserProfile
import com.example.ui.AppStrings
import com.example.ui.components.ApiLimitationGlassBanner
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.components.RealTikTokThumbnail
import com.example.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    strings: AppStrings,
    userProfile: TikTokUserProfile?,
    items: List<TikTokActivityItem>,
    ownVideos: List<TikTokOwnVideo>,
    totalCleanedCount: Int,
    recentSessions: List<CleaningSessionSummary>,
    onQuickClean: (ActivityCategory) -> Unit,
    onImportDataExport: () -> Unit,
    onPasteLinks: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSessionDetail: (Long) -> Unit,
    onOpenVideoUrl: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    val likesCount = items.count { it.category == ActivityCategory.LIKES }
    val favoritesCount = items.count { it.category == ActivityCategory.FAVORITES }
    val repostsCount = items.count { it.category == ActivityCategory.REPOSTS }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Welcome Hero Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!userProfile?.avatarUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = userProfile?.avatarUrl,
                                contentDescription = "TikTok profile photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, glass.primary, CircleShape)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_art_1794662452823),
                                contentDescription = "TikTok Cleaner avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, glass.glassHighlight, CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.welcomeBack,
                                style = MaterialTheme.typography.labelMedium,
                                color = glass.textSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (userProfile != null && !userProfile.isDataExportMode) {
                                        "@${userProfile.username}"
                                    } else {
                                        userProfile?.displayName ?: strings.dataExportModeStatus
                                    },
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = glass.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (userProfile?.isVerified == true) {
                                    Icon(
                                        imageVector = Icons.Rounded.Verified,
                                        contentDescription = "Verified",
                                        tint = glass.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            GlassStatusBadge(
                                text = if (userProfile?.isDataExportMode == false) {
                                    strings.connectedStatus
                                } else {
                                    strings.dataExportModeStatus
                                },
                                color = if (userProfile?.isDataExportMode == false) glass.success else glass.accent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryGlowButton(
                        text = strings.quickClean,
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = { onQuickClean(ActivityCategory.ALL) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("quick_clean_button")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = strings.importDataExport,
                            icon = Icons.Rounded.UploadFile,
                            onClick = onImportDataExport,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_import_json_button")
                        )
                        GlassOutlinedButton(
                            text = strings.pasteVideoLinks,
                            icon = Icons.Rounded.AddLink,
                            onClick = onPasteLinks,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dashboard_paste_links_button")
                        )
                    }
                }
            }
        }

        // Actual 4-Metric Glass Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatGlassTile(
                        title = strings.totalLikes,
                        value = likesCount.toString(),
                        icon = Icons.Rounded.Favorite,
                        accent = glass.danger,
                        onClick = { onQuickClean(ActivityCategory.LIKES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_likes")
                    )
                    StatGlassTile(
                        title = strings.totalFavorites,
                        value = favoritesCount.toString(),
                        icon = Icons.Rounded.Bookmark,
                        accent = glass.warning,
                        onClick = { onQuickClean(ActivityCategory.FAVORITES) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_favorites")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatGlassTile(
                        title = strings.totalReposts,
                        value = repostsCount.toString(),
                        icon = Icons.Rounded.Repeat,
                        accent = glass.accent,
                        onClick = { onQuickClean(ActivityCategory.REPOSTS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_reposts")
                    )
                    StatGlassTile(
                        title = strings.totalCleaned,
                        value = totalCleanedCount.toString(),
                        icon = Icons.Rounded.CleaningServices,
                        accent = glass.success,
                        onClick = onOpenHistory,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_total_cleaned")
                    )
                }
            }
        }

        // Real Activity Breakdown Chart Card
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = strings.accountActivityStats,
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val totalAll = (likesCount + favoritesCount + repostsCount).coerceAtLeast(1)
                    val likeRatio = likesCount.toFloat() / totalAll
                    val favRatio = favoritesCount.toFloat() / totalAll
                    val repostRatio = repostsCount.toFloat() / totalAll

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                    ) {
                        val h = size.height
                        val w = size.width
                        drawRoundRect(
                            color = glass.glassBorder,
                            cornerRadius = CornerRadius(h / 2f, h / 2f),
                            size = size
                        )
                        if (likesCount + favoritesCount + repostsCount > 0) {
                            var startX = 0f
                            val likeW = w * likeRatio
                            if (likeW > 0f) {
                                drawRoundRect(
                                    color = glass.danger,
                                    topLeft = Offset(startX, 0f),
                                    size = Size(likeW, h),
                                    cornerRadius = CornerRadius(h / 2f, h / 2f)
                                )
                                startX += likeW
                            }
                            val favW = w * favRatio
                            if (favW > 0f) {
                                drawRoundRect(
                                    color = glass.warning,
                                    topLeft = Offset(startX, 0f),
                                    size = Size(favW, h),
                                    cornerRadius = CornerRadius(h / 2f, h / 2f)
                                )
                                startX += favW
                            }
                            val repW = w * repostRatio
                            if (repW > 0f) {
                                drawRoundRect(
                                    color = glass.accent,
                                    topLeft = Offset(startX, 0f),
                                    size = Size(repW, h),
                                    cornerRadius = CornerRadius(h / 2f, h / 2f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ChartLegendDot("Likes: $likesCount", glass.danger)
                        ChartLegendDot("Favorites: $favoritesCount", glass.warning)
                        ChartLegendDot("Reposts: $repostsCount", glass.accent)
                    }
                }
            }
        }

        // Official TikTok Published Videos (only when OAuth returns real videos)
        if (ownVideos.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = strings.ownVideosSectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(ownVideos, key = { it.id }) { video ->
                            LiquidGlassCard(
                                modifier = Modifier.width(150.dp),
                                cornerRadius = 20.dp,
                                onClick = { if (video.shareUrl.isNotBlank()) onOpenVideoUrl(video.shareUrl) },
                                contentPadding = PaddingValues(10.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                    ) {
                                        RealTikTokThumbnail(
                                            thumbnailUrl = video.coverImageUrl,
                                            contentDescription = video.title,
                                            unavailableLabel = strings.thumbnailUnavailable,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = video.title.ifBlank { video.description.ifBlank { "TikTok Video" } },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = glass.textPrimary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Transparent API Scope Banner
        item {
            ApiLimitationGlassBanner(strings = strings)
        }

        // Recent Cleaning History Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentHistory,
                    style = MaterialTheme.typography.titleMedium,
                    color = glass.textPrimary
                )
                GlassOutlinedButton(
                    text = strings.cleaningHistory,
                    icon = Icons.Rounded.History,
                    onClick = onOpenHistory
                )
            }
        }

        if (recentSessions.isEmpty()) {
            item {
                GlassEmptyState(
                    title = strings.emptyHistoryTitle,
                    subtitle = strings.emptyHistorySubtitle,
                    primaryActionText = strings.smartDeleteAnalysis,
                    primaryActionIcon = Icons.Rounded.AutoAwesome,
                    onPrimaryAction = { onQuickClean(ActivityCategory.ALL) }
                )
            }
        } else {
            items(recentSessions.take(4), key = { it.id }) { session ->
                SessionSummaryCard(
                    session = session,
                    strings = strings,
                    onViewDetails = { onOpenSessionDetail(session.id) }
                )
            }
        }
    }
}

@Composable
private fun StatGlassTile(
    title: String,
    value: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 22.dp,
        accentBorderColor = accent.copy(alpha = 0.5f),
        onClick = onClick,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(22.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = glass.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = glass.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = glass.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartLegendDot(label: String, color: Color) {
    val glass = LocalGlassColors.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = glass.textSecondary
        )
    }
}

@Composable
fun SessionSummaryCard(
    session: CleaningSessionSummary,
    strings: AppStrings,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val dateLabel = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault())
        .format(Date(session.createdAtMs))

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("session_card_${session.id}"),
        cornerRadius = 22.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${session.category.name} • $dateLabel",
                        style = MaterialTheme.typography.labelMedium,
                        color = glass.textSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${session.totalSelected} VT • ${session.totalSucceeded} ${strings.totalSucceeded} • ${session.totalManualAction} Manual",
                        style = MaterialTheme.typography.titleSmall,
                        color = glass.textPrimary
                    )
                }
                GlassStatusBadge(
                    text = session.status.name.replace('_', ' '),
                    color = when (session.status) {
                        com.example.domain.model.CleaningProcessStatus.COMPLETED -> glass.success
                        com.example.domain.model.CleaningProcessStatus.CANCELLED -> glass.danger
                        else -> glass.warning
                    }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            GlassOutlinedButton(
                text = strings.viewDetails,
                onClick = onViewDetails,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("view_details_button_${session.id}")
            )
        }
    }
}
