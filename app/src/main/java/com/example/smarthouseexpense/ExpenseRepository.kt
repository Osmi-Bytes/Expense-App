package com.example.smarthouseexpense

import kotlinx.coroutines.flow.Flow

/**
 * Repository module for handling data operations.
 * It abstracts the data sources from the rest of the app and provides a clean API for data access.
 *
 * @param expenseDao The Data Access Object for the expenses table.
 */
class ExpenseRepository(private val expenseDao: ExpenseDao) {

    /**
     * Retrieves a flow of all expenses for a specific month.
     *
     * @param startTime The start timestamp of the month.
     * @param endTime The end timestamp of the month.
     * @return A Flow emitting a list of Expense objects.
     */
    fun getExpensesForMonth(startTime: Long, endTime: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesForMonth(startTime, endTime)
    }

    /**
     * Retrieves a flow of summed expenses grouped by category for a specific month.
     *
     * @param startTime The start timestamp of the month.
     * @param endTime The end timestamp of the month.
     * @return A Flow emitting a list of CategoryExpense objects.
     */
    fun getCategoryTotalsForMonth(startTime: Long, endTime: Long): Flow<List<CategoryExpense>> {
        return expenseDao.getCategoryTotalsForMonth(startTime, endTime)
    }

    /**
     * Inserts a new expense into the database. This is a suspend function
     * and must be called from a coroutine.
     *
     * @param expense The Expense object to insert.
     */
    suspend fun insert(expense: Expense) {
        expenseDao.insert(expense)
    }

    /**
     * Deletes an expense from the database. This is a suspend function.
     *
     * @param expense The Expense object to delete.
     */
    suspend fun delete(expense: Expense) {
        expenseDao.delete(expense)
    }

    /**
     * Updates an existing expense in the database. This is a suspend function.
     *
     * @param expense The Expense object to update.
     */
    suspend fun update(expense: Expense) {
        expenseDao.update(expense)
    }
}