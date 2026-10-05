package com.bigegg.tvrepairledger.xlsx

import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents
import java.time.LocalDate

/**
 * 导出表头是自描述的：导入时按表头名称识别列，不再依赖固定列位置。
 * 这里列出记录的全部业务字段，保证导出后再导入不丢数据。
 */
val repairWorkbookHeader = listOf(
    "日期",
    "客户姓名",
    "品牌",
    "联系电话",
    "客户地址",
    "维修设备",
    "故障现象",
    "维修项目",
    "收费",
    "零件费",
    "利润",
    "保修天数",
    "备注"
)

fun repairRecordsToWorkbookRows(records: List<RepairRecord>): List<List<String>> {
    return listOf(repairWorkbookHeader) + records
        .sortedWith(compareByDescending<RepairRecord> { it.dateEpochDay }.thenByDescending { it.id })
        .map { record ->
            listOf(
                LocalDate.ofEpochDay(record.dateEpochDay).toString(),
                record.customerName,
                record.brand,
                record.phone,
                record.address,
                record.repairDevice,
                record.faultSymptom,
                record.repairItem,
                formatCents(record.chargedAmountCents),
                record.partsCostCents?.let(::formatCents).orEmpty(),
                formatCents(record.profitCents),
                record.warrantyDays.toString(),
                record.notes
            )
        }
}
