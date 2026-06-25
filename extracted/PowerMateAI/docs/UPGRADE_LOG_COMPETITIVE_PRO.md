# PowerMate AI Competitive Pro Upgrade Log

## Date
2026-06-12

## Goal
Deeply compare PowerMate AI against AmpereFlow and adjacent 1M+/5M+ battery apps, then upgrade the project with stronger feature coverage while keeping the app original, privacy-first and Android-native.

## Main apps studied
- AmpereFlow: Battery Speed, AOD
- AccuBattery
- Ampere
- Battery Guru
- Charge Meter
- AOD Flow-style AOD apps

## Added / upgraded in code
- CompetitiveFeatureRegistry: feature matrix based on public competitor feature buckets.
- AdvancedBatteryInsights: charging health score, battery care score, thermal risk, capacity estimate, discharge insight and slow-session warning.
- ChargeTimeEstimator: time-to-full and time-to-empty estimates when fuel-gauge/current data exists.
- BatterySnapshot: charge counter, fuel-gauge capacity, energy counter, precise percent and runtime fields.
- BatteryStatsManager: reads BatteryManager charge counter, energy counter, capacity, current, voltage and temperature.
- PowerMateViewModel: exposes insights and competitive feature matrix to UI.
- Home dashboard: added health/care/capacity/time insight card and competitor coverage card.
- Live Monitor: added charge counter and discharge metric cards.
- AOD settings: added auto-position shift, media controls and camera shortcut settings foundation.
- History: added care/thermal insight and slow-charger warning.
- Settings: added feature matrix and advanced alert settings.
- Preferences: stores new AOD, notification, alert and speedometer settings.
- AOD quick-action foundation: safe camera/settings shortcuts.
- Tests: added time estimator test.

## Strategy
PowerMate AI should not clone competitors. It should offer the same public value buckets with a cleaner Material 3 AMOLED UI, no account, local-first data and free core tools.

## Known limitation
A real Android Gradle build was not run inside the sandbox because Gradle/Android SDK is unavailable. GitHub Actions is still configured for CI builds.
