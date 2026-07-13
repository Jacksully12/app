package com.granpa.pumpselector;

import android.app.*;
import android.graphics.Typeface;
import android.os.*;
import android.widget.*;
import java.util.*;

public class QAActivity extends Activity {
    @Override protected void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.root(this);LinearLayout h=Ui.card(this);h.addView(Ui.text(this,"Catalogue data QA",25,Ui.TEXT,Typeface.BOLD));h.addView(Ui.text(this,"Live checks from the three packaged catalogues.",13,Ui.MUTED,0));root.addView(h);add(root,"TEXMO",PumpRepository.TEXMO_ASSET);add(root,"LUBI",PumpRepository.LUBI_ASSET);add(root,"KSB",PumpRepository.KSB_ASSET);LinearLayout n=Ui.card(this);n.addView(Ui.text(this,"Selection protection",18,Ui.TEXT,Typeface.BOLD));n.addView(Ui.text(this,"Only selectable records with usable performance curves enter automatic pump recommendations and brand comparison. Motor records use a separate specification workflow.",13,Ui.MUTED,0));root.addView(n);setContentView(Ui.scroll(this,root));}
    void add(LinearLayout root,String brand,String asset){ArrayList<PumpRecord> rows=PumpRepository.getRecords(this,asset);int checked=0,fixed=0,source=0,review=0,motors=0;HashSet<String> cats=new HashSet<>();for(PumpRecord p:rows){cats.add(p.category);if(p.isMotor())motors++;if(!p.selectable||"NEEDS_REVIEW".equals(p.dataStatus))review++;else if("SOURCE_CONFIRMED".equals(p.dataStatus))source++;else if("AUTO_FIXED".equals(p.dataStatus))fixed++;else checked++;}LinearLayout c=Ui.card(this);LinearLayout t=Ui.row(this);t.addView(Ui.text(this,brand,21,Ui.BLUE,Typeface.BOLD),new LinearLayout.LayoutParams(0,-2,1));t.addView(Ui.badge(this,review==0?"Ready":review+" review",review==0?Ui.GREEN:Ui.ORANGE));c.addView(t);c.addView(Ui.text(this,rows.size()+" records • "+cats.size()+" detailed categories"+(motors>0?" • "+motors+" motors":""),14,Ui.MUTED,0));c.addView(Ui.text(this,"QA checked: "+checked+"   Auto-fixed: "+fixed+"   Source-confirmed: "+source+"   Needs review: "+review,13,review==0?Ui.GREEN:Ui.ORANGE,Typeface.BOLD));root.addView(c);}
}
