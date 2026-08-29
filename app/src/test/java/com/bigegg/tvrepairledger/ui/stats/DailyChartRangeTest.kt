package com.bigegg.tvrepairledger.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyChartRangeTest {
    @Test
    fun ranges_useTheRequiredLabelsAndDayCounts() {
        assertEquals("1周", DailyChartRange.Week.label)
        assertEquals(7, DailyChartRange.Week.dayCount)
        assertEquals("1月", DailyChartRange.Month.label)
        assertEquals(30, DailyChartRange.Month.dayCount)
    }
}
