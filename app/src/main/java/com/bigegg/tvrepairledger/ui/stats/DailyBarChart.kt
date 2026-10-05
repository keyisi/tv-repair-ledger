package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dailyLabelFormatter = DateTimeFormatter.ofPattern("MM-dd")

fun dailySummariesToChartPoints(summaries: List<DailyRepairSummary>): List<BarChartPoint> {
    return summaries.map { summary ->
        BarChartPoint(
            label = LocalDate.ofEpochDay(summary.dateEpochDay).format(dailyLabelFormatter),
            revenueCents = summary.revenueCents,
            profitCents = summary.profitCents
        )
    }
}

@Composable
fun DailyBarChart(
    summaries: List<DailyRepairSummary>,
    modifier: Modifier = Modifier,
    minimumDayWidth: Dp = 0.dp
) {
    LedgerBarChart(
        points = dailySummariesToChartPoints(summaries),
        modifier = modifier,
        minimumBarWidth = minimumDayWidth
    )
}
