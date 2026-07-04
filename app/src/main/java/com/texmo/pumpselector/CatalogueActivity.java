package com.texmo.pumpselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CatalogueActivity extends Activity {
    private PumpListAdapter adapter;
    private EditText search;
    private Spinner categorySpinner;
    private Spinner brandSpinner;
    private TextView countText;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Catalogue Search", 25, Ui.TEXT, android.graphics.Typeface.BOLD));
        TextView note = Ui.text(this, "Search model number without spacing also works. Example: ACS1125 finds ACS 1125.", 13, Ui.MUTED, android.graphics.Typeface.NORMAL);
        Ui.addMarginBottom(this, note, 10);
        root.addView(note);

        search = Ui.input(this, "", InputType.TYPE_CLASS_TEXT);
        search.setHint("Search model, category, size, brand");
        Ui.addMarginBottom(this, search, 8);
        root.addView(search);

        LinearLayout filterRow = Ui.row(this);
        categorySpinner = Ui.spinner(this, categoryOptions());
        brandSpinner = Ui.spinner(this, brandOptions());
        filterRow.addView(categorySpinner, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        brandLp.setMargins(Ui.dp(this, 10), 0, 0, 0);
        filterRow.addView(brandSpinner, brandLp);
        Ui.addMarginBottom(this, filterRow, 10);
        root.addView(filterRow);

        countText = Ui.text(this, "", 14, Ui.MUTED, android.graphics.Typeface.BOLD);
        root.addView(countText);

        adapter = new PumpListAdapter(this);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        setContentView(root);

        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable s) {}
        };
        search.addTextChangedListener(watcher);
        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) { refresh(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { refresh(); }
        };
        categorySpinner.setOnItemSelectedListener(listener);
        brandSpinner.setOnItemSelectedListener(listener);
        list.setOnItemClickListener((parent, view, position, id) -> {
            PumpSelector.Result result = adapter.getResult(position);
            Intent i = new Intent(this, PumpDetailsActivity.class);
            i.putExtra("recordId", result.record.id);
            startActivity(i);
        });
        refresh();
    }

    private void refresh() {
        String category = selectedValue(categorySpinner);
        String brand = selectedValue(brandSpinner);
        String keyword = search == null ? "" : search.getText().toString();
        List<PumpSelector.Result> results = PumpSelector.catalogue(PumpRepository.getRecords(this), category, "any", brand, keyword);
        adapter.setItems(results);
        countText.setText(results.size() + " models shown");
    }

    private List<Option> categoryOptions() {
        List<Option> out = new ArrayList<>();
        out.add(new Option("all", "All pump types"));
        out.add(new Option("monoblock_all", "All Monoblock / Centrifugal"));
        out.add(new Option("submersible_all", "All Submersible"));
        out.add(new Option("borewell_all", "All Borewell"));
        for (String c : PumpRepository.getCategories(this)) out.add(new Option(c, c));
        return out;
    }

    private List<Option> brandOptions() {
        List<Option> out = new ArrayList<>();
        out.add(new Option("any", "Any brand"));
        for (String b : PumpRepository.getBrands(this)) out.add(new Option(b, b));
        return out;
    }

    private String selectedValue(Spinner s) {
        Object item = s.getSelectedItem();
        return item instanceof Option ? ((Option) item).value : String.valueOf(item);
    }
}
