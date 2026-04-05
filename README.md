# CardSnap

[![CI](https://github.com/Sensible-Analytics/CardSnap/actions/workflows/ci.yml/badge.svg)](https://github.com/Sensible-Analytics/CardSnap/actions/workflows/ci.yml)
[![Android Build](https://github.com/Sensible-Analytics/CardSnap/actions/workflows/android-build.yml/badge.svg)](https://github.com/Sensible-Analytics/CardSnap/actions/workflows/android-build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green.svg)](https://developer.android.com/distribute/best-practices/develop/target-sdk)

> **Scan business cards instantly. Extract contacts automatically. Save to your phone in seconds.**

CardSnap is a professional business card scanner built with **native Kotlin** and **Jetpack Compose**. It uses Google's ML Kit for OCR to extract contact details from business card photos, then intelligently parses and saves them to your device contacts.

---

## 📱 Screenshots

| Scan Screen | Review Contact | Contacts List | Settings |
|:---:|:---:|:---:|:---:|
| ![Scan Screen](docs/images/screenshots/scan-screen.png) | ![Review](docs/images/screenshots/review-contact.png) | ![Contacts](docs/images/screenshots/contacts-list.png) | ![Settings](docs/images/screenshots/settings.png) |

---

## ✨ Features

- **📸 Instant Scanning** — Point your camera at any business card and capture with a single tap
- **🤖 Smart OCR** — Google ML Kit Text Recognition extracts all text with high accuracy
- **🧠 Intelligent Parsing** — Automatically identifies names, emails, phones, companies, and websites
- **💾 Auto-Save** — Optionally save contacts automatically after each scan
- **📤 Export & Share** — Export contacts as vCard or CSV, share via any app
- **🌐 Multi-Language OCR** — Support for English, Chinese, German, French, Spanish, and more
- **📱 Native Android** — Built with Jetpack Compose, CameraX, and Material 3 design
- **🔒 Privacy-First** — All processing happens on-device. No data sent to servers

---

## 🏗️ Architecture

CardSnap follows the **MVVM architecture** with clean separation of concerns:

```
┌─────────────────────────────────────────────────┐
│                  UI Layer                        │
│  ScanScreen → ContactsScreen → EditContactScreen │
│  SettingsScreen (Jetpack Compose)                │
├─────────────────────────────────────────────────┤
│                ViewModel Layer                   │
│  ScanViewModel, ContactsViewModel, etc.          │
│  (StateFlow + Coroutines)                        │
├─────────────────────────────────────────────────┤
│                Domain Layer                      │
│  ContactParser, OcrEngine, ImageCropper          │
├─────────────────────────────────────────────────┤
│                 Data Layer                       │
│  Room Database ←→ ContactRepository             │
│  DataStore Preferences ←→ SettingsRepository     │
└─────────────────────────────────────────────────┘
```

### Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin 1.9.22 |
| **UI** | Jetpack Compose + Material 3 |
| **Camera** | CameraX |
| **OCR** | Google ML Kit Text Recognition |
| **Database** | Room |
| **Settings** | DataStore Preferences |
| **Async** | Kotlin Coroutines + Flow |
| **Image Loading** | Coil |
| **Architecture** | MVVM + Repository Pattern |

---

## 📥 Download

| Version | Type | Link |
|---------|------|------|
| Latest Debug APK | Debug | [Download from Releases](https://github.com/Sensible-Analytics/CardSnap/releases) |
| Latest Release APK | Release | [Download from Releases](https://github.com/Sensible-Analytics/CardSnap/releases) |

---

## 🚀 How It Works

### 1. Point & Capture
Open the app and point your camera at a business card. The card guide frame helps you align the card properly.

### 2. Automatic OCR
When you tap capture, the image is cropped, rotated (if needed), and processed by ML Kit's Text Recognition engine.

### 3. Smart Parsing
The extracted text is analyzed by our contact parser, which uses regex patterns to identify:
- **Names** — First line without digits or @ symbols
- **Emails** — Standard email format detection
- **Phone numbers** — US/international format recognition
- **Companies** — Detection of business suffixes (Inc, LLC, Corp, etc.)
- **Job titles** — Keyword-based title identification
- **Websites** — URL pattern detection

### 4. Review & Save
Review the extracted contact details, edit any fields if needed, then save to your device database or export as vCard.

---

## 🎯 Use Cases

| Use Case | Description |
|----------|-------------|
| **Networking Events** | Quickly scan dozens of business cards at conferences and meetups |
| **Sales Teams** | Capture prospect contact info during field visits |
| **Recruiters** | Save candidate contact details from career fairs |
| **Real Estate** | Collect agent and client contact information efficiently |
| **Personal Use** | Digitize your stack of physical business cards at home |

---

## 🛠️ Build from Source

### Prerequisites
- **Java 17** (Temurin recommended)
- **Android SDK** (compileSdk 35, minSdk 26)
- **Gradle 8.8**

### Commands

```bash
# Clone the repository
git clone https://github.com/Sensible-Analytics/CardSnap.git
cd CardSnap

# Build debug APK
cd android
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run E2E tests (requires emulator)
./gradlew connectedAndroidTest
```

### Output
- Debug APK: `android/app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `android/app/build/outputs/apk/release/app-release-unsigned.apk`

---

## 📋 Requirements

| Requirement | Value |
|------------|-------|
| **Minimum Android** | 8.0 (API 26) |
| **Target Android** | 14.0 (API 34) |
| **Compile SDK** | 35 |
| **Camera** | Required |
| **Permissions** | Camera, Contacts, Internet, Vibrate |

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/Sensible-Analytics/CardSnap/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Sensible-Analytics/CardSnap/discussions)

---

<p align="center">
  Made with ❤️ by <a href="https://github.com/Sensible-Analytics">Sensible Analytics</a>
</p>
