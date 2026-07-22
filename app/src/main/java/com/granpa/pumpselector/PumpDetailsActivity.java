package com.granpa.pumpselector;

import android.app.Activity;
import android.content.ClipData;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class PumpDetailsActivity extends Activity {
    PumpRecord rec;
    boolean has;
    double head, flow;
    String unit = "LPH";
    String asset = PumpRepository.TEXMO_ASSET;
    String brand = "TEXMO";

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        asset = PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));
        brand = getIntent().getStringExtra("brand");
        if (brand == null || brand.trim().isEmpty()) brand = PumpRepository.brandName(asset);
        rec = PumpRepository.findById(this, asset, getIntent().getStringExtra("id"));
        has = getIntent().getBooleanExtra("estimate", false);
        head = getIntent().getDoubleExtra("head", Double.NaN);
        flow = getIntent().getDoubleExtra("flow", Double.NaN);
        unit = PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));

        LinearLayout root = Ui.root(this);
        if (rec == null) {
            root.addView(Ui.text(this, "Model not found", 22, Ui.TEXT, 1));
            setContentView(root);
            return;
        }

        root.addView(header());
        if (rec.isMotor()) {
            root.addView(detailCard("Motor specifications", motorRows()));
            root.addView(catalogueCard());
            root.addView(motorActions());
        } else {
            root.addView(chartCard());
            root.addView(detailCard("Curve points used", curveRows()));
            root.addView(detailCard("Quick specs", quickRows()));
            String[][] technical = technicalRows();
            if (technical.length > 0) root.addView(detailCard("Technical specifications", technical));
            root.addView(catalogueCard());
            root.addView(actions());
        }
        setContentView(Ui.scroll(this, root));
    }

    LinearLayout header() {
        LinearLayout top = Ui.card(this);
        LinearLayout row = Ui.row(this);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));

        TextView model = Ui.text(this, rec.model, 30, Ui.TEXT, 1);
        model.setPadding(Ui.dp(this, 10), 0, 0, 0);
        row.addView(model);
        top.addView(row);

        top.addView(Ui.text(this, rec.category, 16, Ui.BLUE, 1));
        top.addView(Ui.text(this, PumpSelector.trim(rec.hp) + " HP  •  " + PumpSelector.trim(rec.kw) + " kW  •  " + phaseLabel(rec.phase), 16, Ui.MUTED, 1));

        if (has && !Double.isNaN(head) && !Double.isNaN(flow)) {
            top.addView(Ui.text(this, "Estimated at selected head: " + PumpSelector.formatFlow(flow, unit) + " at " + PumpSelector.head(head), 17, Ui.GREEN, 1));
        }
        return top;
    }

    LinearLayout chartCard() {
        LinearLayout c = Ui.card(this);
        c.addView(Ui.text(this, "Performance curve", 20, Ui.TEXT, 1));

        TextView n = Ui.text(this, "Curve through catalogue points. Orange point is the selected duty point. Flow is shown in " + PumpSelector.unitLabel(unit) + ".", 13, Ui.MUTED, 0);
        Ui.mb(this, n, 8);
        c.addView(n);

        PerformanceCurveView chart = new PerformanceCurveView(this);
        chart.setDisplayUnit(unit);
        chart.setData(rec.curve, has ? head : null, has ? flow : null);
        chart.setOnClickListener(v -> openZoomScreen());
        chart.setContentDescription("Performance curve. Tap to open zoom view.");
        c.addView(chart, new LinearLayout.LayoutParams(-1, Ui.dp(this, 330)));

        TextView tapHint = Ui.text(this, "Tap chart for pinch zoom, double-tap zoom, and drag-to-move view.", 12, Ui.BLUE, 1);
        Ui.mb(this, tapHint, 8);
        c.addView(tapHint);

        Button zoom = Ui.secondary(this, "Open closer / Pinch + move");
        LinearLayout.LayoutParams zp = new LinearLayout.LayoutParams(-1, -2);
        zp.topMargin = Ui.dp(this, 4);
        c.addView(zoom, zp);
        zoom.setOnClickListener(v -> openZoomScreen());

        return c;
    }

    void openZoomScreen() {
        android.content.Intent i = new android.content.Intent(this, ChartZoomActivity.class);
        i.putExtra("id", rec.id);
        i.putExtra("estimate", has);
        i.putExtra("head", head);
        i.putExtra("flow", flow);
        i.putExtra("unit", unit);
        i.putExtra("asset", asset);
        i.putExtra("brand", brand);
        startActivity(i);
    }

    LinearLayout catalogueCard() {
        LinearLayout c = Ui.card(this);
        c.addView(Ui.text(this, "Catalogue section", 18, Ui.TEXT, 1));

        TextView t = Ui.text(this, catalogueText(), 14, Ui.MUTED, 0);
        t.setLineSpacing(Ui.dp(this, 2), 1f);
        t.setTextIsSelectable(true);
        c.addView(t);

        return c;
    }

    LinearLayout actions() {
        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = Ui.row(this);
        Button wa = Ui.primary(this, "Share WhatsApp");
        row.addView(wa, new LinearLayout.LayoutParams(0, -2, 1));

        Button dl = Ui.blue(this, "Download Image");
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(0, -2, 1);
        dp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        row.addView(dl, dp);

        Ui.mb(this, row, 10);
        outer.addView(row);

        LinearLayout row2 = Ui.row(this);
        Button copy = Ui.secondary(this, "Copy model number");
        row2.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        Button back = Ui.secondary(this, "Back");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        row2.addView(back, bp);
        outer.addView(row2);

        wa.setOnClickListener(v -> ShareImageBuilder.shareWhatsApp(this, rec, has, head, flow, unit));
        dl.setOnClickListener(v -> ShareImageBuilder.download(this, rec, has, head, flow, unit));
        copy.setOnClickListener(v -> {
            ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("model", rec.model));
            Toast.makeText(this, "Model copied", Toast.LENGTH_SHORT).show();
        });
        back.setOnClickListener(v -> finish());

        return outer;
    }

    String[][] curveRows() {
        ArrayList<String[]> rows = new ArrayList<>();
        double[][] pts = rec.curve;
        int maxFlow = -1, maxHead = -1;
        double bestF = -1, bestH = -1;

        if (pts != null) {
            for (int i = 0; i < pts.length; i++) if (pts[i] != null && pts[i].length >= 2) {
                if (pts[i][1] > bestF) { bestF = pts[i][1]; maxFlow = i; }
                if (pts[i][0] > bestH) { bestH = pts[i][0]; maxHead = i; }
            }

            for (int i = 0; i < pts.length; i++) if (pts[i] != null && pts[i].length >= 2) {
                String label = i == maxFlow ? "Max Flow" : i == maxHead ? "Max Head" : "Curve Point";
                rows.add(new String[]{label, PumpSelector.formatFlow(pts[i][1], unit) + " at " + String.format(Locale.US, "%.1f m", pts[i][0])});
            }
        }

        if (has && !Double.isNaN(head) && !Double.isNaN(flow)) {
            rows.add(new String[]{"Selected point", PumpSelector.formatFlow(flow, unit) + " at " + String.format(Locale.US, "%.1f m", head)});
        }

        return rows.toArray(new String[0][]);
    }

    String[][] motorRows() {
        ArrayList<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Power", PumpSelector.trim(rec.hp) + " HP / " + PumpSelector.trim(rec.kw) + " kW"});
        rows.add(new String[]{"Phase", phaseLabel(rec.phase)});
        rows.add(new String[]{"Speed", empty(rec.rpm) ? "-" : rec.rpm + " RPM"});
        rows.add(new String[]{"Insulation class", empty(rec.insulationClass) ? "-" : rec.insulationClass});
        rows.add(new String[]{"Frame size", empty(rec.frameSize) ? "-" : rec.frameSize});
        rows.add(new String[]{"Motor type", empty(rec.motorType) ? rec.category : rec.motorType});
        if(!empty(rec.ratedCurrent))rows.add(new String[]{"Rated current",rec.ratedCurrent});
        if(!empty(rec.startingCurrent))rows.add(new String[]{"Starting current",rec.startingCurrent});
        if(!empty(rec.ratedTorque))rows.add(new String[]{"Rated torque",rec.ratedTorque});
        rows.add(new String[]{"Page", String.valueOf(rec.page)});
        rows.add(new String[]{"Brand", empty(rec.brand) ? "-" : rec.brand});
        rows.add(new String[]{"Data status", statusLabel(rec)});
        return rows.toArray(new String[0][]);
    }

    LinearLayout motorActions() {
        LinearLayout row = Ui.row(this);
        Button copy = Ui.secondary(this, "Copy motor model");
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        Button back = Ui.secondary(this, "Back");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        row.addView(back, bp);
        copy.setOnClickListener(v -> {
            ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("model", rec.model));
            Toast.makeText(this, "Motor model copied", Toast.LENGTH_SHORT).show();
        });
        back.setOnClickListener(v -> finish());
        return row;
    }

    String[][] quickRows() {
        ArrayList<String[]> rows=new ArrayList<>();
        rows.add(new String[]{"Delivery / Pipe size",empty(rec.size)?"-":rec.size});
        rows.add(new String[]{"Page",String.valueOf(rec.page)});
        rows.add(new String[]{"Head range",safe(rec.headRangeText)+" m"});
        rows.add(new String[]{"Flow range",flowRange(rec)});
        rows.add(new String[]{"Phase",phaseLabel(rec.phase)});
        rows.add(new String[]{"Brand",empty(rec.brand)?"-":rec.brand});
        if(!empty(rec.stages))rows.add(new String[]{"Stages",rec.stages});
        if(!empty(rec.variantLabel))rows.add(new String[]{"Variant",rec.variantLabel});
        rows.add(new String[]{"Data status",statusLabel(rec)});
        return rows.toArray(new String[0][]);
    }


    String[][] technicalRows() {
        ArrayList<String[]> rows = new ArrayList<>();
        addTechnical(rows,"Power supply",rec.powerSupply);
        addTechnical(rows,"Starting method",rec.startingMethod);
        addTechnical(rows,"Suction size",rec.suctionSize);
        addTechnical(rows,"Delivery / outlet size",!empty(rec.deliverySize)?rec.deliverySize:rec.outletDN);
        addTechnical(rows,"NRV size",rec.nrvSize);
        addTechnical(rows,"Cable size",rec.cableSize);
        addTechnical(rows,"Rated current",rec.ratedCurrent);
        addTechnical(rows,"Free passage",rec.freePassage);
        addTechnical(rows,"Maximum solid size",rec.maxSolidSize);
        addTechnical(rows,"Impeller diameter",rec.impellerDiameter);
        addTechnical(rows,"Minimum well diameter",rec.minimumWellDiameter);
        addTechnical(rows,"Nominal speed",rec.nominalSpeed);
        addTechnical(rows,"Rotor",rec.rotor);
        addTechnical(rows,"Cable length",rec.cableLength);
        return rows.toArray(new String[0][]);
    }

    void addTechnical(ArrayList<String[]> rows,String label,String value){if(!empty(value))rows.add(new String[]{label,value});}

    LinearLayout detailCard(String title, String[][] rows) {
        LinearLayout c = Ui.card(this);
        c.addView(Ui.text(this, title, 18, Ui.TEXT, 1));

        for (String[] row : rows) {
            LinearLayout line = Ui.row(this);
            line.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 6));
            TextView l = Ui.text(this, row[0], 15, Ui.MUTED, 1);
            TextView r = Ui.text(this, row[1], 16, Ui.TEXT, 0);
            line.addView(l, new LinearLayout.LayoutParams(0, -2, 1));
            line.addView(r, new LinearLayout.LayoutParams(0, -2, 1));
            c.addView(line);
        }
        return c;
    }

    String flowRange(PumpRecord r) {
        if (!Double.isNaN(r.minFlowLPH) && !Double.isNaN(r.maxFlowLPH)) {
            return PumpSelector.formatFlowNumber(PumpSelector.fromLPH(r.minFlowLPH, unit), unit) + " – " + PumpSelector.formatFlow(r.maxFlowLPH, unit);
        }
        return safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal);
    }

    String catalogueText() {
        String s = safe(rec.catalogueSectionText);
        if (s.isEmpty()) s = !empty(rec.title) ? rec.title : (!empty(rec.brand) && !empty(rec.category) ? rec.brand + " | " + rec.category : rec.category);
        String source = safe(rec.sourceFile);
        if (!source.isEmpty()) s += "\nSource: " + source + (empty(rec.sourcePage) ? "" : " • Page " + rec.sourcePage);
        if (!empty(rec.crossCheckedWith)) s += "\nCross-check: " + rec.crossCheckedWith;
        if (!empty(rec.dataNote) && (!rec.selectable || "NEEDS_REVIEW".equals(rec.dataStatus))) s += "\nQA note: " + rec.dataNote;
        return s;
    }

    String phaseLabel(String p) {
        p = safe(p).toUpperCase(Locale.US);
        boolean s = p.contains("S");
        boolean t = p.contains("T") || p.contains("3");
        if (s && t) return "Single / Three Phase";
        if (s) return "Single Phase";
        if (t) return "Three Phase";
        return p.isEmpty() ? "Phase -" : p;
    }

    String statusLabel(PumpRecord r){
        if(!r.selectable || "NEEDS_REVIEW".equals(r.dataStatus))return "Needs source review";
        if("AUTO_FIXED".equals(r.dataStatus))return "Automatically corrected and checked";
        if("SOURCE_CONFIRMED".equals(r.dataStatus))return "Source confirmed";
        if("SOURCE_EXTRACTED".equals(r.dataStatus))return "Source extracted";
        if("AUTO_CHECKED".equals(r.dataStatus))return "Automatically checked against source structure";
        return "Automatically checked";
    }

    String safe(String s) { return s == null ? "" : s.trim(); }
    boolean empty(String s) { return safe(s).isEmpty(); }
}
