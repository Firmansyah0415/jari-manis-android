package com.jarimanis.jarimanis.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jarimanis.jarimanis.MainActivity
import com.jarimanis.jarimanis.R

class TtdAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            AlarmHelper.restoreAlarm(context)
            return
        }

        // 1. Munculkan Notifikasinya
        showNotification(context)

        // 2. KUNCI PERBAIKAN: Jadwalkan ulang untuk hari/minggu berikutnya secara otomatis
        // Karena Exact Alarm hanya berjalan satu kali!
        AlarmHelper.restoreAlarm(context)
    }

    private fun showNotification(context: Context) {
        val channelId = "ttd_reminder_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pengingat TTD",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val i = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo_jari_manis)
            .setContentTitle("Waktunya Minum TTD!")
            .setContentText("Jangan lupa minum Tablet Tambah Darah (TTD) kamu hari ini ya, biar tetap sehat dan fokus!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(1001, builder.build())
    }
}