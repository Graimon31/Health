package com.example.healthapp

import com.example.healthapp.util.calculateTrendSlope
import com.example.healthapp.util.trendMessageForSlope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AnalyticsTest {

    @Test
    fun risingTrendProducesAlertMessage() {
        val slope = calculateTrendSlope(listOf(70, 72, 75, 80, 85))
        assertTrue(slope > 0.5)
        assertEquals("Пульс растёт — возможная нагрузка или стресс", trendMessageForSlope(slope))
    }

    @Test
    fun stableTrendIsNormal() {
        val slope = calculateTrendSlope(listOf(70, 69, 71, 70, 70))
        assertTrue(slope < 0.5)
        assertEquals("Тренд в норме", trendMessageForSlope(slope))
    }
}
