# Known Remaining Source Review — v5.9.0

## Texmo

No known source-data gaps remain in the supplied workbook. All 1,820 records are recommendation eligible.

## Lubi

No additional screenshots are required for the supplied performance tables. The dataset contains 1,871 records; 1,867 are recommendation eligible. The four Page 74 bare-pump variants are intentionally catalogue-only because they do not define an electrical phase/motor configuration.

## KSB

All 47 formerly flagged agricultural rows were re-transcribed. **Thirteen records still reproduce internally inconsistent values printed in the manufacturer source.** The app preserves those values, marks them `SOURCE_ANOMALY`, excludes them from selection/comparison, and disables customer sharing.

| Source page | Model | Reason confirmation is still required |
|---:|---|---|
| 31 | UPFN 200 2ST + UMAI / H 150 (Radial Flow) | Published 223 m value makes the curve internally impossible. |
| 48 | BPHA 384 4D + HBC (SMALL) | Low-flow source points increase instead of decreasing. |
| 48 | BPHA 384 5J + HBC (SMALL) | Low-flow source points increase instead of decreasing. |
| 49 | BPHA 384 05D + HBCN (BIG) | Low-flow source points increase instead of decreasing. |
| 49 | BPHA 384 06D + HBCN (BIG) | Low-flow source points increase instead of decreasing. |
| 49 | BPHA 384 07G + HBCN (BIG) | Low-flow source points increase instead of decreasing. |
| 60 | MR 7.5 A / 7.5 C- 65-50-39 | Several published flow values rise with increasing head. |
| 61 | MR 30 C- 125-100-45 | Published value at 45 m reverses the expected curve direction. |
| 61 | MR 30 C- 100-75-60 | Multiple published points reverse the expected curve direction. |
| 62 | MR 15 FC- 80-65-56 | Published value at 60 m reverses the expected curve direction. |
| 63 | MR 25 C- 125-100-38 | Published 968 LPM-like point is inconsistent with surrounding values. |
| 67 | VO 100 / 7 | Published 146/147 m sequence is internally non-monotonic. |
| 67 | VO 150 / 4 | Published 59/74 m sequence is internally non-monotonic. |

A corrected KSB technical datasheet, later catalogue edition, or written manufacturer confirmation is required before these 13 records can be enabled. No value has been guessed.

## Build/runtime review still required

- Generate the APK through the included CI workflow or Android Studio.
- Install and test on at least one Android 6/7 device or emulator, one Android 13–15 phone, one Android 16 device/emulator, and one tablet/foldable-sized screen.
- Verify WhatsApp and WhatsApp Business sharing, MediaStore download, rotation/state retention, TalkBack, large fonts and low-memory startup.
- Produce and securely sign the final release APK/AAB.
