package com.powermate.ai.domain.coach

import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import kotlin.test.Test
import kotlin.test.assertTrue

class ChargingCoachTest {
    @Test
    fun slowChargingReturnsInternetAndCableSuggestions() {
        val suggestions = ChargingCoach().suggest(
            BatterySnapshot(
                levelPercent = 42,
                isCharging = true,
                status = ChargingStatus.SlowCharging,
                currentMicroAmp = 350_000,
                voltageMilliVolt = 4_000,
                temperatureCelsius = 34f
            )
        )

        assertTrue(suggestions.any { it.title.contains("network", ignoreCase = true) })
        assertTrue(suggestions.any { it.title.contains("cable", ignoreCase = true) })
    }

    @Test
    fun hotChargingReturnsCoolingSuggestion() {
        val suggestions = ChargingCoach().suggest(
            BatterySnapshot(
                levelPercent = 60,
                isCharging = true,
                status = ChargingStatus.FastCharging,
                temperatureCelsius = 40f
            )
        )

        assertTrue(suggestions.any { it.title.contains("Cool", ignoreCase = true) })
    }
}
