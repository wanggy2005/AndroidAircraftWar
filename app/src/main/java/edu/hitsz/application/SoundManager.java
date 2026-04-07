package edu.hitsz.application;

import android.content.Context;
import android.util.Log;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * 音效管理器 - Android 版本
 * 背景音乐使用 MediaPlayer，短音效使用 SoundPool
 */
public class SoundManager {
    private static final String TAG = "SoundManager";

    private static SoundManager instance;

    private boolean soundEnabled = true;
    private Context context;

    private MediaPlayer bgmPlayer;
    private MediaPlayer bossPlayer;
    private boolean bossMusicActive = false;
    private boolean musicPausedByLifecycle = false;

    private SoundPool soundPool;
    private int soundBulletHit = -1;
    private int soundBombExp   = -1;
    private int soundGetSupply = -1;
    private int soundBulletShoot = -1;
    private int soundGameOver  = -1;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void init(Context ctx) {
        this.context = ctx.getApplicationContext();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setMaxStreams(6)
                .setAudioAttributes(attrs)
                .build();
        soundBulletHit = loadRaw("bullet_hit");
        soundBombExp   = loadRaw("bomb_explosion");
        soundGetSupply = loadRaw("get_supply");
        soundGameOver  = loadRaw("game_over");
        soundBulletShoot = loadRaw("bullet");
    }

    private int loadRaw(String name) {
        if (context == null) return -1;
        int resId = context.getResources().getIdentifier(
                name, "raw", context.getPackageName());
        if (resId == 0) {
            Log.e(TAG, "音频资源不存在: raw/" + name);
            return -1;
        }
        return (resId != 0 && soundPool != null) ? soundPool.load(context, resId, 1) : -1;
    }

    private MediaPlayer createMediaPlayer(String rawName) {
        if (context == null) return null;
        int resId = context.getResources().getIdentifier(
                rawName, "raw", context.getPackageName());
        if (resId == 0) {
            Log.e(TAG, "MediaPlayer 资源不存在: raw/" + rawName);
            return null;
        }
        MediaPlayer player = MediaPlayer.create(context, resId);
        if (player == null) {
            Log.e(TAG, "MediaPlayer.create 返回 null: raw/" + rawName);
        }
        return player;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopAllMusic();
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void startBackgroundMusic() {
        if (!soundEnabled || context == null) return;
        // 切回常规 BGM 时，明确清除 Boss 状态，避免恢复逻辑误判。
        bossMusicActive = false;
        stopBossMusic();
        stopBackgroundMusic();
        bgmPlayer = createMediaPlayer("bgm");
        if (bgmPlayer != null) {
            bgmPlayer.setLooping(true);
            bgmPlayer.start();
        } else {
            Log.e(TAG, "无法播放普通BGM：raw/bgm 未找到或创建失败");
        }
    }

    public void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            try { bgmPlayer.stop(); } catch (Exception ignored) {}
            bgmPlayer.release(); bgmPlayer = null;
        }
    }

    public void startBossMusic() {
        if (!soundEnabled || context == null) return;
        // Boss 战期间只允许 Boss BGM 运行。
        bossMusicActive = true;
        stopBossMusic();
        stopBackgroundMusic();
        bossPlayer = createMediaPlayer("bgm_boss");
        if (bossPlayer != null) {
            bossPlayer.setLooping(true);
            bossPlayer.start();
        } else {
            Log.e(TAG, "无法播放Boss BGM：raw/bgm_boss 未找到或创建失败");
        }
    }

    public void stopBossMusic() {
        if (bossPlayer != null) {
            try { bossPlayer.stop(); } catch (Exception ignored) {}
            bossPlayer.release(); bossPlayer = null;
        }
    }

    public void stopAllMusic() {
        stopBackgroundMusic();
        stopBossMusic();
        musicPausedByLifecycle = false;
    }

    public void switchToBackgroundMusicAfterBoss() {
        // Boss 结束后强制回切普通 BGM，避免出现 BGM 长时间静音。
        bossMusicActive = false;
        stopBossMusic();
        startBackgroundMusic();
    }

    public void pauseAllMusic() {
        if (!soundEnabled) return;
        boolean paused = false;
        if (bgmPlayer != null && bgmPlayer.isPlaying()) {
            bgmPlayer.pause();
            paused = true;
        }
        if (bossPlayer != null && bossPlayer.isPlaying()) {
            bossPlayer.pause();
            paused = true;
        }
        musicPausedByLifecycle = paused;
    }

    public void resumeMusicIfNeeded() {
        if (!soundEnabled || !musicPausedByLifecycle) return;
        if (bossMusicActive) {
            if (bossPlayer != null) {
                bossPlayer.start();
            } else {
                startBossMusic();
            }
        } else {
            if (bgmPlayer != null) {
                bgmPlayer.start();
            } else {
                startBackgroundMusic();
            }
        }
        musicPausedByLifecycle = false;
    }

    public void playBulletHitSound()    { play(soundBulletHit); }
    public void playBombExplosionSound(){ play(soundBombExp); }
    public void playGetSupplySound()    { play(soundGetSupply); }
    public void playGameOverSound()     { play(soundGameOver); }
    public void playBulletShootSound()  { play(soundBulletShoot);}

    private void play(int soundId) {
        if (!soundEnabled || soundPool == null || soundId < 0) return;
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
    }

    public void release() {
        stopAllMusic();
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
