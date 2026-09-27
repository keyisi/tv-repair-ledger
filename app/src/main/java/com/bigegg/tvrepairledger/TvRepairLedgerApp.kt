package com.bigegg.tvrepairledger

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.bigegg.tvrepairledger.data.RepairRepository
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.importer.parseWorkbookRows
import com.bigegg.tvrepairledger.importer.toRepairRecord
import com.bigegg.tvrepairledger.options.CommonOptionStore
import com.bigegg.tvrepairledger.ui.editor.RepairEditorDraft
import com.bigegg.tvrepairledger.ui.editor.RepairEditorScreen
import com.bigegg.tvrepairledger.ui.home.HomeScreen
import com.bigegg.tvrepairledger.ui.list.RepairListScreen
import com.bigegg.tvrepairledger.ui.settings.SettingsScreen
import com.bigegg.tvrepairledger.ui.stats.StatsScreen
import com.bigegg.tvrepairledger.ui.theme.LedgerTheme
import com.bigegg.tvrepairledger.ui.theme.RepairBlueDark
import com.bigegg.tvrepairledger.xlsx.readWorkbookRows
import com.bigegg.tvrepairledger.xlsx.repairRecordsToWorkbookRows
import com.bigegg.tvrepairledger.xlsx.writeWorkbook
import kotlinx.coroutines.launch

private enum class AppScreen(
    val label: String,
    val title: String,
    val icon: ImageVector
) {
    Home("首页", "维修记账", Icons.Default.Home),
    List("台账", "维修台账", Icons.AutoMirrored.Filled.FormatListBulleted),
    Stats("统计", "经营统计", Icons.Default.BarChart),
    Settings("更多", "更多", Icons.Default.SaveAlt),
    Editor("编辑", "维修记录", Icons.Default.Add)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvRepairLedgerApp(
    repository: RepairRepository,
    optionStore: CommonOptionStore
) {
    LedgerTheme {
        val records by repository.observeAll().collectAsState(initial = emptyList())
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var currentScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }
        var editorReturnScreen by rememberSaveable { mutableStateOf(AppScreen.Home) }
        var editingRecord by remember { mutableStateOf<RepairRecord?>(null) }
        var exportDialog by remember { mutableStateOf<ExportDialogState?>(null) }
        var centerMessage by remember { mutableStateOf<String?>(null) }
        var addressOptions by remember { mutableStateOf(optionStore.loadAddressOptions()) }
        var recentAddressOptions by remember { mutableStateOf(optionStore.loadRecentAddressOptions()) }
        var repairDeviceOptions by remember { mutableStateOf(optionStore.loadRepairDeviceOptions()) }
        var faultOptions by remember { mutableStateOf(optionStore.loadFaultOptions()) }
        var repairItemOptions by remember { mutableStateOf(optionStore.loadRepairItemOptions()) }
        val rankedAddressOptions = CommonOptionStore.rankAddressOptions(addressOptions, recentAddressOptions)
        val exportXlsxLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
        ) { uri ->
            if (uri != null) {
                runCatching {
                    val bytes = writeWorkbook(repairRecordsToWorkbookRows(records))
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(bytes)
                    } ?: error("无法打开导出文件")
                }.onSuccess {
                    exportDialog = ExportDialogState("导出完成", "Excel 表格已保存。")
                }.onFailure { error ->
                    exportDialog = ExportDialogState("导出失败", error.message ?: "未知错误")
                }
            }
        }
        val importXlsxLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        parseWorkbookRows(readWorkbookRows(input))
                    } ?: error("无法打开导入文件")
                }.onSuccess { review ->
                    val now = System.currentTimeMillis()
                    scope.launch {
                        review.readyRows.forEach { row ->
                            repository.save(row.toRepairRecord(createdAtMillis = now, updatedAtMillis = now))
                        }
                        exportDialog = ExportDialogState(
                            title = "导入完成",
                            body = "已导入 ${review.readyRows.size} 条；需确认日期 ${review.needsDateConfirmation.size} 条；跳过 ${review.skippedRows.size} 条。"
                        )
                    }
                }.onFailure { error ->
                    exportDialog = ExportDialogState("导入失败", error.message ?: "未知错误")
                }
            }
        }

        fun leaveEditor() {
            editingRecord = null
            currentScreen = editorReturnScreen
        }

        BackHandler(enabled = currentScreen == AppScreen.Editor) {
            leaveEditor()
        }

        LaunchedEffect(centerMessage) {
            if (centerMessage != null) {
                delay(1400L)
                centerMessage = null
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (currentScreen == AppScreen.Editor) {
                                if (editingRecord == null) "新增维修记录" else "编辑维修记录"
                            } else {
                                currentScreen.title
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    actions = {
                        if (currentScreen == AppScreen.Editor) {
                            TextButton(onClick = ::leaveEditor) {
                                Icon(Icons.Default.Close, contentDescription = null)
                                Text("取消")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = RepairBlueDark,
                        titleContentColor = androidx.compose.ui.graphics.Color.White,
                        actionIconContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            },
            bottomBar = {
                if (currentScreen != AppScreen.Editor) {
                    NavigationBar {
                        listOf(AppScreen.Home, AppScreen.List, AppScreen.Stats, AppScreen.Settings).forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen != AppScreen.Editor) {
                    FloatingActionButton(
                        onClick = {
                            editingRecord = null
                            editorReturnScreen = currentScreen
                            currentScreen = AppScreen.Editor
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "新增维修记录")
                    }
                }
            }
        ) { innerPadding ->
            when (currentScreen) {
                AppScreen.Home -> HomeScreen(
                    records = records,
                    onAddClick = {
                        editingRecord = null
                        editorReturnScreen = AppScreen.Home
                        currentScreen = AppScreen.Editor
                    },
                    onRecordClick = {
                        editingRecord = it
                        editorReturnScreen = AppScreen.Home
                        currentScreen = AppScreen.Editor
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.List -> RepairListScreen(
                    records = records,
                    onRecordClick = {
                        editingRecord = it
                        editorReturnScreen = AppScreen.List
                        currentScreen = AppScreen.Editor
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.Stats -> StatsScreen(
                    records = records,
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.Settings -> SettingsScreen(
                    recordCount = records.size,
                    addressOptions = rankedAddressOptions,
                    repairDeviceOptions = repairDeviceOptions,
                    faultOptions = faultOptions,
                    repairItemOptions = repairItemOptions,
                    onImportXlsx = {
                        importXlsxLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/octet-stream",
                                "*/*"
                            )
                        )
                    },
                    onExportXlsx = {
                        exportXlsxLauncher.launch("维修台账.xlsx")
                    },
                    onAddAddressOption = { option ->
                        val updated = CommonOptionStore.normalizeOptions(addressOptions + option)
                        addressOptions = updated
                        optionStore.saveAddressOptions(updated)
                    },
                    onDeleteAddressOption = { option ->
                        val updated = addressOptions.filterNot { it == option }
                        addressOptions = updated
                        optionStore.saveAddressOptions(updated)
                        recentAddressOptions = recentAddressOptions.filterNot { it == option }
                    },
                    onAddRepairDeviceOption = { option ->
                        val updated = CommonOptionStore.normalizeOptions(repairDeviceOptions + option)
                        repairDeviceOptions = updated
                        optionStore.saveRepairDeviceOptions(updated)
                    },
                    onDeleteRepairDeviceOption = { option ->
                        val updated = repairDeviceOptions.filterNot { it == option }
                        repairDeviceOptions = updated
                        optionStore.saveRepairDeviceOptions(updated)
                    },
                    onAddFaultOption = { option ->
                        val updated = CommonOptionStore.normalizeOptions(faultOptions + option)
                        faultOptions = updated
                        optionStore.saveFaultOptions(updated)
                    },
                    onDeleteFaultOption = { option ->
                        val updated = faultOptions.filterNot { it == option }
                        faultOptions = updated
                        optionStore.saveFaultOptions(updated)
                    },
                    onAddRepairItemOption = { option ->
                        val updated = CommonOptionStore.normalizeOptions(repairItemOptions + option)
                        repairItemOptions = updated
                        optionStore.saveRepairItemOptions(updated)
                    },
                    onDeleteRepairItemOption = { option ->
                        val updated = repairItemOptions.filterNot { it == option }
                        repairItemOptions = updated
                        optionStore.saveRepairItemOptions(updated)
                    },
                    modifier = Modifier.padding(innerPadding)
                )

                AppScreen.Editor -> RepairEditorScreen(
                    initialRecord = editingRecord,
                    onSave = { draft ->
                        val wasEditing = editingRecord != null
                        val now = System.currentTimeMillis()
                        val savedRecord = draft.toRecord(
                            id = editingRecord?.id ?: 0L,
                            createdAtMillis = editingRecord?.createdAtMillis ?: now,
                            updatedAtMillis = now
                        )
                        scope.launch {
                            val savedId = repository.save(savedRecord)
                            if (wasEditing) {
                                editingRecord = savedRecord.copy(id = savedId)
                            } else {
                                editingRecord = null
                                currentScreen = editorReturnScreen
                            }
                            centerMessage = "维修记录已保存"
                        }
                    },
                    onDelete = editingRecord?.let { record ->
                        {
                            scope.launch {
                                repository.deleteById(record.id)
                                editingRecord = null
                                currentScreen = editorReturnScreen
                            }
                        }
                    },
                    addressOptions = rankedAddressOptions,
                    onUseAddressOption = { option ->
                        optionStore.recordAddressUse(option)
                        recentAddressOptions = optionStore.loadRecentAddressOptions()
                    },
                    repairDeviceOptions = repairDeviceOptions,
                    faultOptions = faultOptions,
                    repairItemOptions = repairItemOptions,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }

        centerMessage?.let { message ->
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = androidx.compose.ui.graphics.Color(0xDD111827),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                tonalElevation = 6.dp
            ) {
                Text(
                    text = message,
                    color = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        }

        exportDialog?.let { state ->
            ExportTextDialog(
                state = state,
                onDismiss = { exportDialog = null }
            )
        }
    }
}

private data class ExportDialogState(
    val title: String,
    val body: String
)

@Composable
private fun ExportTextDialog(
    state: ExportDialogState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        },
        title = { Text(state.title) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SelectionContainer {
                    Text(state.body)
                }
            }
        }
    )
}

private fun RepairEditorDraft.toRecord(
    id: Long,
    createdAtMillis: Long,
    updatedAtMillis: Long
): RepairRecord {
    return RepairRecord(
        id = id,
        dateEpochDay = date.toEpochDay(),
        address = address.trim(),
        phone = phone.trim(),
        faultSymptom = faultSymptom.trim(),
        repairItem = repairItem.trim(),
        chargedAmountCents = chargedAmountCents,
        partsCostCents = partsCostCents,
        notes = notes.trim(),
        warrantyPeriod = warrantyPeriod.trim(),
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        customerName = customerName.trim(),
        warrantyDays = warrantyDays,
        repairDevice = repairDevice.trim()
    )
}
