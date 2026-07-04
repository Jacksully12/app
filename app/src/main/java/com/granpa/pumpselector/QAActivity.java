package com.granpa.pumpselector;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class QAActivity extends Activity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ArrayList<PumpRecord> rows = PumpRepository.getRecords(this);
        PumpRecord acs = null;
        for (PumpRecord r : rows) if ("ACS 1125".equals(r.model)) acs = r;
        Double q = acs == null ? null : PumpSelector.flowAt(acs, 40);

        LinearLayout root = Ui.root(this);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Granpa QA Report", 26, Ui.TEXT, 1));
        card.addView(Ui.text(this, PumpRepository.note(this), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Native Android app • no WebView • no HTML wrapper", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Default display unit: LPH. LPM remains available from the unit dropdown.", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Checked: dropdown readability, search, fixed flow, range flow, model details, chart, WhatsApp share, download image, GitHub workflow.", 14, Ui.MUTED, 0));
        if (q != null) card.addView(Ui.text(this, "ACS 1125 at 40 m ≈ " + PumpSelector.formatFlow(q, "LPH"), 14, Ui.GREEN, 1));
        root.addView(card);

        LinearLayout rules = Ui.card(this);
        rules.addView(Ui.text(this, "Logic rules", 18, Ui.TEXT, 1));
        rules.addView(Ui.text(this, "Fixed flow: strict ±10% rule. Estimated flow must be between 10% less and 10% more of the searched flow.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Fixed-flow results outside ±10% are not shown. Remaining matches are sorted by closest flow difference first.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Range mode accepts reverse ranges and checks inside min/max.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Page number appears in the app but is hidden from the share image.", 14, Ui.MUTED, 0));
        root.addView(rules);

        setContentView(Ui.scroll(this, root));
    }
}
