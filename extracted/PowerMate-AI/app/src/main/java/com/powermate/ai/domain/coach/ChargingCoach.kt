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
                title = "Plug in to start live analysis",
                reason = "PowerMate reads charger quality, cable health and temperature the moment charging starts.",
                actionLabel = "Waiting",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Info,
                isPrimary = true
            )
        }

        val temp = snapshot.temperatureCelsius
        if ((temp != null && temp >= 38f) || snapshot.health == BatteryHealthStatus.Overheat) {
            suggestions += OptimizationSuggestion(
                title = "Cool down: phone is running hot — charging is throttled",
                reason = "Above 38°C your phone deliberately slows charging to protect the battery. Remove the case and close heavy apps until it cools.",
                actionLabel = "Open display",
                actionType = OptimizationActionType.DisplaySettings,
                impact = OptimizationImpact.High,
                isPrimary = true
            )
        }

        if (snapshot.isCharging && snapshot.status == ChargingStatus.SlowCharging) {
            suggestions += OptimizationSuggestion(
                title = "Weak network signal may be fighting your charger",
                reason = "Mobile data on a poor signal burns more power than a slow charger delivers. Switch to Wi-Fi or enable airplane mode.",
                actionLabel = "Internet controls",
                actionType = OptimizationActionType.InternetPanel,
                impact = OptimizationImpact.High,
                isPrimary = true
            )
            suggestions += OptimizationSuggestion(
                title = "Your cable is the most likely bottleneck",
                reason = "Slow charging is usually the cable, not the charger. Try a USB-C cable under 1 metre with the same adapter.",
                actionLabel = "Run test",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.High
            )
        }

        suggestions += OptimizationSuggestion(
            title = "Bluetooth scanning adds hidden background drain",
            reason = "Even idle Bluetooth scans for devices every few seconds. Turn it off if you are not using wireless audio or accessories.",
            actionLabel = "Bluetooth",
            actionType = OptimizationActionType.BluetoothSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "GPS keeps running even when apps are closed",
            reason = "Location scanning adds heat and background drain. Turn it off while charging unless you actively need navigation.",
            actionLabel = "Location",
            actionType = OptimizationActionType.LocationSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Screen brightness is your biggest charging enemy",
            reason = "Your display uses more power than Bluetooth, GPS and Wi-Fi combined. Auto brightness is the single best charging habit.",
            actionLabel = "Display",
            actionType = OptimizationActionType.DisplaySettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "Battery Saver helps slow chargers keep up",
            reason = "On low-power chargers, background tasks consume almost as much as the charger delivers. Battery Saver tips the balance in your favour.",
            actionLabel = "Battery Saver",
            actionType = OptimizationActionType.BatterySaverSettings,
            impact = OptimizationImpact.Medium
        )

        suggestions += OptimizationSuggestion(
            title = "NFC polls for contactless signals even in your pocket",
            reason = "NFC drain is small but unnecessary when charging. A clean habit to switch off unused radios.",
            actionLabel = "NFC",
            actionType = OptimizationActionType.NfcSettings,
            impact = OptimizationImpact.Low
        )

        if (snapshot.isCharging && snapshot.levelPercent >= 80) {
            suggestions += OptimizationSuggestion(
                title = "You have hit 80% — unplug soon for longer battery life",
                reason = "Lithium batteries age faster above 85%. Stopping here regularly can add years to your battery's lifespan.",
                actionLabel = "Info",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Info
            )
        }

        if (snapshot.pluggedType.label == "Wireless") {
            suggestions += OptimizationSuggestion(
                title = "Wireless charging is convenient but runs hotter",
                reason = "Wireless pads are typically 30-40% slower than a good USB-C cable and generate more heat — which accelerates battery wear.",
                actionLabel = "Run test",
                actionType = OptimizationActionType.None,
                impact = OptimizationImpact.Medium
            )
        }

        if (!snapshot.isSensorReliable) {
            suggestions += OptimizationSuggestion(
                title = "This device reports limited sensor data",
                reason = "Not all phones expose accurate current readings. Charger comparison tests are still reliable when run on the same device.",
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
