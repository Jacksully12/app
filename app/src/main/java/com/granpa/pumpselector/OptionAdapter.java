package com.granpa.pumpselector;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class OptionAdapter extends ArrayAdapter<Option> {
    private final Activity a;
    private final List<Option> opts;

    public OptionAdapter(Activity a, List<Option> o) {
        super(a, 0, o);
        this.a = a;
        this.opts = o;
    }

    @Override
    public int getCount() {
        return opts.size();
    }

    @Override
    public Option getItem(int position) {
        return opts.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return buildRow(position, false);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return buildRow(position, true);
    }

    private View buildRow(int position, boolean dropdown) {
        Option o = getItem(position);

        LinearLayout box = new LinearLayout(a);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);

        int padH = Ui.dp(a, dropdown ? 16 : 14);
        int padV = Ui.dp(a, dropdown ? 12 : 10);
        box.setPadding(padH, padV, padH, padV);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        if (dropdown) {
            lp.setMargins(Ui.dp(a, 8), Ui.dp(a, o.mainCategory ? 6 : 2), Ui.dp(a, 8), Ui.dp(a, o.mainCategory ? 4 : 2));
        }
        box.setLayoutParams(lp);

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(Ui.dp(a, 16));
        bg.setStroke(Ui.dp(a, 1), Ui.BORDER);
        if (dropdown) {
            bg.setColor(o.mainCategory ? Color.rgb(245, 249, 255) : Color.WHITE);
        } else {
            bg.setColor(Color.WHITE);
        }
        box.setBackground(bg);

        if (dropdown) {
            TextView badge = new TextView(a);
            badge.setText(o.mainCategory ? "MAIN CATEGORY" : "SUB CATEGORY");
            badge.setTextSize(11);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextColor(o.mainCategory ? Ui.BLUE : Ui.MUTED);
            badge.setLetterSpacing(0.04f);
            LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            badgeLp.bottomMargin = Ui.dp(a, 4);
            badge.setLayoutParams(badgeLp);
            box.addView(badge);
        }

        TextView title = new TextView(a);
        title.setText(dropdown && !o.mainCategory ? "↳ " + safe(o.label) : safe(o.label));
        title.setTextSize(dropdown ? 18 : 17);
        title.setTextColor(o.mainCategory ? Ui.BLUE : Ui.TEXT);
        title.setTypeface(Typeface.DEFAULT, o.mainCategory ? Typeface.BOLD : Typeface.NORMAL);
        title.setSingleLine(false);
        box.addView(title);

        if (!safe(o.detail).isEmpty()) {
            TextView detail = new TextView(a);
            detail.setText(safe(o.detail));
            detail.setTextSize(dropdown ? 14 : 13);
            detail.setTextColor(Ui.MUTED);
            detail.setSingleLine(false);
            LinearLayout.LayoutParams detailLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            detailLp.topMargin = Ui.dp(a, 3);
            if (dropdown && !o.mainCategory) detailLp.leftMargin = Ui.dp(a, 14);
            detail.setLayoutParams(detailLp);
            box.addView(detail);
        }

        return box;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
