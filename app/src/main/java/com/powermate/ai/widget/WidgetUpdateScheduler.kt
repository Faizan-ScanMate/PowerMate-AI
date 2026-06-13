package com.powermate.ai.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdateScheduler {
    fun updateBatteryWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BatteryWidgetProvider::class.java)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isNotEmpty()) BatteryWidgetProvider.updateAll(context, manager, ids)
    }
}
