package com.bigegg.tvrepairledger.domain

import org.junit.Assert.assertEquals
import org.junit.Test

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
}
