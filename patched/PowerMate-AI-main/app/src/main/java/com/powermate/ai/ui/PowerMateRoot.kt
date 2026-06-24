package com.powermate.ai.ui

import android.content.Intent
import com.powermate.ai.R
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powermate.ai.aod.AodDisplayActivity
import com.powermate.ai.domain.competitive.FeatureAvailability
import com.powermate.ai.domain.model.AppUsageEntry
import com.powermate.ai.domain.model.ChargingSession
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.DiagnosticResult
import com.powermate.ai.domain.model.OptimizationActionType
import com.powermate.ai.domain.model.OptimizationImpact
import com.powermate.ai.domain.model.OptimizationSuggestion
import com.powermate.ai.ui.components.BatteryRing
import com.powermate.ai.ui.components.MetricCard
import com.powermate.ai.ui.components.MiniGraph
import com.powermate.ai.ui.components.PrimaryAction
import com.powermate.ai.ui.components.SectionCard
import com.powermate.ai.ui.components.SettingToggle
import com.powermate.ai.ui.components.StatusChip
import com.powermate.ai.ui.theme.AmoledBlack
import com.powermate.ai.ui.theme.CardElevated
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

private enum class Tab(val label: String, val iconRes: Int) {
    Home("Home", R.drawable.ic_pm_home),
    Live("Live", R.drawable.ic_pm_live),
    Looks("Looks", R.drawable.ic_pm_looks),
    History("History", R.drawable.ic_pm_history),
    Usage("Usage", R.drawable.ic_pm_usage),
    Settings("Settings", R.drawable.ic_pm_settings)
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
                        icon = { NavGlyph(tab = tab, selected = selectedTab == tab) },
                        label = { Text(tab.label, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            Tab.Home -> HomeScreen(controller, padding)
            Tab.Live -> LiveChargingScreen(controller, padding)
            Tab.Looks -> LooksScreen(controller, padding)
            Tab.History -> ChargingHistoryScreen(controller, padding)
            Tab.Usage -> AppUsageAndWidgetsScreen(controller, padding)
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

        item { ChargingIntelligenceDashboard(controller) }

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
private fun ChargingIntelligenceDashboard(controller: PowerMateViewModel) {
    val snapshot = controller.snapshot
    val latest = controller.latestDiagnostic
    val best = controller.sessions.maxByOrNull { it.chargerScore ?: -1 }
    val chargerScore = latest?.chargerScore ?: best?.chargerScore ?: controller.insights.chargingHealthScore
    val cableScore = latest?.cableScore ?: best?.cableScore ?: estimateCableScore(snapshot)
    val stabilityScore = latest?.stabilityScore ?: best?.stabilityScore ?: estimateStabilityScore(snapshot)
    val peakCurrent = latest?.peakCurrentMa ?: best?.peakCurrentMa ?: snapshot.currentMilliAmp
    val averageCurrent = latest?.averageCurrentMa ?: best?.averageCurrentMa ?: snapshot.averageCurrentMilliAmp ?: snapshot.currentMilliAmp
    val peakWattage = latest?.peakWattage ?: best?.peakWattage ?: snapshot.wattage
    val averageWattage = latest?.averageWattage ?: best?.averageWattage ?: snapshot.wattage
    val recommendation = latest?.recommendation ?: buildLiveRecommendation(snapshot, chargerScore, cableScore, stabilityScore)

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Charging Intelligence", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Charger, cable, speed and thermal quality — estimated locally", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("Free", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DiagnosticMetric("Charger", "$chargerScore/100", scoreColor(chargerScore), Modifier.weight(1f))
            DiagnosticMetric("Cable", "$cableScore/100", scoreColor(cableScore), Modifier.weight(1f))
            DiagnosticMetric("Stability", "$stabilityScore/100", scoreColor(stabilityScore), Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DiagnosticMetric("Peak current", peakCurrent?.format0("mA") ?: "--", Cyan, Modifier.weight(1f))
            DiagnosticMetric("Avg current", averageCurrent?.format0("mA") ?: "--", SoftPrimary, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DiagnosticMetric("Peak watts", peakWattage?.format1("W") ?: "--", SuccessGreen, Modifier.weight(1f))
            DiagnosticMetric("Avg watts", averageWattage?.format1("W") ?: "--", WarningAmber, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DiagnosticMetric("Temperature", snapshot.temperatureCelsius?.format1("°C") ?: "--", riskColor(controller.insights.thermalRiskLabel), Modifier.weight(1f))
            DiagnosticMetric("Temp trend", temperatureTrendLabel(snapshot.temperatureCelsius, controller.sessions), riskColor(controller.insights.thermalRiskLabel), Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))
        Text("Recommendation", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(recommendation, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun BatteryHealthInsightCard(controller: PowerMateViewModel) {
    val insights = controller.insights
    val snapshot = controller.snapshot
    val runtimeEstimate = snapshot.timeToFullMinutes?.let { "Full in ${formatMinutes(it)}" }
        ?: snapshot.timeToEmptyMinutes?.let { "Runtime ${formatMinutes(it)}" }
        ?: "Needs supported sensor"

    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(insights.headline, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Battery health, heat and runtime signals", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip(insights.thermalRiskLabel, riskColor(insights.thermalRiskLabel))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                "Care score",
                "${insights.batteryCareScore}/100",
                insights.wearLevelLabel,
                Modifier.weight(1f),
                Cyan
            )
            MetricCard(
                "Temp risk",
                insights.thermalRiskLabel,
                snapshot.temperatureCelsius?.format1("°C") ?: "Unknown",
                Modifier.weight(1f),
                riskColor(insights.thermalRiskLabel)
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
                "Runtime",
                runtimeEstimate,
                "Live estimate",
                Modifier.weight(1f),
                WarningAmber
            )
        }

        Spacer(Modifier.height(14.dp))
        Text("Safe charging tips", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))

        val careTips = listOf(
            "Keep daily charging near 20–85% when possible.",
            "Avoid thick cases or gaming while the phone is hot.",
            "Use the same phone when comparing chargers for fair results."
        )
        careTips.forEach { tip ->
            Text("• $tip", color = TextSecondary, fontSize = 12.sp)
        }

        insights.details.take(2).forEach { detail ->
            Text("• $detail", color = TextSecondary, fontSize = 12.sp)
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

        item { RealtimeBatteryLabCard(controller) }

        item { ChargingIntelligenceDashboard(controller) }

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
            item { DiagnosticResultCard(result) }
        }
    }
}

@Composable
private fun RealtimeBatteryLabCard(controller: PowerMateViewModel) {
    val snap = controller.snapshot
    val currentSource = when {
        snap.currentMilliAmp != null -> "Measured now"
        snap.averageCurrentMilliAmp != null -> "Average sensor"
        else -> "Unavailable"
    }
    val wattSource = when {
        snap.wattage != null && snap.currentMilliAmp != null -> "Measured estimate"
        snap.wattage != null -> "Average estimate"
        else -> "Unavailable"
    }

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Real-time charging lab", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Refreshes every second from Android battery sensors", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip(if (snap.isSensorReliable) "Live" else "Limited", if (snap.isSensorReliable) SuccessGreen else WarningAmber)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SensorReadoutCard("Current", snap.currentMilliAmp?.format0("mA") ?: snap.averageCurrentMilliAmp?.format0("mA") ?: "--", currentSource, Cyan, Modifier.weight(1f))
            SensorReadoutCard("Wattage", snap.wattage?.format1("W") ?: "--", wattSource, SuccessGreen, Modifier.weight(1f))
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SensorReadoutCard("Voltage", snap.voltageVolt?.format2("V") ?: "--", snap.pluggedType.label, SoftPrimary, Modifier.weight(1f))
            SensorReadoutCard("Thermal", snap.temperatureCelsius?.format1("°C") ?: "--", temperatureTrendLabel(snap.temperatureCelsius, controller.sessions), riskColor(controller.insights.thermalRiskLabel), Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Text(
            sensorTruthNote(snap.isSensorReliable),
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun SensorReadoutCard(
    label: String,
    value: String,
    source: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 88.dp)
            .background(CardElevated.copy(alpha = 0.62f), RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = accent, fontSize = 21.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(source, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
                Text("Responsive tips + safe Android settings shortcuts", color = TextSecondary, fontSize = 12.sp)
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
    val hasAction = suggestion.actionType != OptimizationActionType.None || suggestion.actionLabel == "Run test"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardElevated.copy(alpha = 0.55f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Text(
            suggestion.title,
            color = TextMain,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(
                suggestion.impact.label,
                impactColor(suggestion.impact),
                modifier = Modifier.widthIn(min = 74.dp, max = 132.dp)
            )

            if (hasAction) {
                Spacer(Modifier.weight(1f))
                TextButton(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 0.dp)
                        .heightIn(min = 36.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
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
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            suggestion.reason,
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun LooksScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val context = LocalContext.current

    ScreenShell("Looks", "AOD styles, themes and widgets without Pro locks", padding) {
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

        item { LooksThemeGallery() }

        item {
            AodStylePreviewSection(controller)
        }

        item { WidgetLookPreviewSection(controller) }

        item {
            SectionCard {
                Text("Display safety", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Burn-in protection gently shifts the clock and ring so OLED pixels are not stressed in one fixed place.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Night dim lowers the AOD look at night for comfort and battery care while staying fully local.",
                    color = TextSecondary,
                    fontSize = 13.sp
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
private fun LooksThemeGallery() {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Looks studio", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Premium visual presets without heavy dependencies", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("Free", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            LookPresetCard("Power Blue", "Clean default", PrimaryBlue, Cyan, Modifier.weight(1f))
            LookPresetCard("Neon Pulse", "Charging glow", SuccessGreen, Cyan, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            LookPresetCard("AMOLED Black", "Deep OLED", Color.Black, SoftPrimary, Modifier.weight(1f))
            LookPresetCard("Clean Light", "Readable cards", Color(0xFFF5F8FF), PrimaryBlue, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LookPresetCard(
    title: String,
    subtitle: String,
    base: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 118.dp)
            .background(CardElevated.copy(alpha = 0.72f), RoundedCornerShape(22.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(base, accent.copy(alpha = 0.72f))))
        )
        Column {
            Text(title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun WidgetLookPreviewSection(controller: PowerMateViewModel) {
    SectionCard {
        Text("Widget looks", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Preview battery, charging speed and care widgets before adding them from the Android home screen.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            WidgetPreviewCard("Compact", "2×1", "${controller.snapshot.levelPercent}%", controller.snapshot.status.label, Cyan, Modifier.weight(1f))
            WidgetPreviewCard("Lab", "2×1", controller.snapshot.wattage?.format1("W") ?: "-- W", controller.snapshot.currentMilliAmp?.format0("mA") ?: "-- mA", SuccessGreen, Modifier.weight(1f))
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

        item { BestChargerSummaryCard(controller) }

        item {
            SectionCard {
                Text("Local-only history", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Charging sessions, charger scores and diagnostic notes stay on this device. No login, Firebase or cloud sync is used.",
                    color = TextSecondary,
                    fontSize = 13.sp
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
            item {
                SectionCard {
                    Text("Session timeline", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("Recent plug-in and charger diagnostics with local-only scores.", color = TextSecondary, fontSize = 13.sp)
                }
            }
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.userLabel ?: "Charging session",
                    color = TextMain,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(formatTime(session.startTime), color = TextSecondary, fontSize = 12.sp)
                Text(sessionSummaryLine(session), color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.width(10.dp))
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
private fun AppUsageAndWidgetsScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val context = LocalContext.current
    val entries = controller.appUsageEntries
    val total24h = entries.sumOf { it.last24hTimeMs }
    val totalToday = entries.sumOf { it.todayTimeMs }
    val topApp = entries.maxByOrNull { it.last24hTimeMs }

    ScreenShell("Usage", "Real app icons, screen-time clues and battery hints", padding) {
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App usage intelligence", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Uses Android Usage Access + installed app logos locally", color = TextSecondary, fontSize = 12.sp)
                    }
                    StatusChip(
                        if (controller.hasUsageStatsAccess) "Live local" else "Permission",
                        if (controller.hasUsageStatsAccess) SuccessGreen else WarningAmber
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (!controller.hasUsageStatsAccess) {
                    Text(
                        "Grant Usage Access to show real installed app names, logos, last-used time and foreground usage. Nothing is uploaded.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    PrimaryAction(
                        "Open Usage Access Settings",
                        { openOptimizationShortcut(context, OptimizationActionType.UsageAccessSettings) },
                        Modifier.fillMaxWidth()
                    )
                } else if (entries.isEmpty()) {
                    Text(
                        "Permission is enabled, but Android has not reported enough app activity yet. Open a few apps, then refresh.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = controller::refreshAppUsageNow) {
                        Text("Refresh usage", color = Cyan, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DiagnosticMetric("Today", formatDurationMs(totalToday), Cyan, Modifier.weight(1f))
                        DiagnosticMetric("Last 24h", formatDurationMs(total24h), SuccessGreen, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        DiagnosticMetric("Apps", entries.size.toString(), SoftPrimary, Modifier.weight(1f))
                        DiagnosticMetric("Top app", topApp?.appName ?: "--", WarningAmber, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Refreshes automatically while open. Usage stats can be delayed by Android on some devices.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = controller::refreshAppUsageNow) {
                            Text("Refresh", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (entries.isNotEmpty()) {
            item {
                SectionCard {
                    Text("Top foreground apps", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Sorted by last 24h foreground time. Icons come from installed apps, not generated placeholders.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    entries.forEachIndexed { index, entry ->
                        if (index > 0) Spacer(Modifier.height(10.dp))
                        AppUsageRow(entry)
                    }
                }
            }

            item {
                SectionCard {
                    Text("Usage coach", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    val heaviest = topApp
                    val message = when {
                        heaviest == null -> "Usage data is still warming up."
                        heaviest.last24hTimeMs >= 3 * 60L * 60L * 1000L -> "${heaviest.appName} is your heaviest foreground app. Long screen-on time usually affects battery more than background services."
                        heaviest.categoryLabel == "Game" -> "Games can raise temperature and reduce charging speed. Avoid gaming during charger tests."
                        else -> "Your tracked app usage looks moderate. Use charging tests with the screen off for cleaner charger results."
                    }
                    Text(message, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
        }

        item { WidgetCatalogSection(controller) }

        item {
            SectionCard {
                Text("Local-only privacy", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(
                    "App usage, charging history, widget data and AOD preferences stay on this device. No Firebase, login, analytics SDK or cloud backend is used.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun AppUsageRow(entry: AppUsageEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardElevated.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconAvatar(entry)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.appName, color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${entry.categoryLabel} • ${entry.drainHint}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                UsageProgressBar(entry.percentOfTrackedUsage / 100f)
            }
            Spacer(Modifier.width(10.dp))
            StatusChip("${String.format(Locale.US, "%.1f", entry.percentOfTrackedUsage)}%", SoftPrimary)
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            UsageMiniStat("24h", formatDurationMs(entry.last24hTimeMs), Modifier.weight(1f))
            UsageMiniStat("Today", formatDurationMs(entry.todayTimeMs), Modifier.weight(1f))
            UsageMiniStat("Last", formatRelativeTime(entry.lastTimeUsed), Modifier.weight(1f))
        }
    }
}

@Composable
private fun AppIconAvatar(entry: AppUsageEntry) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PrimaryBlue.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = entry.iconBitmap
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = entry.appName,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(PrimaryBlue, Cyan))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.appName.firstOrNull()?.uppercaseChar()?.toString() ?: "•",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun UsageProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(SoftPrimary.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0.04f, 1f))
                .height(5.dp)
                .background(Brush.linearGradient(listOf(PrimaryBlue, Cyan)))
        )
    }
}

@Composable
private fun UsageMiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AmoledBlack.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = TextMain, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WidgetCatalogSection(controller: PowerMateViewModel) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Free widgets", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Home-screen tools included with no Pro locks", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("4 widgets", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            WidgetPreviewCard("Battery Quick", "2×1", "${controller.snapshot.levelPercent}%", controller.snapshot.status.label, Cyan, Modifier.weight(1f))
            WidgetPreviewCard("Charging Speed", "2×1", controller.snapshot.wattage?.format1("W") ?: "-- W", controller.snapshot.currentMilliAmp?.format0("mA") ?: "-- mA", SuccessGreen, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            WidgetPreviewCard("Battery Care", "2×2", "${controller.insights.batteryCareScore}/100", controller.insights.thermalRiskLabel, WarningAmber, Modifier.weight(1f))
            WidgetPreviewCard("AOD Launch", "1×1", "AOD", "Open display", PrimaryBlue, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Add them from Android home screen: long-press home screen → Widgets → PowerMate AI.",
            color = TextSecondary,
            fontSize = 12.sp,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun WidgetPreviewCard(
    title: String,
    size: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .heightIn(min = 132.dp)
            .background(CardElevated.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(size, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
        }
        Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SettingsScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val context = LocalContext.current

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

        item {
            SectionCard {
                Text("AOD settings", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text("AOD stays device-only and uses lightweight previews, burn-in protection and night dim.", color = TextSecondary, fontSize = 12.sp)

                SettingToggle(
                    "AOD enabled",
                    "Show charging display while plugged in",
                    controller.settings.aodEnabled
                ) { checked ->
                    controller.updateSettings { it.copy(aodEnabled = checked) }
                }

                SettingToggle(
                    "Burn-in protection",
                    "Shift elements to protect OLED screens",
                    controller.settings.burnInProtection
                ) { checked ->
                    controller.updateSettings { it.copy(burnInProtection = checked) }
                }

                SettingToggle(
                    "Night dim",
                    "Dim the charging display at night",
                    controller.settings.nightDimMode
                ) { checked ->
                    controller.updateSettings { it.copy(nightDimMode = checked) }
                }
            }
        }

        item {
            SectionCard {
                Text("Safe Android shortcuts", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    "PowerMate only opens Android settings panels. It never silently toggles Bluetooth, display or battery features.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(8.dp))

                SettingsShortcutRow("Bluetooth", "Open Bluetooth settings") {
                    openOptimizationShortcut(context, OptimizationActionType.BluetoothSettings)
                }
                SettingsShortcutRow("Battery optimization", "Open battery optimization settings") {
                    openOptimizationShortcut(context, OptimizationActionType.BatterySaverSettings)
                }
                SettingsShortcutRow("Display / brightness", "Open display settings") {
                    openOptimizationShortcut(context, OptimizationActionType.DisplaySettings)
                }
                SettingsShortcutRow("Usage access", "Allow local app-usage battery insights") {
                    openOptimizationShortcut(context, OptimizationActionType.UsageAccessSettings)
                }
            }
        }

        item { FeatureMatrixCard(controller) }

        item {
            SectionCard {
                Text("Privacy & local data", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

@Composable
private fun NavGlyph(tab: Tab, selected: Boolean) {
    val background = if (selected) {
        Brush.linearGradient(listOf(PrimaryBlue, Cyan.copy(alpha = 0.85f)))
    } else {
        Brush.linearGradient(listOf(CardElevated, CardElevated.copy(alpha = 0.72f)))
    }
    val iconColor = if (selected) Color.White else TextSecondary

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(background, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = tab.iconRes),
            contentDescription = tab.label,
            tint = iconColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun DiagnosticResultCard(result: DiagnosticResult) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Charging diagnostic", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Charger, cable and current stability result", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("${result.chargerScore}/100", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DiagnosticMetric("Charger score", "${result.chargerScore}/100", SuccessGreen, Modifier.weight(1f))
            DiagnosticMetric("Cable score", "${result.cableScore}/100", Cyan, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DiagnosticMetric("Stability", "${result.stabilityScore}/100", SoftPrimary, Modifier.weight(1f))
            DiagnosticMetric("Peak current", result.peakCurrentMa?.format0("mA") ?: "--", WarningAmber, Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DiagnosticMetric("Avg wattage", result.averageWattage?.format1("W") ?: "--", SuccessGreen, Modifier.weight(1f))
            DiagnosticMetric("Thermal", result.temperatureSafety, riskColor(result.temperatureSafety), Modifier.weight(1f))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DiagnosticMetric("Avg current", result.averageCurrentMa?.format0("mA") ?: "--", Cyan, Modifier.weight(1f))
            DiagnosticMetric("Peak watts", result.peakWattage?.format1("W") ?: "--", SuccessGreen, Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))
        Text("Recommendation", color = TextMain, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(result.recommendation, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
    }
}

@Composable
private fun DiagnosticMetric(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardElevated.copy(alpha = 0.62f), RoundedCornerShape(18.dp))
            .padding(12.dp)
            .heightIn(min = 62.dp)
    ) {
        Text(label, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(4.dp))
        Text(value, color = accent, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AodStylePreviewSection(controller: PowerMateViewModel) {
    SectionCard {
        Text("Style previews", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Minimal, Neon, Ring and Text layouts are lightweight and work without cloud features.", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AodStylePreviewCard("Minimal", "22:45", "Clean clock", Cyan, Modifier.weight(1f))
            AodStylePreviewCard("Neon", "${controller.snapshot.levelPercent}%", "Glow look", PrimaryBlue, Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            AodStylePreviewCard("Ring", "◌", "Battery ring", SuccessGreen, Modifier.weight(1f))
            AodStylePreviewCard("Text", controller.snapshot.wattage?.format1("W") ?: "-- W", "Simple stats", WarningAmber, Modifier.weight(1f))
        }
    }
}

@Composable
private fun AodStylePreviewCard(title: String, sample: String, subtitle: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .heightIn(min = 108.dp)
            .background(Color.Black, RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(sample, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BestChargerSummaryCard(controller: PowerMateViewModel) {
    val best = controller.sessions.maxByOrNull { it.chargerScore ?: -1 }

    SectionCard {
        Text("Best charger summary", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        if (best == null) {
            Text("Run a diagnostic to compare chargers and cables locally.", color = TextSecondary, fontSize = 13.sp)
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(best.userLabel ?: "Top charger", color = TextMain, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Avg ${best.averageWattage?.format1("W") ?: "--"} • Peak ${best.peakCurrentMa?.format0("mA") ?: "--"}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.width(10.dp))
                StatusChip("${best.chargerScore ?: 0}/100", SuccessGreen)
            }
        }
    }
}

@Composable
private fun SettingsShortcutRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        TextButton(
            modifier = Modifier.defaultMinSize(minWidth = 0.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            onClick = onClick
        ) {
            Text("Open", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}


private fun estimateCableScore(snapshot: com.powermate.ai.domain.model.BatterySnapshot): Int {
    if (!snapshot.isSensorReliable) return 58
    val current = snapshot.currentMilliAmp ?: snapshot.averageCurrentMilliAmp ?: return 62
    val wattage = snapshot.wattage ?: 0f
    val base = when {
        wattage >= 20f -> 92
        wattage >= 12f -> 82
        wattage >= 7f -> 72
        wattage >= 4.5f -> 58
        else -> 44
    }
    val currentBonus = when {
        current >= 3000f -> 8
        current >= 2000f -> 4
        current < 700f -> -8
        else -> 0
    }
    return (base + currentBonus).coerceIn(35, 100)
}

private fun estimateStabilityScore(snapshot: com.powermate.ai.domain.model.BatterySnapshot): Int = when {
    !snapshot.isSensorReliable -> 58
    snapshot.status == ChargingStatus.UnstableCharging -> 42
    snapshot.status == ChargingStatus.SlowCharging -> 62
    snapshot.status == ChargingStatus.FastCharging || snapshot.status == ChargingStatus.VeryFastCharging -> 84
    snapshot.isCharging -> 76
    else -> 68
}

private fun scoreColor(score: Int): Color = when {
    score >= 82 -> SuccessGreen
    score >= 64 -> Cyan
    score >= 48 -> WarningAmber
    else -> DangerRed
}

private fun buildLiveRecommendation(
    snapshot: com.powermate.ai.domain.model.BatterySnapshot,
    chargerScore: Int,
    cableScore: Int,
    stabilityScore: Int
): String = when {
    !snapshot.isSensorReliable -> "This phone limits current sensor precision. Compare chargers on the same phone and trust relative scores, not fake hardware claims."
    (snapshot.temperatureCelsius ?: 0f) >= 42f -> "Temperature is high. Remove the case, stop heavy apps and avoid fast charging until it cools."
    chargerScore >= 85 && cableScore >= 80 && stabilityScore >= 80 -> "Charging setup looks strong. Keep daily charging near 20–85% for battery care."
    cableScore < 55 -> "Cable may be limiting current. Try a better cable and run a 60-second diagnostic again."
    chargerScore < 55 -> "Charger output looks weak or unstable. Try a wall adapter with higher reliable output."
    stabilityScore < 55 -> "Power is fluctuating. Check the port, cable connection and avoid moving the phone while testing."
    else -> "Charging looks usable. Run a diagnostic on each charger to build a reliable local comparison history."
}

private fun sensorTruthNote(isReliable: Boolean): String =
    if (isReliable) {
        "Values are read locally from Android battery sensors. Wattage is calculated from current × voltage, so it stays honest instead of pretending to read charger IC data."
    } else {
        "This phone hides precise current sensors. PowerMate marks those values as unavailable/estimated instead of showing fake hardware numbers."
    }

private fun temperatureTrendLabel(currentTemp: Float?, sessions: List<ChargingSession>): String {
    val previousMax = sessions.mapNotNull { it.maxTemperatureC }.maxOrNull()
    return when {
        currentTemp == null -> "Unknown"
        previousMax == null -> "Live only"
        currentTemp > previousMax + 1.5f -> "Warming"
        currentTemp < previousMax - 2f -> "Cooler"
        else -> "Stable"
    }
}

private fun formatRelativeTime(timeMs: Long): String {
    if (timeMs <= 0L) return "--"
    val diff = (System.currentTimeMillis() - timeMs).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = minutes / 60L
    val days = hours / 24L
    return when {
        minutes < 1 -> "Now"
        minutes < 60 -> "${minutes}m"
        hours < 24 -> "${hours}h"
        days < 7 -> "${days}d"
        else -> formatTime(timeMs)
    }
}

private fun formatDurationMs(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

private fun riskColor(label: String): Color = when {
    label.contains("safe", ignoreCase = true) -> SuccessGreen
    label.contains("warm", ignoreCase = true) -> WarningAmber
    label.contains("hot", ignoreCase = true) -> DangerRed
    label.contains("too", ignoreCase = true) -> DangerRed
    else -> TextSecondary
}

private fun Float.format0(unit: String): String = "${toInt()} $unit"

private fun Float.format1(unit: String): String =
    "${String.format(Locale.US, "%.1f", this)} $unit"

private fun Float.format2(unit: String): String =
    "${String.format(Locale.US, "%.2f", this)} $unit"

private fun sessionSummaryLine(session: ChargingSession): String {
    val end = session.endBatteryPercent?.let { " → $it%" } ?: ""
    val duration = session.endTime?.let { formatDurationMs(it - session.startTime) } ?: "running"
    return "${session.startBatteryPercent}%$end • $duration • ${session.pluggedType.label}"
}

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
