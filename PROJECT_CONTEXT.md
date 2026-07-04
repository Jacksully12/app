# Granpa Pump Selector — Project Context

Granpa Pump Selector is a native Android app that helps users find suitable pump models from a catalogue dataset.

## Current user requirements

- Native Android app, not WebView or HTML.
- App name: Granpa.
- Logo is included as `app_logo.png`.
- Main screen model search must be at the top.
- Brand filter must not be shown on the main screen.
- Results cards should keep detailed technical info including page number.
- Model details screen should keep full technical info and catalogue context.
- Share image is for WhatsApp/customer sharing and must be simplified.
- Page number should be visible in the app but hidden from the final shared image.
- Direct Share WhatsApp button should be available when a model details screen is opened.

## Important design split

### In-app view
Detailed and technical. Good for dealer/internal checking.

### Share image
Clean and customer-facing. Good for WhatsApp sharing.

## Chart requirement

The model chart must show one full end-to-end performance curve line with:
- Head (m) on Y-axis
- Flow Rate (LPH) on X-axis
- selected operating point in orange
- dashed guide lines
- no overlapping text

## Catalogue section display rule
- The in-app model details screen must show a compact Catalogue section in this style: `TEXMO | catalogue heading / voltage/frequency line`.
- The Catalogue section text must come from the catalogue/CSV page header data stored in `pumps.json` as `catalogueSectionText`.
- Do not show the full raw row text in this section.
- Keep detailed row values in Quick specs and Curve points used instead.
- The WhatsApp/share image remains customer-facing and must not show the page number.
