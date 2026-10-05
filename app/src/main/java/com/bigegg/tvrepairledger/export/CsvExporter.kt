package com.bigegg.tvrepairledger.export

import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents
import java.time.LocalDate

fun exportCsv(records: List<RepairRecord>): String {
    val header = "date,customerName,brand,address,phone,faultSymptom,repairItem,chargedAmount,partsCost,profit,notes,warrantyDays"
    val rows = records.map { record ->
        listOf(
            LocalDate.ofEpochDay(record.dateEpochDay).toString(),
            record.customerName,
            record.brand,
            record.address,
            record.phone,
            record.faultSymptom,
            record.repairItem,
            formatCents(record.chargedAmountCents),
            record.partsCostCents?.let(::formatCents).orEmpty(),
            formatCents(record.profitCents),
            record.notes,
            record.warrantyDays.toString()
        ).joinToString(",") { it.csvEscape() }
    }

    return (listOf(header) + rows).joinToString("\n")
}

private fun String.csvEscape(): String {
    val needsQuotes = contains(",") || contains("\"") || contains("\n") || contains("\r")
    val escaped = replace("\"", "\"\"")
    return if (needsQuotes) "\"$escaped\"" else escaped
}
