# Granpa Pump Selector v5.9.0 — QA Report

## Verdict

**Source/data QA: PASS with controlled exclusions.**  
**Production release: HOLD until an Android build, device regression test and release signing are completed.**

## Final counts

| Brand | Total | Selectable | Needs review | Source anomaly | Catalogue-only |
|---|---:|---:|---:|---:|---:|
| Texmo | 1,820 | 1,820 | 0 | 0 | 0 |
| Lubi | 1,871 | 1,867 | 0 | 0 | 4 |
| KSB | 1,273 | 1,260 | 0 | 13 | 0 |
| **Total** | **4,964** | **4,947** | **0** | **13** | **4** |

## Tests completed in this audit

- Parsed all three JSON assets successfully.
- Ran `tools/validate_data.py`: **PASS**.
- Verified record counts, metadata counts, unique IDs and mandatory provenance fields.
- Verified no recommendation-eligible record has an invalid shared curve.
- Verified curve range metadata agrees with source points.
- Verified all 24 known Lubi Page 30/31 duplicates are removed.
- Verified Page 70/71/72 Lubi counts are 34/27/21 and four Page 74 bare-pump variants exist.
- Verified seven damaged `AS` aliases are absent and correct `LAS` records remain.
- Verified the four Texmo Page 32 fixtures against the authoritative layout worksheet.
- Verified the exact set of 13 KSB `SOURCE_ANOMALY` records.
- Compiled and ran a standalone Java harness for conversion, interpolation, curve rejection, category separation, phase safety, ranking consistency and share eligibility: **PASS**.
- Parsed all Android XML files: **PASS**.
- Parsed GitHub Actions YAML: **PASS**.
- Ran Python bytecode compilation for the validator: **PASS**.
- Ran Java source parsing through `javac`; no syntax diagnostics were found before expected missing-Android-SDK symbol errors.
- Verified state-save/restore handlers are present for the main selector, compare form and catalogue filters.

## Tests not executable in this environment

This execution environment does not contain Gradle or the Android SDK and cannot retrieve the Android toolchain through its shell. Therefore the following were configured but not executed locally:

- `testDebugUnitTest`
- Android lint
- `assembleDebug`
- APK installation/instrumentation testing
- physical-device share/download testing

The included GitHub Actions workflow pins Java 17, Gradle 8.13, Android API 36, Build Tools 36.0.0 and AGP 8.13.2, then runs the above checks.

## Security/build review

- No Internet permission is requested; datasets remain offline assets.
- Cleartext traffic is disabled.
- App backup is disabled.
- File sharing uses a non-exported `FileProvider` and content URIs.
- Legacy Android download uses the Storage Access Framework, avoiding broad storage permission; Android 10+ uses MediaStore.
- Only WhatsApp and WhatsApp Business are declared in package-visibility queries.
- Activities without launcher intent filters are not exported.
- Customer share/download is blocked for nonapproved records.
- Current source targets API 36; release signing material is intentionally not included.

## Manual device matrix required before production

1. Cold start and catalogue loading on a low-memory device.
2. Every brand/category/phase combination.
3. Fixed-flow, range-flow and closest-fallback results.
4. Compare screen consistency with normal selector.
5. Invalid/anomalous record warnings and disabled sharing.
6. Chart zoom, pan, interpolation markers and share-image parity.
7. Rotation, multi-window, tablet/foldable layouts and system-bar insets.
8. TalkBack, font scaling, keyboard navigation and colour contrast.
9. WhatsApp, WhatsApp Business and generic share chooser.
10. Image download on Android 9 and Android 10+ storage paths.
