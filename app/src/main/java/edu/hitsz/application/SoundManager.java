package edu.hitsz.application;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
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
    private boolean bossMusicActive = false;
    private boolean musicPausedByLifecycle = false;

    private SoundPool soundPool;
    private int soundBulletHit = -1;
    private int soundBombExp   = -1;
    private int soundGetSupply = -1;
    private int soundBulletShoot = -1;
    private int soundGameOver  = -1;

    private boolean initialized = false;
    private AudioManager audioManager;

    /** 音效音量（相对于背景音乐较小） */
    private static final float SFX_VOLUME = 0.1f;

    private SoundManager() {}

    public static SoundManager getInstance() {
        if (instance == null) instance = new SoundManager();
        return instance;
    }

    public void init(Context ctx) {
        if (initialized) return;
        this.context = ctx.getApplicationContext();
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

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
        initialized = true;
    }

    private int loadRaw(String name) {
        if (context == null) return -1;
        int resId = context.getResources().getIdentifier(name, "raw", context.getPackageName());
        if (resId == 0) return -1;
        return soundPool != null ? soundPool.load(context, resId, 1) : -1;
    }

    private MediaPlayer createMediaPlayer(String rawName) {
        if (context == null) return null;
        int resId = context.getResources().getIdentifier(rawName, "raw", context.getPackageName());
        if (resId == 0) return null;

        try {
            if (audioManager != null) {
                audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            }
            MediaPlayer player = MediaPlayer.create(context, resId);
            if (player == null) {
                player = createMediaPlayerManual(resId);
            }
            return player;
        } catch (Exception e) {
            return null;
        }
    }

    private MediaPlayer createMediaPlayerManual(int resId) {
        try {
            MediaPlayer player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            android.content.res.AssetFileDescriptor afd = context.getResources().openRawResourceFd(resId);
            if (afd != null) {
                player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                afd.close();
                player.prepare();
                return player;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void createAndPlayBackgroundMusicAsync(final String rawName, final boolean isBoss) {
        new Thread(() -> {
            MediaPlayer player = createMediaPlayer(rawName);
            if (player != null) {
                player.setLooping(true);
                player.setVolume(1f, 1f);
                if (isBoss) {
                    bossPlayer = player;
                } else {
                    bgmPlayer = player;
                }
                try {
                    player.start();
                } catch (IllegalStateException ignored) {}
            }
        }, "BGM-Thread").start();
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopAllMusic();
    }

    public boolean isSoundEnabled() { return soundEnabled; }

    public void startBackgroundMusic() {
        if (!soundEnabled || context == null) return;
        bossMusicActive = false;
        stopBossMusic();
        stopBackgroundMusic();
        createAndPlayBackgroundMusicAsync("bgm", false);
    }

    public void stopBackgroundMusic() {
        if (bgmPlayer != null) {
            try { bgmPlayer.stop(); } catch (Exception ignored) {}
            bgmPlayer.release();
            bgmPlayer = null;
        }
    }

    public void startBossMusic() {
        if (!soundEnabled || context == null) return;
        bossMusicActive = true;
        stopBossMusic();
        stopBackgroundMusic();
        createAndPlayBackgroundMusicAsync("bgm_boss", true);
    }

    public void stopBossMusic() {
        if (bossPlayer != null) {
            try { bossPlayer.stop(); } catch (Exception ignored) {}
            bossPlayer.release();
            bossPlayer = null;
        }
    }

    public void stopAllMusic() {
        stopBackgroundMusic();
        stopBossMusic();
        musicPausedByLifecycle = false;
    }

    public void switchToBackgroundMusicAfterBoss() {
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
    public void playBulletShootSound()  { play(soundBulletShoot); }

    private void play(int soundId) {
        if (!soundEnabled || soundPool == null || soundId < 0) return;
        soundPool.play(soundId, SFX_VOLUME, SFX_VOLUME, 1, 0, 1f);
    }

    public void release() {
        stopAllMusic();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        if (audioManager != null) {
            audioManager.abandonAudioFocus(null);
        }
        initialized = false;
        context = null;
        audioManager = null;
        soundEnabled = true;
    }
}
