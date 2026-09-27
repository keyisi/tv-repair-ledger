package com.bigegg.tvrepairledger.xlsx

import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents
import java.time.LocalDate

val repairWorkbookHeader = listOf(
    "日期",
    "地址",
    "配件",
    "故障现象",
    "收费",
    "零件费",
    "电话",
    "备注",
    "",
    "合计",
    "零件费",
    "利润"
)

fun repairRecordsToWorkbookRows(records: List<RepairRecord>): List<List<String>> {
    return listOf(repairWorkbookHeader) + records
        .sortedWith(compareByDescending<RepairRecord> { it.dateEpochDay }.thenByDescending { it.id })
        .map { record ->
            listOf(
                LocalDate.ofEpochDay(record.dateEpochDay).toString(),
                record.address,
                record.repairItem,
                record.faultSymptom,
                formatCents(record.chargedAmountCents),
                record.partsCostCents?.let(::formatCents).orEmpty(),
                record.phone,
                record.notes,
                "",
                formatCents(record.chargedAmountCents),
                record.partsCostCents?.let(::formatCents).orEmpty(),
                formatCents(record.profitCents)
            )
        }
}
