package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CompareActivity extends Activity {
    EditText head, flow;
    Spinner unit, cat, phase;

    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.root(this);

        LinearLayout header = Ui.card(this);
        header.addView(Ui.text(this, "Compare brands", 26, Ui.TEXT, 1));
        header.addView(Ui.text(this, "Enter one duty point and choose a pump type for a like-for-like comparison across brands.", 14, Ui.MUTED, 0));
        root.addView(header);

        LinearLayout card = Ui.card(this);

        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout hr = Ui.row(this);
        head = Ui.input(this, "40", Ui.numberInput());
        hr.addView(head, new LinearLayout.LayoutParams(0, -2, 1));
        TextView m = Ui.text(this, " m", 15, Ui.MUTED, 1);
        m.setPadding(Ui.dp(this, 8), 0, 0, 0);
        hr.addView(m);
        card.addView(hr);

        card.addView(Ui.label(this, "Required flow"));
        LinearLayout fr = Ui.row(this);
        flow = Ui.input(this, "1200", Ui.numberInput());
        fr.addView(flow, new LinearLayout.LayoutParams(0, -2, 1));

        unit = Ui.spinner(this, options(new String[][]{
                {"LPH", "LPH", "Litres per hour"},
                {"LPM", "LPM", "Litres per minute"},
                {"LPS", "LPS", "Litres per second"},
                {"M3H", "m³/hour", "Cubic metres per hour"}
        }));
        LinearLayout.LayoutParams ulp = new LinearLayout.LayoutParams(Ui.dp(this, 150), -2);
        ulp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        fr.addView(unit, ulp);
        card.addView(fr);

        card.addView(Ui.label(this, "Pump type"));
        cat = Ui.spinner(this, categories());
        card.addView(cat);

        card.addView(Ui.label(this, "Phase"));
        phase = Ui.spinner(this, options(new String[][]{
                {"any", "Any phase", "Show single and three phase models"},
                {"S", "Single phase", "Usually 220 V supply"},
                {"T", "Three phase", "Usually 380/415 V supply"}
        }));
        Ui.mb(this, phase, 18);
        card.addView(phase);

        Button compare = Ui.primary(this, "Show compare results");
        compare.setTextSize(14);
        card.addView(compare);
        root.addView(card);

        TextView note = Ui.text(this, "The next screen will show Texmo, Lubi and KSB matches separately so it is easier to review and open each model.", 13, Ui.MUTED, 0);
        root.addView(note);

        setContentView(Ui.scroll(this, root));

        compare.setOnClickListener(v -> openCompareResults());
    }

    void openCompareResults() {
        double h = val(head);
        double f = val(flow);

        if (Double.isNaN(h) || h < 0 || Double.isNaN(f) || f <= 0) {
            Toast.makeText(this, "Enter valid head and flow values", Toast.LENGTH_LONG).show();
            return;
        }

        Intent i = new Intent(this, CompareResultsActivity.class);
        i.putExtra("head", h);
        i.putExtra("flow", f);
        i.putExtra("unit", sel(unit));
        i.putExtra("cat", sel(cat));
        i.putExtra("phase", sel(phase));
        startActivity(i);
    }

    List<Option> categories() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("borewell_all", "Borewell Submersible", "Main category", true));
        o.add(new Option("openwell_all", "Openwell Submersible", "Main category", true));
        o.add(new Option("monoblock_all", "Centrifugal / Surface Monoblock", "Self priming, jet, centrifugal, monoblock/monobloc", true));
        o.add(new Option("multistage_all", "Multistage Pumps", "Vertical/openwell/horizontal multistage", true));
        o.add(new Option("booster_all", "Booster / Pressure Pumps", "Booster and pressure pump sections", true));
        o.add(new Option("dewatering_all", "Dewatering / Sewage", "Drainage, sewage and similar pumps", true));
        return o;
    }

    List<Option> options(String[][] arr) {
        ArrayList<Option> o = new ArrayList<>();
        for (String[] a : arr) o.add(new Option(a[0], a[1], a.length > 2 ? a[2] : "", true));
        return o;
    }

    String sel(Spinner s) { return ((Option) s.getSelectedItem()).value; }
    double val(EditText e) { try { return Double.parseDouble(e.getText().toString().trim()); } catch (Exception ex) { return Double.NaN; } }
}
