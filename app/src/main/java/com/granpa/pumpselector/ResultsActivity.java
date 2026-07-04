package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ResultsActivity extends Activity {
    PumpListAdapter adapter;
    ArrayList<PumpSelector.Result> all = new ArrayList<>();
    double head;

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        Intent in = getIntent();
        head = in.getDoubleExtra("head", Double.NaN);
        boolean range = in.getBooleanExtra("range", false);
        double f1 = in.getDoubleExtra("flow1", Double.NaN);
        double f2 = in.getDoubleExtra("flow2", f1);
        String unit = in.getStringExtra("unit");
        String cat = in.getStringExtra("cat");
        String phase = in.getStringExtra("phase");
        String key = in.getStringExtra("key");
        PumpSelector.Req req = PumpSelector.req(range, f1, f2, unit);
        all = PumpSelector.select(PumpRepository.getRecords(this), head, req, cat, phase, key);

        LinearLayout root = Ui.root(this);
        LinearLayout summary = Ui.card(this);
        summary.addView(Ui.text(this, "Results", 25, Ui.TEXT, 1));
        summary.addView(Ui.text(this, PumpSelector.head(head) + " fixed head • " + (req == null ? "-" : req.label), 14, Ui.MUTED, 0));
        summary.addView(Ui.text(this, all.size() + " matching models", 22, all.isEmpty() ? Ui.ORANGE : Ui.GREEN, 1));
        if (req != null) summary.addView(Ui.text(this, "Rule: " + req.rule, 13, Ui.MUTED, 0));
        root.addView(summary);

        EditText search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search within results");
        Ui.mb(this, search, 8);
        root.addView(search);

        LinearLayout actions = Ui.row(this);
        Button csv = Ui.secondary(this, "Copy CSV");
        actions.addView(csv, new LinearLayout.LayoutParams(0, -2, 1));
        Button back = Ui.secondary(this, "Back");
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, -2, 1);
        blp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions.addView(back, blp);
        Ui.mb(this, actions, 8);
        root.addView(actions);

        adapter = new PumpListAdapter(this);
        adapter.setItems(all);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            public void onTextChanged(CharSequence s, int st, int before, int count) { filter(s.toString()); }
            public void afterTextChanged(Editable e) {}
        });
        list.setOnItemClickListener((p, v, pos, id) -> openDetails(adapter.getResult(pos)));
        back.setOnClickListener(v -> finish());
        csv.setOnClickListener(v -> copyCsv());
    }

    void filter(String q) {
        if (q == null || q.trim().isEmpty()) { adapter.setItems(all); return; }
        ArrayList<PumpSelector.Result> out = new ArrayList<>();
        for (PumpSelector.Result r : all) if (PumpSelector.kw(r.r, q)) out.add(r);
        adapter.setItems(out);
    }

    void openDetails(PumpSelector.Result r) {
        Intent i = new Intent(this, PumpDetailsActivity.class);
        i.putExtra("id", r.r.id);
        i.putExtra("head", r.head);
        i.putExtra("flow", r.flow);
        i.putExtra("estimate", r.estimate);
        startActivity(i);
    }

    void copyCsv() {
        StringBuilder sb = new StringBuilder("Model,HP,kW,Phase,Estimated LPH,Head,Category\n");
        for (PumpSelector.Result x : all) {
            PumpRecord r = x.r;
            sb.append('"').append(r.model.replace("\"","\"\"")).append("\",")
              .append(r.hp).append(',').append(r.kw).append(',')
              .append('"').append(r.phase).append("\",")
              .append(String.format(Locale.US, "%.0f", x.flow)).append(',')
              .append(String.format(Locale.US, "%.1f", x.head)).append(',')
              .append('"').append(r.category.replace("\"","\"\"")).append("\"\n");
        }
        ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("granpa-results.csv", sb.toString()));
        Toast.makeText(this, "CSV copied", Toast.LENGTH_SHORT).show();
    }
}
