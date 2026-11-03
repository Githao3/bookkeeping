package com.bookkeeping.app.ui.ledger

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
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
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Month Header with Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPreviousMonth) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            contentDescription = "上月",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = FormatHelper.formatMonth(currentMonthTimestamp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    IconButton(onClick = onNextMonth) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "下月",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats 3 columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "总支出",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(totalExpense),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "总收入",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(totalIncome),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "结余",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatHelper.formatCurrency(balance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (balance >= 0) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Transactions List
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "本月暂无账单明细",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "点击底部 + 按钮记一笔吧",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
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
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = FormatHelper.formatDayHeader(firstTimestamp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (dayExpense > 0.0) {
                                    Text(
                                        text = "支: ${FormatHelper.formatCurrency(dayExpense)}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (dayIncome > 0.0) {
                                    Text(
                                        text = "收: ${FormatHelper.formatCurrency(dayIncome)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // Transactions in this day
                    items(dayTransactions, key = { it.transaction.id }) { item ->
                        TransactionItemCard(
                            item = item,
                            onDelete = { transactionToDelete = item.transaction }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
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
fun TransactionItemCard(
    item: TransactionWithDetails,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tx = item.transaction
    val category = item.category
    val sourceAccount = item.sourceAccount
    val targetAccount = item.targetAccount

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onDelete() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            val (icon, bgCol) = when (tx.type) {
                TransactionType.EXPENSE -> {
                    val color = category?.let { Color(it.colorHex) } ?: Color(0xFFFF7043)
                    val ic = category?.let { IconHelper.getCategoryIcon(it.iconName) }
                        ?: IconHelper.getCategoryIcon("Restaurant")
                    ic to color
                }
                TransactionType.INCOME -> {
                    val color = category?.let { Color(it.colorHex) } ?: Color(0xFF4CAF50)
                    val ic = category?.let { IconHelper.getCategoryIcon(it.iconName) }
                        ?: IconHelper.getCategoryIcon("Work")
                    ic to color
                }
                TransactionType.TRANSFER -> {
                    IconHelper.getCategoryIcon("Transfer") to Color(0xFF1976D2)
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(bgCol.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = bgCol,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Info
            Column(modifier = Modifier.weight(1f)) {
                val title = when (tx.type) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> category?.name ?: "未分类"
                    TransactionType.TRANSFER -> "转账"
                }
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Account Tag
                    val accountDesc = when (tx.type) {
                        TransactionType.EXPENSE, TransactionType.INCOME -> sourceAccount?.name ?: "账户"
                        TransactionType.TRANSFER -> "${sourceAccount?.name} ➔ ${targetAccount?.name}"
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = accountDesc,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }

                    if (tx.note.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tx.note,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Amount & Time
            Column(horizontalAlignment = Alignment.End) {
                val (prefix, amountCol) = when (tx.type) {
                    TransactionType.EXPENSE -> "-" to MaterialTheme.colorScheme.onSurface
                    TransactionType.INCOME -> "+" to Color(0xFF2E7D32)
                    TransactionType.TRANSFER -> "" to Color(0xFF1976D2)
                }
                Text(
                    text = "$prefix${FormatHelper.formatCurrency(tx.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = amountCol
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = FormatHelper.formatTime(tx.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
