package com.powermate.ai.aod

import kotlin.math.roundToInt

class BurnInProtectionController(
    private val maxOffsetPx: Int = 28,
    private val shiftIntervalMillis: Long = 60_000L
) {
    fun offsetForTime(nowMillis: Long): Pair<Int, Int> {
        val step = (nowMillis / shiftIntervalMillis).toInt()
        val x = (((step * 37) % (maxOffsetPx * 2 + 1)) - maxOffsetPx)
        val y = (((step * 53) % (maxOffsetPx * 2 + 1)) - maxOffsetPx)
        return x to y
    }

    fun dimAlphaForNightMode(hour24: Int): Float {
        return if (hour24 >= 22 || hour24 <= 6) 0.72f else 1f
    }
}
