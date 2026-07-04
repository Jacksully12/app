# AGENTS.md — Granpa Pump Selector Project Rules

This project is a native Android app for selecting pump models from a catalogue dataset.

## Core product rules

1. The app name is **Granpa**.
2. The app must stay a **native Android app**. Do not convert it into HTML, WebView, React, Flutter, or a website wrapper.
3. The pump catalogue data is loaded from `app/src/main/assets/pumps.json`.
4. The app is intended to work offline for selection and model viewing.
5. GitHub Actions must build a debug APK artifact using `.github/workflows/android.yml`.

## UI rules

### Main screen
- Keep the model search field near the top so the user can type easily.
- Do not add a brand filter to the main search screen.
- Keep fixed-head selection simple.
- Keep the app logo visible in the header.

### Results screen
Each result card must keep this detailed technical format:

```text
MODEL
HP • kW • Phase S/T
Pump type/category
At selected head: estimated LPH • difference/status
Page N • Size X • BRAND
```

Do not remove page number, size, or brand from the in-app result cards.

### Model details screen
The model details page must keep full technical detail:
- model number
- category
- HP / kW / phase
- estimated flow at selected head
- performance curve chart
- curve points used
- quick specs
- catalogue section
- direct Share WhatsApp button
- Share Image fallback button

The **Quick specs** card must keep these fields when available:
- Delivery / Pipe size
- Page
- Head range
- Discharge range
- Flow range
- Brand
- Stages
- Sheet

### Share image
The generated WhatsApp/share image is customer-facing and must be cleaner than the app details screen.

Rules for share image:
- Do not show page number.
- Do not show internal sheet name.
- Do not show too many catalogue details.
- Include Granpa branding.
- Include model number, HP, kW, phase, pump type, selected head, estimated flow, and performance curve.
- The chart must have no overlapping labels or legends.

## Chart rules

1. Each model details screen shows **one curve line only**.
2. The chart must use:
   - Y-axis: Head (m)
   - X-axis: Flow Rate (LPH)
3. The visible curve must feel complete from axis to axis:
   - start from the head side / Y-axis
   - continue smoothly through catalogue points
   - end at the flow side / X-axis
4. The selected operating point must be orange with dashed guide lines.
5. Do not show multiple curves unless a future compare feature is explicitly added.

## Selection logic rules

1. Use fixed head only.
2. Do not apply a 10% upper limit.
3. Fixed flow mode: estimated flow at selected head must be greater than or equal to required flow.
4. Range mode: estimated flow must be inside the normalized min/max range.
5. Reverse flow ranges must be accepted, e.g. `13500 to 4500` must be treated as `4500 to 13500`.
6. Convert all flow values internally to LPH:
   - LPH = direct
   - LPM × 60
   - LPS × 3600
   - m³/hour × 1000
7. Do not extrapolate for selection outside the catalogue head range.
8. Use linear interpolation between available catalogue curve points for selection estimates.

## Build rules

- Java 17.
- Android Gradle Plugin requires Gradle 8.9 or compatible.
- Keep GitHub workflow YAML free from merge conflict markers.
- Artifact name should be `granpa-pump-selector-debug-apk`.

## Data rules

- Keep catalogue data source in `pumps.json`.
- Do not manually change individual pump values unless verified against the source catalogue.
- Keep page numbers in the app because they help verify against the physical catalogue.
- Page numbers must not appear in the final WhatsApp/share image.

## Future feature notes

Possible future features:
- compare models
- favourites
- recent searches
- total head/friction calculator
- product image mapping table
- image cache for online product images

Do not add network fetching for product images unless the app includes clear fallbacks and Android internet permissions.
