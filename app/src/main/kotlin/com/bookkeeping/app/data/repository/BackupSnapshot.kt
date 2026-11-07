package com.bookkeeping.app.data.repository

import com.bookkeeping.app.data.Money
import com.bookkeeping.app.data.local.entity.*

data class BackupSnapshot(val accounts: List<AccountEntity>, val categories: List<CategoryEntity>, val transactions: List<TransactionEntity>) {
    fun validate() {
        require(accounts.all { it.id > 0 && it.name.isNotBlank() && it.name.length <= 60 && it.balance in -Money.MAX_CENTS..Money.MAX_CENTS }) { "备份账户无效" }
        require(categories.all { it.id > 0 && it.name.isNotBlank() && it.type != TransactionType.TRANSFER }) { "备份分类无效" }
        require(accounts.map { it.id }.distinct().size == accounts.size && categories.map { it.id }.distinct().size == categories.size && transactions.map { it.id }.distinct().size == transactions.size) { "备份存在重复编号" }
        val accountIds = accounts.map { it.id }.toSet()
        val categoryMap = categories.associateBy { it.id }
        transactions.forEach { tx ->
            require(tx.id > 0 && tx.amount in 1..Money.MAX_CENTS && tx.timestamp > 0 && tx.note.length <= 2000 && tx.sourceAccountId in accountIds) { "备份流水或关联无效" }
            if (tx.type == TransactionType.TRANSFER) {
                require(tx.targetAccountId in accountIds && tx.targetAccountId != tx.sourceAccountId && tx.categoryId == null) { "备份转账关联无效" }
            } else require(tx.targetAccountId == null && categoryMap[tx.categoryId]?.type == tx.type) { "备份分类关联无效" }
        }
    }
}
