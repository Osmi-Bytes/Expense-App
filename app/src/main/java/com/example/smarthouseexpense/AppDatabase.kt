package com.example.smarthouseexpense

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // This abstract function returns our ExpenseDao. Room will generate the implementation.
    abstract fun expenseDao(): ExpenseDao

    // This is a "companion object," which is similar to a static block in other languages.
    // It allows us to create a single instance of our database (a "singleton").
    companion object {

        // The @Volatile annotation ensures that the INSTANCE variable is always up-to-date
        // and visible to all execution threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // This function gets or creates the database instance.
        fun getDatabase(context: Context): AppDatabase {
            // If the INSTANCE is not null, then return it.
            // If it is, then create the database.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_database" // This is the actual file name of the database on the device.
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}