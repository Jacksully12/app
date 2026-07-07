package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;

public class SplashActivity extends Activity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(Ui.dp(this, 28), Ui.topSafe(this), Ui.dp(this, 28), Ui.dp(this, 28));
        root.setBackgroundColor(Ui.BG);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(Ui.dp(this, 132), Ui.dp(this, 132));
        logoLp.setMargins(0, 0, 0, Ui.dp(this, 18));
        root.addView(logo, logoLp);

        TextView brand = new TextView(this);
        brand.setText("GRANPA");
        brand.setTextColor(Ui.TEXT);
        brand.setTextSize(30);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setGravity(Gravity.CENTER);
        brand.setLetterSpacing(0.08f);
        root.addView(brand, new LinearLayout.LayoutParams(-1, -2));

        TextView sub = new TextView(this);
        sub.setText("Pump Selector");
        sub.setTextColor(Ui.MUTED);
        sub.setTextSize(17);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, Ui.dp(this, 6), 0, 0);
        root.addView(sub, subLp);

        LinearLayout group = root;
        group.setAlpha(0f);
        group.setTranslationY(Ui.dp(this, 12));
        setContentView(root);

        group.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 1250);
    }
}
