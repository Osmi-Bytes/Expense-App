package com.example.smarthouseexpense

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog


class DashboardFragment : Fragment() {

    private lateinit var pieChart: PieChart
    private lateinit var settingsManager: SettingsManager

    // Use activityViewModels to share the ViewModel between fragments.
    private val viewModel: ExpenseViewModel by activityViewModels {
        ExpenseViewModelFactory((requireActivity().application as ExpenseApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        pieChart = view.findViewById(R.id.pieChart)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingsManager = SettingsManager(requireContext())

        // Observe the data
        viewModel.expensesByCategory.observe(viewLifecycleOwner) { categoryExpenses ->
            if (categoryExpenses.isNullOrEmpty()) {
                // Handle empty state for the chart
                pieChart.clear()
                pieChart.invalidate()
            } else {
                setupPieChart(categoryExpenses)
            }
        }

        view.findViewById<ImageView>(R.id.settings_icon).setOnClickListener {
            showCurrencyChooserDialog()
        }
    }

    private fun showCurrencyChooserDialog() {
        val currencies = arrayOf("Dollar ($)", "Dirham (AED)", "Rupee (₹)", "Euro (€)")
        val currencySymbols = arrayOf("$", "AED", "₹", "€")

        AlertDialog.Builder(requireContext())
            .setTitle("Choose Currency")
            .setItems(currencies) { dialog, which ->
                val selectedSymbol = currencySymbols[which]
                settingsManager.saveCurrencySymbol(selectedSymbol)
                // We need to refresh the UI, so we "re-select" the current fragment
                // This is a simple way to force a redraw.
                requireActivity().recreate()
                dialog.dismiss()
            }
            .show()
    }

    private fun setupPieChart(data: List<CategoryExpense>) {
        val entries = ArrayList<PieEntry>()
        for (item in data) {
            entries.add(PieEntry(item.totalAmount.toFloat(), item.category))
        }

        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)
        pieData.setValueTextColor(Color.BLACK)

        pieChart.data = pieData
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1400)
        pieChart.invalidate() // refresh
    }
}