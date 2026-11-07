package com.bookkeeping.app.data.repository

import androidx.room.withTransaction
import com.bookkeeping.app.data.Money
import com.bookkeeping.app.data.local.AppDatabase
import com.bookkeeping.app.data.local.entity.*
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

class BookkeepingRepository(private val database: AppDatabase) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()
    val allTransactions = transactionDao.getAllTransactionsWithDetailsFlow()
    val allAccounts = accountDao.getAllAccountsFlow()
    val allCategories = categoryDao.getAllCategoriesFlow()
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionWithDetails>> = transactionDao.getTransactionsBetweenWithDetailsFlow(startTime, endTime)
    fun getTotalAmountByType(type: TransactionType, startTime: Long, endTime: Long): Flow<Long> = transactionDao.getTotalAmountByTypeBetweenFlow(type, startTime, endTime)
    fun getCategories(type: TransactionType) = categoryDao.getCategoriesByTypeFlow(type)

    private suspend fun validate(tx: TransactionEntity, permittedArchived: Set<Long> = emptySet()) {
        require(tx.amount in 1..Money.MAX_CENTS) { "金额须大于零且在有效范围内" }
        require(tx.timestamp > 0 && tx.note.length <= 2000) { "日期或备注无效" }
        val source = accountDao.getAccountById(tx.sourceAccountId)
        require(source != null && (!source.isArchived || source.id in permittedArchived)) { "请选择有效的账户" }
        if (tx.type == TransactionType.TRANSFER) {
            require(tx.targetAccountId != null && tx.targetAccountId != tx.sourceAccountId) { "转出和转入账户必须不同" }
            val target = accountDao.getAccountById(tx.targetAccountId)
            require(target != null && (!target.isArchived || target.id in permittedArchived)) { "请选择有效的转入账户" }
            require(tx.categoryId == null) { "转账不能设置收支分类" }
        } else {
            require(tx.targetAccountId == null) { "收支不需要转入账户" }
            val category = tx.categoryId?.let { categoryDao.getCategoryById(it) }
            require(category?.type == tx.type) { "请选择对应的分类" }
        }
    }
    private suspend fun adjust(id: Long, delta: Long) {
        val account = accountDao.getAccountById(id) ?: error("账户已失效，操作已取消")
        val balance = Math.addExact(account.balance, delta)
        require(balance in -Money.MAX_CENTS..Money.MAX_CENTS) { "账户余额超出范围" }
        accountDao.update(account.copy(balance = balance))
    }
    private suspend fun apply(tx: TransactionEntity, direction: Long) {
        val amount = Math.multiplyExact(tx.amount, direction)
        adjust(tx.sourceAccountId, if (tx.type == TransactionType.INCOME) amount else -amount)
        if (tx.type == TransactionType.TRANSFER) adjust(tx.targetAccountId ?: error("转入账户失效"), amount)
    }
    suspend fun addTransaction(tx: TransactionEntity): Long = database.withTransaction {
        require(tx.id == 0L) { "新增流水不能覆盖已有记录" }
        validate(tx)
        val id = transactionDao.insert(tx)
        apply(tx, 1)
        id
    }
    suspend fun updateTransaction(tx: TransactionEntity) = database.withTransaction {
        val old = transactionDao.getTransactionById(tx.id) ?: error("流水已删除，请刷新")
        validate(tx, setOfNotNull(old.sourceAccountId, old.targetAccountId))
        apply(old, -1)
        transactionDao.update(tx)
        apply(tx, 1)
    }
    suspend fun deleteTransaction(tx: TransactionEntity) = database.withTransaction {
        transactionDao.getTransactionById(tx.id)?.let { old ->
            apply(old, -1)
            transactionDao.deleteById(old.id)
        }
    }
    suspend fun undoDelete(tx: TransactionEntity) = database.withTransaction {
        require(transactionDao.getTransactionById(tx.id) == null) { "流水已存在，不能重复恢复" }
        validate(tx, setOfNotNull(tx.sourceAccountId, tx.targetAccountId))
        transactionDao.insert(tx)
        apply(tx, 1)
    }
    suspend fun addAccount(account: AccountEntity): Long = database.withTransaction {
        require(account.id == 0L && account.name.isNotBlank() && account.name.length <= 60) { "账户名称须为 1–60 个字" }
        require(account.balance in -Money.MAX_CENTS..Money.MAX_CENTS) { "余额超出范围" }
        accountDao.insert(account)
    }
    suspend fun updateAccount(account: AccountEntity) = database.withTransaction {
        require(account.name.isNotBlank() && account.name.length <= 60) { "账户名称须为 1–60 个字" }
        require(account.balance in -Money.MAX_CENTS..Money.MAX_CENTS) { "余额超出范围" }
        val current = accountDao.getAccountById(account.id) ?: error("账户已删除")
        accountDao.update(current.copy(name = account.name, type = account.type, iconName = account.iconName,
            colorHex = account.colorHex, balance = account.balance, isIncludedInTotal = account.isIncludedInTotal))
    }
    suspend fun archiveAccount(account: AccountEntity) = database.withTransaction {
        val current = accountDao.getAccountById(account.id) ?: error("账户已删除")
        accountDao.update(current.copy(isArchived = !current.isArchived))
    }
    suspend fun deleteAccount(account: AccountEntity) = database.withTransaction {
        require(transactionDao.accountUsage(account.id) == 0) { "账户有关联流水，请使用归档保留历史" }
        accountDao.getAccountById(account.id)?.let { accountDao.delete(it) }
    }
    suspend fun undoDeleteAccount(account: AccountEntity) = database.withTransaction {
        require(accountDao.getAccountById(account.id) == null) { "账户已存在，不能重复恢复" }
        accountDao.insert(account)
    }
    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun snapshot(): BackupSnapshot = database.withTransaction {
        BackupSnapshot(accountDao.snapshot(), categoryDao.snapshot(), transactionDao.snapshot())
    }
    suspend fun restore(snapshot: BackupSnapshot, beforeReplace: suspend (BackupSnapshot) -> Unit = {}) = database.withTransaction {
        snapshot.validate()
        beforeReplace(BackupSnapshot(accountDao.snapshot(), categoryDao.snapshot(), transactionDao.snapshot()))
        transactionDao.clear()
        categoryDao.clear()
        accountDao.clear()
        accountDao.insertAll(snapshot.accounts)
        categoryDao.insertAll(snapshot.categories)
        snapshot.transactions.forEach { transactionDao.insert(it) }
    }
}
