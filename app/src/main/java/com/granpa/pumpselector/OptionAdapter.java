package com.granpa.pumpselector;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class OptionAdapter extends ArrayAdapter<Option> {
    private final Activity activity;
    private final List<Option> options;

    public OptionAdapter(Activity activity, List<Option> options) {
        super(activity, android.R.layout.simple_spinner_item, options);
        this.activity = activity;
        this.options = options;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Option item = options.get(position);
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(Ui.dp(activity, 14), Ui.dp(activity, 8), Ui.dp(activity, 14), Ui.dp(activity, 8));
        box.setMinimumHeight(Ui.dp(activity, 58));

        TextView title = Ui.text(activity, item.label, 16, Ui.TEXT, Typeface.BOLD);
        title.setSingleLine(false);
        box.addView(title);

        if (!item.detail.isEmpty()) {
            TextView detail = Ui.text(activity, item.detail, 12, Ui.MUTED, Typeface.NORMAL);
            detail.setSingleLine(false);
            box.addView(detail);
        }
        return box;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Option item = options.get(position);
        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(Ui.dp(activity, 16), Ui.dp(activity, 12), Ui.dp(activity, 16), Ui.dp(activity, 12));
        box.setMinimumHeight(Ui.dp(activity, 74));
        box.setBackgroundColor(android.graphics.Color.WHITE);

        TextView title = Ui.text(activity, item.label, 16, item.mainCategory ? Ui.BLUE : Ui.TEXT, Typeface.BOLD);
        title.setSingleLine(false);
        box.addView(title);

        if (!item.detail.isEmpty()) {
            TextView detail = Ui.text(activity, item.detail, 12, Ui.MUTED, Typeface.NORMAL);
            detail.setSingleLine(false);
            box.addView(detail);
        }
        return box;
    }
}
