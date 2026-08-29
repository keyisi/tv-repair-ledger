package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bigegg.tvrepairledger.domain.CategoryRepairSummary
import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import com.bigegg.tvrepairledger.domain.MonthlyRepairSummary
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.summarizeByDay
import com.bigegg.tvrepairledger.domain.summarizeByFault
import com.bigegg.tvrepairledger.domain.summarizeByMonth
import com.bigegg.tvrepairledger.domain.summarizeByRepairItem
import com.bigegg.tvrepairledger.domain.summarizeRecentDays
import com.bigegg.tvrepairledger.ui.components.AmountRow
import com.bigegg.tvrepairledger.ui.components.LedgerCard
import com.bigegg.tvrepairledger.ui.components.SectionHeader
import com.bigegg.tvrepairledger.ui.components.StatProgressRow
import com.bigegg.tvrepairledger.ui.theme.CostAmber
import com.bigegg.tvrepairledger.ui.theme.DangerRed
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import com.bigegg.tvrepairledger.ui.theme.RepairBlue
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

internal fun currentDailyStatsEpochDay(clock: Clock): Long = LocalDate.now(clock).toEpochDay()

internal fun nextDailyStatsDateRefresh(clock: Clock): Instant {
    return LocalDate.now(clock).plusDays(1).atStartOfDay(clock.zone).toInstant()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    records: List<RepairRecord>,
    modifier: Modifier = Modifier,
    clock: Clock = Clock.systemDefaultZone()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var chartRange by remember { mutableStateOf(DailyChartRange.Week) }
    var todayEpochDay by remember(clock) { mutableLongStateOf(currentDailyStatsEpochDay(clock)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val tabs = listOf("按日", "按月", "维修项目", "故障类型")
    val daily = summarizeByDay(records)
    val recentDaily = summarizeRecentDays(
        records = records,
        endDateEpochDay = todayEpochDay,
        dayCount = chartRange.dayCount
    )
    val monthly = summarizeByMonth(records)
    val itemSummaries = summarizeByRepairItem(records)
    val faultSummaries = summarizeByFault(records)
    val revenue = records.sumOf { it.chargedAmountCents }
    val cost = records.sumOf { it.partsCostCents ?: 0L }
    val profit = revenue - cost

    fun refreshToday() {
        todayEpochDay = currentDailyStatsEpochDay(clock)
    }

    DisposableEffect(lifecycleOwner, clock) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshToday()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(clock) {
        while (true) {
            val delayMillis = Duration.between(clock.instant(), nextDailyStatsDateRefresh(clock))
                .toMillis()
                .coerceAtLeast(1L)
            delay(delayMillis)
            refreshToday()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LedgerCard(modifier = Modifier.padding(top = 16.dp)) {
                    SectionHeader("全部经营数据", action = "${records.size} 单")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("总收入", style = MaterialTheme.typography.bodySmall, color = Ink500)
                            Text("¥${formatCents(revenue)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("总利润", style = MaterialTheme.typography.bodySmall, color = Ink500)
                            Text("¥${formatCents(profit)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ProfitGreen)
                        }
                    }
                    AmountRow("总成本", "¥${formatCents(cost)}", tint = CostAmber)
                }
            }

            when (selectedTab) {
                0 -> {
                    item {
                        LedgerCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "最近 ${chartRange.dayCount} 天",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                SingleChoiceSegmentedButtonRow {
                                    DailyChartRange.entries.forEachIndexed { index, range ->
                                        SegmentedButton(
                                            selected = chartRange == range,
                                            onClick = { chartRange = range },
                                            shape = SegmentedButtonDefaults.itemShape(
                                                index = index,
                                                count = DailyChartRange.entries.size
                                            ),
                                            label = { Text(range.label) }
                                        )
                                    }
                                }
                            }
                            DailyBarChart(
                                summaries = recentDaily,
                                modifier = Modifier.fillMaxWidth(),
                                minimumDayWidth = if (chartRange == DailyChartRange.Month) 42.dp else 0.dp
                            )
                        }
                    }
                    item { SectionHeader("每日明细") }
                    if (daily.isEmpty()) {
                        item { EmptyStats("暂无按日统计") }
                    } else {
                        items(daily) { summary -> DayStatCard(summary) }
                    }
                }

                1 -> {
                    item { SectionHeader("月度表现") }
                    if (monthly.isEmpty()) {
                        item { EmptyStats("暂无月度统计") }
                    } else {
                        items(monthly) { summary ->
                            MonthStatCard(summary = summary, maxProfit = monthly.maxOf { it.profitCents }.coerceAtLeast(1L))
                        }
                    }
                }

                2 -> {
                    item { SectionHeader("维修项目利润排行") }
                    if (itemSummaries.isEmpty()) {
                        item { EmptyStats("暂无维修项目统计") }
                    } else {
                        items(itemSummaries) { summary ->
                            CategoryStatCard(summary = summary, maxProfit = itemSummaries.maxOf { it.profitCents }.coerceAtLeast(1L))
                        }
                    }
                }

                else -> {
                    item { SectionHeader("故障类型利润排行") }
                    if (faultSummaries.isEmpty()) {
                        item { EmptyStats("暂无故障类型统计") }
                    } else {
                        items(faultSummaries) { summary ->
                            CategoryStatCard(summary = summary, maxProfit = faultSummaries.maxOf { it.profitCents }.coerceAtLeast(1L))
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.padding(bottom = 12.dp))
            }
        }
    }
}

@Composable
private fun MonthStatCard(summary: MonthlyRepairSummary, maxProfit: Long) {
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        StatProgressRow(
            title = "${summary.year}-${summary.month.toString().padStart(2, '0')}",
            subtitle = "${summary.count} 单 · 收入 ¥${formatCents(summary.revenueCents)} · 成本 ¥${formatCents(summary.costCents)}",
            value = "¥${formatCents(summary.profitCents)}",
            progress = summary.profitCents.toFloat() / maxProfit.toFloat(),
            tint = ProfitGreen
        )
    }
}

@Composable
private fun DayStatCard(summary: DailyRepairSummary) {
    val date = LocalDate.ofEpochDay(summary.dateEpochDay)
    val profitColor = if (summary.profitCents < 0L) DangerRed else ProfitGreen
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        StatProgressRow(
            title = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            subtitle = "${summary.count} 单 · 收入 ¥${formatCents(summary.revenueCents)} · 零件费 ¥${formatCents(summary.costCents)}",
            value = "利润 ¥${formatCents(summary.profitCents)}",
            progress = 1f,
            tint = profitColor
        )
    }
}

@Composable
private fun CategoryStatCard(summary: CategoryRepairSummary, maxProfit: Long) {
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        StatProgressRow(
            title = summary.label,
            subtitle = "${summary.count} 单 · 收入 ¥${formatCents(summary.revenueCents)} · 成本 ¥${formatCents(summary.costCents)}",
            value = "¥${formatCents(summary.profitCents)}",
            progress = summary.profitCents.toFloat() / maxProfit.toFloat(),
            tint = RepairBlue
        )
    }
}

@Composable
private fun EmptyStats(text: String) {
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("新增维修记录后，统计会自动更新。", color = Ink500)
    }
}
