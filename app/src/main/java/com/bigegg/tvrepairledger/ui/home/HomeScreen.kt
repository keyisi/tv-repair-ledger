package com.bigegg.tvrepairledger.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.isWarrantyExpired
import com.bigegg.tvrepairledger.domain.profitCents
import com.bigegg.tvrepairledger.domain.remainingWarrantyDays
import com.bigegg.tvrepairledger.domain.summarizeByMonth
import com.bigegg.tvrepairledger.ui.components.AmountRow
import com.bigegg.tvrepairledger.ui.components.InfoChip
import com.bigegg.tvrepairledger.ui.components.LedgerCard
import com.bigegg.tvrepairledger.ui.components.MetricCard
import com.bigegg.tvrepairledger.ui.components.SectionHeader
import com.bigegg.tvrepairledger.ui.theme.CostAmber
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.LedgerTheme
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import com.bigegg.tvrepairledger.ui.theme.RepairBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    records: List<RepairRecord>,
    onAddClick: () -> Unit,
    onRecordClick: (RepairRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    val currentMonthSummary = summarizeByMonth(records).firstOrNull {
        it.year == currentDate.year && it.month == currentDate.monthValue
    }
    val recentRecords = records.sortedByDescending { it.dateEpochDay }.take(5)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            LedgerCard(modifier = Modifier.padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                        Text("今日工作台", style = MaterialTheme.typography.labelLarge, color = Ink500)
                        Text(
                            "把上门维修、收费和利润记清楚",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "本月已完成 ${currentMonthSummary?.count ?: 0} 单，最近 ${records.size} 条维修记录可查询。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink500
                        )
                    }
                    Icon(Icons.Default.Build, contentDescription = null, tint = RepairBlue)
                }
                Button(onClick = onAddClick, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("新增维修记录")
                }
            }
        }

        item {
            SectionHeader("本月经营概览")
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "收入",
                    value = "¥${formatCents(currentMonthSummary?.revenueCents ?: 0L)}",
                    caption = "全部收费",
                    tint = RepairBlue,
                    icon = Icons.Default.Payments,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "利润",
                    value = "¥${formatCents(currentMonthSummary?.profitCents ?: 0L)}",
                    caption = "扣除配件",
                    tint = ProfitGreen,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "成本",
                    value = "¥${formatCents(currentMonthSummary?.costCents ?: 0L)}",
                    caption = "配件支出",
                    tint = CostAmber,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "单量",
                    value = "${currentMonthSummary?.count ?: 0}",
                    caption = "本月维修",
                    tint = Color(0xFF475467),
                    icon = Icons.Default.Build,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            SectionHeader("最近维修", action = "按时间排序")
        }

        if (recentRecords.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            items(recentRecords, key = { it.id }) { record ->
                RecentRecordCard(
                    record = record,
                    onClick = { onRecordClick(record) }
                )
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

@Composable
private fun EmptyState() {
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        Text("暂无维修记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text("点击新增维修记录后，这里会显示最近的客户和利润信息。", color = Ink500)
    }
}

@Composable
private fun RecentRecordCard(
    record: RepairRecord,
    onClick: () -> Unit
) {
    val dateText = LocalDate.ofEpochDay(record.dateEpochDay).format(DateTimeFormatter.ofPattern("MM-dd"))
    val today = LocalDate.now().toEpochDay()
    val remainingDays = remainingWarrantyDays(record, today)
    val expired = isWarrantyExpired(record, today)
    LedgerCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = record.customerName.ifBlank {
                        if (record.address.isBlank()) "未填客户" else record.address
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = record.faultSymptom.ifBlank { "未填写故障" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = Ink500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            InfoChip(dateText)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (record.repairDevice.isNotBlank()) {
                InfoChip(record.repairDevice)
            }
            InfoChip(record.repairItem.ifBlank { "未填项目" })
            InfoChip(if (expired) "已过保" else "保修剩 $remainingDays 天")
        }
        if (expired) {
            Text(
                text = "已过保 ${-remainingDays} 天",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        AmountRow("收费", "¥${formatCents(record.chargedAmountCents)}")
        AmountRow("利润", "¥${formatCents(record.profitCents)}", tint = ProfitGreen)
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    LedgerTheme {
        HomeScreen(
            records = emptyList(),
            onAddClick = {},
            onRecordClick = {}
        )
    }
}
