package com.example.smarthouseexpense

// This is not an entity. It's a simple data class to hold the result
// of our GROUP BY query.
data class CategoryExpense(
    val category: String,
    val totalAmount: Double
)