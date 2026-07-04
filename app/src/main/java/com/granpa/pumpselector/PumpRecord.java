package com.granpa.pumpselector;

import org.json.*;

public class PumpRecord {
    public String id = "";
    public String model = "";
    public String brand = "";
    public String category = "";
    public String phase = "";
    public String hpText = "";
    public String kwText = "";
    public String stages = "";
    public String headRangeText = "";
    public String dischargeRangeText = "";
    public String flowUnitOriginal = "";
    public String size = "";
    public String title = "";
    public String imageUrl = "";
    public int page;
    public double hp = Double.NaN;
    public double kw = Double.NaN;
    public double minHead = Double.NaN;
    public double maxHead = Double.NaN;
    public double minFlowLPH = Double.NaN;
    public double maxFlowLPH = Double.NaN;
    public double[][] curve = new double[0][0];

    public static PumpRecord fromJson(JSONObject o) {
        PumpRecord r = new PumpRecord();
        r.id = o.optString("id", "");
        r.model = o.optString("model", "");
        r.brand = o.optString("brand", "");
        r.category = o.optString("category", "");
        r.phase = o.optString("phase", "");
        r.hpText = o.optString("hpText", "");
        r.kwText = o.optString("kwText", "");
        r.stages = o.optString("stages", "");
        r.headRangeText = o.optString("headRangeText", "");
        r.dischargeRangeText = o.optString("dischargeRangeText", "");
        r.flowUnitOriginal = o.optString("flowUnitOriginal", "");
        r.size = o.optString("size", "");
        r.title = o.optString("title", "");
        r.imageUrl = o.optString("imageUrl", "");
        r.page = o.optInt("page", 0);
        r.hp = o.optDouble("hp", Double.NaN);
        r.kw = o.optDouble("kw", Double.NaN);
        r.minHead = o.optDouble("minHead", Double.NaN);
        r.maxHead = o.optDouble("maxHead", Double.NaN);
        r.minFlowLPH = o.optDouble("minFlowLPH", Double.NaN);
        r.maxFlowLPH = o.optDouble("maxFlowLPH", Double.NaN);
        JSONArray a = o.optJSONArray("curve");
        if (a != null) {
            r.curve = new double[a.length()][2];
            for (int i = 0; i < a.length(); i++) {
                JSONArray p = a.optJSONArray(i);
                if (p != null) {
                    r.curve[i][0] = p.optDouble(0);
                    r.curve[i][1] = p.optDouble(1);
                }
            }
        }
        return r;
    }
}
