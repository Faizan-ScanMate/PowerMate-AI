package com.powermate.ai.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.powermate.ai.widget.WidgetUpdateScheduler

class ChargerEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdateScheduler.updateBatteryWidgets(context)
    }
}
