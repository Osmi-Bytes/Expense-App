package com.example.smarthouseexpense

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var settingsManager: SettingsManager
    private lateinit var monthSelectorButton: Button
    private lateinit var emptyChartText: TextView

    // Use activityViewModels to share the ViewModel between fragments.
    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModelFactory((requireActivity().application as ExpenseApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        // Initialize views
        pieChart = view.findViewById(R.id.pieChart)
        monthSelectorButton = view.findViewById(R.id.month_selector_button)
        emptyChartText = view.findViewById(R.id.empty_chart_text)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager(requireContext())
        setupClickListeners()

        // Use lifecycleScope to safely collect the StateFlow
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedDate.collectLatest { (year, month) ->
                updateMonthSelectorButtonText(year, month)
            }
        }

        // Observe the category expenses for the selected month
        viewModel.expensesByCategoryForMonth.observe(viewLifecycleOwner) { categoryExpenses ->
            if (categoryExpenses.isNullOrEmpty()) {
                pieChart.visibility = View.GONE
                emptyChartText.visibility = View.VISIBLE
            } else {
                pieChart.visibility = View.VISIBLE
                emptyChartText.visibility = View.GONE
                setupPieChart(categoryExpenses)
            }
        }
    }

    /**
     * Sets up all the click listeners for the fragment's views.
     */
    private fun setupClickListeners() {
        view?.findViewById<ImageView>(R.id.settings_icon)?.setOnClickListener {
            showCurrencyChooserDialog()
        }

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
     * Sets up and populates the PieChart with expense data.
     */
    private fun setupPieChart(data: List<CategoryExpense>) {
        val entries = ArrayList<PieEntry>()
        for (item in data) {
            entries.add(PieEntry(item.totalAmount.toFloat(), item.category))
        }

        val dataSet = PieDataSet(entries, "Expenses").apply {
            colors = ColorTemplate.MATERIAL_COLORS.toList()
            valueTextSize = 12f
            valueTextColor = Color.BLACK
            // Tell the dataSet to draw values as percentages
            setDrawValues(true)
            valueFormatter = PercentFormatter(pieChart)
        }

        val pieData = PieData(dataSet)

        pieChart.apply {
            this.data = pieData
            // Use this to display percentages on the chart
            setUsePercentValues(true)
            description.isEnabled = false
            legend.isEnabled = false // Hiding the legend for a cleaner look
            isDrawHoleEnabled = true
            holeRadius = 40f
            transparentCircleRadius = 45f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            // Animate the chart
            animateY(1400)
            invalidate()
        }
    }

    /**
     * Shows the dialog for choosing a currency symbol.
     */
    private fun showCurrencyChooserDialog() {
        val currencies = arrayOf("Dollar ($)", "Dirham (AED)", "Rupee (Rs)", "Euro (€)")
        val currencySymbols = arrayOf("$", "AED", "Rs", "€")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Currency")
            .setItems(currencies) { dialog, which ->
                val selectedSymbol = currencySymbols[which]
                settingsManager.saveCurrencySymbol(selectedSymbol)
                requireActivity().recreate()
                dialog.dismiss()
            }
            .show()
    }
}