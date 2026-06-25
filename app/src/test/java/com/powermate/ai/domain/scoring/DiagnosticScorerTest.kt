package com.powermate.ai.domain.scoring

import com.powermate.ai.domain.model.BatteryHealthStatus
import com.powermate.ai.domain.model.BatterySnapshot
import com.powermate.ai.domain.model.ChargingStatus
import com.powermate.ai.domain.model.PluggedType
import org.junit.Assert.assertTrue
import org.junit.Test

class DiagnosticScorerTest {
    @Test fun strongStableChargerScoresHigh() {
        val readings = List(20) { index ->
            BatterySnapshot(
                levelPercent = 55 + index / 5,
                isCharging = true,
                status = ChargingStatus.FastCharging,
                pluggedType = PluggedType.Ac,
                currentMicroAmp = -3_000_000,
                voltageMilliVolt = 9000,
                temperatureCelsius = 34f,
                wattage = 27f,
                health = BatteryHealthStatus.Good
            )
        }
        val result = DiagnosticScorer.score(readings)
        assertTrue(result.chargerScore >= 80)
        assertTrue(result.cableScore >= 70)
    }
}
