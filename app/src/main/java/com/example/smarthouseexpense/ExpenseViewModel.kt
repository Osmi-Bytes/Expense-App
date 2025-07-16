package com.example.smarthouseexpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ViewModel to manage and provide data for the UI layer.
 * It acts as the bridge between the UI and the data layer (Repository).
 *
 * @param repository The repository that provides data access methods.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    // A StateFlow to hold the currently selected year and month.
    // It is initialized with the current real-world month and year.
    private val _selectedDate = MutableStateFlow(Pair(
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH)
    ))
    val selectedDate: StateFlow<Pair<Int, Int>> = _selectedDate

    // LiveData stream for the list of expenses.
    // It uses `flatMapLatest` to react to changes in `selectedDate`.
    // Whenever `selectedDate` changes, it re-queries the database for the new month's data.
    val expensesForMonth: LiveData<List<Expense>> = selectedDate.flatMapLatest { (year, month) ->
        val (start, end) = getMonthStartAndEnd(year, month)
        repository.getExpensesForMonth(start, end)
    }.asLiveData()

    // LiveData stream for the category totals, used by the Pie Chart.
    // This also reacts to changes in `selectedDate` to fetch data for the appropriate month.
    val expensesByCategoryForMonth: LiveData<List<CategoryExpense>> = selectedDate.flatMapLatest { (year, month) ->
        val (start, end) = getMonthStartAndEnd(year, month)
        repository.getCategoryTotalsForMonth(start, end)
    }.asLiveData()

    /**
     * Updates the selected date, triggering all observing LiveData to refresh.
     *
     * @param year The year to select.
     * @param month The month to select (0-11 for Jan-Dec).
     */
    fun selectMonth(year: Int, month: Int) {
        _selectedDate.value = Pair(year, month)
    }

    /**
     * Inserts a new expense into the database.
     * This action is only permitted if the selected month in the UI is the current real-world month.
     *
     * @param expense The Expense object to insert.
     * @return true if the insert was permitted, false otherwise.
     */
    fun insert(expense: Expense): Boolean {
        val currentCalendar = Calendar.getInstance()
        val currentYear = currentCalendar.get(Calendar.YEAR)
        val currentMonth = currentCalendar.get(Calendar.MONTH)

        // Prevent adding expenses to past or future months.
        if (_selectedDate.value.first == currentYear && _selectedDate.value.second == currentMonth) {
            viewModelScope.launch {
                repository.insert(expense)
            }
            return true
        }
        return false
    }

    /**
     * Deletes an expense from the database.
     * This is launched in a coroutine within the viewModelScope.
     *
     * @param expense The Expense object to delete.
     */
    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }

    /**
     * A helper function to calculate the start and end timestamps (in milliseconds)
     * for a given year and month.
     *
     * @param year The target year.
     * @param month The target month (0-11).
     * @return A Pair containing the start timestamp and end timestamp.
     */
    private fun getMonthStartAndEnd(year: Int, month: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endTime = calendar.timeInMillis

        return Pair(startTime, endTime)
    }
}

/**
 * Factory for creating an ExpenseViewModel with a constructor that takes an ExpenseRepository.
 */
class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}