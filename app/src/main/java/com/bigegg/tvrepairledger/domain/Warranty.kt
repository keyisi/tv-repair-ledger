package com.bigegg.tvrepairledger.domain

fun remainingWarrantyDays(
    record: RepairRecord,
    todayEpochDay: Long
): Int {
    val expiresEpochDay = record.dateEpochDay + record.warrantyDays
    return (expiresEpochDay - todayEpochDay).toInt()
}

fun isWarrantyExpired(
    record: RepairRecord,
    todayEpochDay: Long
): Boolean = remainingWarrantyDays(record, todayEpochDay) < 0
