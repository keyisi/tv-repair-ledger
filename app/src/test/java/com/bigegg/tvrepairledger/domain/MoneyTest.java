package com.bigegg.tvrepairledger.domain;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MoneyTest {
    @Test
    public void parseMoneyToCents_acceptsWholeYuan() {
        assertEquals(Long.valueOf(35000L), MoneyKt.parseMoneyToCents("350"));
    }

    @Test
    public void parseMoneyToCents_acceptsDecimals() {
        assertEquals(Long.valueOf(35050L), MoneyKt.parseMoneyToCents("350.50"));
    }

    @Test
    public void parseMoneyToCents_rejectsInvalidText() {
        assertNull(MoneyKt.parseMoneyToCents("abc"));
    }

    @Test
    public void formatCents_formatsYuanWithTwoDecimals() {
        assertEquals("350.50", MoneyKt.formatCents(35050L));
    }

    @Test
    public void repairRecord_profitTreatsBlankCostAsZero() {
        RepairRecord record = new RepairRecord(
                1L,
                19810L,
                "sample-address",
                "",
                "sample-fault",
                "sample-item",
                50000L,
                null,
                "sample-notes",
                "",
                1L,
                1L
        );

        assertEquals(50000L, RepairRecordKt.getProfitCents(record));
    }
}
