#!/usr/bin/env python3
"""Strict source-aware validation for Granpa Pump Selector data assets."""
from __future__ import annotations

import collections
import json
import math
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "app" / "src" / "main" / "assets"
EXPECTED_COUNTS = {
    "texmo_pumps.json": (1820, 1820),
    "lubi_pumps.json": (1871, 1867),
    "ksb_pumps.json": (1273, 1260),
}
ALLOWED_CATEGORIES = {
    "BOREWELL_SUBMERSIBLE", "OPENWELL_SUBMERSIBLE", "SURFACE_MONOBLOCK",
    "JET_PUMP", "MULTISTAGE", "BOOSTER", "SEWAGE_DEWATERING", "MOTOR", "SOLAR", "OTHER",
}
KSB_SOURCE_ANOMALY_IDS = {
    "ksb_agri_upfn_200_2st_umai_h_150_radial_flow_p31",
    "ksb_agri_bpha_384_4d_hbc_small_mixed_flow_p48",
    "ksb_agri_bpha_384_5j_hbc_small_mixed_flow_p48",
    "ksb_agri_bpha_384_05d_hbcn_big_mixed_flow_p49",
    "ksb_agri_bpha_384_06d_hbcn_big_mixed_flow_p49",
    "ksb_agri_bpha_384_07g_hbcn_big_mixed_flow_p49",
    "ksb_agri_mr_7_5_a_7_5_c_65_50_39_p60",
    "ksb_agri_mr_30_c_125_100_45_p61",
    "ksb_agri_mr_30_c_100_75_60_p61",
    "ksb_agri_mr_15_fc_80_65_56_p62",
    "ksb_agri_mr_25_c_125_100_38_p63",
    "ksb_agri_vo_100_7_p67",
    "ksb_agri_vo_150_4_p67",
}

errors: list[str] = []
warnings: list[str] = []
assets: dict[str, dict] = {}


def fail(message: str) -> None:
    errors.append(message)


def warn(message: str) -> None:
    warnings.append(message)


def finite_number(value) -> bool:
    return isinstance(value, (int, float)) and not isinstance(value, bool) and math.isfinite(float(value))


def curve_groups(record: dict) -> list[tuple[float, float]]:
    groups: dict[float, list[float]] = collections.defaultdict(list)
    for index, point in enumerate(record.get("curve") or []):
        if not isinstance(point, list) or len(point) < 2 or not finite_number(point[0]) or not finite_number(point[1]):
            fail(f"{record.get('id')}: invalid curve point at index {index}: {point!r}")
            return []
        head, flow = float(point[0]), float(point[1])
        if head < 0 or flow < 0:
            fail(f"{record.get('id')}: negative curve point {point!r}")
            return []
        groups[head].append(flow)
    return [(head, sum(flows) / len(flows)) for head, flows in sorted(groups.items())]


def valid_curve(record: dict) -> bool:
    points = curve_groups(record)
    if len(points) < 2:
        return False
    previous = float("inf")
    for _, flow in points:
        if flow > previous * 1.02 + 5:
            return False
        previous = flow
    return True


def normalized_model(value: str) -> str:
    return re.sub(r"[^A-Z0-9]", "", (value or "").upper())


def technical_key(record: dict):
    curve = tuple(tuple(round(float(x), 6) for x in point[:2]) for point in (record.get("curve") or []))
    return (
        normalized_model(record.get("model", "")), record.get("hp"), record.get("kw"), record.get("phase"),
        str(record.get("stages", "")).strip(), str(record.get("size", "")).strip().upper(),
        record.get("normalizedCategory"), str(record.get("variantLabel", "")).strip().upper(),
        str(record.get("rpm", "")).strip(), str(record.get("insulationClass", "")).strip().upper(),
        str(record.get("motorType", "")).strip().upper(), curve,
    )


def validate_file(filename: str) -> None:
    path = ASSETS / filename
    try:
        data = json.loads(path.read_text(encoding="utf-8"))
    except Exception as exc:
        fail(f"{filename}: cannot parse JSON: {exc}")
        return
    assets[filename] = data
    records = data.get("records")
    metadata = data.get("metadata") or {}
    if not isinstance(records, list):
        fail(f"{filename}: records is not an array")
        return

    expected_total, expected_selectable = EXPECTED_COUNTS[filename]
    selectable_count = sum(bool(r.get("selectable", True)) for r in records)
    if len(records) != expected_total:
        fail(f"{filename}: expected {expected_total} records, found {len(records)}")
    if selectable_count != expected_selectable:
        fail(f"{filename}: expected {expected_selectable} selectable records, found {selectable_count}")
    for field in ("recordCount", "totalRecords"):
        if metadata.get(field) != len(records):
            fail(f"{filename}: metadata.{field}={metadata.get(field)!r}, expected {len(records)}")
    for field in ("selectableCount", "selectableRecords"):
        if metadata.get(field) != selectable_count:
            fail(f"{filename}: metadata.{field}={metadata.get(field)!r}, expected {selectable_count}")
    if metadata.get("version") != "5.9.0":
        fail(f"{filename}: metadata version must be 5.9.0")

    seen_ids: set[str] = set()
    duplicate_groups: dict[tuple, list[dict]] = collections.defaultdict(list)
    for record in records:
        rid = str(record.get("id") or "").strip()
        model = str(record.get("model") or "").strip()
        prefix = f"{filename}:{rid or model or '<unknown>'}"
        if not rid:
            fail(f"{filename}: record missing ID ({model})")
        elif rid in seen_ids:
            fail(f"{filename}: duplicate ID {rid}")
        seen_ids.add(rid)
        if not model:
            fail(f"{prefix}: missing model")
        if not str(record.get("brand") or "").strip():
            fail(f"{prefix}: missing brand")
        if not finite_number(record.get("hp")) or float(record.get("hp")) <= 0:
            fail(f"{prefix}: invalid HP {record.get('hp')!r}")
        if not finite_number(record.get("kw")) or float(record.get("kw")) <= 0:
            fail(f"{prefix}: invalid kW {record.get('kw')!r}")
        if record.get("normalizedCategory") not in ALLOWED_CATEGORIES:
            fail(f"{prefix}: invalid normalizedCategory {record.get('normalizedCategory')!r}")

        status = record.get("dataStatus") or ""
        selectable = bool(record.get("selectable", True))
        phase = record.get("phase")
        catalogue_only = status == "SOURCE_CONFIRMED_CATALOGUE_ONLY"
        if phase not in {"S", "T", "S, T"} and not (catalogue_only and phase == ""):
            fail(f"{prefix}: invalid phase {phase!r}")
        if status == "NEEDS_REVIEW":
            fail(f"{prefix}: unresolved NEEDS_REVIEW status remains")
        if status in {"SOURCE_ANOMALY", "SOURCE_CONFIRMED_CATALOGUE_ONLY"} and selectable:
            fail(f"{prefix}: {status} record must be nonselectable")
        if not selectable and status not in {"SOURCE_ANOMALY", "SOURCE_CONFIRMED_CATALOGUE_ONLY"}:
            fail(f"{prefix}: nonselectable record has unsupported status {status!r}")
        if status == "SOURCE_ANOMALY" and not str(record.get("dataNote") or "").strip():
            fail(f"{prefix}: source anomaly is missing a QA note")

        source_file = str(record.get("sourceFile") or "").strip()
        source_page = str(record.get("sourcePage") or "").strip()
        if not source_file:
            fail(f"{prefix}: missing sourceFile")
        if not source_page:
            fail(f"{prefix}: missing sourcePage")

        is_motor = record.get("normalizedCategory") == "MOTOR"
        curve = record.get("curve") or []
        if is_motor:
            if curve:
                warn(f"{prefix}: motor record unexpectedly contains a pump curve")
        else:
            curve_ok = valid_curve(record)
            if selectable and not curve_ok:
                fail(f"{prefix}: selectable curve fails shared validation")
            if status == "SOURCE_ANOMALY" and curve_ok:
                warn(f"{prefix}: source anomaly curve currently passes automatic validation; manual exclusion retained")
            if curve:
                heads = [float(p[0]) for p in curve]
                flows = [float(p[1]) for p in curve]
                expected_ranges = {
                    "minHead": min(heads), "maxHead": max(heads),
                    "minFlowLPH": min(flows), "maxFlowLPH": max(flows),
                }
                for key, expected in expected_ranges.items():
                    if not finite_number(record.get(key)) or abs(float(record[key]) - expected) > 1e-6:
                        fail(f"{prefix}: {key}={record.get(key)!r}, expected {expected}")

        hp, kw = float(record["hp"]), float(record["kw"])
        ratio = kw / hp
        if ratio < 0.45 or ratio > 1.0:
            fail(f"{prefix}: implausible kW/HP ratio {ratio:.3f} ({hp} HP, {kw} kW)")
        elif ratio < 0.60 or ratio > 0.85:
            warn(f"{prefix}: unusual kW/HP ratio {ratio:.3f}")

        duplicate_groups[technical_key(record)].append(record)

    for group in duplicate_groups.values():
        if len(group) > 1:
            ids = ", ".join(str(r.get("id")) for r in group)
            fail(f"{filename}: exact technical duplicate group remains: {ids}")

    print(f"{filename}: {len(records)} records, {selectable_count} selectable")


for filename in EXPECTED_COUNTS:
    validate_file(filename)

if not errors:
    texmo = assets["texmo_pumps.json"]["records"]
    lubi = assets["lubi_pumps.json"]["records"]
    ksb = assets["ksb_pumps.json"]["records"]

    def by_model(records, model):
        return [r for r in records if r.get("model") == model]

    # Texmo source rebuild fixtures.
    if len(by_model(texmo, "SMES90/09")) != 1 or len(by_model(texmo, "SMES90/10")) != 1:
        fail("Texmo: SMES90/09 and SMES90/10 split variants are not both present exactly once")
    page32_fixtures = {
        "CRDP03S": (50.0, [[5.0, 9000.0], [7.0, 7020.0], [10.5, 3000.0]]),
        "CDP0380S": (80.0, [[3.0, 12600.0], [4.5, 9000.0], [8.0, 1800.0]]),
        "CDP0780S": (80.0, [[4.0, 16200.0], [6.5, 10800.0], [11.0, 1800.0]]),
        "CRDP11S": (50.0, [[4.5, 16020.0], [14.0, 9000.0], [17.5, 3000.0]]),
    }
    for model, (size, curve) in page32_fixtures.items():
        rows = [r for r in by_model(texmo, model) if r.get("page") == 32]
        if len(rows) != 1 or str(int(size)) not in str(rows[0].get("size")) or rows[0].get("curve") != curve:
            fail(f"Texmo: Page 32 corrected fixture failed for {model}")

    # Lubi missing-page integration and dedup fixtures.
    page_expected = {"70": 34, "71": 27, "72": 21}
    for page, expected in page_expected.items():
        actual = sum(str(r.get("sourcePage")) == page and r.get("dataStatus") == "SOURCE_CONFIRMED" for r in lubi)
        if actual != expected:
            fail(f"Lubi: expected {expected} source-confirmed rows on page {page}, found {actual}")
    bare = {"LBM-10", "LBM-20", "LBM-30", "LBM-55"}
    bare_rows = [r for r in lubi if r.get("model") in bare]
    if {r.get("model") for r in bare_rows} != bare or any(r.get("selectable", True) for r in bare_rows):
        fail("Lubi: page 74 bare-pump catalogue-only fixtures failed")
    damaged_as = {"AS 3735", "AS 41035", "AS 41535", "AS 42035", "AS 6735", "AS 61035", "AS 61535"}
    if any(r.get("model") in damaged_as for r in lubi):
        fail("Lubi: damaged page 78 AS parser rows remain")
    for family in ("LSK-70 AF", "LSK-80 AF", "LSK-100 AF", "LSK-120 AF"):
        for record in [r for r in lubi if family in str(r.get("model"))]:
            pages = str(record.get("sourcePage") or "")
            if record.get("page") == 31 and pages.strip() == "31":
                fail(f"Lubi: unconsolidated page 30/31 duplicate remains for {record.get('model')}")

    # KSB review resolution and fail-safe anomaly handling.
    actual_anomaly_ids = {r.get("id") for r in ksb if r.get("dataStatus") == "SOURCE_ANOMALY"}
    if actual_anomaly_ids != KSB_SOURCE_ANOMALY_IDS:
        fail(f"KSB: source anomaly set mismatch. Missing={sorted(KSB_SOURCE_ANOMALY_IDS-actual_anomaly_ids)}, extra={sorted(actual_anomaly_ids-KSB_SOURCE_ANOMALY_IDS)}")
    if any(r.get("dataStatus") == "NEEDS_REVIEW" for r in ksb):
        fail("KSB: NEEDS_REVIEW rows remain")

if warnings:
    print(f"\nWarnings ({len(warnings)}):")
    for message in warnings[:50]:
        print("WARN:", message)
    if len(warnings) > 50:
        print(f"... {len(warnings)-50} more warnings")

if errors:
    print(f"\nValidation failed with {len(errors)} error(s):", file=sys.stderr)
    for message in errors[:300]:
        print("ERROR:", message, file=sys.stderr)
    sys.exit(1)

print("\nSource-aware data validation passed")
