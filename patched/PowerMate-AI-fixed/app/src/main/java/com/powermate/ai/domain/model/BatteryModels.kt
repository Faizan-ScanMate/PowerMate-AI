package com.powermate.ai.domain.model

import kotlin.math.abs

enum class ChargingStatus(val label: String) {
    Charging("Charging"),
    FastCharging("Fast charging"),
    SlowCharging("Slow charging"),
    VeryFastCharging("Very fast charging"),
    UnstableCharging("Unstable charging"),
    Full("Full"),
    NotCharging("Not charging"),
    Unknown("Unknown")
}

enum class PluggedType(val label: String) {
    Usb("USB"),
    Ac("AC"),
    Wireless("Wireless"),
    Dock("Dock"),
    None("None"),
    Unknown("Unknown")
}

enum class BatteryHealthStatus(val label: String) {
    Good("Good"),
    Overheat("Overheat"),
    Cold("Cold"),
    Dead("Dead"),
    OverVoltage("Over voltage"),
    Unknown("Unknown")
}

enum class NotificationFormat(val label: String) {
    Compact("Compact"),
    Detailed("Detailed"),
    WattFocused("Watt focused"),
    TemperatureFocused("Temperature focused")
}

enum class AodMetric(val label: String) {
    BatteryPercent("Battery percent"),
    Current("Current"),
    Wattage("Wattage"),
    Voltage("Voltage"),
    Temperature("Temperature"),
    TimeToFull("Time to full"),
    ChargerScore("Charger score")
}

enum class SpeedometerStyle(val label: String) {
    RingMeter("Ring meter"),
    ArcGauge("Arc gauge"),
    MinimalText("Minimal text"),
    CyberPulse("Cyber pulse"),
    ClassicNeedle("Classic needle"),
    PixelClean("Pixel clean")
}

// AOD display style — selectable in Looks screen
enum class AodDisplayStyle(val label: String, val description: String) {
    PixelClean("Pixel Clean", "Clock, ring and speed — clean layout"),
    MinimalNeon("Minimal Neon", "AMOLED black with accent pulse glow"),
    RingMeter("Ring Meter", "Large battery ring for glanceable charging"),
    CyberPulse("Cyber Pulse", "High-energy pulse line display"),
    ClassicBattery("Classic Battery", "Classic percentage and status text"),
    UltraMinimal("Ultra Minimal", "Only time, percent and tiny status"),
    SpeedGlow("Speed Glow", "Charging speed-focused layout"),
    TextOnly("Text Only", "Lowest visual load — clean typography")
}

data class BatterySnapshot(
    val timestamp: Long = System.currentTimeMillis(),
    val levelPercent: Int = 0,
    val preciseLevelPercent: Float? = null,
    val isCharging: Boolean = false,
    val status: ChargingStatus = ChargingStatus.Unknown,
    val pluggedType: PluggedType = PluggedType.Unknown,
    val currentMicroAmp: Int? = null,
    val averageCurrentMicroAmp: Int? = null,
    val chargeCounterMicroAh: Int? = null,
    val capacityPercentFromFuelGauge: Int? = null,
    val energyCounterNanoWh: Long? = null,
    val voltageMilliVolt: Int? = null,
    val temperatureCelsius: Float? = null,
    val wattage: Float? = null,
    val health: BatteryHealthStatus = BatteryHealthStatus.Unknown,
    val technology: String? = null,
    val timeToFullMinutes: Int? = null,
    val timeToEmptyMinutes: Int? = null,
    val isSensorReliable: Boolean = true
) {
    val currentMilliAmp: Float?
        get() = currentMicroAmp?.let { kotlin.math.abs(it) / 1000f }

    val averageCurrentMilliAmp: Float?
        get() = averageCurrentMicroAmp?.let { kotlin.math.abs(it) / 1000f }

    val voltageVolt: Float?
        get() = voltageMilliVolt?.let { it / 1000f }

    val chargeCounterMah: Float?
        get() = chargeCounterMicroAh?.let { kotlin.math.abs(it) / 1000f }
}

data class ChargingReading(
    val timestamp: Long,
    val batteryPercent: Int,
    val currentMa: Float?,
    val wattage: Float?,
    val voltageMv: Int?,
    val temperatureC: Float?
)

data class ChargingSession(
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val startBatteryPercent: Int,
    val endBatteryPercent: Int?,
    val averageCurrentMa: Float?,
    val peakCurrentMa: Float?,
    val averageWattage: Float?,
    val peakWattage: Float?,
    val minTemperatureC: Float?,
    val maxTemperatureC: Float?,
    val stabilityScore: Int?,
    val chargerScore: Int?,
    val cableScore: Int?,
    val pluggedType: PluggedType,
    val userLabel: String? = null
)

data class DiagnosticResult(
    val id: String,
    val timestamp: Long,
    val chargerScore: Int,
    val cableScore: Int,
    val stabilityScore: Int,
    val averageCurrentMa: Float?,
    val peakCurrentMa: Float?,
    val averageWattage: Float?,
    val peakWattage: Float?,
    val temperatureSafety: String,
    val recommendation: String
)

data class AppSettings(
    val amoledMode: Boolean = true,
    val showWattageInsteadOfAmpere: Boolean = false,
    val alertAt80: Boolean = true,
    val alertAt90: Boolean = false,
    val alertWhenFull: Boolean = true,
    val overheatAlert: Boolean = true,
    val slowChargingAlert: Boolean = true,
    val chargerDisconnectedAlert: Boolean = true,
    val unstableChargingAlert: Boolean = true,
    val batteryDrainingFastAlert: Boolean = true,
    val quietHoursEnabled: Boolean = false,
    val aodEnabled: Boolean = true,
    val burnInProtection: Boolean = true,
    val nightDimMode: Boolean = true,
    val autoPositionShift: Boolean = true,
    val showAodMediaControls: Boolean = false,
    val showAodCameraShortcut: Boolean = false,
    val showAodTorchShortcut: Boolean = false,
    val aodTimeoutMinutes: Int = 30,
    val selectedSpeedometerStyle: SpeedometerStyle = SpeedometerStyle.RingMeter,
    val notificationFormat: NotificationFormat = NotificationFormat.Detailed,
    val temperatureUnit: String = "°C",
    // Dashboard customization
    val showCurrentOnDashboard: Boolean = true,
    val showVoltageOnDashboard: Boolean = true,
    val showTemperatureOnDashboard: Boolean = true,
    val showWattageOnDashboard: Boolean = true,
    val showCapacityOnDashboard: Boolean = false,
    val showTimeToFullOnDashboard: Boolean = true,
    val dashboardCompactMode: Boolean = false,
    val aodPrimaryMetric: String = "percent",
    // Looks / Theme customization
    val selectedThemePreset: String = "PowerBlue",
    val accentColorHex: String = "#00B4D8",
    val cardColorHex: String = "#1A1A2E",
    val fontScale: Float = 1.0f,
    val selectedFontStyle: String = "Default",
    // AOD display style
    val selectedAodStyle: AodDisplayStyle = AodDisplayStyle.PixelClean,
    // AOD accent colour (independent from main accent)
    val aodAccentColorHex: String = "#00B4D8"
)
