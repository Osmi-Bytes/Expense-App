package com.example.smarthouseexpense

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses_table")
data class Expense(
    // 3. Add the @PrimaryKey annotation for our unique ID
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // This will be the unique ID for each expense

    val amount: Double,
    val description: String,
    val category: String,
    val date: Long
)