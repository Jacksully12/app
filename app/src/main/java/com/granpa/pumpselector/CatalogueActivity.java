package com.granpa.pumpselector;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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
    private Spinner category;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Catalogue Search", 25, Ui.TEXT, 1));
        TextView note = Ui.text(this, "Search by model, pump type, phase or size. ACS1125 finds ACS 1125.", 13, Ui.MUTED, 0);
        Ui.mb(this, note, 10);
        root.addView(note);

        search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search model");
        Ui.mb(this, search, 10);
        root.addView(search);

        category = Ui.spinner(this, categoryOptions());
        Ui.mb(this, category, 10);
        root.addView(category);

        adapter = new PumpListAdapter(this);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        TextWatcher w = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { refresh(); }
            @Override public void afterTextChanged(Editable e) {}
        };
        search.addTextChangedListener(w);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { refresh(); }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
        list.setOnItemClickListener((p, v, pos, id) -> {
            PumpSelector.Result r = adapter.getResult(pos);
            Intent intent = new Intent(this, PumpDetailsActivity.class);
            intent.putExtra("id", r.r.id);
            startActivity(intent);
        });
        refresh();
    }

    private void refresh() {
        adapter.setItems(PumpSelector.catalogue(PumpRepository.getRecords(this), selected(category), search.getText().toString()));
    }

    private List<Option> categoryOptions() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types"));
        o.add(new Option("monoblock_all", "All Monoblock / Centrifugal"));
        o.add(new Option("submersible_all", "All Submersible"));
        o.add(new Option("borewell_all", "All Borewell Submersible"));
        for (String c : PumpRepository.categories(this)) o.add(new Option(c, c));
        return o;
    }

    private String selected(Spinner s) {
        Object item = s.getSelectedItem();
        return item instanceof Option ? ((Option) item).value : String.valueOf(item);
    }
}
