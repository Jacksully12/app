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
        public boolean header = false;
        public String groupTitle = "";
    }

    public static Result header(String title) {
        Result r = new Result();
        r.header = true;
        r.groupTitle = title;
        return r;
    }

    public static int realCount(List<Result> rows) {
        int n = 0;
        if (rows == null) return 0;
        for (Result r : rows) if (r != null && !r.header && r.r != null) n++;
        return n;
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
            r.label = "Fixed flow " + formatFlow(x, unit);
            r.rule = "Dealer smart match";
        }
        return r;
    }

    public static ArrayList<Result> selectAllMainGroups(List<PumpRecord> rows, double head, Req req, String phase, String key) {
        ArrayList<Result> out = new ArrayList<>();
        String[][] groups = new String[][]{
                {"borewell_all", "Borewell Submersible"},
                {"openwell_all", "Openwell Submersible"},
                {"monoblock_all", "Centrifugal / Surface Monoblock"},
                {"multistage_all", "Multistage Pumps"},
                {"dewatering_all", "Dewatering / Sewage"},
                {"motors_all", "Motors"}
        };

        for (String[] g : groups) {
            ArrayList<Result> part = select(rows, head, req, g[0], phase, key);
            if (!part.isEmpty()) {
                out.add(header(g[1] + " • " + realCount(part) + " models"));
                out.addAll(part);
            }
        }
        return out;
    }

    public static ArrayList<Result> select(List<PumpRecord> rows, double head, Req req, String cat, String phase, String key) {
        ArrayList<Result> out = new ArrayList<>();
        if (req == null) return out;

        ArrayList<Result> above = new ArrayList<>();
        ArrayList<Result> below = new ArrayList<>();

        for (PumpRecord r : rows) {
            if (!cat(r, cat) || !phase(r, phase) || !kw(r, key)) continue;
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
                out.add(x);
            } else {
                double signed = q - req.min;
                double pct = Math.abs(signed) / Math.max(1d, Math.abs(req.min));
                int band = toleranceBand(pct);
                if (band == 0) continue;

                x.diff = Math.abs(signed);
                String level = toleranceLabel(band);
                if (Math.abs(signed) < 1d) {
                    x.status = "Exact • " + level;
                    above.add(x);
                } else if (signed > 0) {
                    x.status = "Above target • +" + formatFlow(signed, req.unit) + " • " + level;
                    above.add(x);
                } else {
                    x.status = "Below target • -" + formatFlow(Math.abs(signed), req.unit) + " • " + level;
                    below.add(x);
                }
            }
        }

        if (!req.range) {
            Collections.sort(above, dealerBest());
            Collections.sort(below, dealerBest());

            int plus = Math.min(2, above.size());
            int minus = Math.min(2, below.size());

            for (int i = 0; i < plus; i++) out.add(above.get(i));
            for (int i = 0; i < minus; i++) out.add(below.get(i));

            return out;
        }

        Collections.sort(out, (a, b) -> {
            int hp = Double.compare(a.r.hp, b.r.hp);
            if (hp != 0) return hp;
            int kw = Double.compare(a.r.kw, b.r.kw);
            if (kw != 0) return kw;
            int d = Double.compare(a.diff, b.diff);
            if (d != 0) return d;
            return a.r.model.compareToIgnoreCase(b.r.model);
        });
        return out;
    }

    private static Comparator<Result> dealerBest() {
        return (a, b) -> {
            // Fallback bands come first: do not let a ±50% low-HP model hide a ±10% valid match.
            int band = Integer.compare(resultBand(a), resultBand(b));
            if (band != 0) return band;

            // Inside the same quality band, dealer recommendation prefers lower HP/kW.
            int hp = Double.compare(a.r.hp, b.r.hp);
            if (hp != 0) return hp;
            int kw = Double.compare(a.r.kw, b.r.kw);
            if (kw != 0) return kw;

            int d = Double.compare(a.diff, b.diff);
            if (d != 0) return d;
            return a.r.model.compareToIgnoreCase(b.r.model);
        };
    }

    private static int toleranceBand(double pct) {
        if (pct <= 0.10d + 1e-9) return 10;
        if (pct <= 0.20d + 1e-9) return 20;
        if (pct <= 0.30d + 1e-9) return 30;
        if (pct <= 0.50d + 1e-9) return 50;
        return 0;
    }

    private static String toleranceLabel(int band) {
        if (band <= 10) return "Best match ±10%";
        if (band <= 20) return "Extended match ±20%";
        if (band <= 30) return "Wide match ±30%";
        return "Last option ±50%";
    }

    private static int resultBand(Result r) {
        String s = r.status == null ? "" : r.status;
        if (s.contains("±10%")) return 10;
        if (s.contains("±20%")) return 20;
        if (s.contains("±30%")) return 30;
        return 50;
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

        if (s.equals("borewell_all")) {
            return c.contains("borewell");
        }
        if (s.equals("openwell_all")) {
            return c.contains("openwell");
        }
        if (s.equals("monoblock_all")) {
            if (c.contains("openwell")) return false;
            return c.contains("monoblock") || c.contains("jet pump") || c.contains("self priming") || c.contains("centrifugal");
        }
        if (s.equals("multistage_all")) {
            return c.contains("multistage") || c.equals("avrs");
        }
        if (s.equals("dewatering_all")) {
            return c.contains("sewage") || c.contains("dewatering");
        }
        if (s.equals("motors_all")) {
            return c.equals("motors");
        }
        return s.equals(r.category);
    }

    public static boolean phase(PumpRecord r, String want) {
        if (want == null || want.equals("any")) return true;

        // Trust the row-level phase first. Category names such as "3 Phase Horizontal Multistage"
        // must not make a row-level single-phase model appear in the three-phase filter.
        String p = r == null || r.phase == null ? "" : r.phase.toUpperCase(Locale.US).trim();
        boolean rowSingle = p.contains("S");
        boolean rowThree = p.contains("T") || p.contains("3");

        if (rowSingle || rowThree) {
            if (want.equals("S")) return rowSingle;
            if (want.equals("T")) return rowThree;
            return true;
        }

        // Fallback only when the row phase is blank or unusable.
        String c = r == null || r.category == null ? "" : r.category.toUpperCase(Locale.US);
        String section = r == null ? "" : ((r.catalogueSectionText == null ? "" : r.catalogueSectionText) + " " + (r.title == null ? "" : r.title)).toUpperCase(Locale.US);

        boolean single = c.startsWith("SP ") || section.contains("SINGLE PHASE") || section.contains("1 PHASE");
        boolean three = c.startsWith("3 PHASE") || c.equals("MOTORS") || section.contains("THREE PHASE") || section.contains("3 PHASE") || section.contains("415V") || section.contains("380 VOLTS");

        if (want.equals("S")) return single;
        if (want.equals("T")) return three;
        return true;
    }

    public static boolean phase(String p, String want) {
        if (want == null || want.equals("any")) return true;
        p = (p == null ? "" : p).toUpperCase(Locale.US).trim();
        boolean rowSingle = p.contains("S");
        boolean rowThree = p.contains("T") || p.contains("3");
        if (want.equals("S")) return rowSingle;
        if (want.equals("T")) return rowThree;
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
