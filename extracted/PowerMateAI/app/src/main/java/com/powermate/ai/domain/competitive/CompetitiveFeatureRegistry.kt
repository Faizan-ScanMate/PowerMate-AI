package com.powermate.ai.domain.competitive

enum class FeatureAvailability(val label: String) {
    Included("Included"),
    Planned("Planned"),
    NotApplicable("Not applicable")
}

data class CompetitiveFeature(
    val name: String,
    val category: String,
    val powerMateStatus: FeatureAvailability,
    val competitorSignal: String,
    val powerMateAdvantage: String
)

object CompetitiveFeatureRegistry {
    fun all(): List<CompetitiveFeature> = listOf(
        CompetitiveFeature("Live ampere reading", "Core meter", FeatureAvailability.Included, "AmpereFlow / Ampere / Battery Guru", "Free, visible on dashboard and AOD"),
        CompetitiveFeature("Live wattage reading", "Core meter", FeatureAvailability.Included, "AmpereFlow / Battery Guru", "Free and switchable against mA"),
        CompetitiveFeature("Precise battery percentage", "Core meter", FeatureAvailability.Included, "AmpereFlow", "Decimal-ready model with honest fallback"),
        CompetitiveFeature("Voltage and temperature", "Core meter", FeatureAvailability.Included, "AmpereFlow / Battery Guru / Charge Meter", "Shown with thermal risk label"),
        CompetitiveFeature("Fast and slow charging detection", "Core meter", FeatureAvailability.Included, "AmpereFlow / Ampere", "Adds stability and coach recommendations"),
        CompetitiveFeature("Discharge current", "Usage", FeatureAvailability.Included, "Ampere / Charge Meter", "Shows runtime estimate when sensor exists"),
        CompetitiveFeature("Remaining charge time", "Usage", FeatureAvailability.Included, "Charge Meter / AccuBattery", "Fuel-gauge estimate with no fake precision"),
        CompetitiveFeature("Remaining use time", "Usage", FeatureAvailability.Included, "Charge Meter / AccuBattery", "Available when discharge current exists"),
        CompetitiveFeature("Actual capacity estimate", "Health", FeatureAvailability.Included, "AccuBattery / Battery Guru", "Fuel-gauge capacity estimate with confidence label"),
        CompetitiveFeature("Battery wear/care score", "Health", FeatureAvailability.Included, "AccuBattery / Battery Guru", "Combines temp, limit habits and session history"),
        CompetitiveFeature("Charge alarm 80/90/full", "Alerts", FeatureAvailability.Included, "AccuBattery / AmpereFlow / Battery Guru", "No paywall"),
        CompetitiveFeature("Overheat and unstable charger alerts", "Alerts", FeatureAvailability.Included, "Battery Guru / AmpereFlow", "Quiet-hours ready"),
        CompetitiveFeature("Custom notification format", "Alerts", FeatureAvailability.Included, "AmpereFlow changelog-style feature", "Compact, detailed, watt-focused and temp-focused"),
        CompetitiveFeature("AOD charging display", "AOD", FeatureAvailability.Included, "AmpereFlow / AOD Flow", "AOD-style display with burn-in protection"),
        CompetitiveFeature("AOD metric customization", "AOD", FeatureAvailability.Included, "AmpereFlow changelog-style feature", "Free metrics: percent, mA, W, temp, voltage, time"),
        CompetitiveFeature("AOD timeout and night dim", "AOD", FeatureAvailability.Included, "AmpereFlow / AOD Flow", "Designed as battery-safe defaults"),
        CompetitiveFeature("Media controls and quick actions", "AOD", FeatureAvailability.Planned, "AOD Flow / AmpereFlow-style", "Safe shortcuts without broad permissions"),
        CompetitiveFeature("Home screen widgets", "Widgets", FeatureAvailability.Included, "Ampere / Charge Meter premium / Battery Guru", "Core widgets are free"),
        CompetitiveFeature("Picture-in-picture monitor", "Advanced", FeatureAvailability.Planned, "Charge Meter premium", "Mini live monitor planned as free"),
        CompetitiveFeature("Charger and cable comparison", "Diagnostics", FeatureAvailability.Included, "Ampere / Charge Meter / Battery Guru", "Adds 0-100 charger/cable score"),
        CompetitiveFeature("Charging coach", "Optimization", FeatureAvailability.Included, "Not a direct core AmpereFlow feature", "Guided safe Android setting shortcuts")
    )

    fun includedCount(): Int = all().count { it.powerMateStatus == FeatureAvailability.Included }
    fun plannedCount(): Int = all().count { it.powerMateStatus == FeatureAvailability.Planned }
}
