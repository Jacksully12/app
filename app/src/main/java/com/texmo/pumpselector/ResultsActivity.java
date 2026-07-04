package com.texmo.pumpselector;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ResultsActivity extends Activity {
    private PumpListAdapter adapter;
    private final List<PumpSelector.Result> allResults = new ArrayList<>();
    private double head;
    private boolean rangeMode;
    private double flow1;
    private double flow2;
    private String unit;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        head = in.getDoubleExtra("head", Double.NaN);
        rangeMode = in.getBooleanExtra("rangeMode", false);
        flow1 = in.getDoubleExtra("flow1", Double.NaN);
        flow2 = in.getDoubleExtra("flow2", flow1);
        unit = in.getStringExtra("unit");
        String category = in.getStringExtra("category");
        String phase = in.getStringExtra("phase");
        String keyword = in.getStringExtra("keyword");

        PumpSelector.FlowRequirement req = PumpSelector.requirement(rangeMode, flow1, flow2, unit);
        allResults.addAll(PumpSelector.select(PumpRepository.getRecords(this), head, req, category, phase, "any", keyword));

        LinearLayout root = Ui.root(this);
        LinearLayout summary = Ui.card(this);
        summary.addView(Ui.text(this, "Results", 25, Ui.TEXT, android.graphics.Typeface.BOLD));
        String query = PumpSelector.formatHead(head) + " fixed head • " + (req == null ? "invalid water rule" : req.label);
        summary.addView(Ui.text(this, query, 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        summary.addView(Ui.text(this, allResults.size() + " matching models", 22, allResults.isEmpty() ? Ui.ORANGE : Ui.GREEN, android.graphics.Typeface.BOLD));
        if (req != null) summary.addView(Ui.text(this, "Rule: " + req.ruleText, 13, Ui.MUTED, android.graphics.Typeface.NORMAL));
        root.addView(summary);

        EditText within = Ui.input(this, "", InputType.TYPE_CLASS_TEXT);
        within.setHint("Search within results, e.g. ACS1125");
        Ui.addMarginBottom(this, within, 8);
        root.addView(within);

        LinearLayout buttonRow = Ui.row(this);
        android.widget.Button copy = Ui.secondaryButton(this, "Copy CSV");
        buttonRow.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        android.widget.Button back = Ui.secondaryButton(this, "Back to search");
        LinearLayout.LayoutParams backLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        backLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        buttonRow.addView(back, backLp);
        Ui.addMarginBottom(this, buttonRow, 8);
        root.addView(buttonRow);

        if (allResults.isEmpty()) {
            TextView empty = Ui.text(this, "No model matched the strict fixed-head rule. Try All pump types, check phase, or use flow range mode.", 15, Ui.MUTED, android.graphics.Typeface.NORMAL);
            root.addView(empty);
        }

        adapter = new PumpListAdapter(this);
        adapter.setItems(allResults);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        setContentView(root);

        within.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterResults(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        list.setOnItemClickListener((parent, view, position, id) -> openDetails(adapter.getResult(position)));
        back.setOnClickListener(v -> finish());
        copy.setOnClickListener(v -> copyCsv());
    }

    private void filterResults(String q) {
        if (q == null || q.trim().isEmpty()) {
            adapter.setItems(allResults);
            return;
        }
        List<PumpSelector.Result> filtered = new ArrayList<>();
        for (PumpSelector.Result r : allResults) {
            if (PumpSelector.keywordMatches(r.record, q)) filtered.add(r);
        }
        adapter.setItems(filtered);
    }

    private void openDetails(PumpSelector.Result result) {
        Intent i = new Intent(this, PumpDetailsActivity.class);
        i.putExtra("recordId", result.record.id);
        i.putExtra("head", head);
        i.putExtra("estimatedFlow", result.estimatedFlowLPH);
        i.putExtra("hasEstimate", result.hasEstimate);
        startActivity(i);
    }

    private void copyCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("Model,HP,kW,Phase,Estimated LPH,Head,Category,Page\n");
        for (PumpSelector.Result r : allResults) {
            PumpRecord p = r.record;
            sb.append(csv(p.model)).append(',')
              .append(p.hp).append(',')
              .append(p.kw).append(',')
              .append(csv(p.phase)).append(',')
              .append(String.format(Locale.US, "%.0f", r.estimatedFlowLPH)).append(',')
              .append(String.format(Locale.US, "%.1f", r.matchedHead)).append(',')
              .append(csv(p.category)).append(',')
              .append(p.page).append('\n');
        }
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("pump_results.csv", sb.toString()));
        Toast.makeText(this, "CSV copied to clipboard", Toast.LENGTH_SHORT).show();
    }

    private String csv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        return "\"" + t + "\"";
    }
}
