package com.powermate.ai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.local.PowerMateDatabase
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.data.repository.ChargingSessionRepository
import com.powermate.ai.data.usage.AppUsageStatsManager
import com.powermate.ai.ui.PowerMateRoot
import com.powermate.ai.ui.PowerMateViewModel
import com.powermate.ai.ui.theme.PowerMateTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val database = PowerMateDatabase(this)
        val controller = PowerMateViewModel(
            batteryStatsManager = BatteryStatsManager(this),
            repository = ChargingSessionRepository(database),
            preferences = PowerMatePreferences(this),
            appUsageStatsManager = AppUsageStatsManager(this)
        )

        setContent {
            PowerMateTheme {
                PowerMateRoot(controller = controller)
            }
        }
    }
}
