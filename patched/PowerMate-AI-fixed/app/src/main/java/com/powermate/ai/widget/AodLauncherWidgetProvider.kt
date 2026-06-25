package com.powermate.ai.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.powermate.ai.R
import com.powermate.ai.aod.AodDisplayActivity

class AodLauncherWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAll(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
            appWidgetIds.forEach { id ->
                val views = RemoteViews(context.packageName, R.layout.widget_aod_launcher)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    3,
                    Intent(context, AodDisplayActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widgetAodRoot, pendingIntent)
                manager.updateAppWidget(id, views)
            }
        }
    }
}
