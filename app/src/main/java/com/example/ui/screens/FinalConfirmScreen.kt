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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.AnalysisSummary
import com.example.domain.model.TikTokActivityItem
import com.example.ui.AppStrings
import com.example.ui.components.CategoryPill
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.components.RealTikTokThumbnail
import com.example.ui.theme.LocalGlassColors

@Composable
fun FinalConfirmScreen(
    strings: AppStrings,
    summary: AnalysisSummary,
    selectedItems: List<TikTokActivityItem>,
    onCancel: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    val glass = LocalGlassColors.current
    var explicitConfirmed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("final_confirm_screen")
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 135.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 160.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    elevated = true,
                    accentBorderColor = glass.danger
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.WarningAmber,
                                contentDescription = null,
                                tint = glass.danger,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = strings.deleteConfirmationTitle,
                                style = MaterialTheme.typography.headlineSmall,
                                color = glass.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        ConfirmStatRow(strings.totalVtFound, summary.totalFound.toString())
                        ConfirmStatRow(strings.totalVtSelected, summary.totalSelected.toString())
                        ConfirmStatRow("Likes", summary.likesSelected.toString())
                        ConfirmStatRow("Favorites", summary.favoritesSelected.toString())
                        ConfirmStatRow("Reposts", summary.repostsSelected.toString())

                        Spacer(modifier = Modifier.height(4.dp))

                        LiquidGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            cornerRadius = 18.dp,
                            accentBorderColor = glass.warning,
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            Text(
                                text = strings.deleteConfirmationWarning,
                                style = MaterialTheme.typography.bodySmall,
                                color = glass.textPrimary
                            )
                        }
                    }
                }
            }

            items(selectedItems, key = { it.id }) { item ->
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 18.dp,
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            RealTikTokThumbnail(
                                thumbnailUrl = item.thumbnailUrl,
                                contentDescription = "@${item.creatorUsername}",
                                unavailableLabel = strings.thumbnailUnavailable,
                                modifier = Modifier.fillMaxSize()
                            )
                            CategoryPill(
                                category = item.category,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "@${item.creatorUsername}",
                            style = MaterialTheme.typography.labelMedium,
                            color = glass.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Bottom Explicit Consent & Confirm Action Bar
        LiquidGlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(14.dp),
            cornerRadius = 24.dp,
            elevated = true,
            contentPadding = PaddingValues(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = explicitConfirmed,
                        onCheckedChange = { explicitConfirmed = it },
                        modifier = Modifier.testTag("explicit_confirm_checkbox"),
                        colors = CheckboxDefaults.colors(checkedColor = glass.danger)
                    )
                    Text(
                        text = strings.explicitConfirmCheckbox,
                        style = MaterialTheme.typography.bodySmall,
                        color = glass.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassOutlinedButton(
                        text = strings.cancel,
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_cancel_button")
                    )
                    PrimaryGlowButton(
                        text = strings.confirmDelete,
                        icon = Icons.Rounded.DeleteForever,
                        enabled = explicitConfirmed && selectedItems.isNotEmpty(),
                        containerColor = glass.danger,
                        onClick = onConfirmDelete,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("confirm_delete_button")
                    )
                }
            }
        }
    }
}

@Composable
private fun ConfirmStatRow(label: String, value: String) {
    val glass = LocalGlassColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = glass.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = glass.textPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}
