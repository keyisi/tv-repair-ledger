package com.bigegg.tvrepairledger.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class MonthlyChartRangeTest {
    @Test
    fun ranges_useTheRequiredLabelsTitlesAndMonthCounts() {
        assertEquals("6月", MonthlyChartRange.HalfYear.label)
        assertEquals("最近 6 个月", MonthlyChartRange.HalfYear.title)
        assertEquals(6, MonthlyChartRange.HalfYear.monthCount)

        assertEquals("12月", MonthlyChartRange.Year.label)
        assertEquals("最近 12 个月", MonthlyChartRange.Year.title)
        assertEquals(12, MonthlyChartRange.Year.monthCount)
    }

    @Test
    fun allRange_hasNoMonthLimit() {
        assertEquals("全部", MonthlyChartRange.All.label)
        assertEquals("全部月份", MonthlyChartRange.All.title)
        assertEquals(null, MonthlyChartRange.All.monthCount)
        assertEquals(true, MonthlyChartRange.All.isAll)
        assertEquals(false, MonthlyChartRange.HalfYear.isAll)
    }

    @Test
    fun ranges_areDeclaredInSegmentButtonOrder() {
        assertEquals(
            listOf("6月", "12月", "全部"),
            MonthlyChartRange.entries.map { it.label }
        )
    }
}
