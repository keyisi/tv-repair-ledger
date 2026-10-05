package com.bigegg.tvrepairledger.importer

import com.bigegg.tvrepairledger.domain.parseMoneyToCents
import com.bigegg.tvrepairledger.domain.RepairRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportedRepairRow(
    val sourceRowNumber: Int,
    val dateEpochDay: Long,
    val customerName: String = "",
    val brand: String = "",
    val repairDevice: String = "",
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
    warrantyDays = warrantyDays,
    repairDevice = repairDevice,
    brand = brand
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
            brand = row.cell(mapping.brand),
            repairDevice = row.cell(mapping.repairDevice),
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
    val brand: Int,
    val repairDevice: Int,
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
        private const val MISSING = -1

        /**
         * 按表头名称识别每一列，中英文表头都支持，列顺序可以任意调整。
         * 本应用导出的表头（日期/客户姓名/品牌/联系电话/客户地址/维修设备/故障现象/维修项目/收费/零件费/利润/保修天数/备注）
         * 与旧版导出表头（日期/地址/配件/故障现象/收费/零件费/电话/备注/…）都能正确识别。
         */
        fun fromHeader(header: List<String?>): WorkbookMapping {
            val byName = WorkbookMapping(
                date = header.indexOfName("日期", "date"),
                customerName = header.indexOfName("客户姓名", "客户", "姓名", "customerName"),
                brand = header.indexOfName("品牌", "brand"),
                repairDevice = header.indexOfName("维修设备", "机型", "设备", "repairDevice"),
                address = header.indexOfName("客户地址", "地址", "address"),
                phone = header.indexOfName("联系电话", "电话", "phone"),
                faultSymptom = header.indexOfName("故障现象", "故障", "faultSymptom", "fault"),
                repairItem = header.indexOfName("维修项目", "配件", "项目", "repairItem", "part"),
                chargedAmount = header.indexOfName("收费", "收费金额", "chargedAmount", "charged"),
                partsCost = header.indexOfName("零件费", "配件成本", "partsCost", "cost"),
                notes = header.indexOfName("备注", "notes"),
                warrantyDays = header.indexOfName("保修天数", "warrantyDays")
            )

            // 日期与收费都认不出来时，退回旧版的固定列顺序，避免完全陌生的表头导致整表被跳过。
            return if (byName.date != MISSING && byName.chargedAmount != MISSING) byName else legacyPositional()
        }

        private fun legacyPositional() = WorkbookMapping(
            date = 0,
            customerName = MISSING,
            brand = MISSING,
            repairDevice = MISSING,
            address = 1,
            repairItem = 2,
            faultSymptom = 3,
            chargedAmount = 4,
            partsCost = 5,
            phone = 6,
            notes = 7,
            warrantyDays = MISSING
        )
    }
}

private fun List<String?>.indexOfName(vararg names: String): Int {
    forEachIndexed { index, cell ->
        val normalized = cell?.trim().orEmpty()
        if (normalized.isEmpty()) return@forEachIndexed
        if (names.any { it.equals(normalized, ignoreCase = true) }) return index
    }
    return -1
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
