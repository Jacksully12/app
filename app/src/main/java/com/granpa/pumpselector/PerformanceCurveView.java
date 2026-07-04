package com.granpa.pumpselector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class PerformanceCurveView extends View {
    Paint grid = new Paint(1), minorGrid = new Paint(1), axis = new Paint(1), txt = new Paint(1), curvePaint = new Paint(1), selected = new Paint(1);
    double[][] curve;
    Double sh, sf;
    String displayUnit = "LPH";

    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private float zoomFactor = 1f;
    private float panX = 0f;
    private float panY = 0f;
    private float lastTouchX = 0f;
    private float lastTouchY = 0f;
    private boolean pinchZoomEnabled = false;
    private boolean dragging = false;
    private ZoomListener zoomListener;

    public interface ZoomListener { void onZoomChanged(int percent); }

    public PerformanceCurveView(Context c) {
        super(c);

        grid.setColor(Color.rgb(194, 208, 224));
        grid.setStrokeWidth(dp(1.15f));
        grid.setStyle(Paint.Style.STROKE);
        grid.setPathEffect(new DashPathEffect(new float[]{6, 6}, 0));

        minorGrid.setColor(Color.rgb(218, 228, 239));
        minorGrid.setStrokeWidth(1f);
        minorGrid.setStyle(Paint.Style.STROKE);

        axis.setColor(Color.rgb(82, 97, 114));
        axis.setStrokeWidth(dp(1.5f));

        txt.setColor(Ui.TEXT);
        txt.setTextSize(sp(12));

        curvePaint.setColor(Color.rgb(0, 96, 216));
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(dp(3.2f));
        curvePaint.setStrokeCap(Paint.Cap.ROUND);
        curvePaint.setStrokeJoin(Paint.Join.ROUND);

        selected.setColor(Color.rgb(255, 132, 0));
        selected.setStyle(Paint.Style.FILL);

        scaleDetector = new ScaleGestureDetector(c, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (!pinchZoomEnabled) return false;

                float focusX = detector.getFocusX();
                float focusY = detector.getFocusY();
                float previous = zoomFactor;

                zoomFactor *= detector.getScaleFactor();
                zoomFactor = Math.max(1f, Math.min(4f, zoomFactor));

                if (zoomFactor == 1f) {
                    panX = 0f;
                    panY = 0f;
                } else if (previous != zoomFactor) {
                    float scaleChange = zoomFactor / previous;
                    panX = focusX - (focusX - panX) * scaleChange;
                    panY = focusY - (focusY - panY) * scaleChange;
                    clampPan();
                }

                notifyZoomChanged();
                invalidate();
                return true;
            }
        });

        gestureDetector = new GestureDetector(c, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!pinchZoomEnabled) return false;

                if (zoomFactor <= 1.05f) {
                    zoomFactor = 2f;
                    panX = getWidth() / 2f - e.getX();
                    panY = getHeight() / 2f - e.getY();
                    clampPan();
                } else {
                    resetZoom();
                }

                notifyZoomChanged();
                invalidate();
                return true;
            }
        });
    }

    public void setDisplayUnit(String unit) {
        displayUnit = PumpSelector.normalizeUnit(unit);
        invalidate();
    }

    public void setData(double[][] c, Double h, Double f) {
        curve = c;
        sh = h;
        sf = f;
        invalidate();
    }

    public void setPinchZoomEnabled(boolean enabled) {
        pinchZoomEnabled = enabled;
        setFocusable(enabled);
        setClickable(enabled);
    }

    public void setZoomListener(ZoomListener listener) { zoomListener = listener; }

    public void resetZoom() {
        zoomFactor = 1f;
        panX = 0f;
        panY = 0f;
        notifyZoomChanged();
        invalidate();
    }

    public void zoomIn() {
        zoomFactor = Math.min(4f, zoomFactor * 1.25f);
        clampPan();
        notifyZoomChanged();
        invalidate();
    }

    public void zoomOut() {
        zoomFactor = Math.max(1f, zoomFactor / 1.25f);
        if (zoomFactor == 1f) {
            panX = 0f;
            panY = 0f;
        }
        clampPan();
        notifyZoomChanged();
        invalidate();
    }

    public int getZoomPercent() { return Math.round(zoomFactor * 100f); }

    private void notifyZoomChanged() {
        if (zoomListener != null) zoomListener.onZoomChanged(getZoomPercent());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!pinchZoomEnabled) return super.onTouchEvent(event);

        gestureDetector.onTouchEvent(event);
        scaleDetector.onTouchEvent(event);

        boolean multiTouch = event.getPointerCount() > 1;
        boolean canPan = zoomFactor > 1.02f && !scaleDetector.isInProgress();

        if (multiTouch || canPan) {
            if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                dragging = canPan;
                return true;

            case MotionEvent.ACTION_MOVE:
                if (canPan && event.getPointerCount() == 1) {
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    panX += dx;
                    panY += dy;
                    clampPan();
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    dragging = true;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }

        return multiTouch || dragging || zoomFactor > 1.02f || super.onTouchEvent(event);
    }

    private void clampPan() {
        if (zoomFactor <= 1f || getWidth() == 0 || getHeight() == 0) {
            panX = 0f;
            panY = 0f;
            return;
        }

        float maxX = (zoomFactor - 1f) * getWidth() * 0.5f;
        float maxY = (zoomFactor - 1f) * getHeight() * 0.5f;
        panX = Math.max(-maxX, Math.min(maxX, panX));
        panY = Math.max(-maxY, Math.min(maxY, panY));
    }

    @Override
    protected void onMeasure(int w, int h) {
        setMeasuredDimension(MeasureSpec.getSize(w), resolveSize(dp(360), h));
    }

    @Override
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
        if (maxF < 1) maxF = 20;
        if (maxH < 1) maxH = 10;

        double axisF = roundUp(maxF * 1.04, niceFlowStep(maxF));
        double axisH = roundUp(maxH * 1.08, 10);
        RectF plot = new RectF(dp(62), dp(24), getWidth() - dp(22), getHeight() - dp(78));

        c.save();
        if (pinchZoomEnabled && zoomFactor > 1f) {
            c.translate(panX, panY);
            c.scale(zoomFactor, zoomFactor, plot.centerX(), plot.centerY());
        }

        drawGrid(c, plot, axisF, axisH, hasSelected ? PumpSelector.fromLPH(sf, displayUnit) : null);
        drawCurve(c, pts, axisF, axisH, plot);
        if (hasSelected) drawSelectedPoint(c, plot, axisF, axisH, PumpSelector.fromLPH(sf, displayUnit), sh);
        c.restore();

        if (pinchZoomEnabled) {
            Paint hint = new Paint(txt);
            hint.setColor(Color.rgb(88, 101, 119));
            hint.setTextSize(sp(11));
            String msg = zoomFactor > 1f ? "Drag to move • double-tap reset" : "Pinch or double-tap to zoom";
            c.drawText(msg, getWidth() - hint.measureText(msg) - dp(8), dp(14), hint);
        }
    }

    void drawSelectedPoint(Canvas c, RectF plot, double axisF, double axisH, double flowDisplay, double head) {
        float sx = x(flowDisplay, axisF, plot), sy = y(head, axisH, plot);

        Paint dash = new Paint(1);
        dash.setColor(Color.rgb(255, 132, 0));
        dash.setStyle(Paint.Style.STROKE);
        dash.setStrokeWidth(dp(1.25f));
        dash.setPathEffect(new DashPathEffect(new float[]{8, 7}, 0));
        c.drawLine(plot.left, sy, sx, sy, dash);
        c.drawLine(sx, sy, sx, plot.bottom, dash);

        drawHeadBadge(c, formatHead(head), plot.left, sy);
        drawFlowBadge(c, PumpSelector.formatFlowNumber(flowDisplay, displayUnit), sx, plot.bottom + dp(18));

        Paint halo = new Paint(1);
        halo.setStyle(Paint.Style.FILL);
        halo.setColor(Color.argb(42, 255, 132, 0));
        c.drawCircle(sx, sy, dp(16), halo);

        Paint ring = new Paint(1);
        ring.setStyle(Paint.Style.FILL);
        ring.setColor(Color.WHITE);
        c.drawCircle(sx, sy, dp(11), ring);
        c.drawCircle(sx, sy, dp(8), selected);
    }

    ArrayList<double[]> valid() {
        ArrayList<double[]> p = new ArrayList<>();
        if (curve != null) {
            for (double[] q : curve) {
                if (q != null && q.length >= 2 && !Double.isNaN(q[0]) && !Double.isNaN(q[1])) {
                    p.add(new double[]{q[0], PumpSelector.fromLPH(q[1], displayUnit)});
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
            double sfDisplay = PumpSelector.fromLPH(selF, displayUnit);
            boolean duplicate = false;
            for (double[] p : pts) {
                if (Math.abs(p[0] - selH) < 0.01 && Math.abs(p[1] - sfDisplay) < 0.01) duplicate = true;
            }
            if (!duplicate) pts.add(new double[]{selH, sfDisplay});
        }

        Collections.sort(pts, Comparator.comparingDouble(a -> a[1]));
        return pts;
    }

    void drawGrid(Canvas c, RectF p, double maxF, double maxH, Double selectedFlow) {
        Paint gridText = new Paint(txt);
        gridText.setColor(Color.rgb(26, 37, 52));
        gridText.setTextSize(sp(12));

        for (int i = 1; i < 20; i++) {
            if (i % 4 == 0) continue;
            float gx = p.left + i * p.width() / 20f;
            c.drawLine(gx, p.top, gx, p.bottom, minorGrid);
        }
        for (int i = 1; i < 20; i++) {
            if (i % 4 == 0) continue;
            float gy = p.bottom - i * p.height() / 20f;
            c.drawLine(p.left, gy, p.right, gy, minorGrid);
        }

        for (int i = 0; i <= 5; i++) {
            float gx = p.left + i * p.width() / 5f;
            c.drawLine(gx, p.top, gx, p.bottom, grid);
            double tick = maxF * i / 5;
            String l = PumpSelector.formatFlowNumber(tick, displayUnit);
            boolean hideNearBadge = selectedFlow != null && Math.abs(tick - selectedFlow) < maxF / 18;
            if (!hideNearBadge) c.drawText(l, gx - gridText.measureText(l) / 2, p.bottom + dp(27), gridText);
        }
        for (int i = 0; i <= 5; i++) {
            float gy = p.bottom - i * p.height() / 5f;
            c.drawLine(p.left, gy, p.right, gy, grid);
            String l = String.format(Locale.US, "%.0f", maxH * i / 5);
            c.drawText(l, p.left - gridText.measureText(l) - dp(8), gy + dp(4), gridText);
        }

        c.drawLine(p.left, p.top, p.left, p.bottom, axis);
        c.drawLine(p.left, p.bottom, p.right, p.bottom, axis);

        Paint lab = new Paint(txt);
        lab.setColor(Color.rgb(26, 37, 52));
        lab.setTextSize(sp(13.5f));
        lab.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        String xl = "Flow Rate (" + PumpSelector.unitLabel(displayUnit) + ")";
        c.drawText(xl, p.centerX() - lab.measureText(xl) / 2, getHeight() - dp(5), lab);

        c.save();
        c.rotate(-90, dp(18), p.centerY());
        c.drawText("Head (m)", dp(18), p.centerY(), lab);
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

    void drawHeadBadge(Canvas c, String label, float axisX, float centerY) {
        Paint bg = new Paint(1);
        bg.setColor(Color.rgb(255, 132, 0));
        bg.setStyle(Paint.Style.FILL);

        Paint t = new Paint(1);
        t.setColor(Color.WHITE);
        t.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        t.setTextSize(sp(11));

        float pad = dp(7);
        float w = t.measureText(label) + pad * 2;
        float h = dp(24);
        RectF r = new RectF(Math.max(dp(2), axisX - w - dp(5)), centerY - h / 2, axisX - dp(5), centerY + h / 2);
        c.drawRoundRect(r, dp(5), dp(5), bg);
        c.drawText(label, r.centerX() - t.measureText(label) / 2, r.centerY() + dp(4), t);
    }

    void drawFlowBadge(Canvas c, String label, float centerX, float centerY) {
        Paint bg = new Paint(1);
        bg.setColor(Color.rgb(255, 132, 0));
        bg.setStyle(Paint.Style.FILL);

        Paint t = new Paint(1);
        t.setColor(Color.WHITE);
        t.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        t.setTextSize(sp(11));

        float pad = dp(8);
        float w = Math.min(t.measureText(label) + pad * 2, dp(92));
        float h = dp(24);
        RectF r = new RectF(centerX - w / 2, centerY - h / 2, centerX + w / 2, centerY + h / 2);
        if (r.left < dp(58)) r.offset(dp(58) - r.left, 0);
        if (r.right > getWidth() - dp(4)) r.offset(getWidth() - dp(4) - r.right, 0);
        c.drawRoundRect(r, dp(5), dp(5), bg);
        drawCenteredFit(c, label, r, t);
    }

    void drawCenteredFit(Canvas c, String label, RectF r, Paint p) {
        float old = p.getTextSize();
        while (p.measureText(label) > r.width() - dp(8) && p.getTextSize() > sp(8)) p.setTextSize(p.getTextSize() - 1);
        c.drawText(label, r.centerX() - p.measureText(label) / 2, r.centerY() + dp(4), p);
        p.setTextSize(old);
    }

    String formatHead(double v) {
        return Math.abs(v - Math.round(v)) < 0.05 ? String.format(Locale.US, "%.0f", v) : String.format(Locale.US, "%.1f", v);
    }

    double niceFlowStep(double maxF) {
        if (displayUnit.equals("LPM")) {
            if (maxF <= 20) return 5;
            if (maxF <= 60) return 10;
            if (maxF <= 200) return 25;
            if (maxF <= 600) return 100;
            if (maxF <= 2000) return 250;
            return 1000;
        }
        if (displayUnit.equals("LPH")) {
            if (maxF <= 1000) return 200;
            if (maxF <= 3000) return 500;
            if (maxF <= 10000) return 1000;
            if (maxF <= 30000) return 5000;
            return 10000;
        }
        if (displayUnit.equals("LPS")) return maxF <= 10 ? 1 : 5;
        return maxF <= 10 ? 1 : 10;
    }

    double roundUp(double v, double s) { return Math.ceil(v / s) * s; }
    float x(double f, double maxF, RectF r) { return (float) (r.left + (f / maxF) * r.width()); }
    float y(double h, double maxH, RectF r) { return (float) (r.bottom - (h / maxH) * r.height()); }
    int dp(float v) { return (int) (v * getResources().getDisplayMetrics().density + .5f); }
    float sp(float v) { return v * getResources().getDisplayMetrics().scaledDensity; }
}
