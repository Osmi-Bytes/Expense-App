package com.example.smarthouseexpense

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    }

    fun saveCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
    }

    fun getCurrencySymbol(): String {
        // Default to the device's local currency if no preference is set
        return prefs.getString(KEY_CURRENCY_SYMBOL, null) ?: CurrencyHelper.getLocalCurrencySymbol()
    }
}