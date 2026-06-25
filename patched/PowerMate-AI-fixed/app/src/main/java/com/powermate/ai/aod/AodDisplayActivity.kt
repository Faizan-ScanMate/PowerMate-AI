package com.powermate.ai.aod

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.data.battery.BatteryStatsManager
import com.powermate.ai.data.preferences.PowerMatePreferences
import com.powermate.ai.domain.model.AodDisplayStyle
import com.powermate.ai.domain.model.AppSettings
import com.powermate.ai.ui.components.BatteryRingColored
import com.powermate.ai.ui.components.StatusChip
import com.powermate.ai.ui.theme.PowerMateTheme
import com.powermate.ai.ui.theme.TextMain
import com.powermate.ai.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class AodDisplayActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val prefs = PowerMatePreferences(this)
        val settings = prefs.load()
        setContent {
            PowerMateTheme {
                AodScreen(BatteryStatsManager(this), settings)
            }
        }
    }
}

@Composable
private fun AodScreen(manager: BatteryStatsManager, settings: AppSettings) {
    var snapshot by remember { mutableStateOf(manager.currentSnapshot()) }
    var now by remember { mutableStateOf(System.currentTimeMillis()) }

    // Burn-in protection: slowly shift position
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val animX by animateFloatAsState(targetValue = offsetX, animationSpec = tween(8000), label = "burnX")
    val animY by animateFloatAsState(targetValue = offsetY, animationSpec = tween(8000), label = "burnY")

    // Night dim: 10 PM – 7 AM → lower alpha
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val isNight = hour >= 22 || hour < 7
    val screenAlpha = if (settings.nightDimMode && isNight) 0.45f else 1f

    LaunchedEffect(Unit) {
        var tick = 0
        while (true) {
            snapshot = manager.currentSnapshot()
            now = System.currentTimeMillis()
            tick++
            if (settings.burnInProtection && tick % 30 == 0) {
                offsetX = (-12..12).random().toFloat()
                offsetY = (-8..8).random().toFloat()
            }
            delay(5_000)
        }
    }

    val accentColor = runCatching {
        Color(AndroidColor.parseColor(settings.aodAccentColorHex))
    }.getOrElse { Color(0xFF00B4D8) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(screenAlpha),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = animX.dp, y = animY.dp)
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (settings.selectedAodStyle) {
                AodDisplayStyle.PixelClean -> AodPixelClean(now, snapshot, accentColor)
                AodDisplayStyle.MinimalNeon -> AodMinimalNeon(now, snapshot, accentColor)
                AodDisplayStyle.RingMeter -> AodRingMeter(now, snapshot, accentColor)
                AodDisplayStyle.CyberPulse -> AodCyberPulse(now, snapshot, accentColor)
                AodDisplayStyle.ClassicBattery -> AodClassicBattery(now, snapshot, accentColor)
                AodDisplayStyle.UltraMinimal -> AodUltraMinimal(now, snapshot, accentColor)
                AodDisplayStyle.SpeedGlow -> AodSpeedGlow(now, snapshot, accentColor)
                AodDisplayStyle.TextOnly -> AodTextOnly(now, snapshot, accentColor)
            }
        }
    }
}

// ── Primary metric helper ──────────────────────────────────────────────────
private fun primaryMetricValue(snapshot: com.powermate.ai.domain.model.BatterySnapshot, metric: String): String = when (metric) {
    "current" -> snapshot.currentMilliAmp?.let { "${it.toInt()} mA" } ?: "-- mA"
    "wattage" -> snapshot.wattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W"
    "voltage" -> snapshot.voltageVolt?.let { String.format(Locale.US, "%.2f V", it) } ?: "-- V"
    "temperature" -> snapshot.temperatureCelsius?.let { String.format(Locale.US, "%.1f°C", it) } ?: "--°C"
    else -> "${snapshot.levelPercent}%"
}

// ── AOD Style Layouts ──────────────────────────────────────────────────────

@Composable
private fun AodPixelClean(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(24.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 56.sp, fontWeight = FontWeight.Bold)
        Text(SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(now)), color = TextSecondary, fontSize = 15.sp)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        BatteryRingColored(level = snapshot.levelPercent, accent = accent, modifier = Modifier.size(200.dp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusChip(snapshot.status.label, accent)
            StatusChip(snapshot.wattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W", Color(0xFF22C55E))
        }
    }
    AodFooter()
}

@Composable
private fun AodMinimalNeon(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(24.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${snapshot.levelPercent}%",
            color = accent,
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold
        )
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.Light)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, accent, Color.Transparent)))
        )
        Spacer(Modifier.height(12.dp))
        Text(snapshot.status.label, color = accent, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        snapshot.wattage?.let { Text(String.format(Locale.US, "%.1f W", it), color = TextSecondary, fontSize = 13.sp) }
    }
    AodFooter()
}

@Composable
private fun AodRingMeter(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(8.dp))
    BatteryRingColored(level = snapshot.levelPercent, accent = accent, modifier = Modifier.size(260.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Light)
        Text(snapshot.status.label, color = accent, fontSize = 14.sp)
        snapshot.wattage?.let { Text(String.format(Locale.US, "%.1f W", it), color = TextSecondary, fontSize = 12.sp) }
    }
    AodFooter()
}

@Composable
private fun AodCyberPulse(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(12.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("POWERMATE AI", color = accent.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
        Spacer(Modifier.height(8.dp))
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 52.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, accent, Color.Transparent)))
        )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${snapshot.levelPercent}%", color = accent, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Text(snapshot.status.label.uppercase(Locale.US), color = accent.copy(alpha = 0.7f), fontSize = 12.sp, letterSpacing = 2.sp)
    }
    AodFooter()
}

@Composable
private fun AodClassicBattery(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(24.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 48.sp, fontWeight = FontWeight.Bold)
        Text(SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(now)), color = TextSecondary, fontSize = 14.sp)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🔋 ${snapshot.levelPercent}%", color = accent, fontSize = 52.sp, fontWeight = FontWeight.Bold)
        Text(snapshot.status.label, color = TextSecondary, fontSize = 16.sp)
        snapshot.wattage?.let {
            Spacer(Modifier.height(8.dp))
            Text(String.format(Locale.US, "%.1f W", it), color = accent, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
    }
    AodFooter()
}

@Composable
private fun AodUltraMinimal(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(40.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 64.sp, fontWeight = FontWeight.Thin)
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${snapshot.levelPercent}%", color = accent, fontSize = 24.sp, fontWeight = FontWeight.Light)
        Text(snapshot.status.label, color = TextSecondary.copy(alpha = 0.6f), fontSize = 11.sp)
    }
    Spacer(Modifier.height(40.dp))
}

@Composable
private fun AodSpeedGlow(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(24.dp))
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CHARGING SPEED", color = accent.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Spacer(Modifier.height(6.dp))
        Text(
            snapshot.wattage?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W",
            color = accent, fontSize = 64.sp, fontWeight = FontWeight.Bold
        )
        Text(
            snapshot.currentMilliAmp?.let { "${it.toInt()} mA" } ?: "-- mA",
            color = TextSecondary, fontSize = 20.sp
        )
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Light)
        Text("${snapshot.levelPercent}%  •  ${snapshot.status.label}", color = TextSecondary, fontSize = 13.sp)
    }
    AodFooter()
}

@Composable
private fun AodTextOnly(now: Long, snapshot: com.powermate.ai.domain.model.BatterySnapshot, accent: Color) {
    Spacer(Modifier.height(24.dp))
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(SimpleDateFormat("HH:mm", Locale.US).format(Date(now)), color = TextMain, fontSize = 44.sp, fontWeight = FontWeight.Bold)
        Text(SimpleDateFormat("EEE, MMM d", Locale.US).format(Date(now)), color = TextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        TextOnlyRow("Battery", "${snapshot.levelPercent}%", accent)
        TextOnlyRow("Status", snapshot.status.label, TextSecondary)
        snapshot.wattage?.let { TextOnlyRow("Wattage", String.format(Locale.US, "%.1f W", it), accent) }
        snapshot.currentMilliAmp?.let { TextOnlyRow("Current", "${it.toInt()} mA", TextSecondary) }
        snapshot.temperatureCelsius?.let { TextOnlyRow("Temp", String.format(Locale.US, "%.1f°C", it), TextSecondary) }
    }
    AodFooter()
}

@Composable
private fun TextOnlyRow(label: String, value: String, valueColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AodFooter() {
    Column(
        modifier = Modifier
            .background(Color(0xFF08111F), RoundedCornerShape(24.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AMOLED-safe charging display", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text("Tap back / home to close", color = TextSecondary, fontSize = 11.sp)
    }
}
