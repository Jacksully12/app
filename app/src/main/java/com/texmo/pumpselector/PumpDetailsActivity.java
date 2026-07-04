package com.texmo.pumpselector;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PumpDetailsActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String id = getIntent().getStringExtra("recordId");
        PumpRecord r = PumpRepository.findById(this, id);
        LinearLayout root = Ui.root(this);
        if (r == null) {
            root.addView(Ui.text(this, "Model not found", 22, Ui.TEXT, android.graphics.Typeface.BOLD));
            setContentView(root);
            return;
        }

        boolean hasEstimate = getIntent().getBooleanExtra("hasEstimate", false);
        double h = getIntent().getDoubleExtra("head", Double.NaN);
        double q = getIntent().getDoubleExtra("estimatedFlow", Double.NaN);

        LinearLayout top = Ui.card(this);
        LinearLayout titleRow = Ui.row(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42));
        titleRow.addView(logo, logoLp);
        TextView title = Ui.text(this, r.model, 28, Ui.TEXT, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        titleRow.addView(title, titleLp);
        top.addView(titleRow);
        top.addView(Ui.text(this, safe(r.category), 15, Ui.BLUE, android.graphics.Typeface.BOLD));
        String hpKw = trim(r.hp) + " HP  •  " + trim(r.kw) + " kW  •  " + phaseLabel(r.phase);
        top.addView(Ui.text(this, hpKw, 15, Ui.MUTED, android.graphics.Typeface.BOLD));
        if (hasEstimate && !Double.isNaN(h) && !Double.isNaN(q)) {
            top.addView(Ui.text(this, "Estimated at selected head: " + PumpSelector.formatLPH(q) + " at " + PumpSelector.formatHead(h), 16, Ui.GREEN, android.graphics.Typeface.BOLD));
        }
        root.addView(top);

        LinearLayout chartCard = Ui.card(this);
        chartCard.addView(Ui.text(this, "Performance curve", 20, Ui.TEXT, android.graphics.Typeface.BOLD));
        TextView chartNote = Ui.text(this, "One curve line per model based on catalogue curve points.", 13, Ui.MUTED, android.graphics.Typeface.NORMAL);
        Ui.addMarginBottom(this, chartNote, 8);
        chartCard.addView(chartNote);
        PerformanceCurveView chart = new PerformanceCurveView(this);
        chart.setData(r.curve, hasEstimate ? h : null, hasEstimate ? q : null);
        chartCard.addView(chart, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, 330)));
        root.addView(chartCard);

        root.addView(detailCard("Curve points used", curveRows(r, hasEstimate, h, q)));
        root.addView(detailCard("Quick specs", new String[][]{
                {"Delivery / Pipe size", safe(r.size).isEmpty() ? "-" : safe(r.size)},
                {"Page", String.valueOf(r.page)},
                {"Head range", safe(r.headRangeText) + " m"},
                {"Discharge range", safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal)},
                {"Flow range", rangeFlowLabel(r)},
                {"Brand", safe(r.brand).isEmpty() ? "-" : safe(r.brand)},
                {"Stages", safe(r.stages).isEmpty() ? "-" : safe(r.stages)},
                {"Sheet", safe(r.sheet)}
        }));

        if (r.title != null && !r.title.trim().isEmpty()) {
            LinearLayout titleCard = Ui.card(this);
            titleCard.addView(Ui.text(this, "Catalogue section", 18, Ui.TEXT, android.graphics.Typeface.BOLD));
            titleCard.addView(Ui.text(this, r.title, 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
            root.addView(titleCard);
        }

        LinearLayout actions = Ui.row(this);
        android.widget.Button copy = Ui.primaryButton(this, "Copy model number");
        actions.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        android.widget.Button back = Ui.secondaryButton(this, "Back");
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        backLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions.addView(back, backLp);
        root.addView(actions);

        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("model", r.model));
            Toast.makeText(this, "Model copied", Toast.LENGTH_SHORT).show();
        });
        back.setOnClickListener(v -> finish());

        setContentView(Ui.scroll(this, root));
    }

    private String[][] curveRows(PumpRecord r, boolean hasEstimate, double head, double flow) {
        List<String[]> rows = new ArrayList<>();
        double[][] pts = r.curve;
        if (pts != null) {
            List<double[]> valid = new ArrayList<>();
            for (double[] p : pts) if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) valid.add(p);
            int maxFlowIndex = -1, maxHeadIndex = -1;
            double bestF = Double.NEGATIVE_INFINITY, bestH = Double.NEGATIVE_INFINITY;
            for (int i = 0; i < valid.size(); i++) {
                if (valid.get(i)[1] > bestF) { bestF = valid.get(i)[1]; maxFlowIndex = i; }
                if (valid.get(i)[0] > bestH) { bestH = valid.get(i)[0]; maxHeadIndex = i; }
            }
            for (int i = 0; i < valid.size(); i++) {
                String label = i == maxFlowIndex ? "Max Flow" : i == maxHeadIndex ? "Max Head" : "BEP";
                rows.add(new String[]{label, String.format(Locale.US, "%,.0f LPH at %.1f m", valid.get(i)[1], valid.get(i)[0])});
            }
        }
        if (hasEstimate && !Double.isNaN(head) && !Double.isNaN(flow)) {
            rows.add(new String[]{"Selected point", String.format(Locale.US, "%,.0f LPH at %.1f m", flow, head)});
        }
        return rows.toArray(new String[0][]);
    }

    private LinearLayout detailCard(String title, String[][] rows) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, title, 18, Ui.TEXT, android.graphics.Typeface.BOLD));
        for (String[] row : rows) {
            LinearLayout line = Ui.row(this);
            line.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            TextView left = Ui.text(this, row[0], 13, Ui.MUTED, android.graphics.Typeface.BOLD);
            TextView right = Ui.text(this, row[1], 14, Ui.TEXT, android.graphics.Typeface.NORMAL);
            line.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            line.addView(right, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            card.addView(line);
        }
        return card;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private String trim(double v) {
        if (Double.isNaN(v)) return "-";
        if (Math.abs(v - Math.round(v)) < 0.00001) return String.format(Locale.US, "%.0f", v);
        return String.format(Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private String rangeFlowLabel(PumpRecord r) {
        if (Double.isNaN(r.minFlowLPH) || Double.isNaN(r.maxFlowLPH)) return "-";
        return PumpSelector.formatLPH(r.minFlowLPH).replace(" LPH", "") + " – " + PumpSelector.formatLPH(r.maxFlowLPH);
    }

    private String phaseLabel(String phase) {
        String p = safe(phase).toUpperCase(Locale.US);
        if (p.contains("S")) return "Single Phase";
        if (p.contains("T")) return "Three Phase";
        return p.isEmpty() ? "Phase -" : p;
    }
}
