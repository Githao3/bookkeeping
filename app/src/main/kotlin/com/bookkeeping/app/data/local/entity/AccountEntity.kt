package com.bookkeeping.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    WECHAT,     // 微信钱包
    ALIPAY,     // 支付宝
    BANK_CARD,  // 银行卡/储蓄卡
    CREDIT_CARD,// 信用卡
    CASH,       // 现金
    OTHER       // 其它账户
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Long = 0,
    val iconName: String = "AccountBalanceWallet",
    val colorHex: Long = 0xFF4CAF50,
    val isIncludedInTotal: Boolean = true,
    val isArchived: Boolean = false
)
