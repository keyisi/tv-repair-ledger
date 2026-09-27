package com.bigegg.tvrepairledger.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.options.CommonOptionStore
import com.bigegg.tvrepairledger.ui.components.InfoChip
import com.bigegg.tvrepairledger.ui.components.LedgerCard
import com.bigegg.tvrepairledger.ui.components.SectionHeader
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.RepairBlue

@Composable
fun SettingsScreen(
    recordCount: Int,
    addressOptions: List<String>,
    repairDeviceOptions: List<String>,
    faultOptions: List<String>,
    repairItemOptions: List<String>,
    onImportXlsx: () -> Unit,
    onExportXlsx: () -> Unit,
    onAddAddressOption: (String) -> Unit,
    onDeleteAddressOption: (String) -> Unit,
    onAddRepairDeviceOption: (String) -> Unit,
    onDeleteRepairDeviceOption: (String) -> Unit,
    onAddFaultOption: (String) -> Unit,
    onDeleteFaultOption: (String) -> Unit,
    onAddRepairItemOption: (String) -> Unit,
    onDeleteRepairItemOption: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("本地数据管理", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("当前共有 $recordCount 条维修记录", color = Ink500)
                }
                Icon(Icons.Default.Lock, contentDescription = null, tint = RepairBlue)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("无账号")
                InfoChip("不联网")
                InfoChip("本机保存")
            }
        }

        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader("Excel 导入导出")
            Text("选择真实 .xlsx 文件导入维修记录，也可以把当前台账导出为 Excel 表格。", color = Ink500)
            Button(
                onClick = onImportXlsx,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Text("导入 Excel 表格")
            }
            OutlinedButton(
                onClick = onExportXlsx,
                modifier = Modifier.fillMaxWidth(),
                enabled = recordCount > 0
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Text("导出 Excel 表格")
            }
        }

        Text("常用内容管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

        OptionManagementCard(
            title = "常用地址",
            description = "添加小区名、街道名或常去地址前缀，新增维修记录时可从地址输入框右侧按钮选择。",
            inputPlaceholder = "例如：幸福小区",
            options = addressOptions,
            onAddOption = onAddAddressOption,
            onDeleteOption = onDeleteAddressOption,
            searchable = true
        )

        OptionManagementCard(
            title = "常用维修设备",
            description = "维护维修设备输入框右侧下拉列表。",
            inputPlaceholder = "例如：液晶电视",
            options = repairDeviceOptions,
            onAddOption = onAddRepairDeviceOption,
            onDeleteOption = onDeleteRepairDeviceOption
        )

        OptionManagementCard(
            title = "常用故障现象",
            description = "维护故障现象输入框右侧下拉列表。",
            inputPlaceholder = "例如：开机黑屏",
            options = faultOptions,
            onAddOption = onAddFaultOption,
            onDeleteOption = onDeleteFaultOption
        )

        OptionManagementCard(
            title = "常用维修项目",
            description = "维护维修项目输入框右侧下拉列表。",
            inputPlaceholder = "例如：更换背光灯条",
            options = repairItemOptions,
            onAddOption = onAddRepairItemOption,
            onDeleteOption = onDeleteRepairItemOption
        )

        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = RepairBlue)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("隐私优先", fontWeight = FontWeight.SemiBold)
                    Text("应用不需要登录，不连接云端，维修记录默认只保留在本机。", color = Ink500)
                }
            }
        }

        Text(
            text = "Excel 导入会跳过无法识别的无效行；只有月日、缺少年份的日期会提示需人工确认。",
            style = MaterialTheme.typography.bodySmall,
            color = Ink500
        )
    }
}

@Composable
private fun OptionManagementCard(
    title: String,
    description: String,
    inputPlaceholder: String,
    options: List<String>,
    onAddOption: (String) -> Unit,
    onDeleteOption: (String) -> Unit,
    searchable: Boolean = false
) {
    var input by rememberSaveable(title) { mutableStateOf("") }
    var query by rememberSaveable(title, "query") { mutableStateOf("") }
    var expanded by rememberSaveable(title, "expanded") { mutableStateOf(false) }
    val visibleOptions = if (searchable) {
        CommonOptionStore.visibleAddressOptions(options, query)
    } else {
        options
    }

    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${options.size} 个常用项", color = Ink500)
            }
            TextButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
                Text(if (expanded) "收起" else "展开")
            }
        }

        if (!expanded) return@LedgerCard

        Text(description, color = Ink500)
        if (searchable) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("搜索") },
                placeholder = { Text("输入小区名关键字") },
                singleLine = true
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("新增") },
                placeholder = { Text(inputPlaceholder) },
                singleLine = true
            )
            Button(
                onClick = {
                    val trimmed = input.trim()
                    if (trimmed.isNotEmpty()) {
                        onAddOption(trimmed)
                        input = ""
                    }
                },
                enabled = input.isNotBlank()
            ) {
                Text("添加")
            }
        }

        if (options.isEmpty()) {
            Text("暂无常用项", color = Ink500)
        } else if (visibleOptions.isEmpty()) {
            Text("没有匹配项", color = Ink500)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                visibleOptions.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(option, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onDeleteOption(option) }) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Text("删除")
                        }
                    }
                }
            }
        }
    }
}
