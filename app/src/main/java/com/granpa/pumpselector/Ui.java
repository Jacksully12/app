package com.granpa.pumpselector;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

public class Ui {
    public static final int BG = Color.rgb(246, 248, 251);
    public static final int CARD = Color.WHITE;
    public static final int TEXT = Color.rgb(16, 32, 51);
    public static final int MUTED = Color.rgb(96, 112, 133);
    public static final int BORDER = Color.rgb(220, 229, 238);
    public static final int ORANGE = Color.rgb(242, 103, 34);
    public static final int BLUE = Color.rgb(18, 67, 114);
    public static final int GREEN = Color.rgb(17, 132, 82);

    public static int dp(Activity a, int value) {
        return (int) (value * a.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int topSafePadding(Activity a) {
        int status = 0;
        try {
            int id = a.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (id > 0) status = a.getResources().getDimensionPixelSize(id);
        } catch (Exception ignored) {}
        return Math.max(dp(a, 26), status + dp(a, 10));
    }

    public static TextView text(Activity a, String s, int sp, int color, int style) {
        TextView t = new TextView(a);
        t.setText(s == null ? "" : s);
        t.setTextSize(sp);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, style);
        t.setLineSpacing(0, 1.08f);
        return t;
    }

    public static TextView label(Activity a, String s) {
        TextView t = text(a, s, 13, TEXT, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, dp(a, 14), 0, dp(a, 6));
        t.setLayoutParams(lp);
        return t;
    }

    public static GradientDrawable bg(Activity a, int fill, int stroke, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(fill);
        g.setCornerRadius(dp(a, radius));
        g.setStroke(dp(a, 1), stroke);
        return g;
    }

    public static LinearLayout root(Activity a) {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.VERTICAL);
        r.setPadding(dp(a, 16), topSafePadding(a), dp(a, 16), dp(a, 16));
        r.setBackgroundColor(BG);
        return r;
    }

    public static ScrollView scroll(Activity a, View child) {
        ScrollView s = new ScrollView(a);
        s.setFillViewport(false);
        s.addView(child);
        return s;
    }

    public static LinearLayout card(Activity a) {
        LinearLayout c = new LinearLayout(a);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(a, 16), dp(a, 16), dp(a, 16), dp(a, 16));
        c.setBackground(bg(a, CARD, BORDER, 18));
        c.setElevation(dp(a, 1));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(a, 14));
        c.setLayoutParams(lp);
        return c;
    }

    public static LinearLayout row(Activity a) {
        LinearLayout r = new LinearLayout(a);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        return r;
    }

    public static EditText input(Activity a, String value, int inputType) {
        EditText e = new EditText(a);
        e.setText(value == null ? "" : value);
        e.setTextSize(16);
        e.setTextColor(TEXT);
        e.setHintTextColor(Color.rgb(130, 139, 151));
        e.setSingleLine(true);
        e.setInputType(inputType);
        e.setPadding(dp(a, 14), 0, dp(a, 14), 0);
        e.setMinHeight(dp(a, 54));
        e.setBackground(bg(a, Color.WHITE, BORDER, 16));
        return e;
    }

    public static Spinner spinner(Activity a, List<Option> options) {
        Spinner s = new Spinner(a);
        OptionAdapter adapter = new OptionAdapter(a, options);
        s.setAdapter(adapter);
        s.setMinimumHeight(dp(a, 62));
        s.setPadding(0, 0, 0, 0);
        s.setDropDownVerticalOffset(dp(a, 6));
        s.setBackground(bg(a, Color.WHITE, BORDER, 16));
        return s;
    }

    public static Button primary(Activity a, String s) {
        Button b = new Button(a);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(bg(a, ORANGE, ORANGE, 16));
        b.setMinHeight(dp(a, 54));
        b.setElevation(dp(a, 2));
        return b;
    }

    public static Button blue(Activity a, String s) {
        Button b = new Button(a);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(bg(a, BLUE, BLUE, 16));
        b.setMinHeight(dp(a, 54));
        b.setElevation(dp(a, 2));
        return b;
    }

    public static Button secondary(Activity a, String s) {
        Button b = new Button(a);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextColor(BLUE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setBackground(bg(a, Color.rgb(239, 246, 255), Color.rgb(207, 221, 236), 16));
        b.setMinHeight(dp(a, 52));
        b.setElevation(dp(a, 1));
        return b;
    }

    public static void mb(Activity a, View v, int bottom) {
        ViewGroup.LayoutParams old = v.getLayoutParams();
        LinearLayout.LayoutParams lp;
        if (old instanceof LinearLayout.LayoutParams) lp = (LinearLayout.LayoutParams) old;
        else lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dp(a, bottom));
        v.setLayoutParams(lp);
    }

    public static int numberInput() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }
}
