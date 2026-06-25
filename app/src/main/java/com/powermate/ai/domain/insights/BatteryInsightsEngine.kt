package com.powermate.ai.domain.insights

import com.powermate.ai.domain.model.BatteryHealthStatus
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.ChargingStatus
import kotlin.math.abs
import kotlin.math.roundToInt

data class AdvancedBatteryInsights(
    val chargingHealthScore: Int,
    val batteryCareScore: Int,
    val estimatedCapacityMah: Float?,
    val capacityConfidence: String,
    val wearLevelLabel: String,
    val dischargeRateMa: Float?,
    val thermalRiskLabel: String,
    val bestChargerScore: Int?,
    val slowestChargerWarning: String?,
    val headline: String,
    val details: List<String>,
    val screenOffDrainLabel: String = "N/A"
)

class BatteryInsightsEngine {
    fun build(snapshot: BatterySnapshot, sessions: List<ChargingSession>): AdvancedBatteryInsights {
        val thermalScore = temperatureScore(snapshot.temperatureCelsius)
        val speedScore = speedScore(snapshot)
        val stabilityScore = sessions.firstOrNull()?.stabilityScore ?: 76
        val chargingHealthScore = weightedScore(
            speedScore to 0.35f,
            stabilityScore to 0.25f,
            thermalScore to 0.25f,
            sensorScore(snapshot) to 0.15f
        )
        val batteryCareScore = weightedScore(
            thermalScore to 0.45f,
            chargeLimitHabitScore(snapshot.levelPercent) to 0.25f,
            historyHabitScore(sessions) to 0.20f,
            sensorScore(snapshot) to 0.10f
        )
        val estimatedCapacity = estimateCapacityMah(snapshot)
        val bestCharger = sessions.mapNotNull { it.chargerScore }.maxOrNull()
        val slowestWarning = sessions
            .filter { (it.averageWattage ?: 99f) < 5f }
            .maxByOrNull { it.startTime }
            ?.let { "Recent slow session detected: ${it.averageWattage?.round1() ?: "--"} W average." }

        val headline = when {
            snapshot.health == BatteryHealthStatus.Overheat || (snapshot.temperatureCelsius ?: 0f) >= 42f -> "Temperature is limiting safe charging"
            snapshot.status == ChargingStatus.VeryFastCharging -> "Very fast charging looks strong"
            snapshot.status == ChargingStatus.FastCharging -> "Fast charging is active"
            snapshot.status == ChargingStatus.SlowCharging -> "Charging speed can improve"
            !snapshot.isCharging -> "Connect charger for live diagnostics"
            else -> "Charging looks normal"
        }
        val details = buildList {
            add("Charging health score combines speed, stability, temperature and sensor reliability.")
            if (estimatedCapacity != null) add("Fuel gauge estimate: about ${estimatedCapacity.roundToInt()} mAh capacity.")
            snapshot.timeToFullMinutes?.let { add("Estimated time to full: ${formatMinutes(it)}.") }
            snapshot.timeToEmptyMinutes?.let { add("Estimated runtime remaining: ${formatMinutes(it)}.") }
            if (!snapshot.isSensorReliable) add("Current sensor unavailable; PowerMate will avoid fake precision.")
        }

        val screenOffDrain = if (!snapshot.isCharging) {
            snapshot.currentMilliAmp?.let { "${it.toInt()} mA" } ?: "N/A"
        } else {
            "N/A"
        }

        return AdvancedBatteryInsights(
            chargingHealthScore = chargingHealthScore,
            batteryCareScore = batteryCareScore,
            estimatedCapacityMah = estimatedCapacity,
            capacityConfidence = if (estimatedCapacity != null) "Fuel-gauge based" else "Needs supported sensor",
            wearLevelLabel = wearLabel(batteryCareScore),
            dischargeRateMa = if (!snapshot.isCharging) snapshot.currentMilliAmp else null,
            thermalRiskLabel = thermalLabel(snapshot.temperatureCelsius),
            bestChargerScore = bestCharger,
            slowestChargerWarning = slowestWarning,
            headline = headline,
            details = details,
            screenOffDrainLabel = screenOffDrain
        )
    }

    private fun speedScore(snapshot: BatterySnapshot): Int = when (snapshot.status) {
        ChargingStatus.VeryFastCharging -> 100
        ChargingStatus.FastCharging -> 88
        ChargingStatus.Charging -> 72
        ChargingStatus.SlowCharging -> 42
        ChargingStatus.UnstableCharging -> 35
        ChargingStatus.Full -> 95
        ChargingStatus.NotCharging -> 60
        ChargingStatus.Unknown -> 55
    }

    private fun temperatureScore(temp: Float?): Int = when {
        temp == null -> 75
        temp < 10f -> 60
        temp < 36f -> 100
        temp < 39f -> 86
        temp < 42f -> 65
        temp < 46f -> 40
        else -> 20
    }

    private fun sensorScore(snapshot: BatterySnapshot): Int = if (snapshot.isSensorReliable) 100 else 58

    private fun chargeLimitHabitScore(level: Int): Int = when {
        level in 20..85 -> 100
        level in 86..92 -> 82
        level > 92 -> 58
        else -> 72
    }

    private fun historyHabitScore(sessions: List<ChargingSession>): Int {
        if (sessions.isEmpty()) return 76
        val avgMaxTemp = sessions.mapNotNull { it.maxTemperatureC }.averageOrNull() ?: return 78
        return temperatureScore(avgMaxTemp)
    }

    private fun estimateCapacityMah(snapshot: BatterySnapshot): Float? {
        val charge = snapshot.chargeCounterMah ?: return null
        val level = snapshot.capacityPercentFromFuelGauge ?: snapshot.levelPercent
        if (level !in 5..100) return null
        return (charge / (level / 100f)).takeIf { it in 500f..9000f }
    }

    private fun wearLabel(score: Int): String = when {
        score >= 88 -> "Excellent care"
        score >= 72 -> "Good care"
        score >= 55 -> "Needs better habits"
        else -> "High wear risk"
    }

    private fun thermalLabel(temp: Float?): String = when {
        temp == null -> "Unknown"
        temp < 38f -> "Safe"
        temp < 42f -> "Warm"
        temp < 46f -> "Hot"
        else -> "Too hot"
    }

    private fun weightedScore(vararg scores: Pair<Int, Float>): Int =
        scores.sumOf { (it.first * it.second).toDouble() }.roundToInt().coerceIn(0, 100)

    private fun List<Float>.averageOrNull(): Float? = if (isEmpty()) null else average().toFloat()
    private fun Float.round1(): String = String.format(java.util.Locale.US, "%.1f", this)
    private fun formatMinutes(minutes: Int): String = if (minutes < 60) "$minutes min" else "${minutes / 60}h ${minutes % 60}m"
}
