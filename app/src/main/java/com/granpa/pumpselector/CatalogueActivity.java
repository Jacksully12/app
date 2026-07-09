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
    Spinner cat;
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

        TextView note = Ui.text(this, "Search by model, type, size, category or catalogue section.", 13, Ui.MUTED, 0);
        Ui.mb(this, note, 10);
        root.addView(note);

        search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search here, e.g. model, Openwell, 50");
        Ui.mb(this, search, 10);
        root.addView(search);

        cat = Ui.spinner(this, categories());
        Ui.mb(this, cat, 10);
        root.addView(cat);

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

        cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { refresh(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

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
        ArrayList<PumpSelector.Result> res = PumpSelector.catalogue(PumpRepository.getRecords(this, asset), sel(cat), search.getText().toString());
        adapter.setItems(res);
        count.setText(res.size() + " catalogue models shown");
    }

    List<Option> categories() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types", "Main category • full catalogue", true));
        o.add(new Option("borewell_all", "Borewell Submersible", "Main category • borewell sections", true));
        o.add(new Option("openwell_all", "Openwell Submersible", "Main category • openwell sections", true));
        o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Main category • self priming, jet, centrifugal and monoblock", true));
        o.add(new Option("multistage_all", "Multistage Pumps", "Main category • multistage sections", true));
        o.add(new Option("booster_all", "Booster / Pressure Pumps", "Main category • booster and pressure pump sections", true));
        o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • drainage, sewage and dewatering", true));
        o.add(new Option("motors_all", "Motors", "Main category • motor section", true));
        for (String c : PumpRepository.categories(this, asset)) o.add(new Option(c, "Sub category • " + c, "", false));
        return o;
    }

    String sel(Spinner s) { return ((Option) s.getSelectedItem()).value; }
}
