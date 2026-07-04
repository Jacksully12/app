package com.granpa.pumpselector;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class ResultsActivity extends Activity {
    private PumpListAdapter adapter;
    private final ArrayList<PumpSelector.Result> all = new ArrayList<>();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        Intent in = getIntent();
        double head = in.getDoubleExtra("head", Double.NaN);
        boolean range = in.getBooleanExtra("range", false);
        double f1 = in.getDoubleExtra("flow1", Double.NaN);
        double f2 = in.getDoubleExtra("flow2", f1);
        String unit = in.getStringExtra("unit");
        String cat = in.getStringExtra("cat");
        String phase = in.getStringExtra("phase");
        String key = in.getStringExtra("key");

        PumpSelector.Req req = PumpSelector.req(range, f1, f2, unit);
        all.addAll(PumpSelector.select(PumpRepository.getRecords(this), head, req, cat, phase, key));

        LinearLayout root = Ui.root(this);
        LinearLayout summary = Ui.card(this);
        summary.addView(Ui.text(this, "Results", 25, Ui.TEXT, 1));
        summary.addView(Ui.text(this, PumpSelector.head(head) + " fixed head • " + (req == null ? "-" : req.label), 14, Ui.MUTED, 0));
        summary.addView(Ui.text(this, all.size() + " matching models", 22, all.isEmpty() ? Ui.ORANGE : Ui.GREEN, 1));
        if (req != null) summary.addView(Ui.text(this, "Rule: " + req.rule, 13, Ui.MUTED, 0));
        root.addView(summary);

        EditText search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search within results, e.g. ACS1125");
        Ui.mb(this, search, 10);
        root.addView(search);

        LinearLayout actions = Ui.row(this);
        Button csv = Ui.secondary(this, "Copy CSV");
        actions.addView(csv, new LinearLayout.LayoutParams(0, -2, 1));
        Button back = Ui.secondary(this, "Back to search");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions.addView(back, bp);
        Ui.mb(this, actions, 10);
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
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable e) {}
        });

        list.setOnItemClickListener((p, v, pos, id) -> openDetails(adapter.getResult(pos)));
        back.setOnClickListener(v -> finish());
        csv.setOnClickListener(v -> copyCsv());
    }

    private void filter(String q) {
        if (q == null || q.trim().isEmpty()) {
            adapter.setItems(all);
            return;
        }
        ArrayList<PumpSelector.Result> out = new ArrayList<>();
        for (PumpSelector.Result r : all) {
            if (PumpSelector.kw(r.r, q)) out.add(r);
        }
        adapter.setItems(out);
    }

    private void openDetails(PumpSelector.Result r) {
        Intent intent = new Intent(this, PumpDetailsActivity.class);
        intent.putExtra("id", r.r.id);
        intent.putExtra("head", r.head);
        intent.putExtra("flow", r.flow);
        intent.putExtra("estimate", r.estimate);
        startActivity(intent);
    }

    private void copyCsv() {
        StringBuilder sb = new StringBuilder("Model,HP,kW,Phase,Estimated LPH,Head,Category,Page,Size,Brand\n");
        for (PumpSelector.Result x : all) {
            PumpRecord r = x.r;
            sb.append('"').append(r.model.replace("\"", "\"\"")).append("\",")
              .append(r.hp).append(',').append(r.kw).append(',')
              .append('"').append(r.phase).append("\",")
              .append(String.format(Locale.US, "%.0f", x.flow)).append(',')
              .append(String.format(Locale.US, "%.1f", x.head)).append(',')
              .append('"').append(r.category.replace("\"", "\"\"")).append("\",")
              .append(r.page).append(',')
              .append('"').append(r.size.replace("\"", "\"\"")).append("\",")
              .append('"').append(r.brand.replace("\"", "\"\"")).append("\"\n");
        }
        ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("granpa-results.csv", sb.toString()));
        Toast.makeText(this, "CSV copied", Toast.LENGTH_SHORT).show();
    }
}
