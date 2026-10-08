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

    @Test
    fun barChartAxisTicks_coversPositiveRangeAndZeroLine() {
        val ticks = barChartAxisTicks(BarChartScale(maxPositiveCents = 50000L, maxNegativeMagnitudeCents = 0L))

        assertEquals(listOf(50000L, 25000L, 0L), ticks)
    }

    @Test
    fun barChartAxisTicks_addsNegativeTicksWhenProfitIsBelowZero() {
        val ticks = barChartAxisTicks(BarChartScale(maxPositiveCents = 50000L, maxNegativeMagnitudeCents = 10000L))

        assertEquals(listOf(50000L, 25000L, 0L, -5000L, -10000L), ticks)
    }

    @Test
    fun barChartAxisTicks_neverDuplicatesZeroLineForTinyRanges() {
        val ticks = barChartAxisTicks(BarChartScale(maxPositiveCents = 1L, maxNegativeMagnitudeCents = 1L))

        assertEquals(listOf(1L, 0L, -1L), ticks.distinct())
        assertEquals(1, ticks.count { it == 0L })
    }
}
