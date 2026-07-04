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

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        PumpRepository.getRecords(this);

        LinearLayout root = Ui.root(this);

        LinearLayout header = Ui.card(this);
        LinearLayout row = Ui.row(this);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64)));
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(Ui.dp(this, 12), 0, 0, 0);
        titleBox.addView(Ui.text(this, "Granpa", 28, Ui.TEXT, 1));
        titleBox.addView(Ui.text(this, "Pump selector with chart and WhatsApp image share", 14, Ui.MUTED, 0));
        row.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(row);
        root.addView(header);

        LinearLayout card = Ui.card(this);

        card.addView(Ui.label(this, "Search model / keyword"));
        modelSearch = Ui.input(this, "", InputType.TYPE_CLASS_TEXT);
        modelSearch.setHint("Type first, e.g. ACS1125, ASM, JRF17");
        card.addView(modelSearch);
        TextView searchHint = Ui.text(this, "Search ignores spaces, so ACS1125 finds ACS 1125.", 12, Ui.MUTED, 0);
        searchHint.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        card.addView(searchHint);

        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout headRow = Ui.row(this);
        head = Ui.input(this, "40", Ui.numberInput());
        headRow.addView(head, new LinearLayout.LayoutParams(0, -2, 1));
        TextView metre = Ui.text(this, " m", 15, Ui.MUTED, 1);
        metre.setPadding(Ui.dp(this, 8), 0, 0, 0);
        headRow.addView(metre);
        card.addView(headRow);

        card.addView(Ui.label(this, "Water input mode"));
        mode = Ui.spinner(this, opts(new String[][]{{"fixed", "Fixed water flow"}, {"range", "Water-flow range"}}));
        card.addView(mode);

        card.addView(Ui.label(this, "Water flow"));
        LinearLayout flowRow = Ui.row(this);
        flow1 = Ui.input(this, "1200", Ui.numberInput());
        flowRow.addView(flow1, new LinearLayout.LayoutParams(0, -2, 1));
        unit = Ui.spinner(this, opts(new String[][]{{"LPH", "LPH"}, {"LPM", "LPM"}, {"LPS", "LPS"}, {"M3H", "m³/hour"}}));
        LinearLayout.LayoutParams unitLp = new LinearLayout.LayoutParams(Ui.dp(this, 130), -2);
        unitLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        flowRow.addView(unit, unitLp);
        card.addView(flowRow);

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
        cat = Ui.spinner(this, cats());
        card.addView(cat);

        card.addView(Ui.label(this, "Phase"));
        phase = Ui.spinner(this, opts(new String[][]{{"any", "Any"}, {"S", "Single phase"}, {"T", "Three phase"}}));
        card.addView(phase);

        Button find = Ui.primary(this, "Find suitable pumps");
        Ui.mb(this, find, 10);
        card.addView(find);

        Button browse = Ui.secondary(this, "Browse full catalogue");
        Ui.mb(this, browse, 10);
        card.addView(browse);

        Button qa = Ui.secondary(this, "Data QA report");
        card.addView(qa);

        root.addView(card);
        root.addView(Ui.text(this, PumpRepository.note(this), 12, Ui.MUTED, 0));
        setContentView(Ui.scroll(this, root));

        update();
        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { update(); }
            public void onNothingSelected(AdapterView<?> p) {}
        });

        find.setOnClickListener(v -> openResults());
        browse.setOnClickListener(v -> startActivity(new Intent(this, CatalogueActivity.class)));
        qa.setOnClickListener(v -> startActivity(new Intent(this, QAActivity.class)));
    }

    void openResults() {
        double h = val(head);
        double f1 = val(flow1);
        double f2 = val(flow2);
        boolean range = sel(mode).equals("range");
        if (Double.isNaN(h) || h < 0 || Double.isNaN(f1) || f1 <= 0 || (range && (Double.isNaN(f2) || f2 <= 0))) {
            Toast.makeText(this, "Enter valid head and water flow values", Toast.LENGTH_LONG).show();
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
        i.putExtra("key", modelSearch.getText().toString());
        startActivity(i);
    }

    void update() {
        boolean r = sel(mode).equals("range");
        flow2Box.setVisibility(r ? View.VISIBLE : View.GONE);
        hint.setText(r ? "Range accepts either order, e.g. 13,500 to 4,500." : "Fixed mode shows pumps with equal or more water at the selected head. No upper limit.");
    }

    List<Option> cats() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types"));
        o.add(new Option("monoblock_all", "All Monoblock / Centrifugal"));
        o.add(new Option("submersible_all", "All Submersible"));
        o.add(new Option("borewell_all", "All Borewell Submersible"));
        for (String c : PumpRepository.categories(this)) o.add(new Option(c, c));
        return o;
    }

    List<Option> opts(String[][] a) {
        ArrayList<Option> o = new ArrayList<>();
        for (String[] x : a) o.add(new Option(x[0], x[1]));
        return o;
    }

    String sel(Spinner s) {
        Object o = s.getSelectedItem();
        return o instanceof Option ? ((Option) o).value : String.valueOf(o);
    }

    double val(EditText e) {
        try { return Double.parseDouble(e.getText().toString().replace(",", "").trim()); }
        catch (Exception ex) { return Double.NaN; }
    }
}
