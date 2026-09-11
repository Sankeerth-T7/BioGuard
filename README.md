# BioGuard — Biodiversity & Conservation Assistant 🌿🇮🇳

**BioGuard** is an offline-first, native Android conservation assistant and ecological monitoring platform built with modern Jetpack Compose, Gemini AI, Survey of India (SOI) compliant sovereign cartography, and CameraX vision.

---

## 🌟 Key Features

### 1. 🧭 Official Survey of India (SOI) Sovereign Cartography
- **Accurate Borders**: Precise sovereign territorial boundaries of the Republic of India including Jammu & Kashmir, Ladakh, Arunachal Pradesh, Sir Creek, and island territories (Andaman & Nicobar, Lakshadweep).
- **EPSG:3857 Web Mercator Projection**: Responsive rendering with zoom, pan, pinch gestures, and automated boundary-fit bounding box calculation.
- **Ecological Layers**: Himalayan Snow Zones, Western Ghats biodiversity hotspots, major river basins (Ganga, Brahmaputra, Indus, Godavari, Krishna, Narmada), and critical national parks & biosphere reserves.
- **Astronomical & Geodetic Overlays**: Tropic of Cancer ($23.5^\circ\text{ N}$) and Indian Standard Time Meridian ($82.5^\circ\text{ E}$).

### 2. 🔍 Real-Time Species Identification & CameraX Vision
- **Live Camera Lens**: In-app camera preview powered by Android Jetpack CameraX with torch/flash toggle, tap-to-focus, and photo capture.
- **Gemini AI Vision Analysis**: Multimodal identification of flora and fauna species, IUCN Red List conservation status, ecological role, and preservation guidance.
- **Gallery Import**: Alternative photo picker flow for analyzing pre-captured observations.

### 3. 🛡️ Eco-Citizen Field Reporting
- **Incident Logging**: Record poaching, deforestation, human-wildlife conflict, forest fires, or illegal encroachment.
- **Evidence Attachment**: Photo capture, GPS coordinates, severity classification, and offline draft storage.
- **Sync Architecture**: Multi-device cloud sync with offline-first Room database resilience.

### 4. 👤 User Profile & Eco-Cloud Account
- **Eco-Credibility System**: Observation counter, verification badges, and rank progression.
- **Account Management**: Seamless Google Sign-In and email login, multi-device backup, and accessible **Log Out** options with clear confirmation dialogs.

---

## 🛠️ Architecture & Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin 2.0+ (100% Kotlin DSL) |
| **UI Framework** | Jetpack Compose with Material 3 Design System |
| **Local Database** | Room Database (SQLite) with Flow & Coroutines |
| **Camera & Media** | AndroidX CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view`) |
| **AI Integration** | Google Gemini Generative AI (Vision & Multimodal Text) |
| **Authentication** | Google Identity Services (`CredentialManager`) & Firebase Auth |
| **Cartography** | Vector-projected sovereign GIS canvas (`Canvas`, Mercator projection) |

---

## 🔐 Google Authentication Setup

To enable Google Sign-In with Firebase Authentication:

1. **Firebase Console**:
   - Go to the [Firebase Console](https://console.firebase.google.com/) and select your project.
   - Navigate to **Project Settings** > **General**.
   - Under **Your apps**, add an Android application with package name:
     `com.aistudio.bioguard.kxmpzq`
2. **Add SHA-1 Fingerprint**:
   - Add your debug and release SHA-1 keystore fingerprints.
3. **Web Client ID**:
   - In Firebase Console, go to **Authentication** > **Sign-in method** > **Google**.
   - Copy the **Web client ID** (OAuth 2.0 Client ID) and configure it in the application.
4. **Configuration File**:
   - Download `google-services.json` and place it in the `/app` directory.

---

## 🚀 Building and Running

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17+
- Android SDK 34 (Android 14) or newer

### Build Commands
```bash
# Build the debug APK
gradle assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

---

## 📜 Legal & Sovereign Compliance
The India sovereign boundaries rendered in this application adhere to Survey of India guidelines, accurately depicting all external boundaries including the Union Territories of Jammu & Kashmir and Ladakh. Developed for educational, research, and environmental conservation purposes.
