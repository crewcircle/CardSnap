# Architectural Guardrails — CardScannerApp

> These guardrails protect the architectural integrity of CardScannerApp. All contributors and AI agents must follow them.

---

## 1. Tech Stack (Locked)

| Layer | Technology | Version Constraint | Notes |
|-------|-----------|-------------------|-------|
| **Framework** | React Native (bare workflow) | 0.73.x | No Expo managed workflow |
| **Language** | TypeScript | >= 5.0 | Strict mode enabled |
| **Camera** | react-native-vision-camera | 4.x | Primary camera interface |
| **Document Scanner** | react-native-document-scanner-plugin | 1.8.x | Quad detection + perspective correction |
| **OCR** | react-native-vision-camera-mlkit | 0.4.x | Google ML Kit on-device text recognition |
| **Image Preprocessing** | react-native-image-manipulator | 1.x | Resize to 1200px before OCR |
| **Contact Parsing** | BCR Library (vendored) | — | Copied into `src/vendor/bcr/` |
| **Contacts** | react-native-contacts | 7.x | Use `openContactForm`, never `addContact` |
| **vCard Export** | react-native-vcards | 0.0.x | vCard 3.0 format |
| **File System** | react-native-fs | 2.20.x | File I/O for vCard generation |
| **Sharing** | react-native-share | 10.x | Native share sheet |
| **Navigation** | @react-navigation/native + stack | — | 3-screen flow: Scan → Review → Save |
| **E2E Testing** | Detox | 20.x | Camera bypass via deep link injection |
| **Animation** | react-native-reanimated | 3.x | Required by vision-camera |
| **Worklets** | react-native-worklets-core | 1.x | Required by vision-camera |

### Dependency Rules
- **Pin exact versions** — no `^` or `~` prefixes on native modules
- **No Expo** — this is a bare React Native project. Do not introduce Expo packages
- **No cloud OCR** — all OCR processing must remain on-device (Google ML Kit)
- **No external analytics** — no tracking, crash reporting, or telemetry SDKs

---

## 2. Architecture Principles

### 2.1 Four-Layer Pipeline
```
Camera + Doc Scan  →  On-Device OCR  →  Field Parser  →  Contact Save + vCard
Layer 1                Layer 2           Layer 3          Layer 4
```

Each layer is **independent** and communicates only through the `ContactCard` type.

### 2.2 Single Source of Truth — ContactCard
```ts
interface ContactCard {
  name: string;
  firstName: string;
  lastName: string;
  company: string;
  title: string;
  email: string;
  phone: string;
  address: string;
  website: string;
  rawOcrText: string;
  imageUri: string;
  scannedAt: string;
}
```
- All layers produce/consume this type
- **Never** add fields without updating all consumers
- **Never** bypass this type for inter-layer communication

### 2.3 User-in-the-Loop for Contact Writes
- Always use `openContactForm` — never `addContact`
- User must confirm before any contact is written to the OS address book
- Silently writing garbled OCR output fails App Store review

### 2.4 Review Screen is Mandatory
- OCR is imperfect — always show editable fields before save
- Every input must have `testID={field-${key}}` for E2E testing
- No auto-save without user review

---

## 3. Platform-Specific Guardrails

### iOS
- **Camera orientation**: ML Kit reads sensor buffer as landscape-fixed. MUST pass `outputOrientation: 'portrait'` on iOS
- **File URIs**: Document scanner returns `file://` prefix — strip before passing to ML Kit
- **vCard MIME type**: Use `text/vcard`
- **Minimum iOS**: 14.0

### Android
- **Camera orientation**: EXIF handles rotation automatically — do NOT pass `outputOrientation`
- **File URIs**: Document scanner returns bare paths — no stripping needed
- **vCard MIME type**: Use `text/x-vcard` (required for Outlook compatibility)
- **Minimum SDK**: 26 (Android 8.0)

### Cross-Platform
- Always strip `file://` prefix before passing URI to ML Kit on **all** platforms (safe no-op on Android)
- Resize images to 1200px width before OCR on both platforms
- Never compress below 0.7 quality

---

## 4. Testing Guardrails

### Unit Tests
- Minimum 90% coverage threshold
- All utility functions must have tests
- ParserService must be tested with real card text samples

### E2E Tests (Detox)
- **Physical device required** — no camera on emulators/simulators
- Use **deep link injection** (`cardscanner://inject?imageUri=...`) to bypass camera in tests
- Test assets must be normalized to 1200px width
- Required test cards:
  - `card_standard_1200.jpg` — full fields
  - `card_minimal_1200.jpg` — minimal fields (crash safety)
  - `card_complex_1200.jpg` — non-standard layout

### CI Requirements
- Lint + TypeScript check on every PR
- Unit tests with coverage enforcement
- E2E tests for both iOS and Android

---

## 5. Security & Privacy

- **No data leaves the device** — all processing is local
- **No external APIs** — no cloud OCR, no analytics, no telemetry
- **Camera permission** — only active during scan session
- **Contacts permission** — only when user explicitly saves
- **No network calls** — the app should function fully offline

---

## 6. File Structure

```
src/
├── types/
│   └── ContactCard.ts          # Shared type — create first
├── services/
│   ├── OcrService.ts           # Layer 2: ML Kit OCR
│   ├── ParserService.ts        # Layer 3: BCR field extraction
│   ├── ContactService.ts       # Layer 4: Save to OS contacts
│   └── VCardService.ts         # Layer 4: vCard export + share
├── utils/
│   └── imagePreprocess.ts      # Image resize before OCR
├── vendor/
│   └── bcr/                    # Vendored BCR Library
├── screens/
│   ├── ScanScreen.tsx          # Layer 1: Camera + document scan
│   ├── ReviewScreen.tsx        # Editable fields before save
│   └── SaveScreen.tsx          # Contact save + vCard share
└── App.tsx                     # Navigation stack
```

---

## 7. Change Protocol

Before making architectural changes:

1. **Check this document** — does the change violate any guardrail?
2. **Update guardrails** — if intentionally changing architecture, update this file first
3. **Create an ADR** — for significant changes, create an Architecture Decision Record in `docs/adr/`
4. **Update tests** — ensure E2E tests still pass with the change
5. **Update docs** — keep `docs/ARCHITECTURE.md` in sync

---

## 8. Known Issues & Workarounds

| Issue | Fix | Status |
|-------|-----|--------|
| iOS OCR text rotated | Pass `outputOrientation: 'portrait'` | ✅ Documented |
| `file://` prefix on iOS | Strip before ML Kit on all platforms | ✅ Documented |
| ML Kit pod fails on Apple Silicon sim | Use physical device or `arch -x86_64 pod install` | ⚠️ Workaround |
| vCard fails in Outlook Android | Use `text/x-vcard` MIME type | ✅ Documented |
| BCR returns empty Company | Fallback: largest non-name block | ⚠️ Workaround |
| Detox camera on emulator | Deep link injection bypass | ✅ Documented |

---

*Last updated: 2026-04-05*
