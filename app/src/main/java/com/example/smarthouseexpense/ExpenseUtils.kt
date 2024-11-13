package com.example.smarthouseexpense

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Function to save expenses to SharedPreferences
fun saveExpenses(context: Context, expenses: List<Expense>) {
    val sharedPreferences = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    val gson = Gson()
    val expensesJson = gson.toJson(expenses)
    editor.putString("expenses", expensesJson)
    editor.apply()
}

// Function to load expenses from SharedPreferences
fun loadExpenses(context: Context): List<Expense> {
    val sharedPreferences = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE)
    val gson = Gson()
    val expensesJson = sharedPreferences.getString("expenses", "[]") // Default to empty list
    val type = object : TypeToken<List<Expense>>() {}.type
    return gson.fromJson(expensesJson, type)
}
