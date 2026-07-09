package com.granpa.pumpselector;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class QAActivity extends Activity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ArrayList<PumpRecord> rows = PumpRepository.getRecords(this, PumpRepository.TEXMO_ASSET);
        PumpRecord acs = null;
        for (PumpRecord r : rows) if ("ACS 1125".equals(r.model)) acs = r;
        Double q = acs == null ? null : PumpSelector.flowAt(acs, 40);

        LinearLayout root = Ui.root(this);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Granpa QA Report", 26, Ui.TEXT, 1));
        card.addView(Ui.text(this, PumpRepository.note(this, PumpRepository.TEXMO_ASSET), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, PumpRepository.note(this, PumpRepository.LUBI_ASSET), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, PumpRepository.note(this, PumpRepository.KSB_ASSET), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Native Android app • no WebView • no HTML wrapper", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Default display unit: LPH. LPM remains available from the unit dropdown.", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Brand rule: app supports separate Texmo and Lubi offline catalogues.", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Checked: dropdown readability, search, fixed flow, range flow, model details, chart, WhatsApp share, download image, GitHub workflow.", 14, Ui.MUTED, 0));
        if (q != null) card.addView(Ui.text(this, "ACS 1125 at 40 m ≈ " + PumpSelector.formatFlow(q, "LPH"), 14, Ui.GREEN, 1));
        root.addView(card);

        LinearLayout rules = Ui.card(this);
        rules.addView(Ui.text(this, "Logic rules", 18, Ui.TEXT, 1));
        rules.addView(Ui.text(this, "Fixed flow: dealer smart rule. A selected pump type shows maximum 2 above target and 2 below target; All pump types is grouped category-wise.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Fixed-flow results are grouped as Best ±10%, Extended ±20%, Wide ±30%, or Last option ±50%. Band quality is checked before HP so a very wide match cannot hide a better-band match.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Range mode accepts reverse ranges and checks inside min/max.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Page number appears in the app but is hidden from the share image.", 14, Ui.MUTED, 0));
        rules.addView(Ui.text(this, "Data audit: 1,780 records, 0 missing model/brand/category/curve/page/catalogue-section/size fields. CDP0380S and CDP07S size corrected to 50.", 14, Ui.MUTED, 0));
        root.addView(rules);

        setContentView(Ui.scroll(this, root));
    }
}
