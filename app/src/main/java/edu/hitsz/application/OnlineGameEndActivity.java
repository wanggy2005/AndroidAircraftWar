package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

/**
 * 联机对战结算页面
 */
public class OnlineGameEndActivity extends AppCompatActivity {

    public static final String EXTRA_MY_SCORE = "myScore";
    public static final String EXTRA_OPPONENT_SCORE = "opponentScore";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_MY_TIME = "myTime";
    public static final String EXTRA_OPPONENT_TIME = "opponentTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_game_end);

        // 获取数据
        int myScore = getIntent().getIntExtra(EXTRA_MY_SCORE, 0);
        int opponentScore = getIntent().getIntExtra(EXTRA_OPPONENT_SCORE, 0);
        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        long myTime = getIntent().getLongExtra(EXTRA_MY_TIME, 0);
        long opponentTime = getIntent().getLongExtra(EXTRA_OPPONENT_TIME, 0);

        // 绑定视图
        TextView tvResult = findViewById(R.id.tvResult);
        TextView tvMyScore = findViewById(R.id.tvMyScore);
        TextView tvOpponentScore = findViewById(R.id.tvOpponentScore);
        TextView tvMyTime = findViewById(R.id.tvMyTime);
        TextView tvOpponentTime = findViewById(R.id.tvOpponentTime);
        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        TextView tvScoreDiff = findViewById(R.id.tvScoreDiff);

        // 设置分数（带动画）
        animateScore(tvMyScore, myScore);
        animateScore(tvOpponentScore, opponentScore);

        // 存活时间
        tvMyTime.setText("存活: " + formatTime(myTime));
        tvOpponentTime.setText("存活: " + formatTime(opponentTime));

        // 难度
        tvDifficulty.setText("难度: " + (difficulty != null ? difficulty : "未知"));

        // 分差
        int diff = Math.abs(myScore - opponentScore);
        tvScoreDiff.setText("分差: " + diff);

        // 胜负判定
        if (myScore > opponentScore) {
            tvResult.setText("胜利!");
            tvResult.setTextColor(0xFFFFD700); // 金色
        } else if (myScore < opponentScore) {
            tvResult.setText("失败");
            tvResult.setTextColor(0xFFFF4444); // 红色
        } else {
            tvResult.setText("平局");
            tvResult.setTextColor(0xFFB8B8D1); // 灰色
        }

        // 按钮事件
        findViewById(R.id.btnBackToLobby).setOnClickListener(v -> {
            Intent intent = new Intent(this, OnlineLobbyActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnBackToMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
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
        // 返回主菜单
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
