package com.bookkeeping.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

/**
 * 玻璃数字键盘（4×4）：数字键凹陷轨道底 + 发丝描边，完成键翡翠实心。
 * 计算器键（+/−/清空/⌫）视觉弱化为次要色。
 */
@Composable
fun NumberPad(onDigitClick: (String) -> Unit, onDeleteClick: () -> Unit, onClearClick: () -> Unit,
    onCompleteClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val haptic = LocalHapticFeedback.current
    fun tap() { haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }

    Column(modifier.fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            listOf("1", "2", "3", "⌫"),
            listOf("4", "5", "6", "+"),
            listOf("7", "8", "9", "−"),
            listOf(".", "0", "C", "完成")
        ).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    val onClick = {
                        tap()
                        when (key) {
                            "⌫" -> onDeleteClick()
                            "C" -> onClearClick()
                            "完成" -> onCompleteClick()
                            else -> onDigitClick(key)
                        }
                    }
                    when (key) {
                        "⌫" -> GlassKey(icon = Icons.AutoMirrored.Filled.Backspace, enabled = enabled, onClick = onClick)
                        "完成" -> GlassKey(label = key, emphasized = true, enabled = enabled, onClick = onClick)
                        else -> GlassKey(label = if (key == "C") "清空" else key, enabled = enabled, onClick = onClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.GlassKey(
    label: String? = null,
    icon: ImageVector? = null,
    emphasized: Boolean = false,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassKeyPadPress"
    )
    val accent = glassAccent()
    val disabledAlpha = if (enabled) 1f else 0.4f
    Box(
        modifier = Modifier
            .weight(1f)
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    emphasized -> accent
                    else -> if (isDark) LiquidGlassTokens.InsetDark else LiquidGlassTokens.InsetLight
                }
            )
            .border(
                0.6.dp,
                if (emphasized) Color.Transparent else hairline(),
                RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            icon != null -> Icon(
                imageVector = icon,
                contentDescription = "删除",
                tint = inkSecondary().copy(alpha = disabledAlpha),
                modifier = Modifier.size(24.dp)
            )
            else -> Text(
                text = label ?: "",
                fontSize = when {
                    emphasized -> 16.sp
                    (label?.length ?: 0) > 1 -> 15.sp
                    else -> 22.sp
                },
                fontWeight = if (emphasized) FontWeight.Bold else FontWeight.SemiBold,
                color = if (emphasized) Color.White.copy(alpha = disabledAlpha)
                        else inkSecondary().copy(alpha = disabledAlpha)
            )
        }
    }
}
