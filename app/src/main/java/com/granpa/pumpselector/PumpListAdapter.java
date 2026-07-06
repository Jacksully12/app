package com.granpa.pumpselector;

import android.app.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PumpListAdapter extends BaseAdapter {
    Activity a;
    ArrayList<PumpSelector.Result> items = new ArrayList<>();
    String displayUnit = "LPH";

    public PumpListAdapter(Activity a) { this.a = a; }

    public void setDisplayUnit(String unit) {
        displayUnit = PumpSelector.normalizeUnit(unit);
        notifyDataSetChanged();
    }

    public void setItems(List<PumpSelector.Result> l) {
        items.clear();
        if (l != null) items.addAll(l);
        notifyDataSetChanged();
    }

    public PumpSelector.Result getResult(int p) { return items.get(p); }
    public int getCount() { return items.size(); }
    public Object getItem(int p) { return items.get(p); }
    public long getItemId(int p) { return p; }

    public View getView(int pos, View cv, ViewGroup parent) {
        PumpSelector.Result it = items.get(pos);

        if (it.header) {
            LinearLayout h = Ui.card(a);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, pos == 0 ? 0 : Ui.dp(a, 18), 0, Ui.dp(a, 10));
            h.setLayoutParams(lp);
            h.setPadding(Ui.dp(a, 16), Ui.dp(a, 14), Ui.dp(a, 16), Ui.dp(a, 12));
            h.setBackground(Ui.bg(a, Color.rgb(245, 249, 255), Ui.BORDER, 20));

            TextView tag = new TextView(a);
            tag.setText("CATEGORY");
            tag.setTextSize(11);
            tag.setTextColor(Ui.MUTED);
            tag.setTypeface(Typeface.DEFAULT_BOLD);
            h.addView(tag);

            TextView title = Ui.text(a, safe(it.groupTitle), 19, Ui.BLUE, Typeface.BOLD);
            LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(-1, -2);
            tlp.setMargins(0, Ui.dp(a, 4), 0, 0);
            title.setLayoutParams(tlp);
            h.addView(title);
            return h;
        }

        PumpRecord r = it.r;
        String u = it.unit == null ? displayUnit : it.unit;

        LinearLayout c = Ui.card(a);
        c.setPadding(Ui.dp(a, 14), Ui.dp(a, 14), Ui.dp(a, 14), Ui.dp(a, 14));

        c.addView(Ui.text(a, safe(r.model), 22, Ui.TEXT, Typeface.BOLD));
        c.addView(Ui.text(a, PumpSelector.trim(r.hp) + " HP  •  " + PumpSelector.trim(r.kw) + " kW  •  Phase " + phaseShort(r.phase), 15, Ui.MUTED, Typeface.NORMAL));
        c.addView(Ui.text(a, safe(r.category), 16, Ui.BLUE, Typeface.BOLD));

        if (it.estimate) {
            int color = matchColor(it.status);
            c.addView(Ui.text(a, "At " + PumpSelector.head(it.head) + ": " + PumpSelector.formatFlow(it.flow, u) + "  •  " + it.status, 17, color, Typeface.BOLD));
        } else {
            c.addView(Ui.text(a, "Head: " + safe(r.headRangeText) + " m  •  Flow: " + rangeFlow(r, u), 15, Ui.MUTED, Typeface.NORMAL));
        }

        String bottom = "Page " + r.page + "  •  Size " + dash(r.size);
        if (!safe(r.brand).isEmpty()) bottom += "  •  " + safe(r.brand);
        c.addView(Ui.text(a, bottom, 15, Ui.MUTED, Typeface.NORMAL));

        return c;
    }

    String rangeFlow(PumpRecord r, String unit) {
        if (!Double.isNaN(r.minFlowLPH) && !Double.isNaN(r.maxFlowLPH)) {
            return PumpSelector.formatFlowNumber(PumpSelector.fromLPH(r.minFlowLPH, unit), unit) + " – " + PumpSelector.formatFlow(r.maxFlowLPH, unit);
        }
        return safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal);
    }

    String phaseShort(String p) {
        p = safe(p).toUpperCase(Locale.US);
        boolean s = p.contains("S");
        boolean t = p.contains("T") || p.contains("3");
        if (s && t) return "S/T";
        if (s) return "S";
        if (t) return "T";
        return p.isEmpty() ? "-" : p;
    }

    int matchColor(String status) {
        String s = status == null ? "" : status;
        if (s.contains("Last option")) return Ui.ORANGE;
        if (s.contains("Wide match")) return Ui.ORANGE;
        if (s.contains("Extended match")) return Ui.BLUE;
        return Ui.GREEN;
    }

    String safe(String s) { return s == null ? "" : s.trim(); }
    String dash(String s) { s = safe(s); return s.isEmpty() ? "-" : s; }
}
