# Deployment Guide

This document outlines the process for building, testing, and deploying the CardSnap to various platforms.

## Overview

The CardSnap uses GitHub Actions for continuous integration and deployment. This guide covers:

- Local development setup
- Building and testing locally
- CI/CD pipeline details
- Release processes

## Local Development Setup

### Prerequisites

1. Node.js (v18 or later)
2. JDK 17 (for Android builds)
3. Xcode (for iOS builds - requires macOS)
4. CocoaPods (for iOS dependencies)
5. Android Studio & Android SDK (for Android builds)

### Installation

```bash
# Install JavaScript dependencies
npm install

# Install iOS dependencies (macOS only)
cd ios && pod install && cd ..
```

## Local Building and Testing

### Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run specific test file
npx jest src/screens/ScannerScreen.test.tsx

# Run tests with coverage
npm test -- --coverage
```

### Linting

```bash
# Run ESLint on entire project
npm run lint

# Run ESLint on specific files/directories
npx eslint src/screens/ScannerScreen.tsx
npx eslint src/utils/
```

### Building Applications

#### Android Debug Build

```bash
npm run build:android
# Output: android/app/build/outputs/apk/debug/app-debug.apk
```

#### iOS Debug Build (macOS only)

```bash
npm run build:ios
# Output: ios/build/Debug-iphonesimulator/CardSnap.app
```

#### Running on Devices/Emulators

```bash
# Run on Android emulator/device
npm run android

# Run on iOS simulator (macOS only)
npm run ios
```

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration. The workflow is defined in `.github/workflows/ci.yml`.

### Workflow Triggers

- Push to `main` branch
- Pull requests targeting `main` branch

### Workflow Jobs

1. **Checkout**: Retrieves the repository code
2. **Setup**: Configures Node.js (v18) and Java (JDK 17)
3. **Dependencies**: Installs npm packages and iOS pods (on macOS)
4. **Linting**: Runs ESLint to check code quality
5. **Testing**: Executes Jest test suite
6. **Building**:
   - Android: Builds debug APK using Gradle
   - iOS: Builds debugger simulator app using xcodebuild (macOS only)
7. **Artifacts**: Uploads build artifacts for download

### Accessing Build Artifacts

After a workflow run completes:

1. Go to the "Actions" tab in your GitHub repository
2. Click on the specific workflow run
3. Scroll down to "Artifacts" section
4. Download the Android APK or iOS app artifacts

## Release Process

### Preparing for Release

1. Ensure all tests pass on the `main` branch
2. Update version numbers in:
   - `package.json` (for npm version)
   - `android/app/build.gradle` (versionName and versionCode)
   - `ios/CardSnap/Info.plist` (CFBundleShortVersionString and CFBundleVersion)
3. Create a git tag for the release:
   ```bash
   git tag -a v1.0.0 -m "Release version 1.0.0"
   git push origin v1.0.0
   ```

### Generating Production Builds

#### Android Release Build

```bash
cd android
./gradlew assembleRelease
# Output: android/app/build/outputs/apk/release/app-release.apk
```

#### iOS Release Build (macOS only)

```bash
xcodebuild -workspace ios/CardSnap.xcworkspace -scheme CardSnap \
  -configuration Release -sdk iphoneos -archivePath ios/build/CardSnap.xcarchive archive
xcodebuild -exportArchive -archivePath ios/build/CardSnap.xcarchive \
  -exportOptionsPlist ios/ExportOptions.plist -exportPath ios/build
```

## Troubleshooting

### Common Android Build Issues

1. **Java Version Conflicts**

   - Ensure JDK 17 is installed and set as JAVA_HOME
   - Clean build: `cd android && ./gradlew clean`

2. **AndroidX Issues**
   - Refer to `docs/BUILD_SUMMARY.md` for known issues and workarounds
   - Ensure proper resolution strategies in `android/build.gradle`

### Common iOS Build Issues

1. **CocoaPods Issues**

   - Delete Pods folder and Podfile.lock, then run `pod install` again
   - Update CocoaPods: `sudo gem install cocoapods`

2. **Code Signing Issues**
   - Ensure proper development team is set in Xcode project settings
   - For CI builds, use a simulator destination to avoid code signing requirements

## Environment Variables for CI

The following environment variables are used in the GitHub Actions workflow:

- `NODE_VERSION`: Node.js version to use (default: 18.x)
- `JAVA_VERSION`: Java version to use (default: 17)

These can be overridden in the workflow file if needed.

## Security Considerations

1. Never commit sensitive information (API keys, credentials) to the repository
2. Use GitHub Secrets for storing sensitive data needed in CI/CD workflows
3. Regularly update dependencies to address security vulnerabilities
4. Consider implementing Firebase App Distribution or TestFlight for beta distribution
