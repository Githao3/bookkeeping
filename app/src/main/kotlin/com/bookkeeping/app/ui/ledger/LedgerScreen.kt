package com.bookkeeping.app.ui.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassCard
import com.bookkeeping.app.ui.components.GlassChip
import com.bookkeeping.app.ui.components.GlassDialogAction
import com.bookkeeping.app.ui.components.GlassDivider
import com.bookkeeping.app.ui.components.GlassIconButton
import com.bookkeeping.app.ui.components.GlassPillButton
import com.bookkeeping.app.ui.components.GlassSearchField
import com.bookkeeping.app.ui.components.GlassTextField
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import java.util.Calendar

@Composable
fun LedgerScreen(
    transactions: List<TransactionWithDetails>,
    currentMonthTimestamp: Long,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var detail by remember { mutableStateOf<TransactionWithDetails?>(null) }
    var query by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var accountId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var categoryId by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var startDate by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var endDate by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var appliedStart by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var appliedEnd by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<Long?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf<String?>(null) }
    fun dateValue(text: String): Long? {
        if (text.isBlank()) return null
        require(Regex("\\d{4}-\\d{2}-\\d{2}").matches(text)) { "日期格式为 yyyy-MM-dd" }
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.CHINA).apply { isLenient = false }
        return formatter.parse(text)?.time
    }
    val visible = transactions.filter { item ->
        val tx = item.transaction
        (accountId == null || tx.sourceAccountId == accountId || tx.targetAccountId == accountId) &&
        (categoryId == null || tx.categoryId == categoryId) &&
        (appliedStart?.let { tx.timestamp >= it } ?: true) &&
        (appliedEnd?.let { tx.timestamp < it } ?: true) &&
        (query.isBlank() || listOf(tx.note, item.category?.name.orEmpty(), item.sourceAccount?.name.orEmpty(), item.targetAccount?.name.orEmpty()).any { text -> text.contains(query, ignoreCase = true) })
    }
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
    val grouped = remember(visible) {
        visible.groupBy { item ->
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
                GlassIconButton(Icons.Default.ChevronLeft, "上月", onClick = onPreviousMonth, size = 48.dp)
                GlassIconButton(Icons.Default.ChevronRight, "下月", onClick = onNextMonth, size = 48.dp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hero Monthly Overview: Flat Glass Card (bottom-bar recipe)
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "当月总支出 (元)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Commanding Financial Typographic Amount
                Text(
                    text = FormatHelper.currency(totalExpense),
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
                            text = "总收入: ${FormatHelper.currency(totalIncome)}",
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
                            text = "结余: ${FormatHelper.currency(balance)}",
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
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassSearchField(query, { query = it }, Modifier.weight(1f), placeholder = "搜索账单")
            GlassPillButton(label = "筛选 ${visible.size}", icon = Icons.Default.Tune, onClick = { showFilters = true })
        }
        if (visible.isEmpty()) {
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
                        text = if (transactions.isEmpty()) "当月暂无账单记录" else "没有符合条件的账单",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 116.dp)
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
                                    text = "支出 ${FormatHelper.currency(dayExpense)}",
                                    fontSize = 12.sp,
                                    color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                                )
                            }
                        }
                    }

                    // Inset grouped glass card per day
                    item {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = 4.dp
                        ) {
                            Column {
                                dayTransactions.forEachIndexed { index, item ->
                                    ModernTransactionRow(item = item, onClick = { detail = item })
                                    if (index < dayTransactions.size - 1) {
                                        GlassDivider(startIndent = 66.dp, endIndent = 16.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showFilters) GlassAlertDialog(onDismissRequest = { showFilters = false }, title = { Text("筛选当月账单") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("账户", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassChip(accountId == null, "全部", { accountId = null })
                transactions.flatMap { listOfNotNull(it.sourceAccount, it.targetAccount) }.distinctBy { it.id }.forEach { a ->
                    GlassChip(accountId == a.id, a.name, { accountId = a.id })
                }
            }
            Text("分类", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassChip(categoryId == null, "全部", { categoryId = null })
                transactions.mapNotNull { it.category }.distinctBy { it.id }.forEach { c ->
                    GlassChip(categoryId == c.id, c.name, { categoryId = c.id })
                }
            }
            GlassTextField(startDate, { startDate = it; dateError = null }, placeholder = "开始日期 yyyy-MM-dd（可空）")
            GlassTextField(endDate, { endDate = it; dateError = null }, placeholder = "结束日期 yyyy-MM-dd（可空）")
            dateError?.let { Text(it, color = LiquidGlassTokens.ExpenseRed, fontSize = 13.sp) }
        }
    }, confirmButton = { GlassDialogAction("应用", onClick = {
        try {
            val start = dateValue(startDate); val end = dateValue(endDate)
            require(start == null || end == null || start <= end) { "结束日期不能早于开始日期" }
            appliedStart = start
            appliedEnd = end?.let { Calendar.getInstance().apply { timeInMillis = it; add(Calendar.DAY_OF_MONTH, 1) }.timeInMillis }
            showFilters = false
        }
        catch (e: Exception) { dateError = e.message ?: "日期无效" }
    }, emphasized = true) }, dismissButton = { GlassDialogAction("重置", onClick = { accountId = null; categoryId = null; startDate = ""; endDate = ""; appliedStart = null; appliedEnd = null; dateError = null; showFilters = false }) })
    detail?.let { item ->
        GlassAlertDialog(onDismissRequest = { detail = null }, title = { Text("账单详情") },
            text = { Column(Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(FormatHelper.currency(item.transaction.amount), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight)
                Text("${when (item.transaction.type) { TransactionType.EXPENSE -> "支出"; TransactionType.INCOME -> "收入"; TransactionType.TRANSFER -> "转账" }} · ${item.category?.name ?: "转账"}")
                Text("${item.sourceAccount?.name.orEmpty()}${item.targetAccount?.let { " → " + it.name }.orEmpty()}")
                Text(java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA).format(java.util.Date(item.transaction.timestamp)))
                Text(item.transaction.note.ifBlank { "无备注" })
                GlassDialogAction("删除账单", onClick = { transactionToDelete = item.transaction; detail = null }, danger = true, modifier = Modifier.padding(top = 4.dp))
            } }, confirmButton = { GlassDialogAction("编辑", onClick = { onEditTransaction(item.transaction); detail = null }, emphasized = true) },
            dismissButton = { GlassDialogAction("关闭", onClick = { detail = null }) })
    }
    // Deletion Confirmation Dialog
    if (transactionToDelete != null) {
        val target = transactionToDelete!!
        GlassAlertDialog(
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
                    "金额: ${FormatHelper.currency(target.amount)}\n" +
                            "删除后相关账户余额将自动联动回滚。"
                )
            },
            confirmButton = {
                GlassDialogAction(
                    label = "删除",
                    onClick = {
                        onDeleteTransaction(target)
                        transactionToDelete = null
                    },
                    danger = true
                )
            },
            dismissButton = {
                GlassDialogAction("取消", onClick = { transactionToDelete = null })
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
                    IconHelper.getCategoryIcon("Transfer")
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
                // 按天分组后每行标注具体时刻
                append(java.text.SimpleDateFormat("HH:mm", java.util.Locale.CHINA).format(java.util.Date(tx.timestamp)))
                when (tx.type) {
                    TransactionType.TRANSFER -> {
                        append(" · ${sourceAccount?.name ?: "账户"} ➔ ${targetAccount?.name ?: "账户"}")
                    }
                    else -> {
                        sourceAccount?.name?.let { append(" · "); append(it) }
                    }
                }
                if (tx.note.isNotBlank()) {
                    append(" · ")
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
            text = "$amountPrefix${FormatHelper.currency(tx.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
