# Granpa Pump Selector

Native Android pump selector app for Granpa. The app loads catalogue data from `app/src/main/assets/pumps.json` and works offline for pump search, selection, details and share image generation.

## Version 2.0.0 UI/UX update

This version reviews and improves the whole app:

- Safer top padding on all screens so content is not cut off.
- Cleaner cards, typography, spacing and button hierarchy.
- Improved dropdowns with readable two-line options.
- Main category and sub category labels are clearer.
- Search box remains near the top of the main screen.
- Results cards keep technical dealer format.
- Model details screen keeps page number, size, brand, stages, sheet and catalogue section.
- Share image is simplified and customer-facing.
- WhatsApp share and image download are direct buttons in model details.
- Shared/downloaded image does not include page number.
- GitHub Actions workflow uses Gradle 8.9.

## Build APK using GitHub Actions

1. Upload this project to GitHub.
2. Go to **Actions**.
3. Run **Build Android APK**.
4. Download artifact: **granpa-pump-selector-debug-apk**.

## Local build

Open the project in Android Studio or run:

```bash
gradle assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/
```
