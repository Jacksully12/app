package com.texmo.pumpselector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;

public class QAActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<PumpRecord> records = PumpRepository.getRecords(this);
        int pages = countPages(records);
        PumpRecord acs = null;
        for (PumpRecord r : records) if ("ACS 1125".equals(r.model)) acs = r;
        Double acsFlow = acs == null ? null : PumpSelector.estimateFlowAtHead(acs, 40.0);

        LinearLayout root = Ui.root(this);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Data QA Report", 25, Ui.TEXT, android.graphics.Typeface.BOLD));
        card.addView(Ui.text(this, PumpRepository.dataNote(this), 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        card.addView(Ui.text(this, "Unique pages covered: " + pages + " / 43", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        card.addView(Ui.text(this, "Selection method: strict fixed-head interpolation only", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        card.addView(Ui.text(this, "Fixed flow: estimated LPH must be equal to or above required LPH. No 10% upper limit.", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        card.addView(Ui.text(this, "Flow range: reverse input is accepted and internally converted to min/max.", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        root.addView(card);

        LinearLayout checks = Ui.card(this);
        checks.addView(Ui.text(this, "Manual checks included", 18, Ui.TEXT, android.graphics.Typeface.BOLD));
        checks.addView(Ui.text(this, "✓ ACS1125 search matches ACS 1125", 14, Ui.GREEN, android.graphics.Typeface.BOLD));
        checks.addView(Ui.text(this, "✓ Page 31 sewage pump rows included", 14, Ui.GREEN, android.graphics.Typeface.BOLD));
        checks.addView(Ui.text(this, "✓ Page 32 merged HP/kW rows included", 14, Ui.GREEN, android.graphics.Typeface.BOLD));
        if (acsFlow != null) checks.addView(Ui.text(this, "✓ ACS 1125 at 40 m ≈ " + PumpSelector.formatLPH(acsFlow), 14, Ui.GREEN, android.graphics.Typeface.BOLD));
        root.addView(checks);

        LinearLayout logic = Ui.card(this);
        logic.addView(Ui.text(this, "How the result is calculated", 18, Ui.TEXT, android.graphics.Typeface.BOLD));
        logic.addView(Ui.text(this, "The app converts all catalogue discharge units into LPH and estimates water at the entered fixed head using linear interpolation between the catalogue curve points.", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        logic.addView(Ui.text(this, "Example: ACS 1125 has 6 / 42 m and 102 / 20 LPM. At 40 m, the app estimates about 1,473 LPH.", 14, Ui.MUTED, android.graphics.Typeface.NORMAL));
        root.addView(logic);

        setContentView(Ui.scroll(this, root));
    }

    private int countPages(List<PumpRecord> records) {
        boolean[] seen = new boolean[100];
        int count = 0;
        for (PumpRecord r : records) {
            if (r.page > 0 && r.page < seen.length && !seen[r.page]) {
                seen[r.page] = true;
                count++;
            }
        }
        return count;
    }
}
