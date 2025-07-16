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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    // This query is no longer used by the main UI, but can be kept for other purposes.
    @Query("SELECT * FROM expenses_table ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    // New query to get all expenses within a specific date range (start and end of a month)
    @Query("SELECT * FROM expenses_table WHERE date BETWEEN :startTime AND :endTime ORDER BY date DESC")
    fun getExpensesForMonth(startTime: Long, endTime: Long): Flow<List<Expense>>

    // New query to get category totals within a specific date range
    @Query("SELECT category, SUM(amount) as totalAmount FROM expenses_table WHERE date BETWEEN :startTime AND :endTime GROUP BY category")
    fun getCategoryTotalsForMonth(startTime: Long, endTime: Long): Flow<List<CategoryExpense>>


    @Query("SELECT * FROM expenses_table WHERE id = :id")
    fun getExpenseById(id: Int): Flow<Expense>
}