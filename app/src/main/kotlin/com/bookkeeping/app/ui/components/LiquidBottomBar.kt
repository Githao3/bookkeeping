package com.bookkeeping.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import com.bookkeeping.app.ui.backdrop.opticalLiquidGlass
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

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
            .padding(horizontal = 24.dp)
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Capsule Bar with Optical Liquid Glass & AGSL chromatic dispersion
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .opticalLiquidGlass(
                    shape = CircleShape,
                    elevation = 16.dp,
                    blurRadius = 36f,
                    refractionHeight = 22f,
                    refractionAmount = 18f
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left pair of tabs
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoolTabItem(
                    selected = currentTab == 0,
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    label = "明细",
                    onClick = { onTabSelected(0) }
                )
                CoolTabItem(
                    selected = currentTab == 1,
                    icon = Icons.Default.PieChart,
                    label = "统计",
                    onClick = { onTabSelected(1) }
                )
            }

            // Center space reserved for the elevated Coolapk emerald lens button
            Spacer(modifier = Modifier.width(66.dp))

            // Right pair of tabs
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoolTabItem(
                    selected = currentTab == 2,
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "资产",
                    onClick = { onTabSelected(2) }
                )
                CoolTabItem(
                    selected = currentTab == 3,
                    icon = Icons.Default.Tune,
                    label = "设置",
                    onClick = { onTabSelected(3) }
                )
            }
        }

        // Center Elevated Coolapk-Style Liquid Lens Action Button
        CoolapkElevatedAddButton(
            onClick = onAddClick,
            modifier = Modifier.offset(y = (-8).dp)
        )
    }
}

@Composable
private fun CoolTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }

    val activeColor = if (isDark) Color(0xFF60A5FA) else LiquidGlassTokens.AccentBlue
    val inactiveColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF8E8E93)

    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "contentColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "tabScale"
    )

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .scale(scale)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Subtle indicator pill when active
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (selected) {
                        if (isDark) Color(0x2660A5FA) else Color(0x18007AFF)
                    } else Color.Transparent
                )
                .padding(horizontal = 14.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor
        )
    }
}

/**
 * True Coolapk/iQIYI Center Elevated Liquid Lens Button
 * Features:
 * - Convex glossy radial gradient (Emerald)
 * - Specular rim highlight & diffused colored drop shadow
 * - Interactive spring tactile bounce on click
 */
@Composable
private fun CoolapkElevatedAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(56.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = Color(0x4000C853),
                spotColor = Color(0x6000C853)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676), // Bright liquid center
                        Color(0xFF00C853), // Rich emerald body
                        Color(0xFF00963E)  // Darker lens perimeter
                    ),
                    radius = 80f
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x99FFFFFF), // Top specular sheen
                        Color(0x22FFFFFF),
                        Color(0x40006428)  // Bottom depth rim
                    )
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "记一笔",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}
