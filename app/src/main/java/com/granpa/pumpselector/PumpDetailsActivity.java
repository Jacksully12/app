package com.granpa.pumpselector;

import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.util.*;

public class PumpDetailsActivity extends Activity {
    PumpRecord rec;boolean has;double head,flow;String unit="LPH",asset=PumpRepository.TEXMO_ASSET,brand="TEXMO";
    LinearLayout performanceHolder;Button shortlistButton;

    protected void onCreate(Bundle b){
        super.onCreate(b);asset=PumpRepository.normalizeAsset(getIntent().getStringExtra("asset"));brand=getIntent().getStringExtra("brand");if(empty(brand))brand=PumpRepository.brandName(asset);
        rec=PumpRepository.findById(this,asset,getIntent().getStringExtra("id"));has=getIntent().getBooleanExtra("estimate",false);head=getIntent().getDoubleExtra("head",Double.NaN);flow=getIntent().getDoubleExtra("flow",Double.NaN);unit=PumpSelector.normalizeUnit(getIntent().getStringExtra("unit"));
        LinearLayout root=Ui.root(this);if(rec==null){root.addView(Ui.text(this,"Model not found",22,Ui.TEXT,Typeface.BOLD));setContentView(root);return;}
        LocalStore.addRecent(this,asset,rec.id,brand);root.addView(header());root.addView(detailCard("Overview",overviewRows()));
        if(rec.isMotor())root.addView(detailCard("Motor specifications",motorRows()));else{root.addView(performanceCard());root.addView(detailCard("Technical specifications",technicalRows()));}
        root.addView(catalogueCard());root.addView(actions());setContentView(Ui.scroll(this,root));
    }

    LinearLayout header(){
        LinearLayout top=Ui.card(this);LinearLayout row=Ui.row(this);ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.app_logo);row.addView(logo,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,42)));
        LinearLayout labels=new LinearLayout(this);labels.setOrientation(LinearLayout.VERTICAL);labels.setPadding(Ui.dp(this,10),0,0,0);labels.addView(Ui.text(this,rec.model,25,Ui.TEXT,Typeface.BOLD));labels.addView(Ui.text(this,rec.brand+" • "+rec.category,14,Ui.BLUE,Typeface.BOLD));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));top.addView(row);
        if(has&&!Double.isNaN(head)&&!Double.isNaN(flow))top.addView(Ui.text(this,PumpSelector.formatFlow(flow,unit)+" at "+PumpSelector.head(head),18,Ui.GREEN,Typeface.BOLD));
        LinearLayout badges=Ui.row(this);TextView status=Ui.badge(this,statusLabel(rec),statusColor(rec));badges.addView(status);if(LocalStore.isShortlisted(this,asset,rec.id)){TextView saved=Ui.badge(this,"★ Shortlisted",Ui.ORANGE);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-2,-2);sp.setMargins(Ui.dp(this,8),0,0,0);badges.addView(saved,sp);}top.addView(badges);return top;
    }

    LinearLayout performanceCard(){
        LinearLayout c=Ui.card(this);c.addView(Ui.text(this,"Performance",19,Ui.TEXT,Typeface.BOLD));
        LinearLayout tabs=Ui.row(this);Button curve=Ui.blue(this,"Curve");Button table=Ui.secondary(this,"Performance table");tabs.addView(curve,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);tp.setMargins(Ui.dp(this,8),0,0,0);tabs.addView(table,tp);Ui.mb(this,tabs,8);c.addView(tabs);
        performanceHolder=new LinearLayout(this);performanceHolder.setOrientation(LinearLayout.VERTICAL);c.addView(performanceHolder);showCurve();
        curve.setOnClickListener(v->{curve.setBackground(Ui.solid(this,Ui.BLUE,15));curve.setTextColor(android.graphics.Color.WHITE);table.setBackground(Ui.bg(this,Ui.SOFT_BLUE,android.graphics.Color.rgb(207,221,236),15));table.setTextColor(Ui.BLUE);showCurve();});
        table.setOnClickListener(v->{table.setBackground(Ui.solid(this,Ui.BLUE,15));table.setTextColor(android.graphics.Color.WHITE);curve.setBackground(Ui.bg(this,Ui.SOFT_BLUE,android.graphics.Color.rgb(207,221,236),15));curve.setTextColor(Ui.BLUE);showTable();});
        return c;
    }

    void showCurve(){
        if(performanceHolder==null)return;performanceHolder.removeAllViews();TextView legend=Ui.text(this,"○ Catalogue points   — Interpolated curve   ● Selected duty point",12,Ui.MUTED,0);Ui.mb(this,legend,5);performanceHolder.addView(legend);
        PerformanceCurveView chart=new PerformanceCurveView(this);chart.setDisplayUnit(unit);chart.setData(rec.curve,has?head:null,has?flow:null);chart.setPointListener((h,f,catalogue)->Toast.makeText(this,(catalogue?"Catalogue point: ":"Selected point: ")+PumpSelector.formatFlow(f,unit)+" at "+String.format(Locale.US,"%.1f m",h),Toast.LENGTH_SHORT).show());performanceHolder.addView(chart,new LinearLayout.LayoutParams(-1,Ui.dp(this,330)));
        LinearLayout controls=Ui.row(this);Button zoom=Ui.secondary(this,"Full-screen chart");controls.addView(zoom,new LinearLayout.LayoutParams(0,-2,1));Button share=Ui.secondary(this,"Share image");LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-2,1);sp.setMargins(Ui.dp(this,8),0,0,0);controls.addView(share,sp);performanceHolder.addView(controls);zoom.setOnClickListener(v->openZoomScreen());share.setOnClickListener(v->ShareImageBuilder.share(this,rec,has,head,flow,false,unit));
        if(has&&!Double.isNaN(head)&&!Double.isNaN(flow)){TextView selected=Ui.text(this,"Selected point: "+PumpSelector.formatFlow(flow,unit)+" at "+PumpSelector.head(head),14,Ui.GREEN,Typeface.BOLD);selected.setPadding(0,Ui.dp(this,9),0,0);performanceHolder.addView(selected);}
    }

    void showTable(){
        performanceHolder.removeAllViews();LinearLayout table=new LinearLayout(this);table.setOrientation(LinearLayout.VERTICAL);double[][] pts=rec.curve==null?new double[0][0]:rec.curve;ArrayList<double[]> clean=new ArrayList<>();for(double[]p:pts)if(p!=null&&p.length>=2)clean.add(p);clean.sort(Comparator.comparingDouble(p->p[0]));
        LinearLayout header=tableRow("Head (m)","Flow ("+PumpSelector.unitLabel(unit)+")",true);table.addView(header);for(double[]p:clean)table.addView(tableRow(PumpSelector.trim(p[0]),PumpSelector.formatFlowNumber(PumpSelector.fromLPH(p[1],unit),unit),false));if(has)table.addView(tableRow(PumpSelector.trim(head),PumpSelector.formatFlowNumber(PumpSelector.fromLPH(flow,unit),unit)+" • selected",true));performanceHolder.addView(table);
    }

    LinearLayout tableRow(String a,String b,boolean bold){LinearLayout r=Ui.row(this);r.setPadding(Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8));r.setBackground(Ui.bg(this,bold?Ui.SOFT_BLUE:android.graphics.Color.WHITE,Ui.BORDER,8));TextView l=Ui.text(this,a,13,Ui.TEXT,bold?Typeface.BOLD:0);TextView v=Ui.text(this,b,13,bold?Ui.BLUE:Ui.MUTED,bold?Typeface.BOLD:0);r.addView(l,new LinearLayout.LayoutParams(0,-2,1));r.addView(v,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,Ui.dp(this,4));r.setLayoutParams(lp);return r;}

    void openZoomScreen(){Intent i=new Intent(this,ChartZoomActivity.class);i.putExtra("id",rec.id);i.putExtra("estimate",has);i.putExtra("head",head);i.putExtra("flow",flow);i.putExtra("unit",unit);i.putExtra("asset",asset);i.putExtra("brand",brand);startActivity(i);}

    String[][] overviewRows(){ArrayList<String[]>r=new ArrayList<>();r.add(new String[]{"Power",PumpSelector.trim(rec.hp)+" HP / "+PumpSelector.trim(rec.kw)+" kW"});r.add(new String[]{"Phase",phaseLabel(rec.phase)});if(!empty(rec.stages))r.add(new String[]{"Stages",rec.stages});if(!empty(rec.displaySize()))r.add(new String[]{"Primary size",rec.displaySize()});r.add(new String[]{"Catalogue page",String.valueOf(rec.page)});r.add(new String[]{"Data status",statusLabel(rec)});return r.toArray(new String[0][]);}
    String[][] motorRows(){ArrayList<String[]>r=new ArrayList<>();add(r,"Speed",empty(rec.rpm)?rec.nominalSpeed:rec.rpm+" RPM");add(r,"Insulation class",rec.insulationClass);add(r,"Frame size",rec.frameSize);add(r,"Motor type",empty(rec.motorType)?rec.category:rec.motorType);return r.toArray(new String[0][]);}
    String[][] technicalRows(){ArrayList<String[]>r=new ArrayList<>();add(r,"Pipe / outlet size",rec.pipeSize);add(r,"Delivery size",rec.deliverySize);add(r,"Suction size",rec.suctionSize);add(r,"NRV size",rec.nrvSize);add(r,"Cable size",rec.cableSize);add(r,"Rated current",rec.ratedCurrent);add(r,"Free passage",rec.freePassage);add(r,"Maximum solid size",rec.maxSolidSize);add(r,"Impeller diameter",rec.impellerDiameter);add(r,"Motor diameter",rec.motorDiameter);add(r,"Nominal speed",empty(rec.nominalSpeed)?(!empty(rec.rpm)?rec.rpm+" RPM":""):rec.nominalSpeed);add(r,"Starting method",rec.startingMethod);add(r,"Head range",safe(rec.headRangeText)+(empty(rec.headRangeText)?"":" m"));add(r,"Flow range",flowRange(rec));add(r,"Technical note",rec.technicalNote);if(r.isEmpty())add(r,"Catalogue size",rec.size);return r.toArray(new String[0][]);}
    void add(ArrayList<String[]> rows,String label,String value){if(!empty(value))rows.add(new String[]{label,value});}

    LinearLayout detailCard(String title,String[][] rows){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,title,18,Ui.TEXT,Typeface.BOLD));for(String[]row:rows){LinearLayout x=Ui.row(this);x.setPadding(0,Ui.dp(this,7),0,Ui.dp(this,7));TextView l=Ui.text(this,row[0],13,Ui.MUTED,0);TextView v=Ui.text(this,row[1],14,Ui.TEXT,Typeface.BOLD);v.setGravity(Gravity.END);x.addView(l,new LinearLayout.LayoutParams(0,-2,1));x.addView(v,new LinearLayout.LayoutParams(0,-2,1));c.addView(x);}return c;}
    LinearLayout catalogueCard(){LinearLayout c=Ui.card(this);c.addView(Ui.text(this,"Catalogue information",18,Ui.TEXT,Typeface.BOLD));c.addView(Ui.text(this,catalogueText(),13,Ui.MUTED,0));return c;}

    LinearLayout actions(){
        LinearLayout outer=new LinearLayout(this);outer.setOrientation(LinearLayout.VERTICAL);LinearLayout row=Ui.row(this);Button share=Ui.primary(this,"Share image");row.addView(share,new LinearLayout.LayoutParams(0,-2,1));Button save=Ui.blue(this,"Save image");LinearLayout.LayoutParams sv=new LinearLayout.LayoutParams(0,-2,1);sv.setMargins(Ui.dp(this,8),0,0,0);row.addView(save,sv);Ui.mb(this,row,8);outer.addView(row);
        LinearLayout row2=Ui.row(this);shortlistButton=Ui.secondary(this,LocalStore.isShortlisted(this,asset,rec.id)?"Remove shortlist":"Add to shortlist");row2.addView(shortlistButton,new LinearLayout.LayoutParams(0,-2,1));Button copy=Ui.secondary(this,"Copy model");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(0,-2,1);cp.setMargins(Ui.dp(this,8),0,0,0);row2.addView(copy,cp);outer.addView(row2);
        share.setOnClickListener(v->ShareImageBuilder.share(this,rec,has,head,flow,false,unit));save.setOnClickListener(v->ShareImageBuilder.download(this,rec,has,head,flow,unit));shortlistButton.setOnClickListener(v->{boolean saved=LocalStore.toggleShortlist(this,asset,rec.id,brand);shortlistButton.setText(saved?"Remove shortlist":"Add to shortlist");Toast.makeText(this,saved?"Added to shortlist":"Removed from shortlist",Toast.LENGTH_SHORT).show();});copy.setOnClickListener(v->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("model",rec.model));Toast.makeText(this,"Model copied",Toast.LENGTH_SHORT).show();});return outer;
    }

    String catalogueText(){StringBuilder s=new StringBuilder();s.append(empty(rec.catalogueSectionText)?rec.title:rec.catalogueSectionText);if(!empty(rec.variantLabel))s.append("\nVariant: ").append(rec.variantLabel);if(!empty(rec.dataNote))s.append("\n").append(rec.dataNote);return s.toString();}
    String flowRange(PumpRecord r){if(!Double.isNaN(r.minFlowLPH)&&!Double.isNaN(r.maxFlowLPH))return PumpSelector.formatFlowNumber(PumpSelector.fromLPH(r.minFlowLPH,unit),unit)+" – "+PumpSelector.formatFlow(r.maxFlowLPH,unit);return safe(r.dischargeRangeText)+" "+safe(r.flowUnitOriginal);}
    String statusLabel(PumpRecord r){if(!r.selectable||"NEEDS_REVIEW".equals(r.dataStatus))return"Needs source review";if("SOURCE_CONFIRMED".equals(r.dataStatus))return"Source confirmed";if("AUTO_FIXED".equals(r.dataStatus))return"Automatically corrected and checked";return"QA checked";}
    int statusColor(PumpRecord r){return(!r.selectable||"NEEDS_REVIEW".equals(r.dataStatus))?Ui.ORANGE:Ui.GREEN;}
    String phaseLabel(String p){p=safe(p).toUpperCase(Locale.US);boolean s=p.contains("S"),t=p.contains("T")||p.contains("3");if(s&&t)return"Single / Three phase";if(s)return"Single phase";if(t)return"Three phase";return"-";}
    String safe(String s){return s==null?"":s.trim();}boolean empty(String s){return safe(s).isEmpty();}
}
