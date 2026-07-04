package com.granpa.pumpselector;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PumpListAdapter extends BaseAdapter {
    private final Activity activity;
    private final ArrayList<PumpSelector.Result> items = new ArrayList<>();

    public PumpListAdapter(Activity activity) {
        this.activity = activity;
    }

    public void setItems(List<PumpSelector.Result> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public PumpSelector.Result getResult(int position) {
        return items.get(position);
    }

    @Override public int getCount() { return items.size(); }
    @Override public Object getItem(int position) { return items.get(position); }
    @Override public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PumpSelector.Result item = items.get(position);
        PumpRecord r = item.r;

        LinearLayout card = Ui.card(activity);
        card.setPadding(Ui.dp(activity, 14), Ui.dp(activity, 14), Ui.dp(activity, 14), Ui.dp(activity, 14));

        card.addView(Ui.text(activity, safe(r.model), 22, Ui.TEXT, Typeface.BOLD));
        card.addView(Ui.text(activity,
                PumpSelector.trim(r.hp) + " HP  •  " + PumpSelector.trim(r.kw) + " kW  •  Phase " + phaseShort(r.phase),
                15, Ui.MUTED, Typeface.NORMAL));

        card.addView(Ui.text(activity, safe(r.category), 16, Ui.BLUE, Typeface.BOLD));

        if (item.estimate) {
            card.addView(Ui.text(activity,
                    "At " + PumpSelector.head(item.head) + ": " + PumpSelector.lph(item.flow) + "  •  " + item.status,
                    17, Ui.GREEN, Typeface.BOLD));
        } else {
            card.addView(Ui.text(activity,
                    "Head: " + safe(r.headRangeText) + " m  •  Flow: " + safe(r.dischargeRangeText) + " " + safe(r.flowUnitOriginal),
                    15, Ui.MUTED, Typeface.NORMAL));
        }

        String bottom = "Page " + r.page + "  •  Size " + dashIfEmpty(r.size);
        if (!safe(r.brand).isEmpty()) bottom += "  •  " + safe(r.brand);
        card.addView(Ui.text(activity, bottom, 15, Ui.MUTED, Typeface.NORMAL));

        return card;
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
