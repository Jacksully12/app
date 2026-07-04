# Granpa Pump Selector

Native Android pump selector app for Granpa.

## Latest update

This version fixes the chart and share image layout:

- App name: **Granpa**
- Model search is at the top of the main screen
- Direct **Share WhatsApp** button in model details
- Image-only sharing, no PDF export
- App details screen keeps page number and full technical details
- Final shared image hides page number
- Performance curve now visually starts from the Y-axis and ends at the X-axis
- Share image chart spacing fixed so axis labels and legend do not overlap
- GitHub Actions updated to Gradle 8.9

## Build APK using GitHub Actions

1. Upload/replace this project in your GitHub repo.
2. Go to **Actions**.
3. Run **Build Android APK**.
4. Download the artifact named **granpa-pump-selector-debug-apk**.

## Local build

Open the project in Android Studio and run:

```bash
gradle assembleDebug
```

The APK will be created at:

```text
app/build/outputs/apk/debug/
```
