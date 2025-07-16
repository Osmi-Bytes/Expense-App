package com.example.smarthouseexpense

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

private fun hideKeyboard(activity: Activity) {
    val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    var view = activity.currentFocus
    if (view == null) {
        view = View(activity)
    }
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

class MainActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as ExpenseApplication).repository)
    }
    private val categories = listOf("Food", "Transport", "Bills", "Groceries", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav_view).setupWithNavController(navController)

        // Setup FAB
        findViewById<FloatingActionButton>(R.id.addExpenseButton).setOnClickListener {
            showAddExpenseDialog()
        }
    }

    // The dialog logic remains in the MainActivity as it can be called from anywhere.
    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        builder.setView(view)

        val amountInput: TextInputEditText = view.findViewById(R.id.amountInput)
        val descriptionInput: TextInputEditText = view.findViewById(R.id.descriptionInput)
        val categoryAutoComplete: AutoCompleteTextView = view.findViewById(R.id.categoryAutoComplete)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryAutoComplete.setAdapter(categoryAdapter)

        categoryAutoComplete.setOnClickListener {
            hideKeyboard(this)
            // We need to re-show the dropdown because hiding the keyboard might dismiss it
            categoryAutoComplete.showDropDown()
        }

        builder.setTitle("Add Expense")
            .setPositiveButton("Add") { dialog, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()
                val description = descriptionInput.text.toString()
                val category = categoryAutoComplete.text.toString()

                if (amount != null && description.isNotEmpty() && category.isNotEmpty()) {
                    val newExpense = Expense(amount = amount, description = description, category = category, date = System.currentTimeMillis())
                    viewModel.insert(newExpense)
                } else {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }
}