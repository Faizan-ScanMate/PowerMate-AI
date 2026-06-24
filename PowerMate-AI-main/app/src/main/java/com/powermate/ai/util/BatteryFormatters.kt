package com.powermate.ai.util

import java.util.Locale
import kotlin.math.abs

object BatteryFormatters {
    fun currentMicroAmpToMilliAmpText(value: Int?): String {
        val ma = value?.let { abs(it) / 1000f } ?: return "-- mA"
        return "${ma.toInt()} mA"
    }

    fun wattText(value: Float?): String = value?.let { String.format(Locale.US, "%.1f W", it) } ?: "-- W"
    fun voltText(milliVolt: Int?): String = milliVolt?.let { String.format(Locale.US, "%.2f V", it / 1000f) } ?: "-- V"
    fun tempText(celsius: Float?, unit: String = "°C"): String = celsius?.let { String.format(Locale.US, "%.1f%s", it, unit) } ?: "--$unit"
    fun percent(value: Int?): String = value?.coerceIn(0, 100)?.let { "$it%" } ?: "--%"
}
