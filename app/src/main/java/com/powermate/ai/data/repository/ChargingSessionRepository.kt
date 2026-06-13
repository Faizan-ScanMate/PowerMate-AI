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
        val result = DiagnosticScorer.score(diagnosticBuffer.toList())
        database.insertDiagnostic(result)
        saveSyntheticSession(result)
        diagnosticBuffer.clear()
        return result
    }

    fun recentSessions(): List<ChargingSession> = database.recentSessions()

    fun clearHistory() = database.clearAll()

    private fun saveSyntheticSession(result: DiagnosticResult) {
        val session = ChargingSession(
            id = result.id,
            startTime = result.timestamp - 60_000,
            endTime = result.timestamp,
            startBatteryPercent = 0,
            endBatteryPercent = null,
            averageCurrentMa = result.averageCurrentMa,
            peakCurrentMa = result.peakCurrentMa,
            averageWattage = result.averageWattage,
            peakWattage = result.peakWattage,
            minTemperatureC = null,
            maxTemperatureC = null,
            stabilityScore = result.stabilityScore,
            chargerScore = result.chargerScore,
            cableScore = result.cableScore,
            pluggedType = com.powermate.ai.domain.model.PluggedType.Unknown,
            userLabel = "Diagnostic ${result.chargerScore}/100"
        )
        database.insertSession(session)
    }
}

fun Float?.formatMetric(unit: String, fallback: String = "--"): String =
    this?.let { "${(it * 10f).roundToInt() / 10f} $unit" } ?: fallback
