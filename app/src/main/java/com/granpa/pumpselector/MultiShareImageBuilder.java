package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.*;
import java.util.*;

public class MultiShareImageBuilder {
    static final int W=1080;

    public static void shareComparison(Activity a,double head,double targetLPH,String unit,List<PumpSelector.Result> best){
        Bitmap b=buildComparison(a,head,targetLPH,unit,best);shareBitmap(a,b,"granpa-comparison.png","Pump comparison");
    }

    public static void shareShortlist(Activity a,List<PumpRecord> rows){
        Bitmap b=buildShortlist(a,rows);shareBitmap(a,b,"granpa-shortlist.png","Pump shortlist");
    }

    static Bitmap buildComparison(Activity a,double head,double targetLPH,String unit,List<PumpSelector.Result> best){
        int count=Math.max(1,best==null?0:best.size());int h=420+count*270;
        Bitmap bm=Bitmap.createBitmap(W,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.rgb(247,249,252));
        Paint p=new Paint(1);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(Ui.TEXT);p.setTextSize(54);c.drawText("Granpa Pump Comparison",60,86,p);
        p.setTypeface(Typeface.DEFAULT);p.setTextSize(30);p.setColor(Ui.MUTED);c.drawText(PumpSelector.head(head)+" • Required "+PumpSelector.formatFlow(targetLPH,unit),60,135,p);
        p.setColor(Ui.BLUE);p.setStrokeWidth(3);c.drawLine(60,165,W-60,165,p);
        int y=210;
        if(best!=null)for(PumpSelector.Result x:best){
            PumpRecord r=x.r;if(r==null)continue;
            card(c,45,y,W-45,y+230);
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(34);p.setColor(Ui.BLUE);c.drawText(safe(r.brand),70,y+48,p);
            p.setTextSize(36);p.setColor(Ui.TEXT);drawFit(c,safe(r.model),70,y+95,p,W-150);
            p.setTypeface(Typeface.DEFAULT);p.setTextSize(27);p.setColor(Ui.MUTED);
            c.drawText(PumpSelector.trim(r.hp)+" HP • "+PumpSelector.trim(r.kw)+" kW • "+phase(r.phase),70,y+138,p);
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(31);p.setColor(matchColor(x.status));
            c.drawText(PumpSelector.formatFlow(x.flow,unit)+" at "+PumpSelector.head(x.head),70,y+182,p);
            p.setTypeface(Typeface.DEFAULT);p.setTextSize(25);p.setColor(Ui.MUTED);
            String foot=(empty(r.displaySize())?"":r.displaySize()+" • ")+"Page "+r.page;
            c.drawText(foot,70,y+215,p);
            y+=255;
        }
        p.setTextSize(22);p.setColor(Ui.MUTED);p.setTypeface(Typeface.DEFAULT);
        c.drawText("Based on manufacturer catalogue data and interpolation between published points.",60,h-45,p);
        return bm;
    }

    static Bitmap buildShortlist(Activity a,List<PumpRecord> rows){
        int count=Math.max(1,rows==null?0:rows.size());int h=300+count*220;
        Bitmap bm=Bitmap.createBitmap(W,h,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(bm);c.drawColor(Color.rgb(247,249,252));
        Paint p=new Paint(1);p.setTypeface(Typeface.DEFAULT_BOLD);p.setColor(Ui.TEXT);p.setTextSize(54);c.drawText("Granpa Pump Shortlist",60,88,p);
        p.setTypeface(Typeface.DEFAULT);p.setTextSize(28);p.setColor(Ui.MUTED);c.drawText(count+" selected model"+(count==1?"":"s"),60,135,p);
        p.setColor(Ui.BLUE);p.setStrokeWidth(3);c.drawLine(60,165,W-60,165,p);
        int y=200,n=1;
        if(rows!=null)for(PumpRecord r:rows){
            card(c,45,y,W-45,y+185);
            p.setTypeface(Typeface.DEFAULT_BOLD);p.setTextSize(28);p.setColor(Ui.BLUE);c.drawText(n+++". "+safe(r.brand),70,y+42,p);
            p.setTextSize(34);p.setColor(Ui.TEXT);drawFit(c,safe(r.model),70,y+88,p,W-150);
            p.setTypeface(Typeface.DEFAULT);p.setTextSize(27);p.setColor(Ui.MUTED);
            c.drawText(PumpSelector.trim(r.hp)+" HP • "+PumpSelector.trim(r.kw)+" kW • "+phase(r.phase),70,y+130,p);
            String foot=(empty(r.displaySize())?"":r.displaySize()+" • ")+safe(r.category)+" • Page "+r.page;
            p.setTextSize(23);drawFit(c,foot,70,y+165,p,W-150);
            y+=205;
        }
        p.setTextSize(22);p.setColor(Ui.MUTED);c.drawText("Final selection should consider pipe losses and installation conditions.",60,h-35,p);
        return bm;
    }

    static void card(Canvas c,float l,float t,float r,float b){Paint fill=new Paint(1);fill.setColor(Color.WHITE);c.drawRoundRect(new RectF(l,t,r,b),26,26,fill);Paint stroke=new Paint(1);stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(2);stroke.setColor(Ui.BORDER);c.drawRoundRect(new RectF(l,t,r,b),26,26,stroke);}

    static void drawFit(Canvas c,String s,float x,float y,Paint p,float max){String v=s;while(p.measureText(v)>max&&v.length()>5)v=v.substring(0,v.length()-2)+"…";c.drawText(v,x,y,p);}

    static void shareBitmap(Activity a,Bitmap b,String name,String title){
        try{
            File dir=new File(a.getCacheDir(),"images");if(!dir.exists())dir.mkdirs();File f=new File(dir,name);
            try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}
            Uri uri=FileProvider.getUriForFile(a,a.getPackageName()+".fileprovider",f);
            Intent i=new Intent(Intent.ACTION_SEND);i.setType("image/png");i.putExtra(Intent.EXTRA_STREAM,uri);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            a.startActivity(Intent.createChooser(i,title));
        }catch(Exception e){android.widget.Toast.makeText(a,"Unable to share image",android.widget.Toast.LENGTH_LONG).show();}
    }

    static int matchColor(String s){s=s==null?"":s;if(s.contains("Wide")||s.contains("Last"))return Ui.ORANGE;if(s.contains("Extended"))return Ui.BLUE;return Ui.GREEN;}
    static String phase(String p){p=safe(p).toUpperCase(Locale.US);boolean s=p.contains("S");boolean t=p.contains("T")||p.contains("3");if(s&&t)return "Single / Three Phase";if(s)return "Single Phase";if(t)return "Three Phase";return "Phase -";}
    static String safe(String s){return s==null?"":s.trim();}static boolean empty(String s){return safe(s).isEmpty();}
}
