package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.*;
import java.util.*;

public class RecentActivity extends Activity {
    LinearLayout root,content;
    protected void onCreate(Bundle b){super.onCreate(b);root=Ui.root(this);LinearLayout header=Ui.card(this);header.addView(Ui.text(this,"Recent activity",24,Ui.TEXT,Typeface.BOLD));header.addView(Ui.text(this,"Reopen recently viewed models or repeat a saved duty-point search.",13,Ui.MUTED,0));root.addView(header);Button clear=Ui.secondary(this,"Clear history");Ui.mb(this,clear,10);root.addView(clear);content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);root.addView(content);clear.setOnClickListener(v->{LocalStore.clearRecent(this);load();});setContentView(Ui.scroll(this,root));load();}
    protected void onResume(){super.onResume();load();}
    void load(){if(content==null)return;content.removeAllViews();ArrayList<LocalStore.SavedRef> recent=LocalStore.recent(this);ArrayList<LocalStore.SearchRef> searches=LocalStore.searches(this);
        content.addView(Ui.text(this,"Recently viewed models",18,Ui.TEXT,Typeface.BOLD));if(recent.isEmpty())content.addView(Ui.text(this,"No recently viewed models.",13,Ui.MUTED,0));for(LocalStore.SavedRef ref:recent){PumpRecord r=PumpRepository.findById(this,ref.asset,ref.id);if(r!=null)content.addView(modelCard(r,ref.asset,ref.time));}
        TextView sh=Ui.text(this,"Recent searches",18,Ui.TEXT,Typeface.BOLD);sh.setPadding(0,Ui.dp(this,8),0,0);content.addView(sh);if(searches.isEmpty())content.addView(Ui.text(this,"No recent searches.",13,Ui.MUTED,0));for(LocalStore.SearchRef s:searches)content.addView(searchCard(s));}
    View modelCard(PumpRecord r,String asset,long time){LinearLayout c=Ui.card(this);c.setPadding(Ui.dp(this,14),Ui.dp(this,11),Ui.dp(this,14),Ui.dp(this,11));c.addView(Ui.text(this,r.model,18,Ui.TEXT,Typeface.BOLD));c.addView(Ui.text(this,r.brand+" • "+r.category+" • "+PumpSelector.trim(r.hp)+" HP",13,Ui.MUTED,0));c.addView(Ui.text(this,timeText(time),11,Ui.MUTED,0));c.setOnClickListener(v->{Intent i=new Intent(this,PumpDetailsActivity.class);i.putExtra("asset",asset);i.putExtra("brand",r.brand);i.putExtra("id",r.id);startActivity(i);});return c;}
    View searchCard(LocalStore.SearchRef s){LinearLayout c=Ui.card(this);c.setPadding(Ui.dp(this,14),Ui.dp(this,11),Ui.dp(this,14),Ui.dp(this,11));c.addView(Ui.text(this,s.brand+" • "+categoryLabel(s.cat),17,Ui.BLUE,Typeface.BOLD));String flow=s.range?PumpSelector.formatFlow(PumpSelector.toLPH(s.flow1,s.unit),s.unit)+" – "+PumpSelector.formatFlow(PumpSelector.toLPH(s.flow2,s.unit),s.unit):PumpSelector.formatFlow(PumpSelector.toLPH(s.flow1,s.unit),s.unit);c.addView(Ui.text(this,PumpSelector.head(s.head)+" • "+flow+" • "+phaseLabel(s.phase),13,Ui.MUTED,0));c.setOnClickListener(v->{Intent i=new Intent(this,MainActivity.class);i.putExtra("asset",s.asset);i.putExtra("brand",s.brand);i.putExtra("initialHead",s.head);i.putExtra("initialFlow1",s.flow1);i.putExtra("initialFlow2",s.flow2);i.putExtra("initialRange",s.range);i.putExtra("initialUnit",s.unit);i.putExtra("initialCat",s.cat);i.putExtra("initialPhase",s.phase);startActivity(i);});return c;}
    String timeText(long t){if(t<=0)return"";return new SimpleDateFormat("dd MMM, h:mm a",Locale.getDefault()).format(new Date(t));}
    String categoryLabel(String c){if("borewell_all".equals(c))return"Borewell";if("openwell_all".equals(c))return"Openwell";if("monoblock_all".equals(c))return"Surface / Monoblock";if("multistage_all".equals(c))return"Multistage";if("booster_all".equals(c))return"Booster";if("dewatering_all".equals(c))return"Dewatering / Sewage";if("motors_all".equals(c))return"Motors";if("all".equals(c))return"All pump types";return c;}
    String phaseLabel(String p){if("S".equals(p))return"Single phase";if("T".equals(p))return"Three phase";return"Any phase";}
}
