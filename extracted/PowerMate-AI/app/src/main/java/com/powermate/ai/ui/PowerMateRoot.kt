package com.powermate.ai.ui

import android.content.Intent
import androidx.core.content.FileProvider
import com.powermate.ai.export.HistoryExportManager
import android.graphics.Color as AndroidColor
import com.powermate.ai.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.powermate.ai.ui.components.BatteryRingColored
import com.powermate.ai.ui.components.CircularScore
import com.powermate.ai.ui.components.GradientBanner
import com.powermate.ai.ui.components.HealthBar
import com.powermate.ai.ui.components.MetricCard
import com.powermate.ai.ui.components.MiniGraph
import com.powermate.ai.ui.components.PrimaryAction
import com.powermate.ai.ui.components.SectionCard
import com.powermate.ai.ui.components.SensorReadoutCard
import com.powermate.ai.ui.components.SettingToggle
import com.powermate.ai.ui.components.StatusChip
import com.powermate.ai.ui.theme.AmoledBlack
import com.powermate.ai.ui.theme.CardDark
import com.powermate.ai.ui.theme.CardElevated
import com.powermate.ai.ui.theme.CardHighlight
import com.powermate.ai.ui.theme.Cyan
import com.powermate.ai.ui.theme.DangerRed
import com.powermate.ai.ui.theme.PrimaryBlue
import com.powermate.ai.ui.theme.SoftPrimary
import com.powermate.ai.ui.theme.SuccessGreen
import com.powermate.ai.ui.theme.TextMain
import com.powermate.ai.ui.theme.TextMuted
import com.powermate.ai.ui.theme.TextSecondary
import com.powermate.ai.ui.theme.WarningAmber
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Theme helpers ──────────────────────────────────────────────────────────
/** Converts the saved hex string to a Compose Color, falling back to Cyan. */
private fun accentFromHex(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrElse { Cyan }

/** Scales a base sp value by the user font scale preference. */
private fun Float.scaled(scale: Float) = this * scale

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
    headerGradient: List<Color> = listOf(PrimaryBlue, Cyan),
    badge: String? = null,
    badgeColor: Color = SuccessGreen,
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
            GradientBanner(
                title = title,
                subtitle = subtitle,
                gradient = headerGradient,
                badge = badge,
                badgeColor = badgeColor
            )
        }

        content()
    }
}

@Composable
private fun HomeScreen(controller: PowerMateViewModel, padding: PaddingValues) {
    val snap = controller.snapshot
    val context = LocalContext.current
    val accent = accentFromHex(controller.settings.accentColorHex)
    val fs = controller.settings.fontScale

    ScreenShell(
        "PowerMate AI", "Your private battery command center",
        padding,
        headerGradient = listOf(Color(0xFF1D4ED8), Color(0xFF0891B2)),
        badge = "All Free",
        badgeColor = SuccessGreen
    ) {
        item {
            SectionCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    BatteryRingColored(level = snap.levelPercent, accent = accent, isCharging = snap.isCharging)
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

        item { QuickStatsBar(controller) }

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
                    Modifier.weight(1f),
                    fontScale = controller.settings.fontScale
                )
                MetricCard(
                    "Power",
                    snap.wattage?.format1("W") ?: "--",
                    "Estimated",
                    Modifier.weight(1f),
                    SuccessGreen,
                    fontScale = controller.settings.fontScale
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
        item { BatteryHealthProgressCard(controller) }
        item { ChargingCoachCard(controller) }
        item { DeepSleepCard(controller) }
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
    val accent = accentFromHex(controller.settings.accentColorHex)

    ScreenShell("Live Monitor", "Real-time current, wattage, capacity and stability", padding) {
        item {
            SectionCard {
                Column {
                    Text(
                        snap.currentMilliAmp?.format0("mA") ?: snap.averageCurrentMilliAmp?.format0("mA") ?: "-- mA",
                        color = accent,
                        fontSize = (48 * controller.settings.fontScale).sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        snap.wattage?.let {
                            StatusChip("%.1fW".format(it), SuccessGreen, pulsing = snap.isCharging)
                        }
                        StatusChip(snap.status.label, statusColor(snap.status), pulsing = snap.isCharging)
                    }
                }
                Text(snap.status.label, color = TextSecondary, fontSize = 14.sp)

                Spacer(Modifier.height(18.dp))

                MiniGraph(values = listOf(20f, 45f, 52f, 48f, 65f, 70f, 62f, 80f, 76f, 84f))
            }
        }

        item { RealtimeBatteryLabCard(controller) }

        item { LiveSpeedometerCard(controller) }
        item { CapacityEstimateCard(controller) }

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
private fun LocalSensorReadoutCard(
    label: String,
    value: String,
    source: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    // Delegates to the shared, richer SensorReadoutCard in ui.components so both
    // the original call sites here and the new MEGA UPGRADE cards stay in sync.
    SensorReadoutCard(label, value, source, accent, modifier)
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
    val accent = accentFromHex(controller.settings.accentColorHex)
    val aodAccent = accentFromHex(controller.settings.aodAccentColorHex)

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
                    Text("Charging • ${controller.snapshot.levelPercent}%", color = aodAccent, fontSize = 16.sp)

                    Spacer(Modifier.height(24.dp))

                    BatteryRingColored(
                        level = controller.snapshot.levelPercent,
                        accent = aodAccent,
                        modifier = Modifier.size(160.dp),
                        isCharging = controller.snapshot.isCharging
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

        item { LooksThemeGallery(controller) }

        item { AccentColorSection(controller) }

        item { AodAccentColorSection(controller) }

        item { FontSizeSection(controller) }

        item { AodStyleSelectorSection(controller) }

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
private fun LooksThemeGallery(controller: PowerMateViewModel) {
    val themes = listOf(
        Triple("PowerBlue", "Default — clean and sharp", listOf(Color(0xFF00B4D8), Color(0xFF0077B6))),
        Triple("Neon Pulse", "High contrast glow", listOf(Color(0xFF39FF14), Color(0xFF00B4D8))),
        Triple("AMOLED Black", "Deep OLED, pure dark", listOf(Color(0xFF000000), Color(0xFF7B2FBE))),
        Triple("Ember Red", "Bold and energetic", listOf(Color(0xFFFF4D4D), Color(0xFFFF9F1C))),
        Triple("Clean White", "Light and readable", listOf(Color(0xFFF5F8FF), Color(0xFF0077B6))),
        Triple("Midnight Purple", "Premium dark look", listOf(Color(0xFF7B2FBE), Color(0xFF00B4D8)))
    )

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Theme preset", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text("Tap to apply — changes immediately", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("Free", SuccessGreen)
        }

        Spacer(Modifier.height(12.dp))

        themes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { (name, subtitle, colors) ->
                    val isSelected = controller.settings.selectedThemePreset == name
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 118.dp)
                            .background(
                                if (isSelected) CardElevated else CardElevated.copy(alpha = 0.55f),
                                RoundedCornerShape(22.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Cyan else Color.Transparent,
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable {
                                controller.updateSettings {
                                    it.copy(
                                        selectedThemePreset = name,
                                        accentColorHex = "#%06X".format(colors[0].value.toLong() and 0xFFFFFF),
                                        amoledMode = name == "AMOLED Black"
                                    )
                                }
                            }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(colors))
                        ) {
                            if (isSelected) {
                                Text(
                                    "✓",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Column {
                            Text(name, color = TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(subtitle, color = TextSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun FontSizeSection(controller: PowerMateViewModel) {
    val scales = listOf(
        Triple(0.85f, "Small", "Compact — more info fits"),
        Triple(1.0f, "Default", "Balanced and readable"),
        Triple(1.15f, "Large", "Easier to read at a glance"),
        Triple(1.3f, "XL", "Maximum readability")
    )
    SectionCard {
        Text("Text size", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Affects all labels and values", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            scales.forEach { (scale, label, _) ->
                val isSelected = controller.settings.fontScale == scale
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) PrimaryBlue.copy(alpha = 0.25f) else CardElevated.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Cyan else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { controller.updateSettings { it.copy(fontScale = scale) } }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Aa",
                        color = if (isSelected) Cyan else TextSecondary,
                        fontSize = (14 * scale).sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, color = if (isSelected) TextMain else TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AccentColorSection(controller: PowerMateViewModel) {
    val colors = listOf(
        Pair("#00B4D8", "Cyan"),
        Pair("#39FF14", "Neon Green"),
        Pair("#FF4D4D", "Red"),
        Pair("#FF9F1C", "Orange"),
        Pair("#7B2FBE", "Purple"),
        Pair("#FFFFFF", "White"),
        Pair("#FFD700", "Gold"),
        Pair("#00F5D4", "Mint")
    )
    SectionCard {
        Text("Accent colour", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Used on values, rings and highlights", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colors.forEach { (hex, label) ->
                val colorVal = Color(AndroidColor.parseColor(hex))
                val isSelected = controller.settings.accentColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colorVal, RoundedCornerShape(50))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable {
                            controller.updateSettings { it.copy(accentColorHex = hex) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text("✓", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "Selected: ${colors.firstOrNull { it.first.equals(controller.settings.accentColorHex, ignoreCase = true) }?.second ?: "Custom"}",
            color = TextSecondary, fontSize = 12.sp
        )
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
    val context = LocalContext.current
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

        item { ChargerComparisonCard(controller) }

        if (controller.sessions.isNotEmpty()) {
            item {
                PrimaryAction(
                    "Export history (CSV)",
                    onClick = { shareHistoryCsv(context, controller.sessions) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

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

private fun shareHistoryCsv(context: android.content.Context, sessions: List<ChargingSession>) {
    runCatching {
        val file = HistoryExportManager(context).exportCsv(sessions)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share charging history"))
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

                Spacer(Modifier.height(10.dp))
                Text("Speedometer style", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Controls the main battery ring style on the home screen", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                com.powermate.ai.domain.model.SpeedometerStyle.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { style ->
                            val isSel = controller.settings.selectedSpeedometerStyle == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (isSel) PrimaryBlue.copy(alpha = 0.22f) else CardElevated.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                    .border(width = if (isSel) 2.dp else 0.dp, color = if (isSel) Cyan else Color.Transparent, shape = RoundedCornerShape(10.dp))
                                    .clickable { controller.updateSettings { it.copy(selectedSpeedometerStyle = style) } }
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Text(style.label, color = if (isSel) Cyan else TextSecondary, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                }
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

                Spacer(Modifier.height(10.dp))
                Text("Notification format", color = TextMain, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("How much detail appears in charging notifications", color = TextSecondary, fontSize = 12.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    com.powermate.ai.domain.model.NotificationFormat.entries.forEach { fmt ->
                        val isSel = controller.settings.notificationFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isSel) PrimaryBlue.copy(alpha = 0.22f) else CardElevated.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                .border(width = if (isSel) 2.dp else 0.dp, color = if (isSel) Cyan else Color.Transparent, shape = RoundedCornerShape(10.dp))
                                .clickable { controller.updateSettings { it.copy(notificationFormat = fmt) } }
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(fmt.label, color = if (isSel) Cyan else TextSecondary, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
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
            SectionCard(gradient = listOf(Color(0xFF1E293B), Color(0xFF0F172A))) {
                GradientBanner(
                    title = "Why PowerMate is Different",
                    subtitle = "Free forever • No login • No cloud • No fake data",
                    gradient = listOf(Color(0xFF1D4ED8), Color(0xFF0891B2))
                )
                Spacer(Modifier.height(14.dp))
                val advantages = listOf(
                    "✓ All features FREE — no Pro tier, no paywalls",
                    "✓ No ads — ever, on any screen",
                    "✓ No account, email or phone number required",
                    "✓ No Firebase, no cloud sync, no telemetry SDK",
                    "✓ Battery data stays 100% on your device",
                    "✓ Never shows fake hardware readings — marks unavailable sensors honestly",
                    "✓ Beats AccuBattery: free capacity estimates, no 1-day history lock",
                    "✓ Beats AmpereFlow: real battery health scoring, not just AOD aesthetics"
                )
                advantages.forEach {
                    Text(it, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
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
private fun AodAccentColorSection(controller: PowerMateViewModel) {
    val colors = listOf(
        Pair("#00B4D8", "Cyan"),
        Pair("#39FF14", "Neon Green"),
        Pair("#FF4D4D", "Red"),
        Pair("#FF9F1C", "Orange"),
        Pair("#7B2FBE", "Purple"),
        Pair("#FFFFFF", "White"),
        Pair("#FFD700", "Gold"),
        Pair("#00F5D4", "Mint"),
        Pair("#FF69B4", "Pink"),
        Pair("#22C55E", "Green")
    )
    SectionCard {
        Text("AOD accent colour", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Used on the AOD display — independent from main accent", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.take(5).forEach { (hex, _) ->
                val colorVal = Color(android.graphics.Color.parseColor(hex))
                val isSelected = controller.settings.aodAccentColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colorVal, RoundedCornerShape(50))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { controller.updateSettings { it.copy(aodAccentColorHex = hex) } },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Text("✓", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.drop(5).forEach { (hex, _) ->
                val colorVal = Color(android.graphics.Color.parseColor(hex))
                val isSelected = controller.settings.aodAccentColorHex.equals(hex, ignoreCase = true)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(colorVal, RoundedCornerShape(50))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Transparent,
                            shape = RoundedCornerShape(50)
                        )
                        .clickable { controller.updateSettings { it.copy(aodAccentColorHex = hex) } },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Text("✓", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "AOD: ${colors.firstOrNull { it.first.equals(controller.settings.aodAccentColorHex, ignoreCase = true) }?.second ?: "Custom"}",
            color = TextSecondary, fontSize = 12.sp
        )
    }
}

@Composable
private fun AodStyleSelectorSection(controller: PowerMateViewModel) {
    val styles = com.powermate.ai.domain.model.AodDisplayStyle.entries
    SectionCard {
        Text("AOD display style", color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Tap a style to apply — takes effect next time AOD opens", color = TextSecondary, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        styles.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { style ->
                    val isSelected = controller.settings.selectedAodStyle == style
                    val aodAccent = accentFromHex(controller.settings.aodAccentColorHex)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 96.dp)
                            .background(
                                if (isSelected) CardElevated else CardElevated.copy(alpha = 0.45f),
                                RoundedCornerShape(18.dp)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) aodAccent else Color.Transparent,
                                shape = RoundedCornerShape(18.dp)
                            )
                            .clickable { controller.updateSettings { it.copy(selectedAodStyle = style) } }
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(style.label, color = if (isSelected) aodAccent else TextMain, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            if (isSelected) Text("✓", color = aodAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(style.description, color = TextSecondary, fontSize = 10.sp, maxLines = 2, lineHeight = 13.sp, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
        }
        Text("Active: ${controller.settings.selectedAodStyle.label}", color = TextSecondary, fontSize = 12.sp)
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


@Composable
private fun QuickStatsBar(controller: PowerMateViewModel) {
    val snap = controller.snapshot
    val accent = accentFromHex(controller.settings.accentColorHex)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Charger score circular widget
        SectionCard(modifier = Modifier.weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularScore(
                    label = "Charger",
                    score = controller.latestDiagnostic?.chargerScore
                        ?: controller.sessions.maxOfOrNull { it.chargerScore ?: 0 }
                        ?: 0,
                    accent = SuccessGreen,
                    size = 72.dp
                )
            }
        }
        // Battery care circular widget
        SectionCard(modifier = Modifier.weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularScore(
                    label = "Care",
                    score = controller.insights.batteryCareScore,
                    accent = Cyan,
                    size = 72.dp
                )
            }
        }
        // Charging health circular widget
        SectionCard(modifier = Modifier.weight(1f)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularScore(
                    label = "Health",
                    score = controller.insights.chargingHealthScore,
                    accent = SoftPrimary,
                    size = 72.dp
                )
            }
        }
    }
}

@Composable
private fun BatteryHealthProgressCard(controller: PowerMateViewModel) {
    val insights = controller.insights
    val snap = controller.snapshot
    val careScore = insights.batteryCareScore
    val barColor = when {
        careScore >= 80 -> SuccessGreen
        careScore >= 55 -> WarningAmber
        else -> DangerRed
    }

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Battery Care Score", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Based on charging habits, temp and history", color = TextSecondary, fontSize = 12.sp)
            }
            Text("$careScore/100", color = barColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        HealthBar(progress = careScore / 100f, accent = barColor)
        Spacer(Modifier.height(10.dp))

        val tipText = when {
            careScore >= 85 -> "Excellent! Your battery habits are protecting long-term capacity."
            careScore >= 65 -> "Good habits. Avoid charging above 85% daily for better longevity."
            careScore >= 45 -> "Fair. Your battery may be experiencing some wear from heat or overcharging."
            else -> "Needs attention. High temps or repeated 100% charges are reducing lifespan."
        }
        Text(tipText, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
private fun LiveSpeedometerCard(controller: PowerMateViewModel) {
    val snap = controller.snapshot
    val accent = accentFromHex(controller.settings.accentColorHex)
    val currentMa = snap.currentMilliAmp ?: snap.averageCurrentMilliAmp ?: 0f
    val maxExpected = 6000f
    val ratio = (currentMa.coerceAtLeast(0f) / maxExpected).coerceIn(0f, 1f)

    SectionCard(gradient = listOf(CardDark, Color(0xFF0A1628))) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Live Speed Meter", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Real-time current draw from sensor", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip(
                if (snap.isSensorReliable) "Live" else "Est.",
                if (snap.isSensorReliable) SuccessGreen else WarningAmber,
                pulsing = snap.isSensorReliable && snap.isCharging
            )
        }
        Spacer(Modifier.height(14.dp))

        // Speedometer-style arc indicator
        Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
            val cx = size.width / 2f
            val cy = size.height * 0.95f
            val radius = size.width * 0.42f
            val startAngle = 180f
            val sweepTotal = 180f
            val sw = Stroke(14.dp.toPx(), cap = StrokeCap.Round)

            drawArc(color = CardHighlight, startAngle = startAngle, sweepAngle = sweepTotal, useCenter = false,
                topLeft = Offset(cx - radius, cy - radius), size = Size(radius * 2, radius * 2), style = sw)

            drawArc(
                brush = Brush.sweepGradient(listOf(SuccessGreen, WarningAmber, DangerRed, DangerRed)),
                startAngle = startAngle, sweepAngle = sweepTotal * ratio, useCenter = false,
                topLeft = Offset(cx - radius, cy - radius), size = Size(radius * 2, radius * 2), style = sw
            )

            // Needle
            val needleAngle = (180 + sweepTotal * ratio) * (PI / 180f)
            val needleLen = radius * 0.72f
            drawLine(
                color = Color.White,
                start = Offset(cx, cy),
                end = Offset((cx + needleLen * cos(needleAngle)).toFloat(), (cy + needleLen * sin(needleAngle)).toFloat()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = CardHighlight, radius = 8.dp.toPx(), center = Offset(cx, cy))
            drawCircle(color = accent, radius = 5.dp.toPx(), center = Offset(cx, cy))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("0 mA", color = TextMuted, fontSize = 10.sp)
            Text(
                currentMa.let { "${it.toInt()} mA" },
                color = accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("6000 mA", color = TextMuted, fontSize = 10.sp)
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            SensorReadoutCard("Wattage", snap.wattage?.let { "%.1fW".format(it) } ?: "--", "V×A", SuccessGreen, Modifier.weight(1f), snap.isCharging)
            SensorReadoutCard("Voltage", snap.voltageVolt?.let { "%.2fV".format(it) } ?: "--", snap.pluggedType.label, SoftPrimary, Modifier.weight(1f), false)
            SensorReadoutCard("Temp", snap.temperatureCelsius?.let { "%.1f°C".format(it) } ?: "--", snap.health.label, WarningAmber, Modifier.weight(1f), snap.isCharging)
        }
    }
}

@Composable
private fun DeepSleepCard(controller: PowerMateViewModel) {
    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Deep Sleep & Standby", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Screen-off battery consumption tracking", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("Free", SuccessGreen)
        }
        Spacer(Modifier.height(12.dp))

        val snap = controller.snapshot
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Screen OFF drain", controller.insights.screenOffDrainLabel, "While in standby", Modifier.weight(1f), SoftPrimary)
            MetricCard("Discharge rate", controller.insights.dischargeRateMa?.let { "${it.toInt()} mA" } ?: "--", "When unplugged", Modifier.weight(1f), WarningAmber)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "PowerMate tracks discharge when screen is off to identify background apps draining your battery — same as AccuBattery deep sleep mode, completely free.",
            color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp
        )
    }
}

@Composable
private fun ChargerComparisonCard(controller: PowerMateViewModel) {
    val sessions = controller.sessions
    if (sessions.size < 2) return

    val sorted = sessions.sortedByDescending { it.chargerScore ?: 0 }
    val best = sorted.firstOrNull()
    val worst = sorted.lastOrNull()

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Charger Comparison", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Best vs worst from your saved tests", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip("${sessions.size} tests", Cyan)
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f).background(SuccessGreen.copy(alpha = 0.08f), RoundedCornerShape(18.dp)).padding(12.dp)) {
                StatusChip("Best", SuccessGreen)
                Spacer(Modifier.height(8.dp))
                Text(best?.userLabel ?: "Charger", color = TextMain, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${best?.chargerScore ?: 0}/100", color = SuccessGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Avg ${best?.averageWattage?.let { "%.1fW".format(it) } ?: "--"}", color = TextSecondary, fontSize = 11.sp)
            }
            Column(modifier = Modifier.weight(1f).background(DangerRed.copy(alpha = 0.08f), RoundedCornerShape(18.dp)).padding(12.dp)) {
                StatusChip("Weakest", DangerRed)
                Spacer(Modifier.height(8.dp))
                Text(worst?.userLabel ?: "Charger", color = TextMain, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${worst?.chargerScore ?: 0}/100", color = DangerRed, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Avg ${worst?.averageWattage?.let { "%.1fW".format(it) } ?: "--"}", color = TextSecondary, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("Test each charger on this same device screen-off for cleanest results.", color = TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun CapacityEstimateCard(controller: PowerMateViewModel) {
    val insights = controller.insights
    val snap = controller.snapshot
    val cap = insights.estimatedCapacityMah
    val chargeCounter = snap.chargeCounterMah

    SectionCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Capacity Estimate", color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("mAh reading from charge counter sensor", color = TextSecondary, fontSize = 12.sp)
            }
            StatusChip(insights.capacityConfidence, Cyan)
        }
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Est. capacity", cap?.let { "${it.toInt()} mAh" } ?: "--", insights.capacityConfidence, Modifier.weight(1f), Cyan)
            MetricCard("Charge counter", chargeCounter?.let { "${it.toInt()} mAh" } ?: "--", "Fuel gauge sensor", Modifier.weight(1f), SoftPrimary)
        }
        Spacer(Modifier.height(10.dp))

        val note = when (insights.capacityConfidence) {
            "High confidence" -> "Sensor data is consistent. This reading is reliable for comparing chargers."
            "Medium confidence" -> "Sensor data varies slightly. Run more charge cycles for a better estimate."
            else -> "Limited sensor data. This phone may restrict charge counter access. Values are estimated."
        }
        Text(note, color = TextSecondary, fontSize = 12.sp, lineHeight = 17.sp)
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
