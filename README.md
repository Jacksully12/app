# Granpa Pump Selector - Native Android App

This is a native Android Studio project for the Granpa Pump Selector app.

## Latest update
- App renamed to **Granpa**
- Main screen model search moved to the top
- Brand filter removed from the main search screen
- Model details screen includes a full end-to-end performance curve chart
- Direct **Share WhatsApp** button added inside model details
- Share output is an image only, not PDF
- Shared image does not show catalogue page number
- App logo added as app icon and header logo

## Build APK
1. Upload this project to GitHub.
2. The included GitHub Actions workflow runs on push.
3. Open Actions → Build Android APK.
4. Download artifact: `granpa-pump-selector-debug-apk`.

Or open in Android Studio and run:
Build → Build Bundle(s) / APK(s) → Build APK(s)

## Logic
- Fixed head only
- Strict fixed-head calculation
- No 10% upper limit
- Fixed flow = estimated flow at fixed head must be >= required flow
- Range flow = estimated flow at fixed head must be inside range
- Reverse range supported, e.g. 13,500 to 4,500
- Search ignores spaces, so ACS1125 finds ACS 1125
