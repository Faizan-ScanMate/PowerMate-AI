package com.powermate.ai.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.powermate.ai.MainActivity
import com.powermate.ai.R

class NotificationHelper(private val context: Context) {
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val monitor = NotificationChannel(CHANNEL_MONITOR, "PowerMate Monitor", NotificationManager.IMPORTANCE_LOW)
        val alerts = NotificationChannel(CHANNEL_ALERTS, "Charging Alerts", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(monitor)
        manager.createNotificationChannel(alerts)
    }

    fun monitorNotification(text: String): Notification = build(CHANNEL_MONITOR, "PowerMate AI", text, ongoing = true)
    fun alertNotification(title: String, text: String): Notification = build(CHANNEL_ALERTS, title, text, ongoing = false)

    private fun build(channelId: String, title: String, text: String, ongoing: Boolean): Notification {
        ensureChannels()
        val pendingIntent = PendingIntent.getActivity(
            context,
            100,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = if (Build.VERSION.SDK_INT >= 26) Notification.Builder(context, channelId) else Notification.Builder(context)
        return builder
            .setSmallIcon(R.drawable.ic_powermate)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setAutoCancel(!ongoing)
            .build()
    }

    companion object {
        const val CHANNEL_MONITOR = "powermate_monitor"
        const val CHANNEL_ALERTS = "powermate_alerts"
    }
}
