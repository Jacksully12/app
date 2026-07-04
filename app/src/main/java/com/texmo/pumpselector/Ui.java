package com.texmo.pumpselector;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    public static final int TEXT = Color.rgb(24, 35, 54);
    public static final int MUTED = Color.rgb(91, 108, 130);
    public static final int BORDER = Color.rgb(221, 229, 239);
    public static final int ORANGE = Color.rgb(242, 103, 34);
    public static final int BLUE = Color.rgb(18, 67, 114);
    public static final int GREEN = Color.rgb(35, 132, 82);

    public static int dp(Activity a, int value) {
        return (int) (value * a.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static TextView text(Activity a, String s, int sp, int color, int style) {
        TextView v = new TextView(a);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setTypeface(Typeface.DEFAULT, style);
        v.setIncludeFontPadding(true);
        return v;
    }

    public static TextView label(Activity a, String s) {
        TextView v = text(a, s, 13, TEXT, Typeface.BOLD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(a, 12), 0, dp(a, 6));
        v.setLayoutParams(lp);
        return v;
    }

    public static EditText input(Activity a, String value, int inputType) {
        EditText e = new EditText(a);
        e.setText(value == null ? "" : value);
        e.setTextSize(16);
        e.setTextColor(TEXT);
        e.setSingleLine(true);
        e.setInputType(inputType);
        e.setPadding(dp(a, 14), 0, dp(a, 14), 0);
        e.setBackground(roundRect(a, Color.WHITE, BORDER, 14));
        e.setMinHeight(dp(a, 52));
        return e;
    }

    public static Spinner spinner(Activity a, List<Option> options) {
        Spinner s = new Spinner(a);
        ArrayAdapter<Option> adapter = new ArrayAdapter<>(a, android.R.layout.simple_spinner_dropdown_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(adapter);
        s.setBackground(roundRect(a, Color.WHITE, BORDER, 14));
        s.setPadding(dp(a, 8), 0, dp(a, 8), 0);
        s.setMinimumHeight(dp(a, 52));
        return s;
    }

    public static Button primaryButton(Activity a, String s) {
        Button b = new Button(a);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(roundRect(a, ORANGE, ORANGE, 14));
        b.setMinHeight(dp(a, 54));
        return b;
    }

    public static Button secondaryButton(Activity a, String s) {
        Button b = new Button(a);
        b.setText(s);
        b.setTextColor(BLUE);
        b.setTextSize(14);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(roundRect(a, Color.rgb(239, 246, 255), Color.rgb(207, 221, 236), 14));
        b.setMinHeight(dp(a, 48));
        return b;
    }

    public static GradientDrawable roundRect(Activity a, int fill, int stroke, int radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(fill);
        gd.setCornerRadius(dp(a, radiusDp));
        gd.setStroke(dp(a, 1), stroke);
        return gd;
    }

    public static LinearLayout root(Activity a) {
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a, 16), dp(a, 16), dp(a, 16), dp(a, 16));
        root.setBackgroundColor(BG);
        return root;
    }

    public static ScrollView scroll(Activity a, View child) {
        ScrollView sc = new ScrollView(a);
        sc.setFillViewport(false);
        sc.addView(child);
        return sc;
    }

    public static LinearLayout card(Activity a) {
        LinearLayout card = new LinearLayout(a);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(a, 16), dp(a, 16), dp(a, 16), dp(a, 16));
        card.setBackground(roundRect(a, CARD, BORDER, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(a, 12));
        card.setLayoutParams(lp);
        return card;
    }

    public static LinearLayout row(Activity a) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    public static void addMarginBottom(Activity a, View v, int bottomDp) {
        ViewGroup.LayoutParams old = v.getLayoutParams();
        LinearLayout.LayoutParams lp;
        if (old instanceof LinearLayout.LayoutParams) lp = (LinearLayout.LayoutParams) old;
        else lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, dp(a, bottomDp));
        v.setLayoutParams(lp);
    }

    public static int numberInput() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }
}
