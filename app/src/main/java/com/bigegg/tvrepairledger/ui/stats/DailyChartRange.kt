package com.bigegg.tvrepairledger.ui.stats

enum class DailyChartRange(
    val label: String,
    val dayCount: Int
) {
    Week(label = "1周", dayCount = 7),
    Month(label = "1月", dayCount = 30)
}
