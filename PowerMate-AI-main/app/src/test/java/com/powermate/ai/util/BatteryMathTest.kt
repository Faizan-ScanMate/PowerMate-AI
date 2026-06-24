package com.powermate.ai.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BatteryMathTest {
    @Test fun wattageUsesCurrentAndVoltage() {
        val watts = BatteryMath.wattage(currentMicroAmp = -2_000_000, voltageMilliVolt = 5000)
        assertEquals(10.0f, watts!!, 0.01f)
    }

    @Test fun invalidWattageReturnsNull() {
        assertNull(BatteryMath.wattage(null, 5000))
        assertNull(BatteryMath.wattage(1_000_000, null))
    }

    @Test fun levelIsClamped() {
        assertEquals(50, BatteryMath.levelPercent(50, 100))
        assertEquals(0, BatteryMath.levelPercent(-1, 100))
    }
}
