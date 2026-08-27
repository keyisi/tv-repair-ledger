package com.bigegg.tvrepairledger.ui.stats

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class StatsDateStateTest {
    @Test
    fun dailyStatsDateState_usesTheClockZoneForTodayAndNextMidnight() {
        val clock = Clock.fixed(
            Instant.parse("2026-08-28T15:59:00Z"),
            ZoneId.of("Asia/Shanghai")
        )

        assertEquals(LocalDate.of(2026, 8, 28).toEpochDay(), currentDailyStatsEpochDay(clock))
        assertEquals(Instant.parse("2026-08-28T16:00:00Z"), nextDailyStatsDateRefresh(clock))
    }
}
