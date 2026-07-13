package com.granpa.pumpselector;

import android.app.*;
import android.os.*;
import android.graphics.Typeface;
import android.widget.*;

public class ChartZoomActivity extends Activity {
    PumpRecord rec;boolean has;double head,flow;String unit="LPH",asset=PumpRepository.TEXMO_ASSET,brand="TEXMO";PerformanceCurveView chart;TextView zoomInfo,pointInfo;
    @Override protected void onCreate(Bundle b){super.onCreate(b);asset=PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));brand=getIntent().getStringExtra("brand");if(brand==null||brand.trim().isEmpty())brand=PumpRepository.brandName(asset);rec=PumpRepository.findById(this,asset,getIntent().getStringExtra("id"));has=getIntent().getBooleanExtra("estimate",false);head=getIntent().getDoubleExtra("head",Double.NaN);flow=getIntent().getDoubleExtra("flow",Double.NaN);unit=PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));
        LinearLayout root=Ui.root(this);root.addView(Ui.text(this,"Performance curve",24,Ui.TEXT,Typeface.BOLD));root.addView(Ui.text(this,(rec!=null?rec.model:"Pump model")+" • "+PumpSelector.unitLabel(unit),17,Ui.BLUE,Typeface.BOLD));TextView help=Ui.text(this,"Pinch or double-tap to zoom. Drag after zooming and tap any catalogue point for exact values.",13,Ui.MUTED,0);Ui.mb(this,help,10);root.addView(help);
        LinearLayout toolbar=Ui.row(this);Button minus=Ui.secondary(this,"− Zoom");toolbar.addView(minus,new LinearLayout.LayoutParams(0,-2,1));Button reset=Ui.blue(this,"Reset");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(0,-2,1);rp.setMargins(Ui.dp(this,8),0,Ui.dp(this,8),0);toolbar.addView(reset,rp);Button plus=Ui.primary(this,"+ Zoom");toolbar.addView(plus,new LinearLayout.LayoutParams(0,-2,1));Ui.mb(this,toolbar,8);root.addView(toolbar);
        zoomInfo=Ui.text(this,"Zoom: 100%",13,Ui.MUTED,Typeface.BOLD);root.addView(zoomInfo);pointInfo=Ui.text(this,"Tap a catalogue point to inspect it.",14,Ui.BLUE,Typeface.BOLD);Ui.mb(this,pointInfo,8);root.addView(pointInfo);
        LinearLayout card=Ui.card(this);chart=new PerformanceCurveView(this);chart.setDisplayUnit(unit);if(rec!=null)chart.setData(rec.curve,has?head:null,has?flow:null);chart.setPinchZoomEnabled(true);chart.setZoomListener(percent->updateZoom());chart.setPointListener((h,f,catalogue)->pointInfo.setText((catalogue?"Catalogue point: ":"Selected duty point: ")+PumpSelector.head(h)+" • "+PumpSelector.formatFlow(f,unit)));card.addView(chart,new LinearLayout.LayoutParams(-1,Ui.dp(this,540)));root.addView(card);
        LinearLayout actions=Ui.row(this);Button share=Ui.primary(this,"Share model image");actions.addView(share,new LinearLayout.LayoutParams(0,-2,1));Button close=Ui.secondary(this,"Back");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.setMargins(Ui.dp(this,8),0,0,0);actions.addView(close,cp);root.addView(actions);
        minus.setOnClickListener(v->{chart.zoomOut();updateZoom();});plus.setOnClickListener(v->{chart.zoomIn();updateZoom();});reset.setOnClickListener(v->{chart.resetZoom();updateZoom();});close.setOnClickListener(v->finish());share.setOnClickListener(v->{if(rec==null)Toast.makeText(this,"Model unavailable",Toast.LENGTH_SHORT).show();else ShareImageBuilder.share(this,rec,has,head,flow,false,unit);});setContentView(Ui.scroll(this,root));}
    void updateZoom(){zoomInfo.setText("Zoom: "+chart.getZoomPercent()+"%"+(chart.getZoomPercent()>100?" • Drag to move":""));}
}
