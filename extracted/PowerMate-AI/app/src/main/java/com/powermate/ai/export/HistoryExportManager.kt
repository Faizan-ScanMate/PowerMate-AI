package com.powermate.ai.export

import android.content.Context
import com.powermate.ai.domain.model.ChargingSession
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryExportManager(private val context: Context) {
    fun exportCsv(sessions: List<ChargingSession>): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "powermate-history-${System.currentTimeMillis()}.csv")
        file.writeText(buildCsv(sessions))
        return file
    }

    fun buildCsv(sessions: List<ChargingSession>): String {
        val header = "id,start,end,avg_current_ma,peak_current_ma,avg_wattage,peak_wattage,stability,charger_score,cable_score,plugged_type\n"
        val rows = sessions.joinToString("\n") { session ->
            listOf(
                session.id,
                formatTime(session.startTime),
                session.endTime?.let(::formatTime).orEmpty(),
                session.averageCurrentMa?.toString().orEmpty(),
                session.peakCurrentMa?.toString().orEmpty(),
                session.averageWattage?.toString().orEmpty(),
                session.peakWattage?.toString().orEmpty(),
                session.stabilityScore?.toString().orEmpty(),
                session.chargerScore?.toString().orEmpty(),
                session.cableScore?.toString().orEmpty(),
                session.pluggedType.name
            ).joinToString(",") { it.replace(",", " ") }
        }
        return header + rows + "\n"
    }

    private fun formatTime(time: Long): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(time))
}
