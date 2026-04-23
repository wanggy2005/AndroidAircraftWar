package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.network.ApiClient;
import edu.hitsz.network.GameSyncManager;

/**
 * 游戏运行 Activity（承载 GameView SurfaceView）
 */
public class GameActivity extends AppCompatActivity implements GameView.GameCallback {

    public static final String EXTRA_DIFFICULTY = "difficulty";
    public static final String EXTRA_SOUND      = "sound";
    public static final String EXTRA_ONLINE     = "online";
    public static final String EXTRA_ROOM_ID    = "roomId";
    public static final String EXTRA_PLAYER_ID  = "playerId";
    public static final String EXTRA_SERVER_URL = "serverUrl";

    private static final int MSG_GAME_OVER = 1001;
    private static final int MSG_ONLINE_GAME_OVER = 1002;

    private boolean isOnline = false;
    private GameSyncManager syncManager;

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
            } else if (msg.what == MSG_ONLINE_GAME_OVER) {
                Bundle data = msg.getData();
                Intent intent = new Intent(GameActivity.this, OnlineGameEndActivity.class);
                intent.putExtra(OnlineGameEndActivity.EXTRA_MY_SCORE,
                        data.getInt("myScore", 0));
                intent.putExtra(OnlineGameEndActivity.EXTRA_OPPONENT_SCORE,
                        data.getInt("opponentScore", 0));
                intent.putExtra(OnlineGameEndActivity.EXTRA_DIFFICULTY,
                        data.getString("difficulty", "普通模式"));
                intent.putExtra(OnlineGameEndActivity.EXTRA_MY_TIME,
                        data.getLong("myTime", 0L));
                intent.putExtra(OnlineGameEndActivity.EXTRA_OPPONENT_TIME,
                        data.getLong("opponentTime", 0L));
                intent.putExtra("roomId", data.getString("roomId"));
                intent.putExtra("playerId", data.getString("playerId"));
                intent.putExtra("serverUrl", data.getString("serverUrl"));
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
        isOnline = getIntent().getBooleanExtra(EXTRA_ONLINE, false);

        GameView gameView;
        if (isOnline) {
            String roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
            String playerId = getIntent().getStringExtra(EXTRA_PLAYER_ID);
            String serverUrl = getIntent().getStringExtra(EXTRA_SERVER_URL);
            if (serverUrl != null) ApiClient.setBaseUrl(serverUrl);
            syncManager = new GameSyncManager(playerId, roomId);
            gameView = new GameView(this, difficulty, soundOn, this, true, syncManager);
        } else {
            gameView = new GameView(this, difficulty, soundOn, this);
        }
        setContentView(gameView);
    }

    @Override
    public void onGameOver(int score, String difficulty, long gameTimeMs) {
        if (isOnline) {
            // 联机模式：等待双方都死亡后跳转结算页面
            // GameView 会调用 onOnlineGameOver
        } else {
            Message message = uiHandler.obtainMessage(MSG_GAME_OVER);
            Bundle bundle = new Bundle();
            bundle.putInt(GameEndActivity.EXTRA_SCORE, score);
            bundle.putString(GameEndActivity.EXTRA_DIFFICULTY, difficulty);
            bundle.putLong(GameEndActivity.EXTRA_GAME_TIME, gameTimeMs);
            message.setData(bundle);
            uiHandler.sendMessage(message);
        }
    }

    public void onOnlineGameOver(int myScore, int opponentScore, String difficulty,
                                  long myTime, long opponentTime) {
        Message message = uiHandler.obtainMessage(MSG_ONLINE_GAME_OVER);
        Bundle bundle = new Bundle();
        bundle.putInt("myScore", myScore);
        bundle.putInt("opponentScore", opponentScore);
        bundle.putString("difficulty", difficulty);
        bundle.putLong("myTime", myTime);
        bundle.putLong("opponentTime", opponentTime);
        // 传递房间信息，支持回到房间
        bundle.putString("roomId", getIntent().getStringExtra(EXTRA_ROOM_ID));
        bundle.putString("playerId", getIntent().getStringExtra(EXTRA_PLAYER_ID));
        bundle.putString("serverUrl", getIntent().getStringExtra(EXTRA_SERVER_URL));
        message.setData(bundle);
        uiHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHandler.removeCallbacksAndMessages(null);
        if (syncManager != null) {
            syncManager.stop();
        }
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
