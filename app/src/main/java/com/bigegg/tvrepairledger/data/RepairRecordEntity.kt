package com.bigegg.tvrepairledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bigegg.tvrepairledger.domain.RepairRecord

@Entity(tableName = "repair_records")
data class RepairRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyPeriod: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val customerName: String = "",
    val warrantyDays: Int = 90,
    val repairDevice: String = "",
    val brand: String = ""
)

fun RepairRecordEntity.toDomain(): RepairRecord = RepairRecord(
    id = id,
    dateEpochDay = dateEpochDay,
    address = address,
    phone = phone,
    faultSymptom = faultSymptom,
    repairItem = repairItem,
    chargedAmountCents = chargedAmountCents,
    partsCostCents = partsCostCents,
    notes = notes,
    warrantyPeriod = warrantyPeriod,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    customerName = customerName,
    warrantyDays = warrantyDays,
    repairDevice = repairDevice,
    brand = brand
)

fun RepairRecord.toEntity(): RepairRecordEntity = RepairRecordEntity(
    id = id,
    dateEpochDay = dateEpochDay,
    address = address,
    phone = phone,
    faultSymptom = faultSymptom,
    repairItem = repairItem,
    chargedAmountCents = chargedAmountCents,
    partsCostCents = partsCostCents,
    notes = notes,
    warrantyPeriod = warrantyPeriod,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis,
    customerName = customerName,
    warrantyDays = warrantyDays,
    repairDevice = repairDevice,
    brand = brand
)
