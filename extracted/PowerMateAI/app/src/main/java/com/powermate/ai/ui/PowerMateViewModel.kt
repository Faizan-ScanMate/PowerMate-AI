package com.powermate.ai.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powermate.ai.alerts.ChargingAlertManager
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.data.repository.ChargingSessionRepository
import com.powermate.ai.data.usage.AppUsageStatsManager
import com.powermate.ai.domain.coach.ChargingCoach
import com.powermate.ai.domain.competitive.CompetitiveFeature
import com.powermate.ai.domain.competitive.CompetitiveFeatureRegistry
import com.powermate.ai.domain.insights.BatteryInsightsEngine
import com.powermate.ai.domain.model.AppSettings
import com.powermate.ai.domain.model.AppUsageEntry
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.DiagnosticResult
import com.powermate.ai.domain.model.OptimizationSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Holds UI state for the app. As a real androidx ViewModel (not a plain class), this instance
 * survives configuration changes such as rotation, and its [viewModelScope] is cancelled
 * automatically when the owning Activity is finished for good — so the refresh loop below
 * never leaks or keeps running after the screen is gone.
 */
class PowerMateViewModel(
    appContext: Context,
    private val batteryStatsManager: BatteryStatsManager,
    private val repository: ChargingSessionRepository,
    private val preferences: PowerMatePreferences,
    private val appUsageStatsManager: AppUsageStatsManager
) : ViewModel() {
    private val chargingCoach = ChargingCoach()
    private val insightsEngine = BatteryInsightsEngine()
    private val alertManager = ChargingAlertManager(appContext)

    var snapshot by mutableStateOf(BatterySnapshot())
        private set
    var settings by mutableStateOf(preferences.load())
        private set
    var sessions by mutableStateOf<List<ChargingSession>>(emptyList())
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
    /** Recent battery percent readings, newest last — feeds the live mini-graph with real data only. */
    val recentLevelHistory = MutableStateFlow<List<Float>>(emptyList())
    val competitiveFeatures: List<CompetitiveFeature> = CompetitiveFeatureRegistry.all()
    private var lastUsageRefreshAt = 0L

    init {
        // Load history off the main thread; UI starts with an empty list and fills in once ready.
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) { repository.recentSessions() }
            sessions = loaded
            insights = insightsEngine.build(snapshot, sessions)
        }
    }

    /**
     * Suspends until cancelled, calling [refresh] roughly once per second. Intended to be
     * launched from a lifecycle-aware scope (e.g. `repeatOnLifecycle(STARTED)`) so that it
     * automatically pauses while the app is backgrounded or the screen is off, instead of
     * polling forever regardless of visibility.
     */
    suspend fun runRefreshLoop() {
        while (isActive) {
            refresh()
            kotlinx.coroutines.delay(1_000)
        }
    }

    private suspend fun refresh() {
        snapshot = withContext(Dispatchers.Default) { batteryStatsManager.currentSnapshot() }
        chargingSuggestions = chargingCoach.suggest(snapshot)
        recentLevelHistory.value = (recentLevelHistory.value + snapshot.levelPercent.toFloat()).takeLast(40)
        refreshAppUsageIfNeeded()
        withContext(Dispatchers.Default) { alertManager.evaluate(snapshot, settings) }

        if (isDiagnosticRunning) {
            diagnosticSeconds += 1
            withContext(Dispatchers.IO) { repository.addDiagnosticReading(snapshot) }
            if (diagnosticSeconds >= 60) {
                completeDiagnostic()
                return
            }
        }
        insights = insightsEngine.build(snapshot, sessions)
    }

    fun startDiagnostic() {
        isDiagnosticRunning = true
        diagnosticSeconds = 0
        viewModelScope.launch(Dispatchers.IO) { repository.beginDiagnostic() }
    }

    fun completeDiagnostic() {
        if (!isDiagnosticRunning && diagnosticSeconds == 0) return
        isDiagnosticRunning = false
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) { repository.completeDiagnostic() }
            latestDiagnostic = result
            sessions = withContext(Dispatchers.IO) { repository.recentSessions() }
            insights = insightsEngine.build(snapshot, sessions)
        }
    }

    fun updateSettings(update: (AppSettings) -> AppSettings) {
        settings = update(settings)
        viewModelScope.launch(Dispatchers.IO) { preferences.save(settings) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repository.clearHistory() }
            sessions = emptyList()
            recentLevelHistory.value = emptyList()
            insights = insightsEngine.build(snapshot, sessions)
        }
    }

    private var isRefreshingUsage = false

    fun refreshAppUsageNow() {
        if (isRefreshingUsage) return
        isRefreshingUsage = true
        viewModelScope.launch {
            val access = withContext(Dispatchers.IO) { appUsageStatsManager.hasUsageAccess() }
            hasUsageStatsAccess = access
            appUsageEntries = if (access) {
                withContext(Dispatchers.IO) { appUsageStatsManager.topAppsSince(hours = 24, limit = 12) }
            } else {
                emptyList()
            }
            lastUsageRefreshAt = System.currentTimeMillis()
            isRefreshingUsage = false
        }
    }

    private fun refreshAppUsageIfNeeded() {
        val now = System.currentTimeMillis()
        if (now - lastUsageRefreshAt >= 10_000L) {
            refreshAppUsageNow()
        }
    }
}
