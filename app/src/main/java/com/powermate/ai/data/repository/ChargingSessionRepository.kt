package com.powermate.ai.data.repository

import com.powermate.ai.data.local.PowerMateDatabase
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.DiagnosticResult
import com.powermate.ai.domain.scoring.DiagnosticScorer
import kotlin.math.roundToInt

class ChargingSessionRepository(private val database: PowerMateDatabase) {
    private val diagnosticBuffer = mutableListOf<BatterySnapshot>()

    fun beginDiagnostic() {
        diagnosticBuffer.clear()
    }

    fun addDiagnosticReading(snapshot: BatterySnapshot) {
        diagnosticBuffer += snapshot
    }

    fun completeDiagnostic(): DiagnosticResult {
        val readings = diagnosticBuffer.toList()
        val result = DiagnosticScorer.score(readings)
        database.insertDiagnostic(result)
        saveSyntheticSession(result, readings)
        diagnosticBuffer.clear()
        return result
    }

    fun recentSessions(): List<ChargingSession> = database.recentSessions()

    fun clearHistory() = database.clearAll()

    private fun saveSyntheticSession(result: DiagnosticResult, readings: List<BatterySnapshot>) {
        val first = readings.firstOrNull()
        val last = readings.lastOrNull()
        val temps = readings.mapNotNull { it.temperatureCelsius }
        val session = ChargingSession(
            id = result.id,
            startTime = first?.timestamp ?: (result.timestamp - 60_000),
            endTime = last?.timestamp ?: result.timestamp,
            startBatteryPercent = first?.levelPercent ?: 0,
            endBatteryPercent = last?.levelPercent,
            averageCurrentMa = result.averageCurrentMa,
            peakCurrentMa = result.peakCurrentMa,
            averageWattage = result.averageWattage,
            peakWattage = result.peakWattage,
            minTemperatureC = temps.minOrNull(),
            maxTemperatureC = temps.maxOrNull(),
            stabilityScore = result.stabilityScore,
            chargerScore = result.chargerScore,
            cableScore = result.cableScore,
            pluggedType = last?.pluggedType ?: com.powermate.ai.domain.model.PluggedType.Unknown,
            userLabel = "Diagnostic ${result.chargerScore}/100"
        )
        database.insertSession(session)
    }
}

fun Float?.formatMetric(unit: String, fallback: String = "--"): String =
    this?.let { "${(it * 10f).roundToInt() / 10f} $unit" } ?: fallback
