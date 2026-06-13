package com.powermate.ai.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.aod.AodDisplayActivity
import com.powermate.ai.domain.competitive.FeatureAvailability
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.OptimizationActionType
import com.powermate.ai.domain.model.OptimizationImpact
import com.powermate.ai.domain.model.OptimizationSuggestion
import com.powermate.ai.ui.components.BatteryRing
import com.powermate.ai.ui.components.MetricCard
import com.powermate.ai.ui.components.MiniGraph
import com.powermate.ai.ui.components.PrimaryAction
import com.powermate.ai.ui.components.ScoreGauge
import com.powermate.ai.ui.components.SectionCard
import com.powermate.ai.ui.components.SettingToggle
import com.powermate.ai.ui.components.StatusChip
import com.powermate.ai.ui.theme.AmoledBlack
import com.powermate.ai.ui.theme.Cyan
import com.powermate.ai.ui.theme.DangerRed
import com.powermate.ai.ui.theme.PrimaryBlue
import com.powermate.ai.ui.theme.SoftPrimary
import com.powermate.ai.ui.theme.SuccessGreen
import com.powermate.ai.ui.theme.TextMain
import com.powermate.ai.ui.theme.TextSecondary
import com.powermate.ai.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Tab(val label: String) {
    Home("Home"),
    Live("Live"),
    Aod("AOD"),
    History("History"),
    Settings("Settings")
}

@Composable
fun PowerMateRoot(controller: PowerMateViewModel) {
    var selectedTab by remember { mutableStateOf(Tab.Home) }

    LaunchedEffect(Unit) {
        while (true) {
            controller.refresh()
            delay(1_000)
        }
    }

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF080F18), tonalElevation = 0.dp) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.label.take(1), fontWeight = FontWeight.Bold) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            Tab.Home -> HomeScreen(controller, padding)
            Tab.Live -> LiveChargingScreen(controller, padding)
            Tab.Aod -> AodCustomizationScreen(controller, padding)
            Tab.History -> ChargingHistoryScreen(controller, padding)
            Tab.Settings -> SettingsScreen(controller, padding)
        }
    }
}

@Composable
private fun ScreenShell(
    title: String,
    subtitle: String,
    padding: PaddingValues,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = TextSecondary, fontSize = 13.sp)
                }
                StatusChip("Offline", Cyan)
            }
        }

        content()
    }
}

@Composable
private fun HomeScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val snap = controller.snapshot
    val context = LocalContext.current

    ScreenShell("PowerMate AI", "Private battery command center", padding) {
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BatteryRing(level = snap.levelPercent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    StatusChip(snap.status.label, statusColor(snap.status))
                }

                Spacer(Modifier.height(14.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PrimaryAction(
                        "Start Charger Test",
                        controller::startDiagnostic,
                        Modifier.weight(1f)
                    )
                    PrimaryAction(
                        "Open AOD",
                        { context.startActivity(Intent(context, AodDisplayActivity::class.java)) },
                        Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Current",
                    snap.currentMilliAmp?.format0("mA") ?: "--",
                    if (snap.isSensorReliable) "Live reading" else "Unsupported",
                    Modifier.weight(1f)
                )
                MetricCard(
                    "Power",
                    snap.wattage?.format1("W") ?: "--",
                    "Estimated",
                    Modifier.weight(1f),
                    SuccessGreen
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Voltage",
                    snap.voltageVolt?.format2("V") ?: "--",
                    snap.pluggedType.label,
                    Modifier.weight(1f),
                    SoftPrimary
                )
                MetricCard(
                    "Temp",
                    snap.temperatureCelsius?.format1("°C") ?: "--",
                    snap.health.label,
                    Modifier.weight(1f),
                    WarningAmber
                )
            }
        }

        item { BatteryHealthInsightCard(controller) }
        item { ChargingCoachCard(controller) }
        item { CompetitiveAdvantageCard(controller) }
    }
}

@Composable
private fun BatteryHealthInsightCard(controller: PowerMateViewModel) {
    val insights = controller.insights

    SectionCard {
        Text(insights.headline, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                "Charge health",
                "${insights.chargingHealthScore}/100",
                insights.thermalRiskLabel,
                Modifier.weight(1f),
                SuccessGreen
            )
            MetricCard(
                "Battery care",
                "${insights.batteryCareScore}/100",
                insights.wearLevelLabel,
                Modifier.weight(1f),
                Cyan
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                "Capacity",
                insights.estimatedCapacityMah?.format0("mAh") ?: "--",
                insights.capacityConfidence,
                Modifier.weight(1f),
                SoftPrimary
            )
            MetricCard(
                "Time",
                controller.snapshot.timeToFullMinutes?.let { formatMinutes(it) }
                    ?: controller.snapshot.timeToEmptyMinutes?.let { formatMinutes(it) }
                    ?: "--",
                "Estimate",
                Modifier.weight(1f),
                WarningAmber
            )
        }

        Spacer(Modifier.height(10.dp))

        insights.details.take(3).forEach {
            Text("• $it", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CompetitiveAdvantageCard(controller: PowerMateViewModel) {
    val included = controller.competitiveFeatures.count {
        it.powerMateStatus == FeatureAvailability.Included
    }
    val planned = controller.competitiveFeatures.count {
        it.powerMateStatus == FeatureAvailability.Planned
    }

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Competitor coverage", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(
                    "AmpereFlow + AccuBattery + Ampere + Battery Guru + Charge Meter targets",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            StatusChip("$included free", SuccessGreen)
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "$included included feature targets • $planned planned advanced polish items • no account • local-first",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun LiveChargingScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val snap = controller.snapshot

    ScreenShell("Live Monitor", "Real-time current, wattage, capacity and stability", padding) {
        item {
            SectionCard {
                Text(
                    snap.currentMilliAmp?.format0("mA") ?: "-- mA",
                    color = Cyan,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(snap.status.label, color = TextSecondary, fontSize = 14.sp)

                Spacer(Modifier.height(18.dp))

                MiniGraph(values = listOf(20f, 45f, 52f, 48f, 65f, 70f, 62f, 80f, 76f, 84f))
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Wattage",
                    snap.wattage?.format1("W") ?: "--",
                    "Estimated",
                    Modifier.weight(1f),
                    SuccessGreen
                )
                MetricCard(
                    "Stability",
                    if (controller.isDiagnosticRunning) "Measuring" else "Ready",
                    "60-sec test",
                    Modifier.weight(1f),
                    Cyan
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Peak",
                    snap.currentMilliAmp?.format0("mA") ?: "--",
                    "Current",
                    Modifier.weight(1f),
                    SoftPrimary
                )
                MetricCard(
                    "Session",
                    "${controller.diagnosticSeconds}s",
                    if (controller.isDiagnosticRunning) "Running" else "Idle",
                    Modifier.weight(1f),
                    WarningAmber
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Charge counter",
                    snap.chargeCounterMah?.format0("mAh") ?: "--",
                    "Fuel gauge",
                    Modifier.weight(1f),
                    SoftPrimary
                )
                MetricCard(
                    "Discharge",
                    controller.insights.dischargeRateMa?.format0("mA") ?: "--",
                    "When unplugged",
                    Modifier.weight(1f),
                    WarningAmber
                )
            }
        }

        item {
            val buttonText = if (controller.isDiagnosticRunning) "Finish Test" else "Start 60-sec Diagnostic"
            PrimaryAction(
                buttonText,
                {
                    if (controller.isDiagnosticRunning) {
                        controller.completeDiagnostic()
                    } else {
                        controller.startDiagnostic()
                    }
                },
                Modifier.fillMaxWidth()
            )
        }

        item { ChargingCoachCard(controller) }

        controller.latestDiagnostic?.let { result ->
            item {
                SectionCard {
                    Text("Latest diagnostic", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScoreGauge("Charger", result.chargerScore, Modifier.weight(1f))
                        ScoreGauge("Cable", result.cableScore, Modifier.weight(1f))
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(result.recommendation, color = TextSecondary, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun ChargingCoachCard(controller: PowerMateViewModel) {
    val suggestions = controller.chargingSuggestions.take(6)

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Smart Charging Coach", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Actionable tips + safe Android shortcuts", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("Free", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        if (suggestions.isEmpty()) {
            Text(
                "Connect a charger to get personalized charging-speed suggestions.",
                color = TextSecondary,
                fontSize = 14.sp
            )
        } else {
            suggestions.forEachIndexed { index, suggestion ->
                if (index > 0) Spacer(Modifier.height(12.dp))
                SuggestionRow(suggestion, onRunDiagnostic = controller::startDiagnostic)
            }
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: OptimizationSuggestion,
    onRunDiagnostic: () -> Unit
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    suggestion.title,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(suggestion.impact.label, impactColor(suggestion.impact))
            }

            Spacer(Modifier.height(3.dp))

            Text(suggestion.reason, color = TextSecondary, fontSize = 12.sp)
        }

        if (suggestion.actionType != OptimizationActionType.None || suggestion.actionLabel == "Run test") {
            Spacer(Modifier.width(8.dp))

            TextButton(
                onClick = {
                    if (suggestion.actionLabel == "Run test") {
                        onRunDiagnostic()
                    } else {
                        openOptimizationShortcut(context, suggestion.actionType)
                    }
                }
            ) {
                Text(
                    suggestion.actionLabel,
                    color = Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun AodCustomizationScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val context = LocalContext.current

    ScreenShell("AOD Display", "All charging display styles included", padding) {
        item {
            SectionCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .background(Color.Black, RoundedCornerShape(28.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("22:45", color = TextMain, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text("Charging • ${controller.snapshot.levelPercent}%", color = Cyan, fontSize = 16.sp)

                    Spacer(Modifier.height(24.dp))

                    BatteryRing(
                        level = controller.snapshot.levelPercent,
                        modifier = Modifier.size(160.dp)
                    )

                    Spacer(Modifier.height(20.dp))

                    Text(
                        controller.snapshot.wattage?.format1("W") ?: "Power unavailable",
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            PrimaryAction(
                "Launch AOD-style display",
                { context.startActivity(Intent(context, AodDisplayActivity::class.java)) },
                Modifier.fillMaxWidth()
            )
        }

        item {
            SectionCard {
                Text("Included styles", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pixel Clean • Minimal Neon • Ring Meter • Cyber Pulse • Classic Clock • Ultra Minimal • Speed Glow • Text Only",
                    color = TextSecondary
                )
            }
        }

        item {
            SectionCard {
                Text("AOD controls", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                SettingToggle(
                    "Burn-in protection",
                    "Automatically moves AOD position",
                    controller.settings.burnInProtection
                ) { checked ->
                    controller.updateSettings { it.copy(burnInProtection = checked) }
                }

                SettingToggle(
                    "Auto position shift",
                    "Move clock and ring slowly",
                    controller.settings.autoPositionShift
                ) { checked ->
                    controller.updateSettings { it.copy(autoPositionShift = checked) }
                }

                SettingToggle(
                    "Night dim mode",
                    "Lower brightness at night",
                    controller.settings.nightDimMode
                ) { checked ->
                    controller.updateSettings { it.copy(nightDimMode = checked) }
                }

                SettingToggle(
                    "Show wattage",
                    "Show W instead of mA on AOD",
                    controller.settings.showWattageInsteadOfAmpere
                ) { checked ->
                    controller.updateSettings { it.copy(showWattageInsteadOfAmpere = checked) }
                }

                SettingToggle(
                    "Media controls",
                    "AOD quick media area",
                    controller.settings.showAodMediaControls
                ) { checked ->
                    controller.updateSettings { it.copy(showAodMediaControls = checked) }
                }

                SettingToggle(
                    "Camera shortcut",
                    "Open camera from AOD",
                    controller.settings.showAodCameraShortcut
                ) { checked ->
                    controller.updateSettings { it.copy(showAodCameraShortcut = checked) }
                }
            }
        }
    }
}

@Composable
private fun ChargingHistoryScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    ScreenShell("Charging History", "Sessions, charger score and weekly insights", padding) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Best charger",
                    controller.sessions.maxOfOrNull { it.chargerScore ?: 0 }?.let { "$it/100" } ?: "--",
                    "Saved tests",
                    Modifier.weight(1f),
                    SuccessGreen
                )
                MetricCard(
                    "Sessions",
                    controller.sessions.size.toString(),
                    "Local only",
                    Modifier.weight(1f),
                    Cyan
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                MetricCard(
                    "Care score",
                    "${controller.insights.batteryCareScore}/100",
                    controller.insights.wearLevelLabel,
                    Modifier.weight(1f),
                    SoftPrimary
                )
                MetricCard(
                    "Thermal",
                    controller.insights.thermalRiskLabel,
                    controller.snapshot.temperatureCelsius?.format1("°C") ?: "Unknown",
                    Modifier.weight(1f),
                    WarningAmber
                )
            }
        }

        controller.insights.slowestChargerWarning?.let { warning ->
            item {
                SectionCard {
                    Text(warning, color = WarningAmber, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (controller.sessions.isEmpty()) {
            item {
                SectionCard {
                    Text("No charging history yet", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Run a charger diagnostic to create your first local session. Your data stays on device.",
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(controller.sessions) { session ->
                SessionRow(session)
            }
        }
    }
}

@Composable
private fun SessionRow(session: ChargingSession) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(session.userLabel ?: "Charging session", color = TextMain, fontWeight = FontWeight.Bold)
                Text(formatTime(session.startTime), color = TextSecondary, fontSize = 12.sp)
            }

            StatusChip("${session.chargerScore ?: 0}/100", SuccessGreen)
        }

        Spacer(Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Avg ${session.averageWattage?.format1("W") ?: "--"}",
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Peak ${session.peakCurrentMa?.format0("mA") ?: "--"}",
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                "Stable ${session.stabilityScore ?: 0}%",
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SettingsScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    ScreenShell("Settings", "No login, no cloud, all tools included", padding) {
        item {
            SectionCard {
                Text("Appearance", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                SettingToggle(
                    "AMOLED mode",
                    "True black surfaces for OLED screens",
                    controller.settings.amoledMode
                ) { checked ->
                    controller.updateSettings { it.copy(amoledMode = checked) }
                }

                SettingToggle(
                    "Show wattage first",
                    "Prioritize W over mA on compact displays",
                    controller.settings.showWattageInsteadOfAmpere
                ) { checked ->
                    controller.updateSettings { it.copy(showWattageInsteadOfAmpere = checked) }
                }

                Text(
                    "Speedometer: ${controller.settings.selectedSpeedometerStyle.label}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        item {
            SectionCard {
                Text("Charging alerts", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                SettingToggle(
                    "Notify at 80%",
                    "Battery care reminder",
                    controller.settings.alertAt80
                ) { checked ->
                    controller.updateSettings { it.copy(alertAt80 = checked) }
                }

                SettingToggle(
                    "Notify at 90%",
                    "Optional high limit reminder",
                    controller.settings.alertAt90
                ) { checked ->
                    controller.updateSettings { it.copy(alertAt90 = checked) }
                }

                SettingToggle(
                    "Full charge alert",
                    "Tell me when battery reaches 100%",
                    controller.settings.alertWhenFull
                ) { checked ->
                    controller.updateSettings { it.copy(alertWhenFull = checked) }
                }

                SettingToggle(
                    "Overheat alert",
                    "Warn if temperature becomes unsafe",
                    controller.settings.overheatAlert
                ) { checked ->
                    controller.updateSettings { it.copy(overheatAlert = checked) }
                }

                SettingToggle(
                    "Unstable charger alert",
                    "Warn when power fluctuates too much",
                    controller.settings.unstableChargingAlert
                ) { checked ->
                    controller.updateSettings { it.copy(unstableChargingAlert = checked) }
                }

                Text(
                    "Notification format: ${controller.settings.notificationFormat.label}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        item { FeatureMatrixCard(controller) }

        item {
            SectionCard {
                Text("Privacy", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "PowerMate AI is offline-first. No account is required. Charging history is stored locally and can be cleared anytime.",
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = controller::clearHistory) {
                    Text("Clear local history", color = DangerRed)
                }
            }
        }

        item {
            SectionCard {
                Text("About", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("PowerMate AI v1.1.0 Competitive Pro", color = TextSecondary)
                Text(
                    "Live charging monitor • AOD-style display • Charger test • Capacity insights • Widgets",
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FeatureMatrixCard(controller: PowerMateViewModel) {
    SectionCard {
        Text("Competitive feature matrix", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        controller.competitiveFeatures.take(10).forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(feature.name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(feature.powerMateAdvantage, color = TextSecondary, fontSize = 11.sp)
                }

                StatusChip(
                    feature.powerMateStatus.label,
                    if (feature.powerMateStatus == FeatureAvailability.Included) {
                        SuccessGreen
                    } else {
                        WarningAmber
                    }
                )
            }
        }
    }
}

private fun Float.format0(unit: String): String = "${toInt()} $unit"

private fun Float.format1(unit: String): String =
    "${String.format(Locale.US, "%.1f", this)} $unit"

private fun Float.format2(unit: String): String =
    "${String.format(Locale.US, "%.2f", this)} $unit"

private fun formatTime(time: Long): String =
    SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date(time))

private fun formatMinutes(minutes: Int): String =
    if (minutes < 60) {
        "$minutes min"
    } else {
        "${minutes / 60}h ${minutes % 60}m"
    }

private fun impactColor(impact: OptimizationImpact): Color =
    when (impact) {
        OptimizationImpact.High -> WarningAmber
        OptimizationImpact.Medium -> Cyan
        OptimizationImpact.Low -> SoftPrimary
        OptimizationImpact.Info -> TextSecondary
    }

private fun statusColor(status: ChargingStatus): Color =
    when (status) {
        ChargingStatus.FastCharging,
        ChargingStatus.VeryFastCharging -> SuccessGreen

        ChargingStatus.Charging -> Cyan
        ChargingStatus.SlowCharging,
        ChargingStatus.UnstableCharging -> WarningAmber

        ChargingStatus.NotCharging -> TextSecondary
        ChargingStatus.Full -> PrimaryBlue
        ChargingStatus.Unknown -> TextSecondary
    }
