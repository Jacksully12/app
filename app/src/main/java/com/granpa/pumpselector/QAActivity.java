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
        Double acsFlow = acs == null ? null : PumpSelector.flowAt(acs, 40);

        LinearLayout root = Ui.root(this);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Granpa Data QA", 25, Ui.TEXT, 1));
        card.addView(Ui.text(this, PumpRepository.note(this), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Native Android app. No HTML / WebView.", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Main search brand filter removed.", 14, Ui.GREEN, 1));
        card.addView(Ui.text(this, "Direct WhatsApp image share added to model details.", 14, Ui.GREEN, 1));
        card.addView(Ui.text(this, "Chart uses one complete end-to-end curve per model.", 14, Ui.GREEN, 1));
        if (acsFlow != null) card.addView(Ui.text(this, "ACS 1125 at 40 m ≈ " + PumpSelector.lph(acsFlow), 14, Ui.GREEN, 1));
        root.addView(card);

        LinearLayout logic = Ui.card(this);
        logic.addView(Ui.text(this, "Logic", 18, Ui.TEXT, 1));
        logic.addView(Ui.text(this, "Fixed flow: estimated LPH at selected head must be equal to or above required flow.", 14, Ui.MUTED, 0));
        logic.addView(Ui.text(this, "Range flow: estimated LPH must be inside min/max. Reverse input is accepted.", 14, Ui.MUTED, 0));
        logic.addView(Ui.text(this, "No 10% upper limit is applied.", 14, Ui.MUTED, 0));
        root.addView(logic);

        setContentView(Ui.scroll(this, root));
    }
}
