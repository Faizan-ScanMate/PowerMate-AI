package com.powermate.ai.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.powermate.ai.alerts.ChargingAlertManager
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.notification.NotificationHelper
import com.powermate.ai.widget.WidgetUpdateScheduler

/**
 * Foreground service that keeps live charging data fresh while the phone is plugged in:
 * it refreshes home-screen widgets faster than Android's 30-minute widget update floor,
 * evaluates charging alerts (80%/90%/full/overheat/etc.) even if the app UI isn't open,
 * and shows a live, ongoing notification with current charging stats.
 *
 * The service stops itself automatically when the charger is disconnected, so it never
 * runs (and never drains battery) while the phone isn't charging.
 */
class BatteryMonitorService : Service() {

    private lateinit var batteryStatsManager: BatteryStatsManager
    private lateinit var alertManager: ChargingAlertManager
    private lateinit var preferences: PowerMatePreferences
    private lateinit var notificationHelper: NotificationHelper
    private val handler = Handler(Looper.getMainLooper())

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, TICK_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        batteryStatsManager = BatteryStatsManager(this)
        alertManager = ChargingAlertManager(this)
        preferences = PowerMatePreferences(this)
        notificationHelper = NotificationHelper(this)
        startForeground(NOTIFICATION_ID, notificationHelper.monitorNotification("Watching charging session…"))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun tick() {
        val snapshot = batteryStatsManager.currentSnapshot()
        alertManager.evaluate(snapshot, preferences.load())

        if (!snapshot.isCharging && snapshot.status != ChargingStatus.Full) {
            // Charger was unplugged since this service started. The disconnect alert (if
            // enabled) was just evaluated above on this final tick, so nothing left to monitor.
            stopSelf()
            return
        }

        WidgetUpdateScheduler.updateBatteryWidgets(this)

        val statusText = buildString {
            append(snapshot.status.label)
            append(" • ")
            append(snapshot.levelPercent)
            append("%")
            snapshot.wattage?.let { append(" • %.1f W".format(it)) }
        }
        val manager = getSystemService(android.app.NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notificationHelper.monitorNotification(statusText))
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val TICK_INTERVAL_MS = 15_000L

        /** Starts live monitoring. Safe to call repeatedly (e.g. on every plug-in event). */
        fun start(context: Context) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent) else context.startService(intent)
        }

        /** Stops live monitoring. Safe to call even if the service isn't running. */
        fun stop(context: Context) {
            context.stopService(Intent(context, BatteryMonitorService::class.java))
        }
    }
}
