package com.bookkeeping.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.AccountType
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

/**
 * 扁平玻璃组件库（Glass Kit）
 * 与 LiquidBottomBar 完全同一套视觉配方：纯色中性面板 + 0.6dp 发丝描边 + 柔和投影 + 物理弹簧动效。
 * 不依赖模糊 / AGSL 着色器，Android 10 及以下渲染效果一致。
 */

/** 当前模式的翡翠强调色（选中态、强调操作）。 */
@Composable
fun glassAccent(): Color = if (isSystemInDarkTheme()) Color(0xFF34D399) else Color(0xFF00A844)

@Composable
internal fun hairline(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.BorderDark else LiquidGlassTokens.BorderLight

@Composable
internal fun insetTint(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.InsetDark else LiquidGlassTokens.InsetLight

@Composable
internal fun panelFill(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.GlassPanelDark else LiquidGlassTokens.GlassPanelLight

@Composable
internal fun pillFill(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.PillDark else Color.White

@Composable
internal fun pillBorder(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.PillBorderDark else LiquidGlassTokens.PillBorderLight

@Composable
internal fun inkPrimary(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight

@Composable
internal fun inkSecondary(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight

@Composable
internal fun inkTertiary(): Color =
    if (isSystemInDarkTheme()) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight

/**
 * 扁平玻璃卡片：纯色中性面板 + 发丝描边 + 柔影。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = if (isDark) Color(0x30000000) else Color(0x0A000000),
                spotColor = if (isDark) Color(0x40000000) else Color(0x12000000)
            )
            .clip(shape)
            .background(panelFill())
            .border(0.6.dp, hairline(), shape)
    ) {
        content()
    }
}

/**
 * iOS 分组列表容器（GlassCard 的列布局形态）。
 */
@Composable
fun GlassListSection(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    GlassCard(modifier = modifier, shape = shape, elevation = elevation) {
        Column(content = content)
    }
}

/**
 * 分组列表行：前导染色图标圆 + 标题/副标题 + 尾随内容，点击无水波纹（纯按压反馈交给上层）。
 */
@Composable
fun GlassListRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    iconSize: Dp = 30.dp,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    danger: Boolean = false,
    dimmed: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    val rowInteraction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = rowInteraction,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            val tint = iconTint ?: glassAccent()
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = if (isSystemInDarkTheme()) 0.24f else 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(iconSize * 0.55f)
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (subtitle == null) FontWeight.Medium else FontWeight.SemiBold,
                color = when {
                    danger -> LiquidGlassTokens.ExpenseRed
                    dimmed -> inkTertiary()
                    else -> inkPrimary()
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Spacer(Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = inkTertiary(),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (trailing != null) trailing()
    }
}

/**
 * 发丝分隔线（分组列表行之间）。
 */
@Composable
fun GlassDivider(startIndent: Dp = 16.dp, endIndent: Dp = 16.dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startIndent, end = endIndent)
            .height(0.6.dp)
            .background(
                if (isSystemInDarkTheme()) Color(0x14FFFFFF) else Color(0x0A000000)
            )
    )
}

/**
 * 胶囊搜索框：凹陷轨道 + 放大镜 + 可清空，44dp 薄胶囊。
 */
@Composable
fun GlassSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索"
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(CircleShape)
            .background(insetTint())
            .border(0.6.dp, hairline(), CircleShape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = inkTertiary(),
            modifier = Modifier.size(17.dp)
        )
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    fontSize = 14.sp,
                    color = inkTertiary(),
                    maxLines = 1
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = inkPrimary()),
                cursorBrush = SolidColor(glassAccent()),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            val clearInteraction = remember { MutableInteractionSource() }
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = "清空搜索",
                tint = inkTertiary(),
                modifier = Modifier
                    .size(17.dp)
                    .clickable(
                        interactionSource = clearInteraction,
                        indication = null
                    ) { onQueryChange("") }
            )
        }
    }
}

/**
 * 胶囊按钮：玻璃面板底 + 翡翠强调内容，用于「筛选」「添加」等轻量动作。
 */
@Composable
fun GlassPillButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 36.dp
) {
    val isDark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassPillPress"
    )
    val accent = glassAccent()
    Row(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(4.dp, CircleShape, ambientColor = Color(0x08000000), spotColor = if (isDark) Color(0x30000000) else Color(0x10000000))
            .clip(CircleShape)
            .background(panelFill())
            .border(0.6.dp, hairline(), CircleShape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent.copy(alpha = if (enabled) 1f else 0.38f),
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent.copy(alpha = if (enabled) 1f else 0.38f),
            maxLines = 1
        )
    }
}

/**
 * 圆形图标按钮：凹陷轨道底 + 发丝描边，用于月份箭头等紧凑动作。
 */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    iconSize: Dp = 18.dp,
    enabled: Boolean = true,
    tint: Color? = null
) {
    val isDark = isSystemInDarkTheme()
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "glassIconPress"
    )
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(insetTint())
            .border(0.6.dp, hairline(), CircleShape)
            .clickable(interactionSource = interaction, indication = null, enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = (tint ?: (if (isDark) Color(0xFFCBD5E1) else Color(0xFF374151))).copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * iOS 分段控件：凹陷轨道 + 物理阻尼滑动药丸（与底栏 Pill Indicator 同款动效）。
 * 轨道宽度由调用方决定（fillMaxWidth 或固定 width），槽位等分。
 */
@Composable
fun GlassSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 40.dp,
    accentColor: Color? = null
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    var containerWidthPx by remember { mutableIntStateOf(0) }
    val slotWidthDp = if (containerWidthPx > 0) {
        with(density) { (containerWidthPx.toFloat() / options.size).toDp() }
    } else 0.dp
    val safeIndex = selectedIndex.coerceIn(0, options.size - 1)
    val animatedOffset by animateDpAsState(
        targetValue = if (slotWidthDp > 0.dp) slotWidthDp * safeIndex else 0.dp,
        animationSpec = spring(dampingRatio = 0.84f, stiffness = Spring.StiffnessMediumLow),
        label = "glassSegmentedOffset"
    )
    val accent = accentColor ?: glassAccent()

    Box(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(insetTint())
            .border(0.6.dp, hairline(), CircleShape)
            .padding(3.dp)
            .onSizeChanged { containerWidthPx = it.width }
    ) {
        if (slotWidthDp > 0.dp) {
            // 滑动药丸
            Box(
                modifier = Modifier
                    .offset { IntOffset(animatedOffset.roundToPx(), 0) }
                    .width(slotWidthDp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(pillFill())
                    .border(0.6.dp, pillBorder(), CircleShape)
            )
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            options.forEachIndexed { index, option ->
                val selected = index == safeIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!selected) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(index)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) accent else inkSecondary(),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * 胶囊选择 chip（弹窗内账户/分类/类型选择）。
 */
@Composable
fun GlassChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val isDark = isSystemInDarkTheme()
    val accent = glassAccent()
    Box(
        modifier = modifier
            .height(32.dp)
            .clip(CircleShape)
            .background(
                when {
                    selected -> if (isDark) LiquidGlassTokens.PillDark else LiquidGlassTokens.PillLight
                    else -> Color.Transparent
                }
            )
            .border(
                0.6.dp,
                if (selected) pillBorder() else hairline(),
                CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = when {
                selected -> accent
                enabled -> inkSecondary()
                else -> inkTertiary()
            },
            maxLines = 1
        )
    }
}

/**
 * 凹陷样式文本输入框（弹窗内名称 / 日期输入）。
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(insetTint())
            .border(0.6.dp, hairline(), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                color = inkTertiary(),
                maxLines = 1
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = TextStyle(fontSize = 14.sp, color = inkPrimary()),
            cursorBrush = SolidColor(glassAccent()),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * 细进度轨道（替代 LinearProgressIndicator）：圆角槽 + 分类色填充，弹簧过渡。
 */
@Composable
fun GlassProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = glassAccent(),
    trackColor: Color? = null,
    trackHeight: Dp = 5.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow),
        label = "glassProgress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(trackHeight)
            .clip(CircleShape)
            .background(trackColor ?: insetTint())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(color)
        )
    }
}

/**
 * 环形不确定进度（备份/恢复等异步操作中）。
 */
@Composable
fun GlassProgressIndicator(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "glassIndeterminate")
    val position by transition.animateFloat(
        initialValue = -0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = androidx.compose.animation.core.FastOutSlowInEasing)),
        label = "glassIndeterminatePosition"
    )
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(CircleShape)
            .background(insetTint())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .fillMaxHeight()
                .offset { IntOffset((maxWidth * position.coerceIn(0f, 1f)).roundToPx(), 0) }
                .clip(CircleShape)
                .background(glassAccent())
        )
    }
}

/**
 * 统一玻璃弹窗壳：实色面板、24dp 圆角、无 tonal elevation。
 */
@Composable
fun GlassAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        containerColor = panelFill(),
        tonalElevation = 0.dp,
        iconContentColor = inkSecondary(),
        titleContentColor = inkPrimary(),
        textContentColor = inkSecondary()
    )
}

/**
 * 弹窗按钮动作（翡翠强调 / 危险红 / 普通灰）。
 */
@Composable
fun GlassDialogAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    danger: Boolean = false
) {
    val isDark = isSystemInDarkTheme()
    val baseColor = when {
        danger -> LiquidGlassTokens.ExpenseRed
        emphasized -> if (isDark) Color(0xFF34D399) else Color(0xFF00A844)
        else -> if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
    }
    TextButton(onClick = onClick, enabled = enabled, modifier = modifier) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            color = baseColor.copy(alpha = if (enabled) 1f else 0.38f)
        )
    }
}

/**
 * 祖母绿 Switch 配色（iOS 风格实心轨道）。
 */
@Composable
fun glassSwitchColors(): SwitchColors {
    val isDark = isSystemInDarkTheme()
    val accent = glassAccent()
    return SwitchDefaults.colors(
        checkedTrackColor = accent,
        checkedThumbColor = Color.White,
        checkedBorderColor = Color.Transparent,
        checkedIconColor = Color.White,
        uncheckedTrackColor = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000),
        uncheckedThumbColor = if (isDark) Color(0xFF94A3B8) else Color.White,
        uncheckedBorderColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
        uncheckedIconColor = Color.Transparent
    )
}

/** 账户类型的品牌化图标色（资产页与记一笔面板共用）。 */
fun accountTypeTint(type: AccountType): Color = when (type) {
    AccountType.WECHAT -> Color(0xFF07C160)
    AccountType.ALIPAY -> Color(0xFF1677FF)
    AccountType.BANK_CARD -> Color(0xFF3B82F6)
    AccountType.CREDIT_CARD -> Color(0xFF8B5CF6)
    AccountType.CASH -> Color(0xFFF59E0B)
    AccountType.OTHER -> Color(0xFF9CA3AF)
}
