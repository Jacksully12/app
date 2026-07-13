package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Typeface;
import android.widget.*;

public class BrandSelectionActivity extends Activity {
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root=Ui.root(this);

        LinearLayout header=Ui.card(this);
        LinearLayout row=Ui.row(this);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.app_logo);
        row.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,58),Ui.dp(this,58)));
        LinearLayout text=new LinearLayout(this);text.setOrientation(LinearLayout.VERTICAL);text.setPadding(Ui.dp(this,12),0,0,0);
        text.addView(Ui.text(this,"Granpa",28,Ui.TEXT,Typeface.BOLD));
        text.addView(Ui.text(this,"Pump selection, comparison and dealer shortlist",14,Ui.MUTED,0));
        row.addView(text,new LinearLayout.LayoutParams(0,-2,1));header.addView(row);root.addView(header);

        root.addView(modeCard("TEXMO","Full pump and motor catalogue",PumpRepository.TEXMO_ASSET));
        root.addView(modeCard("LUBI","Performance booklet catalogue",PumpRepository.LUBI_ASSET));
        root.addView(modeCard("KSB","Domestic pump performance catalogue",PumpRepository.KSB_ASSET));

        LinearLayout compare=Ui.card(this);
        compare.addView(Ui.text(this,"COMPARE BRANDS",20,Ui.TEXT,Typeface.BOLD));
        compare.addView(Ui.text(this,"Compare the closest Texmo, Lubi and KSB matches at one duty point.",14,Ui.MUTED,0));
        Button compareButton=Ui.primary(this,"Compare brands");compareButton.setTextSize(14);
        LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(Ui.dp(this,178),Ui.dp(this,46));clp.setMargins(0,Ui.dp(this,10),0,0);compare.addView(compareButton,clp);
        compareButton.setOnClickListener(v->startActivity(new Intent(this,CompareActivity.class)));root.addView(compare);

        LinearLayout shortcuts=Ui.card(this);
        shortcuts.addView(Ui.text(this,"Dealer tools",18,Ui.TEXT,Typeface.BOLD));
        LinearLayout r1=Ui.row(this);
        Button shortlist=Ui.secondary(this,"Shortlist");r1.addView(shortlist,new LinearLayout.LayoutParams(0,-2,1));
        Button recent=Ui.secondary(this,"Recent");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-2,1);rp.setMargins(Ui.dp(this,8),0,0,0);r1.addView(recent,rp);shortcuts.addView(r1);
        LinearLayout r2=Ui.row(this);r2.setPadding(0,Ui.dp(this,8),0,0);
        Button info=Ui.secondary(this,"Catalogue info");r2.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        Button qa=Ui.secondary(this,"Data QA");LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(0,-2,1);qp.setMargins(Ui.dp(this,8),0,0,0);r2.addView(qa,qp);shortcuts.addView(r2);root.addView(shortcuts);

        shortlist.setOnClickListener(v->startActivity(new Intent(this,ShortlistActivity.class)));
        recent.setOnClickListener(v->startActivity(new Intent(this,RecentActivity.class)));
        info.setOnClickListener(v->startActivity(new Intent(this,CatalogueInfoActivity.class)));
        qa.setOnClickListener(v->startActivity(new Intent(this,QAActivity.class)));
        setContentView(Ui.scroll(this,root));
    }

    LinearLayout modeCard(String title,String desc,String asset){
        LinearLayout c=Ui.card(this);c.setPadding(Ui.dp(this,14),Ui.dp(this,13),Ui.dp(this,14),Ui.dp(this,13));
        LinearLayout top=Ui.row(this);LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);
        labels.addView(Ui.text(this,title,21,Ui.BLUE,Typeface.BOLD));labels.addView(Ui.text(this,desc,13,Ui.MUTED,0));
        labels.addView(Ui.text(this,PumpRepository.note(this,asset),12,Ui.MUTED,0));top.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
        Button b=Ui.blue(this,"Open");b.setTextSize(13);top.addView(b,new LinearLayout.LayoutParams(Ui.dp(this,90),Ui.dp(this,44)));c.addView(top);
        b.setOnClickListener(v->{Intent i=new Intent(this,MainActivity.class);i.putExtra("asset",asset);i.putExtra("brand",PumpRepository.brandName(asset));startActivity(i);});
        return c;
    }
}
