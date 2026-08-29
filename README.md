# TV Repair Ledger

Local-only Android app for recording TV repair jobs.

## Current Features

- Add, edit, and delete repair records in a Compose UI backed by Room persistence.
- Track date, customer name, repair device, address, phone, fault symptom, repair item, charged amount, parts cost, notes, and warranty days.
- Store money values as integer cents in app logic.
- Treat blank parts cost as null and derive profit as charged amount minus parts cost.
- Search records by customer name, address, phone, fault symptom, repair item, and notes.
- Manage common address, repair device, fault symptom, and repair item options from the More tab.
- Keep common address, fault symptom, and repair item management collapsed by default in the More tab.
- Search 100 preset nearby community names from the More tab and prioritize recently selected addresses when filling a repair record.
- Select common address, fault symptom, and repair item values from the small button at the end of each input field.
- Default warranty to 90 days, show remaining warranty days, and highlight expired warranty records.
- View monthly, repair item, and fault summaries.
- Import real `.xlsx` files through Android's file picker and save ready rows into the local database.
- Export the current ledger as a `.xlsx` workbook with columns: 日期, 地址, 配件, 故障现象, 收费, 零件费, 电话, 备注, blank separator, 合计, 零件费, 利润.

## Privacy

The app has no account system, no cloud sync, and no network dependency. Records and common dropdown options are stored locally on the device.

## Build

Use Android Studio with the bundled JBR, Android SDK 36, and Gradle wrapper.

```powershell
$env:JAVA_HOME='D:\Program Files\Android\Android Studio\jbr'
$env:ANDROID_HOME='D:\AndroidSDK'
$env:ANDROID_SDK_ROOT='D:\AndroidSDK'
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

The installable debug APK is also copied to a versioned filename such as `app/build/outputs/apk/versioned/电视维修记账_v1.14_debug.apk`.
