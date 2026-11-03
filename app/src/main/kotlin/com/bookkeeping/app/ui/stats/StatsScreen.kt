package com.bookkeeping.app.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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

    val filtered = remember(transactions, selectedType) {
        transactions.filter { it.transaction.type == selectedType }
    }

    val totalAmount = remember(filtered) {
        filtered.sumOf { it.transaction.amount }
    }

    // Group by category and sort by amount descending
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row: 支出 vs 收入
        TabRow(
            selectedTabIndex = if (selectedType == TransactionType.EXPENSE) 0 else 1,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(20.dp))
        ) {
            Tab(
                selected = selectedType == TransactionType.EXPENSE,
                onClick = { selectedType = TransactionType.EXPENSE },
                text = { Text("支出统计") }
            )
            Tab(
                selected = selectedType == TransactionType.INCOME,
                onClick = { selectedType = TransactionType.INCOME },
                text = { Text("收入统计") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Chart Section Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DonutChart(
                            slices = chartSlices,
                            totalAmount = totalAmount,
                            centerTitle = if (selectedType == TransactionType.EXPENSE) "总支出" else "总收入"
                        )
                    }
                }
            }

            // Category Ranking Header
            item {
                Text(
                    text = if (selectedType == TransactionType.EXPENSE) "支出排行榜" else "收入排行榜",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            if (categoryStats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无相关统计数据",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(categoryStats, key = { it.categoryId }) { stat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(stat.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = IconHelper.getCategoryIcon(stat.iconName),
                                        contentDescription = null,
                                        tint = stat.color,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stat.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = FormatHelper.formatCurrency(stat.totalAmount),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${stat.count} 笔",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${(stat.percentage * 100).toInt()}%",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = stat.color
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { stat.percentage },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = stat.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
