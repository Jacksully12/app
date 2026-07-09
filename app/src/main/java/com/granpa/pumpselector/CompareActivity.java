package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CompareActivity extends Activity {
    EditText head, flow;
    Spinner unit, cat, phase;
    PumpListAdapter adapter;
    TextView count, empty;
    ArrayList<PumpSelector.Result> results = new ArrayList<>();
    HashSet<String> collapsedGroups = new HashSet<>();

    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Compare Texmo vs Lubi", 26, Ui.TEXT, 1));
        TextView note = Ui.text(this, "Enter one duty point. The app checks both catalogues and shows the closest options from each brand.", 14, Ui.MUTED, 0);
        Ui.mb(this, note, 10);
        root.addView(note);

        LinearLayout card = Ui.card(this);

        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout hr = Ui.row(this);
        head = Ui.input(this, "40", Ui.numberInput());
        hr.addView(head, new LinearLayout.LayoutParams(0, -2, 1));
        TextView m = Ui.text(this, " m", 15, Ui.MUTED, 1);
        m.setPadding(Ui.dp(this, 8), 0, 0, 0);
        hr.addView(m);
        card.addView(hr);

        card.addView(Ui.label(this, "Required flow"));
        LinearLayout fr = Ui.row(this);
        flow = Ui.input(this, "1200", Ui.numberInput());
        fr.addView(flow, new LinearLayout.LayoutParams(0, -2, 1));
        unit = Ui.spinner(this, options(new String[][]{
                {"LPH", "LPH", "Litres per hour"},
                {"LPM", "LPM", "Litres per minute"},
                {"LPS", "LPS", "Litres per second"},
                {"M3H", "m³/hour", "Cubic metres per hour"}
        }));
        LinearLayout.LayoutParams ulp = new LinearLayout.LayoutParams(Ui.dp(this, 150), -2);
        ulp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        fr.addView(unit, ulp);
        card.addView(fr);

        card.addView(Ui.label(this, "Pump type"));
        cat = Ui.spinner(this, categories());
        card.addView(cat);

        card.addView(Ui.label(this, "Phase"));
        phase = Ui.spinner(this, options(new String[][]{
                {"any", "Any phase", "Show single and three phase models"},
                {"S", "Single phase", "Usually 220 V supply"},
                {"T", "Three phase", "Usually 380/415 V supply"}
        }));
        Ui.mb(this, phase, 14);
        card.addView(phase);

        Button compare = Ui.primary(this, "Compare brands");
        card.addView(compare);
        root.addView(card);

        count = Ui.text(this, "", 18, Ui.GREEN, 1);
        Ui.mb(this, count, 8);
        root.addView(count);

        empty = Ui.text(this, "", 15, Ui.MUTED, 0);
        empty.setVisibility(View.GONE);
        root.addView(empty);

        adapter = new PumpListAdapter(this);
        adapter.setDisplayUnit("LPH");
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);

        compare.setOnClickListener(v -> runCompare());
        list.setOnItemClickListener((p, v, pos, id) -> {
            PumpSelector.Result r = adapter.getResult(pos);
            if (r != null && r.header) {
                String base = baseTitle(r.groupTitle);
                if (collapsedGroups.contains(base)) collapsedGroups.remove(base);
                else collapsedGroups.add(base);
                applyCollapsed();
                return;
            }
            if (r == null || r.r == null) return;
            Intent i = new Intent(this, PumpDetailsActivity.class);
            i.putExtra("asset", "LUBI".equalsIgnoreCase(r.r.brand) ? PumpRepository.LUBI_ASSET : PumpRepository.TEXMO_ASSET);
            i.putExtra("brand", r.r.brand);
            i.putExtra("id", r.r.id);
            i.putExtra("head", r.head);
            i.putExtra("flow", r.flow);
            i.putExtra("estimate", r.estimate);
            i.putExtra("unit", sel(unit));
            startActivity(i);
        });

        runCompare();
    }

    void runCompare() {
        double h = val(head);
        double f = val(flow);
        String u = sel(unit);

        if (Double.isNaN(h) || h < 0 || Double.isNaN(f) || f <= 0) {
            Toast.makeText(this, "Enter valid head and flow values", Toast.LENGTH_LONG).show();
            return;
        }

        PumpSelector.Req req = PumpSelector.req(false, f, f, u);
        results.clear();

        addBrand("TEXMO", PumpRepository.TEXMO_ASSET, h, req);
        addBrand("LUBI", PumpRepository.LUBI_ASSET, h, req);

        int total = PumpSelector.realCount(results);
        count.setText(total + " comparable models");
        count.setTextColor(total == 0 ? Ui.ORANGE : Ui.GREEN);
        empty.setVisibility(total == 0 ? View.VISIBLE : View.GONE);
        empty.setText(total == 0 ? "No comparable model found in either catalogue. Try another pump type, phase, head or flow." : "");
        adapter.setDisplayUnit(u);
        applyCollapsed();
    }

    void addBrand(String brand, String asset, double h, PumpSelector.Req req) {
        ArrayList<PumpSelector.Result> rows = PumpSelector.select(PumpRepository.getRecords(this, asset), h, req, sel(cat), sel(phase), "");
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

    String baseTitle(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+•\\s+\\d+\\s+models?$", "").trim();
    }

    List<Option> categories() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types", "Compare all pump categories", true));
        o.add(new Option("borewell_all", "Borewell Submersible", "Main category", true));
        o.add(new Option("openwell_all", "Openwell Submersible", "Main category", true));
        o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Self priming, jet, centrifugal and monoblock", true));
        o.add(new Option("multistage_all", "Multistage Pumps", "Vertical/openwell/horizontal multistage", true));
        o.add(new Option("booster_all", "Booster / Pressure Pumps", "Booster and pressure pump sections", true));
        o.add(new Option("dewatering_all", "Dewatering / Sewage", "Drainage, sewage and similar pumps", true));
        return o;
    }

    List<Option> options(String[][] arr) {
        ArrayList<Option> o = new ArrayList<>();
        for (String[] a : arr) o.add(new Option(a[0], a[1], a.length > 2 ? a[2] : "", true));
        return o;
    }

    String sel(Spinner s) { return ((Option) s.getSelectedItem()).value; }
    double val(EditText e) { try { return Double.parseDouble(e.getText().toString().trim()); } catch (Exception ex) { return Double.NaN; } }
}
