package com.example.smarthouseexpense

import android.content.Context
import android.os.Bundle
import android.view.View
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
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {

    // This is the modern way to get a ViewModel instance.
    // It uses our custom factory to create the ViewModel with its repository dependency.
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as ExpenseApplication).repository)
    }

    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenses = mutableListOf<Expense>()

    private val categories = listOf("Food", "Transport", "Bills", "Groceries", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // We initialize the adapter with an empty list. The LiveData will provide the data later.
        expenseAdapter = ExpenseAdapter { expense ->
            // This is the lambda for the delete click
            expenseViewModel.delete(expense)
        }

        findViewById<RecyclerView>(R.id.expenseRecyclerView).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }

        // We observe the 'allExpenses' LiveData from the ViewModel.
        expenseViewModel.allExpenses.observe(this) { expenses ->
            // Every time the data in the database changes, this block will be executed.
            expenses?.let {
                // Update the adapter's list
                expenseAdapter.submitList(it)

                // Update the total expense text
                updateTotalExpense(it)

                // Update the empty state view
                updateEmptyStateVisibility(it)
            }
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

        val amountInput: com.google.android.material.textfield.TextInputEditText = view.findViewById(R.id.amountInput)
        val descriptionInput: com.google.android.material.textfield.TextInputEditText = view.findViewById(R.id.descriptionInput)

        val categoryAutoComplete: AutoCompleteTextView = view.findViewById(R.id.categoryAutoComplete)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryAutoComplete.setAdapter(categoryAdapter)

        builder.setTitle("Add Expense")
            .setPositiveButton("Add") { dialog, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()
                val description = descriptionInput.text.toString()

                val category = categoryAutoComplete.text.toString()

                if (amount != null && description.isNotEmpty() && category.isNotEmpty()) {
                    val newExpense = Expense(amount = amount, description = description, category = category, date = System.currentTimeMillis())
                    expenseViewModel.insert(newExpense)
                } else {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    // 5. HELPER FUNCTIONS NOW TAKE THE LIST AS A PARAMETER
    private fun updateTotalExpense(expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        findViewById<TextView>(R.id.totalExpense).text = String.format("$%,.2f", total)
    }

    private fun updateEmptyStateVisibility(expenses: List<Expense>) {
        val emptyStateLayout = findViewById<LinearLayout>(R.id.emptyStateLayout)
        val expenseRecyclerView = findViewById<RecyclerView>(R.id.expenseRecyclerView)
        if (expenses.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            expenseRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            expenseRecyclerView.visibility = View.VISIBLE
        }
    }
}