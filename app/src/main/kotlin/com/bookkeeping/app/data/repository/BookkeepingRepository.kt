package com.bookkeeping.app.data.repository

import androidx.room.withTransaction
import com.bookkeeping.app.data.local.AppDatabase
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.CategoryEntity
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

class BookkeepingRepository(
    private val database: AppDatabase
) {
    private val transactionDao = database.transactionDao()
    private val categoryDao = database.categoryDao()
    private val accountDao = database.accountDao()

    val allTransactions: Flow<List<TransactionWithDetails>> =
        transactionDao.getAllTransactionsWithDetailsFlow()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionWithDetails>> =
        transactionDao.getTransactionsBetweenWithDetailsFlow(startTime, endTime)

    fun getTotalAmountByType(type: TransactionType, startTime: Long, endTime: Long): Flow<Double> =
        transactionDao.getTotalAmountByTypeBetweenFlow(type, startTime, endTime)

    val allAccounts: Flow<List<AccountEntity>> =
        accountDao.getAllAccountsFlow()

    fun getCategories(type: TransactionType): Flow<List<CategoryEntity>> =
        categoryDao.getCategoriesByTypeFlow(type)

    val allCategories: Flow<List<CategoryEntity>> =
        categoryDao.getAllCategoriesFlow()

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return database.withTransaction {
            val id = transactionDao.insert(transaction)
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, -transaction.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, transaction.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, -transaction.amount)
                    transaction.targetAccountId?.let { targetId ->
                        accountDao.adjustBalance(targetId, transaction.amount)
                    }
                }
            }
            id
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.withTransaction {
            when (transaction.type) {
                TransactionType.EXPENSE -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, transaction.amount)
                }
                TransactionType.INCOME -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, -transaction.amount)
                }
                TransactionType.TRANSFER -> {
                    accountDao.adjustBalance(transaction.sourceAccountId, transaction.amount)
                    transaction.targetAccountId?.let { targetId ->
                        accountDao.adjustBalance(targetId, -transaction.amount)
                    }
                }
            }
            transactionDao.delete(transaction)
        }
    }

    suspend fun addAccount(account: AccountEntity): Long = accountDao.insert(account)
    suspend fun updateAccount(account: AccountEntity) = accountDao.update(account)
    suspend fun deleteAccount(account: AccountEntity) = accountDao.delete(account)

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insert(category)
}
