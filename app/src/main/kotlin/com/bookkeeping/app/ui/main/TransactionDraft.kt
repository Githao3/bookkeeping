package com.bookkeeping.app.ui.main

import com.bookkeeping.app.data.local.entity.TransactionType

data class TransactionDraft(val id: Long = 0, val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "0", val categoryId: Long? = null, val sourceId: Long = 0,
    val targetId: Long = 0, val timestamp: Long = System.currentTimeMillis(), val note: String = "") {
    val hasContent get() = id != 0L || amount != "0" || note.isNotBlank()
}
