package com.granpa.pumpselector;

import android.app.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PumpListAdapter extends BaseAdapter {
    Activity a;
    ArrayList<PumpSelector.Result> items=new ArrayList<>();
    HashSet<String> collapsedGroups=new HashSet<>();
    String displayUnit="LPH";

    public PumpListAdapter(Activity a){this.a=a;}
    public void setCollapsedGroups(Set<String> groups){collapsedGroups.clear();if(groups!=null)collapsedGroups.addAll(groups);}
    public void setDisplayUnit(String unit){displayUnit=PumpSelector.normalizeUnit(unit);notifyDataSetChanged();}
    public void setItems(List<PumpSelector.Result> l){items.clear();if(l!=null)items.addAll(l);notifyDataSetChanged();}
    public PumpSelector.Result getResult(int p){return items.get(p);}
    public int getCount(){return items.size();}public Object getItem(int p){return items.get(p);}public long getItemId(int p){return p;}

    public View getView(int pos,View cv,ViewGroup parent){
        PumpSelector.Result it=items.get(pos);
        if(it.header)return header(it,pos);
        PumpRecord r=it.r;String u=it.unit==null?displayUnit:it.unit;
        LinearLayout c=Ui.card(a);c.setPadding(Ui.dp(a,14),Ui.dp(a,12),Ui.dp(a,14),Ui.dp(a,12));

        LinearLayout titleRow=Ui.row(a);
        TextView title=Ui.text(a,safe(r.model),20,Ui.TEXT,Typeface.BOLD);titleRow.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        if(LocalStore.isShortlisted(a,assetForBrand(r.brand),r.id))titleRow.addView(Ui.text(a,"★",20,Ui.ORANGE,Typeface.BOLD));
        c.addView(titleRow);
        if(!safe(r.variantLabel).isEmpty())c.addView(Ui.text(a,safe(r.variantLabel),12,Ui.MUTED,0));

        String power=PumpSelector.trim(r.hp)+" HP • "+PumpSelector.trim(r.kw)+" kW • "+phaseLong(r.phase);
        c.addView(Ui.text(a,power,13,Ui.MUTED,0));

        if(r.isMotor()){
            c.addView(Ui.text(a,dash(r.rpm)+" RPM • Class "+dash(r.insulationClass)+" • Frame "+dash(r.frameSize),15,Ui.BLUE,Typeface.BOLD));
        }else if(it.estimate){
            int color=matchColor(it.status);
            c.addView(Ui.text(a,PumpSelector.formatFlow(it.flow,u)+" at "+PumpSelector.head(it.head),17,color,Typeface.BOLD));
            c.addView(Ui.text(a,safe(it.status),13,color,Typeface.BOLD));
        }else{
            c.addView(Ui.text(a,"Head "+safe(r.headRangeText)+" m • Flow "+rangeFlow(r,u),14,Ui.BLUE,Typeface.BOLD));
        }

        String tech=r.technicalSummary();
        if(!safe(tech).isEmpty())c.addView(Ui.text(a,tech,12,Ui.MUTED,0));
        else if(!safe(r.displaySize()).isEmpty())c.addView(Ui.text(a,"Size "+r.displaySize(),12,Ui.MUTED,0));

        LinearLayout foot=Ui.row(a);
        String left=safe(r.category);TextView cat=Ui.text(a,left,13,Ui.BLUE,Typeface.BOLD);foot.addView(cat,new LinearLayout.LayoutParams(0,-2,1));
        foot.addView(Ui.text(a,safe(r.brand)+" • Page "+r.page,12,Ui.MUTED,0));c.addView(foot);

        if(!r.selectable||"NEEDS_REVIEW".equals(r.dataStatus))c.addView(Ui.text(a,"⚠ Source verification required",12,Ui.ORANGE,Typeface.BOLD));
        else if("SOURCE_CONFIRMED".equals(r.dataStatus))c.addView(Ui.text(a,"✓ Source confirmed",11,Ui.GREEN,Typeface.BOLD));
        return c;
    }

    View header(PumpSelector.Result it,int pos){
        String base=baseTitle(it.groupTitle);boolean collapsed=collapsedGroups.contains(base);
        LinearLayout h=Ui.card(a);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,pos==0?0:Ui.dp(a,14),0,Ui.dp(a,8));h.setLayoutParams(lp);h.setPadding(Ui.dp(a,14),Ui.dp(a,11),Ui.dp(a,14),Ui.dp(a,11));h.setBackground(Ui.bg(a,Color.rgb(242,247,253),Ui.BORDER,17));
        LinearLayout top=Ui.row(a);top.addView(Ui.text(a,safe(it.groupTitle),17,Ui.BLUE,Typeface.BOLD),new LinearLayout.LayoutParams(0,-2,1));top.addView(Ui.text(a,collapsed?"▼":"▲",15,Ui.MUTED,Typeface.BOLD));h.addView(top);return h;
    }

    String rangeFlow(PumpRecord r,String unit){if(!Double.isNaN(r.minFlowLPH)&&!Double.isNaN(r.maxFlowLPH))return PumpSelector.formatFlowNumber(PumpSelector.fromLPH(r.minFlowLPH,unit),unit)+" – "+PumpSelector.formatFlow(r.maxFlowLPH,unit);return safe(r.dischargeRangeText)+" "+safe(r.flowUnitOriginal);}
    String phaseLong(String p){p=safe(p).toUpperCase(Locale.US);boolean s=p.contains("S"),t=p.contains("T")||p.contains("3");if(s&&t)return"Single / Three Phase";if(s)return"Single Phase";if(t)return"Three Phase";return"Phase -";}
    int matchColor(String status){String s=status==null?"":status;if(s.contains("Last option")||s.contains("Wide match"))return Ui.ORANGE;if(s.contains("Extended match"))return Ui.BLUE;return Ui.GREEN;}
    String assetForBrand(String b){b=safe(b).toUpperCase(Locale.US);if("LUBI".equals(b))return PumpRepository.LUBI_ASSET;if("KSB".equals(b))return PumpRepository.KSB_ASSET;return PumpRepository.TEXMO_ASSET;}
    String baseTitle(String s){return s==null?"":s.replaceAll("\\s+•\\s+\\d+\\s+models?$","").trim();}
    String safe(String s){return s==null?"":s.trim();}String dash(String s){s=safe(s);return s.isEmpty()?"-":s;}
}
