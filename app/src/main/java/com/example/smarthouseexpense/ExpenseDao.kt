package com.example.smarthouseexpense

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    // Insert a new expense. If it already exists, replace it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    // Update an existing expense.
    @Update
    suspend fun update(expense: Expense)

    // Delete an expense.
    @Delete
    suspend fun delete(expense: Expense)

    // Get all expenses from the table, ordered by date (newest first).
    // This returns a Flow, which means whenever the data changes, the list is automatically re-emitted.
    @Query("SELECT * FROM expenses_table ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    // Get a single expense by its ID.
    @Query("SELECT * FROM expenses_table WHERE id = :id")
    fun getExpenseById(id: Int): Flow<Expense>

    @Query("SELECT category, SUM(amount) as totalAmount FROM expenses_table GROUP BY category")
    fun getExpensesByCategory(): Flow<List<CategoryExpense>>
}