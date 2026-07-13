package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.graphics.Typeface;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CompareResultsActivity extends Activity {
    double head, flow, targetLPH;
    String unit="LPH", cat="borewell_all", phase="any";
    LinearLayout brandArea, highlightArea;
    TextView totalText, emptyText;
    final LinkedHashMap<String,ArrayList<PumpSelector.Result>> brandResults=new LinkedHashMap<>();
    final ArrayList<PumpSelector.Result> bestResults=new ArrayList<>();

    @Override protected void onCreate(Bundle b){
        super.onCreate(b);
        head=getIntent().getDoubleExtra("head",Double.NaN);
        flow=getIntent().getDoubleExtra("flow",Double.NaN);
        unit=PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));
        cat=safe(getIntent().getStringExtra("cat"));if(cat.isEmpty())cat="borewell_all";
        phase=safe(getIntent().getStringExtra("phase"));if(phase.isEmpty())phase="any";
        targetLPH=PumpSelector.toLPH(flow,unit);

        LinearLayout root=Ui.root(this);
        LinearLayout summary=Ui.card(this);
        summary.addView(Ui.text(this,"Brand comparison",25,Ui.TEXT,Typeface.BOLD));
        summary.addView(Ui.text(this,categoryLabel(cat)+" • "+phaseLabel(phase),14,Ui.BLUE,Typeface.BOLD));
        summary.addView(Ui.text(this,"Required duty point: "+PumpSelector.head(head)+" • "+PumpSelector.formatFlow(targetLPH,unit),14,Ui.MUTED,0));
        totalText=Ui.text(this,"",18,Ui.GREEN,Typeface.BOLD);Ui.mb(this,totalText,0);summary.addView(totalText);root.addView(summary);

        LinearLayout nav=Ui.row(this);
        Button edit=Ui.secondary(this,"Edit search");nav.addView(edit,new LinearLayout.LayoutParams(0,-2,1));
        Button shortlist=Ui.secondary(this,"Manual shortlist");LinearLayout.LayoutParams slp=new LinearLayout.LayoutParams(0,-2,1);slp.setMargins(Ui.dp(this,8),0,0,0);nav.addView(shortlist,slp);Ui.mb(this,nav,10);root.addView(nav);

        LinearLayout shareRow=Ui.row(this);
        Button copy=Ui.secondary(this,"Copy comparison");shareRow.addView(copy,new LinearLayout.LayoutParams(0,-2,1));
        Button share=Ui.primary(this,"Share image");LinearLayout.LayoutParams shp=new LinearLayout.LayoutParams(0,-2,1);shp.setMargins(Ui.dp(this,8),0,0,0);shareRow.addView(share,shp);Ui.mb(this,shareRow,12);root.addView(shareRow);

        emptyText=Ui.text(this,"",14,Ui.MUTED,0);emptyText.setVisibility(View.GONE);Ui.mb(this,emptyText,12);root.addView(emptyText);
        highlightArea=new LinearLayout(this);highlightArea.setOrientation(LinearLayout.VERTICAL);root.addView(highlightArea);
        brandArea=new LinearLayout(this);brandArea.setOrientation(LinearLayout.VERTICAL);root.addView(brandArea);

        setContentView(Ui.scroll(this,root));
        edit.setOnClickListener(v->finish());
        shortlist.setOnClickListener(v->startActivity(new Intent(this,ShortlistActivity.class)));
        copy.setOnClickListener(v->copyComparison());
        share.setOnClickListener(v->{if(bestResults.isEmpty())Toast.makeText(this,"No comparison to share",Toast.LENGTH_SHORT).show();else MultiShareImageBuilder.shareComparison(this,head,targetLPH,unit,bestResults);});
        runCompare();
    }

    void runCompare(){
        brandResults.clear();bestResults.clear();brandArea.removeAllViews();highlightArea.removeAllViews();
        if(Double.isNaN(head)||head<0||Double.isNaN(targetLPH)||targetLPH<=0){
            totalText.setText("0 matching brands");totalText.setTextColor(Ui.ORANGE);
            emptyText.setText("Enter a valid head and flow value on the comparison screen.");emptyText.setVisibility(View.VISIBLE);return;
        }
        PumpSelector.Req req=PumpSelector.req(false,flow,flow,unit);
        addBrand("TEXMO",PumpRepository.TEXMO_ASSET,req);
        addBrand("LUBI",PumpRepository.LUBI_ASSET,req);
        addBrand("KSB",PumpRepository.KSB_ASSET,req);
        totalText.setText(bestResults.size()+" of 3 brands have a comparable model");
        totalText.setTextColor(bestResults.isEmpty()?Ui.ORANGE:Ui.GREEN);
        emptyText.setVisibility(bestResults.isEmpty()?View.VISIBLE:View.GONE);
        emptyText.setText(bestResults.isEmpty()?"No comparable model was found within ±50%. Edit the category, phase, head or flow.":"");
        if(!bestResults.isEmpty())highlightArea.addView(highlightCard());
        for(Map.Entry<String,ArrayList<PumpSelector.Result>> e:brandResults.entrySet())brandArea.addView(brandSection(e.getKey(),e.getValue()));
    }

    void addBrand(String brand,String asset,PumpSelector.Req req){
        ArrayList<PumpSelector.Result> rows=PumpSelector.selectForCompare(PumpRepository.getRecords(this,asset),head,req,cat,phase);
        brandResults.put(brand,rows);
        if(!rows.isEmpty())bestResults.add(rows.get(0));
    }

    LinearLayout highlightCard(){
        LinearLayout c=Ui.card(this);
        c.setBackground(Ui.bg(this,Ui.SOFT_BLUE,Ui.BLUE,20));
        c.addView(Ui.text(this,"Comparison highlights",18,Ui.TEXT,Typeface.BOLD));
        PumpSelector.Result closest=Collections.min(bestResults,Comparator.comparingDouble(x->x.diff));
        double minPower=Double.POSITIVE_INFINITY;ArrayList<String> lowPower=new ArrayList<>();
        for(PumpSelector.Result x:bestResults){double p=power(x.r);if(Double.isNaN(p))continue;if(p<minPower-1e-6){minPower=p;lowPower.clear();lowPower.add(x.r.brand);}else if(Math.abs(p-minPower)<1e-6)lowPower.add(x.r.brand);}
        PumpSelector.Result leastOver=null;double over=Double.POSITIVE_INFINITY;
        for(PumpSelector.Result x:bestResults){double signed=x.flow-targetLPH;if(signed>=-1&&signed<over){over=Math.max(0,signed);leastOver=x;}}
        c.addView(Ui.text(this,"Closest duty-point match: "+closest.r.brand+" — "+closest.r.model,14,Ui.GREEN,Typeface.BOLD));
        if(!lowPower.isEmpty())c.addView(Ui.text(this,"Lowest connected power: "+join(lowPower),13,Ui.MUTED,0));
        if(leastOver!=null)c.addView(Ui.text(this,"Least oversupply: "+leastOver.r.brand+" ("+signedText(leastOver)+")",13,Ui.MUTED,0));
        c.addView(Ui.text(this,"These labels compare catalogue duty-point fit and connected power; they do not claim hydraulic efficiency.",12,Ui.MUTED,0));
        return c;
    }

    LinearLayout brandSection(String brand,ArrayList<PumpSelector.Result> rows){
        LinearLayout section=Ui.card(this);
        LinearLayout titleRow=Ui.row(this);
        titleRow.addView(Ui.text(this,brand,21,Ui.BLUE,Typeface.BOLD),new LinearLayout.LayoutParams(0,-2,1));
        titleRow.addView(Ui.badge(this,rows.isEmpty()?"No match":"Best match",rows.isEmpty()?Ui.ORANGE:Ui.GREEN));
        section.addView(titleRow);
        if(rows.isEmpty()){
            section.addView(Ui.text(this,"No model from this brand falls within the current comparison tolerance.",13,Ui.MUTED,0));
            return section;
        }
        section.addView(modelCard(rows.get(0),true));
        if(rows.size()>1){
            Button toggle=Ui.compact(this,"Show "+(rows.size()-1)+" alternative"+(rows.size()==2?"":"s"));
            LinearLayout alternatives=new LinearLayout(this);alternatives.setOrientation(LinearLayout.VERTICAL);alternatives.setVisibility(View.GONE);
            for(int i=1;i<rows.size();i++)alternatives.addView(modelCard(rows.get(i),false));
            toggle.setOnClickListener(v->{boolean show=alternatives.getVisibility()!=View.VISIBLE;alternatives.setVisibility(show?View.VISIBLE:View.GONE);toggle.setText(show?"Hide alternatives":"Show "+(rows.size()-1)+" alternative"+(rows.size()==2?"":"s"));});
            section.addView(toggle);section.addView(alternatives);
        }
        return section;
    }

    LinearLayout modelCard(PumpSelector.Result x,boolean best){
        PumpRecord r=x.r;
        LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(Ui.dp(this,13),Ui.dp(this,12),Ui.dp(this,13),Ui.dp(this,12));
        c.setBackground(Ui.bg(this,android.graphics.Color.WHITE,best?Ui.BLUE:Ui.BORDER,16));
        LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,Ui.dp(this,10),0,Ui.dp(this,10));c.setLayoutParams(cp);
        c.addView(Ui.text(this,r.model,17,Ui.TEXT,Typeface.BOLD));
        if(!safe(r.variantLabel).isEmpty())c.addView(Ui.text(this,r.variantLabel,12,Ui.BLUE,Typeface.BOLD));
        c.addView(Ui.text(this,PumpSelector.trim(r.hp)+" HP • "+PumpSelector.trim(r.kw)+" kW • "+phaseLabel(r.phase),13,Ui.MUTED,0));
        c.addView(Ui.text(this,PumpSelector.formatFlow(x.flow,unit)+" at "+PumpSelector.head(x.head),16,matchColor(x.status),Typeface.BOLD));
        c.addView(Ui.text(this,signedText(x)+" • "+shortStatus(x.status),13,Ui.MUTED,0));
        String foot=(safe(r.displaySize()).isEmpty()?"":r.displaySize()+" • ")+safe(r.category)+" • Page "+r.page;
        c.addView(Ui.text(this,foot,12,Ui.MUTED,0));
        LinearLayout actions=Ui.row(this);
        Button open=Ui.compact(this,"Open details");actions.addView(open,new LinearLayout.LayoutParams(0,-2,1));
        boolean saved=LocalStore.isShortlisted(this,assetForBrand(r.brand),r.id);
        Button save=Ui.compact(this,saved?"★ Saved":"☆ Shortlist");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1);sp.setMargins(Ui.dp(this,8),0,0,0);actions.addView(save,sp);c.addView(actions);
        open.setOnClickListener(v->openDetails(x));
        save.setOnClickListener(v->{boolean now=LocalStore.toggleShortlist(this,assetForBrand(r.brand),r.id,r.brand);save.setText(now?"★ Saved":"☆ Shortlist");});
        return c;
    }

    void openDetails(PumpSelector.Result x){
        if(x==null||x.r==null)return;Intent i=new Intent(this,PumpDetailsActivity.class);i.putExtra("asset",assetForBrand(x.r.brand));i.putExtra("brand",x.r.brand);i.putExtra("id",x.r.id);i.putExtra("head",x.head);i.putExtra("flow",x.flow);i.putExtra("estimate",true);i.putExtra("unit",unit);startActivity(i);
    }

    void copyComparison(){
        if(bestResults.isEmpty()){Toast.makeText(this,"No comparison to copy",Toast.LENGTH_SHORT).show();return;}
        StringBuilder s=new StringBuilder("GRANPA PUMP COMPARISON\n");s.append(categoryLabel(cat)).append(" • ").append(phaseLabel(phase)).append("\nRequired: ").append(PumpSelector.head(head)).append(" • ").append(PumpSelector.formatFlow(targetLPH,unit)).append("\n\n");
        for(PumpSelector.Result x:bestResults){PumpRecord r=x.r;s.append(r.brand).append("\n").append(r.model).append("\n").append(PumpSelector.trim(r.hp)).append(" HP / ").append(PumpSelector.trim(r.kw)).append(" kW\n").append(PumpSelector.formatFlow(x.flow,unit)).append(" at ").append(PumpSelector.head(head)).append("\n").append(signedText(x)).append("\nPage ").append(r.page).append("\n\n");}
        ClipboardManager cm=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);cm.setPrimaryClip(ClipData.newPlainText("Pump comparison",s.toString().trim()));Toast.makeText(this,"Comparison copied",Toast.LENGTH_SHORT).show();
    }

    String signedText(PumpSelector.Result x){double d=x.flow-targetLPH;if(Math.abs(d)<1)return "Exact target";return (d>0?"+":"−")+PumpSelector.formatFlow(Math.abs(d),unit)+(d>0?" above target":" below target");}
    String shortStatus(String s){s=safe(s);int i=s.lastIndexOf("•");return i>=0?s.substring(i+1).trim():s;}
    int matchColor(String s){s=safe(s);if(s.contains("Last")||s.contains("Wide"))return Ui.ORANGE;if(s.contains("Extended"))return Ui.BLUE;return Ui.GREEN;}
    double power(PumpRecord r){if(r==null)return Double.NaN;if(!Double.isNaN(r.kw)&&r.kw>0)return r.kw;if(!Double.isNaN(r.hp)&&r.hp>0)return r.hp*.746;return Double.NaN;}
    String join(List<String> x){StringBuilder b=new StringBuilder();for(String s:x){if(b.length()>0)b.append(" and ");b.append(s);}return b.toString();}
    String assetForBrand(String b){b=safe(b).toUpperCase(Locale.US);if(b.equals("LUBI"))return PumpRepository.LUBI_ASSET;if(b.equals("KSB"))return PumpRepository.KSB_ASSET;return PumpRepository.TEXMO_ASSET;}
    String categoryLabel(String c){c=safe(c);if(c.equals("borewell_all"))return "Borewell Submersible";if(c.equals("openwell_all"))return "Openwell Submersible";if(c.equals("monoblock_all"))return "Surface Monoblock / Jet";if(c.equals("multistage_all"))return "Multistage Pumps";if(c.equals("booster_all"))return "Booster / Pressure Pumps";if(c.equals("dewatering_all"))return "Dewatering / Sewage";return c;}
    String phaseLabel(String p){p=safe(p).toUpperCase(Locale.US);boolean s=p.contains("S"),t=p.contains("T")||p.contains("3");if(s&&t)return "Single / Three Phase";if(s)return "Single Phase";if(t)return "Three Phase";return "Any Phase";}
    String safe(String s){return s==null?"":s.trim();}
}
