package com.powermate.ai.util

import kotlin.math.abs

object BatteryMath {
    fun wattage(currentMicroAmp: Int?, voltageMilliVolt: Int?): Float? {
        if (currentMicroAmp == null || voltageMilliVolt == null || voltageMilliVolt <= 0) return null
        return (abs(currentMicroAmp) / 1_000_000f) * (voltageMilliVolt / 1000f)
    }

    fun levelPercent(level: Int, scale: Int): Int {
        if (level < 0 || scale <= 0) return 0
        return ((level * 100f) / scale).toInt().coerceIn(0, 100)
    }

    fun temperatureCelsius(rawTenths: Int?): Float? {
        if (rawTenths == null || rawTenths == Int.MIN_VALUE) return null
        return rawTenths / 10f
    }

    fun clampScore(score: Int): Int = score.coerceIn(0, 100)
}
