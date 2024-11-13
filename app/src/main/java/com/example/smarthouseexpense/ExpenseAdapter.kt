package com.example.smarthouseexpense

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

// Adapter for displaying expense list
class ExpenseAdapter(private val expenses: MutableList<Expense>, private val onDeleteClick: (Expense) -> Unit) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    // ViewHolder to bind each expense item
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val amountText: TextView = itemView.findViewById(R.id.expenseAmount)
        val descriptionText: TextView = itemView.findViewById(R.id.expenseDescription)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteExpenseIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.amountText.text = "$${expense.amount}"
        holder.descriptionText.text = expense.description

        // Set up the delete button click listener
        holder.deleteIcon.setOnClickListener {
            // Show confirmation dialog before deletion
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes") { dialog, _ ->
                    onDeleteClick(expense)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }
    }

    override fun getItemCount(): Int {
        return expenses.size
    }
}
