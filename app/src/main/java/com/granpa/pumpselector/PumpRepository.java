package com.granpa.pumpselector;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class PumpRepository {
    private static ArrayList<PumpRecord> records;
    private static JSONObject metadata;

    public static synchronized ArrayList<PumpRecord> getRecords(Context context) {
        load(context);
        return records;
    }

    public static synchronized PumpRecord findById(Context context, String id) {
        load(context);
        if (id == null) return null;
        for (PumpRecord r : records) {
            if (id.equals(r.id)) return r;
        }
        return null;
    }

    public static synchronized JSONObject getMetadata(Context context) {
        load(context);
        return metadata;
    }

    public static synchronized List<String> categories(Context context) {
        load(context);
        TreeSet<String> out = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (PumpRecord r : records) {
            if (r.category != null && !r.category.trim().isEmpty()) out.add(r.category.trim());
        }
        return new ArrayList<>(out);
    }

    private static void load(Context context) {
        if (records != null) return;
        records = new ArrayList<>();
        metadata = new JSONObject();
        try {
            InputStream input = context.getAssets().open("pumps.json");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[16384];
            int n;
            while ((n = input.read(chunk)) != -1) buffer.write(chunk, 0, n);
            input.close();

            JSONObject root = new JSONObject(new String(buffer.toByteArray(), StandardCharsets.UTF_8));
            metadata = root.optJSONObject("metadata");
            if (metadata == null) metadata = new JSONObject();

            JSONArray arr = root.optJSONArray("records");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    PumpRecord r = PumpRecord.fromJson(arr.optJSONObject(i));
                    if (r != null && !Double.isNaN(r.hp) && r.curve != null && r.curve.length > 0) {
                        records.add(r);
                    }
                }
            }
            Collections.sort(records, (a, b) -> {
                int hpCompare = Double.compare(a.hp, b.hp);
                if (hpCompare != 0) return hpCompare;
                return a.model.compareToIgnoreCase(b.model);
            });
        } catch (Exception ignored) {
            records = new ArrayList<>();
            metadata = new JSONObject();
        }
    }

    public static String note(Context context) {
        load(context);
        int min = 999, max = 0;
        Set<Integer> pages = new HashSet<>();
        for (PumpRecord r : records) {
            if (r.page > 0) {
                min = Math.min(min, r.page);
                max = Math.max(max, r.page);
                pages.add(r.page);
            }
        }
        if (pages.isEmpty()) return records.size() + " usable pump rows loaded.";
        return String.format(Locale.US, "%d usable pump rows loaded. Pages %d–%d.", records.size(), min, max);
    }
}
