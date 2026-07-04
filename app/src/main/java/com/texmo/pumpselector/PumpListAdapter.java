package com.texmo.pumpselector;

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
    private final List<PumpSelector.Result> items = new ArrayList<>();

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

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        PumpSelector.Result item = items.get(position);
        PumpRecord r = item.record;
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(activity, 14);
        card.setPadding(pad, pad, pad, pad);
        card.setBackground(Ui.roundRect(activity, Ui.CARD, Ui.BORDER, 16));

        TextView title = Ui.text(activity, r.model == null || r.model.isEmpty() ? "-" : r.model, 18, Ui.TEXT, Typeface.BOLD);
        card.addView(title);

        String hp = Double.isNaN(r.hp) ? "-" : trim(r.hp) + " HP";
        String kw = Double.isNaN(r.kw) ? "-" : trim(r.kw) + " kW";
        String meta = hp + "  •  " + kw + "  •  Phase " + safe(r.phase, "-");
        TextView metaView = Ui.text(activity, meta, 13, Ui.MUTED, Typeface.NORMAL);
        card.addView(metaView);

        String category = safe(r.category, "-");
        TextView catView = Ui.text(activity, category, 13, Ui.BLUE, Typeface.BOLD);
        card.addView(catView);

        if (item.hasEstimate) {
            TextView flow = Ui.text(activity, "At " + PumpSelector.formatHead(item.matchedHead) + ": " + PumpSelector.formatLPH(item.estimatedFlowLPH) + "  •  " + item.status, 14, Ui.GREEN, Typeface.BOLD);
            card.addView(flow);
        } else {
            TextView ranges = Ui.text(activity, "Head: " + safe(r.headRangeText, "-") + " m  •  Discharge: " + safe(r.dischargeRangeText, "-") + " " + safe(r.flowUnitOriginal, ""), 13, Ui.MUTED, Typeface.NORMAL);
            card.addView(ranges);
        }

        String bottom = "Page " + r.page;
        if (r.size != null && !r.size.trim().isEmpty()) bottom += "  •  Size " + r.size;
        if (r.brand != null && !r.brand.trim().isEmpty()) bottom += "  •  " + r.brand;
        TextView page = Ui.text(activity, bottom, 12, Ui.MUTED, Typeface.NORMAL);
        card.addView(page);

        LinearLayout wrapper = new LinearLayout(activity);
        wrapper.setPadding(Ui.dp(activity, 0), Ui.dp(activity, 4), Ui.dp(activity, 0), Ui.dp(activity, 8));
        wrapper.addView(card, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return wrapper;
    }

    private String safe(String s, String fallback) {
        return s == null || s.trim().isEmpty() ? fallback : s.trim();
    }

    private String trim(double v) {
        if (Math.abs(v - Math.round(v)) < 0.00001) return String.format(Locale.US, "%.0f", v);
        return String.format(Locale.US, "%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
