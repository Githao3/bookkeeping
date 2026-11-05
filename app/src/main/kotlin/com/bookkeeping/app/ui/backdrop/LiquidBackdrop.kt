package com.bookkeeping.app.ui.backdrop

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
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
    float dispersionIntensity = chromaticAberration * ((centeredCoord.x * centeredCoord.y) / (halfSize.x * halfSize.y));
    float2 dispersedCoord = d * grad * dispersionIntensity;
    
    half4 color = half4(0.0);
    
    half4 red = content.eval(refractedCoord + dispersedCoord);
    color.r += red.r / 3.5;
    color.a += red.a / 7.0;
    
    half4 orange = content.eval(refractedCoord + dispersedCoord * (2.0 / 3.0));
    color.r += orange.r / 3.5;
    color.g += orange.g / 7.0;
    color.a += orange.a / 7.0;
    
    half4 yellow = content.eval(refractedCoord + dispersedCoord * (1.0 / 3.0));
    color.r += yellow.r / 3.5;
    color.g += yellow.g / 3.5;
    color.a += yellow.a / 7.0;
    
    half4 green = content.eval(refractedCoord);
    color.g += green.g / 3.5;
    color.a += green.a / 7.0;
    
    half4 cyan = content.eval(refractedCoord - dispersedCoord * (1.0 / 3.0));
    color.g += cyan.g / 3.5;
    color.b += cyan.b / 3.0;
    color.a += cyan.a / 7.0;
    
    half4 blue = content.eval(refractedCoord - dispersedCoord * (2.0 / 3.0));
    color.b += blue.b / 3.0;
    color.a += blue.a / 7.0;
    
    half4 purple = content.eval(refractedCoord - dispersedCoord);
    color.r += purple.r / 7.0;
    color.b += purple.b / 3.0;
    color.a += purple.a / 7.0;
    
    return color;
}
"""

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private object LiquidShaderHolder {
    val shader: android.graphics.RuntimeShader by lazy {
        android.graphics.RuntimeShader(RoundedRectRefractionWithDispersionShader)
    }
}

/**
 * Real optical liquid glass modifier.
 * On Android 13+ / 14 / 15 / 16 / 17:
 *   Evaluates hardware AGSL RuntimeShader with real signed-distance-field lens refraction
 *   and chromatic dispersion.
 * On older platforms:
 *   Gracefully applies hardware-accelerated translucent frosted glass surface & specular reflection.
 */
fun Modifier.opticalLiquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 8.dp,
    blurRadius: Float = 32f,
    refractionHeight: Float = 24f,
    refractionAmount: Float = 18f,
    alphaMultiplier: Float = 1.0f
): Modifier = composed {
    val isTiramisuPlus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    val baseModifier = this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = Color(0x12000000),
            spotColor = Color(0x22000000)
        )
        .clip(shape)

    if (isTiramisuPlus) {
        baseModifier
            .graphicsLayer {
                try {
                    val runtimeShader = LiquidShaderHolder.shader
                    val w = size.width
                    val h = size.height
                    val radius = if (shape == CircleShape) minOf(w, h) / 2f else 32f

                    runtimeShader.setFloatUniform("size", w, h)
                    runtimeShader.setFloatUniform("cornerRadii", radius, radius, radius, radius)
                    runtimeShader.setFloatUniform("refractionHeight", refractionHeight)
                    runtimeShader.setFloatUniform("refractionAmount", refractionAmount)
                    runtimeShader.setFloatUniform("depthEffect", 0f)
                    runtimeShader.setFloatUniform("chromaticAberration", 1.0f)

                    val refractionEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content")
                    val blurEffect = RenderEffect.createBlurEffect(blurRadius, blurRadius, Shader.TileMode.CLAMP)
                    val chained = RenderEffect.createChainEffect(refractionEffect, blurEffect)

                    renderEffect = chained.asComposeRenderEffect()
                } catch (_: Exception) {
                    // Graceful fallback if device shader compilation fails
                }
            }
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xE6FFFFFF).copy(alpha = 0.85f * alphaMultiplier),
                        Color(0xDDF8FAFC).copy(alpha = 0.75f * alphaMultiplier)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x99FFFFFF),
                        Color(0x20FFFFFF),
                        Color(0x40FFFFFF)
                    )
                ),
                shape = shape
            )
    } else {
        baseModifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF5FFFFFF).copy(alpha = 0.95f * alphaMultiplier),
                        Color(0xEBF8FAFC).copy(alpha = 0.90f * alphaMultiplier)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x99FFFFFF),
                        Color(0x20FFFFFF),
                        Color(0x40FFFFFF)
                    )
                ),
                shape = shape
            )
    }
}
