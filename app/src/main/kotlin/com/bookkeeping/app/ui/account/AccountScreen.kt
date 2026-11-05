package com.bookkeeping.app.ui.account

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.AccountType
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.theme.LiquidGlassTokens
import com.bookkeeping.app.ui.theme.liquidGlass

@Composable
fun AccountScreen(
    accounts: List<AccountEntity>,
    onAddAccount: (AccountEntity) -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    val netWorth = remember(accounts) {
        accounts.sumOf { it.balance }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Net Assets: Liquid Glass Card
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
                    text = "净资产 (元)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = FormatHelper.formatCurrency(netWorth),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "共计 ${accounts.size} 个账户参与折算",
                    fontSize = 12.sp,
                    color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Accounts Header & Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "资产账户",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            )

            // Add Account Button
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x2660A5FA) else Color(0x14007AFF))
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加账户",
                    tint = LiquidGlassTokens.AccentBlue,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "添加账户",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LiquidGlassTokens.AccentBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Accounts Grouped Inset Card
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
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
                        accounts.forEachIndexed { index, account ->
                            AccountItemRow(
                                account = account,
                                onDelete = { accountToDelete = account }
                            )
                            if (index < accounts.size - 1) {
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

    if (showAddDialog) {
        AddAccountDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onAddAccount(it)
                showAddDialog = false
            }
        )
    }

    if (accountToDelete != null) {
        val target = accountToDelete!!
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            icon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = LiquidGlassTokens.ExpenseRed
                )
            },
            title = { Text("确认删除该账户？", fontWeight = FontWeight.Bold) },
            text = { Text("删除账户「${target.name}」将同步级联删除名下所有流水，此操作不可逆。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(target)
                        accountToDelete = null
                    }
                ) {
                    Text("确认删除", color = LiquidGlassTokens.ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { accountToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun AccountItemRow(
    account: AccountEntity,
    onDelete: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val baseColor = Color(account.colorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDelete)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(baseColor.copy(alpha = if (isDark) 0.25f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = IconHelper.getAccountIcon(account.iconName),
                contentDescription = account.name,
                tint = baseColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            val typeLabel = when (account.type) {
                AccountType.WECHAT -> "微信支付"
                AccountType.ALIPAY -> "支付宝"
                AccountType.BANK_CARD -> "储蓄卡"
                AccountType.CREDIT_CARD -> "信用卡"
                AccountType.CASH -> "现金"
                else -> "其它账户"
            }
            Text(
                text = typeLabel,
                fontSize = 12.sp,
                color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight
            )
        }

        Text(
            text = FormatHelper.formatCurrency(account.balance),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
        )
    }
}

@Composable
private fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balanceString by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.BANK_CARD) }
    var selectedIcon by remember { mutableStateOf("credit_card") }
    var selectedColor by remember { mutableStateOf(0xFF1677FF) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增资产账户", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("账户名称 (如: 工商银行)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balanceString,
                    onValueChange = { balanceString = it },
                    label = { Text("初始余额 (元)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        AccountType.BANK_CARD to "储蓄卡",
                        AccountType.CREDIT_CARD to "信用卡",
                        AccountType.ALIPAY to "支付宝",
                        AccountType.WECHAT to "微信",
                        AccountType.CASH to "现金"
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                when (type) {
                                    AccountType.BANK_CARD -> {
                                        selectedIcon = "credit_card"
                                        selectedColor = 0xFF1677FF
                                    }
                                    AccountType.CREDIT_CARD -> {
                                        selectedIcon = "credit_card"
                                        selectedColor = 0xFFF5222D
                                    }
                                    AccountType.ALIPAY -> {
                                        selectedIcon = "payments"
                                        selectedColor = 0xFF1677FF
                                    }
                                    AccountType.WECHAT -> {
                                        selectedIcon = "payments"
                                        selectedColor = 0xFF52C41A
                                    }
                                    AccountType.CASH -> {
                                        selectedIcon = "attach_money"
                                        selectedColor = 0xFFFAAD14
                                    }
                                    else -> {}
                                }
                            },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val initBalance = balanceString.toDoubleOrNull() ?: 0.0
                        onConfirm(
                            AccountEntity(
                                name = name.trim(),
                                type = selectedType,
                                balance = initBalance,
                                iconName = selectedIcon,
                                colorHex = selectedColor
                            )
                        )
                    }
                }
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
