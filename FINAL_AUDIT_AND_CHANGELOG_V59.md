# Granpa Pump Selector — Final Audit and Changelog v5.9.0

## Release-readiness verdict

The supplied datasets and source code have been corrected to the point that they are **ready for CI compilation and controlled device QA**. They are **not yet safe for public/customer distribution** because no APK could be built or installed in this environment, the release is unsigned, and 13 KSB source-table anomalies remain intentionally excluded.

## Data work completed

### Texmo

- Retained 1,820 records, including the split `SMES90/09` and `SMES90/10` variants.
- Rechecked source units, phases, categories and provenance.
- Corrected Page 32 models `CRDP03S`, `CDP0380S`, `CDP0780S` and `CRDP11S` from the authoritative Page 32 layout sheet.
- Final: **1,820 total / 1,820 selectable / 0 pending**.

### Lubi

- Added 34 Page 70 rows, 27 Page 71 rows, 21 Page 72 rows and four Page 74 bare-pump variants.
- Removed 24 exact Page 30/31 repeated-table duplicates.
- Removed seven corrupted legacy `AS` aliases where correct `LAS` records already exist.
- Consolidated source-page provenance for the retained duplicate-side records.
- Preserved distinct variants when insulation class, phase, RPM, starting method, cable size or motor configuration differs.
- Final: **1,871 total / 1,867 selectable / 4 catalogue-only / 0 pending screenshots**.

### KSB

- Retained 455 domestic and 818 agricultural records.
- Re-transcribed all 47 previously flagged agricultural rows from the supplied screenshots.
- Enabled the 34 rows whose source curves validate.
- Preserved and excluded 13 rows whose printed source curves remain internally inconsistent.
- Final: **1,273 total / 1,260 selectable / 13 source anomalies / 0 `NEEDS_REVIEW` rows**.

## Application fixes completed

- Added `CurveUtils` as the single validation/interpolation authority.
- Selection, comparison, app charts and share/download charts now reject the same invalid curves.
- Customer sharing is blocked for anomalies, catalogue-only rows and other nonselectable records.
- Manufacturer source points are visible on both app and customer charts.
- Jet pumps no longer share the surface-monoblock category.
- Electrical phase must be selected for automatic recommendations and comparison.
- Main and comparison ranking use the same quality-band-first policy.
- Closest-outside-±50% fallback remains available only in comparison when no in-band model exists.
- Catalogues preload in a background thread and search input is debounced.
- List/spinner root views are reused to reduce allocation pressure.
- Android system-bar insets are applied for modern edge-to-edge layouts.
- Main selection, comparison and catalogue filter inputs are retained across rotation/configuration recreation.
- Manifest backup and cleartext traffic are disabled.
- Android 6–9 image download now uses the system document picker instead of an app-private folder; Android 10+ saves to `Pictures/Granpa`.
- Build target updated to API 36; CI now validates data, runs tests/lint and builds an APK.
- Stale reports were moved to `docs/archive/` and replaced with a single current set.

## Files added

- `app/src/main/java/com/granpa/pumpselector/CurveUtils.java`
- `app/src/test/java/com/granpa/pumpselector/PumpSelectorTest.java`
- `tools/validate_data.py` (strict v5.9 validator)
- `DATA_CORRECTIONS_V59.csv`
- `ISSUE_REGISTER_V59.csv`
- `QA_REPORT_V59.md`
- `KNOWN_REMAINING_SOURCE_REVIEW.md`
- `FINAL_AUDIT_AND_CHANGELOG_V59.md`
- `TEST_RESULTS_V59.txt`
- `FILES_CHANGED_V59.txt`

## Principal files modified

- All three JSON assets under `app/src/main/assets/`
- `PumpSelector.java`
- `PerformanceCurveView.java`
- `ShareImageBuilder.java`
- `PumpDetailsActivity.java`
- `PumpListAdapter.java`
- `PumpRepository.java`
- `MainActivity.java`
- `CompareActivity.java`
- `CompareResultsActivity.java`
- `CatalogueActivity.java`
- `ResultsActivity.java`
- `QAActivity.java`
- `SplashActivity.java`
- `BrandSelectionActivity.java`
- `Ui.java`
- `AndroidManifest.xml`
- `app/build.gradle`
- root `build.gradle`
- `.github/workflows/android.yml`
- `README.md`

## Required final release sequence

1. Run the GitHub Actions workflow or open the project with an API 36 Android SDK.
2. Confirm validator, JVM tests, lint and `assembleDebug` all pass.
3. Install the debug APK and complete the device matrix in `QA_REPORT_V59.md`.
4. Obtain manufacturer confirmation for any KSB anomaly that must become selectable; otherwise keep all 13 excluded.
5. Configure the private production keystore outside source control.
6. Build, sign and verify the release APK/AAB.
7. Re-run a smoke test against the signed artifact before distribution.
