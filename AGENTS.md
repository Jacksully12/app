# AGENTS.md — Granpa Pump Selector Rules

## Product rules
- App name: Granpa.
- Native Android app only. Do not convert to WebView, HTML, Flutter or React Native unless explicitly requested.
- Core data lives in `app/src/main/assets/pumps.json`.
- App should remain useful offline.

## Selection logic rules
- Fixed head only.
- No 10% upper limit.
- Fixed flow mode: estimated flow at selected head must be greater than or equal to required flow.
- Range mode: estimated flow must be inside normalized min/max range.
- Reverse flow range must be accepted.
- Convert all units internally to LPH.
- Use interpolation between catalogue curve points only; do not extrapolate selection outside catalogue head range.

## UI rules
- Main model search stays near the top.
- Use custom readable dropdowns with main category and sub category wording.
- Results cards must include page number, size and brand inside the app.
- Model details must show full technical information.
- Catalogue section must use compact catalogue header text from data.
- Share image must not show page number or sheet name.
- Direct model detail actions: Share WhatsApp and Download Image.

## Chart rules
- One curve line per model.
- Y-axis = Head (m), X-axis = Flow Rate (LPH).
- Curve should visually run end-to-end from head side to flow side.
- Selected operating point is orange with dashed guide lines.

## Build rules
- Java 17.
- Gradle 8.9 in GitHub Actions.
- APK artifact name: granpa-pump-selector-debug-apk.


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


## v2.5.0 pinch zoom update
- Added Android-style two-finger pinch zoom support on the closer chart view.
- The chart remains a clean line-only performance curve with the selected orange point and orange guide lines.
- Major and minor grid lines remain visible for easier reading.


## v2.6.0 zoom move + finer grid update
- Improved closer chart view with Android-style pinch zoom and drag-to-move panning.
- Added +/- zoom buttons plus reset view as a fallback for users who prefer buttons.
- Increased minor grid density so the chart can be checked more precisely.
- Kept the chart as a clean line-only performance curve with the orange selected point and guide lines.


## v2.7.0 tap-to-zoom + improved chart navigation
- Tapping the chart on the model details screen now opens the closer zoom view directly.
- The closer chart view supports pinch zoom, double-tap zoom/reset, and drag-to-move panning after zooming.
- Zoom percentage updates while using gestures or buttons.
- Chart gridlines were slightly strengthened for better technical checking.


## v2.8.0 unit display + sorting update
- LPM is now the default unit on the main screen.
- Default flow example changed from 1,200 LPH to 20 LPM.
- When LPH is selected, results, details, curve points, chart axis, zoom chart, share image, download image and CSV use LPH.
- When LPM is selected, the same screens use LPM.
- Internal calculations still use LPH for consistency, but all user-facing values follow the selected unit.
- Fixed result ordering so closest suitable pumps appear first before very oversized pumps.
- Oversized results are marked as oversized instead of looking like the best recommendation.
