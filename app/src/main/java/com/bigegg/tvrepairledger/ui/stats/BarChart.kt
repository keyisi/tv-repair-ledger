package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.formatAxisMoney
import com.bigegg.tvrepairledger.ui.theme.DangerRed
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.LineSoft
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import com.bigegg.tvrepairledger.ui.theme.RepairBlue
import kotlin.math.abs

/** 图表绘图区高度。 */
private val chartHeight = 190.dp

/** Y 轴刻度标签占用的左侧固定宽度，柱体从这条线右边开始画。 */
private val axisGutter = 52.dp

/**
 * 柱状图的一个数据点：一个时间分组（一天或一个月）的收入与利润。
 * 标签由调用方格式化，图表本身不关心分组粒度。
 */
data class BarChartPoint(
    val label: String,
    val revenueCents: Long,
    val profitCents: Long
)

data class BarChartScale(
    val maxPositiveCents: Long,
    val maxNegativeMagnitudeCents: Long
)

fun calculateBarChartScale(points: List<BarChartPoint>): BarChartScale {
    return BarChartScale(
        maxPositiveCents = points.maxOfOrNull {
            maxOf(it.revenueCents, it.profitCents.coerceAtLeast(0L))
        }?.coerceAtLeast(1L) ?: 1L,
        maxNegativeMagnitudeCents = points.minOfOrNull {
            it.profitCents.coerceAtMost(0L)
        }?.let(::abs) ?: 0L
    )
}

/**
 * Y 轴刻度值（单位：分）：正值区取最大值与中值，加零线，亏损时再补负值区两档。
 * 抽成纯函数方便单测，也保证刻度不会因为除不尽而画出重复的一条线。
 */
fun barChartAxisTicks(scale: BarChartScale): List<Long> {
    val ticks = mutableListOf<Long>()
    ticks += scale.maxPositiveCents
    val halfPositive = scale.maxPositiveCents / 2
    if (halfPositive > 0L && halfPositive != scale.maxPositiveCents) {
        ticks += halfPositive
    }
    ticks += 0L
    val maxNegative = scale.maxNegativeMagnitudeCents
    if (maxNegative > 0L) {
        val halfNegative = maxNegative / 2
        if (halfNegative > 0L) {
            ticks += -halfNegative
        }
        ticks += -maxNegative
    }
    return ticks
}

/**
 * 固定的 Y 轴标签，不参与横向滚动，始终贴在最左侧。
 */
@Composable
private fun BarChartAxisLabels(scale: BarChartScale) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = Ink500)
    val ticks = barChartAxisTicks(scale)
    val totalRange = scale.maxPositiveCents + scale.maxNegativeMagnitudeCents

    Canvas(modifier = Modifier.width(axisGutter).fillMaxHeight()) {
        val zeroY = size.height * scale.maxPositiveCents.toFloat() / totalRange.toFloat()
        val positiveHeight = zeroY
        val negativeHeight = size.height - zeroY
        val maxPositive = scale.maxPositiveCents.coerceAtLeast(1L)
        val maxNegative = scale.maxNegativeMagnitudeCents.coerceAtLeast(1L)

        ticks.forEach { tick ->
            val y = if (tick >= 0L) {
                zeroY - positiveHeight * (tick.toFloat() / maxPositive.toFloat())
            } else {
                zeroY + negativeHeight * (abs(tick).toFloat() / maxNegative.toFloat())
            }
            val layoutResult = textMeasurer.measure(formatAxisMoney(tick), labelStyle)
            drawText(
                textLayoutResult = layoutResult,
                topLeft = Offset(
                    0f,
                    (y - layoutResult.size.height / 2f).coerceIn(0f, size.height)
                )
            )
        }
    }
}

/**
 * 收入/利润双柱状图，按日与按月统计共用。
 *
 * 收入柱使用维修蓝，利润柱正常为利润绿、亏损为危险红并从零线向下绘制。
 * 左侧 [axisGutter] 是固定的 Y 轴刻度，右侧绘图区支持横向滚动，并默认定位到最新的一组。
 */
@Composable
fun LedgerBarChart(
    points: List<BarChartPoint>,
    modifier: Modifier = Modifier,
    minimumBarWidth: Dp = 0.dp
) {
    val scale = calculateBarChartScale(points)
    val totalRange = scale.maxPositiveCents + scale.maxNegativeMagnitudeCents
    val ticks = barChartAxisTicks(scale)
    val scrollState = rememberScrollState()
    val rangeKey = points.firstOrNull()?.label to points.lastOrNull()?.label

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            BarChartAxisLabels(scale = scale)

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val plotWidth = maxOf(maxWidth, minimumBarWidth * points.size)
                val maxScroll = scrollState.maxValue

                LaunchedEffect(rangeKey, maxScroll) {
                    if (maxScroll > 0) scrollState.scrollTo(maxScroll)
                }

                Column(
                    modifier = Modifier
                        .horizontalScroll(scrollState)
                        .width(plotWidth),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
                        val zeroY = size.height * scale.maxPositiveCents.toFloat() / totalRange.toFloat()
                        val positiveHeight = zeroY
                        val negativeHeight = size.height - zeroY
                        val maxPositive = scale.maxPositiveCents.coerceAtLeast(1L)
                        val maxNegative = scale.maxNegativeMagnitudeCents.coerceAtLeast(1L)

                        ticks.forEach { tick ->
                            val y = if (tick >= 0L) {
                                zeroY - positiveHeight * (tick.toFloat() / maxPositive.toFloat())
                            } else {
                                zeroY + negativeHeight * (abs(tick).toFloat() / maxNegative.toFloat())
                            }
                            drawLine(
                                color = LineSoft,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val groupWidth = size.width / points.size.coerceAtLeast(1)
                        val barWidth = (groupWidth * 0.25f).coerceAtLeast(4.dp.toPx())

                        points.forEachIndexed { index, point ->
                            val centerX = groupWidth * (index + 0.5f)
                            val revenueHeight = zeroY * point.revenueCents.toFloat() /
                                scale.maxPositiveCents.toFloat()
                            drawRect(
                                color = RepairBlue,
                                topLeft = Offset(centerX - barWidth - 1.dp.toPx(), zeroY - revenueHeight),
                                size = Size(barWidth, revenueHeight)
                            )

                            val profit = point.profitCents
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
                        points.forEach { point ->
                            Text(
                                text = point.label,
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
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChartLegendItem("收入", RepairBlue)
            ChartLegendItem("利润", ProfitGreen)
            if (points.any { it.profitCents < 0L }) ChartLegendItem("亏损", DangerRed)
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
