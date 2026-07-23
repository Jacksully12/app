# Granpa Pump Selector v5.9.0 — Complete End-to-End Audit and Fix Report

**Audit date:** 23 July 2026  
**Audited baseline:** v5.8 source package and v5.5 debug APK package  
**Corrected output:** v5.9.0 source package

## 1. Release-readiness verdict

### Source and data

**PASS with controlled exclusions.** The known, evidence-supported Texmo and Lubi corrections have been applied. All 47 previously flagged KSB agricultural rows were re-transcribed; 34 are now approved and 13 remain deliberately excluded because the supplied manufacturer tables themselves contain internally inconsistent values.

### APK distribution

**HOLD. Do not distribute an APK yet.** This audit environment did not contain Gradle or an Android SDK, so it could not compile, install, lint or run the Android application. The corrected source is ready for CI/Android Studio compilation and controlled device QA, but it is not yet a signed, device-tested production release.

## 2. Final dataset position

| Brand | Total records | Recommendation eligible | Needs review | Source anomaly | Catalogue-only |
|---|---:|---:|---:|---:|---:|
| Texmo | 1,820 | 1,820 | 0 | 0 | 0 |
| Lubi | 1,871 | 1,867 | 0 | 0 | 4 |
| KSB | 1,273 | 1,260 | 0 | 13 | 0 |
| **Total** | **4,964** | **4,947** | **0** | **13** | **4** |

No `NEEDS_REVIEW` record remains. The 17 excluded records remain visible in catalogue/review contexts but cannot enter automatic recommendations, comparison output or customer image sharing.

## 3. Source material reviewed

The audit compared the application assets and logic with the supplied:

- Texmo workbook, including the authoritative Page 32 layout worksheet
- Lubi performance catalogue PDF and all screenshots supplied for Pages 65, 69–74 and 76–78
- KSB Domestic and Agricultural performance catalogues
- KSB full-table screenshots for the previously uncertain agricultural pages
- v5.8 Android source/handoff package
- v5.5 debug APK package
- historical data, logic and QA reports bundled with the handoff

## 4. Texmo audit and corrections

### Final result

- **1,820 total / 1,820 selectable**
- No confirmed missing source model remains in the supplied workbook.
- `SMES90/09` and `SMES90/10` remain as separate model variants.
- Source units, phases, categories and provenance were retained/rechecked.

### Confirmed Page 32 corrections

- `CRDP03S`: corrected delivery size and performance curve
- `CDP0380S`: corrected performance curve
- `CDP0780S`: corrected performance curve
- `CRDP11S`: corrected performance curve

The values were taken from the authoritative Page 32 worksheet; no unsupported value was guessed.

## 5. Lubi audit and corrections

### Added

- Page 70: **34** performance records
- Page 71: **27** performance records
- Page 72: **21** performance records
- Page 74: **4** bare-pump variants
- **Total added: 86**

The Page 74 variants are `LBM-10`, `LBM-20`, `LBM-30` and `LBM-55`. They are intentionally catalogue-only because the table does not define a complete electrical motor/phase configuration.

### Removed

- **24** excess technical duplicates caused by the repeated Page 30/31 table
- **7** corrupted legacy `AS` aliases where the correct `LAS` records already exist
- **Total removed: 31**

### Variant handling

Rows were not merged merely because model names looked similar. Separate records were preserved where there was a real difference in phase, insulation class, RPM, stage count, starting method, cable size, motor type or bare-pump configuration.

### Final result

- **1,871 total / 1,867 selectable / 4 catalogue-only**
- No more Lubi screenshots are required for the supplied catalogue tables.

## 6. KSB audit and corrections

### Completed

- Retained **455 domestic** and **818 agricultural** records.
- Re-transcribed all **47** previously flagged agricultural records from the supplied full-table screenshots.
- Approved **34** corrected records after source and curve validation.
- Removed all `NEEDS_REVIEW` statuses.

### Controlled source anomalies

The following 13 rows reproduce internally inconsistent values printed in the supplied KSB source. Their source values are preserved exactly, but the records are marked `SOURCE_ANOMALY`, nonselectable and nonshareable:

| Page | Model |
|---:|---|
| 31 | UPFN 200 2ST + UMAI / H 150 (Radial Flow) |
| 48 | BPHA 384 4D + HBC (SMALL) (Mixed flow) |
| 48 | BPHA 384 5J + HBC (SMALL) (Mixed flow) |
| 49 | BPHA 384 05D + HBCN (BIG) (Mixed flow) |
| 49 | BPHA 384 06D + HBCN (BIG) (Mixed flow) |
| 49 | BPHA 384 07G + HBCN (BIG) (Mixed flow) |
| 60 | MR 7.5 A / 7.5 C-65-50-39 |
| 61 | MR 30 C-125-100-45 |
| 61 | MR 30 C-100-75-60 |
| 62 | MR 15 FC-80-65-56 |
| 63 | MR 25 C-125-100-38 |
| 67 | VO 100/7 |
| 67 | VO 150/4 |

No inferred replacement such as changing `223` to `23`, `146` to `156`, or `59` to `79` was silently inserted. A corrected KSB datasheet, later catalogue, or written manufacturer confirmation is required before these rows can be enabled.

### Final result

- **1,273 total / 1,260 selectable / 13 source anomalies**
- No additional screenshot from the same KSB PDF is required. Only an independently corrected manufacturer source would resolve the 13 anomalies.

## 7. Selection and comparison logic fixes

- Separated `JET_PUMP` from `SURFACE_MONOBLOCK` so technically different applications are no longer mixed.
- Made phase selection explicit for automatic recommendation and comparison workflows.
- Replaced fragile phase substring matching with exact token handling for `S`, `T`, `Single Phase`, `Three Phase`, `1` and `3`.
- Unified normal-selector and cross-brand comparison ranking.
- Ranking now applies match-quality bands first, then lower HP/kW, then flow difference.
- Preserved the dealer workflow of returning up to two models above and two below the requested fixed flow.
- Preserved comparison-only closest-model fallback when no model falls within ±50%.
- Centralised flow-unit conversion for LPH, LPM, LPS and m³/hour.
- Centralised duplicate-head handling and deterministic interpolation.
- Excluded nonselectable, catalogue-only and source-anomaly rows before recommendation logic.

## 8. Chart and customer-output fixes

- Added `CurveUtils` as the shared source-curve validation/interpolation authority.
- The in-app chart and generated customer image now apply the same invalid-curve policy.
- Both chart paths display manufacturer source points.
- Customer share/download is blocked when a record is nonselectable, catalogue-only, anomalous or has an invalid curve.
- Internal warning content is displayed for controlled-exclusion records.
- Android 10+ download uses MediaStore and saves to `Pictures/Granpa`.
- Android 6–9 download uses the system document picker rather than an app-private directory or broad storage permission.

## 9. Android, performance, UI and security fixes

### Performance

- Preloads all three JSON catalogues outside the UI thread.
- Debounces catalogue search input.
- Reuses list and spinner root views to reduce unnecessary allocations.
- Reports asset-loading failure instead of silently substituting another brand.

### UI and lifecycle

- Adds system-bar inset handling for edge-to-edge layouts.
- Removes portrait-only locking and permits resizable activity layouts.
- Preserves main selection, comparison and catalogue-filter state across configuration recreation.
- Adds warning and disabled states for customer-sharing restrictions.

### Security/privacy

- No Internet permission is requested.
- Cleartext traffic is disabled.
- Android backup is disabled.
- File sharing uses a non-exported `FileProvider` and content URIs.
- Broad storage permissions are not requested.
- Only WhatsApp and WhatsApp Business are declared for package visibility.
- No release keystore or signing secret is included.

## 10. Build configuration and documentation

- Application version changed to **5.9.0**, version code **59**.
- Configured compile/target API 36, Java 17 and Android Gradle Plugin 8.13.2.
- CI pins Gradle 8.13 and Build Tools 36.0.0.
- CI runs data validation, JVM unit tests, Android lint and debug APK assembly.
- Historical reports were moved under `docs/archive/`.
- Current README, QA, source-review, issue-register and changelog documents supersede the older v5.5–v5.8 statements.

## 11. Automated and static tests completed

### Passed locally

- Source-aware data validator
- Exact final count and selectable-count checks
- Unique ID and technical-duplicate checks
- Source-file/page provenance checks
- Unit, phase and category validation
- Curve and range-metadata checks
- Lubi page/count/removal fixtures
- Texmo Page 32 fixtures
- Exact KSB controlled-anomaly fixture
- Standalone Java core-logic harness
- Python bytecode compilation
- Android XML parsing
- GitHub Actions YAML parsing
- Java delimiter/static scan across 21 source/test files
- `javac` syntax parsing with zero Java syntax/parse diagnostics before expected missing Android dependency symbols
- Manifest security assertions

### Not executable locally

- Gradle/JUnit Android unit-test task
- Android lint task
- Android debug APK assembly
- APK installation and runtime testing
- Physical/emulated storage, share, rotation, accessibility and low-memory testing
- Release signing and signed-artifact verification

The audit runtime did not contain Gradle, an Android SDK, `sdkmanager` or a Gradle wrapper, so these checks require the included CI workflow or Android Studio.

## 12. Change record

`DATA_CORRECTIONS_V59.csv` contains **192 row-level data changes**:

- Texmo: 4 modified
- Lubi: 86 added, 31 removed, 24 modified/provenance-consolidated
- KSB: 47 modified

`FILES_CHANGED_V59.txt` lists all source, configuration, documentation and test files added, changed, archived or removed from the original root.

## 13. Remaining actions before release

1. Run the included CI workflow or open the corrected source in Android Studio with the required Android toolchain.
2. Confirm `testDebugUnitTest`, `lintDebug` and `assembleDebug` pass.
3. Install the resulting debug APK and complete phone/tablet/foldable and accessibility regression testing.
4. Keep all 13 KSB anomalies excluded unless corrected manufacturer evidence is received.
5. Configure the organisation’s private signing key outside source control.
6. Build and verify a signed release APK/AAB.
7. Repeat a smoke test on the exact signed artifact before distribution.

## 14. Information still required from the user

- **Texmo:** nothing further for the supplied workbook.
- **Lubi:** nothing further for the supplied catalogue/screenshots.
- **KSB:** nothing further if the 13 source anomalies may remain excluded. To make them selectable, provide an independently corrected official KSB datasheet/catalogue or written manufacturer confirmation for those exact rows.

## 15. Final confirmation

- **Safe to send for CI compilation:** Yes.
- **Safe to use as the corrected source/data baseline:** Yes, with the 13 KSB rows excluded.
- **Safe to distribute as a production APK right now:** No. No APK was produced or installed in this environment, and release signing/device QA remain pending.
