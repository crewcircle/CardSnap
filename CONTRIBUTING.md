# Contributing to CardSnap

Thank you for considering contributing to CardSnap! Please read this guide to understand our development process and how you can contribute effectively.

## How to Contribute

### Reporting Bugs

- Use the GitHub Issues tracker
- Include steps to reproduce, expected behavior, and actual behavior
- Add screenshots if applicable
- Label as "bug"

### Suggesting Features

- Use the GitHub Issues tracker
- Label as "enhancement"
- Describe the feature and its benefits
- Consider if it aligns with the project roadmap

### Submitting Changes

1. Fork the repository
2. Create a new branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests to ensure nothing is broken
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

## Development Setup

### Prerequisites

- Node.js v18 or later
- npm or yarn
- Xcode (for iOS)
- Android Studio (for Android)
- Git

### Installation

```bash
git clone https://github.com/Sensible-Analytics/CardSnap.git
cd CardSnap
npm install
cd ios && pod install && cd ..
```

## Release Process

### Versioning

We use Semantic Versioning (MAJOR.MINOR.PATCH):
- **MAJOR**: Incompatible API changes or major redesigns
- **MINOR**: New features in backward-compatible manner
- **PATCH**: Bug fixes and minor improvements

### Release Workflow

1. **Update Version Numbers**
   - Update `package.json` version
   - Update `app.json` version
   - Update `ios/` and `android/` version if needed

2. **Prepare Release Notes**
   - Document changes in release notes
   - Update CHANGELOG.md if maintained

3. **Generate Release Artifacts**
   ```bash
   # Build iOS release (requires Xcode)
   npm run release:ios
   
   # Build Android release
   npm run release:android
   
   # Verify artifacts
   npm run release:verify
   ```

4. **Create Git Tag and Release**
   ```bash
   git tag -a v1.0.0 -m "Version 1.0.0"
   git push origin v1.0.0
   ```

5. **Submit to App Stores**
   - Upload to App Store Connect (iOS)
   - Upload to Google Play Console (Android)
   - Complete store listings
   - Submit for review

### Store Listing Preparation

See `app-store-listing.md` for detailed store listing requirements and guidelines.

Required assets:
- Screenshots for all required device sizes
- App icons in various sizes
- Privacy policy URL
- Store descriptions and keywords
- Feature graphics

Assets should be placed in the `store-assets/` directory structure.

### CI/CD Release Pipeline

Our CI/CD pipeline includes:
- **CI Workflow**: Runs tests on every push and pull request
- **Android Build**: Builds and tests Android app
- **iOS Build**: Builds and tests iOS app
- **Release Workflow**: Creates GitHub releases with artifacts when tags are pushed

To trigger a release:
1. Ensure all tests pass on main branch
2. Create and push a version tag: `git tag -a v1.0.0 -m "Version 1.0.0"`
3. Push tag: `git push origin v1.0.0`
4. GitHub Actions will automatically build and create a release

### Running Tests

```bash
# Unit tests
npm test

# E2E tests (iOS)
npm run detox:test -- --configuration ios.sim

# E2E tests (Android)
npm run detox:test -- --configuration android.emu

# Linting
npm run lint

# TypeScript
npm run tsc --noEmit
```

## Coding Standards

### TypeScript

- Use strict mode (enabled in tsconfig.json)
- Prefer interfaces over types for object shapes
- Use functional components with hooks
- Export interfaces/types when they're public

### React Native

- Use StyleSheet.create() for styles
- Always provide accessibilityLabel for interactive elements
- Use Platform.OS for platform-specific code when needed
- Use FlatList for long lists of data

### Testing

- Write unit tests for utility functions
- Write E2E tests for user flows
- Follow AAA pattern: Arrange, Act, Assert
- Mock external dependencies appropriately

### Git

- Write clear, descriptive commit messages
- Reference issue numbers when applicable (e.g., "Fixes #123")
- Keep commits focused on single changes
- Use conventional commit format when possible

## Code Review Process

1. All PRs require at least one approval
2. CI must pass (tests, lint, build)
3. No breaking changes without discussion
4. Documentation updated when needed
5. Squash and merge preferred for feature branches

## Community

Please note that this project is released with a Contributor Code of Conduct. By participating in this project you agree to abide by its terms.

## Getting Help

If you need help:

- Check existing issues for similar problems
- Ask in the GitHub Discussions
- Refer to the documentation in /docs
- As a last resort, open a new issue

Thank you for contributing to CardSnap!
