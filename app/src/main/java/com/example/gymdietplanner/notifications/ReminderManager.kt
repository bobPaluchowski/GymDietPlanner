package com.example.gymdietplanner.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.gymdietplanner.data.AppDatabase
import com.example.gymdietplanner.data.PreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.util.Log
import android.os.Build
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

class ReminderManager(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs = PreferencesManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    private val db = AppDatabase.getDatabase(context)

    fun scheduleAll() {
        scope.launch {
            Log.d("ReminderManager", "Scheduling all reminders...")
            if (prefs.isWorkoutNotificationsEnabled()) {
                scheduleWorkoutReminder()
            } else {
                cancelWorkoutReminder()
            }

            if (prefs.isMealNotificationsEnabled()) {
                scheduleMealReminders()
            } else {
                cancelMealReminders()
            }
        }
    }

    private suspend fun scheduleWorkoutReminder() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WORKOUT_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 20) // Default 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // If time has passed, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelWorkoutReminder() {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_WORKOUT_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    private suspend fun scheduleMealReminders() {
        val meals = db.mealDao().getAllMeals().first()
        meals.forEach { meal ->
            meal.days.forEach { day ->
                scheduleSpecificMealAlarm(meal.id, meal.name, meal.time, day)
            }
        }
    }

    private fun scheduleSpecificMealAlarm(mealId: Int, name: String, timeStr: String, dayStr: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_MEAL_REMINDER
            putExtra("meal_name", name)
        }
        
        val requestCode = mealId * 10000 + dayStr.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            val mealTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            val now = LocalDateTime.now()
            
            // Start with today's date and the meal time
            var mealDateTime = LocalDateTime.of(LocalDate.now(), mealTime)
            
            val dayMapper = mapOf(
                "M" to DayOfWeek.MONDAY,
                "T" to DayOfWeek.TUESDAY,
                "W" to DayOfWeek.WEDNESDAY,
                "Th" to DayOfWeek.THURSDAY,
                "F" to DayOfWeek.FRIDAY,
                "S" to DayOfWeek.SATURDAY,
                "Su" to DayOfWeek.SUNDAY
            )
            
            val targetDay = dayMapper[dayStr] ?: DayOfWeek.MONDAY
            
            // Logic: Find the next occurrence where (meal time - 30 mins) is in the future
            while (mealDateTime.dayOfWeek != targetDay || mealDateTime.minusMinutes(30).isBefore(now)) {
                mealDateTime = mealDateTime.plusDays(1)
            }

            val alarmDateTime = mealDateTime.minusMinutes(30)
            val triggerAtMillis = alarmDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            
            Log.d("ReminderManager", "Scheduling meal $name ($dayStr at $timeStr) for alarm at ${alarmDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: Exception) {
            Log.e("ReminderManager", "Error scheduling meal alarm: ${e.message}", e)
        }
    }

    private suspend fun cancelMealReminders() {
        Log.d("ReminderManager", "Cancelling all meal reminders...")
        val meals = db.mealDao().getAllMeals().first()
        val days = listOf("M", "T", "W", "Th", "F", "S", "Su")
        
        meals.forEach { meal ->
            days.forEach { day ->
                val requestCode = meal.id * 10000 + day.hashCode()
                val intent = Intent(context, AlarmReceiver::class.java).apply {
                    action = AlarmReceiver.ACTION_MEAL_REMINDER
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                if (pendingIntent != null) {
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }
}
