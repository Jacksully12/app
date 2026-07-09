package com.granpa.pumpselector;

import android.content.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PumpRepository {
    public static final String TEXMO_ASSET = "texmo_pumps.json";
    public static final String LUBI_ASSET = "lubi_pumps.json";

    private static final HashMap<String, ArrayList<PumpRecord>> cache = new HashMap<>();
    private static final HashMap<String, JSONObject> metaCache = new HashMap<>();

    public static String normalizeAsset(String asset) {
        if (asset == null || asset.trim().isEmpty()) return TEXMO_ASSET;
        return asset.trim();
    }

    public static synchronized ArrayList<PumpRecord> getRecords(Context c) {
        return getRecords(c, TEXMO_ASSET);
    }

    public static synchronized ArrayList<PumpRecord> getRecords(Context c, String asset) {
        asset = normalizeAsset(asset);
        load(c, asset);
        return cache.get(asset);
    }

    public static synchronized JSONObject metadata(Context c) {
        return metadata(c, TEXMO_ASSET);
    }

    public static synchronized JSONObject metadata(Context c, String asset) {
        asset = normalizeAsset(asset);
        load(c, asset);
        return metaCache.get(asset);
    }

    public static synchronized PumpRecord findById(Context c, String id) {
        return findById(c, TEXMO_ASSET, id);
    }

    public static synchronized PumpRecord findById(Context c, String asset, String id) {
        asset = normalizeAsset(asset);
        load(c, asset);
        ArrayList<PumpRecord> rows = cache.get(asset);
        if (rows == null) return null;
        for (PumpRecord r : rows) if (r.id.equals(id)) return r;
        return null;
    }

    private static void load(Context c, String asset) {
        asset = normalizeAsset(asset);
        if (cache.containsKey(asset)) return;

        ArrayList<PumpRecord> rows = new ArrayList<>();
        JSONObject metadata = new JSONObject();

        try {
            InputStream in;
            try {
                in = c.getAssets().open(asset);
            } catch (Exception missing) {
                // Backward compatibility with earlier project versions.
                in = c.getAssets().open("pumps.json");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[16384];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);

            JSONObject root = new JSONObject(new String(out.toByteArray(), StandardCharsets.UTF_8));
            JSONObject m = root.optJSONObject("metadata");
            if (m != null) metadata = m;

            JSONArray arr = root.optJSONArray("records");
            for (int i = 0; arr != null && i < arr.length(); i++) {
                PumpRecord r = PumpRecord.fromJson(arr.getJSONObject(i));
                if (!Double.isNaN(r.hp) && r.curve.length > 0) rows.add(r);
            }
        } catch (Exception e) {
            rows = new ArrayList<>();
            metadata = new JSONObject();
        }

        cache.put(asset, rows);
        metaCache.put(asset, metadata);
    }

    public static List<String> categories(Context c) {
        return categories(c, TEXMO_ASSET);
    }

    public static List<String> categories(Context c, String asset) {
        ArrayList<PumpRecord> rows = getRecords(c, asset);
        TreeSet<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PumpRecord r : rows) if (r.category != null && !r.category.isEmpty()) s.add(r.category);
        return new ArrayList<>(s);
    }

    public static String note(Context c) {
        return note(c, TEXMO_ASSET);
    }

    public static String note(Context c, String asset) {
        ArrayList<PumpRecord> rows = getRecords(c, asset);
        HashSet<Integer> pages = new HashSet<>();
        int min = 999, max = 0;
        String brand = brandName(asset);
        for (PumpRecord r : rows) {
            if (r.page > 0) {
                pages.add(r.page);
                min = Math.min(min, r.page);
                max = Math.max(max, r.page);
            }
        }
        if (rows.isEmpty()) return brand + " • No records loaded";
        return brand + " • " + rows.size() + " pump records • Pages " + min + "–" + max + " • Offline catalogue";
    }

    public static String brandName(String asset) {
        asset = normalizeAsset(asset).toLowerCase(Locale.US);
        if (asset.contains("lubi")) return "LUBI";
        return "TEXMO";
    }
}
