package com.powermate.ai.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.DiagnosticResult
import com.powermate.ai.domain.model.PluggedType

class PowerMateDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE charging_sessions(
                id TEXT PRIMARY KEY,
                startTime INTEGER NOT NULL,
                endTime INTEGER,
                startBatteryPercent INTEGER NOT NULL,
                endBatteryPercent INTEGER,
                averageCurrentMa REAL,
                peakCurrentMa REAL,
                averageWattage REAL,
                peakWattage REAL,
                minTemperatureC REAL,
                maxTemperatureC REAL,
                stabilityScore INTEGER,
                chargerScore INTEGER,
                cableScore INTEGER,
                pluggedType TEXT NOT NULL,
                userLabel TEXT
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE diagnostic_results(
                id TEXT PRIMARY KEY,
                timestamp INTEGER NOT NULL,
                chargerScore INTEGER NOT NULL,
                cableScore INTEGER NOT NULL,
                stabilityScore INTEGER NOT NULL,
                averageCurrentMa REAL,
                peakCurrentMa REAL,
                averageWattage REAL,
                peakWattage REAL,
                temperatureSafety TEXT NOT NULL,
                recommendation TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS charging_sessions")
        db.execSQL("DROP TABLE IF EXISTS diagnostic_results")
        onCreate(db)
    }

    fun insertSession(session: ChargingSession) {
        writableDatabase.insertWithOnConflict("charging_sessions", null, session.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun insertDiagnostic(result: DiagnosticResult) {
        writableDatabase.insertWithOnConflict("diagnostic_results", null, result.toValues(), SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun recentSessions(limit: Int = 20): List<ChargingSession> {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        val result = mutableListOf<ChargingSession>()
        cursor.use {
            while (it.moveToNext()) {
                result += ChargingSession(
                    id = it.getString(it.getColumnIndexOrThrow("id")),
                    startTime = it.getLong(it.getColumnIndexOrThrow("startTime")),
                    endTime = it.getNullableLong("endTime"),
                    startBatteryPercent = it.getInt(it.getColumnIndexOrThrow("startBatteryPercent")),
                    endBatteryPercent = it.getNullableInt("endBatteryPercent"),
                    averageCurrentMa = it.getNullableFloat("averageCurrentMa"),
                    peakCurrentMa = it.getNullableFloat("peakCurrentMa"),
                    averageWattage = it.getNullableFloat("averageWattage"),
                    peakWattage = it.getNullableFloat("peakWattage"),
                    minTemperatureC = it.getNullableFloat("minTemperatureC"),
                    maxTemperatureC = it.getNullableFloat("maxTemperatureC"),
                    stabilityScore = it.getNullableInt("stabilityScore"),
                    chargerScore = it.getNullableInt("chargerScore"),
                    cableScore = it.getNullableInt("cableScore"),
                    pluggedType = runCatching { PluggedType.valueOf(it.getString(it.getColumnIndexOrThrow("pluggedType"))) }.getOrDefault(PluggedType.Unknown),
                    userLabel = it.getStringOrNull("userLabel")
                )
            }
        }
        return result
    }

    fun clearAll() {
        writableDatabase.delete("charging_sessions", null, null)
        writableDatabase.delete("diagnostic_results", null, null)
    }

    private fun ChargingSession.toValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("startTime", startTime)
        put("endTime", endTime)
        put("startBatteryPercent", startBatteryPercent)
        put("endBatteryPercent", endBatteryPercent)
        put("averageCurrentMa", averageCurrentMa)
        put("peakCurrentMa", peakCurrentMa)
        put("averageWattage", averageWattage)
        put("peakWattage", peakWattage)
        put("minTemperatureC", minTemperatureC)
        put("maxTemperatureC", maxTemperatureC)
        put("stabilityScore", stabilityScore)
        put("chargerScore", chargerScore)
        put("cableScore", cableScore)
        put("pluggedType", pluggedType.name)
        put("userLabel", userLabel)
    }

    private fun DiagnosticResult.toValues(): ContentValues = ContentValues().apply {
        put("id", id)
        put("timestamp", timestamp)
        put("chargerScore", chargerScore)
        put("cableScore", cableScore)
        put("stabilityScore", stabilityScore)
        put("averageCurrentMa", averageCurrentMa)
        put("peakCurrentMa", peakCurrentMa)
        put("averageWattage", averageWattage)
        put("peakWattage", peakWattage)
        put("temperatureSafety", temperatureSafety)
        put("recommendation", recommendation)
    }

    private fun android.database.Cursor.getNullableInt(name: String): Int? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getInt(index)
    }

    private fun android.database.Cursor.getNullableLong(name: String): Long? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getLong(index)
    }

    private fun android.database.Cursor.getNullableFloat(name: String): Float? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getFloat(index)
    }

    private fun android.database.Cursor.getStringOrNull(name: String): String? {
        val index = getColumnIndexOrThrow(name)
        return if (isNull(index)) null else getString(index)
    }

    companion object {
        private const val DB_NAME = "powermate.db"
        private const val DB_VERSION = 1
    }
}
