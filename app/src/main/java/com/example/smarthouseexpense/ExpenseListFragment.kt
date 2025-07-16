package com.example.smarthouseexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExpenseListFragment : Fragment() {

    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModelFactory((requireActivity().application as ExpenseApplication).repository)
    }
    private lateinit var expenseAdapter: ExpenseAdapter

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_expense_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Now initialize the declared variable
        settingsManager = SettingsManager(requireContext())

        setupRecyclerView(view)

        viewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            expenses?.let {
                expenseAdapter.submitList(it)
                updateEmptyStateVisibility(view, it)
                updateTotalExpense(view, it)
            }
        }
    }

    private fun setupRecyclerView(view: View) {
        // Get the current symbol and pass it to the adapter
        val currentSymbol = settingsManager.getCurrencySymbol()
        expenseAdapter = ExpenseAdapter(currentSymbol) { expense ->
            // This code will now be executed when the delete icon is clicked
            AlertDialog.Builder(requireContext())
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes") { _, _ ->
                    // Only if the user clicks "Yes", we ask the ViewModel to delete
                    viewModel.delete(expense)
                }
                .setNegativeButton("No", null) // Do nothing on "No"
                .show()
        }

        view.findViewById<RecyclerView>(R.id.expenseRecyclerView).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expenseAdapter
        }
    }

    private fun updateEmptyStateVisibility(view: View, expenses: List<Expense>) {
        val emptyStateLayout = view.findViewById<LinearLayout>(R.id.emptyStateLayout)
        val expenseRecyclerView = view.findViewById<RecyclerView>(R.id.expenseRecyclerView)
        if (expenses.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            expenseRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            expenseRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateTotalExpense(view: View, expenses: List<Expense>) {
        val total = expenses.sumOf { it.amount }
        val totalExpenseTextView = view.findViewById<TextView>(R.id.totalExpense)
        totalExpenseTextView.text = CurrencyHelper.formatAmount(settingsManager, total)
    }
}