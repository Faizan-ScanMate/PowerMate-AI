package com.powermate.ai.domain.insights

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ChargeTimeEstimatorTest {
    @Test
    fun estimatesTimeToFullWhenCharging() {
        val minutes = ChargeTimeEstimator.estimateMinutesToFull(
            levelPercent = 50,
            chargeCounterMicroAh = 2_000_000,
            currentMicroAmp = 2_000_000,
            isCharging = true
        )
        assertEquals(60, minutes)
    }

    @Test
    fun returnsNullWhenNotChargingForTimeToFull() {
        assertNull(ChargeTimeEstimator.estimateMinutesToFull(50, 2_000_000, 2_000_000, false))
    }
}
