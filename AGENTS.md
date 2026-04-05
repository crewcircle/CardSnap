# CardScannerApp - Agent Guidelines

This file provides instructions for AI agents operating in this repository. It covers project setup, development workflows, code style, and best practices.

## 🔴 CRITICAL: Sensible Analytics Workflow

- ❌ NEVER push to main/master
- ❌ NEVER commit directly
- ✅ ALWAYS create feature branch (`feat/`, `fix/`, `refactor/`, `docs/`)
- ✅ ALWAYS use PR workflow
- ✅ Target PR size: < 200 lines changed
- ✅ Include AI Disclosure in PRs

## 📋 Project Overview

CardScannerApp (CardSnap) is a **native Kotlin Android application** for scanning business cards using OCR technology.

### Tech Stack
- **Kotlin 1.9.22** with Coroutines + Flow
- **Jetpack Compose** (Material 3)
- **CameraX** for camera functionality
- **ML Kit Text Recognition** for OCR
- **Room Database** for persistent storage
- **DataStore Preferences** for app settings
- **Espresso + Compose Testing** for E2E testing
- **JUnit 5** for unit testing

### Build Configuration
- **AGP**: 8.6.0
- **Gradle**: 8.8
- **compileSdk**: 35, **minSdk**: 26, **targetSdk**: 34
- **KSP**: 1.9.22-1.0.17

## 🛠️ Development Commands

### Building
```bash
cd android
# Build debug APK
./gradlew assembleDebug

# Build test APK
./gradlew assembleDebugAndroidTest

# Run unit tests
./gradlew testDebugUnitTest

# Run E2E tests (requires running emulator)
./gradlew connectedAndroidTest
```

### Linting
```bash
cd android
./gradlew lint
```

## 📁 Project Structure

```
CardScannerApp/
├── android/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/java/com/cardscannerapp/
│   │   │   │   ├── data/          # Room DB, DAO, Repositories
│   │   │   │   ├── domain/        # Models, OCR, Parser
│   │   │   │   ├── ui/            # Screens, Navigation, Theme
│   │   │   │   ├── util/          # Utilities
│   │   │   │   └── MainActivity.kt
│   │   │   ├── androidTest/       # Espresso E2E tests
│   │   │   └── test/              # JUnit unit tests
│   │   └── build.gradle.kts
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle.properties
├── docs/
└── assets/
```

## 🎨 Code Style Guidelines

### Kotlin Conventions
- Use `val` by default, `var` only when necessary
- Prefer expression bodies for simple functions
- Use data classes for models
- Use sealed classes/interfaces for state
- Follow Android Kotlin style guide
- Use coroutines for async operations
- Use Flow for reactive streams

### Compose Conventions
- Keep composables small and focused
- Use `@Composable` functions for UI only
- Move business logic to ViewModels
- Use `remember` and `collectAsState()` appropriately
- Use `Modifier.testTag()` for E2E test identifiers

### Architecture
- **MVVM pattern**: ViewModel + StateFlow + Compose
- **Repository pattern**: Data access through repositories
- **Single Activity**: MainActivity hosts NavHost
- **Dependency Injection**: Manual DI (no Hilt/Dagger)

## 🧪 Testing Standards

### Unit Tests (JUnit)
- Location: `android/app/src/test/`
- Run: `./gradlew testDebugUnitTest`
- Test domain logic, parsers, utilities

### E2E Tests (Espresso + Compose Testing)
- Location: `android/app/src/androidTest/`
- Run: `./gradlew connectedAndroidTest`
- Requires running Android emulator
- Use `createAndroidComposeRule<MainActivity>()`
- Use `Modifier.testTag()` for component identification

## 🔐 Security
- NO API keys in code
- Use Android Keystore for sensitive data
- Run lint before commit
- Write tests for new features

## 📋 PR Requirements

Every PR MUST include:
- [ ] Summary (what + why)
- [ ] Changes (detailed list)
- [ ] Testing (how to verify)
- [ ] AI Disclosure (AI-Generated: Yes/No, Model, Platform)
- [ ] No debug code (Log.d, TODO, FIXME)
- [ ] No secrets
- [ ] Lint passes

---

*Last updated: 2026-04-05*
*Migration: React Native → Native Kotlin Android*
