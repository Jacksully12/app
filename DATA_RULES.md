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
