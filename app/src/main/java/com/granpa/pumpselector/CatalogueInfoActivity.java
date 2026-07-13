package com.granpa.pumpselector;

import android.app.*;
import android.os.*;
import android.graphics.Typeface;
import android.widget.*;
import org.json.*;
import java.util.*;

public class CatalogueInfoActivity extends Activity {
    protected void onCreate(Bundle b){super.onCreate(b);LinearLayout root=Ui.root(this);LinearLayout head=Ui.card(this);head.addView(Ui.text(this,"Catalogue information",24,Ui.TEXT,Typeface.BOLD));head.addView(Ui.text(this,"Offline data sources, verification status and app version.",13,Ui.MUTED,0));root.addView(head);root.addView(brandCard("TEXMO",PumpRepository.TEXMO_ASSET));root.addView(brandCard("LUBI",PumpRepository.LUBI_ASSET));root.addView(brandCard("KSB",PumpRepository.KSB_ASSET));
        LinearLayout app=Ui.card(this);app.addView(Ui.text(this,"App",18,Ui.TEXT,Typeface.BOLD));app.addView(Ui.text(this,"Version 5.4.0\nDatabase version 5.4.0\nWorks fully offline after installation",14,Ui.MUTED,0));root.addView(app);
        LinearLayout status=Ui.card(this);status.addView(Ui.text(this,"Data status",18,Ui.TEXT,Typeface.BOLD));status.addView(Ui.text(this,"Source confirmed — checked directly against the manufacturer table.\n\nQA checked — passed automated power, curve, category and unit checks.\n\nNeeds source review — visible in catalogue browsing but excluded from automatic recommendations.",13,Ui.MUTED,0));root.addView(status);
        LinearLayout note=Ui.card(this);note.addView(Ui.text(this,"Selection note",18,Ui.TEXT,Typeface.BOLD));note.addView(Ui.text(this,"Recommendations use published manufacturer performance data and interpolation between catalogue points. Final selection should also consider pipe losses, voltage, installation conditions, water quality and manufacturer guidance.",13,Ui.MUTED,0));root.addView(note);setContentView(Ui.scroll(this,root));}
    LinearLayout brandCard(String brand,String asset){ArrayList<PumpRecord> rows=PumpRepository.getRecords(this,asset);JSONObject m=PumpRepository.metadata(this,asset);int selectable=0,source=0,review=0;HashSet<String> cats=new HashSet<>();for(PumpRecord r:rows){cats.add(r.category);if(r.selectable)selectable++;if("SOURCE_CONFIRMED".equals(r.dataStatus))source++;if(!r.selectable||"NEEDS_REVIEW".equals(r.dataStatus))review++;}
        LinearLayout c=Ui.card(this);c.addView(Ui.text(this,brand,20,Ui.BLUE,Typeface.BOLD));String src=m.optString("sourceTitle",m.optString("source","Manufacturer catalogue"));c.addView(Ui.text(this,"Source: "+src+"\nRecords: "+rows.size()+"\nSelectable: "+selectable+"\nSource-confirmed repairs/structured rows: "+source+"\nNeeds review: "+review+"\nDetailed categories: "+cats.size(),13,Ui.MUTED,0));return c;}
}
