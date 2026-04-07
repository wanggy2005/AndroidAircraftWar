package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

/**
 * 游戏运行 Activity（承载 GameView SurfaceView）
 */
public class GameActivity extends AppCompatActivity implements GameView.GameCallback {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND      = "sound";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String difficulty = getIntent().getStringExtra(EXTRA_DIFFICULTY);
        if (difficulty == null) difficulty = "普通模式";
        boolean soundOn = getIntent().getBooleanExtra(EXTRA_SOUND, true);

        GameView gameView = new GameView(this, difficulty, soundOn, this);
        setContentView(gameView);
    }

    @Override
    public void onGameOver(int score, String difficulty, long gameTimeMs) {
        Intent intent = new Intent(this, GameEndActivity.class);
        intent.putExtra(GameEndActivity.EXTRA_SCORE,      score);
        intent.putExtra(GameEndActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameEndActivity.EXTRA_GAME_TIME,  gameTimeMs);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundManager.getInstance().release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance().pauseAllMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance().resumeMusicIfNeeded();
    }
}
