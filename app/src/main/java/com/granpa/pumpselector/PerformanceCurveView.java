package com.granpa.pumpselector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class PerformanceCurveView extends View {
    private final Paint grid = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axis = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint curvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selected = new Paint(Paint.ANTI_ALIAS_FLAG);
    private double[][] curve;
    private Double selectedHead;
    private Double selectedFlow;

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
        pointFill.setColor(Color.WHITE);
        pointRing.setColor(Color.rgb(0, 96, 216));
        pointRing.setStyle(Paint.Style.STROKE);
        pointRing.setStrokeWidth(dp(2));
        selected.setColor(Color.rgb(255, 132, 0));
    }

    public void setData(double[][] curve, Double head, Double flow) {
        this.curve = curve;
        this.selectedHead = head;
        this.selectedFlow = flow;
        invalidate();
    }

    @Override
    protected void onMeasure(int width, int height) {
        setMeasuredDimension(MeasureSpec.getSize(width), resolveSize(dp(320), height));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ArrayList<double[]> real = realPoints();
        if (real.isEmpty()) return;
        Collections.sort(real, Comparator.comparingDouble(a -> a[1]));

        double maxFlow = 0;
        double maxHead = 0;
        for (double[] p : real) {
            maxFlow = Math.max(maxFlow, p[1]);
            maxHead = Math.max(maxHead, p[0]);
        }
        if (selectedFlow != null && !Double.isNaN(selectedFlow)) maxFlow = Math.max(maxFlow, selectedFlow);
        if (selectedHead != null && !Double.isNaN(selectedHead)) maxHead = Math.max(maxHead, selectedHead);

        double axisFlow = roundUp(maxFlow, niceStep(maxFlow));
        double axisHead = roundUp(Math.max(10, maxHead * 1.08), 10);
        RectF plot = new RectF(dp(48), dp(18), getWidth() - dp(18), getHeight() - dp(50));
        drawGrid(canvas, plot, axisFlow, axisHead);

        ArrayList<double[]> display = new ArrayList<>();
        display.add(new double[]{maxHead, 0});
        display.addAll(real);
        display.add(new double[]{0, axisFlow});
        drawCurve(canvas, display, axisFlow, axisHead, plot);

        for (double[] p : real) {
            float x = x(p[1], axisFlow, plot);
            float y = y(p[0], axisHead, plot);
            canvas.drawCircle(x, y, dp(6), pointFill);
            canvas.drawCircle(x, y, dp(6), pointRing);
        }

        if (selectedHead != null && selectedFlow != null && !Double.isNaN(selectedHead) && !Double.isNaN(selectedFlow)) {
            float sx = x(selectedFlow, axisFlow, plot);
            float sy = y(selectedHead, axisHead, plot);
            Paint dash = new Paint(Paint.ANTI_ALIAS_FLAG);
            dash.setColor(Color.rgb(255, 132, 0));
            dash.setStyle(Paint.Style.STROKE);
            dash.setStrokeWidth(dp(1));
            dash.setPathEffect(new DashPathEffect(new float[]{8, 8}, 0));
            canvas.drawLine(plot.left, sy, sx, sy, dash);
            canvas.drawLine(sx, sy, sx, plot.bottom, dash);
            canvas.drawCircle(sx, sy, dp(9), selected);
        }
    }

    private ArrayList<double[]> realPoints() {
        ArrayList<double[]> out = new ArrayList<>();
        if (curve != null) {
            for (double[] p : curve) {
                if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) out.add(new double[]{p[0], p[1]});
            }
        }
        return out;
    }

    private void drawGrid(Canvas c, RectF plot, double axisFlow, double axisHead) {
        for (int i = 0; i <= 5; i++) {
            float gx = plot.left + i * plot.width() / 5f;
            c.drawLine(gx, plot.top, gx, plot.bottom, grid);
            String label = String.format(Locale.US, "%,.0f", axisFlow * i / 5);
            c.drawText(label, gx - txt.measureText(label) / 2f, getHeight() - dp(24), txt);
        }
        for (int i = 0; i <= 5; i++) {
            float gy = plot.bottom - i * plot.height() / 5f;
            c.drawLine(plot.left, gy, plot.right, gy, grid);
            String label = String.format(Locale.US, "%.0f", axisHead * i / 5);
            c.drawText(label, plot.left - txt.measureText(label) - dp(6), gy + dp(4), txt);
        }
        c.drawLine(plot.left, plot.top, plot.left, plot.bottom, axis);
        c.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, axis);

        Paint lab = new Paint(txt);
        lab.setTextSize(sp(13));
        String xl = "Flow Rate (LPH)";
        c.drawText(xl, plot.centerX() - lab.measureText(xl) / 2f, getHeight() - dp(4), lab);
        c.save();
        c.rotate(-90, dp(14), plot.centerY());
        c.drawText("Head (m)", dp(14), plot.centerY(), lab);
        c.restore();
    }

    private void drawCurve(Canvas c, ArrayList<double[]> points, double axisFlow, double axisHead, RectF plot) {
        if (points.isEmpty()) return;
        Path path = new Path();
        for (int i = 0; i < points.size(); i++) {
            float cx = x(points.get(i)[1], axisFlow, plot);
            float cy = y(points.get(i)[0], axisHead, plot);
            if (i == 0) path.moveTo(cx, cy);
            else {
                float px = x(points.get(i - 1)[1], axisFlow, plot);
                float py = y(points.get(i - 1)[0], axisHead, plot);
                float mx = (px + cx) / 2f;
                path.cubicTo(mx, py, mx, cy, cx, cy);
            }
        }
        c.drawPath(path, curvePaint);
    }

    private double niceStep(double maxFlow) {
        if (maxFlow <= 1000) return 200;
        if (maxFlow <= 3000) return 500;
        if (maxFlow <= 10000) return 1000;
        if (maxFlow <= 30000) return 5000;
        return 10000;
    }

    private double roundUp(double value, double step) {
        return Math.ceil(value / step) * step;
    }

    private float x(double flow, double axisFlow, RectF plot) {
        return (float) (plot.left + (flow / axisFlow) * plot.width());
    }

    private float y(double head, double axisHead, RectF plot) {
        return (float) (plot.bottom - (head / axisHead) * plot.height());
    }

    private int dp(float v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float sp(float v) {
        return v * getResources().getDisplayMetrics().scaledDensity;
    }
}
