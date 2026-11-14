package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.domain.model.ActivityCategory
import com.example.domain.model.TikTokActivityItem
import com.example.domain.model.VideoItemStatus
import com.example.ui.AppStrings
import com.example.ui.theme.LocalGlassColors

@Composable
fun VideoActivityGridCard(
    item: TikTokActivityItem,
    strings: AppStrings,
    onToggleSelect: (Boolean) -> Unit,
    onPreviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    blurThumbnail: Boolean = false
) {
    val glass = LocalGlassColors.current
    val accentColor = if (item.isSelected) glass.primary else null

    LiquidGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_card_${item.id}"),
        cornerRadius = 22.dp,
        elevated = item.isSelected,
        accentBorderColor = accentColor,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(glass.secondarySurface)
                    .clickable(onClick = onPreviewClick)
                    .testTag("video_thumbnail_${item.id}")
            ) {
                RealTikTokThumbnail(
                    thumbnailUrl = item.thumbnailUrl,
                    contentDescription = "@${item.creatorUsername} video thumbnail",
                    unavailableLabel = strings.thumbnailUnavailable,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (blurThumbnail) Modifier.blur(16.dp) else Modifier)
                )

                // Top gradient overlay for readable badges
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.65f), Color.Transparent)
                            )
                        )
                )

                // Category chip top-left
                CategoryPill(
                    category = item.category,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )

                // Selection checkbox top-right (48dp touch area)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Checkbox(
                        checked = item.isSelected,
                        onCheckedChange = onToggleSelect,
                        modifier = Modifier.testTag("video_checkbox_${item.id}"),
                        colors = CheckboxDefaults.colors(
                            checkedColor = glass.primary,
                            uncheckedColor = Color.White.copy(alpha = 0.85f),
                            checkmarkColor = Color.White
                        )
                    )
                }

                // Bottom status badge
                val (statusLabel, statusColor, statusIcon) = statusVisuals(item.status, strings, glass)
                GlassStatusBadge(
                    text = statusLabel,
                    color = statusColor,
                    icon = statusIcon,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "@${item.creatorUsername}",
                style = MaterialTheme.typography.titleSmall,
                color = glass.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = glass.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = item.activityDateLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RealTikTokThumbnail(
    thumbnailUrl: String?,
    contentDescription: String,
    unavailableLabel: String,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    if (thumbnailUrl.isNullOrBlank()) {
        ThumbnailUnavailableView(label = unavailableLabel, modifier = modifier)
    } else {
        SubcomposeAsyncImage(
            model = thumbnailUrl,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            loading = {
                GlassSkeletonBox(
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 16.dp
                )
            },
            error = {
                ThumbnailUnavailableView(
                    label = unavailableLabel,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(glass.secondarySurface)
                )
            }
        )
    }
}

@Composable
private fun ThumbnailUnavailableView(
    label: String,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    Box(
        modifier = modifier
            .background(glass.secondarySurface)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.BrokenImage,
                contentDescription = label,
                tint = glass.textMuted,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = glass.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CategoryPill(
    category: ActivityCategory,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    val (label, icon, color) = when (category) {
        ActivityCategory.LIKES -> Triple("Like", Icons.Rounded.Favorite, glass.danger)
        ActivityCategory.FAVORITES -> Triple("Favorite", Icons.Rounded.Bookmark, glass.warning)
        ActivityCategory.REPOSTS -> Triple("Repost", Icons.Rounded.Repeat, glass.accent)
        ActivityCategory.ALL -> Triple("All", Icons.Rounded.Verified, glass.primary)
    }
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

fun statusVisuals(
    status: VideoItemStatus,
    strings: AppStrings,
    glass: com.example.ui.theme.LiquidGlassColors
): Triple<String, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    return when (status) {
        VideoItemStatus.LOADED -> Triple(strings.loadedStatus, glass.success, Icons.Rounded.CheckCircle)
        VideoItemStatus.VERIFIED_CLEANED -> Triple(strings.verifiedCleanedStatus, glass.success, Icons.Rounded.Verified)
        VideoItemStatus.THUMBNAIL_UNAVAILABLE -> Triple(strings.thumbnailUnavailable, glass.danger, Icons.Rounded.BrokenImage)
        VideoItemStatus.REQUIRES_MANUAL_ACTION -> Triple(strings.requiresManualAction, glass.warning, Icons.Rounded.TouchApp)
        VideoItemStatus.FAILED -> Triple(strings.failedToLoad, glass.danger, Icons.Rounded.BrokenImage)
        VideoItemStatus.PENDING_ANALYSIS -> Triple("Analyzing", glass.accent, Icons.Rounded.Schedule)
    }
}

@Composable
fun VideoDetailModal(
    item: TikTokActivityItem,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onToggleSelect: (Boolean) -> Unit,
    onOpenInTikTok: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(onClick = onDismiss)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {}
                    .testTag("video_detail_modal"),
                cornerRadius = 28.dp,
                elevated = true,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CategoryPill(category = item.category)
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = strings.cancel,
                                tint = glass.textPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, glass.glassBorder, RoundedCornerShape(20.dp))
                    ) {
                        RealTikTokThumbnail(
                            thumbnailUrl = item.thumbnailUrl,
                            contentDescription = "@${item.creatorUsername} preview",
                            unavailableLabel = strings.thumbnailUnavailable,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "@${item.creatorUsername}",
                        style = MaterialTheme.typography.titleLarge,
                        color = glass.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.description.ifBlank { item.videoUrl },
                        style = MaterialTheme.typography.bodyMedium,
                        color = glass.textSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.activityDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textMuted
                        )
                        val (statusLabel, statusColor, statusIcon) = statusVisuals(item.status, strings, glass)
                        GlassStatusBadge(text = statusLabel, color = statusColor, icon = statusIcon)
                    }

                    if (!item.errorReason.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.errorReason,
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.warning
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GlassOutlinedButton(
                            text = strings.openInTikTok,
                            icon = Icons.AutoMirrored.Rounded.OpenInNew,
                            onClick = { onOpenInTikTok(item.videoUrl) },
                            modifier = Modifier.weight(1f)
                        )
                        PrimaryGlowButton(
                            text = if (item.isSelected) strings.deselectAll else "Select VT",
                            onClick = { onToggleSelect(!item.isSelected) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PasteLinksDialog(
    initialCategory: ActivityCategory,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSubmit: (String, ActivityCategory) -> Unit
) {
    val glass = LocalGlassColors.current
    var linksText by remember { mutableStateOf("") }
    var selectedCategory by remember {
        mutableStateOf(if (initialCategory == ActivityCategory.ALL) ActivityCategory.LIKES else initialCategory)
    }

    Dialog(onDismissRequest = onDismiss) {
        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("paste_links_dialog"),
            cornerRadius = 26.dp,
            elevated = true
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.pasteLinksDialogTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = strings.pasteLinksDialogHint,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ActivityCategory.LIKES to "Likes",
                        ActivityCategory.FAVORITES to "Favorites",
                        ActivityCategory.REPOSTS to "Reposts"
                    ).forEach { (cat, label) ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = glass.primary.copy(alpha = 0.25f),
                                selectedLabelColor = glass.textPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = linksText,
                    onValueChange = { linksText = it },
                    placeholder = { Text("https://www.tiktok.com/@creator/video/...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_links_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = glass.primary,
                        unfocusedBorderColor = glass.glassBorder,
                        focusedTextColor = glass.textPrimary,
                        unfocusedTextColor = glass.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = strings.cancel,
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryGlowButton(
                        text = strings.analyzeLinksBtn,
                        enabled = linksText.isNotBlank(),
                        onClick = { onSubmit(linksText, selectedCategory) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("submit_links_button")
                    )
                }
            }
        }
    }
}

@Composable
fun ApiLimitationGlassBanner(
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        accentBorderColor = glass.accent,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = glass.accent,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.apiLimitationBannerTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = glass.textPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.apiLimitationBannerBody,
                    style = MaterialTheme.typography.bodySmall,
                    color = glass.textSecondary
                )
            }
        }
    }
}
