#!/usr/bin/env python3
"""Fail CI when source uses known Java APIs unavailable on the configured minSdk."""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parents[1]
APP_GRADLE = ROOT / "app" / "build.gradle"
JAVA_ROOT = ROOT / "app" / "src"

text = APP_GRADLE.read_text(encoding="utf-8")
match = re.search(r"\bminSdk\s+(\d+)", text)
if not match:
    print("Unable to determine minSdk from app/build.gradle", file=sys.stderr)
    sys.exit(2)
min_sdk = int(match.group(1))

# These calls require Android API 24 when core-library desugaring is not configured.
# Keep this list intentionally narrow and explicit; Android lint remains authoritative.
api24_patterns = {
    r"\.computeIfAbsent\s*\(": "Map.computeIfAbsent",
    r"\.computeIfPresent\s*\(": "Map.computeIfPresent",
    r"\bDouble\.isFinite\s*\(": "Double.isFinite",
    r"\bFloat\.isFinite\s*\(": "Float.isFinite",
    r"\bComparator\.comparing(?:Double|Int|Long)?\s*\(": "Comparator.comparing*",
    r"\.stream\s*\(": "Collection.stream",
    r"\bjava\.time\.": "java.time",
}

errors = []
if min_sdk < 24:
    for path in JAVA_ROOT.rglob("*.java"):
        source = path.read_text(encoding="utf-8")
        for pattern, name in api24_patterns.items():
            for found in re.finditer(pattern, source):
                line = source.count("\n", 0, found.start()) + 1
                errors.append(f"{path.relative_to(ROOT)}:{line}: {name} requires API 24")

if errors:
    print(f"minSdk compatibility check failed (minSdk={min_sdk}):", file=sys.stderr)
    for error in errors:
        print(f"  {error}", file=sys.stderr)
    sys.exit(1)

print(f"minSdk compatibility check passed (minSdk={min_sdk})")
