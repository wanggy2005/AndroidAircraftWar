package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;
import edu.hitsz.network.ApiClient;
import edu.hitsz.score.ScoreAdapter;
import edu.hitsz.score.ScoreItem;

public class OnlineLeaderboardActivity extends AppCompatActivity {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_ROOM_ID = "roomId";
    public static final String EXTRA_PLAYER_ID = "playerId";
    public static final String EXTRA_SERVER_URL = "serverUrl";
    public static final String EXTRA_RETURN_TO_ROOM = "returnToRoom";
    public static final String EXTRA_BACK_TO_ONLINE_HOME = "backToOnlineHome";

    private String difficulty;
    private String roomId;
    private String playerId;
    private String serverUrl;
    private boolean returnToRoom;
    private boolean backToOnlineHome;

    private ListView listView;
    private ScoreAdapter scoreAdapter;
    private final List<ScoreItem> leaderboard = new ArrayList<>();
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_leaderboard);

        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        playerId = getIntent().getStringExtra(EXTRA_PLAYER_ID);
        serverUrl = getIntent().getStringExtra(EXTRA_SERVER_URL);
        returnToRoom = getIntent().getBooleanExtra(EXTRA_RETURN_TO_ROOM, false);
        backToOnlineHome = getIntent().getBooleanExtra(EXTRA_BACK_TO_ONLINE_HOME, false);

        if (difficulty == null || difficulty.isEmpty()) {
            difficulty = "普通模式";
        }
        if (serverUrl != null && !serverUrl.isEmpty()) {
            ApiClient.setBaseUrl(serverUrl);
        }

        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        Button btnRestart = findViewById(R.id.btnRestart);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnDelete = findViewById(R.id.btnDeleteRecord);
        listView = findViewById(R.id.listLeaderboard);

        tvDifficulty.setText("难度：" + difficulty);

        scoreAdapter = new ScoreAdapter(this, leaderboard);
        listView.setAdapter(scoreAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            scoreAdapter.setSelectedPosition(position);
        });

        btnRestart.setText("再玩一局");
        btnRestart.setOnClickListener(v -> handleRestart());
        btnMenu.setOnClickListener(v -> handleMenu());
        btnDelete.setOnClickListener(v -> deleteSelectedRecord());

        loadLeaderboard();
    }

    private void loadLeaderboard() {
        new Thread(() -> {
            try {
                String response = ApiClient.get("/api/leaderboard/list?difficulty=" +
                        URLEncoder.encode(difficulty, StandardCharsets.UTF_8.name()));
                List<ScoreItem> items = parseEntries(response);
                runOnUiThread(() -> {
                    scoreAdapter.refreshData(items);
                    selectedPosition = -1;
                    listView.clearChoices();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "加载排行榜失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private List<ScoreItem> parseEntries(String response) throws Exception {
        List<ScoreItem> items = new ArrayList<>();
        JSONObject root = new JSONObject(response);
        JSONArray entries = root.optJSONArray("entries");
        if (entries == null) {
            return items;
        }
        for (int i = 0; i < entries.length(); i++) {
            JSONObject item = entries.optJSONObject(i);
            if (item == null) {
                continue;
            }
            items.add(new ScoreItem(
                    item.optInt("id", 0),
                    item.optString("nickname", "player"),
                    item.optInt("score", 0),
                    item.optString("difficulty", difficulty),
                    item.optString("playerId", null)
            ));
        }
        return items;
    }

    private void deleteSelectedRecord() {
        if (selectedPosition < 0 || selectedPosition >= leaderboard.size()) {
            Toast.makeText(this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show();
            return;
        }
        ScoreItem item = leaderboard.get(selectedPosition);
        new Thread(() -> {
            try {
                String body = "{\"id\":" + item.getId() + ",\"playerId\":\"" + playerId + "\"}";
                String response = ApiClient.post("/api/leaderboard/delete", body);
                if (ApiClient.isSuccess(response)) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                        loadLeaderboard();
                    });
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() -> Toast.makeText(this, "删除失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "删除失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void handleRestart() {
        if (returnToRoom) {
            Intent intent = new Intent(this, OnlineLobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("returnToRoom", true);
            intent.putExtra("roomId", roomId);
            intent.putExtra("playerId", playerId);
            intent.putExtra("serverUrl", serverUrl);
            startActivity(intent);
            finish();
            return;
        }
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameActivity.EXTRA_SOUND, true);
        startActivity(intent);
        finish();
    }

    private void handleMenu() {
        if (backToOnlineHome) {
            Intent intent = new Intent(this, OnlineLobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
            return;
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
