package com.granpa.pumpselector;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.List;

public final class Ui {
    private Ui() {}

    public static final int BG = Color.rgb(246, 248, 251);
    public static final int CARD = Color.WHITE;
    public static final int TEXT = Color.rgb(16, 32, 51);
    public static final int MUTED = Color.rgb(96, 112, 133);
    public static final int BORDER = Color.rgb(220, 229, 239);
    public static final int ORANGE = Color.rgb(242, 103, 34);
    public static final int BLUE = Color.rgb(18, 67, 114);
    public static final int GREEN = Color.rgb(18, 126, 76);
    public static final int RED = Color.rgb(180, 55, 35);

    public static int dp(Activity activity, int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    /** Fallback for screens that are not created through {@link #root(Activity)}. */
    public static int topSafe(Activity activity) {
        return dp(activity, 16);
    }

    /**
     * Applies status/navigation-bar insets without relying on hardcoded system resource names.
     * This keeps the programmatic UI usable under Android 15/16 edge-to-edge enforcement.
     */
    public static void applySystemBars(Activity activity, View view,
                                       int leftDp, int topDp, int rightDp, int bottomDp) {
        final int baseLeft = dp(activity, leftDp);
        final int baseTop = dp(activity, topDp);
        final int baseRight = dp(activity, rightDp);
        final int baseBottom = dp(activity, bottomDp);
        view.setPadding(baseLeft, baseTop, baseRight, baseBottom);
        view.setOnApplyWindowInsetsListener((target, insets) -> {
            target.setPadding(
                    baseLeft + insets.getSystemWindowInsetLeft(),
                    baseTop + insets.getSystemWindowInsetTop(),
                    baseRight + insets.getSystemWindowInsetRight(),
                    baseBottom + insets.getSystemWindowInsetBottom()
            );
            return insets;
        });
        view.requestApplyInsets();
    }

    public static GradientDrawable bg(Activity activity, int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(activity, radiusDp));
        drawable.setStroke(dp(activity, 1), stroke);
        return drawable;
    }

    public static GradientDrawable solid(Activity activity, int fill, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(activity, radiusDp));
        return drawable;
    }

    public static TextView text(Activity activity, String value, int sp, int color, int style) {
        TextView text = new TextView(activity);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setTypeface(Typeface.DEFAULT, style);
        text.setLineSpacing(0, 1.08f);
        return text;
    }

    public static TextView label(Activity activity, String value) {
        TextView label = text(activity, value, 13, TEXT, Typeface.BOLD);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(activity, 14), 0, dp(activity, 7));
        label.setLayoutParams(params);
        return label;
    }

    public static LinearLayout root(Activity activity) {
        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        applySystemBars(activity, root, 16, 8, 16, 16);
        return root;
    }

    public static ScrollView scroll(Activity activity, View child) {
        ScrollView scroll = new ScrollView(activity);
        scroll.setFillViewport(true);
        scroll.addView(child);
        return scroll;
    }

    public static LinearLayout row(Activity activity) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        return row;
    }

    public static LinearLayout card(Activity activity) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(activity, 16), dp(activity, 16), dp(activity, 16), dp(activity, 16));
        card.setBackground(bg(activity, CARD, BORDER, 20));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, 0, 0, dp(activity, 14));
        card.setLayoutParams(params);
        return card;
    }

    public static EditText input(Activity activity, String value, int type) {
        EditText input = new EditText(activity);
        input.setText(value);
        input.setTextSize(16);
        input.setSingleLine(true);
        input.setInputType(type);
        input.setPadding(dp(activity, 14), 0, dp(activity, 14), 0);
        input.setMinHeight(dp(activity, 52));
        input.setBackground(bg(activity, Color.WHITE, BORDER, 16));
        return input;
    }

    public static Spinner spinner(Activity activity, List<Option> options) {
        Spinner spinner = new Spinner(activity);
        spinner.setAdapter(new OptionAdapter(activity, options));
        spinner.setMinimumHeight(dp(activity, 58));
        spinner.setDropDownVerticalOffset(dp(activity, 6));
        spinner.setBackground(bg(activity, Color.WHITE, BORDER, 16));
        return spinner;
    }

    public static Button primary(Activity activity, String value) {
        Button button = new Button(activity);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(solid(activity, ORANGE, 16));
        button.setMinHeight(dp(activity, 50));
        return button;
    }

    public static Button secondary(Activity activity, String value) {
        Button button = new Button(activity);
        button.setText(value);
        button.setAllCaps(false);
        button.setTextColor(BLUE);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setBackground(bg(activity, Color.rgb(239, 246, 255), Color.rgb(207, 221, 236), 16));
        button.setMinHeight(dp(activity, 48));
        return button;
    }

    public static Button blue(Activity activity, String value) {
        Button button = primary(activity, value);
        button.setBackground(solid(activity, BLUE, 16));
        return button;
    }

    public static void mb(Activity activity, View view, int bottomDp) {
        LinearLayout.LayoutParams params = view.getLayoutParams() instanceof LinearLayout.LayoutParams
                ? (LinearLayout.LayoutParams) view.getLayoutParams()
                : new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, dp(activity, bottomDp));
        view.setLayoutParams(params);
    }

    public static int numberInput() {
        return InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }
}
