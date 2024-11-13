package com.example.smarthouseexpense

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenses = mutableListOf<Expense>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load saved expenses from SharedPreferences
        expenses = loadExpenses(this).toMutableList()
        updateTotalExpense()

        // Setup RecyclerView
        expenseAdapter = ExpenseAdapter(expenses) { expense ->
            // Handle delete action
            deleteExpense(expense)
        }
        findViewById<RecyclerView>(R.id.expenseRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }

        // FAB to add a new expense
        findViewById<FloatingActionButton>(R.id.addExpenseButton).setOnClickListener {
            showAddExpenseDialog()
        }
    }

    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        builder.setView(view)

        val amountInput: EditText = view.findViewById(R.id.amountInput)
        val descriptionInput: EditText = view.findViewById(R.id.descriptionInput)

        builder.setTitle("Add Expense")
            .setPositiveButton("Add") { dialog, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()
                val description = descriptionInput.text.toString()

                if (amount != null && description.isNotEmpty()) {
                    addExpense(amount, description)
                } else {
                    Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    private fun addExpense(amount: Double, description: String) {
        val newExpense = Expense(amount, description)
        expenses.add(newExpense)
        expenseAdapter.notifyItemInserted(expenses.size - 1)
        saveExpenses(this, expenses) // Save updated expenses to SharedPreferences
        updateTotalExpense()
    }

    private fun deleteExpense(expense: Expense) {
        // Remove expense from the list
        expenses.remove(expense)
        expenseAdapter.notifyDataSetChanged() // Notify adapter to update the RecyclerView
        saveExpenses(this, expenses) // Save updated expenses to SharedPreferences
        updateTotalExpense() // Update total expense after deletion
    }

    private fun updateTotalExpense() {
        val total = expenses.sumOf { it.amount }
        findViewById<TextView>(R.id.totalExpense).text = "Total: $total"
    }

    // Save expenses to SharedPreferences
    private fun saveExpenses(context: Context, expenses: List<Expense>) {
        val sharedPreferences = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val expensesJson = gson.toJson(expenses)
        editor.putString("expenses", expensesJson)
        editor.apply()
    }

    // Load expenses from SharedPreferences
    private fun loadExpenses(context: Context): List<Expense> {
        val sharedPreferences = context.getSharedPreferences("ExpenseApp", Context.MODE_PRIVATE)
        val gson = Gson()
        val expensesJson = sharedPreferences.getString("expenses", "[]") // Default to empty list
        val type = object : TypeToken<List<Expense>>() {}.type
        return gson.fromJson(expensesJson, type)
    }
}

