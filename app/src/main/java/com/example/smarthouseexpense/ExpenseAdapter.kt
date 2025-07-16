package com.example.smarthouseexpense

import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import java.util.Date
import java.util.Locale

class ExpenseAdapter(private val onDeleteClick: (Expense) -> Unit) :
    ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(ExpensesComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense_card, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current, onDeleteClick)
    }

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val amountText: TextView = itemView.findViewById(R.id.expenseAmount)
        private val descriptionText: TextView = itemView.findViewById(R.id.expenseDescription)
        private val dateText: TextView = itemView.findViewById(R.id.expenseDate)
        private val categoryText: TextView = itemView.findViewById(R.id.expenseCategory)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.deleteExpenseIcon)

        fun bind(expense: Expense, onDeleteClick: (Expense) -> Unit) {
            amountText.text = String.format("$%.2f", expense.amount)
            descriptionText.text = expense.description
            dateText.text = formatDate(expense.date)
            categoryText.text = expense.category

            deleteIcon.setOnClickListener {
                onDeleteClick(expense)
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