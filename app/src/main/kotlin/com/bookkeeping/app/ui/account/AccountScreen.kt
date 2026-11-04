package com.bookkeeping.app.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.bookkeeping.app.ui.theme.liquidGlass

@Composable
fun AccountScreen(
    accounts: List<AccountEntity>,
    onAddAccount: (AccountEntity) -> Unit,
    onDeleteAccount: (AccountEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }
    val isDark = isSystemInDarkTheme()

    val totalNetAssets = remember(accounts) {
        accounts.filter { it.isIncludedInTotal }.sumOf { it.balance }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Net Assets Header: Liquid Glass Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(26.dp),
                    elevation = 12.dp
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "净资产 (元)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDark) Color(0xFF9CA3AF) else Color(0xFF6B7280)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = FormatHelper.formatCurrency(totalNetAssets),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF111827)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "共计 ${accounts.size} 个账户参与统计",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF6B7280) else Color(0xFF9CA3AF)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Title & Add Account Glass Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "资产账户",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF111827)
            )

            // Liquid Glass Add Button
            Box(
                modifier = Modifier
                    .liquidGlass(shape = CircleShape, elevation = 4.dp)
                    .clickable { showAddDialog = true }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加账户",
                        tint = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "添加账户",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) Color(0xFF60A5FA) else Color(0xFF2563EB)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Accounts List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(accounts, key = { it.id }) { account ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .liquidGlass(
                            shape = RoundedCornerShape(18.dp),
                            elevation = 4.dp
                        )
                        .clickable { accountToDelete = account }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val iconColor = Color(account.colorHex)
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(iconColor.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = IconHelper.getAccountIcon(account.iconName),
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) Color.White else Color(0xFF111827)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isDark) Color(0x33FFFFFF) else Color(0x1A000000)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = when (account.type) {
                                        AccountType.WECHAT -> "微信支付"
                                        AccountType.ALIPAY -> "支付宝"
                                        AccountType.BANK_CARD -> "储蓄卡"
                                        AccountType.CREDIT_CARD -> "信用卡"
                                        AccountType.CASH -> "现金"
                                        AccountType.OTHER -> "其它"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDark) Color(0xFFD1D5DB) else Color(0xFF4B5563)
                                )
                            }
                        }

                        Text(
                            text = FormatHelper.formatCurrency(account.balance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF111827)
                        )
                    }
                }
            }

            // Bottom padding for floating navigation bar
            item {
                Spacer(modifier = Modifier.height(88.dp))
            }
        }
    }

    // Add Account Dialog
    if (showAddDialog) {
        var accountName by remember { mutableStateOf("") }
        var initialBalanceStr by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(AccountType.BANK_CARD) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新增资产账户") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        label = { Text("账户名称 (如: 建设银行)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = initialBalanceStr,
                        onValueChange = { initialBalanceStr = it },
                        label = { Text("初始余额 (元)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("账户类型", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            AccountType.BANK_CARD to "银行卡",
                            AccountType.ALIPAY to "支付宝",
                            AccountType.WECHAT to "微信",
                            AccountType.CASH to "现金"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (accountName.isNotBlank()) {
                            val balance = initialBalanceStr.toDoubleOrNull() ?: 0.0
                            val (iconName, colorHex) = when (selectedType) {
                                AccountType.WECHAT -> "Wechat" to 0xFF07C160
                                AccountType.ALIPAY -> "Alipay" to 0xFF1677FF
                                AccountType.BANK_CARD -> "BankCard" to 0xFF3F51B5
                                AccountType.CREDIT_CARD -> "BankCard" to 0xFFE91E63
                                AccountType.CASH -> "Cash" to 0xFFFFB300
                                AccountType.OTHER -> "Other" to 0xFF78909C
                            }
                            onAddAccount(
                                AccountEntity(
                                    name = accountName.trim(),
                                    type = selectedType,
                                    balance = balance,
                                    iconName = iconName,
                                    colorHex = colorHex
                                )
                            )
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // Delete Account Dialog
    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            icon = { Icon(Icons.Default.DeleteOutline, contentDescription = null) },
            title = { Text("删除账户") },
            text = { Text("确定要删除账户“${account.name}”吗？此操作不可逆。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteAccount(account)
                        accountToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
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
