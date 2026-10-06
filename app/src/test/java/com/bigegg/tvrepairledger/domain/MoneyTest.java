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
    public void formatMoney_groupsThousandsForDisplay() {
        assertEquals("39,130.00", MoneyKt.formatMoney(3913000L));
        assertEquals("350.50", MoneyKt.formatMoney(35050L));
        assertEquals("0.00", MoneyKt.formatMoney(0L));
        assertEquals("-1,200.00", MoneyKt.formatMoney(-120000L));
    }

    @Test
    public void formatCents_staysUngroupedForExport() {
        // Export files must stay machine readable, so no thousands separator here.
        assertEquals("39130.00", MoneyKt.formatCents(3913000L));
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
