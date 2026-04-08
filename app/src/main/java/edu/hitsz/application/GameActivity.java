package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 游戏运行 Activity（承载 GameView SurfaceView）
 */
public class GameActivity extends AppCompatActivity implements GameView.GameCallback {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND      = "sound";

    private static final int MSG_GAME_OVER = 1001;

    private final Handler uiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_GAME_OVER) {
                Bundle data = msg.getData();
                Intent intent = new Intent(GameActivity.this, GameEndActivity.class);
                intent.putExtra(GameEndActivity.EXTRA_SCORE,
                        data.getInt(GameEndActivity.EXTRA_SCORE, 0));
                intent.putExtra(GameEndActivity.EXTRA_DIFFICULTY,
                        data.getString(GameEndActivity.EXTRA_DIFFICULTY, "普通模式"));
                intent.putExtra(GameEndActivity.EXTRA_GAME_TIME,
                        data.getLong(GameEndActivity.EXTRA_GAME_TIME, 0L));
                startActivity(intent);
                finish();
            }
        }
    };

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
        Message message = uiHandler.obtainMessage(MSG_GAME_OVER);
        Bundle bundle = new Bundle();
        bundle.putInt(GameEndActivity.EXTRA_SCORE, score);
        bundle.putString(GameEndActivity.EXTRA_DIFFICULTY, difficulty);
        bundle.putLong(GameEndActivity.EXTRA_GAME_TIME, gameTimeMs);
        message.setData(bundle);
        uiHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
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
