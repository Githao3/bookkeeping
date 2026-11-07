package com.bookkeeping.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bookkeeping.app.ui.backdrop.opticalLiquidGlass

object LiquidGlassTokens {
    // Pure clean canvas background (Strict no-slop house style)
    val GroundLight = Color(0xFFF4F5F7)
    val GroundDark = Color(0xFF0D0E11)

    // Inset Card Surface
    val CardSurfaceLight = Color(0xFFFFFFFF)
    val CardSurfaceDark = Color(0xFF16181D)

    // Border hairlines
    val BorderLight = Color(0x0F000000)
    val BorderDark = Color(0x1FFFFFFF)

    // Glass Container tints
    val GlassContainerLight = Color(0xE6FFFFFF)
    val GlassContainerDark = Color(0xCC16181D)

    // Flat glass panel fill (LiquidBottomBar shell standard)
    val GlassPanelDark = Color(0xFF1E2430)
    val GlassPanelLight = Color(0xFFFFFFFF)

    // Inset track tints (search fields, segmented tracks, icon-less wells)
    val InsetLight = Color(0x0F000000)
    val InsetDark = Color(0x33FFFFFF)

    // Pill indicator fills (segmented selection / chips)
    val PillLight = Color(0xFFE8EAEE)
    val PillDark = Color(0xFF2C3340)

    // Pill border hairlines (brighter than card borders for on-pill specular)
    val PillBorderLight = Color(0x30FFFFFF)
    val PillBorderDark = Color(0x1AFFFFFF)

    // Primary Accents
    val AccentBlue = Color(0xFF007AFF)       // iOS System Blue
    val AccentEmerald = Color(0xFF00C853)    // Coolapk Emerald Green
    val AccentEmeraldDark = Color(0xFF00A844)
    val ExpenseRed = Color(0xFFE11D48)       // Refined Rose/Crimson
    val IncomeGreen = Color(0xFF10B981)      // Balanced Sage/Green

    // Typography Ink
    val TextPrimaryLight = Color(0xFF111827)
    val TextSecondaryLight = Color(0xFF6B7280)
    val TextTertiaryLight = Color(0xFF9CA3AF)

    val TextPrimaryDark = Color(0xFFF9FAFB)
    val TextSecondaryDark = Color(0xFF9CA3AF)
    val TextTertiaryDark = Color(0xFF6B7280)
}

/**
 * Optical Liquid Glass modifier with AGSL refraction and chromatic dispersion.
 */
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp,
    alphaMultiplier: Float = 1.0f
): Modifier {
    return this.opticalLiquidGlass(
        shape = shape,
        elevation = elevation,
        alphaMultiplier = alphaMultiplier
    )
}
