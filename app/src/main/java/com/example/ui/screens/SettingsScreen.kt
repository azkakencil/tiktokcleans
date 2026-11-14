package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.domain.model.TikTokUserProfile
import com.example.ui.AppStrings
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.theme.LocalGlassColors

@Composable
fun SettingsScreen(
    strings: AppStrings,
    isDarkMode: Boolean,
    language: AppLanguage,
    hideHistoryThumbnails: Boolean,
    userProfile: TikTokUserProfile?,
    onToggleDarkMode: (Boolean) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onToggleHideHistoryThumbs: (Boolean) -> Unit,
    onConnectTikTok: () -> Unit,
    onDeleteLocalData: () -> Unit,
    onDisconnectAccount: () -> Unit,
    onLogout: () -> Unit
) {
    val glass = LocalGlassColors.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                elevated = true
            ) {
                Text(
                    text = strings.settings,
                    style = MaterialTheme.typography.headlineSmall,
                    color = glass.textPrimary
                )
            }
        }

        // Appearance & Language
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DarkMode,
                                contentDescription = null,
                                tint = glass.primary
                            )
                            Text(
                                text = if (isDarkMode) strings.darkMode else strings.lightMode,
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textPrimary
                            )
                        }
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = onToggleDarkMode,
                            modifier = Modifier.testTag("dark_mode_switch"),
                            colors = SwitchDefaults.colors(checkedTrackColor = glass.primary)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = glass.accent
                            )
                            Text(
                                text = strings.languageLabel,
                                style = MaterialTheme.typography.titleSmall,
                                color = glass.textPrimary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = language == AppLanguage.ID,
                                onClick = { onLanguageChange(AppLanguage.ID) },
                                label = { Text("Indonesia") },
                                modifier = Modifier.testTag("lang_id_chip")
                            )
                            FilterChip(
                                selected = language == AppLanguage.EN,
                                onClick = { onLanguageChange(AppLanguage.EN) },
                                label = { Text("English") },
                                modifier = Modifier.testTag("lang_en_chip")
                            )
                        }
                    }
                }
            }
        }

        // Account Connection
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VerifiedUser,
                            contentDescription = null,
                            tint = glass.accent
                        )
                        Text(
                            text = strings.accountConnection,
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary
                        )
                    }
                    GlassStatusBadge(
                        text = if (userProfile?.isDataExportMode == false) {
                            "${strings.connectedStatus} (@${userProfile.username})"
                        } else {
                            strings.dataExportModeStatus
                        },
                        color = if (userProfile?.isDataExportMode == false) glass.success else glass.accent
                    )
                    if (userProfile == null || userProfile.isDataExportMode) {
                        PrimaryGlowButton(
                            text = strings.continueWithTikTok,
                            onClick = onConnectTikTok,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Privacy Settings & Local Data
        item {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PrivacyTip,
                            contentDescription = null,
                            tint = glass.primary
                        )
                        Text(
                            text = strings.privacySettings,
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.hideThumbnailsInHistory,
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = hideHistoryThumbnails,
                            onCheckedChange = onToggleHideHistoryThumbs,
                            modifier = Modifier.testTag("hide_thumbs_switch"),
                            colors = SwitchDefaults.colors(checkedTrackColor = glass.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    GlassOutlinedButton(
                        text = strings.deleteLocalData,
                        icon = Icons.Rounded.DeleteSweep,
                        tint = glass.warning,
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_local_data_button")
                    )
                    GlassOutlinedButton(
                        text = strings.disconnectAccount,
                        icon = Icons.Rounded.LinkOff,
                        tint = glass.danger,
                        onClick = onDisconnectAccount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("disconnect_account_button")
                    )
                    GlassOutlinedButton(
                        text = strings.logout,
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        tint = glass.danger,
                        onClick = onLogout,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("logout_button")
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteLocalData) },
            text = { Text(strings.deleteLocalDataConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteLocalData()
                    }
                ) {
                    Text(strings.deleteLocalData, color = glass.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
