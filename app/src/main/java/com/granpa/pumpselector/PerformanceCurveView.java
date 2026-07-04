package com.granpa.pumpselector;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class PerformanceCurveView extends View {
    Paint grid = new Paint(1), axis = new Paint(1), txt = new Paint(1), curvePaint = new Paint(1), selected = new Paint(1);
    double[][] curve;
    Double sh, sf;

    public PerformanceCurveView(Context c) {
        super(c);
        grid.setColor(Color.rgb(225, 232, 241));
        grid.setStrokeWidth(dp(1));
        axis.setColor(Ui.MUTED);
        axis.setStrokeWidth(dp(1.4f));
        txt.setColor(Ui.TEXT);
        txt.setTextSize(sp(12));
        curvePaint.setColor(Color.rgb(0, 96, 216));
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(dp(3));
        curvePaint.setStrokeCap(Paint.Cap.ROUND);
        curvePaint.setStrokeJoin(Paint.Join.ROUND);
        selected.setColor(Color.rgb(255, 132, 0));
        selected.setStyle(Paint.Style.FILL);
    }

    public void setData(double[][] c, Double h, Double f) {
        curve = c;
        sh = h;
        sf = f;
        invalidate();
    }

    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), resolveSize(dp(320), h));
    }

    protected void onDraw(Canvas c) {
        super.onDraw(c);
        ArrayList<double[]> real = valid();
        if (real.isEmpty()) return;

        boolean hasSelected = sh != null && sf != null && !Double.isNaN(sh) && !Double.isNaN(sf);
        ArrayList<double[]> pts = displayPoints(real, hasSelected ? sh : null, hasSelected ? sf : null);

        double maxF = 0, maxH = 0;
        for (double[] p : pts) {
            maxF = Math.max(maxF, p[1]);
            maxH = Math.max(maxH, p[0]);
        }
        if (maxF < 1) maxF = 1000;
        if (maxH < 1) maxH = 10;

        double axisF = roundUp(maxF * 1.04, niceFlowStep(maxF));
        double axisH = roundUp(maxH * 1.08, 10);
        RectF plot = new RectF(dp(48), dp(18), getWidth() - dp(18), getHeight() - dp(50));

        drawGrid(c, plot, axisF, axisH);
        drawCurve(c, pts, axisF, axisH, plot);

        if (hasSelected) {
            float sx = x(sf, axisF, plot), sy = y(sh, axisH, plot);
            Paint dash = new Paint(1);
            dash.setColor(Color.rgb(255, 132, 0));
            dash.setStyle(Paint.Style.STROKE);
            dash.setStrokeWidth(dp(1));
            dash.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
            c.drawLine(plot.left, sy, sx, sy, dash);
            c.drawLine(sx, sy, sx, plot.bottom, dash);
            Paint halo = new Paint(1);
            halo.setColor(Color.WHITE);
            halo.setStyle(Paint.Style.FILL);
            c.drawCircle(sx, sy, dp(11), halo);
            c.drawCircle(sx, sy, dp(9), selected);
        }
    }

    ArrayList<double[]> valid() {
        ArrayList<double[]> p = new ArrayList<>();
        if (curve != null) {
            for (double[] q : curve) {
                if (q != null && q.length >= 2 && !Double.isNaN(q[0]) && !Double.isNaN(q[1])) {
                    p.add(new double[]{q[0], q[1]});
                }
            }
        }
        Collections.sort(p, Comparator.comparingDouble(a -> a[1]));
        return p;
    }

    ArrayList<double[]> displayPoints(ArrayList<double[]> real, Double selH, Double selF) {
        ArrayList<double[]> pts = new ArrayList<>();
        for (double[] p : real) pts.add(new double[]{p[0], p[1]});

        if (selH != null && selF != null && !Double.isNaN(selH) && !Double.isNaN(selF)) {
            boolean duplicate = false;
            for (double[] p : pts) {
                if (Math.abs(p[0] - selH) < 0.01 && Math.abs(p[1] - selF) < 1) duplicate = true;
            }
            if (!duplicate) pts.add(new double[]{selH, selF});
        }

        Collections.sort(pts, Comparator.comparingDouble(a -> a[1]));

        if (pts.size() >= 2) {
            double[] prev = pts.get(pts.size() - 2);
            double[] last = pts.get(pts.size() - 1);
            double df = last[1] - prev[1];
            double dh = last[0] - prev[0];
            if (df > 0 && dh < 0 && last[0] > 0) {
                double slope = dh / df;
                double zeroFlow = last[1] - last[0] / slope;
                if (zeroFlow > last[1] && zeroFlow < last[1] * 1.8) {
                    pts.add(new double[]{0, zeroFlow});
                }
            }
        }
        return pts;
    }

    void drawGrid(Canvas c, RectF p, double maxF, double maxH) {
        for (int i = 0; i <= 5; i++) {
            float gx = p.left + i * p.width() / 5f;
            c.drawLine(gx, p.top, gx, p.bottom, grid);
            String l = String.format(Locale.US, "%,.0f", maxF * i / 5);
            c.drawText(l, gx - txt.measureText(l) / 2, getHeight() - dp(24), txt);
        }
        for (int i = 0; i <= 5; i++) {
            float gy = p.bottom - i * p.height() / 5f;
            c.drawLine(p.left, gy, p.right, gy, grid);
            String l = String.format(Locale.US, "%.0f", maxH * i / 5);
            c.drawText(l, p.left - txt.measureText(l) - dp(6), gy + dp(4), txt);
        }
        c.drawLine(p.left, p.top, p.left, p.bottom, axis);
        c.drawLine(p.left, p.bottom, p.right, p.bottom, axis);
        Paint lab = new Paint(txt);
        lab.setTextSize(sp(13));
        String xl = "Flow Rate (LPH)";
        c.drawText(xl, p.centerX() - lab.measureText(xl) / 2, getHeight() - dp(4), lab);
        c.save();
        c.rotate(-90, dp(14), p.centerY());
        c.drawText("Head (m)", dp(14), p.centerY(), lab);
        c.restore();
    }

    void drawCurve(Canvas c, ArrayList<double[]> pts, double maxF, double maxH, RectF p) {
        Path path = new Path();
        for (int i = 0; i < pts.size(); i++) {
            float cx = x(pts.get(i)[1], maxF, p), cy = y(pts.get(i)[0], maxH, p);
            if (i == 0) path.moveTo(cx, cy);
            else {
                float px = x(pts.get(i - 1)[1], maxF, p), py = y(pts.get(i - 1)[0], maxH, p);
                float mx = (px + cx) / 2f;
                path.cubicTo(mx, py, mx, cy, cx, cy);
            }
        }
        c.drawPath(path, curvePaint);
    }

    double niceFlowStep(double maxF) {
        if (maxF <= 1000) return 200;
        if (maxF <= 3000) return 500;
        if (maxF <= 10000) return 1000;
        if (maxF <= 30000) return 5000;
        return 10000;
    }

    double roundUp(double v, double s) { return Math.ceil(v / s) * s; }
    float x(double f, double maxF, RectF r) { return (float) (r.left + (f / maxF) * r.width()); }
    float y(double h, double maxH, RectF r) { return (float) (r.bottom - (h / maxH) * r.height()); }
    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
    float sp(float v) { return v * getResources().getDisplayMetrics().scaledDensity; }
}
