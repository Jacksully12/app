# Granpa Pump Selector v5.9.1 Build Fix

## Fixed CI failure

GitHub Android lint failed because `TreeMap.computeIfAbsent` requires Android API 24 while the application supports API 23.

The following API-24-only Java calls were replaced with API-23-compatible code:

- `TreeMap.computeIfAbsent` in both curve grouping paths.
- `Double.isFinite` in curve validation and interpolation input validation.
- `Comparator.comparingDouble` in chart-point sorting.

## Prevention added

- Added `tools/check_min_sdk_compat.py` to detect known API-24-only Java calls when `minSdk` is below 24.
- Added the compatibility check to GitHub Actions before unit tests and Android lint.
- Updated the app to version `5.9.1` (`versionCode 60`).
- Updated supported GitHub Actions to Node.js 24-based major versions where official versions are available.

The datasets are unchanged from the audited v5.9.0 data baseline.
