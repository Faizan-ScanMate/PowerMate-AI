package com.powermate.ai.domain.coach

import com.powermate.ai.domain.model.BatteryHealthStatus
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.OptimizationActionType
import com.powermate.ai.domain.model.OptimizationImpact
import com.powermate.ai.domain.model.OptimizationSuggestion

/**
 * Produces safe, privacy-friendly charging-speed tips.
 * The coach never pretends to directly change restricted system toggles.
 * It opens Android settings shortcuts so the user stays in control.
 */
class ChargingCoach {
    fun suggest(snapshot: BatterySnapshot): List<OptimizationSuggestion> {
        val suggestions = mutableListOf<OptimizationSuggestion>()

        if (!snapshot.isCharging) {
            suggestions += OptimizationSuggestion(
                title = "Connect charger to start optimization",
                reason = "PowerMate can analyze cable, charger, temperature and current once charging begins.",
                actionLabel = "Waiting",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Info,
                isPrimary = true
            )
        }

        val temp = snapshot.temperatureCelsius
        if ((temp != null && temp >= 38f) || snapshot.health == BatteryHealthStatus.Overheat) {
            suggestions += OptimizationSuggestion(
                title = "Cool the phone for faster safe charging",
                reason = "High temperature can slow charging. Remove a thick case, stop heavy apps, and keep the phone away from heat.",
                actionLabel = "Open display",
                actionType = OptimizationActionType.DisplaySettings,
                impact = OptimizationImpact.High,
                isPrimary = true
            )
        }

        if (snapshot.isCharging && snapshot.status == ChargingStatus.SlowCharging) {
            suggestions += OptimizationSuggestion(
                title = "Reduce network drain while charging",
                reason = "Wi‑Fi/mobile data, hotspot and background sync can consume power while charging, especially on weak signals.",
                actionLabel = "Internet controls",
                actionType = OptimizationActionType.InternetPanel,
                impact = OptimizationImpact.High,
                isPrimary = true
            )
            suggestions += OptimizationSuggestion(
                title = "Check cable and adapter",
                reason = "Slow charging is often caused by a weak cable, low-power USB port, dirty connector, or non-fast adapter.",
                actionLabel = "Run test",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.High
            )
        }

        suggestions += OptimizationSuggestion(
            title = "Turn off Bluetooth if unused",
            reason = "Bluetooth devices and scanning can add small background drain during charging.",
            actionLabel = "Bluetooth",
            actionType = OptimizationActionType.BluetoothSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Turn off location if not needed",
            reason = "GPS/location scanning can increase heat and background power use.",
            actionLabel = "Location",
            actionType = OptimizationActionType.LocationSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Use low or auto brightness",
            reason = "The display is one of the biggest power users. Lower brightness helps more power go into the battery.",
            actionLabel = "Display",
            actionType = OptimizationActionType.DisplaySettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Enable Battery Saver during slow charging",
            reason = "Battery Saver can reduce background work and help low-power chargers perform better.",
            actionLabel = "Battery Saver",
            actionType = OptimizationActionType.BatterySaverSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Turn off NFC if unused",
            reason = "NFC usually has low drain, but disabling unused radios keeps charging cleaner on some devices.",
            actionLabel = "NFC",
            actionType = OptimizationActionType.NfcSettings,
            impact = OptimizationImpact.Low
        )

        if (snapshot.isCharging && snapshot.levelPercent >= 80) {
            suggestions += OptimizationSuggestion(
                title = "Stop around 80–90% for battery care",
                reason = "Charging becomes slower near full and can add extra battery wear. Use the 80% or 90% reminder for daily charging.",
                actionLabel = "Info",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Info
            )
        }

        if (snapshot.pluggedType.label == "Wireless") {
            suggestions += OptimizationSuggestion(
                title = "Use wired charging for fastest speed",
                reason = "Wireless charging is convenient but can be warmer and slower than a quality USB-C cable and wall adapter.",
                actionLabel = "Run test",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Medium
            )
        }

        if (!snapshot.isSensorReliable) {
            suggestions += OptimizationSuggestion(
                title = "Sensor precision may be limited",
                reason = "Some phones do not expose accurate current data. Use charger comparison results on the same phone.",
                actionLabel = "Info",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Info
            )
        }

        return suggestions.distinctBy { it.title }.sortedWith(
            compareByDescending<OptimizationSuggestion> { it.isPrimary }
                .thenBy { it.impact.ordinal }
        )
    }
}
