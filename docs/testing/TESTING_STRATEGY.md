# CardSnap Testing Strategy

## Current Test Structure

### Unit Tests (src/test - JVM)
**Location:** `android/app/src/test/java/com/cardsnap/`

| Test File | Coverage |
|-----------|----------|
| ContactParserTest.kt | Contact parsing logic |
| VCardGeneratorTest.kt | vCard export logic |
| AppSettingsTest.kt | Settings deserialization |
| ContactCardTest.kt | Contact data model |

**4 unit tests** - testing pure business logic without Android dependencies.

### Instrumented Tests (src/androidTest - Device/Emulator)
**Location:** `android/app/src/androidTest/java/com/cardsnap/`

| Category | Test Files |
|----------|-----------|
| **Core Features** | CardScanTest, ContactsTest, EditContactTest |
| **User Features** | AutoSaveTest, ExportTest, SettingsTest |
| **UX** | UxFeaturesTest, NavigationTest, AccessibilityTest |
| **Scenarios** | CardScenariosTest, RealWorldScenariosTest |
| **Error Handling** | ErrorHandlingTest, PermissionsTest, BusinessCardValidationTest |

**15 test files** + 3 helper classes.

---

## Android Testing Best Practices (from research)

### Test Pyramid (Industry Standard)
- **70% Unit Tests** - Fast, run on JVM
- **20% Integration/Instrumented Tests** - Component testing
- **10% E2E Tests** - Critical user journeys

### Recommended Coverage Distribution
| Type | Coverage | Run Time |
|------|----------|----------|
| Unit | ~70% | < 5 min |
| Instrumented | ~20% | 5-15 min |
| E2E | ~10% | 15+ min |

---

## Current Assessment

### What's Good
- Clear separation: src/test (unit) vs src/androidTest (instrumented)
- Good test file organization in tests/ subdirectory
- Comprehensive E2E test plan exists

### Gaps
| Gap | Current | Recommended |
|-----|----------|-------------|
| Unit test count | 4 files | 15+ files |
| Domain layer tests | 1 | 5+ (all parsers, OCR, use cases) |
| ViewModel tests | 0 | 4+ (all ViewModels) |
| Repository tests | 0 | 2+ |

---

## Action Items

1. ✅ Document current test structure
2. ✅ Research best practices
3. ⬜ Add ViewModel unit tests
4. ⬜ Add Repository unit tests  
5. ⬜ Expand Domain layer unit tests
6. ⬜ Review and update E2E test plan

---

*Last updated: 2026-04-08*