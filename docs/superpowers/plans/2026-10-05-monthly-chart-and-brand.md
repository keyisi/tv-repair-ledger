# Monthly Chart and Repair Brand Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add an income/profit bar chart with a 6-month / 12-month / all-months range switch to the existing “按月” statistics tab, and add a `品牌` field to repair records across entry, list, search, option management, Excel import/export, and backup.

**Architecture:** One shared `BarChartPoint` + `BarChartScale` + `LedgerBarChart` model renders both the daily and the monthly chart, so plotting logic exists in exactly one place. `StatsScreen` owns the selected monthly range; `domain/RepairStats` owns continuous zero-filled month aggregation. The `品牌` field is added to the domain record and Room entity, migrated via a new `MIGRATION_3_4`, and the workbook exporter becomes self-describing while the importer switches from fixed column positions to header-name matching.

**Tech Stack:** Kotlin 2.3, Jetpack Compose Material 3, Room 2.8, Java Time, JUnit 4, Gradle Android plugin 9.2.

## Global Constraints

- Daily statistics and the daily detail list must keep their existing behavior and visuals.
- The monthly range choices are exactly `6月`, `12月`, `全部`; default to `6月`.
- Month labels use `yyyy-MM`; missing months are zero-filled, oldest on the left, newest on the right.
- Do not add a third-party chart dependency.
- `品牌` is a free-text field that may be blank; a blank brand must not break list rendering, search, export, or import.
- The Room migration must only add a column and must preserve all existing rows.
- Previously exported Chinese-header and English-header workbooks must still import successfully.
- Set `versionCode` to 16 and `versionName` to `1.15`.
- Produce `电视维修记账_v1.15_debug.apk`.

---

### Task 1: Shared Bar Chart, Monthly Chart, and Range Switch

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/BarChart.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/MonthlyChartRange.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/MonthlyBarChart.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/DailyBarChart.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/BarChartScaleTest.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/BarChartPointTest.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/MonthlyChartRangeTest.kt`
- Delete: `app/src/test/java/com/bigegg/tvrepairledger/ui/stats/DailyChartScaleTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt`

**Interfaces:**
- Consumes: `summarizeByMonth`, `summarizeRecentDays`, `DailyRepairSummary`, `MonthlyRepairSummary`.
- Produces: `BarChartPoint`, `BarChartScale`, `calculateBarChartScale`, `LedgerBarChart`, `dailySummariesToChartPoints`, `monthlySummariesToChartPoints`, `MonthlyChartRange`, `summariesByMonthAscending`, `summarizeRecentMonths`.

- [ ] **Step 1: Extract the shared chart**

Move the existing `DailyBarChart` drawing body into `LedgerBarChart(points: List<BarChartPoint>, modifier, minimumBarWidth)` unchanged except that it iterates `points` and reads `point.label`. Keep the zero-line, the revenue bar offset, the negative-profit bar drawn below the zero line, `DangerRed` loss coloring, horizontal scrolling, and the auto-scroll-to-latest effect.

Replace `DailyChartScale`/`calculateDailyChartScale` with the generic `BarChartScale`/`calculateBarChartScale`, and reduce `DailyBarChart` to a thin wrapper over `LedgerBarChart`.

- [ ] **Step 2: Add zero-filled month aggregation**

In `domain/RepairStats.kt` add `summariesByMonthAscending(records)` and `summarizeRecentMonths(records, endYearMonth, monthCount)`; the latter requires `monthCount > 0`, starts at `endYearMonth.minusMonths(monthCount - 1)`, and substitutes zero summaries for months without records.

- [ ] **Step 3: Add the range model and the monthly chart**

`MonthlyChartRange` exposes `label`, `title`, `monthCount: Int?`, and `isAll`, declared as `HalfYear`, `Year`, `All`.

`MonthlyBarChart` formats labels as `yyyy-MM` and defaults `minimumMonthWidth` to `56.dp`.

- [ ] **Step 4: Wire the 按月 tab**

In `StatsScreen`, add `monthlyChartRange` state defaulting to `HalfYear`, compute the chart summaries from the selected range, and render a chart card above the existing 「月度表现」 section header. The card header shows `monthlyChartRange.title` and the three segmented buttons. When the computed summaries are empty, show `暂无月度数据` instead of an empty canvas.

- [ ] **Step 5: Run the full test suite and build the APK**

```bash
source ~/.config/android/env.sh
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`, zero failed tests.

---

### Task 2: Brand Field End to End

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairRecord.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordEntity.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/AppDatabase.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordDao.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/MainActivity.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/options/CommonOptionStore.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/editor/RepairEditorScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/list/RepairListScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/data/RepairRecordEntityMappingTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/options/CommonOptionStoreTest.kt`

**Interfaces:**
- Consumes: the existing `CommonOptionStore` option pattern and the editor `OptionTextField`.
- Produces: `RepairRecord.brand`, `RepairRecordEntity.brand`, `AppDatabase.MIGRATION_3_4`, `AppDatabase.ALL_MIGRATIONS`, `CommonOptionStore.loadBrandOptions/saveBrandOptions/defaultBrandOptions`, `RepairEditorDraft.brand`.

- [ ] **Step 1: Add the field to the domain and the entity**

Append `val brand: String = ""` to `RepairRecord` (after `repairDevice`) and to `RepairRecordEntity`, and map it in both `toDomain()` and `toEntity()`.

- [ ] **Step 2: Migrate the database**

Bump `@Database(version = 4)` and add `MIGRATION_3_4` running `ALTER TABLE repair_records ADD COLUMN brand TEXT NOT NULL DEFAULT ''`. Expose `ALL_MIGRATIONS` and register it in `MainActivity` with `addMigrations(*AppDatabase.ALL_MIGRATIONS)`.

- [ ] **Step 3: Add brand options**

Add `KEY_BRANDS`, `loadBrandOptions()`, `saveBrandOptions()`, and `defaultBrandOptions` (20 common TV brands) to `CommonOptionStore`, mirroring the repair-device option pattern.

- [ ] **Step 4: Add the editor field and settings management**

Add `brand` to `RepairEditorDraft` and a `brandOptions` parameter to `RepairEditorScreen`; render an `OptionTextField` labeled `品牌` directly below `维修设备` and pass the value through on save. Add a `常用品牌` `OptionManagementCard` to `SettingsScreen` below `常用维修设备`.

- [ ] **Step 5: Show and search the brand**

Add `record.brand` to the ledger search fields and render a brand `InfoChip` in both the ledger list item and the home recent-record card. Convert both chip rows to `FlowRow` so a fourth and fifth chip wrap instead of overflowing.

- [ ] **Step 6: Wire the app state**

Hold `brandOptions` state in `TvRepairLedgerApp`, pass it to the editor and the settings screen with add/delete handlers, and map `brand = brand.trim()` in `RepairEditorDraft.toRecord`.

- [ ] **Step 7: Run the full test suite and build the APK**

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`; `app/schemas/.../4.json` is generated and contains the `brand` column.

---

### Task 3: Excel, CSV, and Backup Carry the Brand

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/xlsx/RepairWorkbookRows.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporter.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/export/CsvExporter.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/backup/BackupCodec.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/xlsx/XlsxWorkbookCodecTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/export/CsvExporterTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/backup/BackupCodecTest.kt`
- Modify: `app/src/test/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporterTest.kt`

**Interfaces:**
- Consumes: `formatCents`, `profitCents`, `ImportedRepairRow.toRepairRecord`.
- Produces: a self-describing `repairWorkbookHeader`, header-name-driven `WorkbookMapping`, `ImportedRepairRow.brand`, `ImportedRepairRow.repairDevice`.

- [ ] **Step 1: Make the export self-describing**

Export `日期 / 客户姓名 / 品牌 / 联系电话 / 客户地址 / 维修设备 / 故障现象 / 维修项目 / 收费 / 零件费 / 利润 / 保修天数 / 备注`, covering every business field so a round trip loses nothing.

- [ ] **Step 2: Match columns by header name**

Replace the positional `WorkbookMapping.fromHeader` with name matching that accepts both Chinese and English aliases, ignores derived columns such as `利润`, and falls back to the legacy fixed order when `日期`/`date` and `收费`/`charged` cannot both be resolved. Carry `brand` and `repairDevice` through `ImportedRepairRow.toRepairRecord`.

- [ ] **Step 3: Add the brand to CSV and backup**

Insert `brand` into the CSV header and row, and encode/decode `brand` in the backup JSON using `optString("brand", "")` so older backups still decode.

- [ ] **Step 4: Prove the round trip and the legacy paths**

Tests must assert: export→import preserves every business field; the legacy Chinese header and the legacy English header still import; a scrambled column order still imports; an unrecognized header falls back to fixed positions; a non-blank `零件费` is not confused with `利润`; backups without a `brand` key decode to an empty brand.

- [ ] **Step 5: Run the full test suite**

```bash
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`, zero failed tests.

---

### Task 4: Version 1.15, Emulator Verification, and Commit

**Files:**
- Modify: `app/build.gradle.kts`
- Modify: `README.md`
- Create: `docs/superpowers/specs/2026-10-05-monthly-chart-and-brand-design.md`
- Create: `docs/superpowers/plans/2026-10-05-monthly-chart-and-brand.md`

**Interfaces:**
- Consumes: the completed monthly chart and brand field.
- Produces: `电视维修记账_v1.15_debug.apk` and pushed commits.

- [ ] **Step 1: Bump the version and update the README**

Set `appVersionCode = 16` and `appVersionName = "1.15"`, refresh the feature list and the APK filename in `README.md`.

- [ ] **Step 2: Verify the database migration on an emulator against a real old database**

Capture the schema-3 database created by the previously installed build, insert a legacy row (no `brand` column), push it back, install the new APK, launch it, and confirm the row survived with an empty brand and that the `brand` column now exists.

- [ ] **Step 3: Verify the new UI on the emulator**

Confirm the `品牌` field and dropdown appear in the editor, the `常用品牌` card appears on the More tab, the `按月` tab shows the chart with working `6月 / 12月 / 全部` switching, and that a record saved with a brand displays that brand in the list.

- [ ] **Step 4: Build and inspect the versioned APK**

```bash
./gradlew :app:assembleDebug
ls -l app/build/outputs/apk/versioned/电视维修记账_v1.15_debug.apk
```

- [ ] **Step 5: Commit and push**

```bash
git add -A
git commit -m "feat: monthly income/profit chart with range switch and repair brand field"
git push
```
