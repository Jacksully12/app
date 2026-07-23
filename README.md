# Granpa Pump Selector — v5.9.0

Offline native Android pump selector for **Texmo, Lubi and KSB**. This package is the corrected source/QA build created from the supplied workbooks, PDFs, screenshots and v58 handoff.

## Current dataset

| Brand | Total records | Recommendation eligible | Excluded |
|---|---:|---:|---:|
| Texmo | 1,820 | 1,820 | 0 |
| Lubi | 1,871 | 1,867 | 4 catalogue-only bare-pump variants |
| KSB | 1,273 | 1,260 | 13 source-table anomalies |
| **Total** | **4,964** | **4,947** | **17** |

The 13 KSB anomalies are kept in catalogue view with their source values unchanged, but they cannot enter recommendations, comparison results or customer share/download images.

## Key v5.9.0 changes

- Added 86 missing Lubi rows: 82 performance records from Pages 70–72 and 4 Page 74 bare-pump variants.
- Removed 24 excess Lubi technical duplicates from the repeated Page 30/31 table.
- Removed 7 damaged legacy `AS` rows superseded by correct `LAS` records.
- Corrected four shifted Texmo Page 32 records from the authoritative `Page 32 Layout` worksheet.
- Re-transcribed all 47 previously flagged KSB agricultural rows from supplied screenshots.
- Preserved 13 internally inconsistent KSB source curves as nonselectable `SOURCE_ANOMALY` records rather than guessing values.
- Separated jet pumps from surface monoblocks.
- Made automatic recommendations require an explicit electrical phase.
- Unified main-selector and brand-comparison ranking.
- Centralised curve validation/interpolation for selection, on-screen charts and shared images.
- Blocked unapproved/catalogue-only/anomalous records from customer sharing.
- Added source-point markers to shared charts.
- Preloaded all catalogues off the UI thread and debounced catalogue search.
- Added system-bar inset handling for Android 15/16 edge-to-edge layouts.
- Disabled Android backup and cleartext traffic.
- Updated build configuration to API 36, AGP 8.13.2, Gradle 8.13 and Java 17.
- Added source-aware validation and core selector unit tests.

## Validation

Run:

```bash
python3 tools/validate_data.py
gradle --no-daemon testDebugUnitTest lintDebug assembleDebug
```

The pinned GitHub Actions workflow runs data validation, JVM unit tests, Android lint and a debug APK build.

## Release status

The corrected **source package is ready for CI/device QA**, but it is not yet a production-distribution APK. Before external release, build it in Android Studio or GitHub Actions, test the generated APK on real phones/tablets, and sign a release build with the organisation's private keystore.

See:

- `FINAL_AUDIT_AND_CHANGELOG_V59.md`
- `QA_REPORT_V59.md`
- `KNOWN_REMAINING_SOURCE_REVIEW.md`
- `DATA_CORRECTIONS_V59.csv`
- `ISSUE_REGISTER_V59.csv`

Historical v5.5–v5.8 reports are retained under `docs/archive/` and are superseded by the v5.9 documents.
