package com.granpa.pumpselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private EditText modelSearch;
    private EditText headInput;
    private EditText flowOneInput;
    private EditText flowTwoInput;
    private Spinner modeSpinner;
    private Spinner unitSpinner;
    private Spinner categorySpinner;
    private Spinner phaseSpinner;
    private LinearLayout secondFlowBox;
    private TextView hintText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PumpRepository.getRecords(this);

        LinearLayout root = Ui.root(this);
        root.addView(headerCard());
        root.addView(searchCard());
        root.addView(Ui.text(this, PumpRepository.note(this), 12, Ui.MUTED, 0));

        setContentView(Ui.scroll(this, root));
        updateFlowMode();

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateFlowMode(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private LinearLayout headerCard() {
        LinearLayout card = Ui.card(this);
        LinearLayout row = Ui.row(this);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        row.addView(logo, new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64)));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setPadding(Ui.dp(this, 12), 0, 0, 0);
        titleBox.addView(Ui.text(this, "Granpa", 28, Ui.TEXT, 1));
        titleBox.addView(Ui.text(this, "Pump selector with chart and WhatsApp image share", 14, Ui.MUTED, 0));
        row.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(row);
        return card;
    }

    private LinearLayout searchCard() {
        LinearLayout card = Ui.card(this);

        card.addView(Ui.label(this, "Search model / keyword"));
        modelSearch = Ui.input(this, "", InputType.TYPE_CLASS_TEXT);
        modelSearch.setHint("Type first, e.g. ACS1125, ASM, JRF17");
        card.addView(modelSearch);
        TextView searchHint = Ui.text(this, "Search ignores spaces, so ACS1125 finds ACS 1125.", 12, Ui.MUTED, 0);
        searchHint.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        card.addView(searchHint);

        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout headRow = Ui.row(this);
        headInput = Ui.input(this, "40", Ui.numberInput());
        headRow.addView(headInput, new LinearLayout.LayoutParams(0, -2, 1));
        TextView metre = Ui.text(this, " m", 15, Ui.MUTED, 1);
        metre.setPadding(Ui.dp(this, 8), 0, 0, 0);
        headRow.addView(metre);
        card.addView(headRow);

        card.addView(Ui.label(this, "Water input mode"));
        modeSpinner = Ui.spinner(this, options(new String[][]{
                {"fixed", "Fixed water flow", "Enter one required discharge value"},
                {"range", "Water-flow range", "Enter minimum and maximum acceptable discharge"}
        }));
        card.addView(modeSpinner);

        card.addView(Ui.label(this, "Water flow"));
        LinearLayout flowRow = Ui.row(this);
        flowOneInput = Ui.input(this, "1200", Ui.numberInput());
        flowRow.addView(flowOneInput, new LinearLayout.LayoutParams(0, -2, 1));
        unitSpinner = Ui.spinner(this, options(new String[][]{
                {"LPH", "LPH", "Litres per hour"},
                {"LPM", "LPM", "Litres per minute"},
                {"LPS", "LPS", "Litres per second"},
                {"M3H", "m³/hour", "Cubic metres per hour"}
        }));
        LinearLayout.LayoutParams unitLp = new LinearLayout.LayoutParams(Ui.dp(this, 130), -2);
        unitLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        flowRow.addView(unitSpinner, unitLp);
        card.addView(flowRow);

        secondFlowBox = new LinearLayout(this);
        secondFlowBox.setOrientation(LinearLayout.VERTICAL);
        secondFlowBox.addView(Ui.label(this, "Second flow value"));
        flowTwoInput = Ui.input(this, "4500", Ui.numberInput());
        secondFlowBox.addView(flowTwoInput);
        card.addView(secondFlowBox);

        hintText = Ui.text(this, "", 13, Ui.MUTED, 0);
        hintText.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        card.addView(hintText);

        card.addView(Ui.label(this, "Pump type"));
        categorySpinner = Ui.spinner(this, categoryOptions());
        card.addView(categorySpinner);

        card.addView(Ui.label(this, "Phase"));
        phaseSpinner = Ui.spinner(this, options(new String[][]{
                {"any", "Any phase", "Show single and three phase models"},
                {"S", "Single phase", "Usually 220 V supply"},
                {"T", "Three phase", "Usually 380 V supply"}
        }));
        Ui.mb(this, phaseSpinner, 18);
        card.addView(phaseSpinner);

        Button find = Ui.primary(this, "Find suitable pumps");
        Ui.mb(this, find, 12);
        card.addView(find);
        find.setOnClickListener(v -> openResults());

        Button browse = Ui.blue(this, "Browse full catalogue");
        card.addView(browse);
        browse.setOnClickListener(v -> startActivity(new Intent(this, CatalogueActivity.class)));

        return card;
    }

    private void updateFlowMode() {
        boolean range = selected(modeSpinner).equals("range");
        secondFlowBox.setVisibility(range ? View.VISIBLE : View.GONE);
        hintText.setText(range
                ? "Range accepts either order, e.g. 13,500 to 4,500."
                : "Fixed mode shows pumps with equal or more water at the selected head. No upper limit.");
    }

    private void openResults() {
        double head = parse(headInput);
        double first = parse(flowOneInput);
        double second = parse(flowTwoInput);
        boolean range = selected(modeSpinner).equals("range");

        if (Double.isNaN(head) || head < 0 || Double.isNaN(first) || first <= 0 || (range && (Double.isNaN(second) || second <= 0))) {
            Toast.makeText(this, "Enter valid head and water flow values", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("head", head);
        intent.putExtra("range", range);
        intent.putExtra("flow1", first);
        intent.putExtra("flow2", range ? second : first);
        intent.putExtra("unit", selected(unitSpinner));
        intent.putExtra("cat", selected(categorySpinner));
        intent.putExtra("phase", selected(phaseSpinner));
        intent.putExtra("key", modelSearch.getText().toString());
        startActivity(intent);
    }

    private List<Option> categoryOptions() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types", "Main category • search the full catalogue", true));
        o.add(new Option("borewell_all", "Borewell Submersible", "Main category • all borewell submersible pumps", true));
        o.add(new Option("monoblock_all", "Monoblock / Centrifugal", "Main category • agricultural and centrifugal monoblock pumps", true));
        o.add(new Option("dewatering_all", "Dewatering / Sewage", "Main category • construction, drainage and wastewater pumps", true));
        for (String c : PumpRepository.categories(this)) {
            o.add(new Option(c, "Sub category • " + c, categoryDetail(c), false));
        }
        return o;
    }

    private String categoryDetail(String c) {
        String lower = c == null ? "" : c.toLowerCase(java.util.Locale.US);
        if (lower.contains("borewell")) return "Detailed catalogue group under borewell pumps";
        if (lower.contains("agricultural")) return "Detailed catalogue group for agricultural monoblock pumps";
        if (lower.contains("centrifugal")) return "Detailed catalogue group for centrifugal monoblock pumps";
        if (lower.contains("sewage") || lower.contains("dewatering")) return "Detailed catalogue group for dewatering / sewage pumps";
        return "Detailed catalogue group";
    }

    private List<Option> options(String[][] values) {
        ArrayList<Option> out = new ArrayList<>();
        for (String[] v : values) {
            String detail = v.length > 2 ? v[2] : "";
            out.add(new Option(v[0], v[1], detail));
        }
        return out;
    }

    private String selected(Spinner s) {
        Object item = s.getSelectedItem();
        return item instanceof Option ? ((Option) item).value : String.valueOf(item);
    }

    private double parse(EditText e) {
        try { return Double.parseDouble(e.getText().toString().replace(",", "").trim()); }
        catch (Exception ex) { return Double.NaN; }
    }
}
