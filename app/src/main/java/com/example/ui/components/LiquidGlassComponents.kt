package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.LocalGlassColors

@Composable
fun LiquidGlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(glass.background)
            .drawBehind {
                val topLeftOrbColor = if (glass.isDark) {
                    glass.primary.copy(alpha = 0.22f)
                } else {
                    glass.primary.copy(alpha = 0.14f)
                }
                val midRightOrbColor = if (glass.isDark) {
                    glass.accent.copy(alpha = 0.18f)
                } else {
                    glass.accent.copy(alpha = 0.12f)
                }
                val bottomOrbColor = if (glass.isDark) {
                    Color(0xFF311068).copy(alpha = 0.25f)
                } else {
                    glass.primary.copy(alpha = 0.08f)
                }

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(topLeftOrbColor, Color.Transparent),
                        center = Offset(size.width * 0.15f, size.height * 0.12f),
                        radius = size.maxDimension * 0.52f
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.12f),
                    radius = size.maxDimension * 0.52f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(midRightOrbColor, Color.Transparent),
                        center = Offset(size.width * 0.88f, size.height * 0.34f),
                        radius = size.maxDimension * 0.45f
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.34f),
                    radius = size.maxDimension * 0.45f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(bottomOrbColor, Color.Transparent),
                        center = Offset(size.width * 0.35f, size.height * 0.88f),
                        radius = size.maxDimension * 0.5f
                    ),
                    center = Offset(size.width * 0.35f, size.height * 0.88f),
                    radius = size.maxDimension * 0.5f
                )
            },
        content = content
    )
}

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    elevated: Boolean = false,
    accentBorderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val glass = LocalGlassColors.current
    val shape: Shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(),
        label = "glassCardScale"
    )

    val fillBrush = Brush.linearGradient(
        colors = if (elevated) {
            listOf(
                glass.glassElevatedFill,
                glass.glassFill
            )
        } else {
            listOf(
                glass.glassFill.copy(alpha = glass.glassFill.alpha * 1.25f),
                glass.glassFill
            )
        }
    )

    val borderBrush = if (accentBorderColor != null) {
        Brush.linearGradient(
            colors = listOf(
                accentBorderColor.copy(alpha = 0.65f),
                glass.glassBorder
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                glass.glassHighlight,
                glass.glassBorder,
                glass.glassBorder.copy(alpha = 0.06f)
            )
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(brush = fillBrush, shape = shape)
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = androidx.compose.material3.ripple(),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun PrimaryGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = LocalGlassColors.current.primary
) {
    val animatedColor by animateColorAsState(
        targetValue = if (enabled) containerColor else containerColor.copy(alpha = 0.35f),
        label = "buttonColor"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedColor,
            contentColor = Color.White,
            disabledContainerColor = animatedColor,
            disabledContentColor = Color.White.copy(alpha = 0.55f)
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GlassOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    tint: Color = LocalGlassColors.current.textPrimary
) {
    val glass = LocalGlassColors.current
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, glass.glassBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = glass.glassFill,
            contentColor = tint
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun GlassStatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun GlassSkeletonBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp
) {
    val glass = LocalGlassColors.current
    val transition = rememberInfiniteTransition(label = "skeletonShimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                if (glass.isDark) Color.White.copy(alpha = alpha)
                else Color.Black.copy(alpha = alpha * 0.6f)
            )
            .border(1.dp, glass.glassBorder, RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun GlassEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    primaryActionText: String? = null,
    primaryActionIcon: ImageVector? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val glass = LocalGlassColors.current
    LiquidGlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_empty_vault_1794662455653),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(124.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, glass.glassBorder, RoundedCornerShape(24.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = glass.textPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = glass.textSecondary,
                textAlign = TextAlign.Center
            )
            if (primaryActionText != null && onPrimaryAction != null) {
                Spacer(modifier = Modifier.height(18.dp))
                PrimaryGlowButton(
                    text = primaryActionText,
                    icon = primaryActionIcon,
                    onClick = onPrimaryAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (secondaryActionText != null && onSecondaryAction != null) {
                Spacer(modifier = Modifier.height(10.dp))
                GlassOutlinedButton(
                    text = secondaryActionText,
                    onClick = onSecondaryAction,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
