package com.powermate.ai.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context

object WidgetUpdateScheduler {
    fun updateBatteryWidgets(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        updateProvider(context, manager, BatteryWidgetProvider::class.java) { ids ->
            BatteryWidgetProvider.updateAll(context, manager, ids)
        }
        updateProvider(context, manager, ChargingSpeedWidgetProvider::class.java) { ids ->
            ChargingSpeedWidgetProvider.updateAll(context, manager, ids)
        }
        updateProvider(context, manager, BatteryHealthWidgetProvider::class.java) { ids ->
            BatteryHealthWidgetProvider.updateAll(context, manager, ids)
        }
        updateProvider(context, manager, AodLauncherWidgetProvider::class.java) { ids ->
            AodLauncherWidgetProvider.updateAll(context, manager, ids)
        }
    }

    private fun updateProvider(
        context: Context,
        manager: AppWidgetManager,
        providerClass: Class<*>,
        update: (IntArray) -> Unit
    ) {
        val component = ComponentName(context, providerClass)
        val ids = manager.getAppWidgetIds(component)
        if (ids.isNotEmpty()) update(ids)
    }
}
