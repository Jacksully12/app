package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CompareActivity extends Activity {
    EditText head,flow;Spinner unit,cat,phase;TextView error;
    @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.root(this);
        LinearLayout header=Ui.card(this);header.addView(Ui.text(this,"Compare brands",25,Ui.TEXT,Typeface.BOLD));header.addView(Ui.text(this,"Compare like-for-like Texmo, Lubi and KSB models at one duty point.",14,Ui.MUTED,0));root.addView(header);
        LinearLayout card=Ui.card(this);
        card.addView(Ui.step(this,"1","Select pump type"));cat=Ui.spinner(this,categories());card.addView(cat);
        card.addView(Ui.step(this,"2","Select phase"));phase=Ui.spinner(this,options(new String[][]{{"any","Any phase","Single and three phase"},{"S","Single phase","Usually 220 V"},{"T","Three phase","Usually 380/415 V"}}));card.addView(phase);
        card.addView(Ui.step(this,"3","Enter duty point"));card.addView(Ui.label(this,"Required head"));LinearLayout hr=Ui.row(this);head=Ui.input(this,"40",Ui.numberInput());hr.addView(head,new LinearLayout.LayoutParams(0,-2,1));TextView m=Ui.text(this,"metres",13,Ui.MUTED,Typeface.BOLD);m.setPadding(Ui.dp(this,10),0,0,0);hr.addView(m);card.addView(hr);
        card.addView(Ui.label(this,"Required flow"));LinearLayout fr=Ui.row(this);flow=Ui.input(this,"1200",Ui.numberInput());fr.addView(flow,new LinearLayout.LayoutParams(0,-2,1));unit=Ui.spinner(this,options(new String[][]{{"LPH","LPH","Litres per hour"},{"LPM","LPM","Litres per minute"},{"M3H","m³/hour","Cubic metres per hour"},{"LPS","LPS","Litres per second"}}));LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(Ui.dp(this,145),-2);up.setMargins(Ui.dp(this,8),0,0,0);fr.addView(unit,up);card.addView(fr);
        error=Ui.text(this,"",13,Ui.RED,Typeface.BOLD);error.setVisibility(View.GONE);Ui.mb(this,error,8);card.addView(error);
        Button compare=Ui.primary(this,"Compare Texmo, Lubi and KSB");card.addView(compare);root.addView(card);
        LinearLayout manual=Ui.card(this);manual.addView(Ui.text(this,"Manual model comparison",18,Ui.TEXT,Typeface.BOLD));manual.addView(Ui.text(this,"Save models from any brand to the shortlist, then review and share them together as one image.",13,Ui.MUTED,0));Button openShortlist=Ui.secondary(this,"Open customer shortlist");Ui.mb(this,openShortlist,0);manual.addView(openShortlist);root.addView(manual);
        setContentView(Ui.scroll(this,root));compare.setOnClickListener(v->openResults());openShortlist.setOnClickListener(v->startActivity(new Intent(this,ShortlistActivity.class)));
    }
    void openResults(){double h=val(head),f=val(flow);if(Double.isNaN(h)||h<0){showError("Enter a valid head of 0 metres or more.");head.requestFocus();return;}if(Double.isNaN(f)||f<=0){showError("Enter a flow greater than zero.");flow.requestFocus();return;}error.setVisibility(View.GONE);Intent i=new Intent(this,CompareResultsActivity.class);i.putExtra("head",h);i.putExtra("flow",f);i.putExtra("unit",sel(unit));i.putExtra("cat",sel(cat));i.putExtra("phase",sel(phase));startActivity(i);}
    void showError(String s){error.setText(s);error.setVisibility(View.VISIBLE);}
    List<Option> categories(){ArrayList<Option> o=new ArrayList<>();o.add(new Option("borewell_all","Borewell Submersible","Like-for-like borewell models",true));o.add(new Option("openwell_all","Openwell Submersible","Openwell pump models",true));o.add(new Option("monoblock_all","Surface Monoblock / Jet","Self-priming, jet and centrifugal",true));o.add(new Option("multistage_all","Multistage Pumps","Vertical, horizontal and openwell multistage",true));o.add(new Option("booster_all","Booster / Pressure Pumps","Pressure boosting systems",true));o.add(new Option("dewatering_all","Dewatering / Sewage","Drainage and sewage models",true));return o;}
    List<Option> options(String[][] a){ArrayList<Option> o=new ArrayList<>();for(String[] x:a)o.add(new Option(x[0],x[1],x.length>2?x[2]:"",true));return o;}
    String sel(Spinner s){Object x=s.getSelectedItem();return x instanceof Option?((Option)x).value:"";}
    double val(EditText e){try{return Double.parseDouble(e.getText().toString().trim());}catch(Exception ex){return Double.NaN;}}
}
