package com.example.smarthouseexpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // Use LiveData to cache the list of expenses.
    // This will survive configuration changes and automatically update the UI.
    val allExpenses: LiveData<List<Expense>> = repository.allExpenses.asLiveData()

    val expensesByCategory: LiveData<List<CategoryExpense>> = repository.expensesByCategory.asLiveData()

    // Launch a new coroutine to insert an expense in a non-blocking way.
    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    // Launch a new coroutine to delete an expense.
    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
}

// This is a ViewModelFactory. We need this because our ViewModel has a non-empty constructor.
// It tells the system HOW to create an instance of our ExpenseViewModel.
class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}