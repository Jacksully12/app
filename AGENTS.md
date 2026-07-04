# AGENTS.md — Granpa Pump Selector Project Rules

## Project identity
- App name: Granpa.
- Project type: Native Android Java app.
- Do not convert this into HTML, WebView, React, Flutter, or a website wrapper.

## Data rules
- Main pump data is in `app/src/main/assets/pumps.json`.
- Catalogue section data is also included in `app/src/main/assets/catalogue_sections.csv`.
- Use LPH internally for all flow comparisons.
- Do not apply a 10% upper limit.
- Do not extrapolate for pump selection outside the catalogue head range.
- Use linear interpolation between catalogue curve points for the operating estimate.

## Main screen rules
- Model search must stay near the top.
- Brand filter must not be shown on the main search screen.
- Keep fixed head and water flow inputs simple.
- Browse full catalogue button must have a clear blue filled style.

## Results screen rules
Each result card must keep this format:
```text
MODEL
HP • kW • Phase S/T
Pump type/category
At selected head: estimated LPH • difference/status
Page N • Size X • BRAND
```

## Model details rules
Model details must include:
- model header
- performance curve
- curve points used
- quick specs
- catalogue section
- one Share WhatsApp button
- one Download Image button

The details screen may show page number. The final share/download image must not show page number.

## Catalogue section rule
The Catalogue section should be compact:
```text
TEXMO | catalogue heading / voltage-frequency line
```
This must come from `catalogueSectionText` in `pumps.json`, generated from catalogue page header data.

## Share image rules
- Customer-facing image only.
- No page number.
- No sheet name.
- No internal raw catalogue row text.
- Include Granpa branding, model, HP, kW, phase, type, selected head, estimated flow, and chart.
- Avoid overlapping text in chart labels, legend, and footer.

## Chart rules
- One curve line per model.
- Y-axis = Head (m).
- X-axis = Flow Rate (LPH).
- Selected operating point must be orange with dashed guide lines.
- Visible curve may be extended to axis endpoints for readability, but catalogue points must remain unchanged in the data cards.

## Build rules
- Java 17.
- GitHub Actions uses Gradle 8.9.
- Artifact name: `granpa-pump-selector-debug-apk`.
- No Git merge conflict markers in workflow files.
