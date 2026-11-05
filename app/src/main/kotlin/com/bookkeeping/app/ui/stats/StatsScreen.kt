package com.bookkeeping.app.ui.stats

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import com.bookkeeping.app.ui.theme.liquidGlass

data class CategoryStat(
    val categoryName: String,
    val iconName: String?,
    val colorHex: Long,
    val amount: Double,
    val percentage: Float,
    val count: Int
)

@Composable
fun StatsScreen(
    transactions: List<TransactionWithDetails>,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    val isDark = isSystemInDarkTheme()

    val filtered = remember(transactions, selectedType) {
        transactions.filter { it.transaction.type == selectedType }
    }

    val totalAmount = remember(filtered) {
        filtered.sumOf { it.transaction.amount }
    }

    val categoryStats = remember(filtered, totalAmount) {
        if (totalAmount <= 0.0) emptyList()
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
                        percentage = (sum / totalAmount).toFloat(),
                        count = list.size
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    val chartSlices = remember(categoryStats) {
        categoryStats.map {
            ChartSlice(
                color = Color(it.colorHex),
                percentage = it.percentage
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

        // Segmented Switcher: 支出统计 vs 收入统计
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(if (isDark) Color(0x33FFFFFF) else Color(0x0F000000))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isExpense = selectedType == TransactionType.EXPENSE
            val isIncome = selectedType == TransactionType.INCOME

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isExpense) {
                            if (isDark) Color(0x33F43F5E) else Color.White
                        } else Color.Transparent
                    )
                    .clickable { selectedType = TransactionType.EXPENSE }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "支出统计",
                    fontSize = 14.sp,
                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                    color = if (isExpense) {
                        if (isDark) Color(0xFFFB7185) else LiquidGlassTokens.ExpenseRed
                    } else {
                        if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                    }
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) {
                            if (isDark) Color(0x3310B981) else Color.White
                        } else Color.Transparent
                    )
                    .clickable { selectedType = TransactionType.INCOME }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "收入统计",
                    fontSize = 14.sp,
                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                    color = if (isIncome) {
                        LiquidGlassTokens.IncomeGreen
                    } else {
                        if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Chart Card: Liquid Glass
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 8.dp
                        )
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        slices = chartSlices,
                        totalAmount = totalAmount,
                        centerTitle = if (selectedType == TransactionType.EXPENSE) "总支出" else "总收入"
                    )
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(
                                shape = RoundedCornerShape(20.dp),
                                elevation = 3.dp
                            )
                    ) {
                        Column {
                            categoryStats.forEachIndexed { index, stat ->
                                CategoryStatRow(stat = stat, index = index + 1)
                                if (index < categoryStats.size - 1) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 68.dp, end = 16.dp)
                                            .height(0.6.dp)
                                            .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0A000000))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryStatRow(stat: CategoryStat, index: Int) {
    val isDark = isSystemInDarkTheme()
    val baseColor = Color(stat.colorHex)

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
                    text = FormatHelper.formatCurrency(stat.amount),
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

            LinearProgressIndicator(
                progress = { stat.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = baseColor,
                trackColor = if (isDark) Color(0x1FFFFFFF) else Color(0x0F000000),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

data class ChartSlice(val color: Color, val percentage: Float)

@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    totalAmount: Double,
    centerTitle: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val progress = remember { Animatable(0f) }

    LaunchedEffect(slices, totalAmount) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 800))
    }

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 26.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            if (slices.isEmpty()) {
                drawArc(
                    color = if (isDark) Color(0x22FFFFFF) else Color(0x0D000000),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                var currentAngle = -90f
                slices.forEach { slice ->
                    val sweep = slice.percentage * 360f * progress.value
                    if (sweep > 0f) {
                        drawArc(
                            color = slice.color,
                            startAngle = currentAngle,
                            sweepAngle = sweep - 1.5f, // subtle gap
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        currentAngle += sweep
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = FormatHelper.formatCurrency(totalAmount),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            )
        }
    }
}
