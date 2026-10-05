# TV Repair Ledger

Local-only Android app for recording TV repair jobs.

## Current Features

- Add, edit, and delete repair records in a Compose UI backed by Room persistence.
- Track date, customer name, repair device, brand, address, phone, fault symptom, repair item, charged amount, parts cost, notes, and warranty days.
- Store money values as integer cents in app logic.
- Treat blank parts cost as null and derive profit as charged amount minus parts cost.
- Search records by customer name, address, phone, repair device, brand, fault symptom, repair item, and notes.
- Manage common address, repair device, brand, fault symptom, and repair item options from the More tab.
- Keep common address, fault symptom, and repair item management collapsed by default in the More tab.
- Search 100 preset nearby community names from the More tab and prioritize recently selected addresses when filling a repair record.
- Select common address, repair device, brand, fault symptom, and repair item values from the small button at the end of each input field.
- Default warranty to 90 days, show remaining warranty days, and highlight expired warranty records.
- View daily and monthly income/profit bar charts, plus repair item and fault summaries.
- Switch the daily chart between the latest 7 and 30 days, and the monthly chart between the latest 6 months, latest 12 months, and all recorded months.
- Import real `.xlsx` files through Android's file picker and save ready rows into the local database.
- Export the current ledger as a self-describing `.xlsx` workbook covering every business field: 日期, 客户姓名, 品牌, 联系电话, 客户地址, 维修设备, 故障现象, 维修项目, 收费, 零件费, 利润, 保修天数, 备注. Importing a previously exported workbook restores every field.
- Import still recognizes older workbook layouts, including the legacy Chinese header and the English header, and matches columns by header name so the column order can be rearranged.

## Privacy

The app has no account system, no cloud sync, and no network dependency. Records and common dropdown options are stored locally on the device.

## Build

Use Android SDK 36 with the Gradle wrapper. On macOS the toolchain is wired up by `~/.config/android/env.sh` (JDK 21 + Android SDK), so a login shell is enough:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

On Windows, point `JAVA_HOME` at Android Studio's bundled JBR and run `.\gradlew.bat` instead:

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

The installable debug APK is also copied to a versioned filename such as `app/build/outputs/apk/versioned/电视维修记账_v1.15_debug.apk`.
