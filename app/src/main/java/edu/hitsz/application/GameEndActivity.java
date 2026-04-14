package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;
import edu.hitsz.score.ScoreAdapter;
import edu.hitsz.score.ScoreDbHelper;
import edu.hitsz.score.ScoreItem;

/**
 * 游戏结束界面 Activity（保留原有界面与按钮布局）
 */
public class GameEndActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "score";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_GAME_TIME = "gameTime";

    private ScoreDbHelper scoreDbHelper;
    private String difficulty;
    private ListView listView;
    private ScoreAdapter scoreAdapter;
    private final List<ScoreItem> leaderboard = new ArrayList<>();
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        int score = getIntent().getIntExtra(EXTRA_SCORE, 0);
        difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null) {
            difficulty = "普通模式";
        }

        scoreDbHelper = new ScoreDbHelper(this);

        TextView tvScore = findViewById(R.id.tvScore);
        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        Button btnRestart = findViewById(R.id.btnRestart);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnDelete = findViewById(R.id.btnDeleteRecord);
        listView = findViewById(R.id.listLeaderboard);

        tvScore.setText("最终得分：" + score);
        tvDifficulty.setText("难度：" + difficulty);

        if (score > 0) {
            scoreDbHelper.addScore("Player", score, difficulty);
        }

        scoreAdapter = new ScoreAdapter(this, leaderboard);
        listView.setAdapter(scoreAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            selectedPosition = position;
            scoreAdapter.setSelectedPosition(position);
        });

        animateScore(tvScore, score);
        loadLeaderboard();

        btnRestart.setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
            intent.putExtra(GameActivity.EXTRA_SOUND, true);
            startActivity(intent);
            finish();
        });

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            if (selectedPosition >= 0 && selectedPosition < leaderboard.size()) {
                ScoreItem item = leaderboard.get(selectedPosition);
                int rows = scoreDbHelper.deleteScore(item.getId());
                Toast.makeText(this, rows > 0 ? "已删除" : "删除失败", Toast.LENGTH_SHORT).show();
                loadLeaderboard();
            } else {
                Toast.makeText(this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLeaderboard() {
        List<ScoreItem> allScores = scoreDbHelper.getScoresByDifficulty(difficulty);
        scoreAdapter.refreshData(allScores);
        selectedPosition = -1;
        listView.clearChoices();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scoreDbHelper != null) {
            scoreDbHelper.close();
        }
    }
}
