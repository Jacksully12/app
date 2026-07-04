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
