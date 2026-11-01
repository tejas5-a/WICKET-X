package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.ChampagneHighlight
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.GlassSurfaceElevated
import com.example.ui.theme.MintBorder
import com.example.ui.theme.MintPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * Atmospheric Deep Midnight backdrop with subtle dual ambient glow spots (Mint Cyan & Champagne Gold)
 * to provide light refraction through the translucent glass cards.
 */
@Composable
fun GlassScaffoldBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .drawBehind {
                // Subtle Mint ambient aura in top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(MintPrimary.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(size.width * 0.9f, size.height * 0.08f),
                        radius = size.width * 0.8f
                    )
                )
                // Warm Champagne ambient aura in bottom-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(ChampagneHighlight.copy(alpha = 0.05f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.75f),
                        radius = size.width * 0.85f
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Translucent frosted glass card with subtle hairline specular border, responsive width, and internal padding.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderColor: Color = GlassBorder,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = GlassSurface,
    isMintHighlighted: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val finalBorderColor = if (isMintHighlighted) MintBorder else borderColor
    val finalBackground = if (isMintHighlighted) {
        Brush.linearGradient(
            colors = listOf(
                MintPrimary.copy(alpha = 0.14f),
                GlassSurface
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                backgroundColor,
                Color(0x0AFFFFFF)
            )
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(finalBackground)
            .border(borderWidth, finalBorderColor, shape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * High-contrast mint primary action button with neon aura and flexible responsive height.
 */
@Composable
fun MintGlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(14.dp),
    icon: ImageVector? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.97f else 1f, label = "buttonScale")

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MintPrimary,
            contentColor = BackgroundDark,
            disabledContainerColor = GlassSurface,
            disabledContentColor = TextMuted
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .scale(scale)
            .heightIn(min = 48.dp)
            .border(1.dp, MintPrimary.copy(alpha = 0.8f), shape)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = BackgroundDark,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = if (enabled) BackgroundDark else TextMuted,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Translucent secondary glass button with frosted border and flexible responsive height.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(14.dp),
    icon: ImageVector? = null,
    textColor: Color = TextPrimary,
    borderColor: Color = GlassBorder,
    testTag: String = ""
) {
    Surface(
        onClick = onClick,
        shape = shape,
        color = GlassSurface,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Glass pill badge for stats, over indicators, live tags, and status pills.
 */
@Composable
fun GlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MintPrimary,
    textColor: Color = color,
    backgroundColor: Color = (if (textColor != MintPrimary) textColor else color).copy(alpha = 0.15f),
    borderColor: Color = (if (textColor != MintPrimary) textColor else color).copy(alpha = 0.35f),
    shape: Shape = RoundedCornerShape(50.dp)
) {
    Surface(
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Large tactile glass keypad button for scoring engine (0, 1, 2, 3, 4, 5, 6).
 */
@Composable
fun GlassKeypadButton(
    modifier: Modifier = Modifier,
    text: String = "",
    label: String = text,
    tag: String = "",
    isBoundary: Boolean = false,
    isMintAccent: Boolean = isBoundary,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.94f else 1f, label = "keypadScale")

    val bg = if (isMintAccent) {
        Brush.verticalGradient(
            colors = listOf(
                MintPrimary.copy(alpha = 0.28f),
                MintPrimary.copy(alpha = 0.12f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                GlassSurfaceElevated,
                GlassSurface
            )
        )
    }

    val borderStrokeColor = if (isMintAccent) MintPrimary else GlassBorder

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .scale(scale)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderStrokeColor, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .then(if (tag.isNotBlank()) Modifier.testTag(tag) else Modifier)
    ) {
        Text(
            text = label.ifBlank { text },
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (isMintAccent) MintPrimary else TextPrimary
        )
    }
}
