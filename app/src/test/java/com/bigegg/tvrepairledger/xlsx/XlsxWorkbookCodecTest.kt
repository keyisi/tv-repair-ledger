package com.bigegg.tvrepairledger.xlsx

import org.junit.Assert.assertEquals
import org.junit.Test
import com.bigegg.tvrepairledger.domain.RepairRecord
import java.io.ByteArrayInputStream

class XlsxWorkbookCodecTest {
    @Test
    fun writeThenReadWorkbook_preservesRowsAndCells() {
        val rows = listOf(
            listOf("date", "customerName", "address", "charged"),
            listOf("2026-07-07", "张三", "Building 3, Room 1804", "500.00"),
            listOf("2026-07-08", "李四", "contains \"quote\"", "260.00")
        )

        val bytes = writeWorkbook(rows)
        val decoded = readWorkbookRows(ByteArrayInputStream(bytes))

        assertEquals(rows, decoded)
    }

    @Test
    fun readWorkbookRows_preservesBlankCellsBetweenValues() {
        val rows = listOf(
            listOf("A", "B", "C"),
            listOf("left", "", "right")
        )

        val decoded = readWorkbookRows(ByteArrayInputStream(writeWorkbook(rows)))

        assertEquals("left", decoded[1][0])
        assertEquals("", decoded[1][1])
        assertEquals("right", decoded[1][2])
    }

    @Test
    fun repairRecordsToWorkbookRows_exportsEveryBusinessColumn() {
        val rows = repairRecordsToWorkbookRows(
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
                    warrantyPeriod = "120天",
                    createdAtMillis = 1L,
                    updatedAtMillis = 1L,
                    customerName = "张三",
                    warrantyDays = 120,
                    repairDevice = "液晶电视",
                    brand = "小米"
                )
            )
        )

        assertEquals(
            listOf(
                "日期", "客户姓名", "品牌", "联系电话", "客户地址", "维修设备", "故障现象",
                "维修项目", "收费", "零件费", "利润", "保修天数", "备注"
            ),
            rows.first()
        )
        assertEquals(
            listOf(
                "2024-03-28", "张三", "小米", "180", "Address", "液晶电视", "black screen",
                "lamp", "500.00", "50.00", "450.00", "120", "note"
            ),
            rows[1]
        )
    }
}
