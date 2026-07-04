package com.granpa.pumpselector;

import android.content.*;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class PerformanceCurveView extends View {
    Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint axis = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint curveP = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pointFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint pointRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    Paint sel = new Paint(Paint.ANTI_ALIAS_FLAG);
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
        curveP.setColor(Color.rgb(0, 96, 216));
        curveP.setStyle(Paint.Style.STROKE);
        curveP.setStrokeWidth(dp(3));
        curveP.setStrokeCap(Paint.Cap.ROUND);
        curveP.setStrokeJoin(Paint.Join.ROUND);
        pointFill.setColor(Color.WHITE);
        pointFill.setStyle(Paint.Style.FILL);
        pointRing.setColor(Color.rgb(0, 96, 216));
        pointRing.setStyle(Paint.Style.STROKE);
        pointRing.setStrokeWidth(dp(2));
        sel.setColor(Color.rgb(255, 132, 0));
        sel.setStyle(Paint.Style.FILL);
    }

    public void setData(double[][] c, Double head, Double flow) {
        curve = c;
        sh = head;
        sf = flow;
        invalidate();
    }

    @Override protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), resolveSize(dp(320), h));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ArrayList<double[]> realPts = validCurvePoints();
        if (realPts.isEmpty()) return;
        Collections.sort(realPts, Comparator.comparingDouble(a -> a[1]));

        double maxF = 0, maxH = 0;
        for (double[] p : realPts) {
            maxF = Math.max(maxF, p[1]);
            maxH = Math.max(maxH, p[0]);
        }
        if (sf != null && !Double.isNaN(sf)) maxF = Math.max(maxF, sf);
        if (sh != null && !Double.isNaN(sh)) maxH = Math.max(maxH, sh);
        if (maxF < 1) maxF = 1000;
        if (maxH < 1) maxH = 10;

        double axisMaxF = roundUp(maxF, niceFlowStep(maxF));
        double axisMaxH = roundUp(maxH * 1.08, 10);
        if (axisMaxH < 10) axisMaxH = 10;

        RectF plot = new RectF(dp(48), dp(18), getWidth() - dp(18), getHeight() - dp(50));

        drawGridAndAxes(canvas, plot, axisMaxF, axisMaxH);

        ArrayList<double[]> displayPts = displayCurvePoints(realPts, axisMaxF, maxH);
        drawSmoothCurve(canvas, displayPts, axisMaxF, axisMaxH, plot);

        // Draw only the actual catalogue points, not the artificial visual axis endpoints.
        for (double[] p : realPts) {
            float px = x(p[1], axisMaxF, plot);
            float py = y(p[0], axisMaxH, plot);
            canvas.drawCircle(px, py, dp(6), pointFill);
            canvas.drawCircle(px, py, dp(6), pointRing);
        }

        if (sh != null && sf != null && !Double.isNaN(sh) && !Double.isNaN(sf)) {
            float sx = x(sf, axisMaxF, plot);
            float sy = y(sh, axisMaxH, plot);
            Paint dashed = new Paint(Paint.ANTI_ALIAS_FLAG);
            dashed.setColor(Color.rgb(255, 132, 0));
            dashed.setStyle(Paint.Style.STROKE);
            dashed.setStrokeWidth(dp(1));
            dashed.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
            canvas.drawLine(plot.left, sy, sx, sy, dashed);
            canvas.drawLine(sx, sy, sx, plot.bottom, dashed);
            canvas.drawCircle(sx, sy, dp(9), sel);
        }
    }

    private ArrayList<double[]> validCurvePoints() {
        ArrayList<double[]> pts = new ArrayList<>();
        if (curve != null) {
            for (double[] p : curve) {
                if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) {
                    pts.add(new double[]{p[0], p[1]});
                }
            }
        }
        return pts;
    }

    private ArrayList<double[]> displayCurvePoints(ArrayList<double[]> realPts, double axisMaxF, double maxRealHead) {
        ArrayList<double[]> out = new ArrayList<>();
        // Visual-only point: make the pump curve clearly begin on the Y-axis.
        out.add(new double[]{maxRealHead, 0});
        for (double[] p : realPts) out.add(new double[]{p[0], p[1]});
        // Visual-only point: make the pump curve clearly finish on the X-axis.
        out.add(new double[]{0, axisMaxF});
        return out;
    }

    private void drawGridAndAxes(Canvas c, RectF plot, double axisMaxF, double axisMaxH) {
        int xSteps = 5;
        int ySteps = 5;
        for (int i = 0; i <= xSteps; i++) {
            float gx = plot.left + i * plot.width() / xSteps;
            c.drawLine(gx, plot.top, gx, plot.bottom, grid);
            String label = String.format(Locale.US, "%,.0f", axisMaxF * i / xSteps);
            c.drawText(label, gx - txt.measureText(label) / 2, getHeight() - dp(24), txt);
        }
        for (int i = 0; i <= ySteps; i++) {
            float gy = plot.bottom - i * plot.height() / ySteps;
            c.drawLine(plot.left, gy, plot.right, gy, grid);
            String label = String.format(Locale.US, "%.0f", axisMaxH * i / ySteps);
            c.drawText(label, plot.left - txt.measureText(label) - dp(6), gy + dp(4), txt);
        }
        c.drawLine(plot.left, plot.top, plot.left, plot.bottom, axis);
        c.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, axis);

        Paint lab = new Paint(txt);
        lab.setTextSize(sp(13));
        String xl = "Flow Rate (LPH)";
        c.drawText(xl, plot.centerX() - lab.measureText(xl) / 2, getHeight() - dp(4), lab);
        c.save();
        c.rotate(-90, dp(14), plot.centerY());
        c.drawText("Head (m)", dp(14), plot.centerY(), lab);
        c.restore();
    }

    private void drawSmoothCurve(Canvas c, ArrayList<double[]> pts, double axisMaxF, double axisMaxH, RectF plot) {
        if (pts.isEmpty()) return;
        Path path = new Path();
        for (int i = 0; i < pts.size(); i++) {
            float cx = x(pts.get(i)[1], axisMaxF, plot);
            float cy = y(pts.get(i)[0], axisMaxH, plot);
            if (i == 0) path.moveTo(cx, cy);
            else {
                float px = x(pts.get(i - 1)[1], axisMaxF, plot);
                float py = y(pts.get(i - 1)[0], axisMaxH, plot);
                float mx = (px + cx) / 2f;
                path.cubicTo(mx, py, mx, cy, cx, cy);
            }
        }
        c.drawPath(path, curveP);
    }

    private double niceFlowStep(double maxF) {
        if (maxF <= 1000) return 200;
        if (maxF <= 3000) return 500;
        if (maxF <= 10000) return 1000;
        if (maxF <= 30000) return 5000;
        return 10000;
    }

    private double roundUp(double value, double step) {
        return Math.ceil(value / step) * step;
    }

    private float x(double flow, double axisMaxF, RectF r) {
        return (float) (r.left + (flow / axisMaxF) * r.width());
    }

    private float y(double head, double axisMaxH, RectF r) {
        return (float) (r.bottom - (head / axisMaxH) * r.height());
    }

    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
    float sp(float v) { return v * getResources().getDisplayMetrics().scaledDensity; }
}
