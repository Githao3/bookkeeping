package com.bookkeeping.app.ui.account

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bookkeeping.app.data.Money
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.AccountType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.ui.common.FormatHelper
import com.bookkeeping.app.ui.common.IconHelper
import com.bookkeeping.app.ui.components.GlassAlertDialog
import com.bookkeeping.app.ui.components.GlassChip
import com.bookkeeping.app.ui.components.GlassDialogAction
import com.bookkeeping.app.ui.components.GlassDivider
import com.bookkeeping.app.ui.components.GlassListRow
import com.bookkeeping.app.ui.components.GlassListSection
import com.bookkeeping.app.ui.components.GlassPillButton
import com.bookkeeping.app.ui.components.GlassTextField
import com.bookkeeping.app.ui.components.accountTypeTint
import com.bookkeeping.app.ui.theme.LiquidGlassTokens

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AccountScreen(accounts: List<AccountEntity>, transactions: List<TransactionWithDetails>, saving: Boolean, error: String?,
    onAddAccount: (AccountEntity, () -> Unit) -> Unit, onDeleteAccount: (AccountEntity, () -> Unit) -> Unit,
    onArchiveAccount: (AccountEntity, () -> Unit) -> Unit, modifier: Modifier = Modifier) {
    var showAdd by rememberSaveable { mutableStateOf(false) }
    var detailId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editId by rememberSaveable { mutableStateOf<Long?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }
    val detail = accounts.find { it.id == detailId }
    val isDark = isSystemInDarkTheme()

    Column(modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text("净资产", fontSize = 15.sp, fontWeight = FontWeight.Medium,
            color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight)
        Spacer(Modifier.height(2.dp))
        Text(
            FormatHelper.currency(accounts.filter { it.isIncludedInTotal }.sumOf { it.balance }),
            fontSize = 34.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp,
            color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
        )

        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("资产账户", fontSize = 17.sp, fontWeight = FontWeight.Bold,
                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight)
            GlassPillButton("添加账户", onClick = { showAdd = true }, icon = Icons.Default.Add, enabled = !saving)
        }
        Spacer(Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 116.dp)
        ) {
            if (accounts.isEmpty()) {
                item {
                    Text(
                        "还没有账户，点击右上角「添加账户」开始记录资产。",
                        fontSize = 14.sp,
                        color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight,
                        modifier = Modifier.padding(vertical = 40.dp)
                    )
                }
            } else {
                // iOS 风格内嵌分组列表：单个玻璃卡承载全部账户行
                item {
                    GlassListSection(shape = RoundedCornerShape(20.dp), elevation = 6.dp) {
                        accounts.forEachIndexed { index, account ->
                            GlassListRow(
                                title = account.name,
                                subtitle = label(account.type) + if (account.isArchived) " · 已归档" else "",
                                icon = IconHelper.getAccountIcon(account.iconName),
                                iconTint = accountTypeTint(account.type),
                                dimmed = account.isArchived,
                                onClick = { detailId = account.id },
                                trailing = {
                                    Text(
                                        FormatHelper.currency(account.balance),
                                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                                        color = if (account.isArchived) {
                                            if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight
                                        } else {
                                            if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                                        }
                                    )
                                }
                            )
                            if (index < accounts.size - 1) GlassDivider(startIndent = 58.dp)
                        }
                    }
                }
            }
        }
    }

    if (showAdd || editId != null) AccountEditor(accounts.find { it.id == editId }, saving, error,
        onDismiss = { if (!saving) { showAdd = false; editId = null } }, onConfirm = { account ->
            onAddAccount(account) { showAdd = false; editId = null }
        })
    detail?.let { account ->
        val flows = transactions.filter { it.transaction.sourceAccountId == account.id || it.transaction.targetAccountId == account.id }
        GlassAlertDialog(onDismissRequest = { if (!saving) detailId = null }, title = { Text(account.name) }, text = {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("余额", fontSize = 14.sp, color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight)
                    Text(FormatHelper.currency(account.balance), fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("关联流水", fontSize = 14.sp, color = if (isDark) LiquidGlassTokens.TextSecondaryDark else LiquidGlassTokens.TextSecondaryLight)
                    Text("${flows.size} 笔", fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight)
                }
                if (flows.isNotEmpty()) {
                    GlassDivider()
                    flows.forEach { item ->
                        Column(Modifier.padding(vertical = 4.dp)) {
                            Text(
                                "${FormatHelper.formatMonth(item.transaction.timestamp)} · ${item.category?.name ?: "转账"}　${FormatHelper.currency(item.transaction.amount)}",
                                fontSize = 13.sp, fontWeight = FontWeight.Medium,
                                color = if (isDark) LiquidGlassTokens.TextPrimaryDark else LiquidGlassTokens.TextPrimaryLight
                            )
                            if (item.transaction.note.isNotBlank()) {
                                Text(item.transaction.note, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = if (isDark) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight)
                            }
                        }
                    }
                }
                GlassDialogAction(if (account.isArchived) "恢复账户" else "归档账户", onClick = { onArchiveAccount(account) { detailId = null } }, enabled = !saving)
                GlassDialogAction("删除账户", onClick = { confirmDelete = true }, enabled = !saving, danger = true)
                if (error != null) Text(error, color = LiquidGlassTokens.ExpenseRed, fontSize = 13.sp)
            }
        }, confirmButton = { GlassDialogAction("编辑", onClick = { editId = account.id; detailId = null }, enabled = !saving, emphasized = true) },
        dismissButton = { GlassDialogAction("关闭", onClick = { detailId = null }, enabled = !saving) })
        if (confirmDelete) GlassAlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("删除账户？") },
            text = { Text(if (flows.isEmpty()) "删除「${account.name}」及其余额。" else "账户有关联流水，不能直接删除。归档会保留余额与全部历史。") },
            confirmButton = { GlassDialogAction(label = if (flows.isEmpty()) "删除" else if (account.isArchived) "关闭" else "归档", enabled = !saving, danger = flows.isEmpty(), onClick = {
                if (flows.isEmpty()) onDeleteAccount(account) { confirmDelete = false; detailId = null }
                else if (account.isArchived) { confirmDelete = false }
                else onArchiveAccount(account) { confirmDelete = false; detailId = null }
            }) },
            dismissButton = { GlassDialogAction("取消", onClick = { confirmDelete = false }) })
    }
}

private fun label(type: AccountType) = when(type) {
    AccountType.BANK_CARD -> "储蓄卡"; AccountType.CREDIT_CARD -> "信用卡"; AccountType.ALIPAY -> "支付宝"
    AccountType.WECHAT -> "微信"; AccountType.CASH -> "现金"; AccountType.OTHER -> "其他"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccountEditor(account: AccountEntity?, saving: Boolean, error: String?, onDismiss: () -> Unit, onConfirm: (AccountEntity) -> Unit) {
    var name by rememberSaveable(account?.id) { mutableStateOf(account?.name.orEmpty()) }
    var balance by rememberSaveable(account?.id) { mutableStateOf(Money.decimal(account?.balance ?: 0)) }
    var type by rememberSaveable(account?.id) { mutableStateOf(account?.type ?: AccountType.BANK_CARD) }
    var fieldError by remember { mutableStateOf<String?>(null) }
    GlassAlertDialog(onDismissRequest = onDismiss, title = { Text(if (account == null) "新增账户" else "编辑账户") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassTextField(name, { name = it }, placeholder = "账户名称（最多60字）", enabled = !saving)
            GlassTextField(balance, { balance = it }, placeholder = if (account == null) "初始余额（元）" else "当前余额（元）", enabled = !saving)
            if (account != null) Text("余额调整不会计为收入或支出。", fontSize = 12.sp,
                color = if (isSystemInDarkTheme()) LiquidGlassTokens.TextTertiaryDark else LiquidGlassTokens.TextTertiaryLight)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AccountType.entries.forEach { item -> GlassChip(type == item, label(item), { type = item }, enabled = !saving) }
            }
            (fieldError ?: error)?.let { Text(it, color = LiquidGlassTokens.ExpenseRed, fontSize = 13.sp) }
        }
    }, confirmButton = { GlassDialogAction(if (saving) "保存中…" else "保存", enabled = !saving, emphasized = true, onClick = {
        try {
            require(name.isNotBlank() && name.trim().length <= 60) { "账户名称须为 1–60 个字" }
            val cents = Money.parse(balance, allowNegative = true)
            val icon = when(type) { AccountType.WECHAT -> "Wechat"; AccountType.ALIPAY -> "Alipay"; AccountType.CASH -> "Cash"; AccountType.BANK_CARD, AccountType.CREDIT_CARD -> "BankCard"; else -> "AccountBalanceWallet" }
            fieldError = null
            onConfirm((account ?: AccountEntity(name = name, type = type)).copy(name = name.trim(), type = type, balance = cents, iconName = icon))
        } catch(e: Exception) { fieldError = e.message }
    }) }, dismissButton = { GlassDialogAction("取消", onClick = onDismiss, enabled = !saving) })
}
