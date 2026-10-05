package com.bigegg.tvrepairledger.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class BarChartScaleTest {
    private fun point(revenue: Long, profit: Long) = BarChartPoint(
        label = "2026-09",
        revenueCents = revenue,
        profitCents = profit
    )

    @Test
    fun calculateBarChartScale_coversRevenuePositiveAndNegativeProfit() {
        val scale = calculateBarChartScale(
            listOf(point(50000, 45000), point(30000, -10000))
        )

        assertEquals(50000L, scale.maxPositiveCents)
        assertEquals(10000L, scale.maxNegativeMagnitudeCents)
    }

    @Test
    fun calculateBarChartScale_usesStableRangeForAllZeroValues() {
        val scale = calculateBarChartScale(listOf(point(0, 0)))

        assertEquals(1L, scale.maxPositiveCents)
        assertEquals(0L, scale.maxNegativeMagnitudeCents)
    }

    @Test
    fun calculateBarChartScale_usesStableRangeForEmptyInput() {
        val scale = calculateBarChartScale(emptyList())

        assertEquals(1L, scale.maxPositiveCents)
        assertEquals(0L, scale.maxNegativeMagnitudeCents)
    }

    @Test
    fun calculateBarChartScale_coversPositiveProfitAboveRevenue() {
        val scale = calculateBarChartScale(listOf(point(1000, 5000)))

        assertEquals(5000L, scale.maxPositiveCents)
        assertEquals(0L, scale.maxNegativeMagnitudeCents)
    }
}
