package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    EditText head,flow1,flow2,maxHp,maxKw,outlet,maxOversupply;
    Spinner mode,unit,cat,phase;
    LinearLayout dutyBox,flow2Box,advancedBox;
    TextView hint,flow1Label,error;
    CheckBox verifiedOnly,closeOnly;
    Button find,more;
    String asset=PumpRepository.TEXMO_ASSET,brand="TEXMO";

    protected void onCreate(Bundle b){
        super.onCreate(b);
        asset=PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));
        brand=getIntent().getStringExtra("brand");if(empty(brand))brand=PumpRepository.brandName(asset);
        PumpRepository.getRecords(this,asset);

        LinearLayout root=Ui.root(this);root.addView(header());
        LinearLayout card=Ui.card(this);

        card.addView(Ui.step(this,"1","Select product category"));
        cat=Ui.spinner(this,categories());Ui.mb(this,cat,8);card.addView(cat);

        card.addView(Ui.step(this,"2","Select electrical phase"));
        phase=Ui.spinner(this,options(new String[][]{{"any","Any phase","Show all available phase options"},{"S","Single phase","Usually 220–240 V"},{"T","Three phase","Usually 380–415 V"}}));
        Ui.mb(this,phase,8);card.addView(phase);

        dutyBox=new LinearLayout(this);dutyBox.setOrientation(LinearLayout.VERTICAL);
        dutyBox.addView(Ui.step(this,"3","Enter required duty point"));
        dutyBox.addView(Ui.label(this,"Required head"));
        LinearLayout hr=Ui.row(this);head=Ui.input(this,"40",Ui.numberInput());hr.addView(head,new LinearLayout.LayoutParams(0,-2,1));
        TextView mt=Ui.text(this,"m",15,Ui.MUTED,Typeface.BOLD);mt.setPadding(Ui.dp(this,10),0,Ui.dp(this,4),0);hr.addView(mt);dutyBox.addView(hr);

        dutyBox.addView(Ui.label(this,"Flow input mode"));
        mode=Ui.spinner(this,options(new String[][]{{"fixed","Fixed required flow","Select closest models around one flow"},{"range","Acceptable flow range","Select models within a minimum and maximum range"}}));dutyBox.addView(mode);

        flow1Label=Ui.label(this,"Required flow");dutyBox.addView(flow1Label);
        LinearLayout fr=Ui.row(this);flow1=Ui.input(this,"1200",Ui.numberInput());fr.addView(flow1,new LinearLayout.LayoutParams(0,-2,1));
        unit=Ui.spinner(this,options(new String[][]{{"LPH","LPH","Litres per hour"},{"LPM","LPM","Litres per minute"},{"M3H","m³/hour","Cubic metres per hour"},{"LPS","LPS","Litres per second"}}));
        LinearLayout.LayoutParams ulp=new LinearLayout.LayoutParams(Ui.dp(this,140),-2);ulp.setMargins(Ui.dp(this,8),0,0,0);fr.addView(unit,ulp);dutyBox.addView(fr);

        flow2Box=new LinearLayout(this);flow2Box.setOrientation(LinearLayout.VERTICAL);flow2Box.addView(Ui.label(this,"Maximum flow"));flow2=Ui.input(this,"4500",Ui.numberInput());flow2Box.addView(flow2);dutyBox.addView(flow2Box);
        hint=Ui.text(this,"",13,Ui.MUTED,0);hint.setPadding(0,Ui.dp(this,9),0,0);dutyBox.addView(hint);card.addView(dutyBox);

        more=Ui.secondary(this,"More filters");Ui.mb(this,more,8);card.addView(more);
        advancedBox=advancedFilters();advancedBox.setVisibility(View.GONE);card.addView(advancedBox);

        error=Ui.text(this,"",13,Ui.RED,Typeface.BOLD);error.setVisibility(View.GONE);Ui.mb(this,error,8);card.addView(error);
        find=Ui.primary(this,"Find suitable pumps");Ui.mb(this,find,8);card.addView(find);
        Button browse=Ui.blue(this,"Search full catalogue");card.addView(browse);
        root.addView(card);

        LinearLayout quick=Ui.row(this);
        Button shortlist=Ui.compact(this,"Shortlist");quick.addView(shortlist,new LinearLayout.LayoutParams(0,-2,1));
        Button recent=Ui.compact(this,"Recent");LinearLayout.LayoutParams q2=new LinearLayout.LayoutParams(0,-2,1);q2.setMargins(Ui.dp(this,8),0,0,0);quick.addView(recent,q2);root.addView(quick);

        TextView note=Ui.text(this,PumpRepository.note(this,asset),12,Ui.MUTED,0);note.setPadding(0,Ui.dp(this,10),0,0);root.addView(note);
        setContentView(Ui.scroll(this,root));

        AdapterView.OnItemSelectedListener updater=new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int pos,long id){update();}public void onNothingSelected(AdapterView<?>p){}};
        cat.setOnItemSelectedListener(updater);phase.setOnItemSelectedListener(updater);mode.setOnItemSelectedListener(updater);unit.setOnItemSelectedListener(updater);
        more.setOnClickListener(v->{boolean show=advancedBox.getVisibility()!=View.VISIBLE;advancedBox.setVisibility(show?View.VISIBLE:View.GONE);more.setText(show?"Hide filters":"More filters");});
        find.setOnClickListener(v->openResults());browse.setOnClickListener(v->openCatalogue(sel(cat),sel(phase)));
        shortlist.setOnClickListener(v->startActivity(new Intent(this,ShortlistActivity.class)));recent.setOnClickListener(v->startActivity(new Intent(this,RecentActivity.class)));
        applyInitialValues();update();
    }

    LinearLayout header(){
        LinearLayout h=Ui.card(this);LinearLayout row=Ui.row(this);ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.app_logo);row.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
        LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setPadding(Ui.dp(this,12),0,0,0);t.addView(Ui.text(this,"Granpa",28,Ui.TEXT,Typeface.BOLD));t.addView(Ui.text(this,brand+" selector",14,Ui.MUTED,0));row.addView(t,new LinearLayout.LayoutParams(0,-2,1));
        Button change=Ui.compact(this,"Change");row.addView(change,new LinearLayout.LayoutParams(Ui.dp(this,86),Ui.dp(this,42)));change.setOnClickListener(v->{startActivity(new Intent(this,BrandSelectionActivity.class));finish();});h.addView(row);return h;
    }

    LinearLayout advancedFilters(){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(Ui.dp(this,12),Ui.dp(this,4),Ui.dp(this,12),Ui.dp(this,12));box.setBackground(Ui.bg(this,Ui.SOFT_BLUE,Ui.BORDER,16));
        box.addView(Ui.text(this,"Optional filters",15,Ui.BLUE,Typeface.BOLD));
        box.addView(Ui.label(this,"Maximum connected power"));
        LinearLayout p=Ui.row(this);maxHp=Ui.input(this,"",Ui.numberInput());maxHp.setHint("Max HP");p.addView(maxHp,new LinearLayout.LayoutParams(0,-2,1));maxKw=Ui.input(this,"",Ui.numberInput());maxKw.setHint("Max kW");LinearLayout.LayoutParams kp=new LinearLayout.LayoutParams(0,-2,1);kp.setMargins(Ui.dp(this,8),0,0,0);p.addView(maxKw,kp);box.addView(p);
        box.addView(Ui.label(this,"Outlet / technical size contains"));outlet=Ui.input(this,"",android.text.InputType.TYPE_CLASS_TEXT);outlet.setHint("Example: 32 mm or 50 x 40");box.addView(outlet);
        box.addView(Ui.label(this,"Maximum oversupply (%)"));maxOversupply=Ui.input(this,"",Ui.numberInput());maxOversupply.setHint("Example: 20");box.addView(maxOversupply);
        verifiedOnly=new CheckBox(this);verifiedOnly.setText("Source-confirmed or QA-cleared records only");verifiedOnly.setTextColor(Ui.TEXT);verifiedOnly.setChecked(true);box.addView(verifiedOnly);
        closeOnly=new CheckBox(this);closeOnly.setText("Close matches only (within ±10%)");closeOnly.setTextColor(Ui.TEXT);box.addView(closeOnly);
        return box;
    }

    void update(){
        boolean motor=isMotorCategory(sel(cat));dutyBox.setVisibility(motor?View.GONE:View.VISIBLE);find.setText(motor?"Show matching motors":"Find suitable pumps");
        if(motor){closeOnly.setChecked(false);maxOversupply.setText("");return;}
        boolean range="range".equals(sel(mode));flow2Box.setVisibility(range?View.VISIBLE:View.GONE);flow1Label.setText(range?"Minimum flow":"Required flow");
        hint.setText(range?"Enter the minimum and maximum acceptable flow in "+PumpSelector.unitLabel(sel(unit))+".":"The app ranks the closest duty-point matches and keeps wider alternatives clearly labelled.");
    }

    void openResults(){
        if(isMotorCategory(sel(cat))){openCatalogue(sel(cat),sel(phase));return;}
        double h=val(head),f1=val(flow1),f2=val(flow2);boolean range="range".equals(sel(mode));
        if(Double.isNaN(h)||h<=0){showError("Enter a required head greater than 0 m.");return;}
        if(Double.isNaN(f1)||f1<=0){showError("Enter a valid required flow.");return;}
        if(range&&(Double.isNaN(f2)||f2<=0||f2<f1)){showError("Enter a maximum flow that is greater than or equal to the minimum flow.");return;}
        error.setVisibility(View.GONE);
        Intent i=new Intent(this,ResultsActivity.class);i.putExtra("head",h);i.putExtra("range",range);i.putExtra("flow1",f1);i.putExtra("flow2",range?f2:f1);i.putExtra("unit",sel(unit));i.putExtra("cat",sel(cat));i.putExtra("phase",sel(phase));i.putExtra("key","");i.putExtra("asset",asset);i.putExtra("brand",brand);
        i.putExtra("maxHp",valOptional(maxHp));i.putExtra("maxKw",valOptional(maxKw));i.putExtra("outlet",outlet.getText().toString().trim());i.putExtra("verifiedOnly",verifiedOnly.isChecked());i.putExtra("closeOnly",closeOnly.isChecked());i.putExtra("maxOversupply",valOptional(maxOversupply));
        LocalStore.addSearch(this,asset,brand,h,range,f1,range?f2:f1,sel(unit),sel(cat),sel(phase));startActivity(i);
    }

    void showError(String s){error.setText(s);error.setVisibility(View.VISIBLE);}
    void openCatalogue(String initialCat,String initialPhase){Intent ci=new Intent(this,CatalogueActivity.class);ci.putExtra("asset",asset);ci.putExtra("brand",brand);ci.putExtra("initialCat",initialCat);ci.putExtra("initialPhase",initialPhase);startActivity(ci);}

    boolean isMotorCategory(String value){if("motors_all".equals(value))return true;for(PumpRecord r:PumpRepository.getRecords(this,asset))if(value!=null&&value.equals(r.category)&&r.isMotor())return true;return false;}

    List<Option> categories(){
        ArrayList<Option> o=new ArrayList<>();
        if(PumpRepository.TEXMO_ASSET.equals(asset)){
            o.add(new Option("all","All pump types","Main category • full catalogue",true));o.add(new Option("borewell_all","Borewell Submersible","Main category • borewell sections",true));o.add(new Option("openwell_all","Openwell Submersible","Main category • openwell sections",true));o.add(new Option("monoblock_all","Centrifugal / Surface Monoblock","Main category • self priming, jet, centrifugal and agricultural monoblock",true));o.add(new Option("multistage_all","Multistage Pumps","Main category • AVRS, vertical inline and horizontal multistage",true));o.add(new Option("dewatering_all","Dewatering / Sewage","Main category • sewage and dewatering pumps",true));o.add(new Option("motors_all","Motors","Main category • 35 bare and flange motor models",true));
        }else if(PumpRepository.LUBI_ASSET.equals(asset)){
            o.add(new Option("all","All pump types","Main category • full Lubi catalogue",true));o.add(new Option("borewell_all","Borewell Submersible","Main category • borewell sections",true));o.add(new Option("openwell_all","Openwell Submersible","Main category • openwell sections",true));o.add(new Option("monoblock_all","Centrifugal / Surface Monoblock","Main category • self priming, jet and monoblock sections",true));o.add(new Option("multistage_all","Multistage Pumps","Main category • horizontal and vertical multistage",true));o.add(new Option("booster_all","Booster / Pressure Pumps","Main category • booster and pressure sections",true));o.add(new Option("dewatering_all","Dewatering / Sewage","Main category • drainage, sewage and dewatering",true));
        }else{
            o.add(new Option("all","All pump types","Main category • full KSB catalogue",true));o.add(new Option("borewell_all","Borewell Submersible","Main category • water-filled and oil-filled borewell sections",true));o.add(new Option("openwell_all","Openwell Submersible","Main category • openwell sections",true));o.add(new Option("monoblock_all","Centrifugal / Surface Monoblock","Main category • self priming, jet, monobloc and surface sections",true));o.add(new Option("multistage_all","Multistage Pumps","Main category • multistage sections",true));o.add(new Option("booster_all","Booster / Pressure Pumps","Main category • booster and pressure sections",true));o.add(new Option("dewatering_all","Dewatering / Sewage","Main category • drainage and sewage sections",true));
        }
        for(String c:PumpRepository.categories(this,asset))o.add(new Option(c,"Sub category • "+c,detail(c),false));return o;
    }

    String detail(String c){c=safe(c).toLowerCase(Locale.US);if(c.contains("openwell"))return"Detailed openwell catalogue section";if(c.contains("borewell"))return"Detailed borewell catalogue section";if(c.contains("avrs")||c.contains("multistage"))return"Detailed multistage catalogue section";if(c.contains("motor"))return"Detailed motor catalogue section";if(c.contains("agricultural")||c.contains("centrifugal")||c.contains("jet")||c.contains("self priming")||c.contains("monobloc")||c.contains("surface"))return"Detailed surface / monoblock section";if(c.contains("dewatering")||c.contains("sewage"))return"Detailed dewatering / sewage section";return"Detailed catalogue section";}

    void applyInitialValues(){Intent i=getIntent();if(i.hasExtra("initialHead"))head.setText(PumpSelector.trim(i.getDoubleExtra("initialHead",40)));if(i.hasExtra("initialFlow1"))flow1.setText(PumpSelector.trim(i.getDoubleExtra("initialFlow1",1200)));if(i.hasExtra("initialFlow2"))flow2.setText(PumpSelector.trim(i.getDoubleExtra("initialFlow2",4500)));selectValue(unit,i.getStringExtra("initialUnit"));selectValue(cat,i.getStringExtra("initialCat"));selectValue(phase,i.getStringExtra("initialPhase"));selectValue(mode,i.getBooleanExtra("initialRange",false)?"range":"fixed");}
    void selectValue(Spinner s,String value){if(s==null||empty(value))return;for(int n=0;n<s.getCount();n++){Object o=s.getItemAtPosition(n);if(o instanceof Option&&value.equals(((Option)o).value)){s.setSelection(n);return;}}}
    List<Option> options(String[][] a){ArrayList<Option> o=new ArrayList<>();for(String[]x:a)o.add(new Option(x[0],x[1],x.length>2?x[2]:""));return o;}
    String sel(Spinner s){if(s==null||s.getSelectedItem()==null)return"";Object o=s.getSelectedItem();return o instanceof Option?((Option)o).value:String.valueOf(o);}
    double val(EditText e){try{return Double.parseDouble(e.getText().toString().replace(",","").trim());}catch(Exception x){return Double.NaN;}}
    double valOptional(EditText e){double v=val(e);return Double.isNaN(v)?-1:v;}
    String safe(String s){return s==null?"":s.trim();}boolean empty(String s){return safe(s).isEmpty();}
}
