package com.granpa.pumpselector;

import android.app.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PumpListAdapter extends BaseAdapter {
    Activity a;
    ArrayList<PumpSelector.Result> items = new ArrayList<>();

    public PumpListAdapter(Activity a) { this.a = a; }

    public void setItems(List<PumpSelector.Result> l) {
        items.clear();
        if (l != null) items.addAll(l);
        notifyDataSetChanged();
    }

    public PumpSelector.Result getResult(int p) { return items.get(p); }
    public int getCount() { return items.size(); }
    public Object getItem(int p) { return items.get(p); }
    public long getItemId(int p) { return p; }

    public View getView(int p, View v, ViewGroup parent) {
        PumpSelector.Result it = items.get(p);
        PumpRecord r = it.r;

        LinearLayout c = Ui.card(a);
        c.setPadding(Ui.dp(a, 14), Ui.dp(a, 14), Ui.dp(a, 14), Ui.dp(a, 14));

        c.addView(Ui.text(a, safe(r.model), 22, Ui.TEXT, Typeface.BOLD));
        c.addView(Ui.text(a,
                PumpSelector.trim(r.hp) + " HP  •  " + PumpSelector.trim(r.kw) + " kW  •  Phase " + phaseShort(r.phase),
                15, Ui.MUTED, Typeface.NORMAL));

        TextView type = Ui.text(a, safe(r.category), 16, Ui.BLUE, Typeface.BOLD);
        c.addView(type);

        if (it.estimate) {
            c.addView(Ui.text(a,
                    "At " + PumpSelector.head(it.head) + ": " + PumpSelector.lph(it.flow) + "  •  " + it.status,
                    17, Ui.GREEN, Typeface.BOLD));
        } else {
            c.addView(Ui.text(a,
                    "Head: " + safe(r.headRangeText) + " m  •  Flow: " + safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal),
                    15, Ui.MUTED, Typeface.NORMAL));
        }

        String bottom = "Page " + r.page + "  •  Size " + dashIfEmpty(r.size);
        if (!safe(r.brand).isEmpty()) bottom += "  •  " + safe(r.brand);
        c.addView(Ui.text(a, bottom, 15, Ui.MUTED, Typeface.NORMAL));

        return c;
    }

    private String phaseShort(String phase) {
        String p = safe(phase).toUpperCase(Locale.US);
        if (p.contains("S")) return "S";
        if (p.contains("T") || p.contains("3")) return "T";
        return p.isEmpty() ? "-" : p;
    }

    private String safe(String s) { return s == null ? "" : s.trim(); }
    private String dashIfEmpty(String s) { return safe(s).isEmpty() ? "-" : safe(s); }
}
