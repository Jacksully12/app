package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CatalogueActivity extends Activity {
    PumpListAdapter adapter;
    EditText search;
    Spinner cat, phase;
    TextView count;
    String asset = PumpRepository.TEXMO_ASSET;
    String brand = "TEXMO";

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        asset = PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));
        brand = getIntent().getStringExtra("brand");
        if (brand == null || brand.trim().isEmpty()) brand = PumpRepository.brandName(asset);

        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, brand + " Catalogue / Model Search", 26, Ui.TEXT, 1));
        TextView note = Ui.text(this, "Search by model, type, size, frame, RPM, category or catalogue section.", 13, Ui.MUTED, 0);
        Ui.mb(this, note, 10);
        root.addView(note);

        search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search model, type, size, frame or category");
        Ui.mb(this, search, 10);
        root.addView(search);

        cat = Ui.spinner(this, categories());
        Ui.mb(this, cat, 10);
        root.addView(cat);

        phase = Ui.spinner(this, phaseOptions());
        Ui.mb(this, phase, 10);
        root.addView(phase);

        count = Ui.text(this, "", 14, Ui.MUTED, 1);
        Ui.mb(this, count, 8);
        root.addView(count);

        adapter = new PumpListAdapter(this);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        TextWatcher w = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { refresh(); }
            public void afterTextChanged(Editable e) {}
        };
        search.addTextChangedListener(w);

        AdapterView.OnItemSelectedListener refreshListener = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { refresh(); }
            public void onNothingSelected(AdapterView<?> p) {}
        };
        cat.setOnItemSelectedListener(refreshListener);
        phase.setOnItemSelectedListener(refreshListener);

        selectValue(cat, getIntent().getStringExtra("initialCat"));
        selectValue(phase, getIntent().getStringExtra("initialPhase"));

        list.setOnItemClickListener((p, v, pos, id) -> {
            PumpSelector.Result r = adapter.getResult(pos);
            if (r == null || r.r == null || r.header) return;
            Intent i = new Intent(this, PumpDetailsActivity.class);
            i.putExtra("asset", asset);
            i.putExtra("brand", brand);
            i.putExtra("id", r.r.id);
            startActivity(i);
        });
        refresh();
    }

    void refresh() {
        ArrayList<PumpSelector.Result> raw = PumpSelector.catalogue(PumpRepository.getRecords(this, asset), sel(cat), search.getText().toString());
        ArrayList<PumpSelector.Result> filtered = new ArrayList<>();
        String ph = sel(phase);
        for (PumpSelector.Result r : raw) {
            if (r != null && r.r != null && PumpSelector.phase(r.r, ph)) filtered.add(r);
        }
        adapter.setItems(filtered);
        boolean motors = "motors_all".equals(sel(cat)) || (!filtered.isEmpty() && filtered.get(0).r.isMotor());
        count.setText(filtered.size() + (motors ? " motor models shown" : " catalogue models shown"));
    }

    List<Option> phaseOptions() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("any", "Any phase", "Show single and three phase models"));
        o.add(new Option("S", "Single phase", "Usually 220–240 V supply"));
        o.add(new Option("T", "Three phase", "Usually 380–415 V supply"));
        return o;
    }

    List<Option> categories() {
        ArrayList<Option> o = new ArrayList<>();
        if (PumpRepository.TEXMO_ASSET.equals(asset)) {
            o.add(new Option("all", "All pump types", "Main category • full catalogue", true));
            o.add(new Option("borewell_all", "Borewell Submersible", "Main category • borewell sections", true));
            o.add(new Option("openwell_all", "Openwell Submersible", "Main category • openwell sections", true));
            o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Main category • self priming, jet, centrifugal and agricultural monoblock", true));
            o.add(new Option("multistage_all", "Multistage Pumps", "Main category • AVRS, vertical inline and horizontal multistage", true));
            o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • sewage and dewatering pumps", true));
            o.add(new Option("motors_all", "Motors", "Main category • 35 bare and flange motor models", true));
        } else if (PumpRepository.LUBI_ASSET.equals(asset)) {
            o.add(new Option("all", "All pump types", "Main category • full Lubi catalogue", true));
            o.add(new Option("borewell_all", "Borewell Submersible", "Main category • borewell sections", true));
            o.add(new Option("openwell_all", "Openwell Submersible", "Main category • openwell sections", true));
            o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Main category • self priming, jet and monoblock sections", true));
            o.add(new Option("multistage_all", "Multistage Pumps", "Main category • horizontal and vertical multistage", true));
            o.add(new Option("booster_all", "Booster / Pressure Pumps", "Main category • booster and pressure sections", true));
            o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • drainage, sewage and dewatering", true));
        } else {
            o.add(new Option("all", "All pump types", "Main category • full KSB catalogue", true));
            o.add(new Option("borewell_all", "Borewell Submersible", "Main category • water-filled and oil-filled borewell sections", true));
            o.add(new Option("openwell_all", "Openwell Submersible", "Main category • openwell sections", true));
            o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Main category • self priming, jet, monobloc and surface sections", true));
            o.add(new Option("multistage_all", "Multistage Pumps", "Main category • multistage sections", true));
            o.add(new Option("booster_all", "Booster / Pressure Pumps", "Main category • booster and pressure sections", true));
            o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • drainage and sewage sections", true));
        }
        for (String c : PumpRepository.categories(this, asset)) o.add(new Option(c, "Sub category • " + c, "", false));
        return o;
    }

    void selectValue(Spinner spinner, String value) {
        if (value == null || value.trim().isEmpty()) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item instanceof Option && value.equals(((Option) item).value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    String sel(Spinner s) {
        Object x = s == null ? null : s.getSelectedItem();
        return x instanceof Option ? ((Option) x).value : "any";
    }
}
