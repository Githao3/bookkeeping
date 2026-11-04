package com.bookkeeping.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.ui.theme.LiquidGlassDefaults
import com.bookkeeping.app.ui.theme.liquidGlass

@Composable
fun LiquidBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Glass Island Capsule
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .liquidGlass(
                    shape = CircleShape,
                    elevation = 16.dp,
                    hasRainbowRim = true
                )
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // Tab 0: 明细
            LiquidTabItem(
                selected = currentTab == 0,
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                label = "明细",
                onClick = { onTabSelected(0) }
            )

            // Tab 1: 统计
            LiquidTabItem(
                selected = currentTab == 1,
                icon = Icons.Default.PieChart,
                label = "统计",
                onClick = { onTabSelected(1) }
            )

            // Center space placeholder for the elevated Lens Action Button
            Spacer(modifier = Modifier.width(60.dp))

            // Tab 2: 资产
            LiquidTabItem(
                selected = currentTab == 2,
                icon = Icons.Default.AccountBalanceWallet,
                label = "资产",
                onClick = { onTabSelected(2) }
            )
        }

        // Center Elevated Liquid Lens Action Button (Like the green Coolapk / iQiyi button in the screenshot!)
        CenterLiquidAddButton(
            onClick = onAddClick,
            modifier = Modifier.offset(y = (-4).dp)
        )
    }
}

@Composable
private fun LiquidTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    val activeColor = if (isDark) Color(0xFF60A5FA) else Color(0xFF1677FF)
    val inactiveColor = if (isDark) Color(0x99FFFFFF) else Color(0x99374151)

    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        label = "color"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Subtle active pill highlight background behind active tab
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            if (isDark) Color(0x2B3B82F6) else Color(0x1F2563EB)
                        } else Color.Transparent
                    )
                    .padding(horizontal = 12.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Center elevated Liquid Lens button with refractive rainbow rim and radial glow
 */
@Composable
private fun CenterLiquidAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emeraldGradient = Brush.verticalGradient(
        listOf(
            Color(0xFF00E599), // Top vibrant light emerald
            Color(0xFF00B075), // Mid body
            Color(0xFF00875A)  // Deep bottom rim
        )
    )

    val rainbowSweep = Brush.sweepGradient(LiquidGlassDefaults.RainbowColors)

    Box(
        modifier = modifier
            .size(56.dp)
            // Outer diffused colorful glow shadow
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x5500B075),
                spotColor = Color(0x8800C48C)
            )
            // Chromatic aberration halo ring
            .border(
                width = 2.dp,
                brush = rainbowSweep,
                shape = CircleShape
            )
            .padding(2.dp)
            .clip(CircleShape)
            .background(emeraldGradient)
            .border(
                width = 1.dp,
                color = Color(0x80FFFFFF),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "记一笔",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}
