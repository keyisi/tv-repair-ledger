package com.bigegg.tvrepairledger.domain

/** 保修临期阈值：剩余天数不超过这个值就提示「即将过保」。 */
const val WARRANTY_EXPIRING_THRESHOLD_DAYS = 7

/**
 * 保修状态三态：在保 / 临期 / 已过保。
 * 界面按状态上色，避免只有「过了 / 没过」两种、临期单子看不出来。
 */
enum class WarrantyStatus {
    ACTIVE,
    EXPIRING,
    EXPIRED
}

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

fun warrantyStatus(
    record: RepairRecord,
    todayEpochDay: Long
): WarrantyStatus {
    val remainingDays = remainingWarrantyDays(record, todayEpochDay)
    return when {
        remainingDays < 0 -> WarrantyStatus.EXPIRED
        remainingDays <= WARRANTY_EXPIRING_THRESHOLD_DAYS -> WarrantyStatus.EXPIRING
        else -> WarrantyStatus.ACTIVE
    }
}
