# PowerMate AI TRD

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Android BatteryManager
- SQLiteOpenHelper for local history foundation
- SharedPreferences for lightweight settings foundation
- RemoteViews AppWidgetProvider
- GitHub Actions
- Gradle 9.4.1 on CI
- Android Gradle Plugin 9.2.1
- Kotlin 2.4.0
- Compose BOM 2026.05.00

## Architecture

```text
com.powermate.ai
├── data
│   ├── battery
│   ├── local
│   ├── preferences
│   └── repository
├── domain
│   ├── model
│   └── scoring
├── ui
│   ├── components
│   └── theme
├── aod
├── service
├── receiver
└── widget
```

## Battery data

Battery data is read from ACTION_BATTERY_CHANGED and BatteryManager properties. Devices that do not expose current sensors are handled with fallback text instead of fake numbers.

## Safety

- No private OEM APIs
- No root required
- No account required
- No cloud required
- No dangerous permissions
- Foreground service is present but not started by default
