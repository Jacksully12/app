# Data Rules

- Source data is converted into `pumps.json`.
- Pump records include model, brand, category, phase, HP, kW, stages, head range, discharge range, flow unit, size, page, sheet and curve points.
- Catalogue section uses `catalogueSectionText` generated from page headers.
- Flow calculations use LPH internally.
- Page numbers remain visible inside the app for catalogue verification.
- Page numbers are hidden in share image output.


## v2.3.0 natural curve update
- Chart curve now stays within the real catalogue curve range and no longer touches the axis ends.
- App chart and WhatsApp/download image chart both use the same natural curve style.
- Selected point and orange dashed guide lines remain visible.


## v2.4.0 grid + zoom update
- Added smaller minor grid lines in addition to the main grid lines on app charts and shared images.
- Added a zoom option so users can open the chart in a closer view and zoom in or out when needed.
- Natural curve shape, selected orange point and orange guide lines remain unchanged.
