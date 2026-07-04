# Granpa Pump Selector

Native Android app for pump selection.

## v1.6.0 updates
- Only two main model-detail actions:
  - Share WhatsApp
  - Download Image
- Generic Share Image button removed.
- Download Image saves the generated pump recommendation image to Pictures / Granpa on modern Android.
- Improved UI spacing, card styling, safe top padding, and button colours across the app.
- Catalogue section uses compact catalogue page-header text from `pumps.json`.
- Page number remains visible in the app but is hidden from share/download images.

## Build APK with GitHub Actions
1. Upload/replace this project in your GitHub repo.
2. Open Actions.
3. Run **Build Android APK**.
4. Download the artifact named `granpa-pump-selector-debug-apk`.

## Local build
Open in Android Studio, or run:
```bash
gradle assembleDebug
```
