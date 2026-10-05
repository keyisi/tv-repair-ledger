package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.MonthlyRepairSummary
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val monthLabelFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

fun monthlySummariesToChartPoints(summaries: List<MonthlyRepairSummary>): List<BarChartPoint> {
    return summaries.map { summary ->
        BarChartPoint(
            label = YearMonth.of(summary.year, summary.month).format(monthLabelFormatter),
            revenueCents = summary.revenueCents,
            profitCents = summary.profitCents
        )
    }
}

@Composable
fun MonthlyBarChart(
    summaries: List<MonthlyRepairSummary>,
    modifier: Modifier = Modifier,
    minimumMonthWidth: Dp = 56.dp
) {
    LedgerBarChart(
        points = monthlySummariesToChartPoints(summaries),
        modifier = modifier,
        minimumBarWidth = minimumMonthWidth
    )
}
