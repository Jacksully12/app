package com.granpa.pumpselector;
import android.content.*; import org.json.*; import java.io.*; import java.nio.charset.StandardCharsets; import java.util.*;
public class PumpRepository { private static ArrayList<PumpRecord> records; private static JSONObject metadata;
 public static synchronized ArrayList<PumpRecord> getRecords(Context c){ load(c); return records; }
 public static synchronized PumpRecord findById(Context c,String id){ load(c); for(PumpRecord r:records) if(r.id.equals(id)) return r; return null; }
 public static synchronized JSONObject getMetadata(Context c){ load(c); return metadata; }
 private static void load(Context c){ if(records!=null)return; records=new ArrayList<>(); metadata=new JSONObject(); try{ InputStream in=c.getAssets().open("pumps.json"); ByteArrayOutputStream b=new ByteArrayOutputStream(); byte[] buf=new byte[16384]; int n; while((n=in.read(buf))!=-1)b.write(buf,0,n); JSONObject root=new JSONObject(new String(b.toByteArray(), StandardCharsets.UTF_8)); metadata=root.optJSONObject("metadata"); JSONArray arr=root.optJSONArray("records"); for(int i=0;arr!=null&&i<arr.length();i++){ PumpRecord r=PumpRecord.fromJson(arr.getJSONObject(i)); if(!Double.isNaN(r.hp)&&r.curve.length>0) records.add(r); } }catch(Exception e){} }
 public static List<String> categories(Context c){ load(c); TreeSet<String> s=new TreeSet<>(String.CASE_INSENSITIVE_ORDER); for(PumpRecord r:records) if(r.category!=null&&!r.category.isEmpty())s.add(r.category); return new ArrayList<>(s); }
 public static String note(Context c){ load(c); int min=999,max=0; HashSet<Integer> pages=new HashSet<>(); for(PumpRecord r:records){ if(r.page>0){pages.add(r.page); min=Math.min(min,r.page); max=Math.max(max,r.page);} } return records.size()+" usable pump rows loaded. Pages "+min+"–"+max+". App name: Granpa."; }
}
