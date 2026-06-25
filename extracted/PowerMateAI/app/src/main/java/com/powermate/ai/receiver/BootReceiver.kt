package com.powermate.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.powermate.ai.service.BatteryMonitorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Widgets and alerts are otherwise restored lazily on the next battery/charger event.
        // The one exception: if the device reboots while already plugged in, there is no
        // fresh ACTION_POWER_CONNECTED broadcast to rely on, so check current charging state
        // directly and resume monitoring if needed. This is intentionally a single cheap
        // status check, not a long-running task, so it stays battery-friendly and Play-policy safe.
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        if (isCharging) {
            BatteryMonitorService.start(context)
        }
    }
}
