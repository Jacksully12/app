package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class ShortlistActivity extends Activity {
    PumpListAdapter adapter;ListView list;TextView count,empty;ArrayList<PumpRecord> models=new ArrayList<>();ArrayList<PumpSelector.Result> rows=new ArrayList<>();
    protected void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.root(this);LinearLayout head=Ui.card(this);head.addView(Ui.text(this,"Customer shortlist",24,Ui.TEXT,1));head.addView(Ui.text(this,"Save models from any brand and share one customer-friendly image.",13,Ui.MUTED,0));count=Ui.text(this,"",18,Ui.GREEN,1);head.addView(count);root.addView(head);
        LinearLayout actions=Ui.row(this);Button share=Ui.primary(this,"Share image");actions.addView(share,new LinearLayout.LayoutParams(0,-2,1));Button clear=Ui.secondary(this,"Clear");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.setMargins(Ui.dp(this,8),0,0,0);actions.addView(clear,cp);Ui.mb(this,actions,8);root.addView(actions);
        empty=Ui.text(this,"Your shortlist is empty. Open a model and tap Add to shortlist.",14,Ui.MUTED,0);root.addView(empty);adapter=new PumpListAdapter(this);list=new ListView(this);list.setDivider(null);list.setCacheColorHint(0);list.setAdapter(adapter);root.addView(list,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);
        list.setOnItemClickListener((p,v,pos,id)->open(adapter.getResult(pos)));list.setOnItemLongClickListener((p,v,pos,id)->{PumpSelector.Result r=adapter.getResult(pos);if(r!=null&&r.r!=null){LocalStore.removeShortlist(this,assetForBrand(r.r.brand),r.r.id);load();Toast.makeText(this,"Removed from shortlist",Toast.LENGTH_SHORT).show();}return true;});
        share.setOnClickListener(v->{if(models.isEmpty())Toast.makeText(this,"Add models first",Toast.LENGTH_SHORT).show();else MultiShareImageBuilder.shareShortlist(this,models);});clear.setOnClickListener(v->{LocalStore.clearShortlist(this);load();});load();}
    protected void onResume(){super.onResume();load();}
    void load(){models.clear();rows.clear();for(LocalStore.SavedRef ref:LocalStore.shortlist(this)){PumpRecord r=PumpRepository.findById(this,ref.asset,ref.id);if(r==null)continue;models.add(r);PumpSelector.Result x=new PumpSelector.Result();x.r=r;x.estimate=false;rows.add(x);}adapter.setItems(rows);count.setText(models.size()+" saved model"+(models.size()==1?"":"s"));empty.setVisibility(models.isEmpty()?View.VISIBLE:View.GONE);}
    void open(PumpSelector.Result x){if(x==null||x.r==null)return;Intent i=new Intent(this,PumpDetailsActivity.class);i.putExtra("asset",assetForBrand(x.r.brand));i.putExtra("brand",x.r.brand);i.putExtra("id",x.r.id);startActivity(i);}
    String assetForBrand(String b){b=b==null?"":b.toUpperCase(Locale.US);if(b.equals("LUBI"))return PumpRepository.LUBI_ASSET;if(b.equals("KSB"))return PumpRepository.KSB_ASSET;return PumpRepository.TEXMO_ASSET;}
}
