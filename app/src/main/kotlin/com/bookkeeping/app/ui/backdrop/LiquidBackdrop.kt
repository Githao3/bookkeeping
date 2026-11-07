package com.bookkeeping.app.ui.backdrop

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.intellij.lang.annotations.Language

@Language("AGSL")
private const val RoundedRectRefractionWithDispersionShader = """
uniform shader content;

uniform float2 size;
uniform float4 cornerRadii;
uniform float refractionHeight;
uniform float refractionAmount;
uniform float depthEffect;
uniform float chromaticAberration;

float radiusAt(float2 coord, float4 radii) {
    if (coord.x >= 0.0) {
        if (coord.y <= 0.0) return radii.y;
        else return radii.z;
    } else {
        if (coord.y <= 0.0) return radii.x;
        else return radii.w;
    }
}

float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    float outside = length(max(cornerCoord, 0.0)) - radius;
    float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
    return outside + inside;
}

float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
    float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
    if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
        return sign(coord) * normalize(max(cornerCoord, 0.0));
    } else {
        float gradX = step(cornerCoord.y, cornerCoord.x);
        return sign(coord) * float2(gradX, 1.0 - gradX);
    }
}

float circleMap(float x) {
    return 1.0 - sqrt(1.0 - x * x);
}

half4 main(float2 coord) {
    float2 halfSize = size * 0.5;
    float2 centeredCoord = coord - halfSize;
    float radius = radiusAt(coord, cornerRadii);
    
    float sd = sdRoundedRect(centeredCoord, halfSize, radius);
    if (-sd >= refractionHeight) {
        return content.eval(coord);
    }
    sd = min(sd, 0.0);
    
    float d = circleMap(1.0 - -sd / refractionHeight) * refractionAmount;
    float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
    float2 grad = normalize(gradSdRoundedRect(centeredCoord, halfSize, gradRadius) + depthEffect * normalize(centeredCoord));
    
    float2 refractedCoord = coord + d * grad;
    // Pure neutral optical refraction, completely zero chromatic dispersion
    return content.eval(refractedCoord);
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
object LiquidShaderHolder {
    val shader: android.graphics.RuntimeShader by lazy {
        android.graphics.RuntimeShader(RoundedRectRefractionWithDispersionShader)
    }
}

/**
 * Optical Liquid Glass modifier.
 * Applies authentic frosted translucent glass styling with soft ambient elevation shadow
 * and specular hairline border.
 * Crucially does NOT blur child content, keeping text and icons 100% needle-sharp.
 */
fun Modifier.opticalLiquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp,
    blurRadius: Float = 24f,
    refractionHeight: Float = 20f,
    refractionAmount: Float = 16f,
    alphaMultiplier: Float = 1.0f
): Modifier = composed {
    val isDark = isSystemInDarkTheme()

    this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x33000000) else Color(0x0C000000),
            spotColor = if (isDark) Color(0x4D000000) else Color(0x18000000)
        )
        .clip(shape)
        .background(
            if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xF01C212B).copy(alpha = 0.94f * alphaMultiplier),
                        Color(0xE6141820).copy(alpha = 0.90f * alphaMultiplier)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xF8FFFFFF).copy(alpha = 0.96f * alphaMultiplier),
                        Color(0xEFF8FAFC).copy(alpha = 0.92f * alphaMultiplier)
                    )
                )
            }
        )
        .border(
            width = 0.8.dp,
            brush = Brush.verticalGradient(
                listOf(
                    if (isDark) Color(0x38FFFFFF) else Color(0xC0FFFFFF),
                    if (isDark) Color(0x10FFFFFF) else Color(0x30FFFFFF),
                    if (isDark) Color(0x20FFFFFF) else Color(0x55FFFFFF)
                )
            ),
            shape = shape
        )
}

/**
 * Two-layer Liquid Glass Surface composable.
 * Isolates the AGSL refraction and dispersion shader to the background plane,
 * ensuring that any foreground content (text, icons, buttons) is rendered at 100% full
 * native sharpness and clarity.
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    elevation: Dp = 10.dp,
    surfaceAlpha: Float = 0.92f,
    enableShader: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val isTiramisuPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    Box(
        modifier = modifier.shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color(0x40000000) else Color(0x10000000),
            spotColor = if (isDark) Color(0x55000000) else Color(0x1F000000)
        )
    ) {
        // 1. Isolated Background Glass Layer (AGSL Lens Refraction & Dispersion)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(
                    if (enableShader && isTiramisuPlus) {
                        Modifier.graphicsLayer {
                            try {
                                val runtimeShader = LiquidShaderHolder.shader
                                val w = size.width
                                val h = size.height
                                val radius = if (shape == CircleShape) minOf(w, h) / 2f else 32f

                                runtimeShader.setFloatUniform("size", w, h)
                                runtimeShader.setFloatUniform("cornerRadii", radius, radius, radius, radius)
                                runtimeShader.setFloatUniform("refractionHeight", 22f)
                                runtimeShader.setFloatUniform("refractionAmount", 16f)
                                runtimeShader.setFloatUniform("depthEffect", 0f)
                                runtimeShader.setFloatUniform("chromaticAberration", 0.0f)

                                val refractionEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content")
                                val blurEffect = RenderEffect.createBlurEffect(16f, 16f, Shader.TileMode.CLAMP)
                                val chained = RenderEffect.createChainEffect(refractionEffect, blurEffect)

                                renderEffect = chained.asComposeRenderEffect()
                            } catch (_: Exception) {
                            }
                        }
                    } else Modifier
                )
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xD91E2430).copy(alpha = surfaceAlpha),
                                Color(0xC7131822).copy(alpha = surfaceAlpha * 0.95f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color(0xF2FFFFFF).copy(alpha = surfaceAlpha),
                                Color(0xE8F8FAFC).copy(alpha = surfaceAlpha * 0.92f)
                            )
                        )
                    }
                )
                .border(
                    width = 0.9.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isDark) Color(0x40FFFFFF) else Color(0xD0FFFFFF),
                            if (isDark) Color(0x14FFFFFF) else Color(0x28FFFFFF),
                            if (isDark) Color(0x25FFFFFF) else Color(0x50FFFFFF)
                        )
                    ),
                    shape = shape
                )
        )

        // 2. Foreground Content (100% razor sharp, zero blur)
        content()
    }
}
