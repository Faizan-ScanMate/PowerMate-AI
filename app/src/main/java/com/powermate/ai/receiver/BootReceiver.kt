package com.powermate.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Intentionally lightweight: widgets and alerts are restored lazily on next battery/charger event.
        // Avoid starting long-running services at boot to keep the app battery-friendly and Play policy safe.
    }
}
