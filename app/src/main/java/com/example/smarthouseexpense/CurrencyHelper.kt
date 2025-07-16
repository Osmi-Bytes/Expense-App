package com.example.smarthouseexpense

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyHelper {

    fun getLocalCurrencySymbol(): String {
        return try {
            val currency = Currency.getInstance(Locale.getDefault())
            currency.symbol
        } catch (e: Exception) {
            "$" // Fallback
        }
    }

    // This is the new function we will use everywhere
    fun formatAmount(settings: SettingsManager, amount: Double): String {
        val symbol = settings.getCurrencySymbol()
        return String.format("%s%,.2f", symbol, amount)
    }
}