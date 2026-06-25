package com.powermate.ai.data.battery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.powermate.ai.domain.insights.ChargeTimeEstimator
import com.powermate.ai.domain.model.BatteryHealthStatus
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.PluggedType
import com.powermate.ai.domain.scoring.ChargingSpeedClassifier
import kotlin.math.abs

class BatteryStatsManager(private val context: Context) {
    private val batteryManager: BatteryManager =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    fun currentSnapshot(): BatterySnapshot {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val precisePercent = if (level >= 0 && scale > 0) (level * 100f) / scale else null
        val percent = precisePercent?.toInt() ?: 0
        val statusRaw = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val isCharging = statusRaw == BatteryManager.BATTERY_STATUS_CHARGING ||
            statusRaw == BatteryManager.BATTERY_STATUS_FULL
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)?.takeIf { it > 0 }
        val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val tempC = tempTenths?.takeIf { it != Int.MIN_VALUE }?.let { it / 10f }
        val healthRaw = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)

        val currentNow = safeIntBatteryProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val avgCurrent = safeIntBatteryProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounter = safeIntBatteryProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacityGauge = safeIntBatteryProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val energyCounter = safeLongBatteryProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        val currentForWatts = currentNow ?: avgCurrent
        val wattage = if (currentForWatts != null && voltageMv != null) {
            (abs(currentForWatts) / 1_000_000f) * (voltageMv / 1000f)
        } else null

        val timeToFull = ChargeTimeEstimator.estimateMinutesToFull(
            levelPercent = percent,
            chargeCounterMicroAh = chargeCounter,
            currentMicroAmp = currentForWatts,
            isCharging = isCharging
        )
        val timeToEmpty = ChargeTimeEstimator.estimateMinutesToEmpty(
            levelPercent = percent,
            chargeCounterMicroAh = chargeCounter,
            currentMicroAmp = currentForWatts,
            isCharging = isCharging
        )

        val base = BatterySnapshot(
            levelPercent = percent.coerceIn(0, 100),
            preciseLevelPercent = precisePercent?.coerceIn(0f, 100f),
            isCharging = isCharging,
            status = when (statusRaw) {
                BatteryManager.BATTERY_STATUS_FULL -> ChargingStatus.Full
                BatteryManager.BATTERY_STATUS_DISCHARGING -> ChargingStatus.NotCharging
                else -> ChargingStatus.Unknown
            },
            pluggedType = mapPluggedType(plugged),
            currentMicroAmp = currentNow,
            averageCurrentMicroAmp = avgCurrent,
            chargeCounterMicroAh = chargeCounter,
            capacityPercentFromFuelGauge = capacityGauge?.coerceIn(0, 100),
            energyCounterNanoWh = energyCounter,
            voltageMilliVolt = voltageMv,
            temperatureCelsius = tempC,
            wattage = wattage,
            health = mapHealth(healthRaw),
            technology = technology,
            timeToFullMinutes = timeToFull,
            timeToEmptyMinutes = timeToEmpty,
            isSensorReliable = currentNow != null || avgCurrent != null
        )
        val classified = if (base.status == ChargingStatus.Full) ChargingStatus.Full else ChargingSpeedClassifier.classify(base)
        return base.copy(status = classified)
    }

    private fun safeIntBatteryProperty(property: Int): Int? {
        return try {
            val value = batteryManager.getIntProperty(property)
            value.takeIf { it != Int.MIN_VALUE && it != 0 }
        } catch (_: Exception) {
            null
        }
    }

    private fun safeLongBatteryProperty(property: Int): Long? {
        return try {
            val value = batteryManager.getLongProperty(property)
            value.takeIf { it != Long.MIN_VALUE && it != 0L }
        } catch (_: Exception) {
            null
        }
    }

    private fun mapPluggedType(raw: Int): PluggedType = when (raw) {
        BatteryManager.BATTERY_PLUGGED_USB -> PluggedType.Usb
        BatteryManager.BATTERY_PLUGGED_AC -> PluggedType.Ac
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> PluggedType.Wireless
        BatteryManager.BATTERY_PLUGGED_DOCK -> PluggedType.Dock
        0 -> PluggedType.None
        else -> PluggedType.Unknown
    }

    private fun mapHealth(raw: Int): BatteryHealthStatus = when (raw) {
        BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealthStatus.Good
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealthStatus.Overheat
        BatteryManager.BATTERY_HEALTH_COLD -> BatteryHealthStatus.Cold
        BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealthStatus.Dead
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealthStatus.OverVoltage
        else -> BatteryHealthStatus.Unknown
    }
}
