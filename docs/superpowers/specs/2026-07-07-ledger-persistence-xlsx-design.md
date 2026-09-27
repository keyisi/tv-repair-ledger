# Repair Ledger Persistence and XLSX Design

## Goal

Upgrade the TV repair ledger from an in-memory prototype into a local persistent Android app that can add, edit, delete, import, and export real repair records.

## Scope

- Store records in Room instead of in-memory sample data.
- Add customer name and warranty days to repair records.
- Let the Android back button leave the add/edit screen.
- Support add, edit, and delete from the repair ledger.
- Show repair date in the ledger.
- Show remaining warranty days; expired records are highlighted red.
- Provide dropdown-style editable common options for fault symptoms and repair items.
- Let users add and delete common fault/repair options locally.
- Import `.xlsx` files through Android file picker.
- Export `.xlsx` files through Android create-document flow.

## Data Model

`RepairRecord` keeps existing money fields in cents and gains:

- `customerName: String`
- `warrantyDays: Int`, default `90`

Room moves to version 2 and adds the same columns. Existing rows receive `customerName = ""` and `warrantyDays = 90`.

Common fault and repair-item options are stored locally in `SharedPreferences` as two string sets. They are UI preferences, not ledger records, so they do not need Room migration.

## UI Flow

The app shell owns a Room-backed repository and observes all repair records. Home, ledger, and stats screens render from that observed list.

The editor supports both add and edit:

- New records default to today and `90` warranty days.
- Existing records load their saved values.
- Save inserts or updates Room.
- Delete is available only while editing an existing record.
- The Android back button and top cancel action both leave the editor without saving.

Fault symptom and repair item fields use exposed dropdown menus. Users can type custom values and save them as common options. Each saved option can be deleted from the dropdown area.

## Warranty Rules

Remaining warranty days are calculated as:

`record.dateEpochDay + record.warrantyDays - todayEpochDay`

If the value is negative, the record is expired and is shown in red. Otherwise the UI shows the remaining days.

## XLSX Import and Export

The app uses Android file picker contracts:

- Import: `OpenDocument` with Excel MIME types.
- Export: `CreateDocument` with `.xlsx` MIME type.

To avoid a large Apache POI dependency, the app writes and reads the small subset of OpenXML needed for ledger rows:

- Export writes a valid workbook zip with workbook, worksheet, styles, relationships, and content types.
- Import reads the first worksheet, shared strings if present, inline strings, numeric cells, and plain date text cells.

The importer supports:

- Existing legacy A-H workbook mapping.
- App-exported workbook columns containing customer name and warranty days.

Invalid rows are skipped or flagged for confirmation rather than silently imported.

## Verification

- Unit tests cover record mapping, warranty calculations, common option persistence codec, backup/CSV updates, and XLSX round trip.
- Final verification runs `:app:testDebugUnitTest` and `:app:assembleDebug`.
