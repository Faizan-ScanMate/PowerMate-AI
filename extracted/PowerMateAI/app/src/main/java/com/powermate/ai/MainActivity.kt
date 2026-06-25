package com.powermate.ai

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.powermate.ai.ui.PowerMateRoot
import com.powermate.ai.ui.PowerMateViewModel
import com.powermate.ai.ui.PowerMateViewModelFactory
import com.powermate.ai.ui.theme.PowerMateTheme

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val controller: PowerMateViewModel by viewModels {
        PowerMateViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            PowerMateTheme {
                PowerMateRoot(controller = controller)
            }
        }
    }
}
