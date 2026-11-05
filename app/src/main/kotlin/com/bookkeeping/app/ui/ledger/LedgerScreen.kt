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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
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
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Month Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Pill
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x33FFFFFF) else Color(0x0F000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = FormatHelper.formatMonth(currentMonthTimestamp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )
            }

            // Month Stepper Arrows
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x0F000000))
                        .clickable(onClick = onPreviousMonth),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "上月",
                        tint = if (isDark) Color.White else Color(0xFF374151),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x26FFFFFF) else Color(0x0F000000))
                        .clickable(onClick = onNextMonth),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "下月",
                        tint = if (isDark) Color.White else Color(0xFF374151),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hero Monthly Overview: Refined Apple-Style Liquid Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(24.dp),
                    elevation = 8.dp
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "本月总支出 (元)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Commanding Financial Typographic Amount
                Text(
                    text = FormatHelper.formatCurrency(totalExpense),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Income & Balance Secondary Metrics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(LiquidGlassTokens.IncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "总收入: ${FormatHelper.formatCurrency(totalIncome)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (balance >= 0) LiquidGlassTokens.AccentBlue else LiquidGlassTokens.ExpenseRed
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "结余: ${FormatHelper.formatCurrency(balance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (balance >= 0) {
                                if (isDark) Color(0xFF60A5FA) else LiquidGlassTokens.AccentBlue
                            } else LiquidGlassTokens.ExpenseRed
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List: Inset Grouped iOS Style
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x1FFFFFFF) else Color(0x0A000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = if (isDark) Color(0x55FFFFFF) else Color(0x40000000)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "本月暂无账单记录",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "轻触下方绿色「+」按钮，记录每一笔收支",
                        fontSize = 12.sp,
                        color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                grouped.forEach { (_, dayTransactions) ->
                    val firstTimestamp = dayTransactions.first().transaction.timestamp
                    val dayExpense = dayTransactions
                        .filter { it.transaction.type == TransactionType.EXPENSE }
                        .sumOf { it.transaction.amount }

                    // Daily Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp, bottom = 6.dp, start = 4.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = FormatHelper.formatDayHeader(firstTimestamp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                            )
                            if (dayExpense > 0) {
                                Text(
                                    text = "支出 ¥${String.format(java.util.Locale.CHINA, "%.2f", dayExpense)}",
                                    fontSize = 12.sp,
                                    color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                                )
                            }
                        }
                    }

                    // Inset Grouped Day Card
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
                                dayTransactions.forEachIndexed { index, item ->
                                    ModernTransactionRow(
                                        item = item,
                                        onClick = { transactionToDelete = item.transaction }
                                    )
                                    if (index < dayTransactions.size - 1) {
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

    // Deletion Confirmation Dialog
    if (transactionToDelete != null) {
        val target = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = LiquidGlassTokens.ExpenseRed
                )
            },
            title = { Text("确认删除该账单？", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "金额: ¥${String.format(java.util.Locale.CHINA, "%.2f", target.amount)}\n" +
                            "删除后相关账户余额将自动联动回滚。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteTransaction(target)
                        transactionToDelete = null
                    }
                ) {
                    Text("删除", color = LiquidGlassTokens.ExpenseRed, fontWeight = FontWeight.Bold)
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
private fun ModernTransactionRow(
    item: TransactionWithDetails,
    onClick: () -> Unit
) {
    val tx = item.transaction
    val category = item.category
    val sourceAccount = item.sourceAccount
    val targetAccount = item.targetAccount
    val isDark = isSystemInDarkTheme()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon with Soft Pastel Tinted Circle
        val baseColor = when (tx.type) {
            TransactionType.TRANSFER -> Color(0xFF6366F1)
            else -> category?.let { Color(it.colorHex) } ?: Color(0xFF9E9E9E)
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(baseColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (tx.type == TransactionType.TRANSFER) {
                    IconHelper.getCategoryIcon("swap_horiz")
                } else {
                    IconHelper.getCategoryIcon(category?.iconName ?: "")
                },
                contentDescription = category?.name ?: "转账",
                tint = baseColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center Details: Name & Account/Time Subtext
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = when (tx.type) {
                    TransactionType.TRANSFER -> "转账"
                    else -> category?.name ?: "未分类"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            val subtitle = buildString {
                when (tx.type) {
                    TransactionType.TRANSFER -> {
                        append("${sourceAccount?.name ?: "账户"} ➔ ${targetAccount?.name ?: "账户"}")
                    }
                    else -> {
                        sourceAccount?.name?.let { append(it) }
                    }
                }
                if (tx.note.isNotBlank()) {
                    if (isNotEmpty()) append(" · ")
                    append(tx.note)
                }
            }

            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Right Amount: Clean Tabular Figures
        val amountPrefix = when (tx.type) {
            TransactionType.EXPENSE -> "-"
            TransactionType.INCOME -> "+"
            TransactionType.TRANSFER -> ""
        }
        val amountColor = when (tx.type) {
            TransactionType.EXPENSE -> if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            TransactionType.INCOME -> LiquidGlassTokens.IncomeGreen
            TransactionType.TRANSFER -> if (isDark) Color(0xFF818CF8) else Color(0xFF6366F1)
        }

        Text(
            text = "$amountPrefix${FormatHelper.formatCurrency(tx.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
