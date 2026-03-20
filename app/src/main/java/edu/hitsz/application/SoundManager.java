package edu.hitsz.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;

/**
 * 音效管理器 - Android 版本
 * 背景音乐使用 MediaPlayer，短音效使用 SoundPool
 */
public class SoundManager {

    private static SoundManager instance;

    private boolean soundEnabled = true;
    private Context context;

    private MediaPlayer bgmPlayer;
    private MediaPlayer bossPlayer;

    private SoundPool soundPool;
    private int soundBulletHit = -1;
    private int soundBombExp   = -1;
    private int soundGetSupply = -1;
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
    }

    private int loadRaw(String name) {
        if (context == null) return -1;
        int resId = context.getResources().getIdentifier(
                name, "raw", context.getPackageName());
        return (resId != 0 && soundPool != null) ? soundPool.load(context, resId, 1) : -1;
    }

    private MediaPlayer createMediaPlayer(String rawName) {
        if (context == null) return null;
        int resId = context.getResources().getIdentifier(
                rawName, "raw", context.getPackageName());
        if (resId == 0) return null;
        return MediaPlayer.create(context, resId);
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopAllMusic();
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void startBackgroundMusic() {
        if (!soundEnabled || context == null) return;
        stopBackgroundMusic();
        bgmPlayer = createMediaPlayer("bgm");
        if (bgmPlayer != null) { bgmPlayer.setLooping(true); bgmPlayer.start(); }
    }

    public void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            try { bgmPlayer.stop(); } catch (Exception ignored) {}
            bgmPlayer.release(); bgmPlayer = null;
        }
    }

    public void startBossMusic() {
        if (!soundEnabled || context == null) return;
        stopBossMusic();
        stopBackgroundMusic();
        bossPlayer = createMediaPlayer("bgm_boss");
        if (bossPlayer != null) { bossPlayer.setLooping(true); bossPlayer.start(); }
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
    }

    public void playBulletHitSound()    { play(soundBulletHit); }
    public void playBombExplosionSound(){ play(soundBombExp); }
    public void playGetSupplySound()    { play(soundGetSupply); }
    public void playGameOverSound()     { play(soundGameOver); }

    private void play(int soundId) {
        if (!soundEnabled || soundPool == null || soundId < 0) return;
        soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
    }

    public void release() {
        stopAllMusic();
        if (soundPool != null) { soundPool.release(); soundPool = null; }
    }
}
