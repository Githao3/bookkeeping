package com.bookkeeping.app

import android.app.Application
import com.bookkeeping.app.data.local.AppDatabase
import com.bookkeeping.app.data.repository.BookkeepingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BookkeepingApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { BookkeepingRepository(database) }
}
