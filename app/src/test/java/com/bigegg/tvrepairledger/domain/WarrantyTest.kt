package com.bigegg.tvrepairledger.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WarrantyTest {
    @Test
    fun remainingWarrantyDays_countsDownFromRepairDate() {
        val record = repairRecord(dateEpochDay = 100L, warrantyDays = 90)

        assertEquals(90, remainingWarrantyDays(record, todayEpochDay = 100L))
        assertEquals(1, remainingWarrantyDays(record, todayEpochDay = 189L))
        assertEquals(0, remainingWarrantyDays(record, todayEpochDay = 190L))
    }

    @Test
    fun isWarrantyExpired_isTrueAfterWarrantyWindow() {
        val record = repairRecord(dateEpochDay = 100L, warrantyDays = 90)

        assertFalse(isWarrantyExpired(record, todayEpochDay = 190L))
        assertTrue(isWarrantyExpired(record, todayEpochDay = 191L))
    }

    @Test
    fun warrantyStatus_splitsActiveExpiringAndExpired() {
        val record = repairRecord(dateEpochDay = 100L, warrantyDays = 90)

        assertEquals(WarrantyStatus.ACTIVE, warrantyStatus(record, todayEpochDay = 100L))
        assertEquals(WarrantyStatus.ACTIVE, warrantyStatus(record, todayEpochDay = 182L))
        assertEquals(WarrantyStatus.EXPIRING, warrantyStatus(record, todayEpochDay = 183L))
        assertEquals(WarrantyStatus.EXPIRING, warrantyStatus(record, todayEpochDay = 190L))
        assertEquals(WarrantyStatus.EXPIRED, warrantyStatus(record, todayEpochDay = 191L))
    }

    @Test
    fun warrantyStatus_thresholdIsSevenRemainingDays() {
        val record = repairRecord(dateEpochDay = 0L, warrantyDays = 30)

        assertEquals(7, WARRANTY_EXPIRING_THRESHOLD_DAYS)
        assertEquals(WarrantyStatus.ACTIVE, warrantyStatus(record, todayEpochDay = 22L))
        assertEquals(WarrantyStatus.EXPIRING, warrantyStatus(record, todayEpochDay = 23L))
    }

    @Test
    fun warrantyStatus_zeroDayWarrantyExpiresOnRepairDay() {
        val record = repairRecord(dateEpochDay = 0L, warrantyDays = 0)

        assertEquals(WarrantyStatus.EXPIRING, warrantyStatus(record, todayEpochDay = 0L))
        assertEquals(WarrantyStatus.EXPIRED, warrantyStatus(record, todayEpochDay = 1L))
    }

    private fun repairRecord(
        dateEpochDay: Long,
        warrantyDays: Int
    ): RepairRecord = RepairRecord(
        id = 1L,
        dateEpochDay = dateEpochDay,
        address = "",
        phone = "",
        faultSymptom = "",
        repairItem = "",
        chargedAmountCents = 0L,
        partsCostCents = null,
        notes = "",
        warrantyPeriod = "",
        createdAtMillis = 1L,
        updatedAtMillis = 1L,
        warrantyDays = warrantyDays
    )
}
