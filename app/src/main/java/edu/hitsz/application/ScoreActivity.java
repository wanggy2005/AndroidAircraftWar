package edu.hitsz.application;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.R;
import edu.hitsz.score.ScoreAdapter;
import edu.hitsz.score.ScoreDbHelper;
import edu.hitsz.score.ScoreItem;

public class ScoreActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE = "score";
    public static final String EXTRA_PLAYER_NAME = "playerName";

    private ListView listViewScores;
    private TextView tvCurrentScore;
    private ScoreAdapter scoreAdapter;
    private final List<ScoreItem> scoreList = new ArrayList<>();

    private ScoreDbHelper dbHelper;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        listViewScores = findViewById(R.id.listViewScores);
        tvCurrentScore = findViewById(R.id.tvCurrentScore);
        dbHelper = new ScoreDbHelper(this);

        scoreAdapter = new ScoreAdapter(this, scoreList);
        listViewScores.setAdapter(scoreAdapter);

        int score = getIntent().getIntExtra(EXTRA_SCORE, -1);
        String playerName = getIntent().getStringExtra(EXTRA_PLAYER_NAME);
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player";
        }

        if (score >= 0) {
            tvCurrentScore.setText(getString(R.string.current_score_text, score));
            saveScoreThenLoad(playerName, score);
        } else {
            tvCurrentScore.setText(R.string.rank_title);
            loadScores();
        }
    }

    private void saveScoreThenLoad(String name, int score) {
        new Thread(() -> {
            dbHelper.addScore(name, score);
            final List<ScoreItem> data = dbHelper.getAllScores();
            mainHandler.post(() -> {
                scoreAdapter.refreshData(data);
            });
        }).start();
    }

    private void loadScores() {
        new Thread(() -> {
            final List<ScoreItem> data = dbHelper.getAllScores();
            mainHandler.post(() -> {
                scoreAdapter.refreshData(data);
            });
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        mainHandler.removeCallbacksAndMessages(null);
    }
}
