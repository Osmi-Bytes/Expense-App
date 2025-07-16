package com.example.smarthouseexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenseListFragment : Fragment() {

    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModelFactory((requireActivity().application as ExpenseApplication).repository)
    }
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var settingsManager: SettingsManager
    private lateinit var monthSelectorButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsManager = SettingsManager(requireContext())

        monthSelectorButton = view.findViewById(R.id.month_selector_button)
        setupRecyclerView(view)
        setupMonthSelector()

        // Observe the list of expenses for the currently selected month
        viewModel.expensesForMonth.observe(viewLifecycleOwner) { expenses ->
            expenses?.let {
                expenseAdapter.submitList(it)
                updateEmptyStateVisibility(view, it)
                updateTotalExpense(view, it)
            }
        }

        // Use lifecycleScope to safely collect the StateFlow for the button text
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { (year, month) ->
                updateMonthSelectorButtonText(year, month)
            }
        }
    }

    /**
     * Configures the RecyclerView and its adapter.
     */
    private fun setupRecyclerView(view: View) {
        val currentSymbol = settingsManager.getCurrencySymbol()
        expenseAdapter = ExpenseAdapter(currentSymbol) { expense ->
            showDeleteConfirmationDialog(expense)
        }
        view.findViewById<RecyclerView>(R.id.expenseRecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
        }
    }

    /**
     * Configures the click listener for the month selector button.
     */
    private fun setupMonthSelector() {
        monthSelectorButton.setOnClickListener {
            val (currentYear, currentMonth) = viewModel.selectedDate.value
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth)
            }
            // Create and show the month picker dialog
            MonthYearPickerDialog(calendar) { year, month ->
                viewModel.selectMonth(year, month)
            }.show(parentFragmentManager, "MonthYearPickerDialog")
        }
    }

    /**
     * Updates the text of the month selector button based on the selected year and month.
     */
    private fun updateMonthSelectorButtonText(year: Int, month: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
        }
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthSelectorButton.text = dateFormat.format(calendar.time)
    }

    /**
     * Shows a confirmation dialog before deleting an expense.
     */
    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.delete(expense)
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Toggles the visibility of the empty state layout based on the expense list.
     */
    private fun updateEmptyStateVisibility(view: View, expenses: List<Expense>) {
        val emptyStateLayout = view.findViewById<LinearLayout>(R.id.emptyStateLayout)
        val expenseRecyclerView = view.findViewById<RecyclerView>(R.id.expenseRecyclerView)
        emptyStateLayout.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
        expenseRecyclerView.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
    }

    /**
     * Calculates and displays the total expense for the given list.
     */
    private fun updateTotalExpense(view: View, expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        val totalExpenseTextView = view.findViewById<TextView>(R.id.totalExpense)
        totalExpenseTextView.text = CurrencyHelper.formatAmount(settingsManager, total)
    }
}