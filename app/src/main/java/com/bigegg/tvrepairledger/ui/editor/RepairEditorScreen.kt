package com.bigegg.tvrepairledger.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.formatMoney
import com.bigegg.tvrepairledger.domain.parseMoneyToCents
import com.bigegg.tvrepairledger.options.CommonOptionStore
import com.bigegg.tvrepairledger.ui.components.AmountRow
import com.bigegg.tvrepairledger.ui.components.LedgerCard
import com.bigegg.tvrepairledger.ui.components.SectionHeader
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

data class RepairEditorDraft(
    val date: LocalDate,
    val customerName: String,
    val repairDevice: String,
    val brand: String,
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyPeriod: String,
    val warrantyDays: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairEditorScreen(
    onSave: (RepairEditorDraft) -> Unit,
    modifier: Modifier = Modifier,
    initialRecord: RepairRecord? = null,
    onDelete: (() -> Unit)? = null,
    addressOptions: List<String> = emptyList(),
    onUseAddressOption: (String) -> Unit = {},
    repairDeviceOptions: List<String> = emptyList(),
    brandOptions: List<String> = emptyList(),
    faultOptions: List<String> = emptyList(),
    repairItemOptions: List<String> = emptyList()
) {
    var dateInput by rememberSaveable(initialRecord?.id) {
        mutableStateOf(initialRecord?.let { LocalDate.ofEpochDay(it.dateEpochDay).toString() } ?: LocalDate.now().toString())
    }
    var customerName by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.customerName.orEmpty()) }
    var repairDevice by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.repairDevice.orEmpty()) }
    var brand by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.brand.orEmpty()) }
    var address by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.address.orEmpty()) }
    var phone by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.phone.orEmpty()) }
    var faultSymptom by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.faultSymptom.orEmpty()) }
    var repairItem by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.repairItem.orEmpty()) }
    var chargedAmount by rememberSaveable(initialRecord?.id) {
        mutableStateOf(initialRecord?.chargedAmountCents?.let(::formatCents).orEmpty())
    }
    var partsCost by rememberSaveable(initialRecord?.id) {
        mutableStateOf(initialRecord?.partsCostCents?.let(::formatCents).orEmpty())
    }
    var notes by rememberSaveable(initialRecord?.id) { mutableStateOf(initialRecord?.notes.orEmpty()) }
    var warrantyDaysInput by rememberSaveable(initialRecord?.id) {
        mutableStateOf((initialRecord?.warrantyDays ?: 90).toString())
    }

    var dateError by remember { mutableStateOf<String?>(null) }
    var chargedAmountError by remember { mutableStateOf<String?>(null) }
    var partsCostError by remember { mutableStateOf<String?>(null) }
    var warrantyDaysError by remember { mutableStateOf<String?>(null) }

    val chargedPreview = parseMoneyToCents(chargedAmount) ?: 0L
    val partsPreview = if (partsCost.isBlank()) 0L else parseMoneyToCents(partsCost) ?: 0L
    val profitPreview = chargedPreview - partsPreview

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader("客户信息")
            DateTextField(
                value = dateInput,
                onValueChange = {
                    dateInput = it
                    dateError = null
                },
                label = "维修日期",
                error = dateError,
                placeholder = "YYYY-MM-DD"
            )
            EditorField(
                value = customerName,
                onValueChange = { customerName = it },
                label = "客户姓名",
                placeholder = "例如：张先生"
            )
            EditorField(
                value = phone,
                onValueChange = { phone = it },
                label = "联系电话",
                placeholder = "用于后续查询",
                keyboardType = KeyboardType.Phone
            )
            AddressTextField(
                value = address,
                onValueChange = { address = it },
                label = "客户地址",
                placeholder = "例如：浦东新区锦绣路88号",
                options = addressOptions,
                onUseOption = onUseAddressOption
            )
            OptionTextField(
                value = repairDevice,
                onValueChange = { repairDevice = it },
                label = "维修设备",
                placeholder = "例如：液晶电视",
                options = repairDeviceOptions
            )
            OptionTextField(
                value = brand,
                onValueChange = { brand = it },
                label = "品牌",
                placeholder = "例如：小米、海信、TCL",
                options = brandOptions
            )
        }

        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader("故障与维修")
            OptionTextField(
                value = faultSymptom,
                onValueChange = { faultSymptom = it },
                label = "故障现象",
                placeholder = "例如：开机黑屏、有声音",
                options = faultOptions
            )
            OptionTextField(
                value = repairItem,
                onValueChange = { repairItem = it },
                label = "维修项目/配件",
                placeholder = "例如：更换背光灯条",
                options = repairItemOptions
            )
        }

        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("金额与利润", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("配件成本可留空，利润自动计算", style = MaterialTheme.typography.bodySmall, color = Ink500)
                }
                Icon(Icons.Default.Payments, contentDescription = null, tint = ProfitGreen)
            }
            EditorField(
                value = chargedAmount,
                onValueChange = {
                    chargedAmount = it
                    chargedAmountError = null
                },
                label = "收费金额",
                placeholder = "例如：380",
                error = chargedAmountError,
                keyboardType = KeyboardType.Decimal
            )
            EditorField(
                value = partsCost,
                onValueChange = {
                    partsCost = it
                    partsCostError = null
                },
                label = "配件成本",
                placeholder = "留空表示未记录成本",
                error = partsCostError,
                keyboardType = KeyboardType.Decimal
            )
            AmountRow("预计利润", "¥${formatMoney(profitPreview)}", tint = ProfitGreen)
        }

        LedgerCard(modifier = Modifier.fillMaxWidth()) {
            SectionHeader("备注与保修")
            EditorField(
                value = notes,
                onValueChange = { notes = it },
                label = "备注",
                placeholder = "客户要求、检测结果、注意事项"
            )
            EditorField(
                value = warrantyDaysInput,
                onValueChange = {
                    warrantyDaysInput = it
                    warrantyDaysError = null
                },
                label = "保修天数",
                placeholder = "默认 90",
                error = warrantyDaysError,
                keyboardType = KeyboardType.Number
            )
        }

        Button(
            onClick = {
                val parsedDate = try {
                    LocalDate.parse(dateInput.trim())
                } catch (_: DateTimeParseException) {
                    dateError = "请输入正确日期"
                    null
                }

                val parsedChargedAmount = parseMoneyToCents(chargedAmount)
                if (parsedChargedAmount == null) {
                    chargedAmountError = "请输入有效收费"
                }

                val trimmedPartsCost = partsCost.trim()
                val parsedPartsCost = when {
                    trimmedPartsCost.isEmpty() -> {
                        partsCostError = null
                        null
                    }
                    else -> parseMoneyToCents(trimmedPartsCost)?.also {
                        partsCostError = null
                    } ?: run {
                        partsCostError = "请输入有效配件成本"
                        null
                    }
                }

                val parsedWarrantyDays = warrantyDaysInput.trim().toIntOrNull()
                if (parsedWarrantyDays == null || parsedWarrantyDays < 0) {
                    warrantyDaysError = "请输入有效保修天数"
                }

                if (
                    parsedDate != null &&
                    parsedChargedAmount != null &&
                    partsCostError == null &&
                    parsedWarrantyDays != null &&
                    parsedWarrantyDays >= 0
                ) {
                    onSave(
                        RepairEditorDraft(
                            date = parsedDate,
                            customerName = customerName,
                            repairDevice = repairDevice,
                            brand = brand,
                            address = address,
                            phone = phone,
                            faultSymptom = faultSymptom,
                            repairItem = repairItem,
                            chargedAmountCents = parsedChargedAmount,
                            partsCostCents = parsedPartsCost,
                            notes = notes,
                            warrantyPeriod = "${parsedWarrantyDays}天",
                            warrantyDays = parsedWarrantyDays
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存维修记录")
        }

        if (onDelete != null) {
            var deleteDialogOpen by remember { mutableStateOf(false) }
            Button(
                onClick = { deleteDialogOpen = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Text("删除这条记录")
            }
            if (deleteDialogOpen) {
                AlertDialog(
                    onDismissRequest = { deleteDialogOpen = false },
                    title = { Text("确认删除？") },
                    text = { Text("删除后不可恢复。") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                deleteDialogOpen = false
                                onDelete()
                            }
                        ) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deleteDialogOpen = false }) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    error: String?
) {
    var dialogOpen by remember { mutableStateOf(false) }

    EditorField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        error = error,
        trailingIcon = {
            IconButton(onClick = { dialogOpen = true }) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "选择维修日期")
            }
        }
    )

    if (dialogOpen) {
        val selectedDate = runCatching { LocalDate.parse(value.trim()) }.getOrNull() ?: LocalDate.now()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onValueChange(
                                Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDate()
                                    .toString()
                            )
                        }
                        dialogOpen = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun AddressTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    options: List<String>,
    onUseOption: (String) -> Unit
) {
    var dialogOpen by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val filteredOptions = CommonOptionStore.visibleAddressOptions(options, query)

    EditorField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        trailingIcon = {
            IconButton(
                onClick = { dialogOpen = true },
                enabled = options.isNotEmpty()
            ) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "搜索常用$label")
            }
        }
    )

    if (dialogOpen) {
        AlertDialog(
            onDismissRequest = { dialogOpen = false },
            confirmButton = {
                TextButton(onClick = { dialogOpen = false }) {
                    Text("关闭")
                }
            },
            title = { Text("选择常用地址") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("搜索小区") },
                        placeholder = { Text("输入一两个字快速查找") },
                        singleLine = true
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        if (filteredOptions.isEmpty()) {
                            Text("没有匹配的小区", color = Ink500)
                        } else {
                            filteredOptions.forEach { option ->
                                TextButton(
                                    onClick = {
                                        onValueChange(option)
                                        onUseOption(option)
                                        query = ""
                                        dialogOpen = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(option, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

@Composable
private fun OptionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    options: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        EditorField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            trailingIcon = {
                IconButton(
                    onClick = { expanded = true },
                    enabled = options.isNotEmpty()
                ) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "选择常用$label")
                }
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun EditorField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = error != null,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        supportingText = {
            if (error != null) {
                Text(error, color = MaterialTheme.colorScheme.error)
            }
        },
        trailingIcon = trailingIcon,
        singleLine = label != "备注"
    )
}

private fun LocalDate.toUtcMillis(): Long {
    return atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
}
