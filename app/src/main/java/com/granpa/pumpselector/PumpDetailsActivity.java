package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PumpDetailsActivity extends Activity {
    PumpRecord rec;
    boolean hasEstimate;
    double head, flow;

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        String id = getIntent().getStringExtra("id");
        rec = PumpRepository.findById(this, id);
        hasEstimate = getIntent().getBooleanExtra("estimate", false);
        head = getIntent().getDoubleExtra("head", Double.NaN);
        flow = getIntent().getDoubleExtra("flow", Double.NaN);

        LinearLayout root = Ui.root(this);
        if (rec == null) {
            root.addView(Ui.text(this, "Model not found", 22, Ui.TEXT, 1));
            setContentView(root);
            return;
        }

        root.addView(modelHeaderCard());
        root.addView(performanceChartCard());
        root.addView(detailCard("Curve points used", curveRows()));
        root.addView(detailCard("Quick specs", quickSpecRows()));
        root.addView(catalogueSectionCard());
        root.addView(actionButtons());

        setContentView(Ui.scroll(this, root));
    }

    LinearLayout modelHeaderCard() {
        LinearLayout top = Ui.card(this);
        LinearLayout row = Ui.row(this);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));
        TextView model = Ui.text(this, safe(rec.model), 30, Ui.TEXT, 1);
        model.setPadding(Ui.dp(this, 10), 0, 0, 0);
        row.addView(model);
        top.addView(row);
        top.addView(Ui.text(this, safe(rec.category), 16, Ui.BLUE, 1));
        top.addView(Ui.text(this,
                PumpSelector.trim(rec.hp) + " HP  •  " + PumpSelector.trim(rec.kw) + " kW  •  " + phaseLabel(rec.phase),
                16, Ui.MUTED, 1));
        if (hasEstimate && !Double.isNaN(head) && !Double.isNaN(flow)) {
            top.addView(Ui.text(this,
                    "Estimated at selected head: " + PumpSelector.lph(flow) + " at " + PumpSelector.head(head),
                    17, Ui.GREEN, 1));
        }
        return top;
    }

    LinearLayout performanceChartCard() {
        LinearLayout chartCard = Ui.card(this);
        chartCard.addView(Ui.text(this, "Performance curve", 20, Ui.TEXT, 1));
        TextView note = Ui.text(this,
                "One complete end-to-end curve line for this model. The orange point is the selected operating point.",
                13, Ui.MUTED, 0);
        Ui.mb(this, note, 8);
        chartCard.addView(note);
        PerformanceCurveView chart = new PerformanceCurveView(this);
        chart.setData(rec.curve, hasEstimate ? head : null, hasEstimate ? flow : null);
        chartCard.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(this, 330)));
        return chartCard;
    }

    LinearLayout catalogueSectionCard() {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Catalogue section", 18, Ui.TEXT, 1));
        card.addView(Ui.text(this, catalogueSectionText(), 16, Ui.MUTED, 0));
        return card;
    }

    LinearLayout actionButtons() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout actions1 = Ui.row(this);
        Button whatsapp = Ui.primary(this, "Share WhatsApp");
        actions1.addView(whatsapp, new LinearLayout.LayoutParams(0, -2, 1));
        Button share = Ui.secondary(this, "Share Image");
        LinearLayout.LayoutParams shp = new LinearLayout.LayoutParams(0, -2, 1);
        shp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions1.addView(share, shp);
        Ui.mb(this, actions1, 10);
        outer.addView(actions1);

        LinearLayout actions2 = Ui.row(this);
        Button copy = Ui.secondary(this, "Copy model number");
        actions2.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        Button back = Ui.secondary(this, "Back");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions2.addView(back, bp);
        outer.addView(actions2);

        whatsapp.setOnClickListener(v -> ShareImageBuilder.shareImage(this, rec, hasEstimate, head, flow, true));
        share.setOnClickListener(v -> ShareImageBuilder.shareImage(this, rec, hasEstimate, head, flow, false));
        copy.setOnClickListener(v -> {
            ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                    .setPrimaryClip(ClipData.newPlainText("model", rec.model));
            Toast.makeText(this, "Model copied", Toast.LENGTH_SHORT).show();
        });
        back.setOnClickListener(v -> finish());
        return outer;
    }

    String[][] curveRows() {
        ArrayList<String[]> rows = new ArrayList<>();
        double[][] pts = rec.curve;
        if (pts != null) {
            int maxFlow = -1, maxHead = -1;
            double bestF = -1, bestH = -1;
            for (int i = 0; i < pts.length; i++) {
                if (pts[i] != null && pts[i].length >= 2) {
                    if (pts[i][1] > bestF) { bestF = pts[i][1]; maxFlow = i; }
                    if (pts[i][0] > bestH) { bestH = pts[i][0]; maxHead = i; }
                }
            }
            for (int i = 0; i < pts.length; i++) {
                if (pts[i] == null || pts[i].length < 2) continue;
                String label = i == maxFlow ? "Max Flow" : i == maxHead ? "Max Head" : "BEP";
                rows.add(new String[]{label, String.format(Locale.US, "%,.0f LPH at %.1f m", pts[i][1], pts[i][0])});
            }
        }
        if (hasEstimate && !Double.isNaN(head) && !Double.isNaN(flow)) {
            rows.add(new String[]{"Selected point", String.format(Locale.US, "%,.0f LPH at %.1f m", flow, head)});
        }
        return rows.toArray(new String[0][]);
    }

    String[][] quickSpecRows() {
        return new String[][]{
                {"Delivery / Pipe size", empty(rec.size) ? "-" : rec.size},
                {"Page", String.valueOf(rec.page)},
                {"Head range", safe(rec.headRangeText) + " m"},
                {"Discharge range", safe(rec.dischargeRangeText) + " " + safe(rec.flowUnitOriginal)},
                {"Flow range", flowRange(rec)},
                {"Brand", empty(rec.brand) ? "-" : rec.brand},
                {"Stages", empty(rec.stages) ? "-" : rec.stages},
                {"Sheet", "Page " + rec.page + " Layout"}
        };
    }

    LinearLayout detailCard(String title, String[][] rows) {
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, title, 18, Ui.TEXT, 1));
        for (String[] row : rows) {
            LinearLayout line = Ui.row(this);
            line.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            TextView l = Ui.text(this, row[0], 15, Ui.MUTED, 1);
            TextView r = Ui.text(this, row[1], 16, Ui.TEXT, 0);
            line.addView(l, new LinearLayout.LayoutParams(0, -2, 1));
            line.addView(r, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(line);
        }
        return card;
    }

    String catalogueSectionText() {
        String title = safe(rec.title);
        if (!title.isEmpty()) return title;
        if (!empty(rec.brand) && !empty(rec.category)) return rec.brand + " | " + rec.category;
        if (!empty(rec.category)) return rec.category;
        return "Catalogue page " + rec.page;
    }

    String safe(String s) { return s == null ? "" : s.trim(); }
    boolean empty(String s) { return safe(s).isEmpty(); }
    String phaseLabel(String p) {
        p = safe(p).toUpperCase(Locale.US);
        if (p.contains("S")) return "Single Phase";
        if (p.contains("T")) return "Three Phase";
        return p.isEmpty() ? "Phase -" : p;
    }
    String flowRange(PumpRecord r) {
        return Double.isNaN(r.minFlowLPH) || Double.isNaN(r.maxFlowLPH)
                ? "-"
                : PumpSelector.lph(r.minFlowLPH).replace(" LPH", "") + " – " + PumpSelector.lph(r.maxFlowLPH);
    }
}
