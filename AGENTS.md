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
