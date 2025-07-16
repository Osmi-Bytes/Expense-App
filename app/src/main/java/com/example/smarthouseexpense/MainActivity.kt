package com.example.smarthouseexpense

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((application as ExpenseApplication).repository)
    }
    private val categories = listOf("Food", "Transport", "Bills", "Groceries", "Entertainment", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<FloatingActionButton>(R.id.addExpenseButton)

        // Setup Navigation
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        findViewById<BottomNavigationView>(R.id.bottom_nav_view).setupWithNavController(navController)

        // Setup FAB click listener
        fab.setOnClickListener {
            showAddExpenseDialog()
        }

        // --- START OF NEW LOGIC ---
        // Observe the selected date from the ViewModel to control FAB visibility.
        lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { (year, month) ->
                val currentCalendar = Calendar.getInstance()
                val currentYear = currentCalendar.get(Calendar.YEAR)
                val currentMonth = currentCalendar.get(Calendar.MONTH)

                // Show the FAB only if the selected month is the current real-world month.
                if (year == currentYear && month == currentMonth) {
                    fab.show()
                } else {
                    fab.hide()
                }
            }
        }
        // --- END OF NEW LOGIC ---
    }

    /**
     * Shows the dialog for adding a new expense.
     */
    private fun showAddExpenseDialog() {
        val builder = AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        builder.setView(view)

        // ... (code for finding views in the dialog)
        val amountInput: TextInputEditText = view.findViewById(R.id.amountInput)
        val descriptionInput: TextInputEditText = view.findViewById(R.id.descriptionInput)
        val categoryAutoComplete: AutoCompleteTextView = view.findViewById(R.id.categoryAutoComplete)
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        categoryAutoComplete.setAdapter(categoryAdapter)

        // Set up listener to hide keyboard
        categoryAutoComplete.setOnClickListener {
            hideKeyboard(this)
            categoryAutoComplete.showDropDown()
        }


        builder.setTitle("Add Expense")
            .setPositiveButton("Add") { dialog, _ ->
                val amount = amountInput.text.toString().toDoubleOrNull()
                val description = descriptionInput.text.toString()
                val category = categoryAutoComplete.text.toString()

                if (amount != null && description.isNotEmpty() && category.isNotEmpty()) {
                    val newExpense = Expense(amount = amount, description = description, category = category, date = System.currentTimeMillis())

                    // The ViewModel's insert function already contains the logic
                    // to prevent adding to past months. We add a Toast for user feedback.
                    val wasInserted = viewModel.insert(newExpense)
                    if (!wasInserted) {
                        Toast.makeText(this, "Can only add expenses to the current month.", Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.create().show()
    }

    /**
     * Helper function to programmatically hide the soft keyboard.
     */
    private fun hideKeyboard(activity: AppCompatActivity) {
        val inputMethodManager = activity.getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}