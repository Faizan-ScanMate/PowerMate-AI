package com.powermate.ai.data.preferences

import android.content.Context
import com.powermate.ai.domain.model.AppSettings
import com.powermate.ai.domain.model.AodDisplayStyle
import com.powermate.ai.domain.model.NotificationFormat
import com.powermate.ai.domain.model.SpeedometerStyle

class PowerMatePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("powermate_prefs", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        amoledMode = prefs.getBoolean("amoledMode", true),
        showWattageInsteadOfAmpere = prefs.getBoolean("showWattageInsteadOfAmpere", false),
        alertAt80 = prefs.getBoolean("alertAt80", true),
        alertAt90 = prefs.getBoolean("alertAt90", false),
        alertWhenFull = prefs.getBoolean("alertWhenFull", true),
        overheatAlert = prefs.getBoolean("overheatAlert", true),
        slowChargingAlert = prefs.getBoolean("slowChargingAlert", true),
        chargerDisconnectedAlert = prefs.getBoolean("chargerDisconnectedAlert", true),
        unstableChargingAlert = prefs.getBoolean("unstableChargingAlert", true),
        batteryDrainingFastAlert = prefs.getBoolean("batteryDrainingFastAlert", true),
        quietHoursEnabled = prefs.getBoolean("quietHoursEnabled", false),
        aodEnabled = prefs.getBoolean("aodEnabled", true),
        burnInProtection = prefs.getBoolean("burnInProtection", true),
        nightDimMode = prefs.getBoolean("nightDimMode", true),
        autoPositionShift = prefs.getBoolean("autoPositionShift", true),
        showAodMediaControls = prefs.getBoolean("showAodMediaControls", false),
        showAodCameraShortcut = prefs.getBoolean("showAodCameraShortcut", false),
        showAodTorchShortcut = prefs.getBoolean("showAodTorchShortcut", false),
        aodTimeoutMinutes = prefs.getInt("aodTimeoutMinutes", 30),
        selectedSpeedometerStyle = enumValueOrDefault(prefs.getString("selectedSpeedometerStyle", null), SpeedometerStyle.RingMeter),
        notificationFormat = enumValueOrDefault(prefs.getString("notificationFormat", null), NotificationFormat.Detailed),
        temperatureUnit = prefs.getString("temperatureUnit", "°C") ?: "°C",
        showCurrentOnDashboard = prefs.getBoolean("showCurrentOnDashboard", true),
        showVoltageOnDashboard = prefs.getBoolean("showVoltageOnDashboard", true),
        showTemperatureOnDashboard = prefs.getBoolean("showTemperatureOnDashboard", true),
        showWattageOnDashboard = prefs.getBoolean("showWattageOnDashboard", true),
        showCapacityOnDashboard = prefs.getBoolean("showCapacityOnDashboard", false),
        showTimeToFullOnDashboard = prefs.getBoolean("showTimeToFullOnDashboard", true),
        dashboardCompactMode = prefs.getBoolean("dashboardCompactMode", false),
        aodPrimaryMetric = prefs.getString("aodPrimaryMetric", "percent") ?: "percent",
        selectedThemePreset = prefs.getString("selectedThemePreset", "PowerBlue") ?: "PowerBlue",
        accentColorHex = prefs.getString("accentColorHex", "#00B4D8") ?: "#00B4D8",
        cardColorHex = prefs.getString("cardColorHex", "#1A1A2E") ?: "#1A1A2E",
        fontScale = prefs.getFloat("fontScale", 1.0f),
        selectedFontStyle = prefs.getString("selectedFontStyle", "Default") ?: "Default",
        selectedAodStyle = enumValueOrDefault(prefs.getString("selectedAodStyle", null), AodDisplayStyle.PixelClean),
        aodAccentColorHex = prefs.getString("aodAccentColorHex", "#00B4D8") ?: "#00B4D8"
    )

    fun save(settings: AppSettings) {
        prefs.edit()
            .putBoolean("amoledMode", settings.amoledMode)
            .putBoolean("showWattageInsteadOfAmpere", settings.showWattageInsteadOfAmpere)
            .putBoolean("alertAt80", settings.alertAt80)
            .putBoolean("alertAt90", settings.alertAt90)
            .putBoolean("alertWhenFull", settings.alertWhenFull)
            .putBoolean("overheatAlert", settings.overheatAlert)
            .putBoolean("slowChargingAlert", settings.slowChargingAlert)
            .putBoolean("chargerDisconnectedAlert", settings.chargerDisconnectedAlert)
            .putBoolean("unstableChargingAlert", settings.unstableChargingAlert)
            .putBoolean("batteryDrainingFastAlert", settings.batteryDrainingFastAlert)
            .putBoolean("quietHoursEnabled", settings.quietHoursEnabled)
            .putBoolean("aodEnabled", settings.aodEnabled)
            .putBoolean("burnInProtection", settings.burnInProtection)
            .putBoolean("nightDimMode", settings.nightDimMode)
            .putBoolean("autoPositionShift", settings.autoPositionShift)
            .putBoolean("showAodMediaControls", settings.showAodMediaControls)
            .putBoolean("showAodCameraShortcut", settings.showAodCameraShortcut)
            .putBoolean("showAodTorchShortcut", settings.showAodTorchShortcut)
            .putInt("aodTimeoutMinutes", settings.aodTimeoutMinutes)
            .putString("selectedSpeedometerStyle", settings.selectedSpeedometerStyle.name)
            .putString("notificationFormat", settings.notificationFormat.name)
            .putString("temperatureUnit", settings.temperatureUnit)
            .putBoolean("showCurrentOnDashboard", settings.showCurrentOnDashboard)
            .putBoolean("showVoltageOnDashboard", settings.showVoltageOnDashboard)
            .putBoolean("showTemperatureOnDashboard", settings.showTemperatureOnDashboard)
            .putBoolean("showWattageOnDashboard", settings.showWattageOnDashboard)
            .putBoolean("showCapacityOnDashboard", settings.showCapacityOnDashboard)
            .putBoolean("showTimeToFullOnDashboard", settings.showTimeToFullOnDashboard)
            .putBoolean("dashboardCompactMode", settings.dashboardCompactMode)
            .putString("aodPrimaryMetric", settings.aodPrimaryMetric)
            .putString("selectedThemePreset", settings.selectedThemePreset)
            .putString("accentColorHex", settings.accentColorHex)
            .putString("cardColorHex", settings.cardColorHex)
            .putFloat("fontScale", settings.fontScale)
            .putString("selectedFontStyle", settings.selectedFontStyle)
            .putString("selectedAodStyle", settings.selectedAodStyle.name)
            .putString("aodAccentColorHex", settings.aodAccentColorHex)
            .apply()
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String?, fallback: T): T =
        runCatching { value?.let { enumValueOf<T>(it) } }.getOrNull() ?: fallback
}
