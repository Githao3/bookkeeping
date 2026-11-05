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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.bookkeeping.app.ui.backdrop.LiquidGlassSurface
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

/**
 * Authentic Coolapk (酷安) Floating Liquid Glass Capsule Navigation Bar.
 * Directly references the official Coolapk bar in the design brief:
 * - 5 mathematically balanced symmetrical slots (2 left, 1 center elevated emerald lens button, 2 right).
 * - Real hardware AGSL SDF lens refraction & chromatic dispersion on the glass surface.
 * - 100% crisp foreground icons and typography without blur.
 */
@Composable
fun LiquidBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .navigationBarsPadding()
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        // Floating Pill Capsule with Isolated AGSL Liquid Glass Shader
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            shape = CircleShape,
            elevation = 14.dp,
            surfaceAlpha = 0.94f
        ) {
            // Symmetrical 5-Slot Layout (weight = 1f each)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 1: 明细
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CoolTabItem(
                        selected = currentTab == 0,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "明细",
                        onClick = { onTabSelected(0) }
                    )
                }

                // Slot 2: 统计
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CoolTabItem(
                        selected = currentTab == 1,
                        icon = Icons.Default.PieChart,
                        label = "统计",
                        onClick = { onTabSelected(1) }
                    )
                }

                // Slot 3: Center Elevated Coolapk Emerald Lens Button (+)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CoolapkElevatedAddButton(
                        onClick = onAddClick
                    )
                }

                // Slot 4: 资产
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CoolTabItem(
                        selected = currentTab == 2,
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "资产",
                        onClick = { onTabSelected(2) }
                    )
                }

                // Slot 5: 设置
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CoolTabItem(
                        selected = currentTab == 3,
                        icon = Icons.Default.Tune,
                        label = "设置",
                        onClick = { onTabSelected(3) }
                    )
                }
            }
        }
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
    val isPressed by interactionSource.collectIsPressedAsState()

    val activeColor = if (isDark) Color(0xFF34D399) else Color(0xFF00C853)
    val inactiveColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "contentColor"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )

    Column(
        modifier = Modifier
            .scale(pressScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container with soft tinted indicator pill when selected
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(
                    if (selected) {
                        if (isDark) Color(0x2634D399) else Color(0x1A00C853)
                    } else Color.Transparent
                )
                .padding(horizontal = 14.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

/**
 * Center Elevated Coolapk-Style Liquid Lens Action Button.
 * Replicates the emerald sphere in Coolapk with convex radial lighting,
 * specular white sheen, and bouncy spring press tactile response.
 */
@Composable
private fun CoolapkElevatedAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(52.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = Color(0x3300C853),
                spotColor = Color(0x5500C853)
            )
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF00E676), // Glowing highlight center
                        Color(0xFF00C853), // Emerald body
                        Color(0xFF00963E)  // Darker lens rim
                    ),
                    radius = 70f
                )
            )
            .border(
                width = 1.2.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0x99FFFFFF), // Top specular reflection
                        Color(0x25FFFFFF),
                        Color(0x35005020)  // Bottom depth rim
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
