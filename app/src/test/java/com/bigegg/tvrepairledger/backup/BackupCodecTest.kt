package com.bigegg.tvrepairledger.backup

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCodecTest {
    @Test
    fun backupRoundTrip_preservesAllFieldsAndNullPartsCost() {
        val record = RepairRecord(
            id = 7L,
            dateEpochDay = 19810L,
            address = "Building 3, Room 1804",
            phone = "18000000000",
            faultSymptom = "black screen",
            repairItem = "lamp",
            chargedAmountCents = 50000L,
            partsCostCents = null,
            notes = "customer said \"urgent\"",
            warrantyPeriod = "90 days",
            createdAtMillis = 1000L,
            updatedAtMillis = 2000L,
            customerName = "张三",
            warrantyDays = 120,
            repairDevice = "液晶电视"
        )

        val decoded = decodeBackup(encodeBackup(listOf(record)))

        assertEquals(listOf(record), decoded)
        assertEquals(null, decoded.single().partsCostCents)
        assertEquals("张三", decoded.single().customerName)
        assertEquals(120, decoded.single().warrantyDays)
        assertEquals("液晶电视", decoded.single().repairDevice)
    }

    @Test
    fun backupRoundTrip_preservesNonNullPartsCost() {
        val record = RepairRecord(
            id = 8L,
            dateEpochDay = 19811L,
            address = "Address",
            phone = "",
            faultSymptom = "no sound",
            repairItem = "audio board",
            chargedAmountCents = 26000L,
            partsCostCents = 4000L,
            notes = "",
            warrantyPeriod = "30 days",
            createdAtMillis = 3000L,
            updatedAtMillis = 4000L
        )

        assertEquals(record, decodeBackup(encodeBackup(listOf(record))).single())
    }
}
