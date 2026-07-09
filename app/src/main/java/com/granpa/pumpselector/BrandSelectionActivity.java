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
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 60), Ui.dp(this, 60)));

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);
        text.setPadding(Ui.dp(this, 12), 0, 0, 0);
        text.addView(Ui.text(this, "Granpa", 28, Ui.TEXT, Typeface.BOLD));
        text.addView(Ui.text(this, "Choose brand or compare catalogues", 14, Ui.MUTED, 0));
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(row);
        root.addView(header);

        root.addView(modeCard(
                "TEXMO",
                "Use the Texmo pump selector catalogue.",
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

        root.addView(modeCard(
                "KSB",
                "Use KSB domestic pump performance data extracted from the uploaded 2026 booklet.",
                "Open KSB",
                PumpRepository.KSB_ASSET,
                "KSB"
        ));

        LinearLayout compare = Ui.card(this);
        compare.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        compare.addView(Ui.text(this, "COMPARE", 22, Ui.TEXT, Typeface.BOLD));
        compare.addView(Ui.text(this, "Compare Texmo, Lubi and KSB at the same head, flow, type and phase.", 14, Ui.MUTED, 0));

        Button btn = smallPrimary("Compare brands");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 190), Ui.dp(this, 48));
        lp.setMargins(0, Ui.dp(this, 10), 0, 0);
        compare.addView(btn, lp);

        btn.setOnClickListener(v -> startActivity(new Intent(this, CompareActivity.class)));
        root.addView(compare);

        setContentView(Ui.scroll(this, root));
    }

    LinearLayout modeCard(String title, String desc, String button, String asset, String brand) {
        LinearLayout c = Ui.card(this);
        c.setPadding(Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14), Ui.dp(this, 14));
        c.addView(Ui.text(this, title, 22, Ui.BLUE, Typeface.BOLD));
        c.addView(Ui.text(this, desc, 14, Ui.MUTED, 0));
        c.addView(Ui.text(this, PumpRepository.note(this, asset), 12, Ui.MUTED, 0));

        Button b = smallPrimary(button);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 160), Ui.dp(this, 48));
        lp.setMargins(0, Ui.dp(this, 10), 0, 0);
        c.addView(b, lp);

        b.setOnClickListener(v -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("asset", asset);
            i.putExtra("brand", brand);
            startActivity(i);
        });
        return c;
    }

    Button smallPrimary(String s) {
        Button b = Ui.primary(this, s);
        b.setTextSize(14);
        b.setMinHeight(Ui.dp(this, 46));
        return b;
    }
}
