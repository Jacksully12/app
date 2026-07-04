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


## v2.1.0 chart cleanup
- Removed white catalogue point markers from the performance chart.
- Chart now shows only the smooth blue curve and the orange selected operating point.
- Removed the artificial long flat start line from the chart curve.
- Added the selected point into the drawing path so the orange point sits on the curve.
- Share/download image chart uses the same cleaner curve style.


v2.2.0 chart grid visibility update:
- Made inside chart grid lines clearly visible on both app chart and share/download image chart.
- Grid lines now use a stronger light-grey dashed style instead of very faint lines.
- Selected point guide lines remain orange dashed lines to both axes.
- Added orange value badges for selected head and selected flow on the chart.
- Kept the chart clean: one blue curve line and one selected orange point only.


## v2.3.0 natural curve update
- Chart curve now stays within the real catalogue curve range and no longer touches the axis ends.
- App chart and WhatsApp/download image chart both use the same natural curve style.
- Selected point and orange dashed guide lines remain visible.


## v2.4.0 grid + zoom update
- Added smaller minor grid lines in addition to the main grid lines on app charts and shared images.
- Added a zoom option so users can open the chart in a closer view and zoom in or out when needed.
- Natural curve shape, selected orange point and orange guide lines remain unchanged.
