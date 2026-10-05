package com.bigegg.tvrepairledger.domain

import java.time.LocalDate
import java.time.YearMonth

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

/**
 * 全部月度汇总，按时间从旧到新排列，供月度图表从左到右绘制。
 */
fun summariesByMonthAscending(records: List<RepairRecord>): List<MonthlyRepairSummary> {
    return summarizeByMonth(records).sortedWith(
        compareBy<MonthlyRepairSummary> { it.year }.thenBy { it.month }
    )
}

/**
 * 以 [endYearMonth] 为最后一个月，生成连续 [monthCount] 个月的月度汇总，缺少记录的月份补零。
 * 与按日图表补零保持一致，便于观察月度趋势和空档。
 */
fun summarizeRecentMonths(
    records: List<RepairRecord>,
    endYearMonth: YearMonth,
    monthCount: Int
): List<MonthlyRepairSummary> {
    require(monthCount > 0) { "monthCount must be positive" }
    val summariesByMonth = summarizeByMonth(records).associateBy { YearMonth.of(it.year, it.month) }
    val startYearMonth = endYearMonth.minusMonths((monthCount - 1).toLong())
    return (0 until monthCount).map { offset ->
        val yearMonth = startYearMonth.plusMonths(offset.toLong())
        summariesByMonth[yearMonth] ?: MonthlyRepairSummary(
            year = yearMonth.year,
            month = yearMonth.monthValue,
            count = 0,
            revenueCents = 0L,
            costCents = 0L,
            profitCents = 0L
        )
    }
}

fun summarizeByRepairItem(records: List<RepairRecord>): List<CategoryRepairSummary> {
    return summarizeByCategory(records) { it.repairItem.ifBlank { "未填写" } }
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
