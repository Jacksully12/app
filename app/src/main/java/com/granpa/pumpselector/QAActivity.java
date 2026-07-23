package com.granpa.pumpselector;

import android.app.*;
import android.os.*;
import android.widget.*;
import java.util.*;

public class QAActivity extends Activity {
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        LinearLayout root = Ui.root(this);
        root.addView(
                Ui.text(this, "Catalogue data QA", 26, Ui.TEXT, 1)
        );

        add(root, "TEXMO", PumpRepository.TEXMO_ASSET);
        add(root, "LUBI", PumpRepository.LUBI_ASSET);
        add(root, "KSB", PumpRepository.KSB_ASSET);

        root.addView(
                Ui.text(
                        this,
                        "Source-review models remain visible in catalogue "
                                + "browsing but are excluded from automatic "
                                + "recommendations and brand comparison.",
                        13,
                        Ui.MUTED,
                        0
                )
        );

        setContentView(Ui.scroll(this, root));
    }

    void add(LinearLayout root, String brand, String asset) {
        ArrayList<PumpRecord> records =
                PumpRepository.getRecords(this, asset);

        int confirmed = 0;
        int checked = 0;
        int fixed = 0;
        int review = 0;
        int anomalies = 0;
        int catalogueOnly = 0;
        HashSet<String> categories = new HashSet<>();

        for (PumpRecord record : records) {
            categories.add(record.category);

            if ("SOURCE_ANOMALY".equals(record.dataStatus)) {
                anomalies++;
            } else if ("SOURCE_CONFIRMED_CATALOGUE_ONLY".equals(record.dataStatus)) {
                catalogueOnly++;
            } else if (!record.selectable || "NEEDS_REVIEW".equals(record.dataStatus)) {
                review++;
            } else if ("SOURCE_CONFIRMED".equals(record.dataStatus)) {
                confirmed++;
            } else if ("AUTO_FIXED".equals(record.dataStatus)) {
                fixed++;
            } else {
                checked++;
            }
        }

        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, brand, 21, Ui.BLUE, 1));
        card.addView(
                Ui.text(
                        this,
                        records.size()
                                + " records • "
                                + categories.size()
                                + " detailed categories",
                        14,
                        Ui.MUTED,
                        0
                )
        );
        card.addView(
                Ui.text(
                        this,
                        "Source confirmed: "
                                + confirmed
                                + "   Checked: "
                                + checked
                                + "   Auto-fixed: "
                                + fixed
                                + "   Needs review: "
                                + review
                                + "   Source anomalies: "
                                + anomalies
                                + "   Catalogue-only: "
                                + catalogueOnly,
                        14,
                        (review + anomalies) > 0 ? Ui.ORANGE : Ui.GREEN,
                        1
                )
        );
        if ("LUBI".equals(brand)) {
            card.addView(Ui.text(this,
                    "Supplied Lubi performance tables are integrated. Exact duplicate records were consolidated; four bare-pump variants remain catalogue-only by design.",
                    12, Ui.MUTED, 0));
        } else if ("KSB".equals(brand) && anomalies > 0) {
            card.addView(Ui.text(this,
                    anomalies + " source-table curves remain internally inconsistent. They are searchable for reference but excluded from recommendations and sharing.",
                    12, Ui.ORANGE, 0));
        }
        root.addView(card);
    }
}
