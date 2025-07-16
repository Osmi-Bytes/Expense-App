package com.example.smarthouseexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class MonthYearPickerDialog(
    private val date: Calendar,
    private val listener: (year: Int, month: Int) -> Unit
) : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.requestFeature(STYLE_NO_TITLE)
        return inflater.inflate(R.layout.dialog_month_year_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val yearPicker = view.findViewById<NumberPicker>(R.id.picker_year)
        val monthsGrid = view.findViewById<GridLayout>(R.id.grid_months)
        val cancelButton = view.findViewById<Button>(R.id.button_cancel)

        // Configure the dynamic year picker
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        yearPicker.minValue = currentYear - 10
        yearPicker.maxValue = currentYear
        yearPicker.value = date.get(Calendar.YEAR)
        // Prevent the soft keyboard from showing on touch
        yearPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        // Programmatically create and add buttons for each month
        val months = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )

        monthsGrid.columnCount = 3
        months.forEachIndexed { index, month ->
            val button = Button(requireContext()).apply {
                text = month
                // Use a standard style for the buttons
                // Note: We use a generic style here to avoid theme conflicts in a dialog.
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                val params = GridLayout.LayoutParams().apply {
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    width = 0
                }
                layoutParams = params

                setOnClickListener {
                    listener.invoke(yearPicker.value, index)
                    dismiss()
                }
            }
            monthsGrid.addView(button)
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    // This ensures the dialog doesn't take up the full screen width on larger devices
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}