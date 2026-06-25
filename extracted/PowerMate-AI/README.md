# PowerMate AI

PowerMate AI is a native Android battery charging monitor, AOD-style charging display, charger diagnostic and battery care app.

## Competitive Pro v1.1

This version is designed against public feature sets from AmpereFlow, AccuBattery, Ampere, Battery Guru and Charge Meter while staying original and Android-native.

### Included

- Live ampere and wattage
- Voltage, temperature and battery health
- Fast/slow/very fast charging classification
- Charger diagnostic test
- Charger score, cable score and stability score
- Battery care score and charging health score
- Capacity estimate when fuel-gauge data is available
- Time-to-full and time-to-empty estimates when supported
- AOD-style charging display
- Burn-in protection and night dim foundation
- Smart Charging Coach with Android settings shortcuts
- Charging history
- Widget foundation
- Custom notification format model
- Competitive feature matrix
- Offline-first privacy posture

## Android support

- minSdk 26: Android 8.0+
- targetSdk 36
- compileSdk 36

## Build

```bash
gradle testDebugUnitTest
gradle :app:assembleDebug
gradle :app:assembleRelease
gradle :app:bundleRelease
```

Release signing uses GitHub Secrets:

- KEYSTORE_BASE64
- KEYSTORE_PASSWORD
- KEY_ALIAS
- KEY_PASSWORD

## Honesty

Battery readings depend on device hardware. PowerMate AI must never fake exact current, wattage or capacity if the device does not expose reliable data.
