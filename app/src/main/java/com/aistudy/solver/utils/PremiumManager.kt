package com.aistudy.solver.utils

import android.content.Context
import android.content.SharedPreferences

class PremiumManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)

    fun isPremium(): Boolean {
        return prefs.getBoolean("is_premium", false)
    }

    fun setPremium(status: Boolean) {
        prefs.edit().putBoolean("is_premium", status).apply()
    }

    fun getPlan(): String? {
        return prefs.getString("active_plan", null)
    }

    fun setPlan(plan: String) {
        prefs.edit().putString("active_plan", plan).apply()
    }
}


