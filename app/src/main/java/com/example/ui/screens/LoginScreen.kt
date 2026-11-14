package com.example.ui.screens

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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.AppStrings
import com.example.ui.components.GlassOutlinedButton
import com.example.ui.components.GlassStatusBadge
import com.example.ui.components.LiquidGlassBackground
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.PrimaryGlowButton
import com.example.ui.theme.LocalGlassColors

@Composable
fun LoginScreen(
    strings: AppStrings,
    isClientConfigured: Boolean,
    isAuthLoading: Boolean,
    onContinueWithTikTok: () -> Unit,
    onImportDataExport: () -> Unit,
    onPasteVideoLinks: () -> Unit,
    onContinueInspector: () -> Unit
) {
    val glass = LocalGlassColors.current
    LiquidGlassBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Hero Liquid Glass Artwork Header
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 28.dp,
                    elevated = true,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_liquid_glass_hero_1794662455034),
                            contentDescription = "TikTok Cleaner Liquid Glass backdrop",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_art_1794662452823),
                                contentDescription = "TikTok Cleaner Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .border(1.5.dp, glass.glassHighlight, RoundedCornerShape(20.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.appName,
                                style = MaterialTheme.typography.displaySmall,
                                color = glass.textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = strings.tagline,
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Official TikTok Login Kit OAuth 2.0 Card
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 26.dp,
                    contentPadding = PaddingValues(22.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GlassStatusBadge(
                                text = strings.officialApiBadge,
                                color = glass.accent,
                                icon = Icons.Rounded.VerifiedUser
                            )
                            GlassStatusBadge(
                                text = "OAuth 2.0 PKCE",
                                color = glass.primary,
                                icon = Icons.Rounded.Lock
                            )
                        }

                        Text(
                            text = strings.loginOAuthSubtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = glass.textSecondary
                        )

                        if (!isClientConfigured) {
                            LiquidGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                cornerRadius = 18.dp,
                                accentBorderColor = glass.warning,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text(
                                    text = strings.oauthMissingKeyNotice,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = glass.warning
                                )
                            }
                        }

                        if (isAuthLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = glass.primary)
                            }
                        } else {
                            PrimaryGlowButton(
                                text = strings.continueWithTikTok,
                                icon = Icons.Rounded.Shield,
                                onClick = onContinueWithTikTok,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("continue_with_tiktok_button")
                            )
                        }
                    }
                }

                // Official TikTok Data Export & Link Analysis Entry
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = strings.dataExportModeStatus,
                            style = MaterialTheme.typography.titleMedium,
                            color = glass.textPrimary
                        )
                        Text(
                            text = strings.emptyActivitySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = glass.textSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        GlassOutlinedButton(
                            text = strings.importDataExport,
                            icon = Icons.Rounded.UploadFile,
                            onClick = onImportDataExport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_import_json_button")
                        )
                        GlassOutlinedButton(
                            text = strings.pasteVideoLinks,
                            icon = Icons.Rounded.AddLink,
                            onClick = onPasteVideoLinks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_paste_links_button")
                        )
                        GlassOutlinedButton(
                            text = strings.dashboard,
                            onClick = onContinueInspector,
                            tint = glass.accent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_open_dashboard_button")
                        )
                    }
                }
            }
        }
    }
}
