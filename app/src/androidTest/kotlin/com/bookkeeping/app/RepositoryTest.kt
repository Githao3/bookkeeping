package com.bookkeeping.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.bookkeeping.app.data.local.AppDatabase
import com.bookkeeping.app.data.local.entity.*
import com.bookkeeping.app.data.repository.*
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*

class RepositoryTest {
    private lateinit var db: AppDatabase
    private lateinit var repo: BookkeepingRepository
    private val context = ApplicationProvider.getApplicationContext<Context>()
    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repo = BookkeepingRepository(db)
        db.accountDao().insertAll(listOf(AccountEntity(1,"现金",AccountType.CASH,10000),AccountEntity(2,"银行卡",AccountType.BANK_CARD,20000)))
        db.categoryDao().insertAll(listOf(CategoryEntity(1,"餐饮",TransactionType.EXPENSE,"Restaurant",0),CategoryEntity(2,"工资",TransactionType.INCOME,"Work",0)))
    }
    @After fun close() { db.close() }
    private fun tx(amount: Long = 8500, type: TransactionType = TransactionType.EXPENSE) = TransactionEntity(amount = amount, type = type, categoryId = if (type == TransactionType.TRANSFER) null else if(type == TransactionType.INCOME) 2 else 1, sourceAccountId = 1, targetAccountId = if(type == TransactionType.TRANSFER) 2 else null, timestamp = 1793973600000L)
    private suspend fun balance(id: Long) = db.accountDao().getAccountById(id)!!.balance
    @Test fun expenseAndRepeatedDelete() = runBlocking {
        val id = repo.addTransaction(tx()); assertEquals(1500L,balance(1))
        val saved = tx().copy(id=id); repo.deleteTransaction(saved.copy(amount=1)); repo.deleteTransaction(saved)
        assertEquals(10000L,balance(1)); assertTrue(db.transactionDao().snapshot().isEmpty())
    }
    @Test fun transferAndEditAreAtomic() = runBlocking {
        val id = repo.addTransaction(tx(1000,TransactionType.TRANSFER))
        assertEquals(9000L,balance(1)); assertEquals(21000L,balance(2))
        repo.updateTransaction(tx(500,TransactionType.INCOME).copy(id=id))
        assertEquals(10500L,balance(1)); assertEquals(20000L,balance(2))
    }
    @Test fun invalidEndpointsAndCategoryAreRejected() = runBlocking {
        for (invalid in listOf(tx(0),tx(-1),tx().copy(sourceAccountId=99),tx().copy(categoryId=2),tx(100,TransactionType.TRANSFER).copy(targetAccountId=1),tx(100,TransactionType.TRANSFER).copy(targetAccountId=99))) {
            assertTrue(runCatching { repo.addTransaction(invalid) }.isFailure)
        }
        assertEquals(10000L,balance(1)); assertTrue(db.transactionDao().snapshot().isEmpty())
    }
    @Test fun invalidEditLeavesOldBalanceAndRow() = runBlocking {
        val id=repo.addTransaction(tx(1000)); val before=repo.snapshot()
        assertTrue(runCatching { repo.updateTransaction(tx(1000,TransactionType.TRANSFER).copy(id=id,targetAccountId=1)) }.isFailure)
        assertEquals(before,repo.snapshot())
    }
    @Test fun archivePreservesHistoryAndBlocksNewEntries() = runBlocking {
        repo.addTransaction(tx(1000,TransactionType.TRANSFER))
        assertTrue(runCatching { repo.deleteAccount(db.accountDao().getAccountById(2)!!) }.isFailure)
        repo.archiveAccount(db.accountDao().getAccountById(1)!!)
        assertTrue(runCatching { repo.addTransaction(tx()) }.isFailure)
        assertEquals(9000L,balance(1)); assertEquals(1,db.transactionDao().snapshot().size)
    }
    @Test fun emptyAccountDeletion() = runBlocking { repo.deleteAccount(db.accountDao().getAccountById(2)!!); assertNull(db.accountDao().getAccountById(2)) }
    @Test fun accountDeletionCanBeUndoneWithoutLosingBalance() = runBlocking {
        val account=db.accountDao().getAccountById(2)!!; repo.deleteAccount(account); repo.undoDeleteAccount(account)
        assertEquals(account,db.accountDao().getAccountById(2))
    }
    @Test fun archivedFlowCanBeEditedDeletedAndUndone() = runBlocking {
        val id=repo.addTransaction(tx(1000)); repo.archiveAccount(db.accountDao().getAccountById(1)!!)
        val edited=tx(2000).copy(id=id); repo.updateTransaction(edited)
        assertEquals(8000L,balance(1)); repo.deleteTransaction(edited); repo.undoDelete(edited)
        assertEquals(8000L,balance(1)); assertEquals(edited,db.transactionDao().getTransactionById(id))
    }
    @Test fun failedRecoveryBackupPreventsReplacement() = runBlocking {
        val before=repo.snapshot(); val empty=before.copy(transactions=emptyList(),accounts=emptyList(),categories=emptyList())
        assertTrue(runCatching { repo.restore(empty) { error("disk full") } }.isFailure)
        assertEquals(before,repo.snapshot())
    }
    @Test fun insertConflictCannotDoubleApply() = runBlocking {
        val id=repo.addTransaction(tx(100)); val before=repo.snapshot()
        assertTrue(runCatching { repo.addTransaction(tx(100).copy(id=id)) }.isFailure); assertEquals(before,repo.snapshot())
    }
    @Test fun balanceOverflowRollsBackInsert() = runBlocking {
        db.accountDao().update(db.accountDao().getAccountById(1)!!.copy(balance=com.bookkeeping.app.data.Money.MAX_CENTS))
        assertTrue(runCatching { repo.addTransaction(tx(1,TransactionType.INCOME)) }.isFailure)
        assertTrue(db.transactionDao().snapshot().isEmpty())
    }
    @Test fun completeBackupRoundTripPreservesAllAssociations() = runBlocking {
        repo.addTransaction(tx(100,TransactionType.TRANSFER).copy(note="长备注,\"引号\"\n下一行"))
        repo.archiveAccount(db.accountDao().getAccountById(2)!!)
        val original=repo.snapshot(); val decoded=BackupCodec.decode(BackupCodec.encode(original))
        repo.deleteTransaction(db.transactionDao().snapshot().first())
        repo.restore(decoded); assertEquals(original,repo.snapshot())
    }
    @Test fun invalidBackupDoesNotReplaceData() = runBlocking {
        val before=repo.snapshot(); val broken=before.copy(transactions=listOf(tx(1,TransactionType.TRANSFER).copy(id=1,targetAccountId=99)))
        assertTrue(runCatching { repo.restore(broken) }.isFailure); assertEquals(before,repo.snapshot())
        val text=BackupCodec.encode(before).replace("\"balance\": 10000", "\"balance\": 10000.5")
        assertTrue(runCatching { BackupCodec.decode(text) }.isFailure)
    }
    @Test fun csvEscapingAndFormulaProtection() = runBlocking {
        repo.addTransaction(tx(100).copy(note="=SUM(1,2)\n\"quoted\""))
        val csv=BackupCodec.csv(repo.snapshot())
        assertTrue(csv.startsWith("\uFEFF")); assertTrue(csv.contains("'=SUM(1,2)")); assertTrue(csv.contains("\"\"quoted\"\"")); assertTrue(csv.contains("\"1.00\""))
    }
    @Test fun migrationPreservesLegacyValuesAndBackup() = runBlocking {
        val name="migration-test-${System.nanoTime()}"; val file=context.getDatabasePath(name); file.parentFile!!.mkdirs()
        val schema=InstrumentationRegistry.getInstrumentation().context.assets.open("com.bookkeeping.app.data.local.AppDatabase/1.json").bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        SQLiteDatabase.openOrCreateDatabase(file,null).use { legacy ->
            val entities=schema.getJSONArray("entities")
            for(i in 0 until entities.length()) { val entity=entities.getJSONObject(i); legacy.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}",entity.getString("tableName"))) }
            legacy.execSQL("INSERT INTO accounts VALUES(1,'旧账户','CASH',1.005,'Cash',0,1)")
            legacy.execSQL("INSERT INTO categories VALUES(1,'餐饮','EXPENSE','Restaurant',0,1)")
            legacy.execSQL("INSERT INTO transactions VALUES(1,0.1,'EXPENSE',1,1,NULL,1793973600000,'旧账')")
            legacy.version=1
        }
        val migrated=Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            assertEquals(101L,migrated.accountDao().getAccountById(1)!!.balance)
            assertEquals(10L,migrated.transactionDao().getTransactionById(1)!!.amount)
            migrated.openHelper.readableDatabase.query("SELECT balance FROM migration_v1_accounts").use { cursor -> assertTrue(cursor.moveToFirst()); assertEquals(1.005,cursor.getDouble(0),0.000001) }
        } finally { migrated.close(); context.deleteDatabase(name) }
    }
}
