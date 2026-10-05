package com.bigegg.tvrepairledger.domain

data class RepairRecord @JvmOverloads constructor(
    val id: Long,
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

val RepairRecord.profitCents: Long
    get() = chargedAmountCents - (partsCostCents ?: 0L)
