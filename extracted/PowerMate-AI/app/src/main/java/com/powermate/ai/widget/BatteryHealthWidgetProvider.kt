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
import com.powermate.ai.data.local.PowerMateDatabase
import com.powermate.ai.domain.insights.BatteryInsightsEngine
import java.util.Locale

class BatteryHealthWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            val snapshot = BatteryStatsManager(context).currentSnapshot()
            val sessions = PowerMateDatabase(context).recentSessions(limit = 10)
            val insights = BatteryInsightsEngine().build(snapshot, sessions)
            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_battery_health)
                views.setTextViewText(R.id.widgetCareScore, "${insights.batteryCareScore}/100")
                views.setTextViewText(R.id.widgetThermalRisk, insights.thermalRiskLabel)
                views.setTextViewText(R.id.widgetWidgetTemp, snapshot.temperatureCelsius?.let { String.format(Locale.US, "%.1f°C", it) } ?: "--°C")
                views.setTextViewText(R.id.widgetHealthHint, insights.wearLevelLabel)
                views.setOnClickPendingIntent(R.id.widgetHealthRoot, openAppIntent(context))
                manager.updateAppWidget(id, views)
            }
        }

        private fun openAppIntent(context: Context): PendingIntent = PendingIntent.getActivity(
            context,
            2,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
