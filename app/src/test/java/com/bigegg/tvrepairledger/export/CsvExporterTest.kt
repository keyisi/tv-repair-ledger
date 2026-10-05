package com.bigegg.tvrepairledger.export

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    @Test
    fun exportCsv_containsHeadersAndDerivedProfit() {
        val csv = exportCsv(
            listOf(
                RepairRecord(
                    id = 1L,
                    dateEpochDay = 19810L,
                    address = "Address",
                    phone = "180",
                    faultSymptom = "black screen",
                    repairItem = "lamp",
                    chargedAmountCents = 50000L,
                    partsCostCents = 5000L,
                    notes = "note",
                    warrantyPeriod = "",
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L
                )
            )
        )

        assertTrue(csv.contains("date,customerName,brand,address,phone,faultSymptom,repairItem,chargedAmount,partsCost,profit,notes,warrantyDays"))
        assertTrue(csv.contains("450.00"))
    }

    @Test
    fun exportCsv_escapesCommasQuotesAndNewlines() {
        val csv = exportCsv(
            listOf(
                RepairRecord(
                    id = 1L,
                    dateEpochDay = 19810L,
                    address = "Building 3, Room \"1804\"",
                    phone = "180",
                    faultSymptom = "black\nscreen",
                    repairItem = "lamp",
                    chargedAmountCents = 50000L,
                    partsCostCents = null,
                    notes = "note",
                    warrantyPeriod = "",
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L,
                    customerName = "Li",
                    brand = "小米"
                )
            )
        )

        val lines = csv.lines()
        assertEquals("date,customerName,brand,address,phone,faultSymptom,repairItem,chargedAmount,partsCost,profit,notes,warrantyDays", lines.first())
        assertTrue(csv.contains(",Li,小米,"))
        assertTrue(csv.contains("\"Building 3, Room \"\"1804\"\"\""))
        assertTrue(csv.contains("\"black\nscreen\""))
        assertTrue(csv.contains(",500.00,,500.00,"))
    }
}
