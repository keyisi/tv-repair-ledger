package com.bigegg.tvrepairledger.importer

import org.junit.Assert.assertEquals
import org.junit.Test
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.xlsx.repairRecordsToWorkbookRows

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

    @Test
    fun parseWorkbookRows_mapsBrandAndRepairDeviceFromOwnExportHeader() {
        val review = parseWorkbookRows(
            listOf(
                listOf(
                    "日期", "客户姓名", "品牌", "联系电话", "客户地址", "维修设备", "故障现象",
                    "维修项目", "收费", "零件费", "利润", "保修天数", "备注"
                ),
                listOf(
                    "2026-09-05", "张先生", "小米", "13800000000", "幸福小区 3栋", "液晶电视", "开机黑屏",
                    "更换背光灯条", "500", "80", "420", "120", "客户急用"
                )
            )
        )

        val row = review.readyRows.single()
        assertEquals("张先生", row.customerName)
        assertEquals("小米", row.brand)
        assertEquals("液晶电视", row.repairDevice)
        assertEquals("13800000000", row.phone)
        assertEquals("幸福小区 3栋", row.address)
        assertEquals("开机黑屏", row.faultSymptom)
        assertEquals("更换背光灯条", row.repairItem)
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(8000L, row.partsCostCents)
        assertEquals(120, row.warrantyDays)
        assertEquals("客户急用", row.notes)
    }

    @Test
    fun parseWorkbookRows_ignoresDerivedProfitColumn() {
        val review = parseWorkbookRows(
            listOf(
                listOf("日期", "收费", "零件费", "利润"),
                listOf("2026-09-05", "500", "80", "420")
            )
        )

        val row = review.readyRows.single()
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(8000L, row.partsCostCents)
        assertEquals("", row.notes)
    }

    @Test
    fun parseWorkbookRows_recognizesColumnsInAnyOrder() {
        val review = parseWorkbookRows(
            listOf(
                listOf("收费", "日期", "客户地址", "配件"),
                listOf("500", "2026-09-05", "幸福小区 3栋", "背光灯条")
            )
        )

        val row = review.readyRows.single()
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals("幸福小区 3栋", row.address)
        assertEquals("背光灯条", row.repairItem)
    }

    @Test
    fun parseWorkbookRows_fallsBackToFixedPositionsForUnknownHeader() {
        val review = parseWorkbookRows(
            listOf(
                listOf("A", "B", "C", "D", "E", "F", "G", "H"),
                listOf("2026-09-05", "幸福小区 3栋", "背光灯条", "开机黑屏", "500", "80", "13800000000", "客户急用")
            )
        )

        val row = review.readyRows.single()
        assertEquals("幸福小区 3栋", row.address)
        assertEquals("背光灯条", row.repairItem)
        assertEquals("开机黑屏", row.faultSymptom)
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(8000L, row.partsCostCents)
        assertEquals("13800000000", row.phone)
        assertEquals("客户急用", row.notes)
    }

    @Test
    fun exportedWorkbookRows_roundTripEveryBusinessFieldBackThroughImport() {
        val original = RepairRecord(
            id = 1L,
            dateEpochDay = java.time.LocalDate.of(2026, 9, 5).toEpochDay(),
            address = "幸福小区 3栋 1804",
            phone = "13800000000",
            faultSymptom = "开机黑屏、有声音",
            repairItem = "更换背光灯条",
            chargedAmountCents = 50000L,
            partsCostCents = 8000L,
            notes = "客户急用，已收定金",
            warrantyPeriod = "120天",
            createdAtMillis = 1L,
            updatedAtMillis = 2L,
            customerName = "张先生",
            warrantyDays = 120,
            repairDevice = "液晶电视",
            brand = "小米"
        )

        val exportedRows: List<List<String?>> = repairRecordsToWorkbookRows(listOf(original))
        val review = parseWorkbookRows(exportedRows)
        val imported = review.readyRows.single()

        assertEquals(0, review.skippedRows.size)
        assertEquals(0, review.needsDateConfirmation.size)
        assertEquals(
            original.copy(id = 0L),
            imported.toRepairRecord(createdAtMillis = 1L, updatedAtMillis = 2L)
        )
    }
}
