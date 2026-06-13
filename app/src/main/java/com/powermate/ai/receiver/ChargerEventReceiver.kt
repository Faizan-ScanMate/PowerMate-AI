package com.powermate.ai.receiver

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.powermate.ai.widget.BatteryWidgetProvider

class ChargerEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BatteryWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        BatteryWidgetProvider.updateAll(context, manager, ids)
    }
}
