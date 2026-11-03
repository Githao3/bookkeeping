package com.bookkeeping.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: TransactionType, // EXPENSE or INCOME
    val iconName: String,
    val colorHex: Long,
    val sortOrder: Int = 0
)
