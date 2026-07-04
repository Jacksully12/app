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
    int baseHeightDp = 430;
    int currentHeightDp = 430;
    PerformanceCurveView chart;
    TextView zoomInfo;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        rec = PumpRepository.findById(this, getIntent().getStringExtra("id"));
        has = getIntent().getBooleanExtra("estimate", false);
        head = getIntent().getDoubleExtra("head", Double.NaN);
        flow = getIntent().getDoubleExtra("flow", Double.NaN);

        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Zoom chart", 24, Ui.TEXT, 1));
        root.addView(Ui.text(this, rec != null ? rec.model : "Pump model", 18, Ui.BLUE, 1));
        root.addView(Ui.text(this, "Use the buttons below to inspect the chart more closely.", 14, Ui.MUTED, 0));

        LinearLayout toolbar = Ui.row(this);
        Button minus = Ui.secondary(this, "Zoom out");
        toolbar.addView(minus, new LinearLayout.LayoutParams(0, -2, 1));
        Button reset = Ui.blue(this, "Reset");
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, -2, 1);
        rp.setMargins(Ui.dp(this, 8), 0, Ui.dp(this, 8), 0);
        toolbar.addView(reset, rp);
        Button plus = Ui.primary(this, "Zoom in");
        toolbar.addView(plus, new LinearLayout.LayoutParams(0, -2, 1));
        Ui.mb(this, toolbar, 10);
        root.addView(toolbar);

        zoomInfo = Ui.text(this, "Zoom: 100%", 14, Ui.MUTED, 1);
        Ui.mb(this, zoomInfo, 10);
        root.addView(zoomInfo);

        LinearLayout card = Ui.card(this);
        chart = new PerformanceCurveView(this);
        if (rec != null) chart.setData(rec.curve, has ? head : null, has ? flow : null);
        card.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(this, currentHeightDp)));
        root.addView(card);

        Button close = Ui.secondary(this, "Back to details");
        root.addView(close);

        minus.setOnClickListener(v -> changeZoom(-70));
        plus.setOnClickListener(v -> changeZoom(70));
        reset.setOnClickListener(v -> { currentHeightDp = baseHeightDp; refreshChartSize(); });
        close.setOnClickListener(v -> finish());

        setContentView(Ui.scroll(this, root));
    }

    void changeZoom(int delta) {
        currentHeightDp = Math.max(320, Math.min(860, currentHeightDp + delta));
        refreshChartSize();
    }

    void refreshChartSize() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) chart.getLayoutParams();
        lp.height = Ui.dp(this, currentHeightDp);
        chart.setLayoutParams(lp);
        int pct = Math.round((currentHeightDp * 100f) / baseHeightDp);
        zoomInfo.setText("Zoom: " + pct + "%");
    }
}
