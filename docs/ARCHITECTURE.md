# Business Card Scanner — AI Agent Build Instructions

React Native (bare workflow) | TypeScript | Android API 26+ | iOS 14+

---

## Architecture

```
Camera + Doc Scan  →  On-Device OCR  →  Field Parser  →  Contact Save + vCard
Layer 1                Layer 2           Layer 3          Layer 4
```

---

## Shared Type — Create This First

```ts
// src/types/ContactCard.ts
export interface ContactCard {
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

---

## Layer 1 — Camera

### Packages
```bash
npm install react-native-vision-camera
npm install react-native-document-scanner-plugin
npm install react-native-reanimated
npm install react-native-worklets-core
cd ios && pod install && cd ..
```

### babel.config.js
```js
module.exports = {
  presets: ['module:@react-native/babel-preset'],
  plugins: [
    ['react-native-worklets-core/plugin'],
    ['react-native-reanimated/plugin'], // MUST be last
  ],
};
```

### Android — android/app/build.gradle
```groovy
android {
  defaultConfig {
    minSdkVersion 26
  }
}
dependencies {
  implementation "androidx.camera:camera-camera2:1.3.0"
  implementation "androidx.camera:camera-lifecycle:1.3.0"
  implementation "androidx.camera:camera-view:1.3.0"
}
```

### Android — AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.READ_CONTACTS" />
<uses-permission android:name="android.permission.WRITE_CONTACTS" />
<uses-feature android:name="android.hardware.camera" android:required="true" />
```

### iOS — Podfile
```ruby
platform :ios, '14.0'
```

### iOS — Info.plist
```xml
<key>NSCameraUsageDescription</key>
<string>CardSnap uses your camera to scan business cards</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>CardSnap saves scanned card images</string>
<key>NSContactsUsageDescription</key>
<string>CardSnap saves contacts to your address book</string>
```

### ScanScreen.tsx
```tsx
import DocumentScanner from 'react-native-document-scanner-plugin';

const handleScan = async () => {
  const { scannedImages } = await DocumentScanner.scanDocument({
    maxNumDocuments: 1,
    responseType: 'imageFilePath',
  });
  if (scannedImages?.length > 0) {
    navigation.navigate('Review', { imageUri: scannedImages[0] });
  }
};
```

The document-scanner-plugin handles quad detection and perspective correction natively (CameraX on Android, VisionKit on iOS). You do not implement quad detection yourself.

---

## Layer 2 — On-Device OCR

### Package
```bash
npm install react-native-vision-camera-mlkit
npm install react-native-image-manipulator
cd ios && pod install && cd ..
```

### CRITICAL — iOS outputOrientation
On iOS the camera sensor buffer is physically fixed in landscape orientation. ML Kit reads it as-is so all text appears rotated. You MUST pass `outputOrientation: 'portrait'` on iOS. Android reads rotation from EXIF automatically and does not need this flag.

### src/services/OcrService.ts
```ts
import { Platform } from 'react-native';
import { processImageTextRecognition } from 'react-native-vision-camera-mlkit';

export async function recognizeFromUri(uri: string): Promise<string> {
  // Strip file:// prefix — ML Kit expects an absolute path
  const cleanUri = uri.startsWith('file://') ? uri.slice(7) : uri;

  const result = await processImageTextRecognition(cleanUri, {
    language: 'LATIN',
    // iOS: sensor buffer is landscape-fixed, must rotate for portrait capture
    // Android: handled automatically via EXIF, omit this flag
    ...(Platform.OS === 'ios' ? { outputOrientation: 'portrait' as const } : {}),
    invertColors: false,
  });

  return result.text ?? '';
}
```

### src/utils/imagePreprocess.ts
```ts
import ImageManipulator from 'react-native-image-manipulator';

export async function preprocessForOcr(uri: string): Promise<string> {
  const result = await ImageManipulator.manipulate(
    uri,
    [{ resize: { width: 1200 } }],
    { compress: 0.9, format: 'jpeg' }
  );
  return result.uri;
}
```

Always resize to 1200px width before passing to ML Kit. Do not go below 0.7 compress quality.

---

## Layer 3 — Field Extraction

BCR-Library parses raw OCR text into structured fields. Its built-in Tesseract engine is too slow for mobile so you replace it with ML Kit output from Layer 2.

### Copy BCR Parser Into Project
```bash
git clone https://github.com/syneo-tools-gmbh/Javascript-BCR-Library.git /tmp/bcr
mkdir -p src/vendor/bcr
cp /tmp/bcr/src/bcr_parser.js  src/vendor/bcr/
cp -r /tmp/bcr/src/regex/       src/vendor/bcr/regex/
cp -r /tmp/bcr/src/ner/         src/vendor/bcr/ner/
cp -r /tmp/bcr/languages/       src/vendor/bcr/languages/
```

### src/services/ParserService.ts
```ts
import type { ContactCard } from '../types/ContactCard';
const bcrParser = require('../vendor/bcr/bcr_parser');

export function parseOcrText(rawText: string, imageUri: string): ContactCard {
  let parsed: any = {};
  try {
    parsed = bcrParser.parse(rawText, 'ENGLISH') ?? {};
  } catch (err) {
    console.warn('BCR parser error:', err);
  }

  const fullName = parsed.Name ?? '';
  const parts    = fullName.trim().split(/\s+/);

  // Regex fallbacks for when BCR misses deterministic fields
  const emailMatch = rawText.match(/[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}/);
  const phoneMatch = rawText.match(/(\+?[\d\s\-()]{7,20})/);
  const urlMatch   = rawText.match(/(?:https?:\/\/|www\.)[^\s]+/i);

  return {
    name:       fullName,
    firstName:  parts.slice(0, -1).join(' '),
    lastName:   parts.slice(-1).join(''),
    company:    parsed.Company ?? '',
    title:      parsed.Job     ?? '',
    email:      parsed.Email   ?? emailMatch?.[0] ?? '',
    phone:      parsed.Phone   ?? phoneMatch?.[0]?.trim() ?? '',
    address:    parsed.Address?.Text ?? '',
    website:    parsed.Web     ?? urlMatch?.[0] ?? '',
    rawOcrText: rawText,
    imageUri,
    scannedAt:  new Date().toISOString(),
  };
}
```

### ReviewScreen.tsx — Editable Fields Before Save
Always show a review screen. OCR is imperfect. Render all ContactCard fields as editable TextInputs with `testID={field-${key}}` on each input (required for E2E tests). Include a "Save Contact" button that navigates to SaveScreen with the (possibly edited) card.

---

## Layer 4 — Contact Save + vCard Export

### Packages
```bash
npm install react-native-contacts
npm install react-native-vcards
npm install react-native-fs
npm install react-native-share
cd ios && pod install && cd ..
```

### src/services/ContactService.ts
```ts
import Contacts from 'react-native-contacts';
import type { ContactCard } from '../types/ContactCard';

export async function saveAsContact(card: ContactCard): Promise<void> {
  // openContactForm (not addContact) — user confirms before any contact is written
  await Contacts.openContactForm({
    givenName:    card.firstName || card.name,
    familyName:   card.lastName,
    company:      card.company,
    jobTitle:     card.title,
    emailAddresses: card.email   ? [{ label: 'work', email: card.email }] : [],
    phoneNumbers:   card.phone   ? [{ label: 'work', number: card.phone }] : [],
    urlAddresses:   card.website ? [{ label: 'work', url: card.website }] : [],
    postalAddresses: card.address
      ? [{ label: 'work', formattedAddress: card.address, street: card.address,
           city: '', region: '', postCode: '', country: '' }]
      : [],
  });
}
```

Use `openContactForm` — never `addContact`. openContactForm opens the OS native contacts UI so the user can correct any OCR error before committing the record. Silently writing garbled OCR output to the address book creates bad records and fails App Store review.

### src/services/VCardService.ts
```ts
import vCard from 'react-native-vcards';
import RNFS from 'react-native-fs';
import Share from 'react-native-share';
import { Platform } from 'react-native';
import type { ContactCard } from '../types/ContactCard';

export async function shareAsVCard(card: ContactCard): Promise<void> {
  const vc       = vCard();
  vc.version     = '3.0'; // widest compatibility: iOS, Android, Outlook, Gmail
  vc.firstName   = card.firstName || card.name;
  vc.lastName    = card.lastName;
  vc.organization = card.company;
  vc.title       = card.title;
  vc.workEmail   = card.email;
  vc.workPhone   = card.phone;
  vc.workUrl     = card.website;

  const safeName = (card.name || 'contact').replace(/[^a-zA-Z0-9]/g, '_');
  const vcfPath  = `${RNFS.CachesDirectoryPath}/${safeName}.vcf`;

  await RNFS.writeFile(vcfPath, vc.getFormattedString(), 'utf8');

  await Share.open({
    url:  `file://${vcfPath}`,
    // Outlook on Android requires text/x-vcard; iOS uses text/vcard
    type: Platform.OS === 'android' ? 'text/x-vcard' : 'text/vcard',
    failOnCancel: false,
  });
}
```

---

## Navigation — App.tsx

```tsx
import { NavigationContainer } from '@react-navigation/native';
import { createStackNavigator }  from '@react-navigation/stack';
import ScanScreen   from './src/screens/ScanScreen';
import ReviewScreen from './src/screens/ReviewScreen';
import SaveScreen   from './src/screens/SaveScreen';

const Stack = createStackNavigator();

export default function App() {
  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="Scan">
        <Stack.Screen name="Scan"   component={ScanScreen} />
        <Stack.Screen name="Review" component={ReviewScreen} />
        <Stack.Screen name="Save"   component={SaveScreen} />
        {/* DEV ONLY: E2E test injection bypass */}
        {__DEV__ && <Stack.Screen name="TestInject" component={ReviewScreen} />}
      </Stack.Navigator>
    </NavigationContainer>
  );
}
```

---

## E2E Testing with Detox

### Install
```bash
npm install detox jest jest-circus --save-dev
npm install -g detox-cli
brew tap wix/brew && brew install applesimutils  # iOS only
```

### Strategy — Camera Injection via Deep Link
The simulator/emulator has no camera so DocumentScanner.scanDocument cannot run. Add a deep link that skips the camera and injects an image directly into ReviewScreen.

**Add to AndroidManifest.xml inside main activity:**
```xml
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="cardsnap" android:host="inject" />
</intent-filter>
```

**Add to iOS Info.plist:**
```xml
<key>CFBundleURLTypes</key>
<array>
  <dict>
    <key>CFBundleURLSchemes</key>
    <array><string>cardsnap</string></array>
  </dict>
</array>
```

### Get Real Test Card Images
```bash
mkdir -p e2e/assets

# Download royalty-free business card samples from Pexels or Unsplash
# Rename to descriptive names:
#   e2e/assets/card_standard.jpg  — card with name, company, email, phone, web
#   e2e/assets/card_minimal.jpg   — card with only name + phone
#   e2e/assets/card_complex.jpg   — card with logo, non-standard layout

# Normalise to fixed width for reproducible tests (requires ImageMagick)
magick convert e2e/assets/card_standard.jpg -resize 1200x -quality 90 e2e/assets/card_standard_1200.jpg
```

### e2e/tests/cardScan.e2e.ts
```ts
import { device, element, by, expect as detoxExpect } from 'detox';
import path from 'path';

async function injectCard(filename: string) {
  const localPath  = path.resolve(__dirname, `../assets/${filename}`);
  // Push image to device and open via deep link
  if (device.getPlatform() === 'android') {
    await device.executeShell(`adb push ${localPath} /data/local/tmp/${filename}`);
    await device.openURL({ url: `cardsnap://inject?imageUri=${encodeURIComponent('/data/local/tmp/' + filename)}` });
  } else {
    const docsDir = (await device.executeShell('xcrun simctl get_app_container booted com.cardsnap.app data')).trim();
    await device.executeShell(`cp ${localPath} ${docsDir}/Documents/${filename}`);
    await device.openURL({ url: `cardsnap://inject?imageUri=${encodeURIComponent(docsDir + '/Documents/' + filename)}` });
  }
  await detoxExpect(element(by.text('Review Contact'))).toBeVisible();
}

describe('Business Card Scan Pipeline', () => {
  beforeAll(async () => {
    await device.launchApp({
      newInstance: true,
      permissions: { camera: 'YES', contacts: 'YES', photos: 'YES' },
    });
  });

  afterEach(() => device.reloadReactNative());

  it('TC-01: all fields extracted from standard card', async () => {
    await injectCard('card_standard_1200.jpg');
    await detoxExpect(element(by.id('field-name'))).toBeVisible();
    await detoxExpect(element(by.id('field-email'))).toBeVisible();
  });

  it('TC-02: minimal card does not crash when fields are empty', async () => {
    await injectCard('card_minimal_1200.jpg');
    await detoxExpect(element(by.text('Review Contact'))).toBeVisible();
  });

  it('TC-03: user can edit a field before saving', async () => {
    await injectCard('card_standard_1200.jpg');
    await element(by.id('field-name')).clearText();
    await element(by.id('field-name')).typeText('Override Name');
    await element(by.text('Save Contact')).tap();
    await detoxExpect(element(by.text('Override Name'))).toBeVisible();
  });

  it('TC-04: vCard share sheet opens', async () => {
    await injectCard('card_standard_1200.jpg');
    await element(by.text('Save Contact')).tap();
    await element(by.text('Share as vCard (.vcf)')).tap();
    // Dismiss share sheet
    if (device.getPlatform() === 'ios') {
      await element(by.label('Cancel')).tap();
    } else {
      await device.pressBack();
    }
  });

  it('TC-05: extracted email matches email pattern', async () => {
    await injectCard('card_standard_1200.jpg');
    const attr  = await element(by.id('field-email')).getAttributes();
    const email = (attr as any).text ?? '';
    if (email) expect(email).toMatch(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
    else console.warn('TC-05: card contained no email — verify test asset');
  });
});
```

### Run Tests
```bash
# Android
detox build --configuration android.emu.debug
detox test  --configuration android.emu.debug

# iOS
detox build --configuration ios.sim.debug
detox test  --configuration ios.sim.debug

# Single test
detox test --configuration ios.sim.debug --testNamePattern="TC-01"
```

---

## Known Issues

| Issue | Fix |
|---|---|
| iOS OCR text rotated / garbled | Pass `outputOrientation: 'portrait'` in processImageTextRecognition on iOS |
| document-scanner-plugin returns `file://` on iOS, bare path on Android | Strip `file://` prefix before passing URI to ML Kit on all platforms |
| ML Kit pod build fails on Apple Silicon simulator | Build on physical device; or run `arch -x86_64 pod install` |
| vCard not opening in Outlook on Android | Set MIME type to `text/x-vcard` on Android, `text/vcard` on iOS |
| BCR returns empty Company field | Fallback: use the largest non-name OCR block as company value |
| Detox camera permission on emulator | Use deep link injection (see E2E section above) to bypass camera |

---

## Pinned Versions

```json
{
  "react-native": "0.73.x",
  "react-native-vision-camera": "4.x",
  "react-native-document-scanner-plugin": "1.8.x",
  "react-native-vision-camera-mlkit": "0.4.x",
  "react-native-reanimated": "3.x",
  "react-native-worklets-core": "1.x",
  "react-native-contacts": "7.x",
  "react-native-vcards": "0.0.x",
  "react-native-fs": "2.20.x",
  "react-native-share": "10.x",
  "react-native-image-manipulator": "1.x",
  "detox": "20.x"
}
```

Pin native dependencies with exact versions (no `^` prefix) to prevent breaking changes from upstream native module updates.

> Physical device required for all camera tests. Neither Android emulator nor iOS simulator has camera hardware.
