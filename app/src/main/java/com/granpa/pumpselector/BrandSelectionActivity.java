package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;

public class BrandSelectionActivity extends Activity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.root(this);

        LinearLayout header = Ui.card(this);
        LinearLayout row = Ui.row(this);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 72), Ui.dp(this, 72)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(Ui.dp(this, 14), 0, 0, 0);
        text.addView(Ui.text(this, "Granpa", 30, Ui.TEXT, Typeface.BOLD));
        text.addView(Ui.text(this, "Choose brand or compare catalogues", 14, Ui.MUTED, 0));
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(row);
        root.addView(header);

        root.addView(modeCard(
                "TEXMO",
                "Use the existing Texmo pump selector catalogue.",
                "Open Texmo",
                PumpRepository.TEXMO_ASSET,
                "TEXMO"
        ));

        root.addView(modeCard(
                "LUBI",
                "Use Lubi performance data extracted from the uploaded booklet.",
                "Open Lubi",
                PumpRepository.LUBI_ASSET,
                "LUBI"
        ));

        LinearLayout compare = Ui.card(this);
        compare.addView(Ui.text(this, "COMPARE", 24, Ui.TEXT, Typeface.BOLD));
        compare.addView(Ui.text(this, "Compare Texmo and Lubi at the same head, flow, type and phase.", 14, Ui.MUTED, 0));
        Button btn = Ui.primary(this, "Compare Texmo vs Lubi");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, Ui.dp(this, 12), 0, 0);
        compare.addView(btn, lp);
        btn.setOnClickListener(v -> startActivity(new Intent(this, CompareActivity.class)));
        root.addView(compare);

        setContentView(Ui.scroll(this, root));
    }

    LinearLayout modeCard(String title, String desc, String button, String asset, String brand) {
        LinearLayout c = Ui.card(this);
        c.addView(Ui.text(this, title, 24, Ui.BLUE, Typeface.BOLD));
        c.addView(Ui.text(this, desc, 14, Ui.MUTED, 0));
        c.addView(Ui.text(this, PumpRepository.note(this, asset), 12, Ui.MUTED, 0));

        Button b = Ui.primary(this, button);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, Ui.dp(this, 12), 0, 0);
        c.addView(b, lp);

        b.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("asset", asset);
            i.putExtra("brand", brand);
            startActivity(i);
        });
        return c;
    }
}
