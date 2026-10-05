package com.bigegg.tvrepairledger.data

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class RepairRecordEntityMappingTest {
    @Test
    fun repairDevice_roundTripsThroughEntityMapping() {
        val record = RepairRecord(
            id = 1L,
            dateEpochDay = 20000L,
            address = "Address",
            phone = "180",
            faultSymptom = "黑屏",
            repairItem = "更换背光灯条",
            chargedAmountCents = 50000L,
            partsCostCents = 10000L,
            notes = "note",
            warrantyPeriod = "90天",
            createdAtMillis = 1L,
            updatedAtMillis = 2L,
            customerName = "张三",
            warrantyDays = 90,
            repairDevice = "液晶电视",
            brand = "小米"
        )

        val entity = record.toEntity()
        val decoded = entity.toDomain()

        assertEquals("液晶电视", entity.repairDevice)
        assertEquals("液晶电视", decoded.repairDevice)
        assertEquals("小米", entity.brand)
        assertEquals("小米", decoded.brand)
    }
}
