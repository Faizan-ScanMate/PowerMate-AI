package com.powermate.ai.domain.scoring

import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.DiagnosticResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object ChargingSpeedClassifier {
    fun classify(snapshot: BatterySnapshot): ChargingStatus {
        if (!snapshot.isCharging) return ChargingStatus.NotCharging
        val wattage = snapshot.wattage ?: return ChargingStatus.Charging
        return when {
            wattage < 4.5f -> ChargingStatus.SlowCharging
            wattage < 12f -> ChargingStatus.Charging
            wattage < 25f -> ChargingStatus.FastCharging
            else -> ChargingStatus.VeryFastCharging
        }
    }
}

object DiagnosticScorer {
    fun score(readings: List<BatterySnapshot>): DiagnosticResult {
        val validWatts = readings.mapNotNull { it.wattage }.filter { it > 0f }
        val validCurrent = readings.mapNotNull { it.currentMilliAmp }.filter { it > 0f }
        val validTemps = readings.mapNotNull { it.temperatureCelsius }
        val avgW = validWatts.averageOrNull()
        val peakW = validWatts.maxOrNull()
        val avgCurrent = validCurrent.averageOrNull()
        val peakCurrent = validCurrent.maxOrNull()
        val stability = calculateStability(validCurrent)
        val tempSafety = calculateTempSafety(validTemps)
        val chargerScore = clampScore(
            ((avgW ?: 0f).normalize(0f, 25f) * 0.35f) +
                ((peakW ?: 0f).normalize(0f, 35f) * 0.20f) +
                (stability * 0.25f) +
                (tempSafety.score * 0.20f)
        )
        val cableScore = clampScore(
            (stability * 0.40f) +
                ((peakCurrent ?: 0f).normalize(0f, 4500f) * 0.25f) +
                ((avgCurrent ?: 0f).normalize(0f, 3000f) * 0.20f) +
                15f
        )
        val recommendation = when {
            chargerScore >= 88 && cableScore >= 85 -> "Excellent charger and cable. Charging is fast and stable."
            chargerScore >= 70 -> "Good charging setup. Keep temperature below 40°C for battery care."
            cableScore < 55 -> "Cable may be limiting charging speed. Try another cable."
            chargerScore < 55 -> "Charging is slow or unstable. Try a higher-quality charger."
            else -> "Charging is usable, but stability can improve."
        }
        return DiagnosticResult(
            id = System.currentTimeMillis().toString(),
            timestamp = System.currentTimeMillis(),
            chargerScore = chargerScore,
            cableScore = cableScore,
            stabilityScore = stability,
            averageCurrentMa = avgCurrent,
            peakCurrentMa = peakCurrent,
            averageWattage = avgW,
            peakWattage = peakW,
            temperatureSafety = tempSafety.label,
            recommendation = recommendation
        )
    }

    private fun calculateStability(values: List<Float>): Int {
        if (values.size < 3) return 70
        val avg = values.averageOrNull() ?: return 70
        if (avg <= 0f) return 70
        val meanAbsDeviation = values.map { abs(it - avg) }.averageOrNull() ?: return 70
        val penalty = ((meanAbsDeviation / avg) * 100f).toInt()
        return (100 - penalty).coerceIn(35, 100)
    }

    private fun calculateTempSafety(values: List<Float>): TempSafety {
        val maxTemp = values.maxOrNull() ?: return TempSafety("Unknown", 75)
        return when {
            maxTemp < 38f -> TempSafety("Safe", 100)
            maxTemp < 42f -> TempSafety("Warm", 80)
            maxTemp < 46f -> TempSafety("Hot", 55)
            else -> TempSafety("Too hot", 25)
        }
    }

    private data class TempSafety(val label: String, val score: Int)
    private fun List<Float>.averageOrNull(): Float? = if (isEmpty()) null else average().toFloat()
    private fun Float.normalize(minValue: Float, maxValue: Float): Float {
        if (maxValue <= minValue) return 0f
        return ((this - minValue) / (maxValue - minValue)).coerceIn(0f, 1f) * 100f
    }
    private fun clampScore(value: Float): Int = min(100, max(0, value.toInt()))
}
