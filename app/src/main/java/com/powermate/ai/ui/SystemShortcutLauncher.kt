package com.powermate.ai.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.powermate.ai.domain.model.OptimizationActionType

fun openOptimizationShortcut(context: Context, actionType: OptimizationActionType) {
    val intent = when (actionType) {
        OptimizationActionType.InternetPanel -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_WIRELESS_SETTINGS)
            }
        }
        OptimizationActionType.WifiSettings -> Intent(Settings.ACTION_WIFI_SETTINGS)
        OptimizationActionType.BluetoothSettings -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        OptimizationActionType.LocationSettings -> Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        OptimizationActionType.DisplaySettings -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
        OptimizationActionType.BatterySaverSettings -> Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        OptimizationActionType.NfcSettings -> Intent(Settings.ACTION_NFC_SETTINGS)
        OptimizationActionType.UsageAccessSettings -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        OptimizationActionType.None -> return
    }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
