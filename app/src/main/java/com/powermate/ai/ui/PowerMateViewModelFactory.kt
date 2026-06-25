package com.powermate.ai.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.local.PowerMateDatabase
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.data.repository.ChargingSessionRepository
import com.powermate.ai.data.usage.AppUsageStatsManager

/**
 * Builds [PowerMateViewModel] with its real dependencies. Using a factory (instead of
 * constructing the ViewModel directly in onCreate) is what lets androidx's `viewModels()`
 * delegate keep the same instance alive across configuration changes such as rotation.
 */
class PowerMateViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = PowerMateDatabase(appContext)
        @Suppress("UNCHECKED_CAST")
        return PowerMateViewModel(
            appContext = appContext,
            batteryStatsManager = BatteryStatsManager(appContext),
            repository = ChargingSessionRepository(database),
            preferences = PowerMatePreferences(appContext),
            appUsageStatsManager = AppUsageStatsManager(appContext)
        ) as T
    }
}
