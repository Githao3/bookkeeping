package com.bookkeeping.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, // 支出
    INCOME,  // 收入
    TRANSFER // 转账
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long? = null,
    val sourceAccountId: Long,
    val targetAccountId: Long? = null,
    val timestamp: Long,
    val note: String = ""
)
