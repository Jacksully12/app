# Latest release — v5.4.0

- Texmo: 1,819 records, zero review items.
- Lubi: 1,724 records, all 42 source-confirmed repairs applied.
- KSB: 455 records, OPAL split and structured technical fields applied.
- Redesigned selector, results, comparison, model details and chart.
- Added shortlist, recent activity and catalogue information.
- Sharing is text or PNG image only; there is no PDF share feature.

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


## v2.9.0 LPH default update
- LPH is now the default unit everywhere again.
- LPM remains available as a dropdown option.
- Main screen default flow is back to 1,200 LPH.
- Catalogue/details/share/chart fallback unit is LPH when no unit is passed.
- User-selected unit still controls all displayed values across results, details, chart, zoom, share image, download image and CSV.


## v3.0.0 strict ±10% fixed-flow rule
- Fixed-flow search now uses a strict tolerance band only.
- A model is shown only if estimated flow at the fixed head is between 10% less and 10% more than the searched flow.
- Nothing above +10% and nothing below -10% is shown in fixed-flow mode.
- The rule works for LPH, LPM, LPS and m³/hour because the app converts internally and displays back in the selected unit.
- Result sorting now uses closest flow difference within the ±10% band.
- LPH remains the default unit; LPM remains available in the dropdown.


## v3.1.0 openwell category correction
- Added `Openwell Submersible` as a main category.
- Corrected ASM SP models to `Openwell Submersible` instead of Centrifugal/Agricultural Monoblock.
- Updated ASM SP catalogue section to the TEXMO Openwell Submersible Monoblocks heading.
- Main category names are now:
  - All pump types
  - Borewell Submersible
  - Openwell Submersible
  - Centrifugal / Surface Monoblock
  - Dewatering / Sewage
- Category priority rule: Openwell Submersible must be checked before Monoblock/Centrifugal, because Openwell Submersible Monoblocks are not surface/centrifugal monoblocks.


## v3.2.0 nearest 2 above + 2 below rule
- Fixed-flow results now show a maximum of 4 nearby recommendations.
- The app first applies the strict ±10% flow band.
- Then it shows only:
  - 2 nearest models above/equal to the searched flow
  - 2 nearest models below the searched flow
- This works for LPH and LPM because the app converts internally and displays in the selected unit.
- Range mode is unchanged.


## v3.3.0 TEXMO brand normalization + data audit
- All records are now stored/displayed as TEXMO.
- Added `DATA_AUDIT_REPORT.txt` with data consistency checks.
- Audit checked 1,780 records for missing model, brand, category, page, catalogue section and curve data.
- Result: 0 missing model/brand/category/page/catalogue-section/curve records.
- Remaining minor data gaps noted: two models have missing size values: CDP0380S and CDP07S.
- ASM SP records remain under Openwell Submersible with TEXMO brand.


## v3.4.0 size correction
- Fixed missing size for CDP0380S: 50.
- Fixed missing size for CDP07S: 50.
- Updated DATA_AUDIT_REPORT.txt.
- Data audit now shows 0 missing size values.


## v3.5.0 dealer smart fallback bands
- Fixed-flow search now uses dealer smart matching.
- Shows maximum 4 models: 2 above/equal target flow and 2 below target flow.
- Candidate bands are labelled:
  - Best match ±10%
  - Extended match ±20%
  - Wide match ±30%
  - Last option ±50%
- Wider matches are visible instead of silently hidden, so dealers can see fallback options.
- Ranking prefers lower HP, then lower kW, then tighter tolerance band, then closest flow.
- Range mode is unchanged.


## v3.6.0 deep filter/sort QA and grouped All results
- Fixed All pump types so categories are no longer mixed into one 4-result list.
- All pump types now shows category-wise sections:
  - Borewell Submersible
  - Openwell Submersible
  - Centrifugal / Surface Monoblock
  - Dewatering / Sewage
- Each section uses max 2 above/equal + 2 below target.
- Fixed fallback sorting: band quality comes before HP, so a ±50% low-HP item cannot hide a ±10% or ±20% better match.
- Inside the same band, lower HP/kW is preferred.
- Added `LOGIC_QA_REPORT.txt` with test results for 40 m and 1200 LPH.


## v3.7.0 clean results header
- Removed the long dealer-rule explanation text from the Results summary card.
- Removed the “shown result limit” and “All pump types grouped category-wise” text from the Results summary card.
- Result cards still show useful match labels such as Best match ±10%, Wide match ±30%, and Last option ±50%.
- Grouped All pump type logic from v3.6.0 remains unchanged.


## v3.8.0 model search moved to catalogue
- Removed the first-page Search model / keyword input from the pump selection form.
- Duty-point search now uses only head, flow, pump type and phase.
- Renamed catalogue button to `Search catalogue / model list`.
- Catalogue screen is now the proper place for model search.
- Catalogue search hint improved: ACS1125, ASM SP, Openwell, 50.
- Grouped All pump type logic remains unchanged.


## v3.9.0 detailed section categories + phase mapping
- Changed detailed categories to shortened catalogue-section names.
- Category assignment now follows catalogue section boundaries using page and model-prefix splits where a page has multiple sections.
- Kept separate:
  - `SP Openwell Monoblock`
  - `SP Openwell Monoblock Water Cooled`
- Renamed High Pressure Vertical Multistage to `AVRS`.
- Changed TP-style names to `3 Phase ...`.
- Added main filters:
  - Multistage Pumps
  - Motors
- Phase filtering now uses category/title-derived phase rules, with row-level phase preserved for mixed S/T sections.
- Added `CATEGORY_PHASE_QA_REPORT.txt`.


## v4.1.0 simple coloured dropdown
- Restored the earlier simple pump-type dropdown layout.
- Removed the large badge/card style from the dropdown.
- Main categories use a subtle light-blue background with blue bold title.
- Sub categories stay white with normal dark text.
- Result category spacing from v4.0.0 is kept.


## v4.2.0 splash screen
- Added a clean loading/splash screen when the app opens.
- Splash screen shows the logo, `GRANPA`, and `Pump Selector`.
- Splash smoothly fades in and then opens the main pump selector screen.


v4.3.0
- Added collapsible grouped category sections in All pump types results.
- Tap category headers to hide/show the cards under that category.
- Added clear Show/Hide state on group headers.


## v4.4.0 Copy Results text
- Replaced `Copy CSV` with `Copy Results`.
- Copied output is now formatted readable text for WhatsApp, SMS, notes and customer sharing.
- Grouped `All pump types` results copy with category headings and numbered items.
- Copy respects the current search text, but collapsed/expanded UI state does not remove items from copied text.


## v4.5.0 QA fixes
- Fixed phase filtering so row-level phase is trusted first (`S`, `T`, or `S,T`).
- Category/title phase wording is now only a fallback when the row phase is missing.
- Results count now updates when searching inside the results screen.
- Added a clear empty message when an internal result search has no matches.
- Removed the unused INTERNET permission from AndroidManifest.xml.
- Fixed grouped header creation to use the standard header factory.


## v4.7.0 separate Compare Results screen
- Compare input is now kept on `CompareActivity`.
- Results now open on a separate `CompareResultsActivity`.
- The Compare form button is renamed to `Show compare results`.
- Compare Results screen shows summary, total comparable models, Texmo/Lubi sections and collapsible brand groups.
- Back button returns to the compare input screen for quick changes.


## v4.8.0 KSB multi-brand update
- Added KSB as a third brand on the brand/mode selection screen.
- Added `assets/ksb_pumps.json`.
- Extracted 322 KSB pump records from `Selection Chart_Domestic.pdf`.
- Compare now includes Texmo, Lubi and KSB.
- Reduced brand screen button sizes so the selection screen looks lighter.
- Added `KSB_MULTIBRAND_QA_REPORT.txt`.
- KSB PDF extraction is automatic and should be spot-checked before production use.


## v4.9.0 KSB full re-extraction and QA
- Re-read the whole KSB Domestic PDF table data.
- Rebuilt `assets/ksb_pumps.json` with 452 KSB records.
- Fixed the KSB borewell column-shift issue where head/flow values could become fake ST stage numbers.
- Corrected the CORA 4AA + UMN example: stages are now 6ST, 7ST, 8ST, 10ST, 12ST, 14ST, 15ST, 16ST and 20ST.
- Fixed KSB unit handling: LPM x 60, LPH unchanged, m3/hr x 1000.
- Improved KSB dewatering/sewage group filtering for AMA PORTER, AMA DRAINER, KSTP, AMAREX and KRTU.
- Added `KSB_FULL_REEXTRACTION_QA_REPORT.txt`.


## v5.0.0 brand-specific category dropdowns
- Preserved the established Texmo main-category dropdown exactly.
- Removed Booster / Pressure Pumps from the Texmo main-category list.
- Kept all Texmo detailed categories below the main categories.
- Search and Catalogue screens now use the same Texmo category structure.
- Lubi and KSB use their own relevant main-category lists with the same professional dropdown styling.
- Added `TEXMO_CATEGORY_DROPDOWN_QA_REPORT.txt`.

## v5.1.0 full UI, chart and data QA update
- Preserved Texmo category dropdown.
- Standardized cross-brand categories.
- Added data-quality eligibility and warnings.
- Corrected reversed Lubi HP/kW fields and cleaned extracted curves.
- Improved chart rendering with monotone smoothing and no axis touching.
- Simplified details/result UI and added live/build-time QA.
