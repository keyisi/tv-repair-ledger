# TV Repair Ledger Android App Design

## Purpose

Build a privacy-first Android app for recording self-employed TV repair jobs. The app replaces the current Excel workflow with a faster phone-first workflow: add a repair job at the customer's site, review recent jobs, search old repairs, and see monthly income, cost, and profit.

The first version is not a general household accounting app. It is a local-only repair ledger shaped around the existing TV repair Excel workbook in this repository.

## Scope

### In Scope for Version 1

- Local-only Android app with no login, no cloud sync, and no network dependency.
- Repair job records with:
  - Date
  - Customer address
  - Phone number
  - Fault symptom
  - Repair item or part
  - Charged amount
  - Parts cost
  - Auto-calculated profit
  - Notes
  - Warranty period
- Home dashboard showing current-month revenue, parts cost, profit, and job count.
- Recent repair list sorted by date descending.
- Add, edit, and delete repair records.
- Search across address, phone, fault symptom, repair item, and notes.
- Statistics by month, repair item, and fault symptom.
- One-time import of the existing Excel data.
- Export current data to CSV or Excel-compatible file.
- Local backup and restore file.

### Out of Scope for Version 1

- User accounts.
- Cloud sync.
- Multi-device collaboration.
- Inventory management.
- Customer relationship management beyond fields stored on repair records.
- Photo attachments.
- Payment collection, invoices, or receipts.
- Push reminders for warranty follow-up.

These can be considered after the core repair ledger is useful and stable.

## Recommended Approach

Use native Android with Kotlin, Jetpack Compose, and Room.

Reasons:

- Room is reliable for local structured data.
- Compose is fast for building form-heavy mobile screens.
- Kotlin keeps the app maintainable if later features are added.
- The app can work fully offline and keep private data on the phone.

## Product Structure

### Home Screen

The home screen is the first screen after launch. It shows:

- This month's revenue.
- This month's parts cost.
- This month's profit.
- This month's job count.
- Recent repair records.
- A prominent add button.

The home screen should be dense and practical. It should favor quick scanning over decoration.

### Repair List

The repair list shows all repair jobs in reverse chronological order. Each row shows:

- Date
- Address
- Fault symptom
- Repair item
- Charged amount
- Profit

Tapping a row opens the repair detail or edit screen.

### Add/Edit Repair

The add/edit form contains:

- Date, defaulting to today.
- Address.
- Phone number as text.
- Fault symptom.
- Repair item or part.
- Charged amount.
- Parts cost.
- Notes.
- Warranty period.

Profit is not entered manually. It is calculated as:

```text
profit = chargedAmount - partsCost
```

If parts cost is blank, calculations treat it as 0, but the UI should still distinguish blank cost from an explicitly entered 0.

### Search

Search must match:

- Address.
- Phone number.
- Fault symptom.
- Repair item or part.
- Notes.

Search results use the same row layout as the repair list.

### Statistics

Version 1 statistics should answer practical questions:

- How much did I earn this month?
- Which months were best?
- Which repair items generate the most profit?
- Which fault symptoms are most common?

Required statistics:

- Monthly revenue, cost, profit, and job count.
- Repair item summary: count, revenue, cost, profit.
- Fault symptom summary: count, revenue, cost, profit.

## Data Model

### RepairRecord

Fields:

- `id`: local generated identifier.
- `date`: repair date.
- `address`: customer address, optional but encouraged.
- `phone`: text, optional.
- `faultSymptom`: text, optional.
- `repairItem`: text, optional.
- `chargedAmount`: decimal money value, required.
- `partsCost`: nullable decimal money value.
- `notes`: text, optional.
- `warrantyPeriod`: text, optional.
- `createdAt`: timestamp.
- `updatedAt`: timestamp.

Derived value:

- `profit = chargedAmount - (partsCost ?: 0)`

Money should be stored in cents or another integer minor-unit format to avoid floating point rounding errors.

## Excel Import Rules

The existing workbook has one useful TV repair record sheet and two empty sheets. The import should use only the main record sheet.

Column mapping from the current workbook:

- Column A: date -> `date`
- Column B: customer address -> `address`
- Column C: part or repair item -> `repairItem`
- Column D: fault symptom -> `faultSymptom`
- Column E: charged amount -> `chargedAmount`
- Column F: parts cost -> `partsCost`
- Column G: phone number -> `phone`
- Column H: notes -> `notes`

Import behavior:

- Ignore the summary formula cells on the right side of the sheet.
- Preserve phone numbers as text.
- Treat blank parts cost as null, not as a typed 0.
- Calculate profit inside the app instead of importing the Excel formula result.
- The two records with date values like `12.27` and `1.2` must be flagged for user confirmation because the year is missing.
- Normalize obvious repair item variants during import only when safe, such as `emmc` and `EMMC` becoming `EMMC`.

The import should produce a simple review result:

- Number of records imported.
- Number of records skipped.
- Number of records needing date confirmation.

## Data Safety

The app must not require network access. User data remains on the device.

Version 1 needs:

- Manual export.
- Manual backup file.
- Manual restore from backup file.

Backup and restore should use app-controlled structured data, not screenshots or rendered spreadsheets.

## Error Handling

- Required money fields should validate before saving.
- Invalid money values should show a clear inline error.
- Delete should require confirmation.
- Import should report rows that cannot be read.
- Restore should not overwrite current data until the backup file is validated.
- If a backup restore fails, existing app data must remain unchanged.

## Testing Strategy

Test coverage should focus on data correctness:

- Profit calculation with blank, zero, and non-zero parts cost.
- Monthly statistics aggregation.
- Repair item and fault summary aggregation.
- Search matching address, phone, fault, repair item, and notes.
- Excel import mapping from the current workbook fields.
- Handling of missing-year dates in imported data.
- Backup restore validation.

Manual verification should cover:

- Adding a repair record on a phone-sized screen.
- Editing and deleting a repair record.
- Searching for an old record by address or repair item.
- Importing the existing Excel workbook.
- Exporting data and confirming it can be opened outside the app.

## Future Enhancements

After version 1 is stable, possible additions are:

- Warranty reminder.
- Photo attachments for repair records.
- Customer history grouped by phone or address.
- Inventory and parts usage.
- Better Excel export with formatted summary sheets.
- Optional encrypted backup.
- Optional local network or private cloud sync.

## Approval State

Approved direction: local-only Android TV repair ledger based on the existing Excel repair records.

Implementation should not start until this design is reviewed and accepted as the working spec.
