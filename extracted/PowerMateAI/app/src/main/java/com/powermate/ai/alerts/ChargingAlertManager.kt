package com.powermate.ai.alerts

import android.app.NotificationManager
import android.content.Context
import com.powermate.ai.domain.model.AppSettings
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.PluggedType
import com.powermate.ai.notification.NotificationHelper

class ChargingAlertManager(private val context: Context) {
    private val helper = NotificationHelper(context)
    private var lastAlertKey: String? = null
    private var wasCharging: Boolean? = null

    fun evaluate(snapshot: BatterySnapshot, settings: AppSettings) {
        // Reset dedup state whenever a charging session starts or ends, so e.g. a "reached 80%"
        // alert can fire again on the next charge cycle instead of being silenced forever after
        // the first time it ever fires.
        if (wasCharging != snapshot.isCharging) {
            lastAlertKey = null
            wasCharging = snapshot.isCharging
        }

        val decision = decide(snapshot, settings) ?: return
        if (decision.key == lastAlertKey) return
        lastAlertKey = decision.key
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(decision.notificationId, helper.alertNotification(decision.title, decision.message))
    }

    fun decide(snapshot: BatterySnapshot, settings: AppSettings): ChargingAlertDecision? {
        if (settings.quietHoursEnabled) return null
        val temp = snapshot.temperatureCelsius
        val drainMa = snapshot.currentMilliAmp
        return when {
            settings.overheatAlert && temp != null && temp >= 45f -> ChargingAlertDecision("hot", 2101, "Battery is hot", "Temperature reached ${temp.toInt()}°C. Unplug or cool the phone if needed.")
            settings.alertWhenFull && snapshot.levelPercent >= 100 -> ChargingAlertDecision("full", 2102, "Battery full", "Your phone reached 100%.")
            settings.alertAt90 && snapshot.levelPercent >= 90 -> ChargingAlertDecision("90", 2103, "Battery reached 90%", "Good point to unplug if you want extra battery care.")
            settings.alertAt80 && snapshot.levelPercent >= 80 -> ChargingAlertDecision("80", 2104, "Battery reached 80%", "Battery care reminder: unplugging around 80% can reduce heat time.")
            settings.slowChargingAlert && snapshot.isCharging && snapshot.status == ChargingStatus.SlowCharging -> ChargingAlertDecision("slow", 2105, "Slow charging detected", "Try another cable or adapter for a better charging speed.")
            settings.chargerDisconnectedAlert && !snapshot.isCharging && snapshot.pluggedType == PluggedType.None -> ChargingAlertDecision("disconnect", 2106, "Charger disconnected", "Charging session ended.")
            settings.batteryDrainingFastAlert && !snapshot.isCharging && drainMa != null && drainMa >= 800f -> ChargingAlertDecision("fast_drain", 2107, "Battery draining quickly", "Current draw is unusually high (${drainMa.toInt()} mA). Check which app is active.")
            else -> null
        }
    }
}

data class ChargingAlertDecision(
    val key: String,
    val notificationId: Int,
    val title: String,
    val message: String
)
