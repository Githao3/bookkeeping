package com.bookkeeping.app.ui.ledger

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.theme.liquidGlass
import java.util.Calendar

@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    currentMonthTimestamp: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    val isDark = isSystemInDarkTheme()

    // Aggregate monthly statistics
    val totalExpense = transactions
        .filter { it.transaction.type == TransactionType.EXPENSE }
        .sumOf { it.transaction.amount }

    val totalIncome = transactions
        .filter { it.transaction.type == TransactionType.INCOME }
        .sumOf { it.transaction.amount }

    val balance = totalIncome - totalExpense

    // Group transactions by calendar day
    val grouped = remember(transactions) {
        transactions.groupBy { item ->
            val cal = Calendar.getInstance().apply { timeInMillis = item.transaction.timestamp }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Top Month Overview: Luxurious Liquid Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp
                )
                .padding(20.dp)
        ) {
            Column {
                // Month Header with Glass Pills for Previous/Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                            .clickable(onClick = onPreviousMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "上月",
                            tint = if (isDark) Color.White else Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = FormatHelper.formatMonth(currentMonthTimestamp),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF111827)
                    )

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .liquidGlass(shape = CircleShape, elevation = 2.dp)
                            .clickable(onClick = onNextMonth),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "下月",
                            tint = if (isDark) Color.White else Color(0xFF1F2937),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Stats: 3 Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Expense
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "总支出",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(totalExpense),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444) // Coral Red
                        )
                    }

                    // Income
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "总收入",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(totalIncome),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981) // Emerald Green
                        )
                    }

                    // Balance
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "结余",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(balance),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) {
                                if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            } else Color(0xFFEF4444)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Transactions List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .liquidGlass(
                            shape = RoundedCornerShape(24.dp),
                            elevation = 6.dp
                        )
                        .padding(vertical = 36.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(54.dp),
                            tint = if (isDark) Color(0x66FFFFFF) else Color(0x44000000)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "本月暂无账单明细",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color.White else Color(0xFF1F2937)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "轻触底部晶莹「+」按钮，记录第一笔吧",
                            fontSize = 13.sp,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                grouped.forEach { (_, dayTransactions) ->
                    val firstTimestamp = dayTransactions.first().transaction.timestamp
                    val dayExpense = dayTransactions
                        .filter { it.transaction.type == TransactionType.EXPENSE }
                        .sumOf { it.transaction.amount }
                    val dayIncome = dayTransactions
                        .filter { it.transaction.type == TransactionType.INCOME }
                        .sumOf { it.transaction.amount }

                    // Daily Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = FormatHelper.formatDayHeader(firstTimestamp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (dayExpense > 0.0) {
                                    Text(
                                        text = "支 ${FormatHelper.formatCurrency(dayExpense)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                                    )
                                }
                                if (dayIncome > 0.0) {
                                    Text(
                                        text = "收 ${FormatHelper.formatCurrency(dayIncome)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }
                    }

                    // Transactions in this day
                    items(dayTransactions, key = { it.transaction.id }) { item ->
                        LiquidTransactionCard(
                            item = item,
                            onDelete = { transactionToDelete = item.transaction }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }

                // Bottom padding for floating navigation bar
                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }
    }

    // Delete confirmation dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("确认删除此记录？") },
            text = {
                Text("删除此笔金额（${FormatHelper.formatCurrency(tx.amount)}）将自动同步回退对应的账户余额。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(tx)
                        transactionToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun LiquidTransactionCard(
    item: TransactionWithDetails,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    val category = item.category
    val sourceAccount = item.sourceAccount
    val targetAccount = item.targetAccount
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                shape = RoundedCornerShape(18.dp),
                elevation = 4.dp
            )
            .clickable(onClick = onDelete)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon in Frosted Glow Circle
            val (icon, bgCol) = when (tx.type) {
                TransactionType.EXPENSE -> {
                    val color = category?.let { Color(it.colorHex) } ?: Color(0xFFFF7043)
                    val ic = category?.let { IconHelper.getCategoryIcon(it.iconName) }
                        ?: IconHelper.getCategoryIcon("Restaurant")
                    ic to color
                }
                TransactionType.INCOME -> {
                    val color = category?.let { Color(it.colorHex) } ?: Color(0xFF10B981)
                    val ic = category?.let { IconHelper.getCategoryIcon(it.iconName) }
                        ?: IconHelper.getCategoryIcon("Work")
                    ic to color
                }
                TransactionType.TRANSFER -> {
                    IconHelper.getCategoryIcon("Transfer") to Color(0xFF3B82F6)
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(bgCol.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bgCol,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                val title = when (tx.type) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> category?.name ?: "未分类"
                    TransactionType.TRANSFER -> "转账"
                }
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val accountDesc = when (tx.type) {
                        TransactionType.EXPENSE, TransactionType.INCOME -> sourceAccount?.name ?: "账户"
                        TransactionType.TRANSFER -> "${sourceAccount?.name} ➔ ${targetAccount?.name}"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = accountDesc,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                        )
                    }

                    if (tx.note.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tx.note,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount & Time
            Column(horizontalAlignment = Alignment.End) {
                val (prefix, amountCol) = when (tx.type) {
                    TransactionType.EXPENSE -> "-" to if (isDark) Color(0xFFF87171) else Color(0xFFEF4444)
                    TransactionType.INCOME -> "+" to Color(0xFF10B981)
                    TransactionType.TRANSFER -> "" to Color(0xFF3B82F6)
                }
                Text(
                    text = "$prefix${FormatHelper.formatCurrency(tx.amount)}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountCol
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FormatHelper.formatTime(tx.timestamp),
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
                )
            }
        }
    }
}
