# Daily Stats Chart Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add daily repair summaries and a native grouped bar chart for the latest seven calendar days to the Android statistics screen.

**Architecture:** Keep all date grouping and zero-filling in pure domain functions in `RepairStats.kt`, so the UI receives deterministic daily summaries. Add a focused Compose chart component that calculates a positive/negative scale with a tested pure helper, then connect it to a new default “按日” tab in `StatsScreen.kt`.

**Tech Stack:** Kotlin 2.3, Jetpack Compose Material 3, Java Time, JUnit 4, Gradle Android plugin 9.2.

## Global Constraints

- Do not add a third-party chart dependency.
- The chart displays exactly the latest 7 calendar days ending on the current local date.
- Missing dates appear as zero in the chart but not in the all-days detail list.
- Revenue uses `RepairBlue`, non-negative profit uses `ProfitGreen`, and negative profit uses `DangerRed`.
- Keep the existing all-time overview and the monthly, repair-item, and fault tabs.
- Set `versionCode` to 14 and `versionName` to `1.13`.
- Produce `电视维修记账_v1.13_debug.apk`.

---

### Task 1: Daily Summary Domain Logic

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt`

**Interfaces:**
- Consumes: `RepairRecord.dateEpochDay`, `chargedAmountCents`, `partsCostCents`, and `profitCents`.
- Produces: `DailyRepairSummary`, `summarizeByDay(records)`, and `summarizeRecentDays(records, endDateEpochDay, dayCount)` for the statistics UI.

- [ ] **Step 1: Write failing tests for grouping and newest-first sorting**

Add to `RepairStatsTest.kt`:

```kotlin
@Test
fun summarizeByDay_groupsMoneyAndSortsNewestFirst() {
    val summaries = summarizeByDay(
        listOf(
            record(1, 19810, "lamp", "black-screen", 50000, 5000),
            record(2, 19810, "lamp", "black-screen", 35000, null),
            record(3, 19811, "EMMC", "boot-logo", 30000, 40000)
        )
    )

    assertEquals(listOf(19811L, 19810L), summaries.map { it.dateEpochDay })
    assertEquals(1, summaries[0].count)
    assertEquals(30000L, summaries[0].revenueCents)
    assertEquals(40000L, summaries[0].costCents)
    assertEquals(-10000L, summaries[0].profitCents)
    assertEquals(2, summaries[1].count)
    assertEquals(85000L, summaries[1].revenueCents)
    assertEquals(5000L, summaries[1].costCents)
    assertEquals(80000L, summaries[1].profitCents)
}
```

- [ ] **Step 2: Run the focused test and verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.domain.RepairStatsTest.summarizeByDay_groupsMoneyAndSortsNewestFirst"
```

Expected: compilation fails because `summarizeByDay` and `DailyRepairSummary` do not exist.

- [ ] **Step 3: Implement the daily summary model and grouping function**

Add to `RepairStats.kt`:

```kotlin
data class DailyRepairSummary(
    val dateEpochDay: Long,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

fun summarizeByDay(records: List<RepairRecord>): List<DailyRepairSummary> {
    return records
        .groupBy { it.dateEpochDay }
        .map { (dateEpochDay, group) ->
            DailyRepairSummary(
                dateEpochDay = dateEpochDay,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedByDescending { it.dateEpochDay }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the command from Step 2.

Expected: `BUILD SUCCESSFUL` and one passing test.

- [ ] **Step 5: Write a failing test for seven continuous calendar days**

Add to `RepairStatsTest.kt`:

```kotlin
@Test
fun summarizeRecentDays_returnsAscendingContinuousDaysAndFillsMissingDates() {
    val summaries = summarizeRecentDays(
        records = listOf(
            record(1, 100, "lamp", "black-screen", 50000, 5000),
            record(2, 94, "EMMC", "boot-logo", 30000, 40000),
            record(3, 93, "old", "old", 99999, null)
        ),
        endDateEpochDay = 100,
        dayCount = 7
    )

    assertEquals((94L..100L).toList(), summaries.map { it.dateEpochDay })
    assertEquals(-10000L, summaries.first().profitCents)
    assertEquals(0, summaries[1].count)
    assertEquals(0L, summaries[1].revenueCents)
    assertEquals(45000L, summaries.last().profitCents)
}
```

- [ ] **Step 6: Run the recent-days test and verify RED**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.domain.RepairStatsTest.summarizeRecentDays_returnsAscendingContinuousDaysAndFillsMissingDates"
```

Expected: compilation fails because `summarizeRecentDays` does not exist.

- [ ] **Step 7: Implement continuous recent-day summaries**

Add to `RepairStats.kt`:

```kotlin
fun summarizeRecentDays(
    records: List<RepairRecord>,
    endDateEpochDay: Long,
    dayCount: Int = 7
): List<DailyRepairSummary> {
    require(dayCount > 0) { "dayCount must be positive" }
    val summariesByDate = summarizeByDay(records).associateBy { it.dateEpochDay }
    val startDateEpochDay = endDateEpochDay - dayCount + 1
    return (startDateEpochDay..endDateEpochDay).map { dateEpochDay ->
        summariesByDate[dateEpochDay] ?: DailyRepairSummary(
            dateEpochDay = dateEpochDay,
            count = 0,
            revenueCents = 0L,
            costCents = 0L,
            profitCents = 0L
        )
    }
}
```

- [ ] **Step 8: Run all domain statistics tests and commit**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.domain.RepairStatsTest"
```

Expected: `BUILD SUCCESSFUL` with all `RepairStatsTest` tests passing.

Commit:

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt
git commit -m "feat: add daily repair summaries"
```

---

### Task 2: Native Seven-Day Bar Chart and Daily Tab

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyBarChart.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/DailyChartScaleTest.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt`

**Interfaces:**
- Consumes: `DailyRepairSummary`, `summarizeByDay`, `summarizeRecentDays`, and theme colors.
- Produces: `DailyChartScale`, `calculateDailyChartScale(summaries)`, and `DailyBarChart(summaries)`.

- [ ] **Step 1: Write failing tests for positive, negative, and all-zero chart scales**

Create `DailyChartScaleTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.ui.stats

import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyChartScaleTest {
    private fun summary(revenue: Long, profit: Long) = DailyRepairSummary(
        dateEpochDay = 1,
        count = 1,
        revenueCents = revenue,
        costCents = revenue - profit,
        profitCents = profit
    )

    @Test
    fun calculateDailyChartScale_coversRevenuePositiveAndNegativeProfit() {
        val scale = calculateDailyChartScale(
            listOf(summary(50000, 45000), summary(30000, -10000))
        )

        assertEquals(50000L, scale.maxPositiveCents)
        assertEquals(10000L, scale.maxNegativeMagnitudeCents)
    }

    @Test
    fun calculateDailyChartScale_usesStableRangeForAllZeroValues() {
        val scale = calculateDailyChartScale(listOf(summary(0, 0)))

        assertEquals(1L, scale.maxPositiveCents)
        assertEquals(0L, scale.maxNegativeMagnitudeCents)
    }
}
```

- [ ] **Step 2: Run the chart-scale tests and verify RED**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.ui.stats.DailyChartScaleTest"
```

Expected: compilation fails because `DailyChartScale` and `calculateDailyChartScale` do not exist.

- [ ] **Step 3: Create the scale helper and grouped bar chart**

Create `DailyBarChart.kt` with:

```kotlin
package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.DailyRepairSummary
import com.bigegg.tvrepairledger.ui.theme.DangerRed
import com.bigegg.tvrepairledger.ui.theme.Ink500
import com.bigegg.tvrepairledger.ui.theme.LineSoft
import com.bigegg.tvrepairledger.ui.theme.ProfitGreen
import com.bigegg.tvrepairledger.ui.theme.RepairBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

data class DailyChartScale(
    val maxPositiveCents: Long,
    val maxNegativeMagnitudeCents: Long
)

fun calculateDailyChartScale(summaries: List<DailyRepairSummary>): DailyChartScale {
    return DailyChartScale(
        maxPositiveCents = summaries.maxOfOrNull {
            maxOf(it.revenueCents, it.profitCents.coerceAtLeast(0L))
        }?.coerceAtLeast(1L) ?: 1L,
        maxNegativeMagnitudeCents = summaries.minOfOrNull {
            it.profitCents.coerceAtMost(0L)
        }?.let(::abs) ?: 0L
    )
}

@Composable
fun DailyBarChart(
    summaries: List<DailyRepairSummary>,
    modifier: Modifier = Modifier
) {
    val scale = calculateDailyChartScale(summaries)
    val totalRange = scale.maxPositiveCents + scale.maxNegativeMagnitudeCents
    val dateFormatter = DateTimeFormatter.ofPattern("MM-dd")

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            val zeroY = size.height * scale.maxPositiveCents.toFloat() / totalRange.toFloat()
            drawLine(LineSoft, Offset(0f, zeroY), Offset(size.width, zeroY), strokeWidth = 1.dp.toPx())
            val groupWidth = size.width / summaries.size.coerceAtLeast(1)
            val barWidth = (groupWidth * 0.25f).coerceAtLeast(4.dp.toPx())

            summaries.forEachIndexed { index, summary ->
                val centerX = groupWidth * (index + 0.5f)
                val revenueHeight = zeroY * summary.revenueCents.toFloat() /
                    scale.maxPositiveCents.toFloat()
                drawRect(
                    color = RepairBlue,
                    topLeft = Offset(centerX - barWidth - 1.dp.toPx(), zeroY - revenueHeight),
                    size = Size(barWidth, revenueHeight)
                )

                val profit = summary.profitCents
                val profitHeight = if (profit >= 0L) {
                    zeroY * profit.toFloat() / scale.maxPositiveCents.toFloat()
                } else {
                    (size.height - zeroY) * abs(profit).toFloat() /
                        scale.maxNegativeMagnitudeCents.coerceAtLeast(1L).toFloat()
                }
                drawRect(
                    color = if (profit < 0L) DangerRed else ProfitGreen,
                    topLeft = Offset(centerX + 1.dp.toPx(), if (profit < 0L) zeroY else zeroY - profitHeight),
                    size = Size(barWidth, profitHeight)
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            summaries.forEach { summary ->
                Text(
                    text = LocalDate.ofEpochDay(summary.dateEpochDay).format(dateFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = Ink500
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ChartLegendItem("收入", RepairBlue)
            ChartLegendItem("利润", ProfitGreen)
            if (summaries.any { it.profitCents < 0L }) ChartLegendItem("亏损", DangerRed)
        }
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Spacer(Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Ink500, fontWeight = FontWeight.Medium)
    }
}
```

- [ ] **Step 4: Run the chart-scale tests and verify GREEN**

Run the command from Step 2.

Expected: `BUILD SUCCESSFUL` and both scale tests pass.

- [ ] **Step 5: Connect the daily tab, chart, and all-days detail list**

Modify `StatsScreen.kt`:

```kotlin
val tabs = listOf("按日", "按月", "维修项目", "故障类型")
val daily = summarizeByDay(records)
val recentDaily = summarizeRecentDays(
    records = records,
    endDateEpochDay = LocalDate.now().toEpochDay()
)
```

Use tab index `0` for a “最近 7 天” `LedgerCard` containing `DailyBarChart(recentDaily)`, followed by “每日明细”. Render each `DailyRepairSummary` with a new private `DayStatCard`; show `EmptyStats("暂无按日统计")` when `daily` is empty. Shift the existing monthly, repair-item, and fault branches to indices 1, 2, and 3:

```kotlin
when (selectedTab) {
    0 -> {
        item {
            LedgerCard(modifier = Modifier.fillMaxWidth()) {
                SectionHeader("最近 7 天", action = "收入 / 利润")
                DailyBarChart(recentDaily, modifier = Modifier.fillMaxWidth())
            }
        }
        item { SectionHeader("每日明细") }
        if (daily.isEmpty()) {
            item { EmptyStats("暂无按日统计") }
        } else {
            items(daily) { summary -> DayStatCard(summary) }
        }
    }

    1 -> {
        item { SectionHeader("月度表现") }
        if (monthly.isEmpty()) {
            item { EmptyStats("暂无月度统计") }
        } else {
            items(monthly) { summary ->
                MonthStatCard(
                    summary = summary,
                    maxProfit = monthly.maxOf { it.profitCents }.coerceAtLeast(1L)
                )
            }
        }
    }

    2 -> {
        item { SectionHeader("维修项目利润排行") }
        if (itemSummaries.isEmpty()) {
            item { EmptyStats("暂无维修项目统计") }
        } else {
            items(itemSummaries) { summary ->
                CategoryStatCard(
                    summary = summary,
                    maxProfit = itemSummaries.maxOf { it.profitCents }.coerceAtLeast(1L)
                )
            }
        }
    }

    else -> {
        item { SectionHeader("故障类型利润排行") }
        if (faultSummaries.isEmpty()) {
            item { EmptyStats("暂无故障类型统计") }
        } else {
            items(faultSummaries) { summary ->
                CategoryStatCard(
                    summary = summary,
                    maxProfit = faultSummaries.maxOf { it.profitCents }.coerceAtLeast(1L)
                )
            }
        }
    }
}
```

Add:

```kotlin
@Composable
private fun DayStatCard(summary: DailyRepairSummary) {
    val date = LocalDate.ofEpochDay(summary.dateEpochDay)
    val profitColor = if (summary.profitCents < 0L) DangerRed else ProfitGreen
    LedgerCard(modifier = Modifier.fillMaxWidth()) {
        StatProgressRow(
            title = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            subtitle = "${summary.count} 单 · 收入 ¥${formatCents(summary.revenueCents)} · 零件费 ¥${formatCents(summary.costCents)}",
            value = "¥${formatCents(summary.profitCents)}",
            progress = 1f,
            tint = profitColor
        )
    }
}
```

- [ ] **Step 6: Compile, run all tests, and commit the UI task**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` with zero failed tests.

Commit:

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyBarChart.kt app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt app/src/test/java/com/bigegg/tvrepairledger/ui/stats/DailyChartScaleTest.kt
git commit -m "feat: show daily statistics chart"
```

---

### Task 3: Versioned APK and Final Verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `README.md`

**Interfaces:**
- Consumes: the completed daily statistics feature.
- Produces: a versioned Debug APK named `电视维修记账_v1.13_debug.apk`.

- [ ] **Step 1: Update the application version and documented APK example**

Change `app/build.gradle.kts`:

```kotlin
val appVersionCode = 14
val appVersionName = "1.13"
```

Change the versioned APK example in `README.md` from `电视维修记账_v1.12_debug.apk` to `电视维修记账_v1.13_debug.apk`.

- [ ] **Step 2: Run the complete JVM test suite**

Run:

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` with zero failed tests.

- [ ] **Step 3: Build the Debug APK and verify the versioned copy exists**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
Get-Item -LiteralPath 'app\build\outputs\apk\versioned\电视维修记账_v1.13_debug.apk'
```

Expected: `BUILD SUCCESSFUL`; the APK exists and has a non-zero file size.

- [ ] **Step 4: Inspect the final diff and commit**

Run:

```powershell
git diff --check
git status --short
```

Expected: no whitespace errors; only intended project and existing unrelated user files appear.

Commit:

```powershell
git add app/build.gradle.kts README.md
git commit -m "build: bump app version to 1.13"
```
