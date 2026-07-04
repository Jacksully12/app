# UI Rules

## Dropdowns
Use `OptionAdapter` for readable two-line dropdowns. Main categories and sub categories must be easy to understand.

## Results cards
Format:

MODEL
HP • kW • Phase S/T
Pump type/category
At fixed head: estimated LPH • difference/status
Page N • Size X • BRAND

## Details screen
Show full technical details, chart, catalogue section and share/download actions.

## Share image
Clean customer-facing PNG. No page number. No sheet name. No overlapping text.


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
