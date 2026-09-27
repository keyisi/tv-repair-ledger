package com.bigegg.tvrepairledger.importer

import com.bigegg.tvrepairledger.domain.parseMoneyToCents
import com.bigegg.tvrepairledger.domain.RepairRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportedRepairRow(
    val sourceRowNumber: Int,
    val dateEpochDay: Long,
    val customerName: String = "",
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyDays: Int = 90
)

data class DateConfirmationRow(
    val sourceRowNumber: Int,
    val rawDate: String,
    val address: String,
    val repairItem: String,
    val chargedAmountText: String
)

data class SkippedImportRow(
    val sourceRowNumber: Int,
    val reason: String
)

data class ImportReview(
    val readyRows: List<ImportedRepairRow>,
    val needsDateConfirmation: List<DateConfirmationRow>,
    val skippedRows: List<SkippedImportRow>
)

fun ImportedRepairRow.toRepairRecord(
    createdAtMillis: Long,
    updatedAtMillis: Long = createdAtMillis
): RepairRecord = RepairRecord(
    id = 0L,
    dateEpochDay = dateEpochDay,
    address = address,
    phone = phone,
    faultSymptom = faultSymptom,
    repairItem = repairItem,
    chargedAmountCents = chargedAmountCents,
    partsCostCents = partsCostCents,
    notes = notes,
    warrantyPeriod = "${warrantyDays}天",
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    customerName = customerName,
    warrantyDays = warrantyDays
)

fun parseWorkbookRows(rows: List<List<String?>>): ImportReview {
    val readyRows = mutableListOf<ImportedRepairRow>()
    val needsDateConfirmation = mutableListOf<DateConfirmationRow>()
    val skippedRows = mutableListOf<SkippedImportRow>()

    val mapping = WorkbookMapping.fromHeader(rows.firstOrNull().orEmpty())

    rows.drop(1).forEachIndexed { index, row ->
        val sourceRowNumber = index + 2
        val dateText = row.cell(mapping.date)
        val chargedAmountText = row.cell(mapping.chargedAmount)
        val partsCostText = row.cell(mapping.partsCost)
        val chargedAmountCents = parseMoneyToCents(chargedAmountText)
        val partsCostCents = if (partsCostText.isBlank()) null else parseMoneyToCents(partsCostText)
        val warrantyDays = row.cell(mapping.warrantyDays).toIntOrNull() ?: 90

        if (chargedAmountCents == null) {
            skippedRows += SkippedImportRow(
                sourceRowNumber = sourceRowNumber,
                reason = "Invalid charged amount: $chargedAmountText"
            )
            return@forEachIndexed
        }

        if (partsCostText.isNotBlank() && partsCostCents == null) {
            skippedRows += SkippedImportRow(
                sourceRowNumber = sourceRowNumber,
                reason = "Invalid parts cost: $partsCostText"
            )
            return@forEachIndexed
        }

        val parsedDate = parseImportDate(dateText)
        if (parsedDate == null) {
            if (looksLikeMissingYearDate(dateText)) {
                needsDateConfirmation += DateConfirmationRow(
                    sourceRowNumber = sourceRowNumber,
                    rawDate = dateText,
                    address = row.cell(mapping.address),
                    repairItem = normalizeRepairItem(row.cell(mapping.repairItem)),
                    chargedAmountText = chargedAmountText
                )
            } else {
                skippedRows += SkippedImportRow(
                    sourceRowNumber = sourceRowNumber,
                    reason = "Invalid date: $dateText"
                )
            }
            return@forEachIndexed
        }

        readyRows += ImportedRepairRow(
            sourceRowNumber = sourceRowNumber,
            dateEpochDay = parsedDate.toEpochDay(),
            customerName = row.cell(mapping.customerName),
            address = row.cell(mapping.address),
            phone = normalizePhone(row.cell(mapping.phone)),
            faultSymptom = row.cell(mapping.faultSymptom),
            repairItem = normalizeRepairItem(row.cell(mapping.repairItem)),
            chargedAmountCents = chargedAmountCents,
            partsCostCents = partsCostCents,
            notes = row.cell(mapping.notes),
            warrantyDays = warrantyDays
        )
    }

    return ImportReview(
        readyRows = readyRows,
        needsDateConfirmation = needsDateConfirmation,
        skippedRows = skippedRows
    )
}

private data class WorkbookMapping(
    val date: Int,
    val customerName: Int,
    val address: Int,
    val phone: Int,
    val faultSymptom: Int,
    val repairItem: Int,
    val chargedAmount: Int,
    val partsCost: Int,
    val notes: Int,
    val warrantyDays: Int
) {
    companion object {
        fun fromHeader(header: List<String?>): WorkbookMapping {
            val normalized = header.map { it?.trim().orEmpty() }
            val exported = normalized.any { it.equals("customerName", ignoreCase = true) || it == "客户姓名" }
            return if (exported) {
                WorkbookMapping(
                    date = 0,
                    customerName = 1,
                    address = 2,
                    phone = 3,
                    faultSymptom = 4,
                    repairItem = 5,
                    chargedAmount = 6,
                    partsCost = 7,
                    notes = 8,
                    warrantyDays = 9
                )
            } else {
                WorkbookMapping(
                    date = 0,
                    customerName = -1,
                    address = 1,
                    repairItem = 2,
                    faultSymptom = 3,
                    chargedAmount = 4,
                    partsCost = 5,
                    phone = 6,
                    notes = 7,
                    warrantyDays = -1
                )
            }
        }
    }
}

private fun List<String?>.cell(index: Int): String = getOrNull(index)?.trim().orEmpty()

private fun parseImportDate(text: String): LocalDate? {
    val normalized = text.trim().substringBefore(' ')
    if (normalized.isEmpty()) return null

    val formatters = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/M/d"),
        DateTimeFormatter.ofPattern("yyyy-M-d"),
        DateTimeFormatter.ofPattern("yyyy.M.d")
    )

    for (formatter in formatters) {
        runCatching { return LocalDate.parse(normalized, formatter) }
    }

    return null
}

private fun looksLikeMissingYearDate(text: String): Boolean {
    return text.trim().matches(Regex("""\d{1,2}\.\d{1,2}"""))
}

private fun normalizeRepairItem(text: String): String {
    return if (text.equals("emmc", ignoreCase = true)) "EMMC" else text
}

private fun normalizePhone(text: String): String {
    return text.removeSuffix(".0")
}
