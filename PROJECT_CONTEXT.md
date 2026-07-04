# Project Context

Granpa Pump Selector is a native Android catalogue-based pump selection app for dealer/customer use.

The app has:
- Main search screen
- Results screen
- Catalogue search screen
- Model details screen
- Performance curve chart
- Curve points used
- Quick specs
- Catalogue section
- WhatsApp share image
- Download image
- QA report

Important split:
- In-app details are technical and include page number.
- Share/download image is customer-facing and hides page number.


## v2.1.0 chart cleanup
- Removed white catalogue point markers from the performance chart.
- Chart now shows only the smooth blue curve and the orange selected operating point.
- Removed the artificial long flat start line from the chart curve.
- Added the selected point into the drawing path so the orange point sits on the curve.
- Share/download image chart uses the same cleaner curve style.
