# Manual QA Guide

This guide covers the remaining real-world flows that are not reliable in Detox or simulator-only testing.

## Recommended Environments

### Simulator and Emulator

- `iOS simulator`: `iPhone 16`
- `Android emulator`: `Pixel 8 / API 34` or the closest available Android 14 image

Use simulator or emulator for:

- app launch and tab navigation
- settings persistence
- contact list rendering
- editing and deleting seeded contacts
- reset behavior

### Real Devices

Use a real device for:

- camera permission flows
- card capture and OCR accuracy
- glare, skew, blur, shadow, and low-light card scans
- multilingual cards
- export and native share sheet behavior
- photo-library and file access prompts

## Debug-Build QA Tools

In debug builds, the Settings screen exposes a `QA Tools` section with a `Load Sample Contacts` button. Use this for simulator and emulator verification of contacts flows without depending on camera hardware.

## Manual Test Matrix

### 1. Launch and Navigation Smoke

Environment:
- `iPhone 16` simulator
- `Pixel 8 / API 34` emulator

Steps:
1. Launch the app from a clean install.
2. Confirm the Scan, Contacts, and Settings tabs are visible.
3. Open each tab once.

Expected results:
- the app opens without crashing
- Contacts shows either an empty state or the seeded contacts list
- Settings loads saved values without flashing invalid defaults

### 2. Settings Persistence

Environment:
- simulator or emulator

Steps:
1. Open Settings.
2. Enable `Spanish`.
3. Turn `Auto-save Contacts` off.
4. Switch `Data Usage` to `Cellular`.
5. Force-close and relaunch the app.
6. Return to Settings and then Scan.

Expected results:
- Settings values persist after relaunch
- the Scan screen summary reflects the saved OCR profile
- the Scan screen summary reflects the saved auto-save state

### 3. Seeded Contacts CRUD

Environment:
- simulator or emulator

Steps:
1. Open Settings.
2. Tap `Load Sample Contacts`.
3. Open Contacts.
4. Open `Jane Doe`.
5. Change the name to `Jane QA` and save.
6. Reopen the edited contact and delete it.

Expected results:
- both sample contacts appear after seeding
- edits persist after save
- deleting one contact leaves the remaining contact intact

### 4. Reset Behavior

Environment:
- simulator or emulator

Steps:
1. Seed sample contacts.
2. Change at least one setting.
3. Tap `Reset App`.
4. Confirm the destructive prompt.
5. Check Contacts and Scan again.

Expected results:
- contacts are removed
- settings return to defaults
- the Scan summary returns to `OCR profile: English`
- the Scan summary returns to `Auto-save: On`

### 5. First-Run Camera Permission

Environment:
- real iPhone
- real Android phone

Steps:
1. Install the app fresh.
2. Open the Scan tab.
3. Deny camera permission.
4. Retry permission from the in-app prompt.
5. Re-enable permission from system settings if needed.

Expected results:
- denial is handled gracefully
- the permission prompt explains why scanning is unavailable
- granting permission returns the user to a usable scan screen

### 6. Standard Business Card Scan

Environment:
- real device

Test cards:
- one clean English card
- one card with phone, email, website, and company

Steps:
1. Scan the card in good lighting.
2. Review the extracted text and parsed fields.
3. Save the contact.
4. Open Contacts and inspect the saved entry.

Expected results:
- OCR extracts the main text block
- parsed name, email, phone, company, and website are reasonable
- the saved contact is editable and remains after relaunch

### 7. Difficult Card Conditions

Environment:
- real device

Card conditions:
- glossy card with glare
- skewed card
- low-light card
- card with handwritten notes
- card with heavy branding/background graphics

Steps:
1. Scan each card once in the difficult condition.
2. If extraction is poor, rescan after adjusting angle or lighting.
3. Record which fields degrade first.

Expected results:
- the app does not crash or hang
- OCR still returns either usable text or an empty-but-safe result
- the user can retake the scan and recover

### 8. Multilingual Card Validation

Environment:
- real device

Card types:
- English + Spanish
- Japanese
- Chinese or Korean

Steps:
1. Enable the relevant OCR languages in Settings.
2. Scan each card.
3. Compare extracted text against the printed card.

Expected results:
- the Scan summary reflects the chosen language profile
- extracted text is directionally correct for the enabled language
- saving the contact preserves non-ASCII characters

### 9. Export Validation

Environment:
- real device preferred

Steps:
1. Save at least one contact.
2. Export one contact as VCard from the Scan results flow.
3. Export all contacts as CSV from Contacts.
4. Share each file to Files, Notes, Mail, or another destination.

Expected results:
- share sheet opens
- exported files are created successfully
- VCard and CSV content can be opened by a receiving app

## Suggested Test Cadence

- `Every PR`: Detox simulator suite plus manual smoke on iOS simulator
- `Before release`: full manual matrix on one real iPhone and one real Android device
- `After OCR or camera changes`: rerun sections 5 through 9
