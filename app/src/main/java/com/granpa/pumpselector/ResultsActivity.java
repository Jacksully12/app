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
    String unit = "LPM";

    protected void onCreate(Bundle b) {
        super.onCreate(b);

        Intent in = getIntent();
        head = in.getDoubleExtra("head", Double.NaN);
        unit = PumpSelector.normalizeUnit(in.getStringExtra("unit"));
        boolean range = in.getBooleanExtra("range", false);
        double f1 = in.getDoubleExtra("flow1", Double.NaN), f2 = in.getDoubleExtra("flow2", f1);

        PumpSelector.Req req = PumpSelector.req(range, f1, f2, unit);
        String selectedCat = in.getStringExtra("cat");
        if ("all".equals(selectedCat)) {
            all = PumpSelector.selectAllMainGroups(PumpRepository.getRecords(this), head, req, in.getStringExtra("phase"), in.getStringExtra("key"));
        } else {
            all = PumpSelector.select(PumpRepository.getRecords(this), head, req, selectedCat, in.getStringExtra("phase"), in.getStringExtra("key"));
        }

        LinearLayout root = Ui.root(this);

        LinearLayout sum = Ui.card(this);
        sum.addView(Ui.text(this, "Results", 26, Ui.TEXT, 1));
        sum.addView(Ui.text(this, PumpSelector.head(head) + " fixed head • " + (req == null ? "Invalid rule" : req.label), 14, Ui.MUTED, 0));
        int matchCount = PumpSelector.realCount(all);
        sum.addView(Ui.text(this, matchCount + " matching models", 24, matchCount == 0 ? Ui.ORANGE : Ui.GREEN, 1));
        if (req != null) sum.addView(Ui.text(this, "Rule: " + req.rule, 14, Ui.MUTED, 0));
        if (req != null && !range) sum.addView(Ui.text(this, "Shown result limit: maximum 2 above target + 2 below target. Wider matches are labelled clearly.", 13, Ui.BLUE, 0));
        if ("all".equals(in.getStringExtra("cat"))) {
            sum.addView(Ui.text(this, "All pump types is grouped category-wise, so one category cannot hide another category.", 13, Ui.BLUE, 0));
        }
        root.addView(sum);

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

        TextView empty = Ui.text(this, "No nearby model found even up to the ±50% fallback band. Try another flow value, pump type, phase, or range mode.", 15, Ui.MUTED, 0);
        if (matchCount == 0) root.addView(empty);

        adapter = new PumpListAdapter(this);
        adapter.setDisplayUnit(unit);
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
        if (q == null || q.trim().isEmpty()) {
            adapter.setItems(all);
            return;
        }

        ArrayList<PumpSelector.Result> out = new ArrayList<>();
        PumpSelector.Result pendingHeader = null;
        boolean headerAdded = false;

        for (PumpSelector.Result r : all) {
            if (r.header) {
                pendingHeader = r;
                headerAdded = false;
                continue;
            }

            if (r.r != null && PumpSelector.kw(r.r, q)) {
                if (pendingHeader != null && !headerAdded) {
                    out.add(pendingHeader);
                    headerAdded = true;
                }
                out.add(r);
            }
        }
        adapter.setItems(out);
    }

    void openDetails(PumpSelector.Result r) {
        if (r == null || r.header || r.r == null) return;
        Intent i = new Intent(this, PumpDetailsActivity.class);
        i.putExtra("id", r.r.id);
        i.putExtra("head", r.head);
        i.putExtra("flow", r.flow);
        i.putExtra("estimate", r.estimate);
        i.putExtra("unit", unit);
        startActivity(i);
    }

    void copyCsv() {
        String u = PumpSelector.unitLabel(unit);
        StringBuilder sb = new StringBuilder("Model,HP,kW,Phase,Estimated " + u + ",Head,Category,Match,Page,Size,Brand\n");
        for (PumpSelector.Result x : all) {
            if (x.header || x.r == null) continue;
            PumpRecord r = x.r;
            sb.append(q(r.model)).append(',')
                    .append(r.hp).append(',')
                    .append(r.kw).append(',')
                    .append(q(r.phase)).append(',')
                    .append(String.format(Locale.US, "%.2f", PumpSelector.fromLPH(x.flow, unit))).append(',')
                    .append(String.format(Locale.US, "%.1f", x.head)).append(',')
                    .append(q(r.category)).append(',')
                    .append(q(x.status)).append(',')
                    .append(r.page).append(',')
                    .append(q(r.size)).append(',')
                    .append(q(r.brand)).append('\n');
        }
        ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("granpa-results.csv", sb.toString()));
        Toast.makeText(this, "CSV copied in " + u, Toast.LENGTH_SHORT).show();
    }

    String q(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }
}
