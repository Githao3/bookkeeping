package com.bookkeeping.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Transaction
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsWithDetailsFlow(): Flow<List<TransactionWithDetails>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetweenWithDetailsFlow(startTime: Long, endTime: Long): Flow<List<TransactionWithDetails>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = :type AND timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalAmountByTypeBetweenFlow(type: TransactionType, startTime: Long, endTime: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)
    @Query("SELECT * FROM transactions ORDER BY id")
    suspend fun snapshot(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clear()
    @Query("SELECT COUNT(*) FROM transactions WHERE sourceAccountId = :id OR targetAccountId = :id")
    suspend fun accountUsage(id: Long): Int
}
