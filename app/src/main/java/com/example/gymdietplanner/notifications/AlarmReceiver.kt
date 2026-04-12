package com.example.gymdietplanner.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.gymdietplanner.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_WORKOUT_REMINDER = "com.example.gymdietplanner.ACTION_WORKOUT_REMINDER"
        const val ACTION_MEAL_REMINDER = "com.example.gymdietplanner.ACTION_MEAL_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val helper = NotificationHelper(context)

        when (intent.action) {
            ACTION_WORKOUT_REMINDER -> {
                handleWorkoutReminder(context, helper)
            }
            ACTION_MEAL_REMINDER -> {
                val mealName = intent.getStringExtra("meal_name") ?: "Meal"
                helper.showMealNotification(
                    "Meal Time Soon",
                    "Your $mealName is in 30 minutes!"
                )
            }
        }
    }

    private fun handleWorkoutReminder(context: Context, helper: NotificationHelper) {
        val db = AppDatabase.getDatabase(context)
        val tomorrow = LocalDate.now().plusDays(1)
        
        val dayMapper = mapOf(
            DayOfWeek.MONDAY to "M",
            DayOfWeek.TUESDAY to "T",
            DayOfWeek.WEDNESDAY to "W",
            DayOfWeek.THURSDAY to "Th",
            DayOfWeek.FRIDAY to "F",
            DayOfWeek.SATURDAY to "S",
            DayOfWeek.SUNDAY to "Su"
        )
        val tomorrowStr = dayMapper[tomorrow.dayOfWeek] ?: "M"

        CoroutineScope(Dispatchers.IO).launch {
            val routines = db.routineDao().getAllRoutines().first()
            val tomorrowRoutines = routines.filter { it.days.contains(tomorrowStr) }

            if (tomorrowRoutines.isNotEmpty()) {
                val routineNames = tomorrowRoutines.joinToString(", ") { it.name }
                helper.showWorkoutNotification(
                    "Workout Tomorrow",
                    "Get ready! You have: $routineNames"
                )
            }
        }
    }
}
