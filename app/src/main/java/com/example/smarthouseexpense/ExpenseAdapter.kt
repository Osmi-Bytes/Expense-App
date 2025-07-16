package com.example.smarthouseexpense

import android.icu.text.SimpleDateFormat
import android.text.TextUtils // <-- ADD THIS IMPORT
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager // <-- ADD THIS IMPORT
import java.util.Date
import java.util.Locale

class ExpenseAdapter(private val currencySymbol: String, private val onDeleteClick: (Expense) -> Unit) :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpensesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense_card, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, currencySymbol, onDeleteClick)
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountText: TextView = itemView.findViewById(R.id.expenseAmount)
        private val descriptionText: TextView = itemView.findViewById(R.id.expenseDescription)
        private val dateText: TextView = itemView.findViewById(R.id.expenseDate)
        private val categoryText: TextView = itemView.findViewById(R.id.expenseCategory)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteExpenseIcon)

        fun bind(expense: Expense, currencySymbol: String, onDeleteClick: (Expense) -> Unit) {
            // Format amount with the provided symbol
            amountText.text = String.format("%s%,.2f", currencySymbol, expense.amount)
            dateText.text = formatDate(expense.date)
            categoryText.text = expense.category
            descriptionText.text = expense.description

            // Set the description to be a single line initially
            descriptionText.maxLines = 1
            descriptionText.ellipsize = TextUtils.TruncateAt.END

            // Set the click listener for the delete icon
            deleteIcon.setOnClickListener {
                onDeleteClick(expense)
            }

            // Set the click listener on the whole item view for expanding/collapsing
            itemView.setOnClickListener {
                // Animate the change for a smooth transition
                TransitionManager.beginDelayedTransition(itemView.parent as ViewGroup)

                // Check the current state and toggle it
                if (descriptionText.maxLines == 1) {
                    descriptionText.maxLines = Integer.MAX_VALUE // Show all lines
                    descriptionText.ellipsize = null
                } else {
                    descriptionText.maxLines = 1 // Collapse to one line
                    descriptionText.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

        private fun formatDate(timestamp: Long): String {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }

    class ExpensesComparator : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Expense, newItem: Expense): Boolean {
            return oldItem == newItem
        }
    }
}