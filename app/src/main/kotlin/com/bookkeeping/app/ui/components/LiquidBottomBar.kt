package com.bookkeeping.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

/**
 * 悬浮胶囊式扁平底栏导航（Floating Capsule Dock Bar）
 * 严格按照参考图重构优化：
 * 1. 【扁、宽比例】：压缩高度至 54dp，横向更舒展（左右 padding 16dp），整体轻巧纤薄，两头完美跑道圆弧（CircleShape）。
 * 2. 【紧凑精致微排版】：图标尺寸 20dp，与文字间距仅 1.5dp，高质感聚拢在药丸中央，告别松散。
 * 3. 【丝滑物理阻尼药丸滑块（Pill Indicator）】：4 槽位等分，物理阻尼弹簧动画丝滑滑动，温润浅灰药丸背景严丝合缝包裹选中项。
 * 4. 【纯净极简中性质感】：彻底杜绝任何彩色色散与彩虹边缘，采用纯净中性微透底色与极细白光边框。
 */
@Composable
fun LiquidBottomBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current

    val totalTabs = 4
    val safeTab = currentTab.coerceIn(0, totalTabs - 1)

    // 记录容器总宽以计算单槽位宽度与滑动药丸精确偏移
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val horizontalMargin = 3.dp

    val slotWidthDp = if (containerWidthPx > 0) {
        with(density) { (containerWidthPx.toFloat() / totalTabs).toDp() }
    } else {
        0.dp
    }

    // 丝滑物理弹簧位移动效
    val animatedIndicatorOffset by animateDpAsState(
        targetValue = if (slotWidthDp > 0.dp) slotWidthDp * safeTab else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.84f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pillIndicatorOffset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        // 扁平修长跑道胶囊外壳（纯净纯色中性质感，彻底消除渐变抖动条纹）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    ambientColor = if (isDark) Color(0x35000000) else Color(0x0C000000),
                    spotColor = if (isDark) Color(0x45000000) else Color(0x14000000)
                )
                .clip(CircleShape)
                .background(
                    if (isDark) Color(0xFF1E2430) else LiquidGlassTokens.CardSurfaceLight
                )
                .border(
                    width = 0.6.dp,
                    color = if (isDark) LiquidGlassTokens.BorderDark else LiquidGlassTokens.BorderLight,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalMargin)
                    .onSizeChanged { containerWidthPx = it.width },
                contentAlignment = Alignment.CenterStart
            ) {
                // 1. 丝滑物理阻尼药丸背景（Pill Indicator）
                if (slotWidthDp > 0.dp) {
                    val pillPaddingHorizontal = 4.dp
                    val pillWidth = slotWidthDp - (pillPaddingHorizontal * 2)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(with(density) { (animatedIndicatorOffset + pillPaddingHorizontal).roundToPx() }, 0) }
                            .width(pillWidth)
                            .height(48.dp)
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .background(
                                if (isDark) {
                                    Color(0xFF2C3340) // 深色模式深岩灰底
                                } else {
                                    Color(0xFFE8EAEE) // 浅色模式温润浅灰底（纯净中性无杂色）
                                }
                            )
                            .border(
                                width = 0.6.dp,
                                color = if (isDark) Color(0x1AFFFFFF) else Color(0x30FFFFFF),
                                shape = CircleShape
                            )
                    )
                }

                // 2. 4 槽位纯平整排布
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Slot 0: 明细
                    CapsuleTabItem(
                        modifier = Modifier.weight(1f),
                        selected = safeTab == 0,
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        label = "明细",
                        onClick = {
                            if (currentTab != 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(0)
                            }
                        }
                    )

                    // Slot 1: 统计
                    CapsuleTabItem(
                        modifier = Modifier.weight(1f),
                        selected = safeTab == 1,
                        icon = Icons.Default.PieChart,
                        label = "统计",
                        onClick = {
                            if (currentTab != 1) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(1)
                            }
                        }
                    )

                    // Slot 2: 资产
                    CapsuleTabItem(
                        modifier = Modifier.weight(1f),
                        selected = safeTab == 2,
                        icon = Icons.Default.AccountBalanceWallet,
                        label = "资产",
                        onClick = {
                            if (currentTab != 2) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(2)
                            }
                        }
                    )

                    // Slot 3: 设置
                    CapsuleTabItem(
                        modifier = Modifier.weight(1f),
                        selected = safeTab == 3,
                        icon = Icons.Default.Tune,
                        label = "设置",
                        onClick = {
                            if (currentTab != 3) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTabSelected(3)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 胶囊底栏单个 Tab 项（紧凑垂直微排版）
 */
@Composable
private fun CapsuleTabItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 选中态高亮翡翠绿，未选中态中性沉静灰
    val activeColor = if (isDark) Color(0xFF34D399) else Color(0xFF00A844)
    val inactiveColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    val contentColor by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tabContentColor"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "tabPressScale"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .scale(pressScale)
            .clip(CircleShape)
            .selectable(
                selected = selected, role = Role.Tab,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.height(0.5.dp))

        Text(
            text = label,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}
