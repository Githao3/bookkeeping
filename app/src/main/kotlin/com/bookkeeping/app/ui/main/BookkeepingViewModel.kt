package com.bookkeeping.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
    val repository: BookkeepingRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    var draft by mutableStateOf(TransactionDraft(
        id = savedState["draftId"] ?: 0L, type = TransactionType.valueOf(savedState["draftType"] ?: "EXPENSE"),
        amount = savedState["draftAmount"] ?: "0", categoryId = savedState["draftCategory"],
        sourceId = savedState["draftSource"] ?: 0L, targetId = savedState["draftTarget"] ?: 0L,
        timestamp = savedState["draftDate"] ?: System.currentTimeMillis(), note = savedState["draftNote"] ?: ""
    ))
        private set
    var saving by mutableStateOf(false)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set
    fun updateDraft(value: TransactionDraft) {
        draft = value
        savedState["draftId"] = value.id; savedState["draftType"] = value.type.name
        savedState["draftAmount"] = value.amount; savedState["draftCategory"] = value.categoryId
        savedState["draftSource"] = value.sourceId; savedState["draftTarget"] = value.targetId
        savedState["draftDate"] = value.timestamp; savedState["draftNote"] = value.note
        saveError = null
    }
    fun clearDraft() = updateDraft(TransactionDraft())

    /** 打开记一笔时刷新新草稿的时间戳为当下（保留已有内容的草稿不动）。 */
    fun beginNewTransaction() {
        val d = draft
        if (d.id == 0L && !d.hasContent) updateDraft(d.copy(timestamp = System.currentTimeMillis()))
    }
    fun editTransaction(tx: TransactionEntity) = updateDraft(TransactionDraft(tx.id, tx.type,
        com.bookkeeping.app.data.Money.decimal(tx.amount), tx.categoryId, tx.sourceAccountId,
        tx.targetAccountId ?: 0L, tx.timestamp, tx.note))
    private fun perform(onSuccess: () -> Unit, action: suspend () -> Unit) {
        if (saving) return
        saving = true; saveError = null
        viewModelScope.launch {
            try { action(); onSuccess() }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { saveError = e.message ?: "操作失败，请重试" }
            finally { saving = false }
        }
    }

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
    fun changeYear(delta: Int) {
        val cal = Calendar.getInstance().apply { timeInMillis = _currentMonthTimestamp.value; add(Calendar.YEAR, delta) }
        _currentMonthTimestamp.value = cal.timeInMillis
    }

    fun addTransaction(transaction: TransactionEntity, onSuccess: () -> Unit) = perform(onSuccess) {
        if (transaction.id == 0L) repository.addTransaction(transaction) else repository.updateTransaction(transaction)
        _currentMonthTimestamp.value = transaction.timestamp
        clearDraft()
    }

    fun deleteTransaction(transaction: TransactionEntity, onSuccess: () -> Unit = {}) = perform(onSuccess) { repository.deleteTransaction(transaction) }
    fun undoDelete(transaction: TransactionEntity, onSuccess: () -> Unit = {}) = perform(onSuccess) { repository.undoDelete(transaction) }

    fun addAccount(account: AccountEntity, onSuccess: () -> Unit) = perform(onSuccess) {
        if (account.id == 0L) repository.addAccount(account) else repository.updateAccount(account)
    }

    fun deleteAccount(account: AccountEntity, onSuccess: () -> Unit) = perform(onSuccess) { repository.deleteAccount(account) }
    fun undoDeleteAccount(account: AccountEntity) = perform({}) { repository.undoDeleteAccount(account) }
    fun archiveAccount(account: AccountEntity, onSuccess: () -> Unit) = perform(onSuccess) { repository.archiveAccount(account) }

    class Factory(
        private val repository: BookkeepingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(BookkeepingViewModel::class.java)) {
                return BookkeepingViewModel(repository, extras.createSavedStateHandle()) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
