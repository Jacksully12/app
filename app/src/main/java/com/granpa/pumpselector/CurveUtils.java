package com.granpa.pumpselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/** Shared source-curve validation used by selection, on-screen charts and share images. */
public final class CurveUtils {
    private CurveUtils() {}

    /**
     * Returns catalogue points in [head metres, flow LPH] form, sorted by flow.
     * Duplicate-head source points are retained for chart display, while validity is
     * checked using their average flow at that head (matching interpolation policy).
     */
    public static ArrayList<double[]> validatedPointsLPH(double[][] curve) {
        ArrayList<double[]> points = new ArrayList<>();
        TreeMap<Double, ArrayList<Double>> byHead = new TreeMap<>();
        if (curve == null) return points;

        for (double[] point : curve) {
            if (point == null || point.length < 2
                    || !isFinite(point[0]) || !isFinite(point[1])
                    || point[0] < 0 || point[1] < 0) {
                return new ArrayList<>();
            }
            ArrayList<Double> flowsAtHead = byHead.get(point[0]);
            if (flowsAtHead == null) {
                flowsAtHead = new ArrayList<>();
                byHead.put(point[0], flowsAtHead);
            }
            flowsAtHead.add(point[1]);
            points.add(new double[]{point[0], point[1]});
        }
        if (byHead.size() < 2) return new ArrayList<>();

        double previousAverage = Double.MAX_VALUE;
        for (Map.Entry<Double, ArrayList<Double>> entry : byHead.entrySet()) {
            double total = 0d;
            for (double flow : entry.getValue()) total += flow;
            double average = total / entry.getValue().size();
            // Allow tiny published rounding plateaus, but reject materially inverted curves.
            if (average > previousAverage * 1.02d + 5d) return new ArrayList<>();
            previousAverage = average;
        }

        Collections.sort(points, new Comparator<double[]>() {
            @Override public int compare(double[] first, double[] second) {
                return Double.compare(first[1], second[1]);
            }
        });
        return points;
    }

    public static ArrayList<double[]> validatedPoints(double[][] curve, String unit) {
        ArrayList<double[]> source = validatedPointsLPH(curve);
        ArrayList<double[]> out = new ArrayList<>();
        for (double[] point : source) {
            out.add(new double[]{point[0], PumpSelector.fromLPH(point[1], unit)});
        }
        return out;
    }

    /** Unique-head points, sorted by head, for deterministic interpolation. */
    public static ArrayList<double[]> interpolationPointsLPH(double[][] curve) {
        if (validatedPointsLPH(curve).size() < 2) return new ArrayList<>();
        TreeMap<Double, ArrayList<Double>> groups = new TreeMap<>();
        for (double[] point : curve) {
            ArrayList<Double> flowsAtHead = groups.get(point[0]);
            if (flowsAtHead == null) {
                flowsAtHead = new ArrayList<>();
                groups.put(point[0], flowsAtHead);
            }
            flowsAtHead.add(point[1]);
        }
        ArrayList<double[]> unique = new ArrayList<>();
        for (Map.Entry<Double, ArrayList<Double>> entry : groups.entrySet()) {
            double total = 0d;
            for (double flow : entry.getValue()) total += flow;
            unique.add(new double[]{entry.getKey(), total / entry.getValue().size()});
        }
        return unique;
    }

    private static boolean isFinite(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static boolean isValid(double[][] curve) {
        return validatedPointsLPH(curve).size() >= 2;
    }

    public static boolean canShare(PumpRecord record) {
        if (record == null || record.isMotor() || !record.selectable) return false;
        String status = record.dataStatus == null ? "" : record.dataStatus;
        if ("NEEDS_REVIEW".equals(status) || "SOURCE_ANOMALY".equals(status)
                || "SOURCE_CONFIRMED_CATALOGUE_ONLY".equals(status)) return false;
        return isValid(record.curve);
    }
}
