package com.bigegg.tvrepairledger.domain

import java.math.BigDecimal
import java.math.RoundingMode

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

fun formatCents(cents: Long): String {
    return BigDecimal(cents)
        .movePointLeft(2)
        .setScale(2, RoundingMode.UNNECESSARY)
        .toPlainString()
}
