# TEXMO Pump Selector - Native Android App

This is a proper native Android app source project. It is **not** an HTML/WebView wrapper.

## Screens included

1. **Search screen**
   - Fixed head input
   - Fixed water flow or water-flow range
   - Unit selection: LPH, LPM, LPS, m³/hour
   - Pump type filter
   - Phase filter
   - Brand filter
   - Model/keyword search

2. **Results screen**
   - Strict fixed-head result list
   - Search within result list
   - Copy CSV result to clipboard
   - Tap any model to open full details

3. **Catalogue search screen**
   - Search all 1,782 catalogue models
   - Model search works without spaces, for example `ACS1125` finds `ACS 1125`
   - Filter by category and brand

4. **Pump details screen**
   - Model number, HP, kW, phase
   - Head range, discharge range, pipe/delivery size
   - Page number
   - Curve points used by the calculation

5. **Data QA screen**
   - Record count
   - Page coverage
   - Logic notes
   - Sample check results

## Selection logic

- Fixed head only.
- No 10% upper limit.
- Fixed flow mode: the estimated flow at the fixed head must be equal to or above the required flow.
- Flow range mode: the estimated flow at the fixed head must be inside the entered range.
- Reverse flow range is accepted, for example `13,500 to 4,500`.
- Flow units are converted internally to LPH.
- Pump curve calculation uses linear interpolation between catalogue curve points.

## Data QA

- 1,782 / 1,782 catalogue rows included.
- Pages 1-43 covered.
- 0 skipped rows.
- Page 31 and Page 32 correction included.
- `ACS1125` search matches `ACS 1125`.

## How to build APK

1. Install Android Studio.
2. Open this folder: `texmo_pump_selector_native_android`.
3. Wait for Gradle sync.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. The APK will be generated inside `app/build/outputs/apk/`.

## Notes

This environment did not include Android SDK / Gradle build tools, so the final APK could not be compiled here. The project is APK-ready and can be built in Android Studio.
