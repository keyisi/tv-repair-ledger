package com.bigegg.tvrepairledger.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

fun parseMoneyToCents(input: String): Long? {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return null

    return try {
        BigDecimal(trimmed)
            .movePointRight(2)
            .setScale(0, RoundingMode.HALF_UP)
            .longValueExact()
    } catch (_: Exception) {
        null
    }
}

/**
 * 机器可读的金额：两位小数、无千分位。
 * 仅用于导出文件（xlsx / CSV）与金额输入框的初始值，不要拿来做界面展示。
 */
fun formatCents(cents: Long): String {
    return BigDecimal(cents)
        .movePointLeft(2)
        .setScale(2, RoundingMode.UNNECESSARY)
        .toPlainString()
}

/**
 * 界面展示用的金额：带千分位，例如 3913000 -> "39,130.00"。
 * 固定 Locale.US，保证小数点为 "."、千分位为 ","，不受系统语言影响。
 */
fun formatMoney(cents: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
        isGroupingUsed = true
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return formatter.format(
        BigDecimal(cents)
            .movePointLeft(2)
            .setScale(2, RoundingMode.UNNECESSARY)
    )
}

/**
 * 图表 Y 轴用的短金额：整元不带小数，满一万折算成「万」，例如 3913000 -> "¥3.9万"。
 * 轴标签空间小，不能用 [formatMoney] 那种带两位小数的完整写法。
 */
fun formatAxisMoney(cents: Long): String {
    val sign = if (cents < 0) "-" else ""
    val absYuan = BigDecimal(kotlin.math.abs(cents))
        .movePointLeft(2)
    return if (absYuan >= BigDecimal("10000")) {
        val wan = absYuan.divide(BigDecimal("10000"), 1, RoundingMode.HALF_UP)
            .stripTrailingZeros()
            .toPlainString()
        "$sign¥${wan}万"
    } else {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            isGroupingUsed = true
            maximumFractionDigits = 0
        }
        "$sign¥${formatter.format(absYuan.setScale(0, RoundingMode.HALF_UP))}"
    }
}
