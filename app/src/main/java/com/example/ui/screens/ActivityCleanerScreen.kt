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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.ActivityCategory
import com.example.domain.model.SortOrder
import com.example.domain.model.TikTokActivityItem
import com.example.ui.AppStrings
import com.example.ui.components.ApiLimitationGlassBanner
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.components.VideoActivityGridCard
import com.example.ui.components.VideoDetailModal
import com.example.ui.theme.LocalGlassColors

@Composable
fun ActivityCleanerScreen(
    category: ActivityCategory,
    strings: AppStrings,
    items: List<TikTokActivityItem>,
    onToggleSelect: (String, Boolean) -> Unit,
    onSelectAll: (Boolean) -> Unit,
    onImportDataExport: () -> Unit,
    onPasteLinks: () -> Unit,
    onRunSmartAnalysis: () -> Unit,
    onOpenInTikTok: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    var searchQuery by remember { mutableStateOf("") }
    var sortOrder by remember { mutableStateOf(SortOrder.NEWEST) }
    var previewItem by remember { mutableStateOf<TikTokActivityItem?>(null) }

    val categoryItems = remember(items, category, searchQuery, sortOrder) {
        items.filter { it.category == category }
            .filter { item ->
                val q = searchQuery.trim()
                q.isEmpty() || item.creatorUsername.contains(q, ignoreCase = true) ||
                    item.description.contains(q, ignoreCase = true)
            }
            .let { list ->
                if (sortOrder == SortOrder.NEWEST) list.sortedByDescending { it.activityTimestampMs }
                else list.sortedBy { it.activityTimestampMs }
            }
    }

    val selectedCount = categoryItems.count { it.isSelected }
    val title = when (category) {
        ActivityCategory.LIKES -> strings.likeCleaner
        ActivityCategory.FAVORITES -> strings.favoritesCleaner
        ActivityCategory.REPOSTS -> strings.repostCleaner
        ActivityCategory.ALL -> strings.smartDeleteAnalysis
    }
    val ctaLabel = when (category) {
        ActivityCategory.LIKES -> strings.unlikeSelected
        ActivityCategory.FAVORITES -> strings.removeSelected
        ActivityCategory.REPOSTS -> strings.undoRepost
        ActivityCategory.ALL -> strings.analyzeBeforeClean
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("activity_cleaner_${category.name}")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    elevated = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = glass.textPrimary
                        )
                        Text(
                            text = "${categoryItems.size} VT • $selectedCount ${strings.totalVtSelected}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(strings.searchPlaceholder) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Search, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("cleaner_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = glass.primary,
                                unfocusedBorderColor = glass.glassBorder,
                                focusedTextColor = glass.textPrimary,
                                unfocusedTextColor = glass.textPrimary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassOutlinedButton(
                                text = if (selectedCount == categoryItems.size && categoryItems.isNotEmpty()) {
                                    strings.deselectAll
                                } else {
                                    strings.selectAll
                                },
                                icon = Icons.Rounded.CheckBox,
                                enabled = categoryItems.isNotEmpty(),
                                onClick = {
                                    val allSelected = selectedCount == categoryItems.size && categoryItems.isNotEmpty()
                                    onSelectAll(!allSelected)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("cleaner_select_all_button")
                            )
                            GlassOutlinedButton(
                                text = if (sortOrder == SortOrder.NEWEST) strings.sortNewest else strings.sortOldest,
                                onClick = {
                                    sortOrder = if (sortOrder == SortOrder.NEWEST) SortOrder.OLDEST else SortOrder.NEWEST
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassOutlinedButton(
                                text = strings.importDataExport,
                                icon = Icons.Rounded.UploadFile,
                                onClick = onImportDataExport,
                                modifier = Modifier.weight(1f)
                            )
                            GlassOutlinedButton(
                                text = strings.pasteVideoLinks,
                                icon = Icons.Rounded.AddLink,
                                onClick = onPasteLinks,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                ApiLimitationGlassBanner(strings = strings)
            }

            if (categoryItems.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GlassEmptyState(
                        title = strings.emptyActivityTitle,
                        subtitle = strings.emptyActivitySubtitle,
                        primaryActionText = strings.importDataExport,
                        primaryActionIcon = Icons.Rounded.UploadFile,
                        onPrimaryAction = onImportDataExport,
                        secondaryActionText = strings.pasteVideoLinks,
                        onSecondaryAction = onPasteLinks
                    )
                }
            } else {
                items(categoryItems, key = { it.id }) { item ->
                    VideoActivityGridCard(
                        item = item,
                        strings = strings,
                        onToggleSelect = { checked -> onToggleSelect(item.id, checked) },
                        onPreviewClick = { previewItem = item }
                    )
                }
            }
        }

        // Mandatory Smart Delete Analysis sticky CTA (never deletes without analysis!)
        if (categoryItems.isNotEmpty()) {
            LiquidGlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp),
                cornerRadius = 24.dp,
                elevated = true,
                contentPadding = PaddingValues(12.dp)
            ) {
                PrimaryGlowButton(
                    text = "$ctaLabel ($selectedCount)",
                    icon = Icons.Rounded.AutoAwesome,
                    onClick = onRunSmartAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cleaner_analyze_cta")
                )
            }
        }
    }

    previewItem?.let { modalItem ->
        val latest = items.firstOrNull { it.id == modalItem.id } ?: modalItem
        VideoDetailModal(
            item = latest,
            strings = strings,
            onDismiss = { previewItem = null },
            onToggleSelect = { checked -> onToggleSelect(latest.id, checked) },
            onOpenInTikTok = onOpenInTikTok
        )
    }
}
