package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ResultsActivity extends Activity {
    PumpListAdapter adapter;
    ArrayList<PumpSelector.Result> source=new ArrayList<>(),filteredBase=new ArrayList<>();
    HashSet<String> collapsedGroups=new HashSet<>();
    double head,targetLPH,maxHp,maxKw,maxOversupply;
    String unit="LPH",asset=PumpRepository.TEXMO_ASSET,brand="TEXMO",selectedCat="all",selectedPhase="any",reqLabel="",outletFilter="",currentQuery="";
    boolean verifiedOnly,closeOnly,range;
    TextView resultCount,emptyMessage,summaryLine;
    Spinner sort;
    EditText search;
    ListView list;
    int restorePosition=0,restoreTop=0;

    protected void onCreate(Bundle b){
        super.onCreate(b);Intent in=getIntent();
        head=in.getDoubleExtra("head",Double.NaN);unit=PumpSelector.normalizeUnit(in.getStringExtra("unit"));asset=PumpRepository.normalizeAsset(in.getStringExtra("asset"));brand=in.getStringExtra("brand");if(empty(brand))brand=PumpRepository.brandName(asset);
        range=in.getBooleanExtra("range",false);double f1=in.getDoubleExtra("flow1",Double.NaN),f2=in.getDoubleExtra("flow2",f1);PumpSelector.Req req=PumpSelector.req(range,f1,f2,unit);targetLPH=range?(PumpSelector.toLPH(f1,unit)+PumpSelector.toLPH(f2,unit))/2d:PumpSelector.toLPH(f1,unit);
        selectedCat=safe(in.getStringExtra("cat"));selectedPhase=safe(in.getStringExtra("phase"));reqLabel=req==null?"":req.label;
        maxHp=in.getDoubleExtra("maxHp",-1);maxKw=in.getDoubleExtra("maxKw",-1);maxOversupply=in.getDoubleExtra("maxOversupply",-1);outletFilter=safe(in.getStringExtra("outlet"));verifiedOnly=in.getBooleanExtra("verifiedOnly",true);closeOnly=in.getBooleanExtra("closeOnly",false);
        source="all".equals(selectedCat)?PumpSelector.selectAllMainGroups(PumpRepository.getRecords(this,asset),head,req,selectedPhase,in.getStringExtra("key")):PumpSelector.select(PumpRepository.getRecords(this,asset),head,req,selectedCat,selectedPhase,in.getStringExtra("key"));
        filteredBase=advancedFilter(source);

        LinearLayout root=Ui.root(this);
        LinearLayout sum=Ui.card(this);LinearLayout titleRow=Ui.row(this);titleRow.addView(Ui.text(this,brand+" Results",24,Ui.TEXT,1),new LinearLayout.LayoutParams(0,-2,1));Button edit=Ui.compact(this,"Edit");titleRow.addView(edit,new LinearLayout.LayoutParams(Ui.dp(this,76),Ui.dp(this,40)));sum.addView(titleRow);
        summaryLine=Ui.text(this,categoryLabel(selectedCat)+" • "+phaseLabel(selectedPhase)+"\n"+PumpSelector.head(head)+" • "+(req==null?"Invalid flow":req.label),13,Ui.MUTED,0);sum.addView(summaryLine);
        int matches=PumpSelector.realCount(filteredBase);resultCount=Ui.text(this,matches+" matching models",21,matches==0?Ui.ORANGE:Ui.GREEN,1);sum.addView(resultCount);root.addView(sum);

        search=Ui.input(this,"",android.text.InputType.TYPE_CLASS_TEXT);search.setHint("Search within results");Ui.mb(this,search,8);root.addView(search);
        LinearLayout tools=Ui.row(this);sort=Ui.spinner(this,sortOptions());tools.addView(sort,new LinearLayout.LayoutParams(0,-2,1));Button copy=Ui.secondary(this,"Copy");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(Ui.dp(this,94),Ui.dp(this,48));cp.setMargins(Ui.dp(this,8),0,0,0);tools.addView(copy,cp);Ui.mb(this,tools,8);root.addView(tools);

        emptyMessage=Ui.text(this,"",14,Ui.MUTED,0);root.addView(emptyMessage);
        adapter=new PumpListAdapter(this);adapter.setDisplayUnit(unit);list=new ListView(this);list.setDivider(null);list.setCacheColorHint(0);list.setAdapter(adapter);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);

        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){currentQuery=s.toString();refresh();}public void afterTextChanged(Editable e){}});
        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?>p,View v,int pos,long id){refresh();}public void onNothingSelected(AdapterView<?>p){}});
        list.setOnItemClickListener((p,v,pos,id)->{PumpSelector.Result r=adapter.getResult(pos);if(r!=null&&r.header){String base=baseTitle(r.groupTitle);if(collapsedGroups.contains(base))collapsedGroups.remove(base);else collapsedGroups.add(base);refresh();return;}openDetails(r);});
        edit.setOnClickListener(v->finish());copy.setOnClickListener(v->copyResults());refresh();
    }

    ArrayList<PumpSelector.Result> advancedFilter(List<PumpSelector.Result> rows){
        ArrayList<PumpSelector.Result> out=new ArrayList<>();PumpSelector.Result header=null;ArrayList<PumpSelector.Result> group=new ArrayList<>();
        for(PumpSelector.Result x:rows){if(x.header){flushAdvanced(out,header,group);header=x;group=new ArrayList<>();continue;}if(pass(x))group.add(x);}flushAdvanced(out,header,group);return out;
    }
    void flushAdvanced(ArrayList<PumpSelector.Result> out,PumpSelector.Result h,List<PumpSelector.Result> g){if(g==null||g.isEmpty())return;if(h!=null)out.add(PumpSelector.header(baseTitle(h.groupTitle)+" • "+g.size()+" models"));out.addAll(g);}
    boolean pass(PumpSelector.Result x){if(x==null||x.r==null)return false;PumpRecord r=x.r;if(maxHp>0&&!Double.isNaN(r.hp)&&r.hp>maxHp+.0001)return false;if(maxKw>0&&!Double.isNaN(r.kw)&&r.kw>maxKw+.0001)return false;if(verifiedOnly&&(!r.selectable||"NEEDS_REVIEW".equals(r.dataStatus)))return false;if(!empty(outletFilter)){String hay=(r.displaySize()+" "+r.technicalSummary()+" "+r.category).toLowerCase(Locale.US);if(!hay.contains(outletFilter.toLowerCase(Locale.US)))return false;}if(x.estimate&&!range&&targetLPH>0){double pct=Math.abs(x.flow-targetLPH)/targetLPH*100d;if(closeOnly&&pct>10.0001)return false;if(maxOversupply>=0&&x.flow>targetLPH*(1d+maxOversupply/100d))return false;}return true;}

    void refresh(){
        ArrayList<PumpSelector.Result> sorted=sortRows(filteredBase,sel(sort));String q=safe(currentQuery).toLowerCase(Locale.US);ArrayList<PumpSelector.Result> out=new ArrayList<>();PumpSelector.Result header=null;ArrayList<PumpSelector.Result> group=new ArrayList<>();
        for(PumpSelector.Result x:sorted){if(x.header){flushVisible(out,header,group,q);header=x;group=new ArrayList<>();continue;}if(q.isEmpty()||PumpSelector.kw(x.r,q))group.add(x);}flushVisible(out,header,group,q);
        int count=PumpSelector.realCount(out);resultCount.setText(count+" matching models");resultCount.setTextColor(count==0?Ui.ORANGE:Ui.GREEN);emptyMessage.setVisibility(count==0?View.VISIBLE:View.GONE);emptyMessage.setText(count==0?"No model remains after these filters. Edit the search, remove optional filters, or browse the full catalogue.":"");adapter.setCollapsedGroups(collapsedGroups);adapter.setItems(out);
    }
    void flushVisible(ArrayList<PumpSelector.Result> out,PumpSelector.Result h,List<PumpSelector.Result> g,String q){if(g==null||g.isEmpty())return;if(h!=null){String base=baseTitle(h.groupTitle);out.add(PumpSelector.header(base+" • "+g.size()+" models"));if(!collapsedGroups.contains(base))out.addAll(g);}else out.addAll(g);}

    ArrayList<PumpSelector.Result> sortRows(List<PumpSelector.Result> input,String mode){
        ArrayList<PumpSelector.Result> out=new ArrayList<>();PumpSelector.Result header=null;ArrayList<PumpSelector.Result> group=new ArrayList<>();for(PumpSelector.Result x:input){if(x.header){flushSorted(out,header,group,mode);header=x;group=new ArrayList<>();}else group.add(x);}flushSorted(out,header,group,mode);return out;
    }
    void flushSorted(ArrayList<PumpSelector.Result> out,PumpSelector.Result h,ArrayList<PumpSelector.Result> g,String mode){if(g.isEmpty())return;g.sort(comparator(mode));if(h!=null)out.add(PumpSelector.header(baseTitle(h.groupTitle)+" • "+g.size()+" models"));out.addAll(g);}
    Comparator<PumpSelector.Result> comparator(String mode){return (a,b)->{if("hp".equals(mode))return compare(a.r.hp,b.r.hp);if("kw".equals(mode))return compare(a.r.kw,b.r.kw);if("flow".equals(mode))return -compare(a.flow,b.flow);if("model".equals(mode))return safe(a.r.model).compareToIgnoreCase(safe(b.r.model));double da=Math.abs(a.flow-targetLPH),db=Math.abs(b.flow-targetLPH);int c=compare(da,db);if(c!=0)return c;return compare(a.r.hp,b.r.hp);};}
    int compare(double a,double b){if(Double.isNaN(a))return 1;if(Double.isNaN(b))return-1;return Double.compare(a,b);}

    void openDetails(PumpSelector.Result r){if(r==null||r.header||r.r==null)return;restorePosition=list.getFirstVisiblePosition();View v=list.getChildAt(0);restoreTop=v==null?0:v.getTop();Intent i=new Intent(this,PumpDetailsActivity.class);i.putExtra("id",r.r.id);i.putExtra("head",r.head);i.putExtra("flow",r.flow);i.putExtra("estimate",r.estimate);i.putExtra("unit",unit);i.putExtra("asset",asset);i.putExtra("brand",brand);startActivity(i);}
    protected void onResume(){super.onResume();if(list!=null)list.post(()->list.setSelectionFromTop(restorePosition,restoreTop));if(adapter!=null)adapter.notifyDataSetChanged();}

    void copyResults(){
        ArrayList<PumpSelector.Result> rows=new ArrayList<>();for(int i=0;i<adapter.getCount();i++){PumpSelector.Result r=adapter.getResult(i);if(!r.header)rows.add(r);}StringBuilder sb=new StringBuilder("Granpa ").append(brand).append(" Pump Selector\n\n");sb.append("Requirement: ").append(PumpSelector.head(head)).append(" • ").append(reqLabel).append("\n");sb.append("Type: ").append(categoryLabel(selectedCat)).append(" • ").append(phaseLabel(selectedPhase)).append("\n\n");int n=1;for(PumpSelector.Result x:rows){PumpRecord r=x.r;sb.append(n++).append(". ").append(r.model).append("\n");sb.append(PumpSelector.trim(r.hp)).append(" HP / ").append(PumpSelector.trim(r.kw)).append(" kW");if(!empty(r.displaySize()))sb.append(" • ").append(r.displaySize());sb.append("\n");if(x.estimate)sb.append(PumpSelector.formatFlow(x.flow,unit)).append(" at ").append(PumpSelector.head(x.head)).append("\n").append(x.status).append("\n");sb.append(r.brand).append(" • Page ").append(r.page).append("\n\n");}
        ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("granpa-results",sb.toString().trim()));Toast.makeText(this,"Results copied",Toast.LENGTH_SHORT).show();
    }

    List<Option> sortOptions(){ArrayList<Option>o=new ArrayList<>();o.add(new Option("closest","Closest duty-point match","Smallest flow difference"));o.add(new Option("hp","Lowest HP","Lower connected horsepower first"));o.add(new Option("kw","Lowest kW","Lower connected power first"));o.add(new Option("flow","Highest flow","Higher calculated flow first"));o.add(new Option("model","Model name","Alphabetical order"));return o;}
    String sel(Spinner s){Object o=s==null?null:s.getSelectedItem();return o instanceof Option?((Option)o).value:o==null?"":String.valueOf(o);}
    String baseTitle(String s){return s==null?"":s.replaceAll("\\s+•\\s+\\d+\\s+models?$","").trim();}
    String categoryLabel(String c){c=safe(c);if(c.equals("all"))return"All pump types";if(c.equals("borewell_all"))return"Borewell Submersible";if(c.equals("openwell_all"))return"Openwell Submersible";if(c.equals("monoblock_all"))return"Centrifugal / Surface Monoblock";if(c.equals("multistage_all"))return"Multistage Pumps";if(c.equals("booster_all"))return"Booster / Pressure Pumps";if(c.equals("dewatering_all"))return"Dewatering / Sewage";if(c.equals("motors_all"))return"Motors";return c;}
    String phaseLabel(String p){p=safe(p);if(p.equals("S"))return"Single phase";if(p.equals("T"))return"Three phase";return"Any phase";}
    String safe(String s){return s==null?"":s.trim();}boolean empty(String s){return safe(s).isEmpty();}
}
