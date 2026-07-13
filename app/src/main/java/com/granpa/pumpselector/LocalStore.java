package com.granpa.pumpselector;

import android.content.*;
import org.json.*;
import java.util.*;

public class LocalStore {
    private static final String PREF="granpa_local_store";
    private static final String SHORTLIST="shortlist";
    private static final String RECENT="recent_models";
    private static final String SEARCHES="recent_searches";

    public static class SavedRef {
        public String asset="", id="", brand="";
        public long time;
        SavedRef(){}
        SavedRef(JSONObject o){asset=o.optString("asset");id=o.optString("id");brand=o.optString("brand");time=o.optLong("time");}
        JSONObject json(){JSONObject o=new JSONObject();try{o.put("asset",asset);o.put("id",id);o.put("brand",brand);o.put("time",time);}catch(Exception ignored){}return o;}
    }

    public static class SearchRef {
        public String asset="",brand="",unit="LPH",cat="all",phase="any";
        public double head,flow1,flow2;
        public boolean range;
        public long time;
        SearchRef(){}
        SearchRef(JSONObject o){asset=o.optString("asset");brand=o.optString("brand");unit=o.optString("unit","LPH");cat=o.optString("cat","all");phase=o.optString("phase","any");head=o.optDouble("head");flow1=o.optDouble("flow1");flow2=o.optDouble("flow2");range=o.optBoolean("range");time=o.optLong("time");}
        JSONObject json(){JSONObject o=new JSONObject();try{o.put("asset",asset);o.put("brand",brand);o.put("unit",unit);o.put("cat",cat);o.put("phase",phase);o.put("head",head);o.put("flow1",flow1);o.put("flow2",flow2);o.put("range",range);o.put("time",time);}catch(Exception ignored){}return o;}
    }

    private static SharedPreferences prefs(Context c){return c.getSharedPreferences(PREF,Context.MODE_PRIVATE);}

    public static boolean isShortlisted(Context c,String asset,String id){
        for(SavedRef r:refs(c,SHORTLIST))if(eq(r.asset,asset)&&eq(r.id,id))return true;
        return false;
    }

    public static boolean toggleShortlist(Context c,String asset,String id,String brand){
        ArrayList<SavedRef> rows=refs(c,SHORTLIST);
        for(int i=0;i<rows.size();i++){
            SavedRef r=rows.get(i);
            if(eq(r.asset,asset)&&eq(r.id,id)){rows.remove(i);saveRefs(c,SHORTLIST,rows);return false;}
        }
        SavedRef r=new SavedRef();r.asset=PumpRepository.normalizeAsset(asset);r.id=id;r.brand=brand;r.time=System.currentTimeMillis();rows.add(0,r);
        while(rows.size()>30)rows.remove(rows.size()-1);
        saveRefs(c,SHORTLIST,rows);return true;
    }

    public static void removeShortlist(Context c,String asset,String id){
        ArrayList<SavedRef> rows=refs(c,SHORTLIST);rows.removeIf(r->eq(r.asset,asset)&&eq(r.id,id));saveRefs(c,SHORTLIST,rows);
    }

    public static ArrayList<SavedRef> shortlist(Context c){return refs(c,SHORTLIST);}

    public static void addRecent(Context c,String asset,String id,String brand){
        ArrayList<SavedRef> rows=refs(c,RECENT);rows.removeIf(r->eq(r.asset,asset)&&eq(r.id,id));
        SavedRef r=new SavedRef();r.asset=PumpRepository.normalizeAsset(asset);r.id=id;r.brand=brand;r.time=System.currentTimeMillis();rows.add(0,r);
        while(rows.size()>20)rows.remove(rows.size()-1);saveRefs(c,RECENT,rows);
    }

    public static ArrayList<SavedRef> recent(Context c){return refs(c,RECENT);}

    public static void addSearch(Context c,String asset,String brand,double head,boolean range,double flow1,double flow2,String unit,String cat,String phase){
        ArrayList<SearchRef> rows=searches(c);
        rows.removeIf(r->eq(r.asset,asset)&&eq(r.cat,cat)&&eq(r.phase,phase)&&eq(r.unit,unit)&&Math.abs(r.head-head)<.001&&Math.abs(r.flow1-flow1)<.001&&Math.abs(r.flow2-flow2)<.001&&r.range==range);
        SearchRef r=new SearchRef();r.asset=asset;r.brand=brand;r.head=head;r.range=range;r.flow1=flow1;r.flow2=flow2;r.unit=unit;r.cat=cat;r.phase=phase;r.time=System.currentTimeMillis();rows.add(0,r);
        while(rows.size()>10)rows.remove(rows.size()-1);saveSearches(c,rows);
    }

    public static ArrayList<SearchRef> searches(Context c){
        ArrayList<SearchRef> rows=new ArrayList<>();
        try{JSONArray a=new JSONArray(prefs(c).getString(SEARCHES,"[]"));for(int i=0;i<a.length();i++)rows.add(new SearchRef(a.getJSONObject(i)));}catch(Exception ignored){}
        return rows;
    }

    public static void clearRecent(Context c){prefs(c).edit().remove(RECENT).remove(SEARCHES).apply();}
    public static void clearShortlist(Context c){prefs(c).edit().remove(SHORTLIST).apply();}

    private static ArrayList<SavedRef> refs(Context c,String key){
        ArrayList<SavedRef> rows=new ArrayList<>();
        try{JSONArray a=new JSONArray(prefs(c).getString(key,"[]"));for(int i=0;i<a.length();i++)rows.add(new SavedRef(a.getJSONObject(i)));}catch(Exception ignored){}
        return rows;
    }
    private static void saveRefs(Context c,String key,List<SavedRef> rows){JSONArray a=new JSONArray();for(SavedRef r:rows)a.put(r.json());prefs(c).edit().putString(key,a.toString()).apply();}
    private static void saveSearches(Context c,List<SearchRef> rows){JSONArray a=new JSONArray();for(SearchRef r:rows)a.put(r.json());prefs(c).edit().putString(SEARCHES,a.toString()).apply();}
    private static boolean eq(String a,String b){return Objects.equals(a==null?"":a,b==null?"":b);}
}
