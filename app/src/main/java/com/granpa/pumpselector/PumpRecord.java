package com.granpa.pumpselector;

import org.json.*;

public class PumpRecord {
    public String id="", model="", brand="", category="", phase="", hpText="", kwText="", stages="";
    public String headRangeText="", dischargeRangeText="", flowUnitOriginal="", size="", sheet="", title="";
    public String catalogueSectionText="", categoryDetail="", imageUrl="", normalizedCategory="OTHER";
    public String dataStatus="AUTO_CHECKED", dataNote="", variantLabel="", rpm="", insulationClass="", frameSize="", motorType="";

    // Structured technical fields. These prevent unrelated KSB values from being displayed under one generic “Size” label.
    public String pipeSize="", deliverySize="", suctionSize="", cableSize="", freePassage="", impellerDiameter="";
    public String nrvSize="", ratedCurrent="", nominalSpeed="", startingMethod="", maxSolidSize="", motorDiameter="", technicalNote="";

    public boolean selectable=true;
    public int page;
    public double hp=Double.NaN, kw=Double.NaN, minHead=Double.NaN, maxHead=Double.NaN, minFlowLPH=Double.NaN, maxFlowLPH=Double.NaN;
    public double[][] curve=new double[0][0];

    public static PumpRecord fromJson(JSONObject o) {
        PumpRecord r=new PumpRecord();
        r.id=o.optString("id"); r.model=o.optString("model"); r.brand=o.optString("brand"); r.category=o.optString("category");
        r.phase=o.optString("phase"); r.hpText=o.optString("hpText"); r.kwText=o.optString("kwText"); r.stages=o.optString("stages");
        r.headRangeText=o.optString("headRangeText"); r.dischargeRangeText=o.optString("dischargeRangeText"); r.flowUnitOriginal=o.optString("flowUnitOriginal");
        r.size=o.optString("size"); r.sheet=o.optString("sheet"); r.title=o.optString("title"); r.catalogueSectionText=o.optString("catalogueSectionText");
        r.categoryDetail=o.optString("categoryDetail"); r.imageUrl=o.optString("imageUrl"); r.normalizedCategory=o.optString("normalizedCategory","OTHER");
        r.dataStatus=o.optString("dataStatus","AUTO_CHECKED"); r.dataNote=o.optString("dataNote"); r.variantLabel=o.optString("variantLabel");
        r.rpm=o.optString("rpm"); r.insulationClass=o.optString("insulationClass"); r.frameSize=o.optString("frameSize"); r.motorType=o.optString("motorType");
        r.pipeSize=o.optString("pipeSize"); r.deliverySize=o.optString("deliverySize"); r.suctionSize=o.optString("suctionSize");
        r.cableSize=o.optString("cableSize"); r.freePassage=o.optString("freePassage"); r.impellerDiameter=o.optString("impellerDiameter");
        r.nrvSize=o.optString("nrvSize"); r.ratedCurrent=o.optString("ratedCurrent"); r.nominalSpeed=o.optString("nominalSpeed");
        r.startingMethod=o.optString("startingMethod"); r.maxSolidSize=o.optString("maxSolidSize"); r.motorDiameter=o.optString("motorDiameter");
        r.technicalNote=o.optString("technicalNote");
        r.selectable=o.optBoolean("selectable",true); r.page=o.optInt("page");
        r.hp=o.optDouble("hp",Double.NaN); r.kw=o.optDouble("kw",Double.NaN); r.minHead=o.optDouble("minHead",Double.NaN); r.maxHead=o.optDouble("maxHead",Double.NaN);
        r.minFlowLPH=o.optDouble("minFlowLPH",Double.NaN); r.maxFlowLPH=o.optDouble("maxFlowLPH",Double.NaN);
        JSONArray a=o.optJSONArray("curve");
        if(a!=null){
            r.curve=new double[a.length()][2];
            for(int i=0;i<a.length();i++){
                JSONArray p=a.optJSONArray(i);
                if(p!=null){r.curve[i][0]=p.optDouble(0); r.curve[i][1]=p.optDouble(1);}
            }
        }
        return r;
    }

    public boolean isMotor(){return "MOTOR".equalsIgnoreCase(normalizedCategory);}

    public String displaySize(){
        if(!empty(pipeSize)) return pipeSize;
        if(!empty(deliverySize)) return deliverySize;
        if(!empty(size)) return size;
        if(!empty(nrvSize)) return nrvSize;
        return "";
    }

    public String technicalSummary(){
        StringBuilder s=new StringBuilder();
        add(s, !empty(pipeSize) ? "Pipe " + pipeSize : "");
        add(s, !empty(freePassage) ? "Free passage " + freePassage : "");
        add(s, !empty(impellerDiameter) ? "Impeller " + impellerDiameter : "");
        add(s, !empty(nrvSize) ? "NRV " + nrvSize : "");
        return s.toString();
    }

    private static void add(StringBuilder b,String value){if(empty(value))return;if(b.length()>0)b.append(" • ");b.append(value);}
    private static boolean empty(String s){return s==null||s.trim().isEmpty();}
}
