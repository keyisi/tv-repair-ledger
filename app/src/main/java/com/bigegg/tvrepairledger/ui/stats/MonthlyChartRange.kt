package com.bigegg.tvrepairledger.ui.stats

/**
 * 月度图表的时间范围。月数为 null 表示展示全部有记录的月份。
 */
enum class MonthlyChartRange(
    val label: String,
    val title: String,
    val monthCount: Int?
) {
    HalfYear(label = "6月", title = "最近 6 个月", monthCount = 6),
    Year(label = "12月", title = "最近 12 个月", monthCount = 12),
    All(label = "全部", title = "全部月份", monthCount = null);

    val isAll: Boolean get() = monthCount == null
}
