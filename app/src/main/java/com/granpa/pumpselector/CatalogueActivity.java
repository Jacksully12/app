package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CatalogueActivity extends Activity {
    PumpListAdapter adapter;
    EditText search;
    Spinner cat;

    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = Ui.root(this);
        root.addView(Ui.text(this, "Catalogue Search", 25, Ui.TEXT, 1));
        TextView note = Ui.text(this, "Search by model, type, phase or size. ACS1125 finds ACS 1125.", 13, Ui.MUTED, 0);
        Ui.mb(this, note, 10);
        root.addView(note);

        search = Ui.input(this, "", android.text.InputType.TYPE_CLASS_TEXT);
        search.setHint("Search model");
        Ui.mb(this, search, 8);
        root.addView(search);

        cat = Ui.spinner(this, cats());
        Ui.mb(this, cat, 8);
        root.addView(cat);

        adapter = new PumpListAdapter(this);
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setCacheColorHint(0);
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);

        TextWatcher w = new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){refresh();}
            public void afterTextChanged(Editable e){}
        };
        search.addTextChangedListener(w);
        cat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onItemSelected(AdapterView<?>p,View v,int pos,long id){refresh();}
            public void onNothingSelected(AdapterView<?>p){}
        });
        list.setOnItemClickListener((p,v,pos,id)->{
            PumpSelector.Result r=adapter.getResult(pos);
            Intent i=new Intent(this,PumpDetailsActivity.class);
            i.putExtra("id",r.r.id);
            startActivity(i);
        });
        refresh();
    }

    void refresh() {
        adapter.setItems(PumpSelector.catalogue(PumpRepository.getRecords(this), sel(cat), search.getText().toString()));
    }

    List<Option> cats() {
        ArrayList<Option> o = new ArrayList<>();
        o.add(new Option("all", "All pump types"));
        o.add(new Option("monoblock_all", "All Monoblock / Centrifugal"));
        o.add(new Option("submersible_all", "All Submersible"));
        o.add(new Option("borewell_all", "All Borewell Submersible"));
        for (String c : PumpRepository.categories(this)) o.add(new Option(c, c));
        return o;
    }

    String sel(Spinner s) {
        Object o = s.getSelectedItem();
        return o instanceof Option ? ((Option)o).value : String.valueOf(o);
    }
}
