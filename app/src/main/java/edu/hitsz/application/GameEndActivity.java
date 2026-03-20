package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

import edu.hitsz.score.FileScoreDao;
import edu.hitsz.score.ScoreManager;
import edu.hitsz.score.ScoreRecord;

import java.util.ArrayList;
import java.util.List;

/**
 * 游戏结束界面 Activity（替代 Java SE 的 Game_End + JFrame）
 */
public class GameEndActivity extends AppCompatActivity {

    public static final String EXTRA_SCORE      = "score";
    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_GAME_TIME  = "gameTime";

    private ScoreManager scoreManager;
    private String difficulty;
    private ListView listView;
    private ArrayAdapter<String> listAdapter;
    private List<ScoreRecord> leaderboard = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_end);

        int    score    = getIntent().getIntExtra(EXTRA_SCORE, 0);
        difficulty      = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        long   gameTime = getIntent().getLongExtra(EXTRA_GAME_TIME, 0);
        if (difficulty == null) difficulty = "普通模式";

        scoreManager = new ScoreManager(new FileScoreDao(this));

        TextView tvScore      = findViewById(R.id.tvScore);
        TextView tvDifficulty = findViewById(R.id.tvDifficulty);
        Button   btnRestart   = findViewById(R.id.btnRestart);
        Button   btnMenu      = findViewById(R.id.btnMenu);
        Button   btnDelete    = findViewById(R.id.btnDeleteRecord);
        listView              = findViewById(R.id.listLeaderboard);

        tvScore.setText("最终得分：" + score);
        tvDifficulty.setText("难度：" + difficulty);

        // 保存本局分数
        if (score > 0) {
            scoreManager.saveGameScore(score, gameTime, "Player", difficulty);
        }

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
            int pos = listView.getCheckedItemPosition();
            if (pos >= 0 && pos < leaderboard.size()) {
                ScoreRecord rec = leaderboard.get(pos);
                boolean ok = scoreManager.deleteScoreRecord(
                        rec.getPlayerName(), rec.getScore(), rec.getDifficulty());
                Toast.makeText(this, ok ? "已删除" : "删除失败", Toast.LENGTH_SHORT).show();
                loadLeaderboard();
            } else {
                Toast.makeText(this, "请先选择要删除的记录", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLeaderboard() {
        leaderboard = scoreManager.getLeaderboard(difficulty);
        List<String> displayList = new ArrayList<>();
        for (int i = 0; i < leaderboard.size(); i++) {
            ScoreRecord r = leaderboard.get(i);
            displayList.add((i + 1) + ". " + r.getPlayerName()
                    + "  " + r.getScore() + "分"
                    + "  " + r.getFormattedGameTime()
                    + "  [" + r.getDifficulty() + "]");
        }
        listAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_single_choice, displayList) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.CheckedTextView view =
                        (android.widget.CheckedTextView) super.getView(position, convertView, parent);
                view.setTextColor(android.graphics.Color.WHITE);
                view.setTextSize(14f);
                view.setPadding(16, 20, 16, 20);
                return view;
            }
        };
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }
}
