package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;
import edu.hitsz.network.ApiClient;

public class OnlineGameEndActivity extends AppCompatActivity {

    public static final String EXTRA_MY_SCORE = "myScore";
    public static final String EXTRA_OPPONENT_SCORE = "opponentScore";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_MY_TIME = "myTime";
    public static final String EXTRA_OPPONENT_TIME = "opponentTime";

    private String roomId;
    private String playerId;
    private String serverUrl;
    private String difficulty;
    private int myScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game_end);

        myScore = getIntent().getIntExtra(EXTRA_MY_SCORE, 0);
        int opponentScore = getIntent().getIntExtra(EXTRA_OPPONENT_SCORE, 0);
        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        long myTime = getIntent().getLongExtra(EXTRA_MY_TIME, 0);
        long opponentTime = getIntent().getLongExtra(EXTRA_OPPONENT_TIME, 0);
        roomId = getIntent().getStringExtra("roomId");
        playerId = getIntent().getStringExtra("playerId");
        serverUrl = getIntent().getStringExtra("serverUrl");

        if (serverUrl != null) {
            ApiClient.setBaseUrl(serverUrl);
        }

        TextView tvResult = findViewById(R.id.tvResult);
        TextView tvMyScore = findViewById(R.id.tvMyScore);
        TextView tvOpponentScore = findViewById(R.id.tvOpponentScore);
        TextView tvMyTime = findViewById(R.id.tvMyTime);
        TextView tvOpponentTime = findViewById(R.id.tvOpponentTime);
        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        TextView tvScoreDiff = findViewById(R.id.tvScoreDiff);

        animateScore(tvMyScore, myScore);
        animateScore(tvOpponentScore, opponentScore);
        tvMyTime.setText("存活: " + formatTime(myTime));
        tvOpponentTime.setText("存活: " + formatTime(opponentTime));
        tvDifficulty.setText("难度: " + (difficulty != null ? difficulty : "未知"));

        int diff = Math.abs(myScore - opponentScore);
        tvScoreDiff.setText("分差: " + diff);

        if (myScore > opponentScore) {
            tvResult.setText("胜利!");
            tvResult.setTextColor(0xFFFFD700);
        } else if (myScore < opponentScore) {
            tvResult.setText("失败");
            tvResult.setTextColor(0xFFFF4444);
        } else {
            tvResult.setText("平局");
            tvResult.setTextColor(0xFFB8B8D1);
        }

        submitScoreIfNeeded();

        findViewById(R.id.btnViewLeaderboard).setOnClickListener(v -> openLeaderboard());

        findViewById(R.id.btnBackToRoom).setOnClickListener(v -> {
            v.setEnabled(false);
            new Thread(() -> {
                try {
                    if (serverUrl != null) {
                        ApiClient.setBaseUrl(serverUrl);
                    }
                    String body = "{\"playerId\":\"" + playerId + "\",\"roomId\":\"" + roomId + "\"}";
                    ApiClient.post("/api/room/return", body);
                } catch (Exception ignored) {
                }
                runOnUiThread(() -> {
                    Intent intent = new Intent(this, OnlineLobbyActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("returnToRoom", true);
                    intent.putExtra("roomId", roomId);
                    intent.putExtra("playerId", playerId);
                    intent.putExtra("serverUrl", serverUrl);
                    startActivity(intent);
                    finish();
                });
            }).start();
        });

        findViewById(R.id.btnBackToLobby).setOnClickListener(v -> {
            Intent intent = new Intent(this, OnlineLobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, OnlineLobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void submitScoreIfNeeded() {
        if (roomId == null || playerId == null || difficulty == null) {
            return;
        }
        new Thread(() -> {
            try {
                String body = "{\"playerId\":\"" + playerId + "\",\"roomId\":\"" + roomId +
                        "\",\"difficulty\":\"" + difficulty + "\",\"score\":" + myScore + "}";
                ApiClient.post("/api/leaderboard/submit", body);
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "在线排行榜提交失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void openLeaderboard() {
        Intent intent = new Intent(this, OnlineLeaderboardActivity.class);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_ROOM_ID, roomId);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_PLAYER_ID, playerId);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_SERVER_URL, serverUrl);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_RETURN_TO_ROOM, true);
        intent.putExtra(OnlineLeaderboardActivity.EXTRA_BACK_TO_ONLINE_HOME, true);
        startActivity(intent);
    }

    private void animateScore(final TextView textView, final int targetScore) {
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofInt(0, targetScore);
        animator.setDuration(1000);
        animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(value));
        });
        animator.start();
    }

    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        }
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return minutes + "分" + seconds + "秒";
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, OnlineLobbyActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
