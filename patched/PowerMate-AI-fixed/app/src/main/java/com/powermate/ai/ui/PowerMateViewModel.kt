package com.powermate.ai.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.data.repository.ChargingSessionRepository
import com.powermate.ai.data.usage.AppUsageStatsManager
import com.powermate.ai.domain.coach.ChargingCoach
import com.powermate.ai.domain.competitive.CompetitiveFeature
import com.powermate.ai.domain.competitive.CompetitiveFeatureRegistry
import com.powermate.ai.domain.insights.AdvancedBatteryInsights
import com.powermate.ai.domain.insights.BatteryInsightsEngine
import com.powermate.ai.domain.model.AppSettings
import com.powermate.ai.domain.model.AppUsageEntry
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.DiagnosticResult
import com.powermate.ai.domain.model.OptimizationSuggestion

class PowerMateViewModel(
    private val batteryStatsManager: BatteryStatsManager,
    private val repository: ChargingSessionRepository,
    private val preferences: PowerMatePreferences,
    private val appUsageStatsManager: AppUsageStatsManager
) {
    private val chargingCoach = ChargingCoach()
    private val insightsEngine = BatteryInsightsEngine()

    var snapshot by mutableStateOf(BatterySnapshot())
        private set
    var settings by mutableStateOf(preferences.load())
        private set
    var sessions by mutableStateOf(repository.recentSessions())
        private set
    var insights by mutableStateOf(insightsEngine.build(snapshot, sessions))
        private set
    var isDiagnosticRunning by mutableStateOf(false)
        private set
    var diagnosticSeconds by mutableStateOf(0)
        private set
    var latestDiagnostic by mutableStateOf<DiagnosticResult?>(null)
        private set
    var chargingSuggestions by mutableStateOf<List<OptimizationSuggestion>>(emptyList())
        private set
    var appUsageEntries by mutableStateOf<List<AppUsageEntry>>(emptyList())
        private set
    var hasUsageStatsAccess by mutableStateOf(false)
        private set
    val competitiveFeatures: List<CompetitiveFeature> = CompetitiveFeatureRegistry.all()
    private var lastUsageRefreshAt = 0L

    fun refresh() {
        snapshot = batteryStatsManager.currentSnapshot()
        sessions = repository.recentSessions()
        insights = insightsEngine.build(snapshot, sessions)
        chargingSuggestions = chargingCoach.suggest(snapshot)
        refreshAppUsageIfNeeded()
        if (isDiagnosticRunning) {
            diagnosticSeconds += 1
            repository.addDiagnosticReading(snapshot)
            if (diagnosticSeconds >= 60) completeDiagnostic()
        }
    }

    fun startDiagnostic() {
        isDiagnosticRunning = true
        diagnosticSeconds = 0
        repository.beginDiagnostic()
    }

    fun completeDiagnostic() {
        if (!isDiagnosticRunning && diagnosticSeconds == 0) return
        isDiagnosticRunning = false
        latestDiagnostic = repository.completeDiagnostic()
        sessions = repository.recentSessions()
        insights = insightsEngine.build(snapshot, sessions)
    }

    fun updateSettings(update: (AppSettings) -> AppSettings) {
        settings = update(settings)
        preferences.save(settings)
    }

    fun clearHistory() {
        repository.clearHistory()
        sessions = repository.recentSessions()
        insights = insightsEngine.build(snapshot, sessions)
    }

    fun refreshAppUsageNow() {
        hasUsageStatsAccess = appUsageStatsManager.hasUsageAccess()
        appUsageEntries = if (hasUsageStatsAccess) {
            appUsageStatsManager.topAppsSince(hours = 24, limit = 12)
        } else {
            emptyList()
        }
        lastUsageRefreshAt = System.currentTimeMillis()
    }

    private fun refreshAppUsageIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastUsageRefreshAt >= 10_000L) {
            refreshAppUsageNow()
        }
    }
}
