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
import java.util.Locale

class ChargingSpeedWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            val snapshot = BatteryStatsManager(context).currentSnapshot()
            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_charging_speed)
                views.setTextViewText(R.id.widgetSpeedTitle, snapshot.status.label)
                views.setTextViewText(R.id.widgetWattage, snapshot.wattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W")
                views.setTextViewText(R.id.widgetCurrent, snapshot.currentMilliAmp?.let { String.format(Locale.US, "%.0f mA", it) } ?: "-- mA")
                views.setTextViewText(R.id.widgetPlugged, snapshot.pluggedType.label)
                views.setOnClickPendingIntent(R.id.widgetSpeedRoot, openAppIntent(context))
                manager.updateAppWidget(id, views)
            }
        }

        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
