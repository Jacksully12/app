package com.granpa.pumpselector;
import android.content.*; import android.graphics.*; import android.view.*; import java.util.*;

public class PerformanceCurveView extends View {
    Paint grid=new Paint(1), axis=new Paint(1), txt=new Paint(1), curveP=new Paint(1), pt=new Paint(1), sel=new Paint(1);
    double[][] curve; Double sh, sf;
    public PerformanceCurveView(Context c){
        super(c);
        grid.setColor(Color.rgb(225,232,241)); grid.setStrokeWidth(dp(1));
        axis.setColor(Ui.MUTED); axis.setStrokeWidth(dp(1.4f));
        txt.setColor(Ui.TEXT); txt.setTextSize(sp(12));
        curveP.setColor(Color.rgb(0,96,216)); curveP.setStyle(Paint.Style.STROKE); curveP.setStrokeWidth(dp(3));
        pt.setColor(Color.WHITE); pt.setStyle(Paint.Style.FILL);
        sel.setColor(Color.rgb(255,132,0)); sel.setStyle(Paint.Style.FILL);
    }
    public void setData(double[][] c, Double head, Double flow){ curve=c; sh=head; sf=flow; invalidate(); }
    protected void onMeasure(int w,int h){ setMeasuredDimension(MeasureSpec.getSize(w), resolveSize(dp(320), h)); }
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        ArrayList<double[]> pts=new ArrayList<>();
        if(curve!=null) for(double[] p:curve) if(p!=null&&p.length>=2&&!Double.isNaN(p[0])&&!Double.isNaN(p[1])) pts.add(new double[]{p[0],p[1]});
        if(pts.isEmpty()) return;
        Collections.sort(pts, Comparator.comparingDouble(a->a[1])); // low flow to high flow
        double minF=pts.get(0)[1], maxF=pts.get(pts.size()-1)[1], maxH=0;
        for(double[] p:pts){ minF=Math.min(minF,p[1]); maxF=Math.max(maxF,p[1]); maxH=Math.max(maxH,p[0]); }
        if(sf!=null&&!Double.isNaN(sf)){ minF=Math.min(minF,sf); maxF=Math.max(maxF,sf); }
        if(sh!=null&&!Double.isNaN(sh)) maxH=Math.max(maxH,sh);
        if(maxF-minF<1) maxF=minF+1000;
        maxH=Math.ceil((maxH*1.15)/10.0)*10.0; if(maxH<10)maxH=10;
        RectF plot=new RectF(dp(48),dp(18),getWidth()-dp(18),getHeight()-dp(46));

        for(int i=0;i<=5;i++){ float x=plot.left+i*plot.width()/5f; canvas.drawLine(x,plot.top,x,plot.bottom,grid); String st=String.format(Locale.US,"%,.0f",minF+(maxF-minF)*i/5); canvas.drawText(st,x-txt.measureText(st)/2,getHeight()-dp(20),txt); }
        for(int i=0;i<=5;i++){ float y=plot.bottom-i*plot.height()/5f; canvas.drawLine(plot.left,y,plot.right,y,grid); String st=String.format(Locale.US,"%.0f",maxH*i/5); canvas.drawText(st,plot.left-txt.measureText(st)-dp(6),y+dp(4),txt); }
        canvas.drawLine(plot.left,plot.top,plot.left,plot.bottom,axis); canvas.drawLine(plot.left,plot.bottom,plot.right,plot.bottom,axis);
        Paint lab=new Paint(txt); lab.setTextSize(sp(13)); String xl="Flow Rate (LPH)"; canvas.drawText(xl,plot.centerX()-lab.measureText(xl)/2,getHeight()-dp(2),lab); canvas.save(); canvas.rotate(-90,dp(14),plot.centerY()); canvas.drawText("Head (m)",dp(14),plot.centerY(),lab); canvas.restore();

        Path path=new Path();
        for(int i=0;i<pts.size();i++){
            float x=x(pts.get(i)[1],minF,maxF,plot), y=y(pts.get(i)[0],maxH,plot);
            if(i==0) path.moveTo(x,y);
            else {
                float px=x(pts.get(i-1)[1],minF,maxF,plot), py=y(pts.get(i-1)[0],maxH,plot);
                float mx=(px+x)/2f;
                path.cubicTo(mx,py,mx,y,x,y);
            }
        }
        canvas.drawPath(path,curveP);
        for(double[] p:pts){ float x=x(p[1],minF,maxF,plot), y=y(p[0],maxH,plot); canvas.drawCircle(x,y,dp(6),pt); Paint ring=new Paint(axis); ring.setColor(Color.rgb(0,96,216)); ring.setStyle(Paint.Style.STROKE); ring.setStrokeWidth(dp(2)); canvas.drawCircle(x,y,dp(6),ring); }
        if(sh!=null&&sf!=null&&!Double.isNaN(sh)&&!Double.isNaN(sf)){
            float sx=x(sf,minF,maxF,plot), sy=y(sh,maxH,plot);
            Paint dashed=new Paint(1); dashed.setColor(Color.rgb(255,132,0)); dashed.setStyle(Paint.Style.STROKE); dashed.setStrokeWidth(dp(1)); dashed.setPathEffect(new DashPathEffect(new float[]{8,8},0));
            canvas.drawLine(plot.left,sy,sx,sy,dashed); canvas.drawLine(sx,sy,sx,plot.bottom,dashed);
            canvas.drawCircle(sx,sy,dp(9),sel);
        }
    }
    float x(double f,double minF,double maxF,RectF r){ return (float)(r.left+(f-minF)/(maxF-minF)*r.width()); }
    float y(double h,double maxH,RectF r){ return (float)(r.bottom-(h/maxH)*r.height()); }
    int dp(float v){ return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    float sp(float v){ return v*getResources().getDisplayMetrics().scaledDensity; }
}
