package com.bigegg.tvrepairledger.ui.stats

import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import com.bigegg.tvrepairledger.domain.MonthlyRepairSummary
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class BarChartPointTest {
    @Test
    fun dailySummariesToChartPoints_usesMonthDayLabels() {
        val epochDay = LocalDate.of(2026, 9, 5).toEpochDay()
        val points = dailySummariesToChartPoints(
            listOf(
                DailyRepairSummary(
                    dateEpochDay = epochDay,
                    count = 1,
                    revenueCents = 50000L,
                    costCents = 5000L,
                    profitCents = 45000L
                )
            )
        )

        assertEquals("09-05", points.single().label)
        assertEquals(50000L, points.single().revenueCents)
        assertEquals(45000L, points.single().profitCents)
    }

    @Test
    fun monthlySummariesToChartPoints_usesYearMonthLabels() {
        val points = monthlySummariesToChartPoints(
            listOf(
                MonthlyRepairSummary(
                    year = 2026,
                    month = 1,
                    count = 2,
                    revenueCents = 80000L,
                    costCents = 2000L,
                    profitCents = 78000L
                )
            )
        )

        assertEquals("2026-01", points.single().label)
        assertEquals(80000L, points.single().revenueCents)
        assertEquals(78000L, points.single().profitCents)
    }

    @Test
    fun chartPoints_preserveOrder() {
        val points = monthlySummariesToChartPoints(
            listOf(
                MonthlyRepairSummary(2025, 12, 1, 1000L, 0L, 1000L),
                MonthlyRepairSummary(2026, 1, 1, 2000L, 0L, 2000L)
            )
        )

        assertEquals(listOf("2025-12", "2026-01"), points.map { it.label })
    }
}
