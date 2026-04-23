package edu.hitsz.application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.network.ApiClient;

/**
 * 联机大厅页面
 */
public class OnlineLobbyActivity extends AppCompatActivity {

    private String playerId;
    private String nickname;
    private String currentRoomId;
    private boolean isHost = false;

    private LinearLayout cardRoomActions, cardRoomStatus;
    private TextView tvPlayerInfo, tvRoomId, tvRoomDifficulty, tvRoomStatus;
    private TextView tvHostStatus, tvGuestStatus;
    private Spinner spinnerDifficulty;
    private EditText etRoomId;
    private View btnReady, btnStartGame, btnLeaveRoom;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pollRunnable;
    private boolean polling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_lobby);

        // 读取登录信息
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        playerId = prefs.getString(LoginActivity.KEY_PLAYER_ID, "");
        nickname = prefs.getString(LoginActivity.KEY_NICKNAME, "");
        String serverUrl = prefs.getString(LoginActivity.KEY_SERVER_URL, "http://10.0.2.2:8888");
        ApiClient.setBaseUrl(serverUrl);

        // 绑定视图
        tvPlayerInfo = findViewById(R.id.tvPlayerInfo);
        cardRoomActions = findViewById(R.id.cardRoomActions);
        cardRoomStatus = findViewById(R.id.cardRoomStatus);
        tvRoomId = findViewById(R.id.tvRoomId);
        tvRoomDifficulty = findViewById(R.id.tvRoomDifficulty);
        tvRoomStatus = findViewById(R.id.tvRoomStatus);
        tvHostStatus = findViewById(R.id.tvHostStatus);
        tvGuestStatus = findViewById(R.id.tvGuestStatus);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        etRoomId = findViewById(R.id.etRoomId);
        btnReady = findViewById(R.id.btnReady);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        tvPlayerInfo.setText("玩家: " + nickname + " (ID: " + playerId + ")");

        // 难度选择
        String[] difficulties = {"简单模式", "普通模式", "困难模式"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, difficulties) {
            @Override
            public android.view.View getView(int pos, android.view.View cv, android.view.ViewGroup p) {
                android.widget.TextView v = (android.widget.TextView) super.getView(pos, cv, p);
                v.setTextColor(android.graphics.Color.WHITE);
                v.setTextSize(14);
                return v;
            }
            @Override
            public android.view.View getDropDownView(int pos, android.view.View cv, android.view.ViewGroup p) {
                android.widget.TextView v = (android.widget.TextView) super.getDropDownView(pos, cv, p);
                v.setTextColor(android.graphics.Color.WHITE);
                v.setTextSize(14);
                v.setPadding(16, 12, 16, 12);
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
        spinnerDifficulty.setSelection(1);

        // 按钮事件
        findViewById(R.id.btnCreateRoom).setOnClickListener(v -> createRoom());
        findViewById(R.id.btnJoinRoom).setOnClickListener(v -> joinRoom());
        btnReady.setOnClickListener(v -> markReady());
        btnStartGame.setOnClickListener(v -> startGame());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> finish());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());

        // 从结算页面“回到房间”跳转过来，自动加入房间
        String returnRoomId = getIntent().getStringExtra("roomId");
        String returnServerUrl = getIntent().getStringExtra("serverUrl");
        if (returnRoomId != null && !returnRoomId.isEmpty()) {
            if (returnServerUrl != null) ApiClient.setBaseUrl(returnServerUrl);
            etRoomId.setText(returnRoomId);
            // 延迟自动加入房间
            handler.postDelayed(this::joinRoom, 500);
        }
    }

    private void createRoom() {
        String difficulty = (String) spinnerDifficulty.getSelectedItem();
        new Thread(() -> {
            try {
                String body = "{\"playerId\":\"" + playerId + "\",\"difficulty\":\"" + difficulty + "\"}";
                String response = ApiClient.post("/api/room/create", body);
                if (ApiClient.isSuccess(response)) {
                    currentRoomId = ApiClient.extractString(response, "roomId");
                    isHost = true;
                    runOnUiThread(() -> {
                        showRoomStatus(difficulty);
                        Toast.makeText(this, "房间创建成功: " + currentRoomId, Toast.LENGTH_SHORT).show();
                    });
                    startPolling();
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() -> Toast.makeText(this, "创建失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void joinRoom() {
        String roomId = etRoomId.getText().toString().trim();
        if (roomId.isEmpty()) {
            Toast.makeText(this, "请输入房间号", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            try {
                String body = "{\"playerId\":\"" + playerId + "\",\"roomId\":\"" + roomId + "\"}";
                String response = ApiClient.post("/api/room/join", body);
                if (ApiClient.isSuccess(response)) {
                    currentRoomId = roomId;
                    isHost = false;
                    String difficulty = ApiClient.extractString(response, "difficulty");
                    runOnUiThread(() -> {
                        showRoomStatus(difficulty != null ? difficulty : "未知");
                        Toast.makeText(this, "成功加入房间", Toast.LENGTH_SHORT).show();
                    });
                    startPolling();
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() -> Toast.makeText(this, "加入失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void markReady() {
        new Thread(() -> {
            try {
                String body = "{\"playerId\":\"" + playerId + "\",\"roomId\":\"" + currentRoomId + "\"}";
                String response = ApiClient.post("/api/room/ready", body);
                if (ApiClient.isSuccess(response)) {
                    runOnUiThread(() -> {
                        btnReady.setEnabled(false);
                        Toast.makeText(this, "已准备", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void startGame() {
        new Thread(() -> {
            try {
                String body = "{\"playerId\":\"" + playerId + "\",\"roomId\":\"" + currentRoomId + "\"}";
                String response = ApiClient.post("/api/room/start", body);
                if (ApiClient.isSuccess(response)) {
                    runOnUiThread(this::launchGame);
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() -> Toast.makeText(this, "开始失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "网络错误", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void launchGame() {
        stopPolling();
        // 获取房间信息中的难度
        String difficulty = tvRoomDifficulty.getText().toString().replace("难度: ", "");
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        boolean soundOn = true; // 联机模式默认开启音效

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameActivity.EXTRA_SOUND, soundOn);
        intent.putExtra(GameActivity.EXTRA_ONLINE, true);
        intent.putExtra(GameActivity.EXTRA_ROOM_ID, currentRoomId);
        intent.putExtra(GameActivity.EXTRA_PLAYER_ID, playerId);
        intent.putExtra(GameActivity.EXTRA_SERVER_URL, ApiClient.getBaseUrl());
        startActivity(intent);
        finish();
    }

    private void leaveRoom() {
        stopPolling();
        currentRoomId = null;
        cardRoomActions.setVisibility(View.VISIBLE);
        cardRoomStatus.setVisibility(View.GONE);
    }

    private void logout() {
        stopPolling();
        // 清除登录信息
        SharedPreferences prefs = getSharedPreferences(LoginActivity.PREF_NAME, MODE_PRIVATE);
        prefs.edit().clear().apply();
        // 跳转到登录页面
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showRoomStatus(String difficulty) {
        cardRoomActions.setVisibility(View.GONE);
        cardRoomStatus.setVisibility(View.VISIBLE);
        tvRoomId.setText("房间号: " + currentRoomId);
        tvRoomDifficulty.setText("难度: " + difficulty);
        tvRoomStatus.setText("状态: 等待中...");
        btnStartGame.setVisibility(isHost ? View.VISIBLE : View.GONE);
        btnReady.setEnabled(true);
    }

    private void startPolling() {
        polling = true;
        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!polling) return;
                new Thread(() -> {
                    try {
                        String response = ApiClient.get("/api/room/info?roomId=" + currentRoomId);
                        if (ApiClient.isSuccess(response)) {
                            String status = ApiClient.extractString(response, "status");
                            boolean hostReady = ApiClient.extractBoolean(response, "hostReady", false);
                            boolean guestReady = ApiClient.extractBoolean(response, "guestReady", false);
                            String guestId = ApiClient.extractString(response, "guestId");

                            runOnUiThread(() -> {
                                tvHostStatus.setText("房主: " + (hostReady ? "✓ 已准备" : "✗ 未准备"));
                                if (guestId != null) {
                                    tvGuestStatus.setText("对手: " + (guestReady ? "✓ 已准备" : "✗ 未准备"));
                                    tvRoomStatus.setText("状态: 对手已加入");
                                } else {
                                    tvGuestStatus.setText("对手: 等待加入...");
                                    tvRoomStatus.setText("状态: 等待对手加入...");
                                }

                                // 游戏开始
                                if ("PLAYING".equals(status)) {
                                    launchGame();
                                }
                            });
                        }
                    } catch (Exception ignored) {}
                    if (polling) handler.postDelayed(pollRunnable, 1000);
                }).start();
            }
        };
        handler.postDelayed(pollRunnable, 500);
    }

    private void stopPolling() {
        polling = false;
        if (pollRunnable != null) handler.removeCallbacks(pollRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPolling();
    }
}
