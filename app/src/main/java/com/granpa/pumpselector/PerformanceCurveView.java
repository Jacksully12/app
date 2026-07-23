package com.granpa.pumpselector;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class PerformanceCurveView extends View {
    Paint major = new Paint(1), minor = new Paint(1), axis = new Paint(1);
    Paint text = new Paint(1), line = new Paint(1), glow = new Paint(1);
    Paint selectedDot = new Paint(1), sourceDot = new Paint(1);
    double[][] curve;
    Double selectedHead, selectedFlow;
    String unit = "LPH";
    ScaleGestureDetector scale;
    GestureDetector gestures;
    float zoom = 1, panX = 0, panY = 0, lastX, lastY;
    boolean enabled = false;
    ZoomListener listener;

    public interface ZoomListener {
        void onZoomChanged(int percent);
    }

    public PerformanceCurveView(Context context) {
        super(context);
        major.setColor(Color.rgb(205, 216, 229));
        major.setStrokeWidth(dp(1));
        minor.setColor(Color.rgb(229, 235, 243));
        minor.setStrokeWidth(dp(.7f));
        axis.setColor(Color.rgb(70, 85, 102));
        axis.setStrokeWidth(dp(1.5f));
        text.setColor(Ui.TEXT);
        text.setTextSize(sp(12));

        line.setColor(Color.rgb(0, 96, 216));
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(dp(3.2f));
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);

        glow.setColor(Color.argb(35, 0, 96, 216));
        glow.setStyle(Paint.Style.STROKE);
        glow.setStrokeWidth(dp(8));

        selectedDot.setColor(Color.rgb(255, 132, 0));
        sourceDot.setColor(Color.rgb(0, 96, 216));
        sourceDot.setStyle(Paint.Style.FILL);

        scale = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    public boolean onScale(ScaleGestureDetector detector) {
                        if (!enabled) return false;
                        float old = zoom;
                        zoom = Math.max(
                                1,
                                Math.min(4, zoom * detector.getScaleFactor())
                        );
                        float focusX = detector.getFocusX();
                        float focusY = detector.getFocusY();
                        if (zoom == 1) {
                            panX = panY = 0;
                        } else if (old != zoom) {
                            float factor = zoom / old;
                            panX = focusX - (focusX - panX) * factor;
                            panY = focusY - (focusY - panY) * factor;
                            clamp();
                        }
                        notifyZoom();
                        invalidate();
                        return true;
                    }
                }
        );

        gestures = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onDoubleTap(MotionEvent event) {
                        if (!enabled) return false;
                        if (zoom < 1.05f) {
                            zoom = 2;
                            panX = getWidth() / 2f - event.getX();
                            panY = getHeight() / 2f - event.getY();
                            clamp();
                        } else {
                            resetZoom();
                        }
                        notifyZoom();
                        invalidate();
                        return true;
                    }
                }
        );
    }

    public void setDisplayUnit(String value) {
        unit = PumpSelector.normalizeUnit(value);
        invalidate();
    }

    public void setData(double[][] value, Double head, Double flow) {
        curve = value;
        selectedHead = head;
        selectedFlow = flow;
        invalidate();
    }

    public void setPinchZoomEnabled(boolean value) {
        enabled = value;
        setClickable(value);
    }

    public void setZoomListener(ZoomListener value) {
        listener = value;
    }

    public void resetZoom() {
        zoom = 1;
        panX = panY = 0;
        notifyZoom();
        invalidate();
    }

    public void zoomIn() {
        zoom = Math.min(4, zoom * 1.25f);
        clamp();
        notifyZoom();
        invalidate();
    }

    public void zoomOut() {
        zoom = Math.max(1, zoom / 1.25f);
        if (zoom == 1) panX = panY = 0;
        clamp();
        notifyZoom();
        invalidate();
    }

    public int getZoomPercent() {
        return Math.round(zoom * 100);
    }

    void notifyZoom() {
        if (listener != null) listener.onZoomChanged(getZoomPercent());
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (!enabled) return super.onTouchEvent(event);
        gestures.onTouchEvent(event);
        scale.onTouchEvent(event);

        boolean pan = zoom > 1.02f && !scale.isInProgress();
        if ((event.getPointerCount() > 1 || pan) && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }

        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            lastX = event.getX();
            lastY = event.getY();
            return true;
        }

        if (
                event.getActionMasked() == MotionEvent.ACTION_MOVE
                && pan
                && event.getPointerCount() == 1
        ) {
            panX += event.getX() - lastX;
            panY += event.getY() - lastY;
            lastX = event.getX();
            lastY = event.getY();
            clamp();
            invalidate();
            return true;
        }

        if (
                (
                        event.getActionMasked() == MotionEvent.ACTION_UP
                        || event.getActionMasked() == MotionEvent.ACTION_CANCEL
                )
                && getParent() != null
        ) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return true;
    }

    void clamp() {
        if (zoom <= 1 || getWidth() == 0) {
            panX = panY = 0;
            return;
        }
        float maxX = (zoom - 1) * getWidth() * .5f;
        float maxY = (zoom - 1) * getHeight() * .5f;
        panX = Math.max(-maxX, Math.min(maxX, panX));
        panY = Math.max(-maxY, Math.min(maxY, panY));
    }

    protected void onMeasure(int width, int height) {
        setMeasuredDimension(
                MeasureSpec.getSize(width),
                resolveSize(dp(360), height)
        );
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        ArrayList<double[]> points = validatedSourcePoints();
        if (points.size() < 2) {
            drawUnavailable(
                    canvas,
                    curve != null && curve.length > 0
                            ? "Curve data requires source review"
                            : "Performance curve not available"
            );
            return;
        }

        boolean hasSelection = selectedHead != null
                && selectedFlow != null
                && !Double.isNaN(selectedHead)
                && !Double.isNaN(selectedFlow);

        double maximumFlow = 0;
        double maximumHead = 0;
        for (double[] point : points) {
            maximumFlow = Math.max(maximumFlow, point[1]);
            maximumHead = Math.max(maximumHead, point[0]);
        }

        if (hasSelection) {
            maximumFlow = Math.max(
                    maximumFlow,
                    PumpSelector.fromLPH(selectedFlow, unit)
            );
            maximumHead = Math.max(maximumHead, selectedHead);
        }

        double axisFlow = roundUp(
                maximumFlow * 1.10,
                niceFlowStep(maximumFlow)
        );
        double axisHead = roundUp(maximumHead * 1.12, 10);

        RectF plot = new RectF(
                dp(62),
                dp(24),
                getWidth() - dp(22),
                getHeight() - dp(78)
        );
        RectF data = new RectF(
                plot.left + dp(10),
                plot.top + dp(10),
                plot.right - dp(10),
                plot.bottom - dp(10)
        );

        canvas.save();
        if (enabled && zoom > 1) {
            canvas.translate(panX, panY);
            canvas.scale(zoom, zoom, plot.centerX(), plot.centerY());
        }

        drawGrid(
                canvas,
                plot,
                axisFlow,
                axisHead,
                hasSelection
                        ? PumpSelector.fromLPH(selectedFlow, unit)
                        : null
        );

        Path path = monotonePath(points, axisFlow, axisHead, data);
        canvas.drawPath(path, glow);
        canvas.drawPath(path, line);
        drawSourcePoints(canvas, points, axisFlow, axisHead, data);

        if (hasSelection) {
            drawSelected(
                    canvas,
                    plot,
                    data,
                    axisFlow,
                    axisHead,
                    PumpSelector.fromLPH(selectedFlow, unit),
                    selectedHead
            );
        }

        canvas.restore();

        Paint legend = new Paint(text);
        legend.setColor(Ui.MUTED);
        legend.setTextSize(sp(10));
        String sourceLegend = "● Catalogue points";
        canvas.drawText(sourceLegend, dp(64), dp(16), legend);

        if (enabled) {
            String message = zoom > 1
                    ? "Drag to move • double-tap reset"
                    : "Pinch or double-tap to zoom";
            canvas.drawText(
                    message,
                    getWidth() - legend.measureText(message) - dp(8),
                    dp(16),
                    legend
            );
        }
    }

    ArrayList<double[]> validatedSourcePoints() {
        return CurveUtils.validatedPoints(curve, unit);
    }

    void drawUnavailable(Canvas canvas, String message) {
        Paint title = new Paint(text);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextSize(sp(15));
        title.setColor(Ui.ORANGE);
        canvas.drawText(
                message,
                getWidth() / 2f - title.measureText(message) / 2f,
                getHeight() / 2f,
                title
        );
    }

    void drawSourcePoints(
            Canvas canvas,
            ArrayList<double[]> points,
            double maximumFlow,
            double maximumHead,
            RectF data
    ) {
        Paint white = new Paint(1);
        white.setColor(Color.WHITE);
        for (double[] point : points) {
            float x = x(point[1], maximumFlow, data);
            float y = y(point[0], maximumHead, data);
            canvas.drawCircle(x, y, dp(5.5f), white);
            canvas.drawCircle(x, y, dp(3.4f), sourceDot);
        }
    }

    Path monotonePath(
            ArrayList<double[]> points,
            double maximumFlow,
            double maximumHead,
            RectF data
    ) {
        int count = points.size();
        float[] x = new float[count];
        float[] y = new float[count];

        for (int index = 0; index < count; index++) {
            x[index] = x(points.get(index)[1], maximumFlow, data);
            y[index] = y(points.get(index)[0], maximumHead, data);
        }

        float[] delta = new float[count - 1];
        float[] tangent = new float[count];

        for (int index = 0; index < count - 1; index++) {
            delta[index] = (y[index + 1] - y[index])
                    / Math.max(1f, x[index + 1] - x[index]);
        }

        tangent[0] = delta[0];
        tangent[count - 1] = delta[count - 2];

        for (int index = 1; index < count - 1; index++) {
            tangent[index] = (delta[index - 1] + delta[index]) / 2f;
        }

        for (int index = 0; index < count - 1; index++) {
            if (Math.abs(delta[index]) < 1e-6) {
                tangent[index] = tangent[index + 1] = 0;
            } else {
                float a = tangent[index] / delta[index];
                float b = tangent[index + 1] / delta[index];
                float sum = a * a + b * b;
                if (sum > 9) {
                    float factor = 3f / (float) Math.sqrt(sum);
                    tangent[index] = factor * a * delta[index];
                    tangent[index + 1] = factor * b * delta[index];
                }
            }
        }

        Path path = new Path();
        path.moveTo(x[0], y[0]);

        for (int index = 0; index < count - 1; index++) {
            float width = x[index + 1] - x[index];
            path.cubicTo(
                    x[index] + width / 3f,
                    y[index] + tangent[index] * width / 3f,
                    x[index + 1] - width / 3f,
                    y[index + 1] - tangent[index + 1] * width / 3f,
                    x[index + 1],
                    y[index + 1]
            );
        }
        return path;
    }

    void drawGrid(
            Canvas canvas,
            RectF plot,
            double maximumFlow,
            double maximumHead,
            Double selected
    ) {
        Paint gridText = new Paint(text);
        gridText.setTextSize(sp(12));

        for (int index = 1; index < 20; index++) {
            float gridX = plot.left + index * plot.width() / 20f;
            float gridY = plot.bottom - index * plot.height() / 20f;
            canvas.drawLine(gridX, plot.top, gridX, plot.bottom, minor);
            canvas.drawLine(plot.left, gridY, plot.right, gridY, minor);
        }

        for (int index = 0; index <= 5; index++) {
            float gridX = plot.left + index * plot.width() / 5f;
            float gridY = plot.bottom - index * plot.height() / 5f;
            canvas.drawLine(gridX, plot.top, gridX, plot.bottom, major);
            canvas.drawLine(plot.left, gridY, plot.right, gridY, major);

            String flowLabel = PumpSelector.formatFlowNumber(
                    maximumFlow * index / 5,
                    unit
            );
            if (
                    selected == null
                    || Math.abs(maximumFlow * index / 5 - selected)
                    > maximumFlow / 18
            ) {
                canvas.drawText(
                        flowLabel,
                        gridX - gridText.measureText(flowLabel) / 2,
                        plot.bottom + dp(27),
                        gridText
                );
            }

            String headLabel = String.format(
                    Locale.US,
                    "%.0f",
                    maximumHead * index / 5
            );
            canvas.drawText(
                    headLabel,
                    plot.left - gridText.measureText(headLabel) - dp(8),
                    gridY + dp(4),
                    gridText
            );
        }

        canvas.drawLine(plot.left, plot.top, plot.left, plot.bottom, axis);
        canvas.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, axis);

        Paint label = new Paint(text);
        label.setTextSize(sp(13.5f));
        label.setTypeface(Typeface.DEFAULT_BOLD);

        String xLabel = "Flow Rate (" + PumpSelector.unitLabel(unit) + ")";
        canvas.drawText(
                xLabel,
                plot.centerX() - label.measureText(xLabel) / 2,
                getHeight() - dp(5),
                label
        );

        canvas.save();
        canvas.rotate(-90, dp(18), plot.centerY());
        canvas.drawText("Head (m)", dp(18), plot.centerY(), label);
        canvas.restore();
    }

    void drawSelected(
            Canvas canvas,
            RectF plot,
            RectF data,
            double maximumFlow,
            double maximumHead,
            double flow,
            double head
    ) {
        float selectedX = x(flow, maximumFlow, data);
        float selectedY = y(head, maximumHead, data);

        Paint dashed = new Paint(1);
        dashed.setColor(Color.rgb(255, 132, 0));
        dashed.setStrokeWidth(dp(1.2f));
        dashed.setPathEffect(
                new DashPathEffect(new float[]{8, 7}, 0)
        );

        canvas.drawLine(plot.left, selectedY, selectedX, selectedY, dashed);
        canvas.drawLine(
                selectedX,
                selectedY,
                selectedX,
                plot.bottom,
                dashed
        );

        Paint halo = new Paint(1);
        halo.setColor(Color.argb(42, 255, 132, 0));
        canvas.drawCircle(selectedX, selectedY, dp(16), halo);

        Paint white = new Paint(1);
        white.setColor(Color.WHITE);
        canvas.drawCircle(selectedX, selectedY, dp(11), white);
        canvas.drawCircle(selectedX, selectedY, dp(8), selectedDot);

        badge(
                canvas,
                String.format(Locale.US, "%.1f m", head),
                plot.left,
                selectedY,
                true
        );
        badge(
                canvas,
                PumpSelector.formatFlowNumber(flow, unit),
                selectedX,
                plot.bottom + dp(18),
                false
        );
    }

    void badge(
            Canvas canvas,
            String value,
            float x,
            float y,
            boolean left
    ) {
        Paint background = new Paint(1);
        background.setColor(Color.rgb(255, 132, 0));

        Paint label = new Paint(1);
        label.setColor(Color.WHITE);
        label.setTypeface(Typeface.DEFAULT_BOLD);
        label.setTextSize(sp(10.5f));

        float width = label.measureText(value) + dp(14);
        float height = dp(23);

        RectF rectangle = left
                ? new RectF(
                        Math.max(dp(2), x - width - dp(5)),
                        y - height / 2,
                        x - dp(5),
                        y + height / 2
                )
                : new RectF(
                        x - width / 2,
                        y - height / 2,
                        x + width / 2,
                        y + height / 2
                );

        if (!left) {
            if (rectangle.left < dp(58)) {
                rectangle.offset(dp(58) - rectangle.left, 0);
            }
            if (rectangle.right > getWidth() - dp(4)) {
                rectangle.offset(
                        getWidth() - dp(4) - rectangle.right,
                        0
                );
            }
        }

        canvas.drawRoundRect(rectangle, dp(5), dp(5), background);
        canvas.drawText(
                value,
                rectangle.centerX() - label.measureText(value) / 2,
                rectangle.centerY() + dp(4),
                label
        );
    }

    double niceFlowStep(double maximum) {
        if (unit.equals("LPM")) {
            if (maximum <= 20) return 5;
            if (maximum <= 60) return 10;
            if (maximum <= 200) return 25;
            if (maximum <= 600) return 100;
            if (maximum <= 2000) return 250;
            return 1000;
        }

        if (unit.equals("LPH")) {
            if (maximum <= 1000) return 200;
            if (maximum <= 3000) return 500;
            if (maximum <= 10000) return 1000;
            if (maximum <= 30000) return 5000;
            return 10000;
        }

        if (unit.equals("LPS")) return maximum <= 10 ? 1 : 5;
        return maximum <= 10 ? 1 : 10;
    }

    double roundUp(double value, double step) {
        return Math.ceil(value / step) * step;
    }

    float x(double flow, double maximum, RectF rectangle) {
        return (float) (
                rectangle.left + flow / maximum * rectangle.width()
        );
    }

    float y(double head, double maximum, RectF rectangle) {
        return (float) (
                rectangle.bottom - head / maximum * rectangle.height()
        );
    }

    int dp(float value) {
        return (int) (
                value * getResources().getDisplayMetrics().density + .5f
        );
    }

    float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
