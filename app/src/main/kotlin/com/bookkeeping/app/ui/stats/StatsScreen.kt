package com.bookkeeping.app.ui.stats

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.components.ChartSlice
import com.bookkeeping.app.ui.components.DonutChart
import com.bookkeeping.app.ui.theme.liquidGlass

data class CategoryStatItem(
    val categoryId: Long,
    val name: String,
    val iconName: String,
    val color: Color,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float
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
        val groups = filtered.groupBy { it.category?.id ?: -1L }
        groups.map { (catId, txList) ->
            val first = txList.firstOrNull()
            val name = first?.category?.name ?: "未分类"
            val iconName = first?.category?.iconName ?: "MoreHoriz"
            val colorHex = first?.category?.colorHex ?: 0xFF9E9E9E
            val sum = txList.sumOf { it.transaction.amount }
            val pct = if (totalAmount > 0) (sum / totalAmount).toFloat() else 0f
            CategoryStatItem(
                categoryId = catId,
                name = name,
                iconName = iconName,
                color = Color(colorHex),
                totalAmount = sum,
                count = txList.size,
                percentage = pct
            )
        }.sortedByDescending { it.totalAmount }
    }

    val chartSlices = remember(categoryStats) {
        categoryStats.map {
            ChartSlice(
                label = it.name,
                value = it.totalAmount,
                color = it.color
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Liquid Segmented Pill Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(shape = CircleShape, elevation = 6.dp)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val isExpense = selectedType == TransactionType.EXPENSE
            // Expense Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isExpense) {
                            if (isDark) Color(0x33EF4444) else Color(0x22EF4444)
                        } else Color.Transparent
                    )
                    .clickable { selectedType = TransactionType.EXPENSE }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "支出统计",
                    fontSize = 14.sp,
                    fontWeight = if (isExpense) FontWeight.Bold else FontWeight.Medium,
                    color = if (isExpense) Color(0xFFEF4444) else if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
            }

            // Income Tab
            val isIncome = selectedType == TransactionType.INCOME
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) {
                            if (isDark) Color(0x3310B981) else Color(0x2210B981)
                        } else Color.Transparent
                    )
                    .clickable { selectedType = TransactionType.INCOME }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "收入统计",
                    fontSize = 14.sp,
                    fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                    color = if (isIncome) Color(0xFF10B981) else if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Chart Liquid Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(26.dp),
                            elevation = 10.dp
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF111827),
                    modifier = Modifier.padding(top = 20.dp, bottom = 10.dp, start = 4.dp)
                )
            }

            if (categoryStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .liquidGlass(shape = RoundedCornerShape(20.dp), elevation = 4.dp)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无相关统计数据",
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                }
            } else {
                items(categoryStats, key = { it.categoryId }) { stat ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .liquidGlass(
                                shape = RoundedCornerShape(18.dp),
                                elevation = 4.dp
                            )
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(stat.color.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getCategoryIcon(stat.iconName),
                                        contentDescription = null,
                                        tint = stat.color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stat.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isDark) Color.White else Color(0xFF111827)
                                        )
                                        Text(
                                            text = FormatHelper.formatCurrency(stat.totalAmount),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else Color(0xFF111827)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${stat.count} 笔交易",
                                            fontSize = 11.sp,
                                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                                        )
                                        Text(
                                            text = "${(stat.percentage * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = stat.color
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { stat.percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = stat.color,
                                trackColor = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)
                            )
                        }
                    }
                }
            }

            // Bottom padding for floating navigation bar
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }
}
