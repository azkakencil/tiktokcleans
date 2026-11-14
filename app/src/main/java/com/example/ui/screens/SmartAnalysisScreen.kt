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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.ActivityCategory
import com.example.domain.model.AnalysisSummary
import com.example.domain.model.DateFilter
import com.example.domain.model.SortOrder
import com.example.domain.model.TikTokActivityItem
import com.example.ui.AnalysisFilterState
import com.example.ui.AnalysisRunState
import com.example.ui.AppStrings
import com.example.ui.components.GlassEmptyState
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassSkeletonBox
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.components.VideoActivityGridCard
import com.example.ui.components.VideoDetailModal
import com.example.ui.theme.LocalGlassColors

@Composable
fun SmartAnalysisScreen(
    strings: AppStrings,
    summary: AnalysisSummary,
    filterState: AnalysisFilterState,
    runState: AnalysisRunState,
    items: List<TikTokActivityItem>,
    allCreators: List<String>,
    onCategoryChange: (ActivityCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onCreatorFilterChange: (String?) -> Unit,
    onDateFilterChange: (DateFilter) -> Unit,
    onSortChange: (SortOrder) -> Unit,
    onToggleSelect: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onReanalyzeThumbnails: () -> Unit,
    onProceedToConfirmation: () -> Unit,
    onImportDataExport: () -> Unit,
    onPasteLinks: () -> Unit,
    onOpenInTikTok: (String) -> Unit
) {
    val glass = LocalGlassColors.current
    var previewItem by remember { mutableStateOf<TikTokActivityItem?>(null) }
    var showCreatorMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("smart_analysis_screen")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp,
                bottom = 104.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Analysis Dashboard Header
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    elevated = true
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.smartDeleteAnalysis,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = glass.textPrimary
                                )
                                Text(
                                    text = strings.officialApiBadge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = glass.accent
                                )
                            }
                            IconButton(
                                onClick = onReanalyzeThumbnails,
                                modifier = Modifier.testTag("reanalyze_thumbnails_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = "Refresh thumbnails",
                                    tint = glass.primary
                                )
                            }
                        }

                        if (runState.isAnalyzing) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                val fraction = if (runState.totalCount == 0) 0f
                                else runState.resolvedCount.toFloat() / runState.totalCount.toFloat()
                                LinearProgressIndicator(
                                    progress = { fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = glass.accent,
                                    trackColor = glass.glassBorder
                                )
                                Text(
                                    text = "oEmbed ${runState.resolvedCount}/${runState.totalCount} • ${runState.currentTitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = glass.textSecondary
                                )
                            }
                        }

                        // 5 Actual Analysis Metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalysisMetricBox(
                                label = strings.totalVtFound,
                                value = summary.totalFound.toString(),
                                color = glass.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            AnalysisMetricBox(
                                label = strings.totalVtSelected,
                                value = summary.totalSelected.toString(),
                                color = glass.primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnalysisMetricBox(
                                label = strings.readyToProcess,
                                value = summary.readyToProcess.toString(),
                                color = glass.success,
                                modifier = Modifier.weight(1f)
                            )
                            AnalysisMetricBox(
                                label = strings.failedToLoad,
                                value = summary.failedToLoad.toString(),
                                color = glass.danger,
                                modifier = Modifier.weight(1f)
                            )
                            AnalysisMetricBox(
                                label = strings.requiresManualAction,
                                value = summary.requiresManualAction.toString(),
                                color = glass.warning,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Category Switcher + Search + Filters + Bulk Select
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                listOf(
                                    ActivityCategory.ALL to "All",
                                    ActivityCategory.LIKES to "Likes",
                                    ActivityCategory.FAVORITES to "Favorites",
                                    ActivityCategory.REPOSTS to "Reposts"
                                )
                            ) { (cat, label) ->
                                FilterChip(
                                    selected = filterState.category == cat,
                                    onClick = { onCategoryChange(cat) },
                                    label = { Text(label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = glass.primary.copy(alpha = 0.28f),
                                        selectedLabelColor = glass.textPrimary
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = filterState.searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text(strings.searchPlaceholder) },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                if (filterState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analysis_search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = glass.primary,
                                unfocusedBorderColor = glass.glassBorder,
                                focusedTextColor = glass.textPrimary,
                                unfocusedTextColor = glass.textPrimary
                            )
                        )

                        // Creator, Date, Sort filter chips
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                Box {
                                    FilterChip(
                                        selected = filterState.creatorFilter != null,
                                        onClick = { showCreatorMenu = true },
                                        label = {
                                            Text(
                                                filterState.creatorFilter?.let { "@$it" }
                                                    ?: strings.allCreators
                                            )
                                        }
                                    )
                                    DropdownMenu(
                                        expanded = showCreatorMenu,
                                        onDismissRequest = { showCreatorMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text(strings.allCreators) },
                                            onClick = {
                                                onCreatorFilterChange(null)
                                                showCreatorMenu = false
                                            }
                                        )
                                        allCreators.forEach { creator ->
                                            DropdownMenuItem(
                                                text = { Text("@$creator") },
                                                onClick = {
                                                    onCreatorFilterChange(creator)
                                                    showCreatorMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            items(
                                listOf(
                                    DateFilter.ALL to strings.allDates,
                                    DateFilter.LAST_7_DAYS to strings.last7Days,
                                    DateFilter.LAST_30_DAYS to strings.last30Days,
                                    DateFilter.OLDER to strings.older
                                )
                            ) { (df, label) ->
                                FilterChip(
                                    selected = filterState.dateFilter == df,
                                    onClick = { onDateFilterChange(df) },
                                    label = { Text(label) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = filterState.sortOrder == SortOrder.NEWEST,
                                    onClick = {
                                        onSortChange(
                                            if (filterState.sortOrder == SortOrder.NEWEST) SortOrder.OLDEST
                                            else SortOrder.NEWEST
                                        )
                                    },
                                    label = {
                                        Text(
                                            if (filterState.sortOrder == SortOrder.NEWEST) strings.sortNewest
                                            else strings.sortOldest
                                        )
                                    }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassOutlinedButton(
                                text = strings.selectAll,
                                icon = Icons.Rounded.CheckCircle,
                                onClick = onSelectAll,
                                enabled = items.isNotEmpty(),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("analysis_select_all")
                            )
                            GlassOutlinedButton(
                                text = strings.deselectAll,
                                icon = Icons.Rounded.Close,
                                onClick = onDeselectAll,
                                enabled = summary.totalSelected > 0,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("analysis_deselect_all")
                            )
                        }
                    }
                }
            }

            if (runState.isAnalyzing && items.isEmpty()) {
                items(4) {
                    GlassSkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        cornerRadius = 22.dp
                    )
                }
            } else if (items.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    GlassEmptyState(
                        title = if (summary.totalFound == 0) strings.emptyActivityTitle else strings.noResultsTitle,
                        subtitle = if (summary.totalFound == 0) strings.emptyActivitySubtitle else strings.noResultsSubtitle,
                        primaryActionText = strings.importDataExport,
                        onPrimaryAction = onImportDataExport,
                        secondaryActionText = strings.pasteVideoLinks,
                        onSecondaryAction = onPasteLinks
                    )
                }
            } else {
                items(items, key = { it.id }) { item ->
                    VideoActivityGridCard(
                        item = item,
                        strings = strings,
                        onToggleSelect = { checked -> onToggleSelect(item.id, checked) },
                        onPreviewClick = { previewItem = item }
                    )
                }
            }
        }

        // Real-time Selected VT Bar -> Final Confirmation
        LiquidGlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            cornerRadius = 24.dp,
            elevated = true,
            accentBorderColor = if (summary.totalSelected > 0) glass.primary else null,
            contentPadding = PaddingValues(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${strings.totalVtSelected}: ${summary.totalSelected}",
                        style = MaterialTheme.typography.titleMedium,
                        color = glass.textPrimary
                    )
                    Text(
                        text = "L:${summary.likesSelected} • F:${summary.favoritesSelected} • R:${summary.repostsSelected}",
                        style = MaterialTheme.typography.labelSmall,
                        color = glass.textSecondary
                    )
                }
                PrimaryGlowButton(
                    text = strings.proceedToConfirmation,
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    enabled = summary.totalSelected > 0,
                    onClick = onProceedToConfirmation,
                    modifier = Modifier.testTag("proceed_to_confirm_button")
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

@Composable
private fun AnalysisMetricBox(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier,
        cornerRadius = 18.dp,
        contentPadding = PaddingValues(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = glass.textSecondary,
                maxLines = 2
            )
        }
    }
}
