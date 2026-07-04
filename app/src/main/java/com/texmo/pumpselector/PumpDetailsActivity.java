package com.texmo.pumpselector;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, r.model, 28, Ui.TEXT, android.graphics.Typeface.BOLD));
        card.addView(Ui.text(this, safe(r.category), 15, Ui.BLUE, android.graphics.Typeface.BOLD));
        String hpKw = trim(r.hp) + " HP  •  " + trim(r.kw) + " kW  •  Phase " + safe(r.phase);
        card.addView(Ui.text(this, hpKw, 15, Ui.MUTED, android.graphics.Typeface.BOLD));
        if (getIntent().getBooleanExtra("hasEstimate", false)) {
            double h = getIntent().getDoubleExtra("head", Double.NaN);
            double q = getIntent().getDoubleExtra("estimatedFlow", Double.NaN);
            card.addView(Ui.text(this, "Strict estimate at " + PumpSelector.formatHead(h) + ": " + PumpSelector.formatLPH(q), 16, Ui.GREEN, android.graphics.Typeface.BOLD));
        }
        root.addView(card);

        root.addView(detailCard("Catalogue details", new String[][]{
                {"Brand", safe(r.brand).isEmpty() ? "-" : safe(r.brand)},
                {"Page", String.valueOf(r.page)},
                {"Head range", safe(r.headRangeText) + " m"},
                {"Discharge range", safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal)},
                {"Delivery / Pipe size", safe(r.size).isEmpty() ? "-" : safe(r.size)},
                {"Stages", safe(r.stages).isEmpty() ? "-" : safe(r.stages)},
                {"Sheet", safe(r.sheet)}
        }));

        LinearLayout curveCard = Ui.card(this);
        curveCard.addView(Ui.text(this, "Curve points used by app", 18, Ui.TEXT, android.graphics.Typeface.BOLD));
        if (r.curve != null) {
            for (double[] p : r.curve) {
                if (p != null && p.length >= 2 && !Double.isNaN(p[0]) && !Double.isNaN(p[1])) {
                    curveCard.addView(Ui.text(this, String.format(Locale.US, "%.1f m  →  %,.0f LPH", p[0], p[1]), 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
                }
            }
        }
        root.addView(curveCard);

        if (r.title != null && !r.title.trim().isEmpty()) {
            LinearLayout titleCard = Ui.card(this);
            titleCard.addView(Ui.text(this, "Catalogue section", 18, Ui.TEXT, android.graphics.Typeface.BOLD));
            titleCard.addView(Ui.text(this, r.title, 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
            root.addView(titleCard);
        }

        android.widget.Button copy = Ui.primaryButton(this, "Copy model number");
        Ui.addMarginBottom(this, copy, 12);
        root.addView(copy);
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("model", r.model));
            Toast.makeText(this, "Model copied", Toast.LENGTH_SHORT).show();
        });

        setContentView(Ui.scroll(this, root));
    }

    private LinearLayout detailCard(String title, String[][] rows) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, title, 18, Ui.TEXT, android.graphics.Typeface.BOLD));
        for (String[] row : rows) {
            LinearLayout line = Ui.row(this);
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
}
