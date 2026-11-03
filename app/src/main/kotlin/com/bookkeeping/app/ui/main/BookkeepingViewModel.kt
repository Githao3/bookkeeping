package com.bookkeeping.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.CategoryEntity
import com.bookkeeping.app.data.local.entity.TransactionEntity
import com.bookkeeping.app.data.local.entity.TransactionType
import com.bookkeeping.app.data.local.model.TransactionWithDetails
import com.bookkeeping.app.data.repository.BookkeepingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class BookkeepingViewModel(
    private val repository: BookkeepingRepository
) : ViewModel() {

    private val _currentMonthTimestamp = MutableStateFlow(System.currentTimeMillis())
    val currentMonthTimestamp: StateFlow<Long> = _currentMonthTimestamp.asStateFlow()

    // Monthly transactions flow
    val monthlyTransactions: StateFlow<List<TransactionWithDetails>> =
        _currentMonthTimestamp.flatMapLatest { timestamp ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = timestamp
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startTime = cal.timeInMillis

            cal.add(Calendar.MONTH, 1)
            val endTime = cal.timeInMillis - 1

            repository.getTransactionsBetween(startTime, endTime)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allTransactions: StateFlow<List<TransactionWithDetails>> =
        repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val accounts: StateFlow<List<AccountEntity>> =
        repository.allAccounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenseCategories: StateFlow<List<CategoryEntity>> =
        repository.getCategories(TransactionType.EXPENSE).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeCategories: StateFlow<List<CategoryEntity>> =
        repository.getCategories(TransactionType.INCOME).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonthTimestamp.value
            add(Calendar.MONTH, -1)
        }
        _currentMonthTimestamp.value = cal.timeInMillis
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _currentMonthTimestamp.value
            add(Calendar.MONTH, 1)
        }
        _currentMonthTimestamp.value = cal.timeInMillis
    }

    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.addAccount(account)
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.deleteAccount(account)
        }
    }

    class Factory(
        private val repository: BookkeepingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BookkeepingViewModel::class.java)) {
                return BookkeepingViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
