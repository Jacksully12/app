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
    HashSet<String> collapsedGroups = new HashSet<>();
    double head;
    String unit = "LPM";
    String asset = PumpRepository.TEXMO_ASSET;
    String brand = "TEXMO";
    String selectedCat = "all";
    String selectedPhase = "any";
    String reqLabel = "";
    String currentQuery = "";
    TextView resultCount;
    TextView emptyMessage;

    protected void onCreate(Bundle b) {
        super.onCreate(b);

        Intent in = getIntent();
        head = in.getDoubleExtra("head", Double.NaN);
        unit = PumpSelector.normalizeUnit(in.getStringExtra("unit"));
        asset = PumpRepository.normalizeAsset(in.getStringExtra("asset"));
        brand = in.getStringExtra("brand");
        if (brand == null || brand.trim().isEmpty()) brand = PumpRepository.brandName(asset);
        boolean range = in.getBooleanExtra("range", false);
        double f1 = in.getDoubleExtra("flow1", Double.NaN), f2 = in.getDoubleExtra("flow2", f1);

        PumpSelector.Req req = PumpSelector.req(range, f1, f2, unit);
        selectedCat = in.getStringExtra("cat");
        selectedPhase = in.getStringExtra("phase");
        reqLabel = req == null ? "" : req.label;
        if ("all".equals(selectedCat)) {
            all = PumpSelector.selectAllMainGroups(PumpRepository.getRecords(this, asset), head, req, selectedPhase, in.getStringExtra("key"));
        } else {
            all = PumpSelector.select(PumpRepository.getRecords(this, asset), head, req, selectedCat, selectedPhase, in.getStringExtra("key"));
        }

        LinearLayout root = Ui.root(this);

        LinearLayout sum = Ui.card(this);
        sum.addView(Ui.text(this, brand + " Results", 26, Ui.TEXT, 1));
        sum.addView(Ui.text(this, PumpSelector.head(head) + " fixed head • " + (req == null ? "Invalid rule" : req.label), 14, Ui.MUTED, 0));
        int matchCount = PumpSelector.realCount(all);
        resultCount = Ui.text(this, matchCount + " matching models", 24, matchCount == 0 ? Ui.ORANGE : Ui.GREEN, 1);
        sum.addView(resultCount);
        // Keep the results header clean for dealer use. Detailed rule labels remain on result cards.
        root.addView(sum);

        EditText search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search within results, e.g. ACS1125");
        Ui.mb(this, search, 10);
        root.addView(search);

        LinearLayout actions = Ui.row(this);
        Button csv = Ui.secondary(this, "Copy Results");
        actions.addView(csv, new LinearLayout.LayoutParams(0, -2, 1));

        Button back = Ui.secondary(this, "Back to search");
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, -2, 1);
        bp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        actions.addView(back, bp);
        Ui.mb(this, actions, 10);
        root.addView(actions);

        emptyMessage = Ui.text(this, "No nearby model found even up to the ±50% fallback band. Try another flow value, pump type, phase, or range mode.", 15, Ui.MUTED, 0);
        emptyMessage.setVisibility(matchCount == 0 ? View.VISIBLE : View.GONE);
        root.addView(emptyMessage);

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

        list.setOnItemClickListener((p, v, pos, id) -> {
            PumpSelector.Result r = adapter.getResult(pos);
            if (r != null && r.header) {
                String base = baseTitle(r.groupTitle);
                if (collapsedGroups.contains(base)) collapsedGroups.remove(base);
                else collapsedGroups.add(base);
                filter(search.getText().toString());
                return;
            }
            openDetails(r);
        });
        back.setOnClickListener(v -> finish());
        csv.setOnClickListener(v -> copyResults());
    }

    void filter(String q) {
        String query = q == null ? "" : q.trim();
        currentQuery = query;

        if (!containsHeaders()) {
            ArrayList<PumpSelector.Result> out = new ArrayList<>();
            if (query.isEmpty()) {
                out.addAll(all);
            } else {
                for (PumpSelector.Result r : all) {
                    if (r.r != null && PumpSelector.kw(r.r, query)) out.add(r);
                }
            }

            int count = PumpSelector.realCount(out);
            updateResultStatus(count, !query.isEmpty());
            adapter.setCollapsedGroups(collapsedGroups);
            adapter.setItems(out);
            return;
        }

        ArrayList<PumpSelector.Result> out = new ArrayList<>();
        PumpSelector.Result pendingHeader = null;
        ArrayList<PumpSelector.Result> groupMatches = new ArrayList<>();
        int modelCount = 0;

        for (PumpSelector.Result r : all) {
            if (r.header) {
                modelCount += flushGroup(out, pendingHeader, groupMatches);
                pendingHeader = r;
                groupMatches = new ArrayList<>();
                continue;
            }

            if (query.isEmpty() || (r.r != null && PumpSelector.kw(r.r, query))) {
                groupMatches.add(r);
            }
        }
        modelCount += flushGroup(out, pendingHeader, groupMatches);

        updateResultStatus(modelCount, !query.isEmpty());
        adapter.setCollapsedGroups(collapsedGroups);
        adapter.setItems(out);
    }

    void updateResultStatus(int count, boolean filtered) {
        if (resultCount != null) {
            resultCount.setText(count + " matching models");
            resultCount.setTextColor(count == 0 ? Ui.ORANGE : Ui.GREEN);
        }
        if (emptyMessage != null) {
            if (count == 0) {
                emptyMessage.setText(filtered
                        ? "No matching model found in these results."
                        : "No nearby model found even up to the ±50% fallback band. Try another flow value, pump type, phase, or range mode.");
                emptyMessage.setVisibility(View.VISIBLE);
            } else {
                emptyMessage.setVisibility(View.GONE);
            }
        }
    }

    boolean containsHeaders() {
        for (PumpSelector.Result r : all) if (r.header) return true;
        return false;
    }

    int flushGroup(ArrayList<PumpSelector.Result> out, PumpSelector.Result header, List<PumpSelector.Result> matches) {
        if (header == null || matches == null || matches.isEmpty()) return 0;
        String base = baseTitle(header.groupTitle);
        out.add(PumpSelector.header(base + " • " + matches.size() + " models"));
        if (!collapsedGroups.contains(base)) out.addAll(matches);
        return matches.size();
    }

    String baseTitle(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+•\\s+\\d+\\s+models?$", "").trim();
    }

    void openDetails(PumpSelector.Result r) {
        if (r == null || r.header || r.r == null) return;
        Intent i = new Intent(this, PumpDetailsActivity.class);
        i.putExtra("id", r.r.id);
        i.putExtra("head", r.head);
        i.putExtra("flow", r.flow);
        i.putExtra("estimate", r.estimate);
        i.putExtra("unit", unit);
        i.putExtra("asset", asset);
        i.putExtra("brand", brand);
        startActivity(i);
    }

    void copyResults() {
        ArrayList<PumpSelector.Result> rows = copyRows();
        StringBuilder sb = new StringBuilder();

        sb.append("Granpa ").append(brand).append(" Pump Selector Results\n\n");
        sb.append("Input\n");
        sb.append("-----\n");
        sb.append("Head: ").append(PumpSelector.head(head)).append("\n");
        if (!safe(reqLabel).isEmpty()) sb.append("Flow: ").append(reqLabel.replace("Fixed flow ", "").replace("Flow range ", "")).append("\n");
        sb.append("Pump Type: ").append(categoryLabel(selectedCat)).append("\n");
        sb.append("Phase: ").append(phaseLabel(selectedPhase)).append("\n");
        if (!safe(currentQuery).isEmpty()) sb.append("Search: ").append(currentQuery).append("\n");
        sb.append("\n");

        if (PumpSelector.realCount(rows) == 0) {
            sb.append("No matching models found.\n");
        } else {
            int n = 1;
            boolean grouped = false;
            for (PumpSelector.Result x : rows) {
                if (x.header) {
                    grouped = true;
                    n = 1;
                    sb.append("\n").append(baseTitle(x.groupTitle)).append("\n");
                    sb.append("--------------------\n");
                    continue;
                }

                if (!grouped && n == 1) {
                    sb.append("Recommended Models\n");
                    sb.append("------------------\n");
                }

                PumpRecord r = x.r;
                sb.append(n++).append(") ").append(safe(r.model)).append("\n");
                sb.append("Category: ").append(safe(r.category)).append("\n");
                sb.append("Power: ").append(PumpSelector.trim(r.hp)).append(" HP / ").append(PumpSelector.trim(r.kw)).append(" kW\n");
                sb.append("Phase: ").append(phaseText(r.phase)).append("\n");
                if (x.estimate) {
                    sb.append("At ").append(PumpSelector.head(x.head)).append(": ").append(PumpSelector.formatFlow(x.flow, unit)).append("\n");
                    sb.append("Match: ").append(safe(x.status)).append("\n");
                } else {
                    sb.append("Head Range: ").append(safe(r.headRangeText)).append(" m\n");
                    sb.append("Flow Range: ").append(safe(r.dischargeRangeText)).append(" ").append(safe(r.flowUnitOriginal)).append("\n");
                }
                sb.append("Page: ").append(r.page).append("\n");
                sb.append("Size: ").append(safe(r.size).isEmpty() ? "-" : safe(r.size)).append("\n\n");
            }
        }

        ((android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE))
                .setPrimaryClip(ClipData.newPlainText("granpa-results.txt", sb.toString().trim()));
        Toast.makeText(this, "Results copied as text", Toast.LENGTH_SHORT).show();
    }

    ArrayList<PumpSelector.Result> copyRows() {
        String query = safe(currentQuery);
        if (query.isEmpty()) return new ArrayList<>(all);

        ArrayList<PumpSelector.Result> rows = new ArrayList<>();
        PumpSelector.Result pendingHeader = null;
        boolean headerAdded = false;

        for (PumpSelector.Result r : all) {
            if (r.header) {
                pendingHeader = r;
                headerAdded = false;
                continue;
            }

            if (r.r != null && PumpSelector.kw(r.r, query)) {
                if (pendingHeader != null && !headerAdded) {
                    rows.add(PumpSelector.header(baseTitle(pendingHeader.groupTitle)));
                    headerAdded = true;
                }
                rows.add(r);
            }
        }
        return rows;
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

    String phaseText(String p) {
        p = safe(p).toUpperCase(Locale.US);
        boolean s = p.contains("S");
        boolean t = p.contains("T") || p.contains("3");
        if (s && t) return "Single / Three phase";
        if (s) return "Single phase";
        if (t) return "Three phase";
        return p.isEmpty() ? "-" : p;
    }

    String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
