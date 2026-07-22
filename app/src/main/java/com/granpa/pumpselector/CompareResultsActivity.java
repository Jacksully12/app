package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CompareResultsActivity extends Activity {
    PumpListAdapter adapter;
    TextView count, empty;
    ArrayList<PumpSelector.Result> results = new ArrayList<>();
    HashSet<String> collapsedGroups = new HashSet<>();

    double head, flow;
    String unit = "LPH";
    String cat = "all";
    String phase = "any";

    protected void onCreate(Bundle b) {
        super.onCreate(b);

        head = getIntent().getDoubleExtra("head", Double.NaN);
        flow = getIntent().getDoubleExtra("flow", Double.NaN);
        unit = PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));
        cat = safe(getIntent().getStringExtra("cat"));
        if (cat.isEmpty()) cat = "all";
        phase = safe(getIntent().getStringExtra("phase"));
        if (phase.isEmpty()) phase = "any";

        LinearLayout root = Ui.root(this);

        LinearLayout summary = Ui.card(this);
        summary.addView(Ui.text(this, "Compare results", 26, Ui.TEXT, 1));
        summary.addView(Ui.text(this,
                PumpSelector.head(head) + " fixed head • Required flow " + PumpSelector.formatFlow(PumpSelector.toLPH(flow, unit), unit),
                14, Ui.MUTED, 0));
        summary.addView(Ui.text(this, "Pump type: " + categoryLabel(cat) + " • Phase: " + phaseLabel(phase), 13, Ui.MUTED, 0));

        count = Ui.text(this, "", 22, Ui.GREEN, 1);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1, -2);
        cp.setMargins(0, Ui.dp(this, 8), 0, 0);
        count.setLayoutParams(cp);
        summary.addView(count);
        root.addView(summary);

        LinearLayout actions = Ui.row(this);
        Button back = Ui.secondary(this, "Back to compare");
        actions.addView(back, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(actions);

        empty = Ui.text(this, "", 15, Ui.MUTED, 0);
        empty.setVisibility(View.GONE);
        root.addView(empty);

        adapter = new PumpListAdapter(this);
        adapter.setDisplayUnit(unit);

        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);

        back.setOnClickListener(v -> finish());

        list.setOnItemClickListener((p, v, pos, id) -> {
            PumpSelector.Result r = adapter.getResult(pos);
            if (r != null && r.header) {
                String base = baseTitle(r.groupTitle);
                if (collapsedGroups.contains(base)) collapsedGroups.remove(base);
                else collapsedGroups.add(base);
                applyCollapsed();
                return;
            }
            openDetails(r);
        });

        runCompare();
    }

    void runCompare() {
        if (Double.isNaN(head) || head < 0 || Double.isNaN(flow) || flow <= 0) {
            empty.setText("Invalid compare input. Go back and enter valid head and flow values.");
            empty.setVisibility(View.VISIBLE);
            count.setText("0 comparable models");
            adapter.setItems(new ArrayList<>());
            return;
        }

        PumpSelector.Req req = PumpSelector.req(false, flow, flow, unit);
        results.clear();
        collapsedGroups.clear();

        addBrand("TEXMO", PumpRepository.TEXMO_ASSET, head, req);
        addBrand("LUBI", PumpRepository.LUBI_ASSET, head, req);
        addBrand("KSB", PumpRepository.KSB_ASSET, head, req);

        int total = PumpSelector.realCount(results);
        count.setText(total + " comparable models");
        count.setTextColor(total == 0 ? Ui.ORANGE : Ui.GREEN);
        empty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        empty.setText(total == 0 ? "No comparable model found in the Texmo, Lubi or KSB catalogues. Try another pump type, phase, head or flow." : "");

        applyCollapsed();
    }

    void addBrand(String brand, String asset, double h, PumpSelector.Req req) {
        ArrayList<PumpSelector.Result> rows = PumpSelector.selectForCompare(PumpRepository.getRecords(this, asset), h, req, cat, phase);
        if (!rows.isEmpty()) {
            results.add(PumpSelector.header(brand + " • " + rows.size() + " models"));
            results.addAll(rows);
        }
    }

    void applyCollapsed() {
        ArrayList<PumpSelector.Result> out = new ArrayList<>();
        String current = "";
        boolean hidden = false;

        for (PumpSelector.Result r : results) {
            if (r.header) {
                current = baseTitle(r.groupTitle);
                hidden = collapsedGroups.contains(current);
                out.add(r);
            } else if (!hidden) {
                out.add(r);
            }
        }

        adapter.setCollapsedGroups(collapsedGroups);
        adapter.setItems(out);
    }

    void openDetails(PumpSelector.Result r) {
        if (r == null || r.header || r.r == null) return;

        Intent i = new Intent(this, PumpDetailsActivity.class);
        i.putExtra("asset", assetForBrand(r.r.brand));
        i.putExtra("brand", r.r.brand);
        i.putExtra("id", r.r.id);
        i.putExtra("head", r.head);
        i.putExtra("flow", r.flow);
        i.putExtra("estimate", r.estimate);
        i.putExtra("unit", unit);
        startActivity(i);
    }

    String assetForBrand(String brand) {
        brand = safe(brand).toUpperCase(Locale.US);
        if (brand.equals("LUBI")) return PumpRepository.LUBI_ASSET;
        if (brand.equals("KSB")) return PumpRepository.KSB_ASSET;
        return PumpRepository.TEXMO_ASSET;
    }

    String baseTitle(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+•\\s+\\d+\\s+models?$", "").trim();
    }

    String categoryLabel(String c) {
        c = safe(c);
        if (c.equals("all")) return "All pump types";
        if (c.equals("borewell_all")) return "Borewell Submersible";
        if (c.equals("openwell_all")) return "Openwell Submersible";
        if (c.equals("monoblock_all")) return "Centrifugal / Surface Monoblock";
        if (c.equals("multistage_all")) return "Multistage Pumps";
        if (c.equals("booster_all")) return "Booster / Pressure Pumps";
        if (c.equals("dewatering_all")) return "Dewatering / Sewage";
        if (c.equals("motors_all")) return "Motors";
        return c;
    }

    String phaseLabel(String p) {
        p = safe(p);
        if (p.equals("S")) return "Single phase";
        if (p.equals("T")) return "Three phase";
        return "Any phase";
    }

    String safe(String s) { return s == null ? "" : s.trim(); }
}
