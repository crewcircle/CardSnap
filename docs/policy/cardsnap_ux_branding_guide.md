# CardSnap — Branding and UX Improvement Instructions

React Native | iOS + Android | Free App | Zero Manual Required

---

## Design Principle

A user installs this app, scans one card, and knows exactly how it works. No onboarding deck. No tutorial video. No help menu. Everything teaches itself through the interface. Every screen has one job and one primary action.

---

## 1. App Identity

### App Name
**CardSnap**
Short, describes the action, easy to remember. Avoid generic names like "Card Scanner" which disappear in search results.

### Tagline (shown on App Store listing and empty state)
*"Scan a card. Save a contact. Done."*

### Logo
A camera aperture shape with a rounded business card outline inside it. Single colour, works on both light and dark backgrounds. Commission this from a designer or use Figma with these specs:
- Icon canvas: 1024 × 1024px
- Foreground: white card outline inside a solid coloured aperture
- Brand colour: `#0066FF` (strong, trustworthy blue — used by finance and productivity apps)
- Corner radius on app icon: iOS handles this automatically; provide square asset

### Colour System
```
Primary blue:      #0066FF   ← buttons, active states, links
Primary dark:      #003DB3   ← pressed states
Surface white:     #FFFFFF   ← card backgrounds
Background grey:   #F5F7FA   ← screen backgrounds
Text primary:      #111827   ← headings, body
Text secondary:    #6B7280   ← labels, hints
Success green:     #10B981   ← saved confirmation
Warning amber:     #F59E0B   ← review required badges
Destructive red:   #EF4444   ← delete, error states
```

### Typography
```
Heading:    System font (SF Pro on iOS, Roboto on Android) Bold, 22–28pt
Body:       System font Regular, 16pt
Label:      System font Medium, 13pt, #6B7280
Button:     System font SemiBold, 16pt
Monospace:  not used
```

Use the system font on both platforms. Do not ship a custom font. System fonts render crisply, load instantly, and users already trust them.

---

## 2. First Launch Experience

### 2.1 Skip the Splash Screen
Remove any splash screen beyond the default OS launch image. Splash screens add 1–2 seconds of dead time on every open. The app opens directly to the camera.

### 2.2 Permission Request — Camera
Do not use the bare OS permission dialog with no context. Show a half-sheet explanation first:

```
┌─────────────────────────────────────┐
│                                     │
│          📷                         │
│                                     │
│   CardSnap needs your camera        │
│   to scan business cards            │
│                                     │
│   Your photos are never uploaded.   │
│   Everything happens on your phone. │
│                                     │
│   ┌─────────────────────────────┐   │
│   │      Allow Camera Access    │   │  ← primary blue button
│   └─────────────────────────────┘   │
│                                     │
│         Not now                     │  ← small grey text link
└─────────────────────────────────────┘
```

**Implementation:**
```tsx
// Show this sheet BEFORE calling requestCameraPermission()
// Only show once — store "camera_prompt_shown" in AsyncStorage
// The "Not now" path lands on a screen explaining the app is useless without camera
```

### 2.3 Permission Denied Recovery
If the user denies camera permission, do not show a generic error. Show a clear recovery path:

```
┌─────────────────────────────────────┐
│                                     │
│   Camera access is off              │
│                                     │
│   CardSnap can't scan cards         │
│   without camera access.            │
│                                     │
│   ┌─────────────────────────────┐   │
│   │     Open Settings           │   │  ← links directly to app settings
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

```tsx
import { Linking } from 'react-native';
// Opens the app's settings page directly on both platforms
Linking.openSettings();
```

---

## 3. Scan Screen

This is the most important screen. The user should be able to scan a card within 3 seconds of opening the app.

### 3.1 Layout

```
┌─────────────────────────────────────┐
│                              ⚙️     │  ← settings icon top-right, no other nav
│                                     │
│                                     │
│   ┌ - - - - - - - - - - - - - - ┐   │
│   |                             |   │  ← animated dashed rectangle
│   |     Hold card here          |   │     pulses once on first open only
│   |                             |   │     same aspect ratio as business card
│   └ - - - - - - - - - - - - - - ┘   │     (3.5 : 2 ratio)
│                                     │
│   Fit the card inside the frame     │  ← hint text, fades after first scan
│                                     │
│         ┌──────────────┐            │
│         │  Scan Card   │            │  ← large primary button, full opacity
│         └──────────────┘            │
│                                     │
│       or upload a photo  ↑          │  ← small secondary text link
└─────────────────────────────────────┘
```

### 3.2 The Card Guide Frame
```tsx
// Animated dashed rectangle — teaches framing without any text
// Dimensions: 85% screen width, height = width × (2/3.5)
// Border: 2px dashed #0066FF with 8px corner radius
// Animation: single gentle pulse (scale 1.0 → 1.02 → 1.0) on first open
// After first successful scan: frame turns solid green briefly then disappears
```

### 3.3 Scan Button Behaviour
```tsx
// State 1: Idle
//   Label: "Scan Card"
//   Background: #0066FF
//   Disabled: false

// State 2: Scanning (document scanner is active)
//   Label: "Scanning..."
//   Background: #003DB3
//   Show ActivityIndicator left of text
//   Disabled: true

// State 3: Processing (OCR running)
//   Label: "Reading card..."
//   Background: #003DB3
//   Show progress dots animation
//   Disabled: true
```

### 3.4 Torch / Flash Toggle
Add a torch button in the top-left corner of the camera view. Business cards are often scanned in restaurants and dim offices. No tooltip needed — use a universally recognised lightning bolt icon. Toggle state: outlined when off, filled yellow when on.

### 3.5 Upload from Gallery
The small "or upload a photo" link below the scan button covers users who already photographed a card in their camera roll. It calls the image picker instead of the document scanner. Same OCR pipeline runs on the selected image.

---

## 4. Processing State

After the user taps Scan Card and the document scanner returns an image, the app processes OCR. This takes 1–3 seconds. Do not show a blank screen.

```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│         [blurred card image]        │  ← show the captured image blurred
│                                     │
│   ┌─────────────────────────────┐   │
│   │  ●●●  Reading card...       │   │  ← overlay card, animated dots
│   │                             │   │
│   │  ████████████░░░░  68%      │   │  ← progress bar (fake progress is fine)
│   └─────────────────────────────┘   │
│                                     │
└─────────────────────────────────────┘
```

Show the actual captured card image blurred in the background during processing. This gives the user confidence the right image was captured and the app did not freeze.

**Fake progress implementation:**
```tsx
// Real OCR has no progress events. Animate a fake progress bar:
// 0% → 40% in 0.5s (fast start feels responsive)
// 40% → 80% in 1.5s (slows down — simulates "heavy work")
// 80% → 95% hold until OCR resolves
// 95% → 100% in 0.2s when OCR promise resolves
// Jump to 100% immediately if OCR finishes early
```

---

## 5. Review Screen

OCR is imperfect. The review screen is where the user corrects mistakes before saving. It must communicate clearly which fields need attention without overwhelming the user.

### 5.1 Layout

```
┌─────────────────────────────────────┐
│  ←   Review Details            Save │  ← Save button top-right, primary blue
│                                     │
│  ┌───────────────────────────────┐  │
│  │  [thumbnail of scanned card] │  │  ← tappable, opens full image
│  └───────────────────────────────┘  │
│                                     │
│  CONTACT DETAILS                    │  ← section label, uppercase small grey
│  ┌───────────────────────────────┐  │
│  │ 👤  Jane Smith               │  │
│  ├───────────────────────────────┤  │
│  │ 🏢  Acme Corporation         │  │
│  ├───────────────────────────────┤  │
│  │ 💼  Chief Technology Officer │  │
│  ├───────────────────────────────┤  │
│  │ 📧  jane@acme.com            │  │
│  ├───────────────────────────────┤  │
│  │ 📞  +61 2 9000 0000          │  │
│  ├───────────────────────────────┤  │
│  │ 🌐  acme.com                 │  │
│  ├───────────────────────────────┤  │
│  │ 📍  123 Main St, Sydney      │  │
│  └───────────────────────────────┘  │
│                                     │
│  Tap any field to edit              │  ← hint, disappears after first edit
└─────────────────────────────────────┘
```

### 5.2 Field Confidence Indicators
Fields where OCR is uncertain should be visually flagged — not with error messages, just a subtle amber left border:

```tsx
// After parsing, score each field:
// HIGH confidence: email (matches regex), phone (matches regex)
// MEDIUM confidence: name, company (BCR returned something)
// LOW confidence: any field that is empty or contains special characters

// Low confidence fields: amber left border, amber icon colour
// High confidence fields: no indicator — clean and uncluttered

const fieldStyle = (confidence: 'high' | 'medium' | 'low') => ({
  borderLeftWidth: confidence === 'low' ? 3 : 0,
  borderLeftColor: '#F59E0B',
});
```

### 5.3 Inline Editing
Each field row is a TextInput in display mode. The placeholder shows the field name. On tap, the input activates and the keyboard appears. No separate "edit mode" toggle. No modal.

```tsx
// The row looks like a read-only cell until tapped
// On focus: border colour changes from transparent to #0066FF
// On blur: saves the value to state automatically
// No save button per field — just tap elsewhere to confirm
```

### 5.4 Empty State Per Field
If OCR missed a field completely, show the field with a placeholder in grey:
```
│ 📧  Add email address             │  ← grey placeholder, tappable
```
Not blank, not an error. An invitation.

### 5.5 Scan Again Button
Add a small "Scan again" text link below the fields for when the OCR result is too poor to correct manually. This avoids back-navigation confusion.

---

## 6. Save Screen

Single screen with two primary actions. Keep it to under 5 elements total.

```
┌─────────────────────────────────────┐
│  ←   Save Contact                   │
│                                     │
│   Jane Smith                        │  ← name, large
│   Chief Technology Officer          │  ← title + company, grey
│   Acme Corporation                  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     Save to Contacts          │  │  ← primary blue, full width
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     Share as vCard (.vcf)     │  │  ← secondary, outlined
│  └───────────────────────────────┘  │
│                                     │
│  ┌───────────────────────────────┐  │
│  │     Send to CRM               │  │  ← tertiary, text only
│  └───────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

### 6.1 Save Confirmation
After "Save to Contacts" completes, show a full-screen success state for 1.5 seconds then automatically return to the scan screen ready for the next card:

```
┌─────────────────────────────────────┐
│                                     │
│                                     │
│              ✓                      │  ← large animated checkmark, green
│                                     │
│         Contact saved               │  ← single line, no clutter
│                                     │
│                                     │
└─────────────────────────────────────┘
```

```tsx
// Animated checkmark:
// Circle draws from 0° to 360° (stroke animation, 400ms)
// Checkmark draws inside (stroke animation, 300ms, 100ms delay)
// Whole thing scales from 0.8 to 1.0 (spring animation)
// Auto-navigate to ScanScreen after 1500ms

setTimeout(() => navigation.navigate('Scan'), 1500);
```

---

## 7. Settings Screen

Accessible from the gear icon on the scan screen. Short list. No deeply nested menus.

```
┌─────────────────────────────────────┐
│  ←   Settings                       │
│                                     │
│  INTEGRATIONS                       │
│  ┌───────────────────────────────┐  │
│  │  HubSpot              ✓ On   │  │
│  ├───────────────────────────────┤  │
│  │  Zoho CRM             + Add  │  │
│  ├───────────────────────────────┤  │
│  │  Airtable             + Add  │  │
│  └───────────────────────────────┘  │
│                                     │
│  PREFERENCES                        │
│  ┌───────────────────────────────┐  │
│  │  Default action       Contacts│  │  ← tap to cycle: Contacts / Ask me
│  ├───────────────────────────────┤  │
│  │  Haptic feedback      ● On   │  │
│  └───────────────────────────────┘  │
│                                     │
│  ABOUT                              │
│  ┌───────────────────────────────┐  │
│  │  Version 1.0.0               │  │
│  ├───────────────────────────────┤  │
│  │  Privacy policy              │  │
│  └───────────────────────────────┘  │
│                                     │
└─────────────────────────────────────┘
```

Keep the settings list to one scroll length maximum. If more integrations are added, paginate them inside the Integrations row rather than extending the settings screen.

---

## 8. In-App Guidance (No Manual Required)

### 8.1 Contextual Tooltips — First Use Only
Each screen shows one tooltip the very first time it is visited. It disappears after 4 seconds or on tap. Never shows again.

```tsx
// Track shown tooltips in AsyncStorage
const TOOLTIP_KEYS = {
  SCAN_FRAME:    'tooltip_scan_frame',
  REVIEW_EDIT:   'tooltip_review_edit',
  SAVE_VCARD:    'tooltip_save_vcard',
};

async function shouldShowTooltip(key: string): Promise<boolean> {
  const shown = await AsyncStorage.getItem(key);
  if (shown) return false;
  await AsyncStorage.setItem(key, 'true');
  return true;
}
```

**Scan screen tooltip:** Appears inside the card guide frame on first open.
```
"Align the card inside this frame, then tap Scan Card"
```

**Review screen tooltip:** Appears below the first field on first visit.
```
"Tap any field to correct it before saving"
```

**Save screen tooltip:** Appears below the vCard button on first visit.
```
"vCard works with Gmail, Outlook, WhatsApp and more"
```

### 8.2 Empty Contacts Warning
If the contacts permission is denied and the user taps "Save to Contacts":
```
The app shows an inline message (not an alert dialog):
"Contacts access is off. Open Settings to allow it."
With an "Open Settings" text link inline.
```
This is less disruptive than a modal alert and keeps the user in context.

### 8.3 No Network Needed Banner
Show a persistent small banner on the scan screen (below the viewfinder, above the button) if the device has no internet connection:

```
●  No internet — scanning still works
```

This prevents users from thinking the app is broken when offline. OCR and contact saving are both fully offline.

---

## 9. Haptic Feedback

Use haptic feedback at four points. It reinforces that something happened without requiring the user to watch the screen.

```tsx
import ReactNativeHapticFeedback from 'react-native-haptic-feedback';

// 1. Shutter — when document scanner captures the image
ReactNativeHapticFeedback.trigger('impactMedium');

// 2. OCR complete — when the review screen loads with results
ReactNativeHapticFeedback.trigger('notificationSuccess');

// 3. Contact saved — when save confirmation appears
ReactNativeHapticFeedback.trigger('notificationSuccess');

// 4. Error — when save or OCR fails
ReactNativeHapticFeedback.trigger('notificationError');
```

```bash
npm install react-native-haptic-feedback
cd ios && pod install && cd ..
```

---

## 10. Icon Set

Use a consistent icon set throughout. Do not mix icon families. Use `react-native-vector-icons` with the Feather set — clean, minimal, recognisable.

```bash
npm install react-native-vector-icons
cd ios && pod install && cd ..
```

| Location | Icon name | Meaning |
|---|---|---|
| Settings button | `settings` | Settings |
| Torch toggle | `zap` | Flash / torch |
| Upload from gallery | `image` | Photo library |
| Name field | `user` | Person |
| Company field | `briefcase` | Organisation |
| Title field | `award` | Job title |
| Email field | `mail` | Email |
| Phone field | `phone` | Phone number |
| Website field | `globe` | Web address |
| Address field | `map-pin` | Location |
| Save to Contacts | `user-plus` | Add contact |
| Share vCard | `share-2` | Share |
| Send to CRM | `send` | Push to app |
| Back navigation | `arrow-left` | Go back |
| Success state | `check-circle` | Done |
| Scan again | `refresh-cw` | Retry |

---

## 11. Micro-interactions

### 11.1 Card Guide Frame Feedback
When the document scanner detects a card edge, the dashed frame border animates to solid. This tells the user "the app sees the card" before they tap the button.

```tsx
// react-native-vision-camera-mlkit returns bounding boxes
// When any text block is detected in the guide rectangle:
// Animate border: dashed blue → solid green over 200ms
// Keep solid green until scan is tapped
```

### 11.2 Field Row Press Animation
Each field row in the Review screen scales down slightly on press (0.98 scale, 100ms) to confirm the tap registered before the keyboard opens.

```tsx
<Animated.View style={{ transform: [{ scale: pressScale }] }}>
  <TouchableOpacity onPressIn={() => Animated.spring(pressScale, { toValue: 0.98 }).start()}>
    ...
  </TouchableOpacity>
</Animated.View>
```

### 11.3 Save Button Loading State
While "Save to Contacts" is waiting for the OS contacts UI to open, animate three dots pulsing inside the button. This prevents double-taps.

---

## 12. App Store Presence

### Screenshots (required for both stores)
Produce these 5 screenshots in order. They must tell the story without any words being read:

1. **Scan screen** with a business card perfectly aligned in the frame guide
2. **Processing screen** with the blurred card visible and the "Reading card..." overlay
3. **Review screen** with all fields populated — use a realistic fake contact (not "John Doe")
4. **Save confirmation** with the green checkmark
5. **Integrations in Settings** showing HubSpot and Google Contacts connected

### App Store Description (first 3 lines are visible without expanding)
```
Scan any business card in seconds. CardSnap reads the text, fills in the contact
details, and saves directly to your phone's contacts — or shares to HubSpot, Zoho,
Airtable, Outlook, and more.

No account needed. No cloud upload. Everything happens on your phone.
Free forever.

HOW IT WORKS
1. Open CardSnap
2. Hold a business card in the frame
3. Tap Scan Card
4. Review and fix any details
5. Save to Contacts or share as vCard

WORKS WITH
• iPhone Contacts and iCloud
• Android Contacts and Google Contacts
• HubSpot CRM
• Zoho CRM
• Airtable
• Microsoft Outlook
• Any app that accepts vCard (.vcf) files — including WhatsApp and Gmail
```

---

## 13. Implementation Checklist for Agent

Complete these in order. Each item is independently verifiable.

- [ ] Apply colour constants from Section 1 to a shared `theme.ts` file imported everywhere
- [ ] Replace app icon with new CardSnap icon at all required resolutions
- [ ] Remove splash screen beyond OS default launch image
- [ ] Implement camera permission half-sheet (Section 2.2) with AsyncStorage "shown" flag
- [ ] Implement `Linking.openSettings()` recovery for denied camera permission
- [ ] Add torch toggle button to ScanScreen using Feather `zap` icon
- [ ] Add "upload from gallery" link below scan button
- [ ] Implement fake progress bar animation during OCR processing (Section 4)
- [ ] Show blurred captured image as background during processing
- [ ] Add card thumbnail to top of ReviewScreen
- [ ] Add amber left border to low-confidence OCR fields
- [ ] Add contextual tooltips (first-use only, stored in AsyncStorage) on three screens
- [ ] Add "No internet" banner to ScanScreen
- [ ] Add "Tap any field to edit" hint to ReviewScreen (first-use only)
- [ ] Implement full-screen success checkmark animation after contact save (Section 6.1)
- [ ] Auto-navigate to ScanScreen 1500ms after success
- [ ] Add haptic feedback at four points (Section 9)
- [ ] Replace all icons with Feather set from table in Section 10
- [ ] Apply press scale animation to ReviewScreen field rows
- [ ] Add field press scale micro-interaction (Section 11.2)
- [ ] Add loading dots to Save button while OS contacts UI is opening
- [ ] Produce 5 App Store screenshots per spec in Section 12
- [ ] Write App Store description from Section 12 copy
