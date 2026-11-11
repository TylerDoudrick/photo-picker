# Google TV Photo Picker for Google Photos

A streamlined, remote-friendly Google TV photo picker that connects to Google Photos via QR code authentication, presents a simplified album list with photo counts, and serves as an Android system photo picker (`ACTION_GET_CONTENT` / `ACTION_PICK`) for wallpaper and launcher apps.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed based on your preferences:
> - **Primary Role**: Operates strictly as an Android system photo picker invoked by external wallpaper applications (`ACTION_GET_CONTENT` and `ACTION_PICK`), while maintaining a Google TV home screen launcher entry for account connection and testing.
> - **Authentication Flow**: TV screen displays a clear QR code and verification code so you can authenticate your Google Account effortlessly from a phone or PC browser (Google OAuth 2.0 Device Flow).
> - **Album & Picker Simplicity**: Minimalist Google TV Leanback layout focusing exclusively on album selection with a prominent photo count label beside each album name, leading directly to a grid of photos to pick from.

---

## 1. Overview & Core Concept

- **What It Does**: Enables Google TV users to pick photos from their Google Photos library when any wallpaper app, screensaver app, or system setting requests an image.
- **Target Audience & Context**: Android TV / Google TV users sitting 10 feet away using a hardware D-pad remote who want their personal Google Photos albums as custom TV wallpapers.
- **Key Value**: Bridges the gap on Google TV where external wallpaper apps need a system photo picker to access cloud Google Photos albums without clumsy on-screen typing.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **System Picker Invocation (`ACTION_GET_CONTENT` / `ACTION_PICK`)**:
   - External wallpaper app on Google TV requests an image.
   - Android system picker dialog prompts the user; user selects "Google Photos Picker".
   - Our app opens in full-screen TV picker mode.
2. **First-Launch / QR Code Sign-In**:
   - If not yet connected, the TV screen presents a clean connection card with:
     - High-contrast QR code pointing to Google's device activation portal (`google.com/device`).
     - A large, readable 8-character user code.
     - Live polling status ("Waiting for approval on your mobile device...").
     - Instant fallback/demo mode button for immediate offline testing.
3. **Album Selection (Ultra-Simple)**:
   - High-contrast, horizontal/vertical TV card grid.
   - Each card displays the album cover thumbnail, the album name in bold, and the photo count label directly beside or below the title (e.g. `Vacation 2026 • 142 photos`).
   - Clean D-pad focus scaling (1.08x scale with glowing border indicator).
4. **Photo Grid & Selection**:
   - Selecting an album transitions smoothly to the photo thumbnail grid.
   - Navigating with the D-pad highlights each photo with a smooth focus frame.
   - Pressing `DPAD_CENTER` or Enter selects the photo.
   - The app downloads/caches the high-res image, generates a secure `content://` URI via `FileProvider`, grants read permissions, sets `RESULT_OK`, and immediately hands control back to the calling wallpaper app.

### Visual Identity & Theme (Google TV Dark Aesthetic)

- **Aesthetic Direction**: Leanback 10-foot UI; deep charcoal/slate backgrounds (`#0F1115` to `#161922`), vibrant Google blue/cyan accent (`#4285F4` / `#8AB4F8`) for active focus states.
- **Focus Indication**: High-visibility focus indicators designed specifically for TV remotes, including elevation, animated outline strokes (3.dp), and slight scale bounce (`spring(dampingRatio = 0.75f)`).
- **Typography & Hierarchy**: Bold display headings for TV distance readability, clear monospaced code blocks for the device authorization code, and subdued metadata (`16.sp` photo count labels).

---

## 3. Key Product Decisions & Trade-Offs

- **Standard Android Picker Intent Filter**:
  - *Chosen Approach*: Declare `<intent-filter>` for `android.intent.action.GET_CONTENT` and `android.intent.action.PICK` with `image/*` MIME type and `CATEGORY_DEFAULT`, `CATEGORY_OPENABLE`.
  - *Why*: This allows any third-party Google TV wallpaper app (e.g., Aerial Views, Wallpaper Changer, Muzei, or native settings) to trigger this app natively.
  - *Fallback*: Also includes `android.intent.category.LEANBACK_LAUNCHER` so the app is accessible on the TV home screen to verify login status and manage albums.
- **Google OAuth 2.0 Device Flow (RFC 8628) + QR Code**:
  - *Chosen Approach*: Implement RFC 8628 Google Device Authorization Grant. The TV fetches a device code, presents a QR code via dynamic Canvas rendering, and polls for token exchange.
  - *Why*: Eliminates the frustration of typing passwords or email addresses with a TV remote.
  - *Resilience*: Includes built-in simulated/sample cloud albums to allow instant validation even before configuring OAuth credentials in the Google Cloud Console.
- **FileProvider URI Granting**:
  - *Chosen Approach*: Download selected cloud photo to app cache and return a content URI via `androidx.core.content.FileProvider` with `Intent.FLAG_GRANT_READ_URI_PERMISSION`.
  - *Why*: Guarantees that external wallpaper apps receive a durable, readable content URI compliant with Android 11+ scoped storage requirements.

---

## 4. Technical Architecture & Data Strategy

```
┌──────────────────────────────────────────────────────────────┐
│                      Calling App                             │
│     (External Wallpaper App, Ambient Screensaver, Launcher)  │
└──────────────────────────────┬───────────────────────────────┘
                               │ ACTION_GET_CONTENT / ACTION_PICK
                               ▼
┌──────────────────────────────────────────────────────────────┐
│              Google TV Photo Picker Activity                 │
│                                                              │
│  ┌────────────────────────┐      ┌────────────────────────┐  │
│  │   Auth / QR Screen     │      │      Album List        │  │
│  │ (google.com/device QR) │ ───► │ (Name + "• 142 photos")│  │
│  └────────────────────────┘      └───────────┬────────────┘  │
│                                              │               │
│                                              ▼               │
│                                  ┌────────────────────────┐  │
│                                  │   Photo Grid (D-Pad)   │  │
│                                  │   Select & Return      │  │
│                                  └───────────┬────────────┘  │
└──────────────────────────────────────────────┼───────────────┘
                                               │
                                               ▼
                              ┌────────────────────────────────┐
                              │     Photo Cache & Provider     │
                              │  FileProvider -> content://... │
                              │   RESULT_OK with URI grants    │
                              └────────────────────────────────┘
```

### Component Structure

1. **Manifest & Intent Handling**:
   - `AndroidManifest.xml` updated with `uses-feature android.software.leanback` (optional = false/true), `touchscreen` false, TV banner, `INTERNET` permission.
   - `MainActivity` configured with `ACTION_GET_CONTENT`, `ACTION_PICK`, and `LEANBACK_LAUNCHER`.
2. **Data & Auth Layer**:
   - `GooglePhotosAuthRepository`: Manages Device Flow code generation, QR generation, token polling, and token caching in `EncryptedSharedPreferences`/`DataStore`.
   - `GooglePhotosRepository`: Fetches album list (with `mediaItemsCount`), retrieves album media items, and downloads full-resolution image on selection.
3. **UI Composables (TV Leanback Optimized)**:
   - `QrAuthScreen`: TV-sized QR code rendering, user code display, connection instructions.
   - `AlbumListScreen`: Simple grid/row of albums with thumbnail, bold title, and photo count label right beside the name.
   - `PhotoGridScreen`: High-resolution photo gallery with remote focus rings and single-click selection.
4. **FileProvider Configuration**:
   - `res/xml/file_paths.xml` configuring `cache-path` for secure temporary photo sharing.
