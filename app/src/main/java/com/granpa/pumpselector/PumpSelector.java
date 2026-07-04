package com.granpa.pumpselector;

import java.util.*;

public class PumpSelector {
    public static class Req {
        public boolean range;
        public double min, max;
        public String unit, label, rule;
    }

    public static class Result {
        public PumpRecord r;
        public double head, flow, diff;
        public String status;
        public boolean estimate;
        public String unit = "LPH";
    }

    public static String normalizeUnit(String u) {
        if (u == null) return "LPH";
        u = u.toUpperCase(Locale.US).trim();
        if (u.equals("LPH") || u.equals("LPM") || u.equals("LPS") || u.equals("M3H")) return u;
        if (u.contains("M3") || u.contains("M³")) return "M3H";
        return "LPH";
    }

    public static double toLPH(double v, String u) {
        u = normalizeUnit(u);
        if (u.equals("LPM")) return v * 60d;
        if (u.equals("LPS")) return v * 3600d;
        if (u.equals("M3H")) return v * 1000d;
        return v;
    }

    public static double fromLPH(double v, String u) {
        u = normalizeUnit(u);
        if (u.equals("LPM")) return v / 60d;
        if (u.equals("LPS")) return v / 3600d;
        if (u.equals("M3H")) return v / 1000d;
        return v;
    }

    public static String unitLabel(String u) {
        u = normalizeUnit(u);
        if (u.equals("M3H")) return "m³/hour";
        return u;
    }

    public static String formatFlow(double lph, String unit) {
        return formatFlowNumber(fromLPH(lph, unit), unit) + " " + unitLabel(unit);
    }

    public static String formatFlowNumber(double value, String unit) {
        unit = normalizeUnit(unit);
        if (unit.equals("LPH")) return String.format(Locale.US, "%,.0f", value);
        if (unit.equals("LPM")) {
            if (Math.abs(value - Math.round(value)) < 0.05) return String.format(Locale.US, "%,.0f", value);
            return String.format(Locale.US, "%,.1f", value);
        }
        if (unit.equals("LPS")) return String.format(Locale.US, "%.2f", value);
        if (Math.abs(value - Math.round(value)) < 0.05) return String.format(Locale.US, "%,.0f", value);
        return String.format(Locale.US, "%,.2f", value);
    }

    public static String formatFlowRange(double minLPH, double maxLPH, String unit) {
        return formatFlowNumber(fromLPH(minLPH, unit), unit) + " – " + formatFlow(fromLPHToLPHForRange(maxLPH), unit);
    }

    private static double fromLPHToLPHForRange(double lph) {
        return lph;
    }

    public static Req req(boolean range, double a, double b, String unit) {
        unit = normalizeUnit(unit);
        double x = toLPH(a, unit), y = toLPH(b, unit);
        if (Double.isNaN(x) || x <= 0 || range && (Double.isNaN(y) || y <= 0)) return null;

        Req r = new Req();
        r.range = range;
        r.unit = unit;

        if (range) {
            r.min = Math.min(x, y);
            r.max = Math.max(x, y);
            r.label = "Range " + formatFlow(r.min, unit) + "–" + formatFlow(r.max, unit);
            r.rule = "estimated flow must be inside " + formatFlow(r.min, unit) + "–" + formatFlow(r.max, unit) + " at the fixed head";
        } else {
            double lower = x * 0.90d;
            double upper = x * 1.10d;
            r.min = x;
            r.max = Double.NaN;
            r.label = "Fixed flow " + formatFlow(x, unit) + " ±10%";
            r.rule = "strict rule: estimated flow must be between " + formatFlow(lower, unit) + " and " + formatFlow(upper, unit) + " at the fixed head";
        }
        return r;
    }

    public static ArrayList<Result> select(List<PumpRecord> rows, double head, Req req, String cat, String phase, String key) {
        ArrayList<Result> out = new ArrayList<>();
        if (req == null) return out;

        for (PumpRecord r : rows) {
            if (!cat(r, cat) || !phase(r.phase, phase) || !kw(r, key)) continue;
            Double q = flowAt(r, head);
            if (q == null) continue;

            Result x = new Result();
            x.r = r;
            x.head = head;
            x.flow = q;
            x.estimate = true;
            x.unit = req.unit;

            if (req.range) {
                if (q < req.min - 1e-4 || q > req.max + 1e-4) continue;
                x.diff = Math.abs(q - (req.min + req.max) / 2d);
                x.status = "Inside range";
            } else {
                double lower = req.min * 0.90d;
                double upper = req.min * 1.10d;
                if (q < lower - 1e-4 || q > upper + 1e-4) continue;

                x.diff = Math.abs(q - req.min);
                double signed = q - req.min;
                if (Math.abs(signed) < 1d) {
                    x.status = "Exact";
                } else if (signed > 0) {
                    x.status = "+" + formatFlow(signed, req.unit);
                } else {
                    x.status = "-" + formatFlow(Math.abs(signed), req.unit);
                }
            }
            out.add(x);
        }

        Collections.sort(out, (a, b) -> {
            int d = Double.compare(a.diff, b.diff);
            if (d != 0) return d;
            int hp = Double.compare(a.r.hp, b.r.hp);
            if (hp != 0) return hp;
            return a.r.model.compareToIgnoreCase(b.r.model);
        });
        return out;
    }

    public static ArrayList<Result> catalogue(List<PumpRecord> rows, String cat, String key) {
        ArrayList<Result> out = new ArrayList<>();
        for (PumpRecord r : rows) {
            if (cat(r, cat) && kw(r, key)) {
                Result x = new Result();
                x.r = r;
                x.unit = "LPH";
                out.add(x);
            }
        }
        Collections.sort(out, (a, b) -> {
            int hp = Double.compare(a.r.hp, b.r.hp);
            if (hp != 0) return hp;
            return a.r.model.compareToIgnoreCase(b.r.model);
        });
        return out;
    }

    public static Double flowAt(PumpRecord r, double head) {
        ArrayList<double[]> pts = new ArrayList<>();
        for (double[] p : r.curve) if (p != null && p.length >= 2) pts.add(new double[]{p[0], p[1]});
        Collections.sort(pts, Comparator.comparingDouble(a -> a[0]));
        if (pts.size() == 1) return Math.abs(pts.get(0)[0] - head) < 1e-4 ? pts.get(0)[1] : null;

        for (int i = 0; i < pts.size() - 1; i++) {
            double[] a = pts.get(i), b = pts.get(i + 1);
            if (head >= Math.min(a[0], b[0]) - 1e-4 && head <= Math.max(a[0], b[0]) + 1e-4) {
                if (Math.abs(b[0] - a[0]) < 1e-9) return Math.max(a[1], b[1]);
                return a[1] + (head - a[0]) / (b[0] - a[0]) * (b[1] - a[1]);
            }
        }
        return null;
    }

    public static boolean cat(PumpRecord r, String s) {
        if (s == null || s.equals("all")) return true;

        String c = (r.category == null ? "" : r.category).toLowerCase(Locale.US);
        String section = ((r.catalogueSectionText == null ? "" : r.catalogueSectionText) + " " + (r.title == null ? "" : r.title)).toLowerCase(Locale.US);

        if (s.equals("openwell_all")) {
            return c.contains("openwell") || section.contains("openwell submersible");
        }
        if (s.equals("borewell_all")) {
            return c.contains("borewell") || section.contains("borewell");
        }
        if (s.equals("dewatering_all")) {
            return c.contains("dewatering") || c.contains("sewage") || section.contains("dewatering") || section.contains("sewage");
        }
        if (s.equals("monoblock_all")) {
            // Important: Openwell Submersible Monoblocks must not be grouped as centrifugal/surface monoblock.
            if (c.contains("openwell") || section.contains("openwell submersible")) return false;
            return c.contains("monoblock") || c.contains("centrifugal") || c.contains("agricultural") || section.contains("centrifugal") || section.contains("jet pump");
        }
        if (s.equals("submersible_all")) {
            return c.contains("submersible");
        }
        return s.equals(r.category);
    }

    public static boolean phase(String p, String want) {
        if (want == null || want.equals("any")) return true;
        p = (p == null ? "" : p).toUpperCase(Locale.US);
        if (want.equals("S")) return p.contains("S");
        if (want.equals("T")) return p.contains("T") || p.contains("3");
        return true;
    }

    public static boolean kw(PumpRecord r, String k) {
        if (k == null || k.trim().isEmpty()) return true;
        String hay = (r.model + " " + r.brand + " " + r.category + " " + r.size + " " + r.catalogueSectionText).toLowerCase(Locale.US);
        return hay.contains(k.toLowerCase(Locale.US)) || norm(hay).contains(norm(k));
    }

    public static String norm(String s) {
        return s == null ? "" : s.toLowerCase(Locale.US).replaceAll("[^a-z0-9]", "");
    }

    public static String lph(double v) {
        return String.format(Locale.US, "%,.0f LPH", v);
    }

    public static String head(double v) {
        return String.format(Locale.US, "%.1f m", v);
    }

    public static String trim(double v) {
        if (Double.isNaN(v)) return "-";
        if (Math.abs(v - Math.round(v)) < 1e-5) return String.format(Locale.US, "%.0f", v);
        return String.format(Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
