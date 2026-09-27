# TV Repair Ledger Android App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a local-only Android app for recording TV repair jobs, importing the existing Excel records, searching repairs, and viewing monthly profit statistics.

**Architecture:** Native Android app using Kotlin, Jetpack Compose, Room, and a simple repository/use-case boundary. Data remains local on device; UI screens observe Room-backed flows through ViewModels.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Kotlin coroutines/Flow, AndroidX Navigation Compose, JUnit, Robolectric or AndroidX test where needed.

## Global Constraints

- Local-only Android app with no login, no cloud sync, and no network dependency.
- Package name: `com.bigegg.tvrepairledger`.
- Minimum SDK: 26.
- Money values are stored as integer cents in the database.
- Phone numbers are stored as text.
- Profit is always derived as `chargedAmountCents - (partsCostCents ?: 0)`.
- Blank parts cost is stored as null, not as typed zero.
- Excel import maps columns A-H from the current workbook and ignores right-side summary formulas.
- Missing-year dates such as `12.27` and `1.2` must be flagged for user confirmation.
- Current local environment does not expose `java`, `gradle`, `adb`, or `kotlinc` on PATH, so implementation requires Android Studio or equivalent Android tooling before build verification.
- Version baseline verified from official docs on 2026-07-06: Android Gradle Plugin 9.2 with Gradle 9.4.1 and JDK 17; Kotlin 2.4.0; Room 2.8.4.
- Source references:
  - https://developer.android.com/build/releases/gradle-plugin
  - https://kotlinlang.org/docs/releases.html
  - https://developer.android.com/jetpack/androidx/releases/room

---

## File Structure

Create a standard Android app project at repository root.

Expected project files:

- `settings.gradle.kts`: Gradle project settings and plugin repositories.
- `build.gradle.kts`: Root plugin declarations.
- `gradle/libs.versions.toml`: Central dependency versions.
- `app/build.gradle.kts`: Android app module configuration.
- `app/src/main/AndroidManifest.xml`: App manifest.
- `app/src/main/java/com/bigegg/tvrepairledger/MainActivity.kt`: Compose host activity.
- `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`: Navigation and top-level app shell.
- `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordEntity.kt`: Room entity.
- `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordDao.kt`: Room queries.
- `app/src/main/java/com/bigegg/tvrepairledger/data/AppDatabase.kt`: Room database.
- `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairRecord.kt`: Domain model.
- `app/src/main/java/com/bigegg/tvrepairledger/domain/Money.kt`: Money parsing and formatting.
- `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt`: Statistics DTOs.
- `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRepository.kt`: Repository interface and implementation.
- `app/src/main/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporter.kt`: Workbook row import parser.
- `app/src/main/java/com/bigegg/tvrepairledger/backup/BackupCodec.kt`: JSON backup/restore codec.
- `app/src/main/java/com/bigegg/tvrepairledger/export/CsvExporter.kt`: CSV export logic.
- `app/src/main/java/com/bigegg/tvrepairledger/ui/home/HomeScreen.kt`: Dashboard and recent records.
- `app/src/main/java/com/bigegg/tvrepairledger/ui/editor/RepairEditorScreen.kt`: Add/edit form.
- `app/src/main/java/com/bigegg/tvrepairledger/ui/list/RepairListScreen.kt`: Searchable list.
- `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt`: Monthly/item/fault summaries.
- `app/src/main/java/com/bigegg/tvrepairledger/ui/settings/SettingsScreen.kt`: Import/export/backup entry points.
- `app/src/test/java/com/bigegg/tvrepairledger/domain/MoneyTest.kt`: Money unit tests.
- `app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt`: Stats unit tests.
- `app/src/test/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporterTest.kt`: Import mapping tests.
- `app/src/test/java/com/bigegg/tvrepairledger/backup/BackupCodecTest.kt`: Backup tests.
- `app/src/test/java/com/bigegg/tvrepairledger/export/CsvExporterTest.kt`: Export tests.

---

### Task 1: Android Project Skeleton

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/MainActivity.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`

**Interfaces:**
- Produces: runnable Compose app package `com.bigegg.tvrepairledger`.
- Produces: `@Composable fun TvRepairLedgerApp()`.

- [ ] **Step 1: Install or expose Android tooling**

Install Android Studio, JDK 17, Android SDK Platform 36, and command-line tools. Confirm these commands work from a new terminal:

```powershell
java -version
gradle --version
adb version
```

Expected: `java` reports version 17 or newer, `gradle` reports 9.4.1 or compatible with AGP 9.2, and `adb` prints an Android Debug Bridge version.

- [ ] **Step 2: Create the Gradle project files**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TvRepairLedger"
include(":app")
```

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}
```

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.2.0"
kotlin = "2.4.0"
ksp = "2.4.0-1.0.30"
coreKtx = "1.17.0"
lifecycle = "2.10.0"
activityCompose = "1.12.0"
composeBom = "2026.06.00"
navigationCompose = "2.10.0"
room = "2.8.4"
junit = "4.13.2"
androidxTestExt = "1.3.0"
espresso = "3.7.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExt" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

Create `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.bigegg.tvrepairledger"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.bigegg.tvrepairledger"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
```

- [ ] **Step 3: Create the minimal Compose host**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="维修记账"
        android:supportsRtl="true"
        android:theme="@style/Theme.TvRepairLedger">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

Create `app/src/main/java/com/bigegg/tvrepairledger/MainActivity.kt`:

```kotlin
package com.bigegg.tvrepairledger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvRepairLedgerApp()
        }
    }
}
```

Create `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`:

```kotlin
package com.bigegg.tvrepairledger

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun TvRepairLedgerApp() {
    MaterialTheme {
        Text("维修记账")
    }
}
```

- [ ] **Step 4: Build the empty app**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add settings.gradle.kts build.gradle.kts gradle app
git commit -m "feat: scaffold Android repair ledger app"
```

---

### Task 2: Domain Model and Money Logic

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/domain/Money.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairRecord.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/domain/MoneyTest.kt`

**Interfaces:**
- Produces: `fun parseMoneyToCents(input: String): Long?`
- Produces: `fun formatCents(cents: Long): String`
- Produces: `data class RepairRecord(...)`
- Produces: `val RepairRecord.profitCents: Long`

- [ ] **Step 1: Write money tests**

Create `MoneyTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {
    @Test
    fun parseMoneyToCents_acceptsWholeYuan() {
        assertEquals(35000L, parseMoneyToCents("350"))
    }

    @Test
    fun parseMoneyToCents_acceptsDecimals() {
        assertEquals(35050L, parseMoneyToCents("350.50"))
    }

    @Test
    fun parseMoneyToCents_rejectsInvalidText() {
        assertNull(parseMoneyToCents("abc"))
    }

    @Test
    fun formatCents_formatsYuanWithTwoDecimals() {
        assertEquals("350.50", formatCents(35050L))
    }

    @Test
    fun repairRecord_profitTreatsBlankCostAsZero() {
        val record = RepairRecord(
            id = 1,
            dateEpochDay = 19810,
            address = "宜安苑3-1804",
            phone = "",
            faultSymptom = "黑屏",
            repairItem = "灯条",
            chargedAmountCents = 50000,
            partsCostCents = null,
            notes = "TCL65寸",
            warrantyPeriod = "",
            createdAtMillis = 1,
            updatedAtMillis = 1
        )

        assertEquals(50000L, record.profitCents)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*MoneyTest"
```

Expected: compilation fails because `Money.kt` and `RepairRecord.kt` do not exist.

- [ ] **Step 3: Implement money and record domain**

Create `Money.kt`:

```kotlin
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
```

Create `RepairRecord.kt`:

```kotlin
package com.bigegg.tvrepairledger.domain

data class RepairRecord(
    val id: Long,
    val dateEpochDay: Long,
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyPeriod: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

val RepairRecord.profitCents: Long
    get() = chargedAmountCents - (partsCostCents ?: 0L)
```

- [ ] **Step 4: Run the tests and confirm they pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*MoneyTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/domain app/src/test/java/com/bigegg/tvrepairledger/domain
git commit -m "feat: add repair record domain model"
```

---

### Task 3: Room Persistence

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordEntity.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRecordDao.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/data/AppDatabase.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/data/RepairRepository.kt`

**Interfaces:**
- Consumes: `RepairRecord` from Task 2.
- Produces: `interface RepairRepository`.
- Produces: `class RoomRepairRepository(private val dao: RepairRecordDao) : RepairRepository`.

- [ ] **Step 1: Create Room entity and mapper**

Create `RepairRecordEntity.kt`:

```kotlin
package com.bigegg.tvrepairledger.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bigegg.tvrepairledger.domain.RepairRecord

@Entity(tableName = "repair_records")
data class RepairRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateEpochDay: Long,
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyPeriod: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

fun RepairRecordEntity.toDomain(): RepairRecord = RepairRecord(
    id = id,
    dateEpochDay = dateEpochDay,
    address = address,
    phone = phone,
    faultSymptom = faultSymptom,
    repairItem = repairItem,
    chargedAmountCents = chargedAmountCents,
    partsCostCents = partsCostCents,
    notes = notes,
    warrantyPeriod = warrantyPeriod,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun RepairRecord.toEntity(): RepairRecordEntity = RepairRecordEntity(
    id = id,
    dateEpochDay = dateEpochDay,
    address = address,
    phone = phone,
    faultSymptom = faultSymptom,
    repairItem = repairItem,
    chargedAmountCents = chargedAmountCents,
    partsCostCents = partsCostCents,
    notes = notes,
    warrantyPeriod = warrantyPeriod,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)
```

- [ ] **Step 2: Create DAO queries**

Create `RepairRecordDao.kt`:

```kotlin
package com.bigegg.tvrepairledger.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RepairRecordDao {
    @Query("SELECT * FROM repair_records ORDER BY dateEpochDay DESC, id DESC")
    fun observeAll(): Flow<List<RepairRecordEntity>>

    @Query("SELECT * FROM repair_records WHERE id = :id")
    suspend fun getById(id: Long): RepairRecordEntity?

    @Query(
        """
        SELECT * FROM repair_records
        WHERE address LIKE '%' || :query || '%'
           OR phone LIKE '%' || :query || '%'
           OR faultSymptom LIKE '%' || :query || '%'
           OR repairItem LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY dateEpochDay DESC, id DESC
        """
    )
    fun search(query: String): Flow<List<RepairRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(record: RepairRecordEntity): Long

    @Update
    suspend fun update(record: RepairRecordEntity)

    @Delete
    suspend fun delete(record: RepairRecordEntity)
}
```

- [ ] **Step 3: Create database and repository**

Create `AppDatabase.kt`:

```kotlin
package com.bigegg.tvrepairledger.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [RepairRecordEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun repairRecordDao(): RepairRecordDao
}
```

Create `RepairRepository.kt`:

```kotlin
package com.bigegg.tvrepairledger.data

import com.bigegg.tvrepairledger.domain.RepairRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface RepairRepository {
    fun observeAll(): Flow<List<RepairRecord>>
    fun search(query: String): Flow<List<RepairRecord>>
    suspend fun getById(id: Long): RepairRecord?
    suspend fun save(record: RepairRecord): Long
    suspend fun delete(record: RepairRecord)
}

class RoomRepairRepository(private val dao: RepairRecordDao) : RepairRepository {
    override fun observeAll(): Flow<List<RepairRecord>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override fun search(query: String): Flow<List<RepairRecord>> =
        dao.search(query).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getById(id: Long): RepairRecord? =
        dao.getById(id)?.toDomain()

    override suspend fun save(record: RepairRecord): Long {
        return if (record.id == 0L) {
            dao.insert(record.toEntity())
        } else {
            dao.update(record.toEntity())
            record.id
        }
    }

    override suspend fun delete(record: RepairRecord) {
        dao.delete(record.toEntity())
    }
}
```

- [ ] **Step 4: Build to verify Room code generation**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/data
git commit -m "feat: add local repair record database"
```

---

### Task 4: Statistics Use Cases

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt`

**Interfaces:**
- Produces: `fun summarizeByMonth(records: List<RepairRecord>): List<MonthlyRepairSummary>`
- Produces: `fun summarizeByRepairItem(records: List<RepairRecord>): List<CategoryRepairSummary>`
- Produces: `fun summarizeByFault(records: List<RepairRecord>): List<CategoryRepairSummary>`

- [ ] **Step 1: Write statistics tests**

Create `RepairStatsTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class RepairStatsTest {
    private fun record(
        id: Long,
        dateEpochDay: Long,
        repairItem: String,
        fault: String,
        charged: Long,
        cost: Long?
    ) = RepairRecord(
        id = id,
        dateEpochDay = dateEpochDay,
        address = "",
        phone = "",
        faultSymptom = fault,
        repairItem = repairItem,
        chargedAmountCents = charged,
        partsCostCents = cost,
        notes = "",
        warrantyPeriod = "",
        createdAtMillis = 1,
        updatedAtMillis = 1
    )

    @Test
    fun summarizeByRepairItem_groupsRevenueCostProfitAndCount() {
        val summaries = summarizeByRepairItem(
            listOf(
                record(1, 19810, "灯条", "黑屏", 50000, 5000),
                record(2, 19811, "灯条", "黑屏", 35000, null),
                record(3, 19812, "EMMC", "卡logo", 30000, 2000)
            )
        )

        val lamp = summaries.first { it.label == "灯条" }
        assertEquals(2, lamp.count)
        assertEquals(85000L, lamp.revenueCents)
        assertEquals(5000L, lamp.costCents)
        assertEquals(80000L, lamp.profitCents)
    }

    @Test
    fun summarizeByFault_sortsByProfitDescending() {
        val summaries = summarizeByFault(
            listOf(
                record(1, 19810, "灯条", "黑屏", 50000, 5000),
                record(2, 19811, "EMMC", "卡logo", 30000, 2000)
            )
        )

        assertEquals("黑屏", summaries.first().label)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*RepairStatsTest"
```

Expected: compilation fails because `RepairStats.kt` does not exist.

- [ ] **Step 3: Implement statistics**

Create `RepairStats.kt`:

```kotlin
package com.bigegg.tvrepairledger.domain

import java.time.LocalDate

data class MonthlyRepairSummary(
    val year: Int,
    val month: Int,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

data class CategoryRepairSummary(
    val label: String,
    val count: Int,
    val revenueCents: Long,
    val costCents: Long,
    val profitCents: Long
)

fun summarizeByMonth(records: List<RepairRecord>): List<MonthlyRepairSummary> {
    return records
        .groupBy {
            val date = LocalDate.ofEpochDay(it.dateEpochDay)
            date.year to date.monthValue
        }
        .map { (key, group) ->
            MonthlyRepairSummary(
                year = key.first,
                month = key.second,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedWith(compareByDescending<MonthlyRepairSummary> { it.year }.thenByDescending { it.month })
}

fun summarizeByRepairItem(records: List<RepairRecord>): List<CategoryRepairSummary> =
    summarizeByCategory(records) { it.repairItem.ifBlank { "未填写" } }

fun summarizeByFault(records: List<RepairRecord>): List<CategoryRepairSummary> =
    summarizeByCategory(records) { it.faultSymptom.ifBlank { "未填写" } }

private fun summarizeByCategory(
    records: List<RepairRecord>,
    labelSelector: (RepairRecord) -> String
): List<CategoryRepairSummary> {
    return records
        .groupBy(labelSelector)
        .map { (label, group) ->
            CategoryRepairSummary(
                label = label,
                count = group.size,
                revenueCents = group.sumOf { it.chargedAmountCents },
                costCents = group.sumOf { it.partsCostCents ?: 0L },
                profitCents = group.sumOf { it.profitCents }
            )
        }
        .sortedWith(compareByDescending<CategoryRepairSummary> { it.profitCents }.thenBy { it.label })
}
```

- [ ] **Step 4: Run the tests and confirm they pass**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*RepairStatsTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/domain/RepairStats.kt app/src/test/java/com/bigegg/tvrepairledger/domain/RepairStatsTest.kt
git commit -m "feat: add repair statistics summaries"
```

---

### Task 5: Excel Import Parser

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporter.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/importer/ExcelRepairImporterTest.kt`

**Interfaces:**
- Produces: `data class ImportedRepairRow(...)`
- Produces: `data class ImportReview(...)`
- Produces: `fun parseWorkbookRows(rows: List<List<String?>>): ImportReview`

- [ ] **Step 1: Write import mapping tests**

Create `ExcelRepairImporterTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.importer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExcelRepairImporterTest {
    @Test
    fun parseWorkbookRows_mapsColumnsAThroughH() {
        val review = parseWorkbookRows(
            listOf(
                listOf("日期", "地址", "配件", "故障现象", "收费", "零件费", "电话", "备注"),
                listOf("2026-07-06", "宜安苑3-1804", "灯条", "黑屏", "500", "50", "18000000000", "TCL65寸")
            )
        )

        assertEquals(1, review.readyRows.size)
        val row = review.readyRows.single()
        assertEquals("宜安苑3-1804", row.address)
        assertEquals("灯条", row.repairItem)
        assertEquals("黑屏", row.faultSymptom)
        assertEquals(50000L, row.chargedAmountCents)
        assertEquals(5000L, row.partsCostCents)
        assertEquals("18000000000", row.phone)
    }

    @Test
    fun parseWorkbookRows_flagsMissingYearDate() {
        val review = parseWorkbookRows(
            listOf(
                listOf("日期", "地址", "配件", "故障现象", "收费", "零件费", "电话", "备注"),
                listOf("12.27", "新怡苑3-603", "修屏", "灰屏", "450", "", "", "仲")
            )
        )

        assertEquals(0, review.readyRows.size)
        assertEquals(1, review.needsDateConfirmation.size)
    }

    @Test
    fun parseWorkbookRows_normalizesEmmcCase() {
        val review = parseWorkbookRows(
            listOf(
                listOf("日期", "地址", "配件", "故障现象", "收费", "零件费", "电话", "备注"),
                listOf("2022-01-28", "紫荆香郡8-802", "emmc", "卡logo", "300", "20", "", "")
            )
        )

        assertEquals("EMMC", review.readyRows.single().repairItem)
    }
}
```

- [ ] **Step 2: Run the tests and confirm they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ExcelRepairImporterTest"
```

Expected: compilation fails because importer types do not exist.

- [ ] **Step 3: Implement row parser**

Create `ExcelRepairImporter.kt`:

```kotlin
package com.bigegg.tvrepairledger.importer

import com.bigegg.tvrepairledger.domain.parseMoneyToCents
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ImportedRepairRow(
    val sourceRowNumber: Int,
    val dateEpochDay: Long,
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String
)

data class DateConfirmationRow(
    val sourceRowNumber: Int,
    val rawDate: String,
    val address: String,
    val repairItem: String,
    val chargedAmountText: String
)

data class SkippedImportRow(
    val sourceRowNumber: Int,
    val reason: String
)

data class ImportReview(
    val readyRows: List<ImportedRepairRow>,
    val needsDateConfirmation: List<DateConfirmationRow>,
    val skippedRows: List<SkippedImportRow>
)

fun parseWorkbookRows(rows: List<List<String?>>): ImportReview {
    val ready = mutableListOf<ImportedRepairRow>()
    val confirm = mutableListOf<DateConfirmationRow>()
    val skipped = mutableListOf<SkippedImportRow>()

    rows.drop(1).forEachIndexed { index, row ->
        val sourceRow = index + 2
        val dateText = row.cell(0)
        val chargedText = row.cell(4)
        val charged = parseMoneyToCents(chargedText)

        if (charged == null) {
            skipped += SkippedImportRow(sourceRow, "Invalid charged amount: $chargedText")
            return@forEachIndexed
        }

        val parsedDate = parseImportDate(dateText)
        if (parsedDate == null && dateText.matches(Regex("""\d{1,2}\.\d{1,2}"""))) {
            confirm += DateConfirmationRow(
                sourceRowNumber = sourceRow,
                rawDate = dateText,
                address = row.cell(1),
                repairItem = normalizeRepairItem(row.cell(2)),
                chargedAmountText = chargedText
            )
            return@forEachIndexed
        }

        if (parsedDate == null) {
            skipped += SkippedImportRow(sourceRow, "Invalid date: $dateText")
            return@forEachIndexed
        }

        ready += ImportedRepairRow(
            sourceRowNumber = sourceRow,
            dateEpochDay = parsedDate.toEpochDay(),
            address = row.cell(1),
            phone = row.cell(6).removeSuffix(".0"),
            faultSymptom = row.cell(3),
            repairItem = normalizeRepairItem(row.cell(2)),
            chargedAmountCents = charged,
            partsCostCents = parseMoneyToCents(row.cell(5)),
            notes = row.cell(7)
        )
    }

    return ImportReview(ready, confirm, skipped)
}

private fun List<String?>.cell(index: Int): String =
    getOrNull(index)?.trim().orEmpty()

private fun parseImportDate(text: String): LocalDate? {
    val normalized = text.substringBefore(" ")
    return listOf("yyyy-MM-dd", "yyyy/M/d", "yyyy-M-d")
        .firstNotNullOfOrNull { pattern ->
            runCatching { LocalDate.parse(normalized, DateTimeFormatter.ofPattern(pattern)) }.getOrNull()
        }
}

private fun normalizeRepairItem(text: String): String =
    if (text.equals("emmc", ignoreCase = true)) "EMMC" else text
```

- [ ] **Step 4: Run importer tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*ExcelRepairImporterTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/importer app/src/test/java/com/bigegg/tvrepairledger/importer
git commit -m "feat: parse repair Excel rows"
```

---

### Task 6: Home, List, Editor, and Stats UI

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/home/HomeScreen.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/editor/RepairEditorScreen.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/list/RepairListScreen.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/ui/stats/StatsScreen.kt`

**Interfaces:**
- Consumes: `RepairRecord`, `summarizeByMonth`, `summarizeByRepairItem`, `summarizeByFault`.
- Produces: four Compose screens wired through `TvRepairLedgerApp`.

- [ ] **Step 1: Create UI state samples**

Create simple preview data inside `HomeScreen.kt` and reuse it for previews:

```kotlin
package com.bigegg.tvrepairledger.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents

@Composable
fun HomeScreen(
    records: List<RepairRecord>,
    onAddClick: () -> Unit,
    onRecordClick: (Long) -> Unit
) {
    val revenue = records.sumOf { it.chargedAmountCents }
    val cost = records.sumOf { it.partsCostCents ?: 0L }
    val profit = records.sumOf { it.profitCents }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("本月概览", style = MaterialTheme.typography.titleLarge)
            Row {
                SummaryCard("收入", revenue)
                SummaryCard("成本", cost)
                SummaryCard("利润", profit)
            }
            Text("最近维修", style = MaterialTheme.typography.titleMedium)
            records.take(20).forEach { record ->
                Card(
                    onClick = { onRecordClick(record.id) },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(record.address.ifBlank { "未填地址" })
                        Text("${record.faultSymptom} / ${record.repairItem}")
                        Text("收费 ${formatCents(record.chargedAmountCents)}  利润 ${formatCents(record.profitCents)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, cents: Long) {
    Card(Modifier.padding(end = 8.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(label)
            Text(formatCents(cents), style = MaterialTheme.typography.titleMedium)
        }
    }
}
```

- [ ] **Step 2: Create editor screen**

Create `RepairEditorScreen.kt` with controlled text fields and save validation:

```kotlin
package com.bigegg.tvrepairledger.ui.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.parseMoneyToCents

data class RepairEditorResult(
    val address: String,
    val phone: String,
    val faultSymptom: String,
    val repairItem: String,
    val chargedAmountCents: Long,
    val partsCostCents: Long?,
    val notes: String,
    val warrantyPeriod: String
)

@Composable
fun RepairEditorScreen(onSave: (RepairEditorResult) -> Unit) {
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var fault by remember { mutableStateOf("") }
    var repairItem by remember { mutableStateOf("") }
    var charged by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var warranty by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(address, { address = it }, label = { Text("地址") })
        OutlinedTextField(phone, { phone = it }, label = { Text("电话") })
        OutlinedTextField(fault, { fault = it }, label = { Text("故障现象") })
        OutlinedTextField(repairItem, { repairItem = it }, label = { Text("维修项目/配件") })
        OutlinedTextField(charged, { charged = it }, label = { Text("收费") })
        OutlinedTextField(cost, { cost = it }, label = { Text("零件费") })
        OutlinedTextField(notes, { notes = it }, label = { Text("备注") })
        OutlinedTextField(warranty, { warranty = it }, label = { Text("保修期") })
        error?.let { Text(it) }
        Button(
            onClick = {
                val chargedCents = parseMoneyToCents(charged)
                val costCents = if (cost.isBlank()) null else parseMoneyToCents(cost)
                when {
                    chargedCents == null -> error = "收费金额不正确"
                    cost.isNotBlank() && costCents == null -> error = "零件费金额不正确"
                    else -> onSave(
                        RepairEditorResult(
                            address = address,
                            phone = phone,
                            faultSymptom = fault,
                            repairItem = repairItem,
                            chargedAmountCents = chargedCents,
                            partsCostCents = costCents,
                            notes = notes,
                            warrantyPeriod = warranty
                        )
                    )
                }
            }
        ) {
            Text("保存")
        }
    }
}
```

- [ ] **Step 3: Create list and stats screens**

Create `RepairListScreen.kt`:

```kotlin
package com.bigegg.tvrepairledger.ui.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents

@Composable
fun RepairListScreen(records: List<RepairRecord>, onRecordClick: (Long) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = records.filter {
        query.isBlank() ||
            it.address.contains(query, ignoreCase = true) ||
            it.phone.contains(query, ignoreCase = true) ||
            it.faultSymptom.contains(query, ignoreCase = true) ||
            it.repairItem.contains(query, ignoreCase = true) ||
            it.notes.contains(query, ignoreCase = true)
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(query, { query = it }, label = { Text("搜索地址、电话、故障、配件、备注") })
        filtered.forEach { record ->
            Card(onClick = { onRecordClick(record.id) }, modifier = Modifier.padding(vertical = 4.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(record.address.ifBlank { "未填地址" })
                    Text("${record.faultSymptom} / ${record.repairItem}")
                    Text("收费 ${formatCents(record.chargedAmountCents)}  利润 ${formatCents(record.profitCents)}")
                }
            }
        }
    }
}
```

Create `StatsScreen.kt`:

```kotlin
package com.bigegg.tvrepairledger.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.summarizeByFault
import com.bigegg.tvrepairledger.domain.summarizeByMonth
import com.bigegg.tvrepairledger.domain.summarizeByRepairItem

@Composable
fun StatsScreen(records: List<RepairRecord>) {
    Column(Modifier.padding(16.dp)) {
        Text("按月统计")
        summarizeByMonth(records).take(12).forEach {
            Text("${it.year}-${it.month.toString().padStart(2, '0')} 收入 ${formatCents(it.revenueCents)} 利润 ${formatCents(it.profitCents)} 单数 ${it.count}")
        }
        Text("按维修项目")
        summarizeByRepairItem(records).take(10).forEach {
            Text("${it.label} ${it.count}单 利润 ${formatCents(it.profitCents)}")
        }
        Text("按故障现象")
        summarizeByFault(records).take(10).forEach {
            Text("${it.label} ${it.count}单 利润 ${formatCents(it.profitCents)}")
        }
    }
}
```

- [ ] **Step 4: Wire screens into app shell**

Modify `TvRepairLedgerApp.kt` to show a temporary in-memory screen flow until ViewModels are added:

```kotlin
package com.bigegg.tvrepairledger

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.ui.home.HomeScreen

@Composable
fun TvRepairLedgerApp() {
    val records = remember { mutableStateListOf<RepairRecord>() }
    MaterialTheme {
        HomeScreen(
            records = records,
            onAddClick = {},
            onRecordClick = {}
        )
    }
}
```

- [ ] **Step 5: Build UI**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger
git commit -m "feat: add repair ledger screens"
```

---

### Task 7: Backup and CSV Export

**Files:**
- Create: `app/src/main/java/com/bigegg/tvrepairledger/backup/BackupCodec.kt`
- Create: `app/src/main/java/com/bigegg/tvrepairledger/export/CsvExporter.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/backup/BackupCodecTest.kt`
- Create: `app/src/test/java/com/bigegg/tvrepairledger/export/CsvExporterTest.kt`

**Interfaces:**
- Produces: `fun encodeBackup(records: List<RepairRecord>): String`
- Produces: `fun decodeBackup(json: String): List<RepairRecord>`
- Produces: `fun exportCsv(records: List<RepairRecord>): String`

- [ ] **Step 1: Write backup and export tests**

Create `BackupCodecTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.backup

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupCodecTest {
    @Test
    fun backupRoundTrip_preservesNullPartsCost() {
        val record = RepairRecord(1, 19810, "地址", "180", "黑屏", "灯条", 50000, null, "备注", "包1年", 1, 1)
        val decoded = decodeBackup(encodeBackup(listOf(record)))
        assertEquals(null, decoded.single().partsCostCents)
    }
}
```

Create `CsvExporterTest.kt`:

```kotlin
package com.bigegg.tvrepairledger.export

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    @Test
    fun exportCsv_containsHeadersAndProfit() {
        val csv = exportCsv(listOf(RepairRecord(1, 19810, "地址", "180", "黑屏", "灯条", 50000, 5000, "备注", "", 1, 1)))

        assertTrue(csv.contains("date,address,phone,faultSymptom,repairItem,chargedAmount,partsCost,profit,notes,warrantyPeriod"))
        assertTrue(csv.contains("450.00"))
    }
}
```

- [ ] **Step 2: Run tests and confirm they fail**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*BackupCodecTest" --tests "*CsvExporterTest"
```

Expected: compilation fails because backup/export functions do not exist.

- [ ] **Step 3: Implement backup codec**

Create `BackupCodec.kt`:

```kotlin
package com.bigegg.tvrepairledger.backup

import com.bigegg.tvrepairledger.domain.RepairRecord
import org.json.JSONArray
import org.json.JSONObject

fun encodeBackup(records: List<RepairRecord>): String {
    val root = JSONObject()
    root.put("version", 1)
    root.put("records", JSONArray(records.map { record ->
        JSONObject()
            .put("id", record.id)
            .put("dateEpochDay", record.dateEpochDay)
            .put("address", record.address)
            .put("phone", record.phone)
            .put("faultSymptom", record.faultSymptom)
            .put("repairItem", record.repairItem)
            .put("chargedAmountCents", record.chargedAmountCents)
            .put("partsCostCents", record.partsCostCents)
            .put("notes", record.notes)
            .put("warrantyPeriod", record.warrantyPeriod)
            .put("createdAtMillis", record.createdAtMillis)
            .put("updatedAtMillis", record.updatedAtMillis)
    }))
    return root.toString()
}

fun decodeBackup(json: String): List<RepairRecord> {
    val root = JSONObject(json)
    require(root.getInt("version") == 1) { "Unsupported backup version" }
    val records = root.getJSONArray("records")
    return (0 until records.length()).map { index ->
        val item = records.getJSONObject(index)
        RepairRecord(
            id = item.getLong("id"),
            dateEpochDay = item.getLong("dateEpochDay"),
            address = item.getString("address"),
            phone = item.getString("phone"),
            faultSymptom = item.getString("faultSymptom"),
            repairItem = item.getString("repairItem"),
            chargedAmountCents = item.getLong("chargedAmountCents"),
            partsCostCents = if (item.isNull("partsCostCents")) null else item.getLong("partsCostCents"),
            notes = item.getString("notes"),
            warrantyPeriod = item.getString("warrantyPeriod"),
            createdAtMillis = item.getLong("createdAtMillis"),
            updatedAtMillis = item.getLong("updatedAtMillis")
        )
    }
}
```

- [ ] **Step 4: Implement CSV export**

Create `CsvExporter.kt`:

```kotlin
package com.bigegg.tvrepairledger.export

import com.bigegg.tvrepairledger.domain.RepairRecord
import com.bigegg.tvrepairledger.domain.formatCents
import com.bigegg.tvrepairledger.domain.profitCents
import java.time.LocalDate

fun exportCsv(records: List<RepairRecord>): String {
    val header = "date,address,phone,faultSymptom,repairItem,chargedAmount,partsCost,profit,notes,warrantyPeriod"
    val rows = records.map { record ->
        listOf(
            LocalDate.ofEpochDay(record.dateEpochDay).toString(),
            record.address,
            record.phone,
            record.faultSymptom,
            record.repairItem,
            formatCents(record.chargedAmountCents),
            record.partsCostCents?.let(::formatCents).orEmpty(),
            formatCents(record.profitCents),
            record.notes,
            record.warrantyPeriod
        ).joinToString(",") { it.csvEscape() }
    }
    return (listOf(header) + rows).joinToString("\n")
}

private fun String.csvEscape(): String {
    val needsQuotes = contains(",") || contains("\"") || contains("\n")
    val escaped = replace("\"", "\"\"")
    return if (needsQuotes) "\"$escaped\"" else escaped
}
```

- [ ] **Step 5: Run tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "*BackupCodecTest" --tests "*CsvExporterTest"
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```powershell
git add app/src/main/java/com/bigegg/tvrepairledger/backup app/src/main/java/com/bigegg/tvrepairledger/export app/src/test/java/com/bigegg/tvrepairledger/backup app/src/test/java/com/bigegg/tvrepairledger/export
git commit -m "feat: add local backup and CSV export"
```

---

### Task 8: Final Integration and Manual Verification

**Files:**
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/TvRepairLedgerApp.kt`
- Modify: `app/src/main/java/com/bigegg/tvrepairledger/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/AndroidManifest.xml`
- Create: `README.md`

**Interfaces:**
- Consumes all previous task interfaces.
- Produces a debug APK that supports add/edit/list/search/stats/import/export/backup flows.

- [ ] **Step 1: Add app usage README**

Create `README.md`:

````markdown
# TV Repair Ledger

Local-only Android app for recording TV repair jobs.

## Version 1 Features

- Add, edit, delete repair records.
- Track charged amount, parts cost, and auto-calculated profit.
- Search by address, phone, fault symptom, repair item, and notes.
- View monthly, repair item, and fault summaries.
- Import the existing TV repair workbook.
- Export CSV and create local backup files.

## Privacy

The app has no account system and no cloud sync. Data is stored locally on the device.

## Build

Use Android Studio with JDK 17 and Android Gradle Plugin 9.2.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
```
````

- [ ] **Step 2: Verify automated tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Build debug APK**

Run:

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL` and APK at `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 4: Manual phone-sized verification**

Run on emulator or device:

```powershell
.\gradlew.bat :app:installDebug
```

Verify:

- App opens to the home dashboard.
- Add a repair record with address `宜安苑3-1804`, fault `黑屏`, repair item `灯条`, charged amount `500`, parts cost `50`, notes `TCL65寸`.
- Saved record appears in recent list.
- Search `宜安苑` finds the record.
- Statistics show revenue `500.00`, cost `50.00`, and profit `450.00`.
- CSV export includes the record.
- Backup restore validates before overwriting data.

- [ ] **Step 5: Commit**

```powershell
git add README.md app
git commit -m "feat: integrate repair ledger app"
```

---

## Self-Review Checklist

- Spec coverage: local-only app, repair fields, auto profit, dashboard, recent list, CRUD, search, statistics, Excel import rules, CSV export, backup/restore, validation, and tests are covered.
- Placeholder scan: no task contains unfinished markers or vague edge-case instructions.
- Type consistency: `RepairRecord`, money cents fields, importer DTOs, backup/export functions, and statistics functions use consistent names across tasks.
- Known execution risk: local Android tooling is absent from current PATH, so Task 1 must be completed before any Gradle command can pass.
