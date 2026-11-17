package com.bookkeeping.app.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassCard
import com.bookkeeping.app.ui.components.GlassDialogAction
import com.bookkeeping.app.ui.components.GlassDivider
import com.bookkeeping.app.ui.components.GlassIconButton
import com.bookkeeping.app.ui.components.GlassProgressTrack
import com.bookkeeping.app.ui.components.GlassSegmented
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

data class CategoryStat(
    val categoryName: String,
    val iconName: String?,
    val colorHex: Long,
    val amount: Long,
    val percentage: Float,
    val count: Int
)

@Composable
fun StatsScreen(
    transactions: List<TransactionWithDetails>,
    currentMonthTimestamp: Long,
    onPreviousMonth: () -> Unit, onNextMonth: () -> Unit,
    onPreviousYear: () -> Unit, onNextYear: () -> Unit,
    modifier: Modifier = Modifier
) {
    var range by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("月") }
    var selectedType by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(TransactionType.EXPENSE) }
    val isDark = isSystemInDarkTheme()
    var detailCategory by remember { mutableStateOf<String?>(null) }

    val filtered = remember(transactions, selectedType, range, currentMonthTimestamp) {
        transactions.filter {
            val selected = java.util.Calendar.getInstance().apply { timeInMillis = currentMonthTimestamp }
            val date = java.util.Calendar.getInstance().apply { timeInMillis = it.transaction.timestamp }
            it.transaction.type == selectedType && (range == "全部" || date.get(java.util.Calendar.YEAR) == selected.get(java.util.Calendar.YEAR) && (range == "年" || date.get(java.util.Calendar.MONTH) == selected.get(java.util.Calendar.MONTH)))
        }
    }

    val totalAmount = remember(filtered) {
        filtered.sumOf { it.transaction.amount }
    }

    val categoryStats = remember(filtered, totalAmount) {
        if (totalAmount <= 0) emptyList()
        else {
            filtered
                .groupBy { it.category?.id ?: 0L }
                .map { (_, list) ->
                    val first = list.first()
                    val sum = list.sumOf { it.transaction.amount }
                    CategoryStat(
                        categoryName = first.category?.name ?: "未分类",
                        iconName = first.category?.iconName,
                        colorHex = first.category?.colorHex ?: 0xFF9E9E9E,
                        amount = sum,
                        percentage = (sum.toDouble() / totalAmount).toFloat(),
                        count = list.size
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    // 相邻撞色自动错开后的展示色：圆盘图与排行榜共用同一份，保证两处颜色一致
    val displayColors = remember(categoryStats) { distinctAdjacentColors(categoryStats.map { Color(it.colorHex) }) }
    val chartSlices = remember(categoryStats, displayColors) {
        categoryStats.mapIndexed { index, stat ->
            ChartSlice(
                name = stat.categoryName,
                amount = stat.amount,
                color = displayColors[index],
                percentage = stat.percentage
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        val rangeOptions = listOf("月", "年", "全部")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            GlassIconButton(
                Icons.Default.ChevronLeft, "上一期",
                onClick = if (range == "年") onPreviousYear else onPreviousMonth,
                enabled = range != "全部"
            )
            Text(
                if (range == "全部") "全部时间" else if (range == "年") java.text.SimpleDateFormat("yyyy年", java.util.Locale.CHINA).format(java.util.Date(currentMonthTimestamp)) else FormatHelper.formatMonth(currentMonthTimestamp),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            )
            GlassIconButton(
                Icons.Default.ChevronRight, "下一期",
                onClick = if (range == "年") onNextYear else onNextMonth,
                enabled = range != "全部"
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Range Segmented Control (iOS style, sliding pill)
        GlassSegmented(
            options = rangeOptions,
            selectedIndex = rangeOptions.indexOf(range).coerceAtLeast(0),
            onSelect = { range = rangeOptions[it] },
            modifier = Modifier.width(216.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segmented Switcher: 支出统计 vs 收入统计
        GlassSegmented(
            options = listOf("支出统计", "收入统计"),
            selectedIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
            onSelect = { selectedType = if (it == 0) TransactionType.EXPENSE else TransactionType.INCOME },
            modifier = Modifier.fillMaxWidth(),
            accentColor = if (selectedType == TransactionType.EXPENSE) LiquidGlassTokens.ExpenseRed else LiquidGlassTokens.IncomeGreen
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 116.dp)
        ) {
            // Chart Card: Flat Glass
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        DonutChart(
                            slices = chartSlices,
                            totalAmount = totalAmount,
                            centerTitle = if (selectedType == TransactionType.EXPENSE) "总支出" else "总收入"
                        )
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = if (selectedType == TransactionType.EXPENSE) "支出排行榜" else "收入排行榜",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight,
                    modifier = Modifier.padding(top = 18.dp, bottom = 8.dp, start = 4.dp)
                )
            }

            if (categoryStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无相关统计数据",
                            fontSize = 14.sp,
                            color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                        )
                    }
                }
            } else {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = 4.dp
                    ) {
                        Column {
                            categoryStats.forEachIndexed { index, stat ->
                                Box(Modifier.clickable { detailCategory = stat.categoryName }) { CategoryStatRow(stat = stat, index = index + 1, displayColor = displayColors[index]) }
                                if (index < categoryStats.size - 1) {
                                    GlassDivider(startIndent = 68.dp, endIndent = 16.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    detailCategory?.let { category ->
        GlassAlertDialog(onDismissRequest = { detailCategory = null }, title = { Text("$category · 当前统计期间") },
            text = { androidx.compose.foundation.lazy.LazyColumn(Modifier.heightIn(max = 360.dp)) {
                itemsIndexed(filtered.filter { (it.category?.name ?: "未分类") == category }) { _, item ->
                    Column(Modifier.padding(vertical = 8.dp)) {
                        Text("${FormatHelper.formatDayHeader(item.transaction.timestamp)} ${FormatHelper.currency(item.transaction.amount)}")
                        Text("${item.sourceAccount?.name.orEmpty()} · ${item.transaction.note}", fontSize = 13.sp)
                    }
                }
            } }, confirmButton = { GlassDialogAction("关闭", onClick = { detailCategory = null }) })
    }
}

@Composable
private fun CategoryStatRow(stat: CategoryStat, index: Int, displayColor: Color) {
    val isDark = isSystemInDarkTheme()
    val baseColor = displayColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(baseColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconHelper.getCategoryIcon(stat.iconName ?: ""),
                contentDescription = stat.categoryName,
                tint = baseColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stat.categoryName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )
                Text(
                    text = FormatHelper.currency(stat.amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${stat.count} 笔交易",
                    fontSize = 12.sp,
                    color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                )
                Text(
                    text = "${(stat.percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = baseColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            GlassProgressTrack(
                progress = stat.percentage,
                color = baseColor
            )
        }
    }
}

data class ChartSlice(val name: String, val amount: Long, val color: Color, val percentage: Float)

/** 相邻撞色自动错开：环形分段按金额排序逐段检查与前一段的色相差，过近则旋转色相（明度/饱和度不变）；
 *  首尾闭环同样检查。圆盘图与排行榜共用同一份结果，保证两处颜色一致，不动数据库中的分类本色。 */
private fun distinctAdjacentColors(colors: List<Color>): List<Color> {
    if (colors.size < 2) return colors
    val out = ArrayList(colors)
    fun hsvOf(c: Color) = FloatArray(3).also { android.graphics.Color.colorToHSV(c.toArgb(), it) }
    fun hue(c: Color) = hsvOf(c)[0]
    fun rotate(c: Color, delta: Float): Color {
        val hsv = hsvOf(c)
        hsv[0] = (hsv[0] + delta) % 360f
        return Color(android.graphics.Color.HSVToColor(hsv))
    }
    fun hueDist(a: Float, b: Float): Float { val d = abs(a - b); return minOf(d, 360f - d) }
    for (i in 1 until out.size) {
        if (hueDist(hue(out[i]), hue(out[i - 1])) < 25f) out[i] = rotate(out[i], 45f)
    }
    if (out.size > 2 && hueDist(hue(out.last()), hue(out.first())) < 25f) {
        out[out.lastIndex] = rotate(out.last(), -45f)
    }
    return out
}

/** 玻璃圆盘图：平头分段环图（方切口 + 固定小间隙，对标 token 用量环图风格）；点击扇区高亮（其余淡化、选中段加粗）并联动中心显示该分类 名称/金额/占比。
 *  全段统一 Stroke 默认平头笔帽，无圆头延伸问题；仅单一分类时无间隙成整环。 */
@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    totalAmount: Long,
    centerTitle: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current
    val progress = remember { Animatable(0f) }
    // 选中扇区（数据变化时自动重置）；再次点击同一扇区或点击环带外取消
    var selected by remember(slices) { mutableStateOf<Int?>(null) }
    val dimAlpha by animateFloatAsState(if (selected == null) 1f else 0.35f, label = "donutDim")
    val popWidth by animateDpAsState(if (selected == null) 0.dp else 6.dp, label = "donutPop")

    LaunchedEffect(slices, totalAmount) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Box(
        modifier = modifier.size(180.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(slices) {
                    detectTapGestures { pos ->
                        val stroke = 22.dp.toPx()
                        val pop = popWidth.toPx()
                        val band = (stroke + pop) / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = (minOf(size.width, size.height).toFloat() - stroke - pop) / 2f
                        val d = hypot((pos.x - center.x).toDouble(), (pos.y - center.y).toDouble())
                        if (d < radius - band || d > radius + band) {
                            if (selected != null) selected = null
                        } else {
                            // 0° = 顶部（-90°），顺时针增长，与 drawArc 的起始角约定同源
                            val deg = (atan2((pos.y - center.y).toDouble(), (pos.x - center.x).toDouble()) * 180 / PI + 450.0) % 360.0
                            var acc = 0f
                            var hit: Int? = null
                            slices.forEachIndexed { index, slice ->
                                val end = acc + slice.percentage * 360f
                                if (deg >= acc && deg < end) hit = index
                                acc = end
                            }
                            val next = if (hit == selected) null else hit
                            if (next != selected) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selected = next
                        }
                    }
                }
        ) {
            val stroke = 22.dp.toPx()
            val pop = popWidth.toPx()
            val band = (stroke + pop) / 2f
            val diameter = size.minDimension - stroke - pop
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(band, band)
            val radius = diameter / 2f

            if (slices.isEmpty()) {
                drawArc(
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x0D000000),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke)
                )
            } else {
                // 平头分段（对标 token 用量环图）：方切口 + 固定小间隙；仅一段时无间隙成整环
                val gapDeg = if (slices.size == 1) 0f else 1f
                var currentAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweep = slice.percentage * 360f * progress.value
                    if (sweep > 0f) {
                        val isSelected = selected == index
                        val width = if (isSelected) stroke + pop else stroke
                        val color = if (selected != null && !isSelected) slice.color.copy(alpha = dimAlpha) else slice.color
                        drawArc(
                            color = color,
                            startAngle = currentAngle + gapDeg / 2,
                            sweepAngle = (sweep - gapDeg).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = width)
                        )
                        currentAngle += sweep
                    }
                }
            }
        }

        // 中心：默认显示 总支出/总收入 + 合计；选中扇区后切换为该分类 名称/金额/占比
        val sel = selected?.let { slices.getOrNull(it) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (sel == null) {
                Text(
                    text = centerTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FormatHelper.currency(totalAmount),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )
            } else {
                Text(
                    text = sel.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 108.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FormatHelper.currency(sel.amount),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(sel.percentage * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = sel.color
                )
            }
        }
    }
}
