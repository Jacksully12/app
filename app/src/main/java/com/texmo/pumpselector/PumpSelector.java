package com.texmo.pumpselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PumpSelector {
    public static class FlowRequirement {
        public boolean rangeMode;
        public double minLPH;
        public double maxLPH;
        public String label;
        public String ruleText;
    }

    public static class Result {
        public PumpRecord record;
        public boolean hasEstimate;
        public double matchedHead;
        public double estimatedFlowLPH;
        public double distanceLPH;
        public String status;

        public static Result fromRecord(PumpRecord r) {
            Result res = new Result();
            res.record = r;
            res.hasEstimate = false;
            return res;
        }
    }

    public static double flowToLPH(double value, String unit) {
        if (Double.isNaN(value)) return Double.NaN;
        String u = unit == null ? "LPH" : unit.toUpperCase(Locale.US);
        if (u.equals("LPM")) return value * 60.0;
        if (u.equals("LPS")) return value * 3600.0;
        if (u.equals("M3H") || u.equals("M³/HOUR") || u.equals("M3/HOUR")) return value * 1000.0;
        return value;
    }

    public static FlowRequirement requirement(boolean rangeMode, double first, double second, String unit) {
        double a = flowToLPH(first, unit);
        if (Double.isNaN(a) || a <= 0) return null;
        FlowRequirement req = new FlowRequirement();
        req.rangeMode = rangeMode;
        if (!rangeMode) {
            req.minLPH = a;
            req.maxLPH = Double.NaN;
            req.label = "Fixed flow " + formatLPH(a);
            req.ruleText = "estimated water must be at least " + formatLPH(a) + " at the fixed head";
            return req;
        }
        double b = flowToLPH(second, unit);
        if (Double.isNaN(b) || b <= 0) return null;
        req.minLPH = Math.min(a, b);
        req.maxLPH = Math.max(a, b);
        req.label = "Flow range " + formatLPH(req.minLPH) + "–" + formatLPH(req.maxLPH);
        req.ruleText = "estimated water must be inside " + formatLPH(req.minLPH) + "–" + formatLPH(req.maxLPH) + " at the fixed head";
        return req;
    }

    public static List<Result> select(List<PumpRecord> records, double head, FlowRequirement req, String category, String phase, String brand, String keyword) {
        List<Result> out = new ArrayList<>();
        if (records == null || req == null || Double.isNaN(head)) return out;
        for (PumpRecord r : records) {
            if (!categoryMatches(r, category)) continue;
            if (!phaseMatches(r.phase, phase)) continue;
            if (brand != null && !brand.equals("any") && !brand.equals(r.brand)) continue;
            if (!keywordMatches(r, keyword)) continue;
            Result result = matchAtFixedHead(r, head, req);
            if (result != null) out.add(result);
        }
        Collections.sort(out, new Comparator<Result>() {
            @Override public int compare(Result a, Result b) {
                int hp = Double.compare(safeHp(a.record), safeHp(b.record));
                if (hp != 0) return hp;
                int dist = Double.compare(a.distanceLPH, b.distanceLPH);
                if (dist != 0) return dist;
                return a.record.model.compareToIgnoreCase(b.record.model);
            }
        });
        return out;
    }

    public static List<Result> catalogue(List<PumpRecord> records, String category, String phase, String brand, String keyword) {
        List<Result> out = new ArrayList<>();
        if (records == null) return out;
        for (PumpRecord r : records) {
            if (!categoryMatches(r, category)) continue;
            if (!phaseMatches(r.phase, phase)) continue;
            if (brand != null && !brand.equals("any") && !brand.equals(r.brand)) continue;
            if (!keywordMatches(r, keyword)) continue;
            out.add(Result.fromRecord(r));
        }
        Collections.sort(out, new Comparator<Result>() {
            @Override public int compare(Result a, Result b) {
                int hp = Double.compare(safeHp(a.record), safeHp(b.record));
                if (hp != 0) return hp;
                return a.record.model.compareToIgnoreCase(b.record.model);
            }
        });
        return out;
    }

    public static Result matchAtFixedHead(PumpRecord r, double head, FlowRequirement req) {
        Double q = estimateFlowAtHead(r, head);
        if (q == null || Double.isNaN(q)) return null;
        Result out = new Result();
        out.record = r;
        out.hasEstimate = true;
        out.matchedHead = head;
        out.estimatedFlowLPH = q;
        if (!req.rangeMode) {
            if (q + 0.0001 < req.minLPH) return null;
            out.distanceLPH = Math.max(0, q - req.minLPH);
            out.status = out.distanceLPH <= 0.0001 ? "Exact" : "+" + formatLPH(out.distanceLPH);
            return out;
        }
        if (q + 0.0001 < req.minLPH || q - 0.0001 > req.maxLPH) return null;
        double target = (req.minLPH + req.maxLPH) / 2.0;
        out.distanceLPH = Math.abs(q - target);
        out.status = "Inside range";
        return out;
    }

    public static Double estimateFlowAtHead(PumpRecord r, double head) {
        if (r == null || r.curve == null || r.curve.length == 0) return null;
        List<double[]> pts = new ArrayList<>();
        for (double[] p : r.curve) {
            if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) pts.add(new double[]{p[0], p[1]});
        }
        if (pts.isEmpty()) return null;
        Collections.sort(pts, new Comparator<double[]>() {
            @Override public int compare(double[] a, double[] b) {
                return Double.compare(a[0], b[0]);
            }
        });
        if (pts.size() == 1) {
            return Math.abs(pts.get(0)[0] - head) <= 0.0001 ? pts.get(0)[1] : null;
        }
        for (int i = 0; i < pts.size() - 1; i++) {
            double[] a = pts.get(i);
            double[] b = pts.get(i + 1);
            double lo = Math.min(a[0], b[0]);
            double hi = Math.max(a[0], b[0]);
            if (head >= lo - 0.0001 && head <= hi + 0.0001) {
                if (Math.abs(b[0] - a[0]) < 0.000001) return Math.max(a[1], b[1]);
                double ratio = (head - a[0]) / (b[0] - a[0]);
                return a[1] + ratio * (b[1] - a[1]);
            }
        }
        return null;
    }

    public static boolean categoryMatches(PumpRecord r, String selected) {
        if (selected == null || selected.equals("all")) return true;
        String c = r.category == null ? "" : r.category.toLowerCase(Locale.US);
        if (selected.equals("monoblock_all")) return c.contains("monoblock") || c.contains("centrifugal");
        if (selected.equals("submersible_all")) return c.contains("submersible");
        if (selected.equals("borewell_all")) return c.contains("borewell");
        return selected.equals(r.category);
    }

    public static boolean phaseMatches(String recPhase, String wanted) {
        if (wanted == null || wanted.equals("any")) return true;
        String p = recPhase == null ? "" : recPhase.toUpperCase(Locale.US);
        if (wanted.equals("S")) return p.contains("S") || p.contains("SINGLE") || p.equals("1");
        if (wanted.equals("T")) return p.contains("T") || p.contains("THREE") || p.contains("3");
        return true;
    }

    public static boolean keywordMatches(PumpRecord r, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String k = keyword.toLowerCase(Locale.US);
        String hay = (r.model + " " + r.brand + " " + r.category + " " + r.title + " " + r.size).toLowerCase(Locale.US);
        return hay.contains(k) || normalize(hay).contains(normalize(k));
    }

    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }

    private static double safeHp(PumpRecord r) {
        return Double.isNaN(r.hp) ? 99999.0 : r.hp;
    }

    public static String formatLPH(double v) {
        return String.format(Locale.US, "%,.0f LPH", v);
    }

    public static String formatHead(double v) {
        return String.format(Locale.US, "%.1f m", v);
    }

    public static String formatNumber(double v, int decimals) {
        if (Double.isNaN(v)) return "-";
        if (decimals <= 0) return String.format(Locale.US, "%,.0f", v);
        return String.format(Locale.US, "% ,." + decimals + "f", v).trim();
    }
}
