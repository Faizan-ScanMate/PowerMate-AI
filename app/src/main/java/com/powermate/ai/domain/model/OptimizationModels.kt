package com.powermate.ai.domain.model

enum class OptimizationActionType {
    InternetPanel,
    WifiSettings,
    BluetoothSettings,
    LocationSettings,
    DisplaySettings,
    BatterySaverSettings,
    NfcSettings,
    UsageAccessSettings,
    None
}

enum class OptimizationImpact(val label: String) {
    High("High impact"),
    Medium("Medium impact"),
    Low("Low impact"),
    Info("Info")
}

data class OptimizationSuggestion(
    val title: String,
    val reason: String,
    val actionLabel: String,
    val actionType: OptimizationActionType,
    val impact: OptimizationImpact,
    val isPrimary: Boolean = false
)
