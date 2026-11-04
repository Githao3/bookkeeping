package com.bookkeeping.app.ui.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.CategoryEntity
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.components.NumberPad
import com.bookkeeping.app.ui.theme.liquidGlass
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionContent(
    accounts: List<AccountEntity>,
    expenseCategories: List<CategoryEntity>,
    incomeCategories: List<CategoryEntity>,
    onSaveTransaction: (TransactionEntity) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountString by remember { mutableStateOf("0") }
    val isDark = isSystemInDarkTheme()

    var selectedCategoryId by remember(selectedType, expenseCategories, incomeCategories) {
        mutableStateOf(
            if (selectedType == TransactionType.EXPENSE) {
                expenseCategories.firstOrNull()?.id
            } else {
                incomeCategories.firstOrNull()?.id
            }
        )
    }

    var selectedSourceAccountId by remember(accounts) {
        mutableStateOf(accounts.firstOrNull()?.id ?: 0L)
    }
    var selectedTargetAccountId by remember(accounts) {
        mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L)
    }

    var selectedTimestamp by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var note by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    fun calculateAmount(): Double {
        return try {
            val sanitized = amountString.trim()
            if (sanitized.contains("+")) {
                val parts = sanitized.split("+")
                parts.sumOf { it.toDoubleOrNull() ?: 0.0 }
            } else if (sanitized.contains("-") && !sanitized.startsWith("-")) {
                val parts = sanitized.split("-")
                val first = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0
                val rest = parts.drop(1).sumOf { it.toDoubleOrNull() ?: 0.0 }
                first - rest
            } else {
                sanitized.toDoubleOrNull() ?: 0.0
            }
        } catch (_: Exception) {
            0.0
        }
    }

    fun handleDigit(digit: String) {
        if (digit == ".") {
            val lastToken = amountString.split("+", "-").lastOrNull() ?: ""
            if (!lastToken.contains(".")) {
                amountString += "."
            }
        } else if (digit in listOf("+", "-")) {
            if (!amountString.endsWith("+") && !amountString.endsWith("-") && amountString != "0") {
                amountString += digit
            }
        } else {
            if (amountString == "0") {
                amountString = digit
            } else {
                amountString += digit
            }
        }
    }

    fun handleDelete() {
        if (amountString.length > 1) {
            amountString = amountString.dropLast(1)
        } else {
            amountString = "0"
        }
    }

    fun handleClear() {
        amountString = "0"
    }

    fun handleComplete() {
        val finalAmount = calculateAmount()
        if (finalAmount <= 0.0) return

        val entity = TransactionEntity(
            amount = finalAmount,
            type = selectedType,
            categoryId = if (selectedType == TransactionType.TRANSFER) null else selectedCategoryId,
            sourceAccountId = selectedSourceAccountId,
            targetAccountId = if (selectedType == TransactionType.TRANSFER) selectedTargetAccountId else null,
            timestamp = selectedTimestamp,
            note = note.trim()
        )
        onSaveTransaction(entity)
        onDismiss()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
    ) {
        // Header with Close and Liquid Pill Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .liquidGlass(shape = CircleShape, elevation = 2.dp)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "关闭",
                    modifier = Modifier.size(20.dp),
                    tint = if (isDark) Color.White else Color(0xFF1F2937)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Liquid Segmented Switcher
            Row(
                modifier = Modifier
                    .weight(1f)
                    .liquidGlass(shape = CircleShape, elevation = 4.dp)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(
                    TransactionType.EXPENSE to "支出",
                    TransactionType.INCOME to "收入",
                    TransactionType.TRANSFER to "转账"
                ).forEach { (type, label) ->
                    val isSelected = selectedType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    if (isDark) Color(0x333B82F6) else Color(0x1F2563EB)
                                } else Color.Transparent
                            )
                            .clickable { selectedType = type }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                            } else {
                                if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(48.dp))
        }

        // Amount Display: Liquid Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .liquidGlass(
                    shape = RoundedCornerShape(22.dp),
                    elevation = 6.dp
                )
                .padding(vertical = 14.dp, horizontal = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "¥",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                    modifier = Modifier.padding(bottom = 6.dp, end = 6.dp)
                )
                Text(
                    text = amountString,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF111827),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Category selection for Expense & Income
        if (selectedType != TransactionType.TRANSFER) {
            val categories = if (selectedType == TransactionType.EXPENSE) expenseCategories else incomeCategories

            Text(
                text = "选择分类",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                maxItemsInEachRow = 5,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                categories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    Column(
                        modifier = Modifier
                            .padding(vertical = 6.dp, horizontal = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Color(cat.colorHex)
                                    else if (isDark) Color(0x33FFFFFF)
                                    else Color(0x1F000000)
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getCategoryIcon(cat.iconName),
                                contentDescription = cat.name,
                                tint = if (isSelected) Color.White else Color(cat.colorHex),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) {
                                if (isDark) Color.White else Color(0xFF111827)
                            } else {
                                if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Account Selector
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            if (selectedType == TransactionType.TRANSFER) {
                Text(
                    text = "转出账户",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = account.id == selectedSourceAccountId,
                            onClick = { selectedSourceAccountId = account.id },
                            label = { Text(account.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = IconHelper.getAccountIcon(account.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "转入账户",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = account.id == selectedTargetAccountId,
                            onClick = { selectedTargetAccountId = account.id },
                            label = { Text(account.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = IconHelper.getAccountIcon(account.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            } else {
                Text(
                    text = "支付账户",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { account ->
                        FilterChip(
                            selected = account.id == selectedSourceAccountId,
                            onClick = { selectedSourceAccountId = account.id },
                            label = { Text(account.name, fontSize = 12.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = IconHelper.getAccountIcon(account.iconName),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }

        // Date & Note Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { showDatePicker = true },
                label = {
                    Text(
                        SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(selectedTimestamp)),
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "选择日期",
                        modifier = Modifier.size(15.dp)
                    )
                }
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                placeholder = { Text("添加备注...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "备注",
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Number Pad
        NumberPad(
            onDigitClick = { handleDigit(it) },
            onDeleteClick = { handleDelete() },
            onClearClick = { handleClear() },
            onCompleteClick = { handleComplete() }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedTimestamp
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedTimestamp = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
