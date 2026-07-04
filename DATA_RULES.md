# Data Rules

1. Source dataset: `app/src/main/assets/pumps.json`.
2. Each pump record should contain:
   - model
   - brand
   - category
   - phase
   - HP
   - kW
   - stages
   - head range
   - discharge range
   - flow unit
   - size
   - page
   - curve points
3. Flow calculations use LPH internally.
4. Selection estimates use interpolation only inside the pump's catalogue head range.
5. Do not guess missing catalogue values.
6. Keep page numbers in app details for catalogue verification.
7. Do not include page number in customer share image.

## Catalogue section display rule
- The in-app model details screen must show a compact Catalogue section in this style: `TEXMO | catalogue heading / voltage/frequency line`.
- The Catalogue section text must come from the catalogue/CSV page header data stored in `pumps.json` as `catalogueSectionText`.
- Do not show the full raw row text in this section.
- Keep detailed row values in Quick specs and Curve points used instead.
- The WhatsApp/share image remains customer-facing and must not show the page number.
