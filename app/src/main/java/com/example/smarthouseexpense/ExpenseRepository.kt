package com.example.smarthouseexpense

import kotlinx.coroutines.flow.Flow

// The repository is the single source of truth for our data.
// It abstracts the data sources (in this case, only the DAO) from the rest of the app.
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    // This property gets all expenses from the DAO. Room executes this on a background thread.
    // The Flow is used so that the ViewModel gets notified of any data changes.
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()

    val expensesByCategory: Flow<List<CategoryExpense>> = expenseDao.getExpensesByCategory()

    // These functions provide a clean API for the ViewModel to use.
    // The 'suspend' modifier tells the compiler that this needs to be called from a coroutine.
    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }
}