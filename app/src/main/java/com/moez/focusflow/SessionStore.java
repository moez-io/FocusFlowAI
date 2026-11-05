package com.moez.focusflow;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Private on-device history. No permissions, accounts, or remote server. */
public final class SessionStore {
    private static final String PREFS = "focusflow_history_v2";
    private static final String KEY = "sessions";
    private SessionStore() {}
    public static final class Session {
        public final long timestamp;
        public final int minutes, interruptions, breaks, energy;
        public final int score;
        Session(long timestamp, int minutes, int interruptions, int breaks, int energy, int score) {
            this.timestamp = timestamp;
            this.minutes = minutes;
            this.interruptions = interruptions;
            this.breaks = breaks;
            this.energy = energy;
            this.score = score;
        }
    }
    public static List<Session> all(Context context) {
        List<Session> entries = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String raw = prefs.getString(KEY, "[]");
        try {
            JSONArray json = new JSONArray(raw);
            for (int i = 0; i < json.length(); i++) {
                JSONObject o = json.getJSONObject(i);
                entries.add(new Session(o.optLong("date"), o.optInt("minutes"),
                        o.optInt("interruptions"), o.optInt("breaks"),
                        o.optInt("energy"), o.optInt("score")));
            }
        } catch (Exception ignored) { /* Corrupted local history becomes an empty list. */ }
        return entries;
    }
    public static void add(Context context, int minutes, int interruptions, int breaks, int energy, int score) {
        JSONArray json = new JSONArray();
        try {
            JSONObject newest = new JSONObject();
            newest.put("date", System.currentTimeMillis());
            newest.put("minutes", minutes);
            newest.put("interruptions", interruptions);
            newest.put("breaks", breaks);
            newest.put("energy", energy);
            newest.put("score", score);
            json.put(newest);
            List<Session> old = all(context);
            for (int i = 0; i < Math.min(old.size(), 59); i++) {
                Session s = old.get(i);
                JSONObject o = new JSONObject();
                o.put("date", s.timestamp);
                o.put("minutes", s.minutes);
                o.put("interruptions", s.interruptions);
                o.put("breaks", s.breaks);
                o.put("energy", s.energy);
                o.put("score", s.score);
                json.put(o);
            }
        } catch (Exception ignored) { return; }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, json.toString()).apply();
    }
    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY).apply();
    }
    public static int average(List<Session> items) {
        if (items.isEmpty()) return 0;
        int total = 0;
        for (Session s : items) total += s.score;
        return Math.round(total / (float) items.size());
    }
    public static int best(List<Session> items) {
        int best = 0;
        for (Session s : items) best = Math.max(best, s.score);
        return best;
    }
}
