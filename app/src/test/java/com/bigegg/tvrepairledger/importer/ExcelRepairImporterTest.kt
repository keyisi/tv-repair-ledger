package com.bigegg.tvrepairledger.importer

import org.junit.Assert.assertEquals
import org.junit.Test

class ExcelRepairImporterTest {
    @Test
    fun parseWorkbookRows_mapsColumnsAThroughH() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("2026-07-06", "Building 3-1804", "lamp", "black screen", "500", "50", "18000000000", "TCL65")
            )
        )

        assertEquals(1, review.readyRows.size)
        val row = review.readyRows.single()
        assertEquals("Building 3-1804", row.address)
        assertEquals("lamp", row.repairItem)
        assertEquals("black screen", row.faultSymptom)
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(5000L, row.partsCostCents)
        assertEquals("18000000000", row.phone)
        assertEquals("TCL65", row.notes)
    }

    @Test
    fun parseWorkbookRows_flagsMissingYearDate() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("12.27", "New Garden 3-603", "screen repair", "gray screen", "450", "", "", "deposit")
            )
        )

        assertEquals(0, review.readyRows.size)
        assertEquals(1, review.needsDateConfirmation.size)
        assertEquals("12.27", review.needsDateConfirmation.single().rawDate)
    }

    @Test
    fun parseWorkbookRows_normalizesEmmcCase() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("2022-01-28", "Address", "emmc", "stuck logo", "300", "20", "", "")
            )
        )

        assertEquals("EMMC", review.readyRows.single().repairItem)
    }

    @Test
    fun parseWorkbookRows_treatsBlankPartsCostAsNull() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("2026/7/6", "Address", "lamp", "black screen", "500", "", "", "")
            )
        )

        assertEquals(null, review.readyRows.single().partsCostCents)
    }

    @Test
    fun parseWorkbookRows_mapsExportedWorkbookColumns() {
        val review = parseWorkbookRows(
            listOf(
                listOf(
                    "date",
                    "customerName",
                    "address",
                    "phone",
                    "faultSymptom",
                    "repairItem",
                    "chargedAmount",
                    "partsCost",
                    "notes",
                    "warrantyDays"
                ),
                listOf(
                    "2026-07-06",
                    "张三",
                    "Address",
                    "18000000000",
                    "black screen",
                    "lamp",
                    "500",
                    "",
                    "note",
                    "120"
                )
            )
        )

        val row = review.readyRows.single()
        assertEquals("张三", row.customerName)
        assertEquals("Address", row.address)
        assertEquals("18000000000", row.phone)
        assertEquals(null, row.partsCostCents)
        assertEquals(120, row.warrantyDays)
    }

    @Test
    fun parseWorkbookRows_mapsChineseLedgerWorkbookColumns() {
        val review = parseWorkbookRows(
            listOf(
                listOf("日期", "地址", "配件", "故障现象", "收费", "零件费", "电话", "备注", "", "合计", "零件费", "利润"),
                listOf("2026-07-06", "幸福小区 3栋", "背光灯条", "黑屏", "500", "80", "18000000000", "客户急用", "", "500", "80", "420")
            )
        )

        val row = review.readyRows.single()
        assertEquals("幸福小区 3栋", row.address)
        assertEquals("背光灯条", row.repairItem)
        assertEquals("黑屏", row.faultSymptom)
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(8000L, row.partsCostCents)
        assertEquals("18000000000", row.phone)
        assertEquals("客户急用", row.notes)
    }

    @Test
    fun parseWorkbookRows_skipsInvalidChargedAmount() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("2026-07-06", "Address", "lamp", "black screen", "abc", "", "", "")
            )
        )

        assertEquals(0, review.readyRows.size)
        assertEquals(1, review.skippedRows.size)
    }

    @Test
    fun parseWorkbookRows_skipsInvalidNonblankPartsCost() {
        val review = parseWorkbookRows(
            listOf(
                listOf("date", "address", "part", "fault", "charged", "cost", "phone", "notes"),
                listOf("2026-07-06", "Address", "lamp", "black screen", "500", "50O", "", "")
            )
        )

        assertEquals(0, review.readyRows.size)
        assertEquals(1, review.skippedRows.size)
    }
}
