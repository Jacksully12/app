package com.texmo.pumpselector;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PumpRepository {
    private static List<PumpRecord> records;
    private static JSONObject metadata;

    public static synchronized List<PumpRecord> getRecords(Context context) {
        ensureLoaded(context);
        return records;
    }

    public static synchronized JSONObject getMetadata(Context context) {
        ensureLoaded(context);
        return metadata;
    }

    public static synchronized PumpRecord findById(Context context, String id) {
        ensureLoaded(context);
        if (id == null) return null;
        for (PumpRecord r : records) {
            if (id.equals(r.id)) return r;
        }
        return null;
    }

    public static synchronized List<String> getCategories(Context context) {
        ensureLoaded(context);
        Set<String> set = new HashSet<>();
        for (PumpRecord r : records) if (r.category != null && !r.category.trim().isEmpty()) set.add(r.category);
        List<String> out = new ArrayList<>(set);
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    public static synchronized List<String> getBrands(Context context) {
        ensureLoaded(context);
        Set<String> set = new HashSet<>();
        for (PumpRecord r : records) if (r.brand != null && !r.brand.trim().isEmpty()) set.add(r.brand);
        List<String> out = new ArrayList<>(set);
        Collections.sort(out, String.CASE_INSENSITIVE_ORDER);
        return out;
    }

    private static void ensureLoaded(Context context) {
        if (records != null) return;
        records = new ArrayList<>();
        try {
            InputStream input = context.getAssets().open("pumps.json");
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] tmp = new byte[16 * 1024];
            int n;
            while ((n = input.read(tmp)) != -1) buffer.write(tmp, 0, n);
            input.close();
            String json = new String(buffer.toByteArray(), StandardCharsets.UTF_8);
            JSONObject root = new JSONObject(json);
            metadata = root.optJSONObject("metadata");
            if (metadata == null) metadata = new JSONObject();
            JSONArray arr = root.optJSONArray("records");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;
                    PumpRecord rec = PumpRecord.fromJson(obj);
                    if (isUsable(rec)) records.add(rec);
                }
            }
            Collections.sort(records, new Comparator<PumpRecord>() {
                @Override public int compare(PumpRecord a, PumpRecord b) {
                    int hp = Double.compare(safeHp(a), safeHp(b));
                    if (hp != 0) return hp;
                    return a.model.compareToIgnoreCase(b.model);
                }
            });
        } catch (Exception e) {
            metadata = new JSONObject();
            records = new ArrayList<>();
        }
    }

    private static boolean isUsable(PumpRecord r) {
        return r != null && !Double.isNaN(r.hp) && r.curve != null && r.curve.length >= 1;
    }

    private static double safeHp(PumpRecord r) {
        return Double.isNaN(r.hp) ? 99999.0 : r.hp;
    }

    public static String dataNote(Context context) {
        ensureLoaded(context);
        int minPage = 9999;
        int maxPage = 0;
        Set<Integer> pages = new HashSet<>();
        for (PumpRecord r : records) {
            if (r.page > 0) {
                pages.add(r.page);
                minPage = Math.min(minPage, r.page);
                maxPage = Math.max(maxPage, r.page);
            }
        }
        String pageText = pages.isEmpty() ? "-" : String.format(Locale.US, "%d–%d", minPage, maxPage);
        int total = metadata != null ? metadata.optInt("totalRecords", records.size()) : records.size();
        int skipped = Math.max(0, total - records.size());
        String skippedText = skipped == 0 ? "0 skipped rows after QA correction." : skipped + " skipped rows.";
        return String.format(Locale.US, "%d usable rows from %d catalogue rows. Pages %s. %s", records.size(), total, pageText, skippedText);
    }
}
