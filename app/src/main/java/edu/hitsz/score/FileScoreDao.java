package edu.hitsz.score;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 使用 SharedPreferences + JSON 存储分数（替代 Java NIO 文件操作）
 */
public class FileScoreDao implements ScoreDao {

    private static final String PREFS_NAME = "aircraft_war_scores";
    private static final String KEY_SCORES  = "scores";

    private final SharedPreferences prefs;

    public FileScoreDao(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public boolean saveScore(ScoreRecord record) {
        try {
            JSONArray arr = loadJsonArray();
            JSONObject obj = new JSONObject();
            obj.put("score",      record.getScore());
            obj.put("gameTime",   record.getGameTime());
            obj.put("createTime", record.getCreateTimeMs());
            obj.put("player",     record.getPlayerName());
            obj.put("difficulty", record.getDifficulty());
            arr.put(obj);
            prefs.edit().putString(KEY_SCORES, arr.toString()).apply();
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    public List<ScoreRecord> getAllScores() {
        List<ScoreRecord> list = new ArrayList<>();
        try {
            JSONArray arr = loadJsonArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new ScoreRecord(
                        obj.getInt("score"),
                        obj.getLong("gameTime"),
                        obj.getString("player"),
                        obj.getString("difficulty"),
                        obj.getLong("createTime")
                ));
            }
        } catch (JSONException ignored) {}
        return list;
    }

    @Override
    public List<ScoreRecord> getLeaderboard(int limit) {
        List<ScoreRecord> all = getAllScores();
        Collections.sort(all);
        return limit > 0 && all.size() > limit ? all.subList(0, limit) : all;
    }

    @Override
    public List<ScoreRecord> getLeaderboard() { return getLeaderboard(10); }

    @Override
    public List<ScoreRecord> getLeaderboardByDifficulty(String difficulty) {
        List<ScoreRecord> filtered = new ArrayList<>();
        for (ScoreRecord r : getAllScores()) {
            if (difficulty.equals(r.getDifficulty())) filtered.add(r);
        }
        Collections.sort(filtered);
        return filtered;
    }

    @Override
    public boolean deleteScoreRecord(String playerName, int score, String difficulty) {
        try {
            JSONArray arr = loadJsonArray();
            JSONArray updated = new JSONArray();
            boolean deleted = false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!deleted
                        && obj.getString("player").equals(playerName)
                        && obj.getInt("score") == score
                        && obj.getString("difficulty").equals(difficulty)) {
                    deleted = true;
                } else {
                    updated.put(obj);
                }
            }
            prefs.edit().putString(KEY_SCORES, updated.toString()).apply();
            return deleted;
        } catch (JSONException e) {
            return false;
        }
    }

    @Override
    public boolean clearAllScores() {
        prefs.edit().remove(KEY_SCORES).apply();
        return true;
    }

    @Override
    public int getScoreCount() { return getAllScores().size(); }

    private JSONArray loadJsonArray() {
        String json = prefs.getString(KEY_SCORES, "[]");
        try { return new JSONArray(json); }
        catch (JSONException e) { return new JSONArray(); }
    }
}
