package com.powermate.ai.domain.insights

import kotlin.math.abs
import kotlin.math.roundToInt

object ChargeTimeEstimator {
    fun estimateMinutesToFull(levelPercent: Int, chargeCounterMicroAh: Int?, currentMicroAmp: Int?, isCharging: Boolean): Int? {
        if (!isCharging || chargeCounterMicroAh == null || currentMicroAmp == null) return null
        val level = levelPercent.coerceIn(1, 99)
        val currentMah = abs(currentMicroAmp) / 1000f
        if (currentMah < 50f) return null
        val currentChargeMah = abs(chargeCounterMicroAh) / 1000f
        val estimatedFullCapacityMah = currentChargeMah / (level / 100f)
        val remainingMah = (estimatedFullCapacityMah - currentChargeMah).coerceAtLeast(0f)
        return ((remainingMah / currentMah) * 60f).roundToInt().takeIf { it in 1..1440 }
    }

    fun estimateMinutesToEmpty(levelPercent: Int, chargeCounterMicroAh: Int?, currentMicroAmp: Int?, isCharging: Boolean): Int? {
        if (isCharging || chargeCounterMicroAh == null || currentMicroAmp == null) return null
        val drainMah = abs(currentMicroAmp) / 1000f
        if (drainMah < 30f) return null
        val currentChargeMah = abs(chargeCounterMicroAh) / 1000f
        return ((currentChargeMah / drainMah) * 60f).roundToInt().takeIf { it in 1..2880 }
    }
}
