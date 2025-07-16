package com.example.smarthouseexpense

import android.app.Application

class ExpenseApplication : Application() {
    // Using 'by lazy' so the database and repository are only created when they're needed
    // rather than when the application starts.
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { ExpenseRepository(database.expenseDao()) }
}