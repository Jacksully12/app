package com.texmo.pumpselector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PerformanceCurveView extends View {
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint curvePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint selectedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bubbleTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final List<double[]> points = new ArrayList<>(); // [head, flowLPH]
    private Double selectedHead;
    private Double selectedFlow;

    public PerformanceCurveView(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        gridPaint.setColor(Color.rgb(227, 233, 241));
        gridPaint.setStrokeWidth(dp(1));

        axisPaint.setColor(Ui.MUTED);
        axisPaint.setStrokeWidth(dp(1.5f));

        labelPaint.setColor(Ui.TEXT);
        labelPaint.setTextSize(sp(12));

        curvePaint.setColor(Color.rgb(34, 99, 225));
        curvePaint.setStyle(Paint.Style.STROKE);
        curvePaint.setStrokeWidth(dp(3));

        pointPaint.setColor(Color.WHITE);
        pointPaint.setStyle(Paint.Style.FILL);

        selectedPaint.setColor(Color.rgb(34, 99, 225));
        selectedPaint.setStyle(Paint.Style.FILL);

        bubblePaint.setColor(Color.WHITE);
        bubblePaint.setStyle(Paint.Style.FILL);
        bubbleBorderPaint.setColor(Color.rgb(34, 99, 225));
        bubbleBorderPaint.setStyle(Paint.Style.STROKE);
        bubbleBorderPaint.setStrokeWidth(dp(1.2f));
        bubbleTextPaint.setColor(Ui.TEXT);
        bubbleTextPaint.setTextSize(sp(12));
    }

    public void setData(double[][] curve, Double selectedHead, Double selectedFlow) {
        points.clear();
        if (curve != null) {
            for (double[] p : curve) {
                if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) {
                    points.add(new double[]{p[0], p[1]});
                }
            }
        }
        Collections.sort(points, Comparator.comparingDouble(a -> a[1]));
        this.selectedHead = selectedHead;
        this.selectedFlow = selectedFlow;
        invalidate();
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int desiredHeight = dp(320);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = desiredHeight;
        if (heightMode == MeasureSpec.EXACTLY) height = MeasureSpec.getSize(heightMeasureSpec);
        else if (heightMode == MeasureSpec.AT_MOST) height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
        setMeasuredDimension(width, height);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float left = dp(48), top = dp(20), right = getWidth() - dp(18), bottom = getHeight() - dp(42);
        RectF plot = new RectF(left, top, right, bottom);

        double maxFlow = 0, maxHead = 0;
        for (double[] p : points) { maxFlow = Math.max(maxFlow, p[1]); maxHead = Math.max(maxHead, p[0]); }
        if (selectedFlow != null && !Double.isNaN(selectedFlow)) maxFlow = Math.max(maxFlow, selectedFlow);
        if (selectedHead != null && !Double.isNaN(selectedHead)) maxHead = Math.max(maxHead, selectedHead);
        if (maxFlow <= 0) maxFlow = 1000;
        if (maxHead <= 0) maxHead = 10;
        maxFlow = roundUp(maxFlow, 1000);
        maxHead = roundUp(maxHead, 10);

        int xSteps = 7;
        int ySteps = (int) Math.max(5, maxHead / 10);
        ySteps = Math.min(ySteps, 8);

        for (int i = 0; i <= xSteps; i++) {
            float x = plot.left + i * plot.width() / xSteps;
            canvas.drawLine(x, plot.top, x, plot.bottom, gridPaint);
            String txt = String.format(Locale.US, "%,.0f", maxFlow * i / xSteps);
            float tw = labelPaint.measureText(txt);
            canvas.drawText(txt, x - tw / 2f, getHeight() - dp(16), labelPaint);
        }
        for (int i = 0; i <= ySteps; i++) {
            float y = plot.bottom - i * plot.height() / ySteps;
            canvas.drawLine(plot.left, y, plot.right, y, gridPaint);
            String txt = String.format(Locale.US, "%.0f", maxHead * i / ySteps);
            float tw = labelPaint.measureText(txt);
            canvas.drawText(txt, plot.left - tw - dp(8), y + dp(4), labelPaint);
        }

        canvas.drawLine(plot.left, plot.top, plot.left, plot.bottom, axisPaint);
        canvas.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, axisPaint);

        Paint axisLabel = new Paint(labelPaint);
        axisLabel.setTextSize(sp(13));
        axisLabel.setColor(Ui.TEXT);
        String xLabel = "Flow (LPH)";
        float xTw = axisLabel.measureText(xLabel);
        canvas.drawText(xLabel, plot.centerX() - xTw / 2f, getHeight() - dp(2), axisLabel);
        canvas.save();
        canvas.rotate(-90, dp(14), plot.centerY());
        canvas.drawText("Head (m)", dp(14), plot.centerY(), axisLabel);
        canvas.restore();

        if (points.size() >= 2) {
            Path path = new Path();
            float startX = xFor(points.get(0)[1], maxFlow, plot);
            float startY = yFor(points.get(0)[0], maxHead, plot);
            path.moveTo(startX, startY);
            for (int i = 1; i < points.size(); i++) {
                float px = xFor(points.get(i - 1)[1], maxFlow, plot);
                float py = yFor(points.get(i - 1)[0], maxHead, plot);
                float cx = xFor(points.get(i)[1], maxFlow, plot);
                float cy = yFor(points.get(i)[0], maxHead, plot);
                float mx = (px + cx) / 2f;
                path.quadTo(px, py, mx, (py + cy) / 2f);
                if (i == points.size() - 1) path.quadTo(cx, cy, cx, cy);
            }
            canvas.drawPath(path, curvePaint);
        }

        int maxHeadIndex = -1, maxFlowIndex = -1;
        double headBest = Double.NEGATIVE_INFINITY, flowBest = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i)[0] > headBest) { headBest = points.get(i)[0]; maxHeadIndex = i; }
            if (points.get(i)[1] > flowBest) { flowBest = points.get(i)[1]; maxFlowIndex = i; }
        }

        for (int i = 0; i < points.size(); i++) {
            double[] p = points.get(i);
            float x = xFor(p[1], maxFlow, plot);
            float y = yFor(p[0], maxHead, plot);
            canvas.drawCircle(x, y, dp(6), pointPaint);
            canvas.drawCircle(x, y, dp(6), bubbleBorderPaint);
            String title;
            if (i == maxHeadIndex) title = "Max Head";
            else if (i == maxFlowIndex) title = "Max Flow";
            else title = "BEP";
            drawPointLabel(canvas, x, y, title, String.format(Locale.US, "%,.0f LPH, %.0f m", p[1], p[0]), plot, i == maxFlowIndex);
        }

        if (selectedHead != null && selectedFlow != null && !Double.isNaN(selectedHead) && !Double.isNaN(selectedFlow)) {
            float sx = xFor(selectedFlow, maxFlow, plot);
            float sy = yFor(selectedHead, maxHead, plot);
            canvas.drawCircle(sx, sy, dp(9), selectedPaint);
            canvas.drawCircle(sx, sy, dp(12), bubbleBorderPaint);
            drawSelectedBubble(canvas, sx, sy, String.format(Locale.US, "Selected point\n%.0f m, %,.0f LPH", selectedHead, selectedFlow), plot);
        }
    }

    private void drawPointLabel(Canvas canvas, float x, float y, String title, String detail, RectF plot, boolean rightSide) {
        Paint titlePaint = new Paint(bubbleTextPaint);
        titlePaint.setColor(Color.rgb(34, 99, 225));
        titlePaint.setFakeBoldText(true);
        float tx = x + dp(rightSide ? 10 : 8);
        float ty = y - dp(10);
        if (!rightSide && tx + dp(120) > plot.right) tx = x - dp(92);
        if (rightSide && tx + dp(120) > plot.right) tx = x - dp(92);
        canvas.drawText(title, tx, ty, titlePaint);
        canvas.drawText(detail, tx, ty + dp(16), bubbleTextPaint);
    }

    private void drawSelectedBubble(Canvas canvas, float x, float y, String text, RectF plot) {
        String[] lines = text.split("\\n");
        float pad = dp(10);
        float width = 0;
        for (String line : lines) width = Math.max(width, bubbleTextPaint.measureText(line));
        width += pad * 2;
        float lineH = dp(17);
        float height = pad * 2 + lineH * lines.length;
        float left = x + dp(8);
        float top = y + dp(8);
        if (left + width > plot.right) left = x - width - dp(8);
        if (top + height > plot.bottom) top = y - height - dp(8);
        RectF box = new RectF(left, top, left + width, top + height);
        canvas.drawRoundRect(box, dp(10), dp(10), bubblePaint);
        canvas.drawRoundRect(box, dp(10), dp(10), bubbleBorderPaint);
        Paint titlePaint = new Paint(bubbleTextPaint);
        titlePaint.setColor(Color.rgb(34, 99, 225));
        titlePaint.setFakeBoldText(true);
        for (int i = 0; i < lines.length; i++) {
            Paint p = i == 0 ? titlePaint : bubbleTextPaint;
            canvas.drawText(lines[i], box.left + pad, box.top + pad + lineH * (i + 1) - dp(4), p);
        }
    }

    private float xFor(double flow, double maxFlow, RectF plot) {
        return (float) (plot.left + (flow / maxFlow) * plot.width());
    }

    private float yFor(double head, double maxHead, RectF plot) {
        return (float) (plot.bottom - (head / maxHead) * plot.height());
    }

    private double roundUp(double value, double step) {
        return Math.ceil(value / step) * step;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
