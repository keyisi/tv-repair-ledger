package com.bigegg.tvrepairledger.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.isWarrantyExpired
import com.bigegg.tvrepairledger.domain.profitCents
import com.bigegg.tvrepairledger.domain.remainingWarrantyDays
import com.bigegg.tvrepairledger.ui.components.AmountRow
import com.bigegg.tvrepairledger.ui.components.InfoChip
import com.bigegg.tvrepairledger.ui.components.LedgerCard
import com.bigegg.tvrepairledger.ui.components.SectionHeader
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun RepairListScreen(
    records: List<RepairRecord>,
    onRecordClick: (RepairRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filteredRecords = records
        .sortedByDescending { it.dateEpochDay }
        .filter { record ->
            if (query.isBlank()) return@filter true
            val keyword = query.trim()
            listOf(
                record.customerName,
                record.address,
                record.phone,
                record.repairDevice,
                record.brand,
                record.faultSymptom,
                record.repairItem,
                record.notes,
                record.warrantyPeriod
            ).any { it.contains(keyword, ignoreCase = true) }
        }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("搜索地址、电话、设备、故障、配件、备注") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true
                )
                SectionHeader(
                    title = if (query.isBlank()) "全部维修记录" else "搜索结果",
                    action = "${filteredRecords.size} 条"
                )
            }
        }

        if (filteredRecords.isEmpty()) {
            item {
                EmptySearchState(query)
            }
        } else {
            items(filteredRecords, key = { it.id }) { record ->
                RepairListItem(record = record, onClick = { onRecordClick(record) })
            }
        }

        item {
            Box(modifier = Modifier.padding(bottom = 12.dp))
        }
    }
}

@Composable
private fun EmptySearchState(query: String) {
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        Text("未找到维修记录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            if (query.isBlank()) "新增记录后会显示在这里。" else "换一个地址、电话或维修项目再试试。",
            color = Ink500
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepairListItem(
    record: RepairRecord,
    onClick: () -> Unit
) {
    val dateText = LocalDate.ofEpochDay(record.dateEpochDay).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
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
                    text = "${record.phone.ifBlank { "无电话" }}  ·  $dateText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Ink500,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            InfoChip("利润 ¥${formatCents(record.profitCents)}")
        }
        Text(
            text = record.faultSymptom.ifBlank { "未填写故障现象" },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (record.repairDevice.isNotBlank()) {
                InfoChip(record.repairDevice)
            }
            if (record.brand.isNotBlank()) {
                InfoChip(record.brand)
            }
            InfoChip(record.repairItem.ifBlank { "未填维修项目" })
            InfoChip(if (record.address.isBlank()) "未填地址" else record.address)
            if (record.partsCostCents == null) {
                InfoChip("成本未填")
            }
        }
        Text(
            text = if (expired) "已过保 ${-remainingDays} 天" else "保修剩余 $remainingDays 天",
            color = if (expired) MaterialTheme.colorScheme.error else Ink500,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (expired) FontWeight.SemiBold else FontWeight.Normal
        )
        AmountRow("收费", "¥${formatCents(record.chargedAmountCents)}")
        AmountRow("净利润", "¥${formatCents(record.profitCents)}", tint = ProfitGreen)
    }
}
