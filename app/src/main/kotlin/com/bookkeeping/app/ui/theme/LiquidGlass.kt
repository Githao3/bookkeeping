package com.bookkeeping.app.ui.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object LiquidGlassDefaults {
    // Light mode frosted liquid glass colors
    val GlassLightTop = Color(0xE6FFFFFF)      // 90% white top reflection
    val GlassLightBottom = Color(0xB8F0F5FA)   // 72% light cool white
    val GlassBorderLight = listOf(
        Color(0xEEFFFFFF),                     // Top specular highlight
        Color(0x66FFFFFF),                     // Mid-translucent
        Color(0x20FFFFFF),                     // Lower edge
        Color(0x40FFFFFF)                      // Bottom subtle bevel
    )

    // Dark mode frosted liquid glass colors
    val GlassDarkTop = Color(0xD9242933)
    val GlassDarkBottom = Color(0xB3181B22)
    val GlassBorderDark = listOf(
        Color(0x66FFFFFF),
        Color(0x26FFFFFF),
        Color(0x10000000),
        Color(0x33FFFFFF)
    )

    // Chromatic Aberration (Rainbow edge reflection inspired by liquid lens)
    val RainbowColors = listOf(
        Color(0x66FF5E7E), // Pink
        Color(0x66FFA940), // Orange
        Color(0x6652C41A), // Green
        Color(0x661890FF), // Blue
        Color(0x66722ED1), // Purple
        Color(0x66FF5E7E)  // Loop
    )
}

/**
 * Applies a luxurious iOS Liquid Glass styling:
 * 1. Soft diffused shadow
 * 2. Translucent frosted gradient background
 * 3. Overhead specular highlight rim border
 * 4. Optional subtle chromatic rainbow refraction highlight
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    elevation: Dp = 10.dp,
    hasRainbowRim: Boolean = false,
    alphaMultiplier: Float = 1.0f
): Modifier = composed {
    val isDark = isSystemInDarkTheme()

    val bgBrush = if (isDark) {
        Brush.verticalGradient(
            listOf(
                LiquidGlassDefaults.GlassDarkTop.copy(alpha = 0.85f * alphaMultiplier),
                LiquidGlassDefaults.GlassDarkBottom.copy(alpha = 0.70f * alphaMultiplier)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                LiquidGlassDefaults.GlassLightTop.copy(alpha = 0.88f * alphaMultiplier),
                LiquidGlassDefaults.GlassLightBottom.copy(alpha = 0.72f * alphaMultiplier)
            )
        )
    }

    val borderBrush = if (hasRainbowRim) {
        Brush.sweepGradient(LiquidGlassDefaults.RainbowColors)
    } else {
        val borderColors = if (isDark) LiquidGlassDefaults.GlassBorderDark else LiquidGlassDefaults.GlassBorderLight
        Brush.verticalGradient(borderColors)
    }

    val shadowColor = if (isDark) Color(0x66000000) else Color(0x1A1B2836)

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            clip = false,
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .clip(shape)
        .background(bgBrush)
        .border(
            width = if (hasRainbowRim) 1.5.dp else 1.0.dp,
            brush = borderBrush,
            shape = shape
        )
}

/**
 * Animated soft ambient background mesh that brings translucent glass to life
 */
@Composable
fun AmbientAtmosphereBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseBg = if (isDark) Color(0xFF0F1115) else Color(0xFFF6F8FC)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseBg)
    ) {
        // Decorative glowing ambient spots shining through liquid glass
        val blob1 = if (isDark) Color(0x1F3B82F6) else Color(0x2860A5FA) // soft blue
        val blob2 = if (isDark) Color(0x1FE11D48) else Color(0x22F472B6) // soft rose
        val blob3 = if (isDark) Color(0x1F10B981) else Color(0x2234D399) // soft mint

        // Top right glow blob
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    drawContent()
                    // Top ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blob1, Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.12f),
                            radius = size.width * 0.65f
                        )
                    )
                    // Mid ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blob2, Color.Transparent),
                            center = Offset(size.width * 0.10f, size.height * 0.40f),
                            radius = size.width * 0.60f
                        )
                    )
                    // Bottom ambient glow for navigation bar
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(blob3, Color.Transparent),
                            center = Offset(size.width * 0.50f, size.height * 0.92f),
                            radius = size.width * 0.55f
                        )
                    )
                }
        )

        content()
    }
}
