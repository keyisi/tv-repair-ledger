package com.bigegg.tvrepairledger.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class RepairStatsTest {
    private fun record(
        id: Long,
        dateEpochDay: Long,
        repairItem: String,
        fault: String,
        charged: Long,
        cost: Long?
    ) = RepairRecord(
        id = id,
        dateEpochDay = dateEpochDay,
        address = "",
        phone = "",
        faultSymptom = fault,
        repairItem = repairItem,
        chargedAmountCents = charged,
        partsCostCents = cost,
        notes = "",
        warrantyPeriod = "",
        createdAtMillis = 1,
        updatedAtMillis = 1
    )

    @Test
    fun summarizeByMonth_groupsAndSortsNewestFirst() {
        val newestDate = java.time.LocalDate.ofEpochDay(19850)
        val summaries = summarizeByMonth(
            listOf(
                record(1, 19810, "lamp", "black-screen", 50000, 5000),
                record(2, 19811, "lamp", "black-screen", 35000, null),
                record(3, 19850, "EMMC", "boot-logo", 30000, 2000)
            )
        )

        assertEquals(2, summaries.size)
        assertEquals(newestDate.year, summaries.first().year)
        assertEquals(newestDate.monthValue, summaries.first().month)
        assertEquals(1, summaries.first().count)
        assertEquals(30000L, summaries.first().revenueCents)
        assertEquals(2000L, summaries.first().costCents)
        assertEquals(28000L, summaries.first().profitCents)
    }

    @Test
    fun summarizeByDay_groupsMoneyAndSortsNewestFirst() {
        val summaries = summarizeByDay(
            listOf(
                record(1, 19810, "lamp", "black-screen", 50000, 5000),
                record(2, 19810, "lamp", "black-screen", 35000, null),
                record(3, 19811, "EMMC", "boot-logo", 30000, 40000)
            )
        )

        assertEquals(listOf(19811L, 19810L), summaries.map { it.dateEpochDay })
        assertEquals(1, summaries[0].count)
        assertEquals(30000L, summaries[0].revenueCents)
        assertEquals(40000L, summaries[0].costCents)
        assertEquals(-10000L, summaries[0].profitCents)
        assertEquals(2, summaries[1].count)
        assertEquals(85000L, summaries[1].revenueCents)
        assertEquals(5000L, summaries[1].costCents)
        assertEquals(80000L, summaries[1].profitCents)
    }

    @Test
    fun summarizeRecentDays_returnsAscendingContinuousDaysAndFillsMissingDates() {
        val summaries = summarizeRecentDays(
            records = listOf(
                record(1, 100, "lamp", "black-screen", 50000, 5000),
                record(2, 94, "EMMC", "boot-logo", 30000, 40000),
                record(3, 93, "old", "old", 99999, null)
            ),
            endDateEpochDay = 100,
            dayCount = 7
        )

        assertEquals((94L..100L).toList(), summaries.map { it.dateEpochDay })
        assertEquals(-10000L, summaries.first().profitCents)
        assertEquals(0, summaries[1].count)
        assertEquals(0L, summaries[1].revenueCents)
        assertEquals(45000L, summaries.last().profitCents)
    }

    @Test
    fun summarizeRecentDays_supportsThirtyContinuousDaysWithBoundaryRecords() {
        val summaries = summarizeRecentDays(
            records = listOf(
                record(1, 171, "lamp", "black-screen", 50000, 5000),
                record(2, 200, "EMMC", "boot-logo", 30000, 2000),
                record(3, 170, "old", "old", 99999, null)
            ),
            endDateEpochDay = 200,
            dayCount = 30
        )

        assertEquals(30, summaries.size)
        assertEquals((171L..200L).toList(), summaries.map { it.dateEpochDay })
        assertEquals(45000L, summaries.first().profitCents)
        assertEquals(0, summaries[1].count)
        assertEquals(28000L, summaries.last().profitCents)
    }

    @Test
    fun summarizeByRepairItem_groupsRevenueCostProfitAndCount() {
        val summaries = summarizeByRepairItem(
            listOf(
                record(1, 19810, "lamp", "black-screen", 50000, 5000),
                record(2, 19811, "lamp", "black-screen", 35000, null),
                record(3, 19812, "EMMC", "boot-logo", 30000, 2000)
            )
        )

        val lamp = summaries.first { it.label == "lamp" }
        assertEquals(2, lamp.count)
        assertEquals(85000L, lamp.revenueCents)
        assertEquals(5000L, lamp.costCents)
        assertEquals(80000L, lamp.profitCents)
    }

    @Test
    fun summarizeByFault_sortsByProfitDescending() {
        val summaries = summarizeByFault(
            listOf(
                record(1, 19810, "lamp", "black-screen", 50000, 5000),
                record(2, 19811, "EMMC", "boot-logo", 30000, 2000)
            )
        )

        assertEquals("black-screen", summaries.first().label)
    }

    @Test
    fun summarizeByCategory_usesFallbackForBlankLabels() {
        val summaries = summarizeByRepairItem(
            listOf(record(1, 19810, "", "", 50000, null))
        )

        assertEquals("\u672a\u586b\u5199", summaries.single().label)
    }

    @Test
    fun summariesByMonthAscending_sortsOldestFirst() {
        val summaries = summariesByMonthAscending(
            listOf(
                record(1, LocalDate.of(2024, 6, 3).toEpochDay(), "lamp", "black-screen", 100, null),
                record(2, LocalDate.of(2024, 1, 3).toEpochDay(), "lamp", "black-screen", 200, null),
                record(3, LocalDate.of(2023, 12, 3).toEpochDay(), "lamp", "black-screen", 300, null)
            )
        )

        assertEquals(
            listOf("2023-12", "2024-01", "2024-06"),
            summaries.map { YearMonth.of(it.year, it.month).toString() }
        )
    }

    @Test
    fun summarizeRecentMonths_returnsAscendingContinuousMonthsAndFillsMissingMonths() {
        val summaries = summarizeRecentMonths(
            records = listOf(
                record(1, LocalDate.of(2024, 3, 28).toEpochDay(), "lamp", "black-screen", 50000, 5000),
                record(2, LocalDate.of(2024, 5, 10).toEpochDay(), "EMMC", "boot-logo", 30000, 2000),
                record(3, LocalDate.of(2024, 6, 3).toEpochDay(), "lamp", "black-screen", 80000, null)
            ),
            endYearMonth = YearMonth.of(2024, 6),
            monthCount = 4
        )

        assertEquals(
            listOf("2024-03", "2024-04", "2024-05", "2024-06"),
            summaries.map { YearMonth.of(it.year, it.month).toString() }
        )
        assertEquals(45000L, summaries.first().profitCents)
        assertEquals(0, summaries[1].count)
        assertEquals(0L, summaries[1].revenueCents)
        assertEquals(0L, summaries[1].costCents)
        assertEquals(28000L, summaries[2].profitCents)
        assertEquals(80000L, summaries.last().profitCents)
    }

    @Test
    fun summarizeRecentMonths_crossesYearBoundaryWithZeroFilledMonths() {
        val summaries = summarizeRecentMonths(
            records = emptyList(),
            endYearMonth = YearMonth.of(2026, 2),
            monthCount = 3
        )

        assertEquals(
            listOf("2025-12", "2026-01", "2026-02"),
            summaries.map { YearMonth.of(it.year, it.month).toString() }
        )
        assertEquals(0, summaries.sumOf { it.count })
        assertEquals(0L, summaries.sumOf { it.profitCents })
    }
}
