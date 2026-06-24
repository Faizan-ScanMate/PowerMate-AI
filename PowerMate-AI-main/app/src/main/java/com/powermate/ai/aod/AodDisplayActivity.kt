package com.powermate.ai.aod

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.ui.components.BatteryRing
import com.powermate.ai.ui.components.StatusChip
import com.powermate.ai.ui.theme.Cyan
import com.powermate.ai.ui.theme.PowerMateTheme
import com.powermate.ai.ui.theme.TextMain
import com.powermate.ai.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AodDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            PowerMateTheme {
                AodScreen(BatteryStatsManager(this))
            }
        }
    }
}

@Composable
private fun AodScreen(manager: BatteryStatsManager) {
    var snapshot by remember { mutableStateOf(manager.currentSnapshot()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            snapshot = manager.currentSnapshot()
            now = System.currentTimeMillis()
            delay(5_000)
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(28.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 56.sp, fontWeight = FontWeight.Bold)
            Text(SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(now)), color = TextSecondary, fontSize = 15.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            BatteryRing(level = snapshot.levelPercent, modifier = Modifier.size(220.dp))
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatusChip(snapshot.status.label, Cyan)
                StatusChip(snapshot.wattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W", Color(0xFF22C55E))
            }
        }
        Column(
            modifier = Modifier.background(Color(0xFF08111F), RoundedCornerShape(24.dp)).padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AMOLED-safe charging display", color = TextMain, fontWeight = FontWeight.SemiBold)
            Text("Tap back/home to close", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
