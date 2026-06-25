package com.powermate.ai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.powermate.ai.MainActivity
import com.powermate.ai.R
import com.powermate.ai.data.battery.BatteryStatsManager

class BatteryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            val snapshot = BatteryStatsManager(context).currentSnapshot()
            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_battery)
                views.setTextViewText(R.id.widgetLevel, "${snapshot.levelPercent}%")
                views.setTextViewText(R.id.widgetStatus, snapshot.status.label)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetLevel, pendingIntent)
                manager.updateAppWidget(id, views)
            }
        }
    }
}
