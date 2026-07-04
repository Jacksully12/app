# Granpa Pump Selector

Native Android pump selector app for Granpa.

## Latest update — v1.4.0

This version keeps the detailed in-app format requested for model results and model details, while keeping the WhatsApp/share image clean and customer-facing.

### In-app format

Result cards show:

```text
MODEL
HP • kW • Phase S/T
Pump type/category
At selected head: estimated LPH • difference/status
Page N • Size X • BRAND
```

Model details screen shows:
- model header
- performance curve
- curve points used
- quick specs
- catalogue section
- Share WhatsApp / Share Image buttons
- Copy model number / Back buttons

Quick specs shows:
- Delivery / Pipe size
- Page
- Head range
- Discharge range
- Flow range
- Brand
- Stages
- Sheet

### Share image format

The final WhatsApp/share image is simplified:
- no page number
- no sheet name
- no unnecessary internal catalogue details
- includes Granpa branding, model, key specs, estimated flow, and chart

## Project context files

Important project rules and context are stored in:

- `AGENTS.md`
- `agent.md`
- `PROJECT_CONTEXT.md`
- `DATA_RULES.md`
- `UI_RULES.md`

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

## Catalogue section source text rule
- The in-app model details screen must show the full catalogue/source row text for the selected model so the user can cross-check against the original CSV/Excel/PDF catalogue.
- Do not remove Page, Brand, Stages, Sheet, head range, discharge range, pipe size, or curve point values from the in-app details.
- The WhatsApp/share image remains customer-facing and must not show the page number.


## v17 layout/catalogue fixes
- Added safe top padding to avoid content being cut at the top of all screens.
- Added spacing between Phase dropdown and the Find suitable pumps button.
- Browse full catalogue button now uses a clear blue filled style.
- Catalogue section now uses compact page-header text from the catalogue data in `pumps.json`.
