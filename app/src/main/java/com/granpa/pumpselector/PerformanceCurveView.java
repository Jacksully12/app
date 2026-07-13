package com.granpa.pumpselector;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;

public class PerformanceCurveView extends View {
    Paint major=new Paint(1),minor=new Paint(1),axis=new Paint(1),text=new Paint(1),line=new Paint(1),glow=new Paint(1),dot=new Paint(1),catalogueDot=new Paint(1);
    double[][] curve;Double selectedHead,selectedFlowLPH;String unit="LPH";
    ScaleGestureDetector scale;GestureDetector gestures;float zoom=1,panX=0,panY=0,lastX,lastY;boolean zoomEnabled=false;ZoomListener zoomListener;PointListener pointListener;
    RectF lastPlot,lastData;double lastMaxFlow,lastMaxHead;ArrayList<double[]> lastPoints=new ArrayList<>();

    public interface ZoomListener{void onZoomChanged(int percent);} public interface PointListener{void onPoint(double head,double flowLPH,boolean cataloguePoint);}

    public PerformanceCurveView(Context c){
        super(c);setFocusable(true);
        major.setColor(Color.rgb(205,216,229));major.setStrokeWidth(dp(1));minor.setColor(Color.rgb(229,235,243));minor.setStrokeWidth(dp(.7f));axis.setColor(Color.rgb(70,85,102));axis.setStrokeWidth(dp(1.5f));
        text.setColor(Ui.TEXT);text.setTextSize(sp(12));line.setColor(Color.rgb(0,96,216));line.setStyle(Paint.Style.STROKE);line.setStrokeWidth(dp(3.2f));line.setStrokeCap(Paint.Cap.ROUND);line.setStrokeJoin(Paint.Join.ROUND);
        glow.setColor(Color.argb(35,0,96,216));glow.setStyle(Paint.Style.STROKE);glow.setStrokeWidth(dp(8));dot.setColor(Color.rgb(255,132,0));catalogueDot.setColor(Color.WHITE);catalogueDot.setStyle(Paint.Style.FILL);
        scale=new ScaleGestureDetector(c,new ScaleGestureDetector.SimpleOnScaleGestureListener(){public boolean onScale(ScaleGestureDetector d){if(!zoomEnabled)return false;float old=zoom;zoom=Math.max(1,Math.min(4,zoom*d.getScaleFactor()));float fx=d.getFocusX(),fy=d.getFocusY();if(zoom==1){panX=panY=0;}else if(old!=zoom){float k=zoom/old;panX=fx-(fx-panX)*k;panY=fy-(fy-panY)*k;clamp();}notifyZoom();invalidate();return true;}});
        gestures=new GestureDetector(c,new GestureDetector.SimpleOnGestureListener(){
            public boolean onDoubleTap(MotionEvent e){if(!zoomEnabled)return false;if(zoom<1.05f){zoom=2;panX=getWidth()/2f-e.getX();panY=getHeight()/2f-e.getY();clamp();}else resetZoom();notifyZoom();invalidate();return true;}
            public boolean onSingleTapConfirmed(MotionEvent e){return handlePointTap(e.getX(),e.getY());}
        });
    }

    public void setDisplayUnit(String u){unit=PumpSelector.normalizeUnit(u);invalidate();}
    public void setData(double[][]c,Double h,Double f){curve=c;selectedHead=h;selectedFlowLPH=f;invalidate();}
    public void setPinchZoomEnabled(boolean e){zoomEnabled=e;setClickable(true);}
    public void setZoomListener(ZoomListener l){zoomListener=l;}
    public void setPointListener(PointListener l){pointListener=l;}
    public void resetZoom(){zoom=1;panX=panY=0;notifyZoom();invalidate();}
    public void zoomIn(){zoom=Math.min(4,zoom*1.25f);clamp();notifyZoom();invalidate();}
    public void zoomOut(){zoom=Math.max(1,zoom/1.25f);if(zoom==1)panX=panY=0;clamp();notifyZoom();invalidate();}
    public int getZoomPercent(){return Math.round(zoom*100);}void notifyZoom(){if(zoomListener!=null)zoomListener.onZoomChanged(getZoomPercent());}

    public boolean onTouchEvent(MotionEvent e){
        gestures.onTouchEvent(e);scale.onTouchEvent(e);boolean pan=zoomEnabled&&zoom>1.02f&&!scale.isInProgress();
        if((e.getPointerCount()>1||pan)&&getParent()!=null)getParent().requestDisallowInterceptTouchEvent(true);
        if(e.getActionMasked()==MotionEvent.ACTION_DOWN){lastX=e.getX();lastY=e.getY();return true;}
        if(e.getActionMasked()==MotionEvent.ACTION_MOVE&&pan&&e.getPointerCount()==1){panX+=e.getX()-lastX;panY+=e.getY()-lastY;lastX=e.getX();lastY=e.getY();clamp();invalidate();return true;}
        if((e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL)&&getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
        return true;
    }

    boolean handlePointTap(float sx,float sy){
        if(pointListener==null||lastData==null||lastPoints.isEmpty())return false;
        float cx=lastPlot.centerX(),cy=lastPlot.centerY();float ux=cx+(sx-panX-cx)/zoom,uy=cy+(sy-panY-cy)/zoom;
        double best=Double.MAX_VALUE;double[] chosen=null;
        for(double[] p:lastPoints){float px=x(PumpSelector.fromLPH(p[1],unit),lastMaxFlow,lastData),py=y(p[0],lastMaxHead,lastData);double d=Math.hypot(px-ux,py-uy);if(d<best){best=d;chosen=p;}}
        if(chosen!=null&&best<=dp(32)){pointListener.onPoint(chosen[0],chosen[1],true);return true;}
        if(selectedHead!=null&&selectedFlowLPH!=null){float px=x(PumpSelector.fromLPH(selectedFlowLPH,unit),lastMaxFlow,lastData),py=y(selectedHead,lastMaxHead,lastData);if(Math.hypot(px-ux,py-uy)<=dp(34)){pointListener.onPoint(selectedHead,selectedFlowLPH,false);return true;}}
        return false;
    }

    void clamp(){if(zoom<=1||getWidth()==0){panX=panY=0;return;}float mx=(zoom-1)*getWidth()*.5f,my=(zoom-1)*getHeight()*.5f;panX=Math.max(-mx,Math.min(mx,panX));panY=Math.max(-my,Math.min(my,panY));}
    protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),resolveSize(dp(360),h));}

    protected void onDraw(Canvas c){
        super.onDraw(c);ArrayList<double[]>pts=valid();if(pts.size()<2)return;boolean has=selectedHead!=null&&selectedFlowLPH!=null&&!Double.isNaN(selectedHead)&&!Double.isNaN(selectedFlowLPH);
        double maxF=0,maxH=0;for(double[]p:pts){maxF=Math.max(maxF,PumpSelector.fromLPH(p[1],unit));maxH=Math.max(maxH,p[0]);}if(has){maxF=Math.max(maxF,PumpSelector.fromLPH(selectedFlowLPH,unit));maxH=Math.max(maxH,selectedHead);}
        double axisF=roundUp(maxF*1.10,niceFlowStep(maxF)),axisH=roundUp(maxH*1.12,10);RectF plot=new RectF(dp(62),dp(24),getWidth()-dp(22),getHeight()-dp(78));RectF data=new RectF(plot.left+dp(10),plot.top+dp(10),plot.right-dp(10),plot.bottom-dp(10));
        lastPlot=new RectF(plot);lastData=new RectF(data);lastMaxFlow=axisF;lastMaxHead=axisH;lastPoints=new ArrayList<>(pts);
        c.save();if(zoomEnabled&&zoom>1){c.translate(panX,panY);c.scale(zoom,zoom,plot.centerX(),plot.centerY());}drawGrid(c,plot,axisF,axisH,has?PumpSelector.fromLPH(selectedFlowLPH,unit):null);
        Path p=monotonePath(pts,axisF,axisH,data);c.drawPath(p,glow);c.drawPath(p,line);drawCatalogueDots(c,pts,data,axisF,axisH);if(has)drawSelected(c,plot,data,axisF,axisH,PumpSelector.fromLPH(selectedFlowLPH,unit),selectedHead);c.restore();
        Paint h=new Paint(text);h.setColor(Ui.MUTED);h.setTextSize(sp(10));String m=zoomEnabled?(zoom>1?"Drag to move • tap a point • double-tap reset":"Pinch, double-tap or tap a point"):"Tap a catalogue point for exact values";c.drawText(m,getWidth()-h.measureText(m)-dp(8),dp(14),h);
    }

    void drawCatalogueDots(Canvas c,List<double[]> pts,RectF data,double mf,double mh){Paint outline=new Paint(1);outline.setStyle(Paint.Style.STROKE);outline.setStrokeWidth(dp(2));outline.setColor(Color.rgb(0,96,216));for(double[]p:pts){float px=x(PumpSelector.fromLPH(p[1],unit),mf,data),py=y(p[0],mh,data);c.drawCircle(px,py,dp(5.5f),catalogueDot);c.drawCircle(px,py,dp(5.5f),outline);}}

    ArrayList<double[]> valid(){TreeMap<Double,Double>m=new TreeMap<>();if(curve!=null)for(double[]q:curve)if(q!=null&&q.length>=2&&!Double.isNaN(q[0])&&!Double.isNaN(q[1])&&q[0]>=0&&q[1]>=0)m.put(q[0],Math.max(m.containsKey(q[0])?m.get(q[0]):0,q[1]));ArrayList<double[]>a=new ArrayList<>();double prev=Double.MAX_VALUE;for(Map.Entry<Double,Double>e:m.entrySet()){double f=e.getValue();if(f<=prev*1.02+1){a.add(new double[]{e.getKey(),f});prev=f;}if(f<=0)break;}a.sort(Comparator.comparingDouble(x->x[1]));return a;}
    Path monotonePath(ArrayList<double[]>p,double mf,double mh,RectF r){int n=p.size();float[]x=new float[n],y=new float[n];for(int i=0;i<n;i++){x[i]=x(PumpSelector.fromLPH(p.get(i)[1],unit),mf,r);y[i]=y(p.get(i)[0],mh,r);}float[]d=new float[n-1],m=new float[n];for(int i=0;i<n-1;i++)d[i]=(y[i+1]-y[i])/Math.max(1f,x[i+1]-x[i]);m[0]=d[0];m[n-1]=d[n-2];for(int i=1;i<n-1;i++)m[i]=(d[i-1]+d[i])/2f;for(int i=0;i<n-1;i++){if(Math.abs(d[i])<1e-6){m[i]=m[i+1]=0;}else{float a=m[i]/d[i],b=m[i+1]/d[i],s=a*a+b*b;if(s>9){float k=3f/(float)Math.sqrt(s);m[i]=k*a*d[i];m[i+1]=k*b*d[i];}}}Path path=new Path();path.moveTo(x[0],y[0]);for(int i=0;i<n-1;i++){float h=x[i+1]-x[i];path.cubicTo(x[i]+h/3f,y[i]+m[i]*h/3f,x[i+1]-h/3f,y[i+1]-m[i+1]*h/3f,x[i+1],y[i+1]);}return path;}
    void drawGrid(Canvas c,RectF p,double mf,double mh,Double selected){Paint gt=new Paint(text);gt.setTextSize(sp(12));for(int i=1;i<20;i++){float gx=p.left+i*p.width()/20f,gy=p.bottom-i*p.height()/20f;c.drawLine(gx,p.top,gx,p.bottom,minor);c.drawLine(p.left,gy,p.right,gy,minor);}for(int i=0;i<=5;i++){float gx=p.left+i*p.width()/5f,gy=p.bottom-i*p.height()/5f;c.drawLine(gx,p.top,gx,p.bottom,major);c.drawLine(p.left,gy,p.right,gy,major);String fl=PumpSelector.formatFlowNumber(mf*i/5,unit);if(selected==null||Math.abs(mf*i/5-selected)>mf/18)c.drawText(fl,gx-gt.measureText(fl)/2,p.bottom+dp(27),gt);String hl=String.format(Locale.US,"%.0f",mh*i/5);c.drawText(hl,p.left-gt.measureText(hl)-dp(8),gy+dp(4),gt);}c.drawLine(p.left,p.top,p.left,p.bottom,axis);c.drawLine(p.left,p.bottom,p.right,p.bottom,axis);Paint lab=new Paint(text);lab.setTextSize(sp(13.5f));lab.setTypeface(Typeface.DEFAULT_BOLD);String xl="Flow ("+PumpSelector.unitLabel(unit)+")";c.drawText(xl,p.centerX()-lab.measureText(xl)/2,getHeight()-dp(5),lab);c.save();c.rotate(-90,dp(18),p.centerY());c.drawText("Head (m)",dp(18),p.centerY(),lab);c.restore();}
    void drawSelected(Canvas c,RectF plot,RectF data,double mf,double mh,double f,double h){float sx=x(f,mf,data),sy=y(h,mh,data);Paint dash=new Paint(1);dash.setColor(Color.rgb(255,132,0));dash.setStrokeWidth(dp(1.2f));dash.setPathEffect(new DashPathEffect(new float[]{8,7},0));c.drawLine(plot.left,sy,sx,sy,dash);c.drawLine(sx,sy,sx,plot.bottom,dash);Paint halo=new Paint(1);halo.setColor(Color.argb(42,255,132,0));c.drawCircle(sx,sy,dp(16),halo);Paint white=new Paint(1);white.setColor(Color.WHITE);c.drawCircle(sx,sy,dp(11),white);c.drawCircle(sx,sy,dp(8),dot);badge(c,String.format(Locale.US,"%.1f m",h),plot.left,sy,true);badge(c,PumpSelector.formatFlowNumber(f,unit),sx,plot.bottom+dp(18),false);}
    void badge(Canvas c,String s,float x,float y,boolean left){Paint bg=new Paint(1);bg.setColor(Color.rgb(255,132,0));Paint t=new Paint(1);t.setColor(Color.WHITE);t.setTypeface(Typeface.DEFAULT_BOLD);t.setTextSize(sp(10.5f));float w=t.measureText(s)+dp(14),hh=dp(23);RectF r=left?new RectF(Math.max(dp(2),x-w-dp(5)),y-hh/2,x-dp(5),y+hh/2):new RectF(x-w/2,y-hh/2,x+w/2,y+hh/2);if(!left){if(r.left<dp(58))r.offset(dp(58)-r.left,0);if(r.right>getWidth()-dp(4))r.offset(getWidth()-dp(4)-r.right,0);}c.drawRoundRect(r,dp(5),dp(5),bg);c.drawText(s,r.centerX()-t.measureText(s)/2,r.centerY()+dp(4),t);}
    double niceFlowStep(double m){if(unit.equals("LPM")){if(m<=20)return 5;if(m<=60)return 10;if(m<=200)return 25;if(m<=600)return 100;if(m<=2000)return 250;return 1000;}if(unit.equals("LPH")){if(m<=1000)return 200;if(m<=3000)return 500;if(m<=10000)return 1000;if(m<=30000)return 5000;return 10000;}if(unit.equals("LPS"))return m<=10?1:5;return m<=10?1:10;}double roundUp(double v,double s){return Math.ceil(v/s)*s;}float x(double f,double m,RectF r){return(float)(r.left+f/m*r.width());}float y(double h,double m,RectF r){return(float)(r.bottom-h/m*r.height());}int dp(float v){return(int)(v*getResources().getDisplayMetrics().density+.5f);}float sp(float v){return v*getResources().getDisplayMetrics().scaledDensity;}
}
