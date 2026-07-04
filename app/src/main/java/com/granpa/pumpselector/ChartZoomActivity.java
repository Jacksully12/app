package com.granpa.pumpselector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChartZoomActivity extends Activity {
    PumpRecord rec;
    boolean has;
    double head, flow;
    String unit = "LPH";
    PerformanceCurveView chart;
    TextView zoomInfo;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        rec = PumpRepository.findById(this, getIntent().getStringExtra("id"));
        has = getIntent().getBooleanExtra("estimate", false);
        head = getIntent().getDoubleExtra("head", Double.NaN);
        flow = getIntent().getDoubleExtra("flow", Double.NaN);
        unit = PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));

        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Zoom chart", 24, Ui.TEXT, 1));
        root.addView(Ui.text(this, (rec != null ? rec.model : "Pump model") + " • " + PumpSelector.unitLabel(unit), 18, Ui.BLUE, 1));

        TextView help = Ui.text(this, "Pinch or double-tap to zoom. After zooming, drag the chart to move around.", 14, Ui.MUTED, 0);
        Ui.mb(this, help, 10);
        root.addView(help);

        LinearLayout toolbar = Ui.row(this);

        Button minus = Ui.secondary(this, "− Zoom");
        toolbar.addView(minus, new LinearLayout.LayoutParams(0, -2, 1));

        Button reset = Ui.blue(this, "Reset view");
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, -2, 1);
        rp.setMargins(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
        toolbar.addView(reset, rp);

        Button plus = Ui.primary(this, "+ Zoom");
        toolbar.addView(plus, new LinearLayout.LayoutParams(0, -2, 1));

        Ui.mb(this, toolbar, 10);
        root.addView(toolbar);

        zoomInfo = Ui.text(this, "Zoom: 100%  •  Move: drag after zooming", 14, Ui.MUTED, 1);
        Ui.mb(this, zoomInfo, 10);
        root.addView(zoomInfo);

        LinearLayout card = Ui.card(this);
        chart = new PerformanceCurveView(this);
        chart.setDisplayUnit(unit);
        if (rec != null) chart.setData(rec.curve, has ? head : null, has ? flow : null);
        chart.setPinchZoomEnabled(true);
        chart.setZoomListener(percent -> updateZoomInfo());
        card.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(this, 540)));
        root.addView(card);

        Button close = Ui.secondary(this, "Back to details");
        root.addView(close);

        minus.setOnClickListener(v -> { chart.zoomOut(); updateZoomInfo(); });
        plus.setOnClickListener(v -> { chart.zoomIn(); updateZoomInfo(); });
        reset.setOnClickListener(v -> { chart.resetZoom(); updateZoomInfo(); });
        close.setOnClickListener(v -> finish());

        setContentView(Ui.scroll(this, root));
    }

    void updateZoomInfo() {
        zoomInfo.setText("Zoom: " + chart.getZoomPercent() + "%  •  Move: drag after zooming");
    }
}
