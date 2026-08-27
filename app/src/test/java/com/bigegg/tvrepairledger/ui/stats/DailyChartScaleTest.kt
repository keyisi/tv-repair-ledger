package com.bigegg.tvrepairledger.ui.stats

import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyChartScaleTest {
    private fun summary(revenue: Long, profit: Long) = DailyRepairSummary(
        dateEpochDay = 1,
        count = 1,
        revenueCents = revenue,
        costCents = revenue - profit,
        profitCents = profit
    )

    @Test
    fun calculateDailyChartScale_coversRevenuePositiveAndNegativeProfit() {
        val scale = calculateDailyChartScale(
            listOf(summary(50000, 45000), summary(30000, -10000))
        )

        assertEquals(50000L, scale.maxPositiveCents)
        assertEquals(10000L, scale.maxNegativeMagnitudeCents)
    }

    @Test
    fun calculateDailyChartScale_usesStableRangeForAllZeroValues() {
        val scale = calculateDailyChartScale(listOf(summary(0, 0)))

        assertEquals(1L, scale.maxPositiveCents)
        assertEquals(0L, scale.maxNegativeMagnitudeCents)
    }
}
