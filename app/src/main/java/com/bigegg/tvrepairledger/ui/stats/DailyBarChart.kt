package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import com.bigegg.tvrepairledger.ui.theme.DangerRed
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.LineSoft
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import com.bigegg.tvrepairledger.ui.theme.RepairBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class DailyChartScale(
    val maxPositiveCents: Long,
    val maxNegativeMagnitudeCents: Long
)

fun calculateDailyChartScale(summaries: List<DailyRepairSummary>): DailyChartScale {
    return DailyChartScale(
        maxPositiveCents = summaries.maxOfOrNull {
            maxOf(it.revenueCents, it.profitCents.coerceAtLeast(0L))
        }?.coerceAtLeast(1L) ?: 1L,
        maxNegativeMagnitudeCents = summaries.minOfOrNull {
            it.profitCents.coerceAtMost(0L)
        }?.let(::abs) ?: 0L
    )
}

@Composable
fun DailyBarChart(
    summaries: List<DailyRepairSummary>,
    modifier: Modifier = Modifier,
    minimumDayWidth: Dp = 0.dp
) {
    val scale = calculateDailyChartScale(summaries)
    val totalRange = scale.maxPositiveCents + scale.maxNegativeMagnitudeCents
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")
    val scrollState = rememberScrollState()
    val rangeKey = summaries.firstOrNull()?.dateEpochDay to summaries.lastOrNull()?.dateEpochDay

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val chartWidth = maxOf(maxWidth, minimumDayWidth * summaries.size)
            val maxScroll = scrollState.maxValue

            LaunchedEffect(rangeKey, maxScroll) {
                if (maxScroll > 0) scrollState.scrollTo(maxScroll)
            }

            Column(
                modifier = Modifier
                    .horizontalScroll(scrollState)
                    .width(chartWidth),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val zeroY = size.height * scale.maxPositiveCents.toFloat() / totalRange.toFloat()
                    drawLine(LineSoft, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.dp.toPx())
                    val groupWidth = size.width / summaries.size.coerceAtLeast(1)
                    val barWidth = (groupWidth * 0.25f).coerceAtLeast(4.dp.toPx())

                    summaries.forEachIndexed { index, summary ->
                        val centerX = groupWidth * (index + 0.5f)
                        val revenueHeight = zeroY * summary.revenueCents.toFloat() /
                            scale.maxPositiveCents.toFloat()
                        drawRect(
                            color = RepairBlue,
                            topLeft = Offset(centerX - barWidth - 1.dp.toPx(), zeroY - revenueHeight),
                            size = Size(barWidth, revenueHeight)
                        )

                        val profit = summary.profitCents
                        val profitHeight = if (profit >= 0L) {
                            zeroY * profit.toFloat() / scale.maxPositiveCents.toFloat()
                        } else {
                            (size.height - zeroY) * abs(profit).toFloat() /
                                scale.maxNegativeMagnitudeCents.coerceAtLeast(1L).toFloat()
                        }
                        drawRect(
                            color = if (profit < 0L) DangerRed else ProfitGreen,
                            topLeft = Offset(centerX + 1.dp.toPx(), if (profit < 0L) zeroY else zeroY - profitHeight),
                            size = Size(barWidth, profitHeight)
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    summaries.forEach { summary ->
                        Text(
                            text = LocalDate.ofEpochDay(summary.dateEpochDay).format(dateFormatter),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelSmall,
                            color = Ink500,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartLegendItem("收入", RepairBlue)
            ChartLegendItem("利润", ProfitGreen)
            if (summaries.any { it.profitCents < 0L }) ChartLegendItem("亏损", DangerRed)
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Spacer(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Ink500, fontWeight = FontWeight.Medium)
    }
}
