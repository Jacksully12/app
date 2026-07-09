package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText modelSearch, head, flow1, flow2;
    Spinner mode, unit, cat, phase;
    LinearLayout flow2Box;
    TextView hint;
    String asset = PumpRepository.TEXMO_ASSET;
    String brand = "TEXMO";

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        asset = PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));
        brand = getIntent().getStringExtra("brand");
        if (brand == null || brand.trim().isEmpty()) brand = PumpRepository.brandName(asset);
        PumpRepository.getRecords(this, asset);

        LinearLayout root = Ui.root(this);
        root.addView(header());

        LinearLayout card = Ui.card(this);

        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout hr = Ui.row(this);
        head = Ui.input(this, "40", Ui.numberInput());
        hr.addView(head, new LinearLayout.LayoutParams(0, -2, 1));
        TextView m = Ui.text(this, " m", 15, Ui.MUTED, 1);
        m.setPadding(Ui.dp(this, 8), 0, 0, 0);
        hr.addView(m);
        card.addView(hr);

        card.addView(Ui.label(this, "Water input mode"));
        mode = Ui.spinner(this, options(new String[][]{
                {"fixed", "Fixed water flow", "Enter one required discharge value"},
                {"range", "Water-flow range", "Enter minimum and maximum acceptable discharge"}
        }));
        card.addView(mode);

        card.addView(Ui.label(this, "Water flow"));
        LinearLayout fr = Ui.row(this);
        flow1 = Ui.input(this, "1200", Ui.numberInput());
        fr.addView(flow1, new LinearLayout.LayoutParams(0, -2, 1));

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

        flow2Box = new LinearLayout(this);
        flow2Box.setOrientation(LinearLayout.VERTICAL);
        flow2Box.addView(Ui.label(this, "Second flow value"));
        flow2 = Ui.input(this, "4500", Ui.numberInput());
        flow2Box.addView(flow2);
        card.addView(flow2Box);

        hint = Ui.text(this, "", 13, Ui.MUTED, 0);
        hint.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        card.addView(hint);

        card.addView(Ui.label(this, "Pump type"));
        cat = Ui.spinner(this, categories());
        card.addView(cat);

        card.addView(Ui.label(this, "Phase"));
        phase = Ui.spinner(this, options(new String[][]{
                {"any", "Any phase", "Show single and three phase models"},
                {"S", "Single phase", "Usually 220 V supply"},
                {"T", "Three phase", "Usually 380 V supply"}
        }));
        Ui.mb(this, phase, 18);
        card.addView(phase);

        Button find = Ui.primary(this, "Find suitable pumps");
        Ui.mb(this, find, 12);
        card.addView(find);

        Button browse = Ui.blue(this, "Search catalogue / model list");
        card.addView(browse);

        root.addView(card);
        root.addView(Ui.text(this, PumpRepository.note(this, asset), 12, Ui.MUTED, 0));
        setContentView(Ui.scroll(this, root));

        update();
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { update(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        unit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { update(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        find.setOnClickListener(v -> openResults());
        browse.setOnClickListener(v -> {
            Intent ci = new Intent(this, CatalogueActivity.class);
            ci.putExtra("asset", asset);
            ci.putExtra("brand", brand);
            startActivity(ci);
        });
    }

    LinearLayout header() {
        LinearLayout h = Ui.card(this);
        LinearLayout row = Ui.row(this);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 72), Ui.dp(this, 72)));

        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setPadding(Ui.dp(this, 14), 0, 0, 0);
        t.addView(Ui.text(this, "Granpa", 30, Ui.TEXT, 1));
        t.addView(Ui.text(this, brand + " pump selector", 14, Ui.MUTED, 0));

        row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        h.addView(row);
        return h;
    }

    void update() {
        boolean r = sel(mode).equals("range");
        String u = PumpSelector.unitLabel(sel(unit));
        flow2Box.setVisibility(r ? View.VISIBLE : View.GONE);
        hint.setText(r
                ? "Range mode uses " + u + ". You can enter either order, for example 4500 to 1200."
                : "Fixed mode uses " + u + ". Dealer smart rule: selected type shows max 2 above + 2 below. All pump types is grouped category-wise so no category is hidden.");
    }

    void openResults() {
        double h = val(head), f1 = val(flow1), f2 = val(flow2);
        boolean range = sel(mode).equals("range");

        if (Double.isNaN(h) || h < 0 || Double.isNaN(f1) || f1 <= 0 || range && (Double.isNaN(f2) || f2 <= 0)) {
            Toast.makeText(this, "Enter valid head and flow values", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(this, ResultsActivity.class);
        i.putExtra("head", h);
        i.putExtra("range", range);
        i.putExtra("flow1", f1);
        i.putExtra("flow2", range ? f2 : f1);
        i.putExtra("unit", sel(unit));
        i.putExtra("cat", sel(cat));
        i.putExtra("phase", sel(phase));
        i.putExtra("key", "");
        i.putExtra("asset", asset);
        i.putExtra("brand", brand);
        startActivity(i);
    }

    List<Option> categories() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types", "Main category • full catalogue", true));
        o.add(new Option("borewell_all", "Borewell Submersible", "Main category • borewell sections", true));
        o.add(new Option("openwell_all", "Openwell Submersible", "Main category • openwell sections", true));
        o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Main category • self priming, jet, centrifugal and agricultural monoblock", true));
        o.add(new Option("multistage_all", "Multistage Pumps", "Main category • AVRS, vertical inline and horizontal multistage", true));
        o.add(new Option("booster_all", "Booster / Pressure Pumps", "Main category • booster and pressure pump sections", true));
        o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • sewage and dewatering pumps", true));
        o.add(new Option("motors_all", "Motors", "Main category • motor section", true));
        for (String c : PumpRepository.categories(this, asset)) o.add(new Option(c, "Sub category • " + c, detail(c), false));
        return o;
    }

    String detail(String c) {
        c = c == null ? "" : c.toLowerCase(Locale.US);
        if (c.contains("openwell")) return "Detailed openwell catalogue section";
        if (c.contains("borewell")) return "Detailed borewell catalogue section";
        if (c.contains("avrs") || c.contains("multistage")) return "Detailed multistage catalogue section";
        if (c.contains("motor")) return "Detailed motor catalogue section";
        if (c.contains("agricultural") || c.contains("centrifugal") || c.contains("jet") || c.contains("self priming")) return "Detailed surface / monoblock catalogue section";
        if (c.contains("dewatering") || c.contains("sewage")) return "Detailed dewatering / sewage catalogue section";
        return "Detailed catalogue section";
    }

    List<Option> options(String[][] a) {
        ArrayList<Option> o = new ArrayList<>();
        for (String[] x : a) o.add(new Option(x[0], x[1], x.length > 2 ? x[2] : ""));
        return o;
    }

    String sel(Spinner s) {
        Object o = s.getSelectedItem();
        return o instanceof Option ? ((Option) o).value : String.valueOf(o);
    }

    double val(EditText e) {
        try { return Double.parseDouble(e.getText().toString().replace(",", "").trim()); }
        catch (Exception x) { return Double.NaN; }
    }
}
