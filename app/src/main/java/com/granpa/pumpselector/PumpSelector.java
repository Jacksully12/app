package com.granpa.pumpselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PumpSelector {
    public static class Req {
        public boolean range;
        public double min;
        public double max;
        public String label;
        public String rule;
    }

    public static class Result {
        public PumpRecord r;
        public double head;
        public double flow;
        public double dist;
        public String status;
        public boolean estimate;
    }

    public static double toLPH(double value, String unit) {
        if (Double.isNaN(value)) return Double.NaN;
        String u = unit == null ? "LPH" : unit.toUpperCase(Locale.US);
        if (u.equals("LPM")) return value * 60.0;
        if (u.equals("LPS")) return value * 3600.0;
        if (u.equals("M3H") || u.equals("M3/HOUR") || u.equals("M³/HOUR")) return value * 1000.0;
        return value;
    }

    public static Req req(boolean range, double first, double second, String unit) {
        double a = toLPH(first, unit);
        double b = toLPH(second, unit);
        if (Double.isNaN(a) || a <= 0 || (range && (Double.isNaN(b) || b <= 0))) return null;

        Req req = new Req();
        req.range = range;
        if (range) {
            req.min = Math.min(a, b);
            req.max = Math.max(a, b);
            req.label = "Flow range " + lph(req.min) + "–" + lph(req.max);
            req.rule = "estimated water must be inside " + lph(req.min) + "–" + lph(req.max);
        } else {
            req.min = a;
            req.max = Double.NaN;
            req.label = "Fixed flow " + lph(req.min);
            req.rule = "estimated water must be at least " + lph(req.min) + " at the fixed head";
        }
        return req;
    }

    public static ArrayList<Result> select(List<PumpRecord> rows, double head, Req req, String category, String phase, String keyword) {
        ArrayList<Result> out = new ArrayList<>();
        if (rows == null || req == null || Double.isNaN(head)) return out;

        for (PumpRecord r : rows) {
            if (!cat(r, category) || !phase(r.phase, phase) || !kw(r, keyword)) continue;
            Double q = flowAt(r, head);
            if (q == null) continue;

            Result result = new Result();
            result.r = r;
            result.head = head;
            result.flow = q;
            result.estimate = true;

            if (req.range) {
                if (q < req.min - 0.0001 || q > req.max + 0.0001) continue;
                result.dist = Math.abs(q - ((req.min + req.max) / 2.0));
                result.status = "Inside range";
            } else {
                if (q < req.min - 0.0001) continue;
                result.dist = q - req.min;
                result.status = result.dist < 1.0 ? "Exact" : "+" + lph(result.dist);
            }
            out.add(result);
        }

        Collections.sort(out, (a, b) -> {
            int hpCompare = Double.compare(a.r.hp, b.r.hp);
            if (hpCompare != 0) return hpCompare;
            int distCompare = Double.compare(a.dist, b.dist);
            if (distCompare != 0) return distCompare;
            return a.r.model.compareToIgnoreCase(b.r.model);
        });
        return out;
    }

    public static ArrayList<Result> catalogue(List<PumpRecord> rows, String category, String keyword) {
        ArrayList<Result> out = new ArrayList<>();
        if (rows == null) return out;
        for (PumpRecord r : rows) {
            if (cat(r, category) && kw(r, keyword)) {
                Result result = new Result();
                result.r = r;
                out.add(result);
            }
        }
        Collections.sort(out, (a, b) -> {
            int hpCompare = Double.compare(a.r.hp, b.r.hp);
            if (hpCompare != 0) return hpCompare;
            return a.r.model.compareToIgnoreCase(b.r.model);
        });
        return out;
    }

    public static Double flowAt(PumpRecord r, double head) {
        ArrayList<double[]> pts = new ArrayList<>();
        if (r == null || r.curve == null) return null;
        for (double[] p : r.curve) {
            if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) {
                pts.add(new double[]{p[0], p[1]});
            }
        }
        if (pts.isEmpty()) return null;
        Collections.sort(pts, Comparator.comparingDouble(a -> a[0]));

        if (pts.size() == 1) {
            return Math.abs(pts.get(0)[0] - head) < 0.0001 ? pts.get(0)[1] : null;
        }

        for (int i = 0; i < pts.size() - 1; i++) {
            double[] a = pts.get(i);
            double[] b = pts.get(i + 1);
            double lo = Math.min(a[0], b[0]);
            double hi = Math.max(a[0], b[0]);
            if (head >= lo - 0.0001 && head <= hi + 0.0001) {
                if (Math.abs(b[0] - a[0]) < 0.000001) return Math.max(a[1], b[1]);
                return a[1] + ((head - a[0]) / (b[0] - a[0])) * (b[1] - a[1]);
            }
        }
        return null;
    }

    public static boolean cat(PumpRecord r, String selected) {
        if (selected == null || selected.equals("all")) return true;
        String c = r.category == null ? "" : r.category.toLowerCase(Locale.US);
        if (selected.equals("monoblock_all")) return c.contains("monoblock") || c.contains("centrifugal");
        if (selected.equals("submersible_all")) return c.contains("submersible");
        if (selected.equals("borewell_all")) return c.contains("borewell");
        return selected.equals(r.category);
    }

    public static boolean phase(String phase, String wanted) {
        if (wanted == null || wanted.equals("any")) return true;
        String p = phase == null ? "" : phase.toUpperCase(Locale.US);
        if (wanted.equals("S")) return p.contains("S") || p.contains("SINGLE");
        if (wanted.equals("T")) return p.contains("T") || p.contains("THREE") || p.contains("3");
        return true;
    }

    public static boolean kw(PumpRecord r, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return true;
        String k = keyword.toLowerCase(Locale.US);
        String hay = (r.model + " " + r.category + " " + r.size + " " + r.phase + " " + r.brand + " " + r.catalogueSectionText).toLowerCase(Locale.US);
        return hay.contains(k) || norm(hay).contains(norm(k));
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
        if (Math.abs(v - Math.round(v)) < 0.00001) return String.format(Locale.US, "%.0f", v);
        return String.format(Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
