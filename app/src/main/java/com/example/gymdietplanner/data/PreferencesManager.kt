package com.example.gymdietplanner.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("gym_diet_planner_prefs", Context.MODE_PRIVATE)

    fun isMetric(): Boolean {
        return sharedPreferences.getBoolean("is_metric", true)
    }

    fun setMetric(isMetric: Boolean) {
        sharedPreferences.edit().putBoolean("is_metric", isMetric).apply()
    }

    fun isWorkoutNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("workout_notifications_enabled", true)
    }

    fun setWorkoutNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("workout_notifications_enabled", enabled).apply()
    }

    fun isMealNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("meal_notifications_enabled", true)
    }

    fun setMealNotificationsEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean("meal_notifications_enabled", enabled).apply()
    }
}
