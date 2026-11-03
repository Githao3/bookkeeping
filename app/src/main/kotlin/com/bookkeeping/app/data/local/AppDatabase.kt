package com.bookkeeping.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bookkeeping.app.data.local.dao.AccountDao
import com.bookkeeping.app.data.local.dao.CategoryDao
import com.bookkeeping.app.data.local.dao.TransactionDao
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.AccountType
import com.bookkeeping.app.data.local.entity.CategoryEntity
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        AccountEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bookkeeping_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val accountDao = database.accountDao()
                val categoryDao = database.categoryDao()

                // Default Accounts
                val defaultAccounts = listOf(
                    AccountEntity(name = "微信零钱", type = AccountType.WECHAT, balance = 1500.0, iconName = "Wechat", colorHex = 0xFF07C160),
                    AccountEntity(name = "支付宝", type = AccountType.ALIPAY, balance = 2800.0, iconName = "Alipay", colorHex = 0xFF1677FF),
                    AccountEntity(name = "招商银行储蓄卡", type = AccountType.BANK_CARD, balance = 12500.0, iconName = "BankCard", colorHex = 0xFFE53935),
                    AccountEntity(name = "现金钱包", type = AccountType.CASH, balance = 300.0, iconName = "Cash", colorHex = 0xFFFFB300)
                )
                accountDao.insertAll(defaultAccounts)

                // Default Expense Categories
                val expenseCategories = listOf(
                    CategoryEntity(name = "餐饮美食", type = TransactionType.EXPENSE, iconName = "Restaurant", colorHex = 0xFFFF7043, sortOrder = 1),
                    CategoryEntity(name = "交通出行", type = TransactionType.EXPENSE, iconName = "DirectionsCar", colorHex = 0xFF42A5F5, sortOrder = 2),
                    CategoryEntity(name = "日用百货", type = TransactionType.EXPENSE, iconName = "ShoppingCart", colorHex = 0xFFEC407A, sortOrder = 3),
                    CategoryEntity(name = "休闲娱乐", type = TransactionType.EXPENSE, iconName = "SportsEsports", colorHex = 0xFFAB47BC, sortOrder = 4),
                    CategoryEntity(name = "住房物业", type = TransactionType.EXPENSE, iconName = "Home", colorHex = 0xFF26A69A, sortOrder = 5),
                    CategoryEntity(name = "医疗健康", type = TransactionType.EXPENSE, iconName = "LocalHospital", colorHex = 0xFFEF5350, sortOrder = 6),
                    CategoryEntity(name = "学习提升", type = TransactionType.EXPENSE, iconName = "School", colorHex = 0xFF5C6BC0, sortOrder = 7),
                    CategoryEntity(name = "数码装备", type = TransactionType.EXPENSE, iconName = "Smartphone", colorHex = 0xFF29B6F6, sortOrder = 8),
                    CategoryEntity(name = "人情往来", type = TransactionType.EXPENSE, iconName = "CardGiftcard", colorHex = 0xFFFF5252, sortOrder = 9),
                    CategoryEntity(name = "其它支出", type = TransactionType.EXPENSE, iconName = "MoreHoriz", colorHex = 0xFF78909C, sortOrder = 10)
                )

                // Default Income Categories
                val incomeCategories = listOf(
                    CategoryEntity(name = "工资薪酬", type = TransactionType.INCOME, iconName = "Work", colorHex = 0xFF4CAF50, sortOrder = 1),
                    CategoryEntity(name = "兼职外快", type = TransactionType.INCOME, iconName = "Laptop", colorHex = 0xFF26C6DA, sortOrder = 2),
                    CategoryEntity(name = "理财收益", type = TransactionType.INCOME, iconName = "TrendingUp", colorHex = 0xFFFFCA28, sortOrder = 3),
                    CategoryEntity(name = "收到红包", type = TransactionType.INCOME, iconName = "RedPacket", colorHex = 0xFFE91E63, sortOrder = 4),
                    CategoryEntity(name = "其它收入", type = TransactionType.INCOME, iconName = "AccountBalanceWallet", colorHex = 0xFF8D6E63, sortOrder = 5)
                )

                categoryDao.insertAll(expenseCategories)
                categoryDao.insertAll(incomeCategories)
            }
        }
    }
}
