package com.granpa.pumpselector;

import android.app.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class OptionAdapter extends ArrayAdapter<Option> {
    Activity a;
    List<Option> opts;

    public OptionAdapter(Activity a, List<Option> o) {
        super(a, android.R.layout.simple_spinner_item, o);
        this.a = a;
        opts = o;
    }

    public View getView(int pos, View convert, ViewGroup parent) {
        return itemView(pos, false, convert);
    }

    public View getDropDownView(int pos, View convert, ViewGroup parent) {
        return itemView(pos, true, convert);
    }

    private View itemView(int pos, boolean dropdown, View convert) {
        Option it = opts.get(pos);

        LinearLayout box = convert instanceof LinearLayout ? (LinearLayout) convert : new LinearLayout(a);
        box.removeAllViews();
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(
                Ui.dp(a, 14),
                Ui.dp(a, dropdown ? 12 : 8),
                Ui.dp(a, 14),
                Ui.dp(a, dropdown ? 12 : 8)
        );
        box.setMinimumHeight(Ui.dp(a, dropdown ? 76 : 62));

        // Same dropdown layout as before, only professionally colorised.
        if (dropdown) {
            box.setBackgroundColor(it.mainCategory ? Color.rgb(241, 247, 253) : Color.WHITE);
        } else {
            box.setBackgroundColor(Color.WHITE);
        }

        TextView title = Ui.text(
                a,
                it.label,
                16,
                it.mainCategory ? Ui.BLUE : Ui.TEXT,
                it.mainCategory ? Typeface.BOLD : Typeface.NORMAL
        );
        title.setSingleLine(false);
        box.addView(title);

        if (it.detail != null && !it.detail.isEmpty()) {
            TextView d = Ui.text(
                    a,
                    it.detail,
                    12,
                    it.mainCategory ? Color.rgb(79, 104, 130) : Ui.MUTED,
                    Typeface.NORMAL
            );
            d.setSingleLine(false);
            box.addView(d);
        }

        return box;
    }
}
