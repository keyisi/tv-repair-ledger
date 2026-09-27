# Repair Ledger Persistence and XLSX Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the Android repair ledger to persist records in Room, support add/edit/delete, warranty tracking, reusable dropdown options, and real `.xlsx` import/export.

**Architecture:** Keep domain logic pure and tested. Use Room through `RepairRepository` for records, a small SharedPreferences-backed store for common dropdown options, and a lightweight OpenXML `.xlsx` codec for import/export.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Android Activity Result contracts, Java Zip/XML APIs, JUnit.

## Global Constraints

- Local-only app with no login, no cloud sync, and no network dependency.
- Package name remains `com.bigegg.tvrepairledger`.
- Money values stay as integer cents.
- Blank parts cost remains null.
- Profit remains derived as `chargedAmountCents - (partsCostCents ?: 0)`.
- Warranty defaults to `90` days.
- `.xlsx` import/export uses Android file picker/create document.
- No Apache POI unless the lightweight OpenXML codec proves insufficient.

---

### Task 1: Domain and Room Model Upgrade

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairRecord.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordEntity.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordDao.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/AppDatabase.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRepository.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/domain/Warranty.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/domain/WarrantyTest.kt`

**Interfaces:**
- Add `customerName: String = ""` and `warrantyDays: Int = 90`.
- Add `remainingWarrantyDays(record: RepairRecord, todayEpochDay: Long): Int`.
- Add `RepairRepository.deleteById(id: Long)`.

### Task 2: Room-Backed App Shell and Editor CRUD

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/MainActivity.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/editor/RepairEditorScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/list/RepairListScreen.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/home/HomeScreen.kt`

**Interfaces:**
- `TvRepairLedgerApp(repository: RepairRepository, optionStore: CommonOptionStore)`.
- Editor accepts an optional existing `RepairRecord`.
- Ledger click opens edit, and edit supports delete.

### Task 3: Common Dropdown Options

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/options/CommonOptionStore.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/editor/RepairEditorScreen.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/options/CommonOptionStoreTest.kt`

**Interfaces:**
- `CommonOptionStore.loadFaultOptions()`
- `CommonOptionStore.loadRepairItemOptions()`
- `CommonOptionStore.saveFaultOptions(options)`
- `CommonOptionStore.saveRepairItemOptions(options)`

### Task 4: XLSX Codec and Import Mapping

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/xlsx/XlsxWorkbookCodec.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporter.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/xlsx/XlsxWorkbookCodecTest.kt`

**Interfaces:**
- `writeWorkbook(rows: List<List<String>>): ByteArray`
- `readWorkbookRows(input: InputStream): List<List<String?>>`
- Import supports legacy A-H and app-exported columns.

### Task 5: Android File Picker Import/Export UI

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/settings/SettingsScreen.kt`

**Interfaces:**
- Import from selected `.xlsx` URI, preview counts, insert ready records.
- Export current Room records to created `.xlsx` URI.

### Task 6: Final Verification

**Files:**
- Modify: `README.md`

**Verification:**
- Run `.\gradlew.bat :app:testDebugUnitTest`.
- Run `.\gradlew.bat :app:assembleDebug`.
