package com.jarimanis.jarimanis.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object AlarmHelper {
    private const val PREFS_NAME = "ttd_alarm_prefs"

    fun setAlarm(context: Context, isDaily: Boolean, dayOfWeek: Int, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TtdAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Meracik kalender target alarm
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (isDaily) {
            // Jika jam yang diset sudah lewat hari ini, atur untuk besok
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
        } else {
            // Jika mingguan, set ke hari yang dipilih (1=Minggu, 2=Senin, dst)
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
            // Jika hari/jam tersebut di minggu ini sudah lewat, atur untuk minggu depan
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        // --- KUNCI PERBAIKAN: GUNAKAN EXACT ALARM ---
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Menembus mode tidur (Doze Mode) Android
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback jika HP pabrikan tertentu memblokir alarm presisi tinggi
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }

        // Simpan state pengaturan agar UI bisa membacanya nanti
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("is_active", true)
            .putBoolean("is_daily", isDaily)
            .putInt("day_of_week", dayOfWeek)
            .putInt("hour", hour)
            .putInt("minute", minute)
            .apply()
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, TtdAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent) // Hentikan alarm di sistem Android

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_active", false).apply()
    }

    fun restoreAlarm(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isActive = prefs.getBoolean("is_active", false)
        if (isActive) {
            val isDaily = prefs.getBoolean("is_daily", true)
            val dayOfWeek = prefs.getInt("day_of_week", Calendar.MONDAY)
            val hour = prefs.getInt("hour", 8)
            val minute = prefs.getInt("minute", 0)
            setAlarm(context, isDaily, dayOfWeek, hour, minute)
        }
    }

    fun getAlarmState(context: Context): AlarmState {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AlarmState(
            isActive = prefs.getBoolean("is_active", false),
            isDaily = prefs.getBoolean("is_daily", true),
            dayOfWeek = prefs.getInt("day_of_week", Calendar.MONDAY),
            hour = prefs.getInt("hour", 8),
            minute = prefs.getInt("minute", 0)
        )
    }
}

data class AlarmState(
    val isActive: Boolean,
    val isDaily: Boolean,
    val dayOfWeek: Int,
    val hour: Int,
    val minute: Int
)