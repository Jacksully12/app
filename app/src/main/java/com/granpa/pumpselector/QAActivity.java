package com.granpa.pumpselector;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class QAActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        ArrayList<PumpRecord> rows = PumpRepository.getRecords(this);
        PumpRecord sample = null;
        for (PumpRecord r : rows) {
            if ("SRF06/09".equals(r.model)) {
                sample = r;
                break;
            }
        }
        Double sampleFlow = sample == null ? null : PumpSelector.flowAt(sample, 40);

        LinearLayout root = Ui.root(this);
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, "Granpa Data QA", 25, Ui.TEXT, 1));
        card.addView(Ui.text(this, PumpRepository.note(this), 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Native Android app. No HTML / WebView.", 14, Ui.MUTED, 0));
        card.addView(Ui.text(this, "Model search is at the top.", 14, Ui.GREEN, 1));
        card.addView(Ui.text(this, "Only Share WhatsApp and Download Image buttons are shown in model details.", 14, Ui.GREEN, 1));
        card.addView(Ui.text(this, "Catalogue section uses compact page-header text from the data.", 14, Ui.GREEN, 1));
        if (sampleFlow != null) card.addView(Ui.text(this, "SRF06/09 at 40 m ≈ " + PumpSelector.lph(sampleFlow), 14, Ui.GREEN, 1));
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
