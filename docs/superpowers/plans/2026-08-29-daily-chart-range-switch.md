# Daily Chart Range Switch Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a default one-week and optional rolling 30-day range switch to the existing daily income/profit chart.

**Architecture:** Introduce a focused `DailyChartRange` enum as the single source of labels and day counts. `StatsScreen` owns the selected range and requests 7 or 30 continuous summaries from the existing domain function; `DailyBarChart` owns only responsive rendering, horizontal scrolling, and latest-date positioning.

**Tech Stack:** Kotlin 2.3, Jetpack Compose Material 3, Java Time, JUnit 4, Gradle Android plugin 9.2.

## Global Constraints

- Keep the existing “按月” statistics page unchanged; the switch belongs only to the “按日” chart.
- The choices are exactly `1周` and `1月`; default to `1周`.
- `1周` means the latest 7 calendar days; `1月` means the latest 30 calendar days ending on the observable current local date.
- Missing dates remain zero-filled in the chart; the all-days detail list remains unchanged.
- The 7-day chart fills the available card width without horizontal scrolling.
- The 30-day chart preserves readable day widths, scrolls horizontally, and initially shows the latest dates.
- Do not add a third-party chart dependency.
- Preserve loss coloring, the four statistics tabs, and the lifecycle-aware current-date refresh.
- Set `versionCode` to 15 and `versionName` to `1.14`.
- Produce `电视维修记账_v1.14_debug.apk`.

---

### Task 1: Range Model, 30-Day Data, and Scrollable Chart UI

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyChartRange.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/DailyChartRangeTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyBarChart.kt`

**Interfaces:**
- Consumes: `summarizeRecentDays(records, endDateEpochDay, dayCount)` and `DailyRepairSummary` from the completed daily-statistics feature.
- Produces: `DailyChartRange.Week`, `DailyChartRange.Month`, and a chart that scrolls only when its computed content is wider than the viewport.

- [ ] **Step 1: Write failing tests for exact range mappings**

Create `DailyChartRangeTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.ui.stats

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyChartRangeTest {
    @Test
    fun ranges_useTheRequiredLabelsAndDayCounts() {
        assertEquals("1周", DailyChartRange.Week.label)
        assertEquals(7, DailyChartRange.Week.dayCount)
        assertEquals("1月", DailyChartRange.Month.label)
        assertEquals(30, DailyChartRange.Month.dayCount)
    }
}
```

- [ ] **Step 2: Run the focused range test and verify RED**

Run:

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.ui.stats.DailyChartRangeTest"
```

Expected: Kotlin test compilation fails because `DailyChartRange` does not exist.

- [ ] **Step 3: Implement the range model**

Create `DailyChartRange.kt`:

```kotlin
package com.bigegg.tvrepairledger.ui.stats

enum class DailyChartRange(
    val label: String,
    val dayCount: Int
) {
    Week(label = "1周", dayCount = 7),
    Month(label = "1月", dayCount = 30)
}
```

- [ ] **Step 4: Run the focused range test and verify GREEN**

Run the command from Step 2.

Expected: `BUILD SUCCESSFUL` and the range test passes.

- [ ] **Step 5: Write a failing 30-day boundary and zero-fill test**

Add to `RepairStatsTest.kt`:

```kotlin
@Test
fun summarizeRecentDays_supportsThirtyContinuousDaysWithBoundaryRecords() {
    val summaries = summarizeRecentDays(
        records = listOf(
            record(1, 171, "lamp", "black-screen", 50000, 5000),
            record(2, 200, "EMMC", "boot-logo", 30000, 2000),
            record(3, 170, "old", "old", 99999, null)
        ),
        endDateEpochDay = 200,
        dayCount = 30
    )

    assertEquals(30, summaries.size)
    assertEquals((171L..200L).toList(), summaries.map { it.dateEpochDay })
    assertEquals(45000L, summaries.first().profitCents)
    assertEquals(0, summaries[1].count)
    assertEquals(28000L, summaries.last().profitCents)
}
```

- [ ] **Step 6: Verify the 30-day test protects the intended behavior**

Temporarily change the test input from `dayCount = 30` to `dayCount = 29`, run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.bigegg.tvrepairledger.domain.RepairStatsTest.summarizeRecentDays_supportsThirtyContinuousDaysWithBoundaryRecords"
```

Expected: assertion failure because the range contains 29 rather than 30 days. Restore the test input to `dayCount = 30`, rerun the same command, and expect `BUILD SUCCESSFUL`. This keeps domain tests independent from the UI range enum.

- [ ] **Step 7: Add range state and segmented controls to `StatsScreen`**

Add state and use the selected day count:

```kotlin
var chartRange by remember { mutableStateOf(DailyChartRange.Week) }

val recentDaily = summarizeRecentDays(
    records = records,
    endDateEpochDay = todayEpochDay,
    dayCount = chartRange.dayCount
)
```

Replace the daily chart card header with:

```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
) {
    Text(
        text = "最近 ${chartRange.dayCount} 天",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    SingleChoiceSegmentedButtonRow {
        DailyChartRange.entries.forEachIndexed { index, range ->
            SegmentedButton(
                selected = chartRange == range,
                onClick = { chartRange = range },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = DailyChartRange.entries.size
                ),
                label = { Text(range.label) }
            )
        }
    }
}
DailyBarChart(
    summaries = recentDaily,
    modifier = Modifier.fillMaxWidth(),
    minimumDayWidth = if (chartRange == DailyChartRange.Month) 42.dp else 0.dp
)
```

Add the exact imports required by the snippet: `Alignment`, `mutableStateOf`, `SingleChoiceSegmentedButtonRow`, `SegmentedButton`, and `SegmentedButtonDefaults`. Do not modify the daily detail source or any non-daily tab branch.

- [ ] **Step 8: Make only the chart plot horizontally scrollable**

Change the `DailyBarChart` signature:

```kotlin
@Composable
fun DailyBarChart(
    summaries: List<DailyRepairSummary>,
    modifier: Modifier = Modifier,
    minimumDayWidth: Dp = 0.dp
)
```

Use a viewport-aware plot while leaving the legend outside the scroll container:

```kotlin
val scrollState = rememberScrollState()
val rangeKey = summaries.firstOrNull()?.dateEpochDay to summaries.lastOrNull()?.dateEpochDay

Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val chartWidth = maxOf(maxWidth, minimumDayWidth * summaries.size)
        val maxScroll = scrollState.maxValue

        LaunchedEffect(rangeKey, maxScroll) {
            if (maxScroll > 0) scrollState.scrollTo(maxScroll)
        }

        Column(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .width(chartWidth),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val zeroY = size.height * scale.maxPositiveCents.toFloat() / totalRange.toFloat()
                drawLine(
                    color = LineSoft,
                    start = Offset(0f, zeroY),
                    end = Offset(size.width, zeroY),
                    strokeWidth = 1.dp.toPx()
                )
                val groupWidth = size.width / summaries.size.coerceAtLeast(1)
                val barWidth = (groupWidth * 0.25f).coerceAtLeast(4.dp.toPx())

                summaries.forEachIndexed { index, summary ->
                    val centerX = groupWidth * (index + 0.5f)
                    val revenueHeight = zeroY * summary.revenueCents.toFloat() /
                        scale.maxPositiveCents.toFloat()
                    drawRect(
                        color = RepairBlue,
                        topLeft = Offset(
                            centerX - barWidth - 1.dp.toPx(),
                            zeroY - revenueHeight
                        ),
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
                        topLeft = Offset(
                            centerX + 1.dp.toPx(),
                            if (profit < 0L) zeroY else zeroY - profitHeight
                        ),
                        size = Size(barWidth, profitHeight)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                summaries.forEach { summary ->
                    Text(
                        text = LocalDate.ofEpochDay(summary.dateEpochDay).format(dateFormatter),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelSmall,
                        color = Ink500,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        ChartLegendItem("收入", RepairBlue)
        ChartLegendItem("利润", ProfitGreen)
        if (summaries.any { it.profitCents < 0L }) ChartLegendItem("亏损", DangerRed)
    }
}
```

Add imports for `BoxWithConstraints`, `horizontalScroll`, `rememberScrollState`, `width`, `Dp`, and `LaunchedEffect`. Keep the existing imports for `Offset`, `Size`, `abs`, and chart theme colors used by the complete drawing block above.

- [ ] **Step 9: Run the full test suite and compile the Compose UI**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

Expected: both commands finish with `BUILD SUCCESSFUL`; no new warnings beyond the existing experimental path warning.

- [ ] **Step 10: Commit Task 1**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyChartRange.kt app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyBarChart.kt app/src/test/java/com/bigegg/tvrepairledger/ui/stats/DailyChartRangeTest.kt app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt
git commit -m "feat: switch daily chart range"
```

---

### Task 2: Version 1.14 APK and Final Verification

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `README.md`

**Interfaces:**
- Consumes: the completed daily chart range switch.
- Produces: `电视维修记账_v1.14_debug.apk`.

- [ ] **Step 1: Update exact version values and documentation**

Change `app/build.gradle.kts`:

```kotlin
val appVersionCode = 15
val appVersionName = "1.14"
```

Change the README APK example to:

```text
电视维修记账_v1.14_debug.apk
```

- [ ] **Step 2: Run all unit tests**

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL` with zero failed tests.

- [ ] **Step 3: Build and verify the versioned APK**

```powershell
.\gradlew.bat :app:assembleDebug
Get-Item -LiteralPath 'app\build\outputs\apk\versioned\电视维修记账_v1.14_debug.apk'
```

Expected: `BUILD SUCCESSFUL`; the exact APK exists with a non-zero length.

- [ ] **Step 4: Inspect and commit the version task**

```powershell
git diff --check
git status --short
git add app/build.gradle.kts README.md
git commit -m "build: bump app version to 1.14"
```

Expected: no whitespace errors; unrelated staged and untracked user files remain preserved.
