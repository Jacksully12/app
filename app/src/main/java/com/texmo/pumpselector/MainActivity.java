package com.texmo.pumpselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private EditText headInput;
    private EditText flowOneInput;
    private EditText flowTwoInput;
    private EditText keywordInput;
    private Spinner flowModeSpinner;
    private Spinner unitSpinner;
    private Spinner categorySpinner;
    private Spinner phaseSpinner;
    private LinearLayout flowTwoWrapper;
    private TextView ruleHint;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PumpRepository.getRecords(this);

        LinearLayout root = Ui.root(this);

        LinearLayout header = Ui.card(this);
        LinearLayout hero = Ui.row(this);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.app_logo);
        LinearLayout.LayoutParams logoLp = new LinearLayout.LayoutParams(Ui.dp(this, 64), Ui.dp(this, 64));
        hero.addView(logo, logoLp);

        LinearLayout heroText = new LinearLayout(this);
        heroText.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams heroTextLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        heroTextLp.setMargins(Ui.dp(this, 12), 0, 0, 0);
        TextView title = Ui.text(this, "Pump Selector", 26, Ui.TEXT, android.graphics.Typeface.BOLD);
        TextView sub = Ui.text(this, "Strict fixed-head calculation • clean search • chart in every model", 14, Ui.MUTED, android.graphics.Typeface.NORMAL);
        heroText.addView(title);
        heroText.addView(sub);
        hero.addView(heroText, heroTextLp);
        header.addView(hero);
        root.addView(header);

        LinearLayout card = Ui.card(this);
        card.addView(Ui.label(this, "Fixed head"));
        LinearLayout headRow = Ui.row(this);
        headInput = Ui.input(this, "40", Ui.numberInput());
        headRow.addView(headInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView metre = Ui.text(this, " metre", 15, Ui.MUTED, android.graphics.Typeface.BOLD);
        metre.setPadding(Ui.dp(this, 8), 0, 0, 0);
        headRow.addView(metre);
        card.addView(headRow);

        card.addView(Ui.label(this, "Water input mode"));
        flowModeSpinner = Ui.spinner(this, options(new String[][]{{"fixed", "Fixed water flow"}, {"range", "Water-flow range"}}));
        card.addView(flowModeSpinner);

        card.addView(Ui.label(this, "Water flow"));
        LinearLayout flowRow = Ui.row(this);
        flowOneInput = Ui.input(this, "1200", Ui.numberInput());
        flowRow.addView(flowOneInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        unitSpinner = Ui.spinner(this, options(new String[][]{{"LPH", "LPH"}, {"LPM", "LPM"}, {"LPS", "LPS"}, {"M3H", "m³/hour"}}));
        LinearLayout.LayoutParams unitLp = new LinearLayout.LayoutParams(Ui.dp(this, 130), ViewGroup.LayoutParams.WRAP_CONTENT);
        unitLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        flowRow.addView(unitSpinner, unitLp);
        card.addView(flowRow);

        flowTwoWrapper = new LinearLayout(this);
        flowTwoWrapper.setOrientation(LinearLayout.VERTICAL);
        flowTwoWrapper.addView(Ui.label(this, "Second flow value"));
        flowTwoInput = Ui.input(this, "4500", Ui.numberInput());
        flowTwoWrapper.addView(flowTwoInput);
        card.addView(flowTwoWrapper);

        ruleHint = Ui.text(this, "", 13, Ui.MUTED, android.graphics.Typeface.NORMAL);
        ruleHint.setPadding(0, Ui.dp(this, 10), 0, Ui.dp(this, 8));
        card.addView(ruleHint);

        card.addView(Ui.label(this, "Pump type"));
        categorySpinner = Ui.spinner(this, categoryOptions());
        card.addView(categorySpinner);

        card.addView(Ui.label(this, "Phase"));
        phaseSpinner = Ui.spinner(this, options(new String[][]{{"any", "Any"}, {"S", "Single phase"}, {"T", "Three phase"}}));
        card.addView(phaseSpinner);

        card.addView(Ui.label(this, "Model search / keyword"));
        keywordInput = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        keywordInput.setHint("Example: ACS1125, JRF17, ASM");
        card.addView(keywordInput);

        android.widget.Button find = Ui.primaryButton(this, "Find suitable pumps");
        Ui.addMarginBottom(this, find, 10);
        card.addView(find);

        android.widget.Button catalogue = Ui.secondaryButton(this, "Browse catalogue / search models");
        Ui.addMarginBottom(this, catalogue, 10);
        card.addView(catalogue);

        android.widget.Button qa = Ui.secondaryButton(this, "Data QA report");
        card.addView(qa);
        root.addView(card);

        TextView note = Ui.text(this, PumpRepository.dataNote(this), 12, Ui.MUTED, android.graphics.Typeface.NORMAL);
        root.addView(note);

        setContentView(Ui.scroll(this, root));
        updateFlowModeUi();

        flowModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updateFlowModeUi(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { updateFlowModeUi(); }
        });

        find.setOnClickListener(v -> openResults());
        catalogue.setOnClickListener(v -> startActivity(new Intent(this, CatalogueActivity.class)));
        qa.setOnClickListener(v -> startActivity(new Intent(this, QAActivity.class)));
    }

    private void openResults() {
        double head = parseDouble(headInput.getText().toString());
        double flow1 = parseDouble(flowOneInput.getText().toString());
        boolean range = selectedValue(flowModeSpinner).equals("range");
        double flow2 = range ? parseDouble(flowTwoInput.getText().toString()) : flow1;
        if (Double.isNaN(head) || head < 0 || Double.isNaN(flow1) || flow1 <= 0 || (range && (Double.isNaN(flow2) || flow2 <= 0))) {
            Toast.makeText(this, "Enter valid head and water flow values", Toast.LENGTH_LONG).show();
            return;
        }
        Intent i = new Intent(this, ResultsActivity.class);
        i.putExtra("head", head);
        i.putExtra("rangeMode", range);
        i.putExtra("flow1", flow1);
        i.putExtra("flow2", flow2);
        i.putExtra("unit", selectedValue(unitSpinner));
        i.putExtra("category", selectedValue(categorySpinner));
        i.putExtra("phase", selectedValue(phaseSpinner));
        i.putExtra("keyword", keywordInput.getText().toString().trim());
        startActivity(i);
    }

    private void updateFlowModeUi() {
        boolean range = selectedValue(flowModeSpinner).equals("range");
        flowTwoWrapper.setVisibility(range ? View.VISIBLE : View.GONE);
        ruleHint.setText(range
                ? "Range mode accepts either order, for example 13,500 to 4,500. Result must be inside the range at the fixed head."
                : "Fixed mode shows pumps that give at least the required water at the fixed head. No upper limit is applied.");
    }

    private List<Option> categoryOptions() {
        List<Option> out = new ArrayList<>();
        out.add(new Option("all", "All pump types"));
        out.add(new Option("monoblock_all", "All Monoblock / Centrifugal"));
        out.add(new Option("submersible_all", "All Submersible"));
        out.add(new Option("borewell_all", "All Borewell Submersible"));
        for (String c : PumpRepository.getCategories(this)) out.add(new Option(c, c));
        return out;
    }

    private List<Option> options(String[][] arr) {
        List<Option> out = new ArrayList<>();
        for (String[] a : arr) out.add(new Option(a[0], a[1]));
        return out;
    }

    private String selectedValue(Spinner s) {
        Object item = s.getSelectedItem();
        return item instanceof Option ? ((Option) item).value : String.valueOf(item);
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s.replace(",", "").trim()); }
        catch (Exception e) { return Double.NaN; }
    }
}
