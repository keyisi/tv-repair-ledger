package com.bigegg.tvrepairledger.domain

import java.time.LocalDate

data class DailyRepairSummary(
    val dateEpochDay: Long,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

data class MonthlyRepairSummary(
    val year: Int,
    val month: Int,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

fun summarizeByDay(records: List<RepairRecord>): List<DailyRepairSummary> {
    return records
        .groupBy { it.dateEpochDay }
        .map { (dateEpochDay, group) ->
            DailyRepairSummary(
                dateEpochDay = dateEpochDay,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedByDescending { it.dateEpochDay }
}

fun summarizeRecentDays(
    records: List<RepairRecord>,
    endDateEpochDay: Long,
    dayCount: Int = 7
): List<DailyRepairSummary> {
    require(dayCount > 0) { "dayCount must be positive" }
    val summariesByDate = summarizeByDay(records).associateBy { it.dateEpochDay }
    val startDateEpochDay = endDateEpochDay - dayCount + 1
    return (startDateEpochDay..endDateEpochDay).map { dateEpochDay ->
        summariesByDate[dateEpochDay] ?: DailyRepairSummary(
            dateEpochDay = dateEpochDay,
            count = 0,
            revenueCents = 0L,
            costCents = 0L,
            profitCents = 0L
        )
    }
}

data class CategoryRepairSummary(
    val label: String,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

fun summarizeByMonth(records: List<RepairRecord>): List<MonthlyRepairSummary> {
    return records
        .groupBy { record ->
            val date = LocalDate.ofEpochDay(record.dateEpochDay)
            date.year to date.monthValue
        }
        .map { (yearMonth, group) ->
            MonthlyRepairSummary(
                year = yearMonth.first,
                month = yearMonth.second,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedWith(
            compareByDescending<MonthlyRepairSummary> { it.year }
                .thenByDescending { it.month }
        )
}

fun summarizeByRepairItem(records: List<RepairRecord>): List<CategoryRepairSummary> {
    return summarizeByCategory(records) { it.repairItem.ifBlank { "\u672a\u586b\u5199" } }
}

fun summarizeByFault(records: List<RepairRecord>): List<CategoryRepairSummary> {
    return summarizeByCategory(records) { it.faultSymptom.ifBlank { "\u672a\u586b\u5199" } }
}

private fun summarizeByCategory(
    records: List<RepairRecord>,
    labelSelector: (RepairRecord) -> String
): List<CategoryRepairSummary> {
    return records
        .groupBy(labelSelector)
        .map { (label, group) ->
            CategoryRepairSummary(
                label = label,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedWith(
            compareByDescending<CategoryRepairSummary> { it.profitCents }
                .thenBy { it.label }
        )
}
