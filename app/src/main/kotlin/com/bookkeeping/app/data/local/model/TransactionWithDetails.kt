package com.bookkeeping.app.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import com.bookkeeping.app.data.local.entity.AccountEntity
import com.bookkeeping.app.data.local.entity.CategoryEntity
import com.bookkeeping.app.data.local.entity.TransactionEntity

data class TransactionWithDetails(
    @Embedded
    val transaction: TransactionEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity?,

    @Relation(
        parentColumn = "sourceAccountId",
        entityColumn = "id"
    )
    val sourceAccount: AccountEntity?,

    @Relation(
        parentColumn = "targetAccountId",
        entityColumn = "id"
    )
    val targetAccount: AccountEntity?
)
