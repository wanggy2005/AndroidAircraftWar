package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import edu.hitsz.aircraft.*;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.difficulty.AbstractDifficulty;
import edu.hitsz.difficulty.DifficultyFactory;
import edu.hitsz.observer.BombManager;
import edu.hitsz.prop.*;
import edu.hitsz.score.FileScoreDao;
import edu.hitsz.score.ScoreManager;

import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主视图 - 基于 SurfaceView，替代原 Java SE 的 JPanel
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // ---------- 常量 ----------
    public static final int LOGIC_WIDTH  = 512;
    public static final int LOGIC_HEIGHT = 768;

    // ---------- 渲染 ----------
    private Thread gameThread;
    private volatile boolean running = false;
    private final int timeInterval = 40; // ms per frame (~25 fps)

    // ---------- 游戏状态 ----------
    private int score = 0;
    private int time  = 0;
    private int cycleTime = 0;
    private int heroShootCycle = 0;
    private int backGroundTop = 0;
    private boolean bossSpawned = false;

    // ---------- 游戏对象 ----------
    private HeroAircraft heroAircraft;
    private final List<AbstractAircraft> enemyAircrafts = new LinkedList<>();
    private final List<BaseBullet>       heroBullets    = new LinkedList<>();
    private final List<BaseBullet>       enemyBullets   = new LinkedList<>();
    private final List<AbstractProp>     props          = new LinkedList<>();

    // ---------- 难度 / 音效 ----------
    private final AbstractDifficulty difficulty;
    private int enemyMaxNumber;
    private int cycleDuration;
    private int heroShootCycleDuration;
    private final Bitmap bgImage;
    private final BombManager bombManager;
    private final SoundManager soundManager;
    private final ScoreManager scoreManager;
    private final long gameStartTime;
    private final String difficultyName;

    // ---------- Paint ----------
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ---------- 缩放 ----------
    private float scaleX = 1f;
    private float scaleY = 1f;

    // ---------- Callback 接口 ----------
    public interface GameCallback {
        void onGameOver(int score, String difficulty, long gameTimeMs);
    }
    private final GameCallback callback;

    public GameView(Context context, String difficultyName, boolean soundEnabled, GameCallback callback) {
        super(context);
        this.difficultyName = difficultyName;
        this.callback = callback;

        // 设置逻辑坐标系尺寸
        AbstractFlyingObject.WINDOW_WIDTH  = LOGIC_WIDTH;
        AbstractFlyingObject.WINDOW_HEIGHT = LOGIC_HEIGHT;

        // 初始化图片和音效
        ImageManager.init(context);
        soundManager = SoundManager.getInstance();
        soundManager.init(context);
        soundManager.setSoundEnabled(soundEnabled);

        // 难度
        difficulty = DifficultyFactory.createDifficulty(difficultyName);
        enemyMaxNumber         = difficulty.getEnemyMaxNumber();
        cycleDuration          = difficulty.getCycleDuration();
        heroShootCycleDuration = difficulty.getHeroShootCycleDuration();
        bgImage = ImageManager.getBackgroundByDifficulty(difficultyName);

        // 英雄机
        heroAircraft = HeroAircraft.getInstance(
                LOGIC_WIDTH / 2, LOGIC_HEIGHT - 80, 0, 0, 1000);

        // 炸弹 / 分数
        bombManager  = new BombManager();
        BombProp.setBombManager(bombManager);
        scoreManager = new ScoreManager(new FileScoreDao(context));
        gameStartTime = System.currentTimeMillis();

        getHolder().addCallback(this);
        setFocusable(true);
    }

    // ======================== SurfaceHolder.Callback ========================

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
        soundManager.startBackgroundMusic();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        scaleX = (float) w / LOGIC_WIDTH;
        scaleY = (float) h / LOGIC_HEIGHT;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        try { if (gameThread != null) gameThread.join(500); } catch (InterruptedException ignored) {}
    }

    // ======================== Game Loop ========================

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0; // 渲染60 FPS
        final double gameSpeedFactor = 0.5; // 游戏速度因子，0.6表示降低为60%速度
        double delta = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick * gameSpeedFactor; // 应用速度因子
            lastTime = now;

            // 更新逻辑 - 固定时间步长
            while (delta >= 1) {
                update();
                delta--;
            }

            // 渲染
            draw();

            // 动态睡眠控制，保持流畅
            long sleepTime = (long) ((1.0 - delta / gameSpeedFactor) * (1000.0 / 60.0));
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ignored) {}
            }
        }
    }

    private void update() {
        time += timeInterval;
        updateBackground(); // 更新背景滚动
        difficulty.increaseDifficulty();
        enemyMaxNumber         = difficulty.getEnemyMaxNumber();
        cycleDuration          = difficulty.getCycleDuration();
        heroShootCycleDuration = difficulty.getHeroShootCycleDuration();

        // 新周期 - 生成敌机
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;

            if (!bossSpawned && difficulty.shouldGenerateBoss(score)) {
                AbstractAircraft boss = AircraftFactory.createBossAircraft();
                enemyAircrafts.add(boss);
                bombManager.registerObserver(boss);
                bossSpawned = true;
                difficulty.updateBossThreshold();
                soundManager.startBossMusic();
            }

            if (enemyAircrafts.size() < enemyMaxNumber && !isBossAlive()) {
                AbstractAircraft enemy = AircraftFactory.createRandomAircraft(
                        difficulty.getEliteEnemyProbability(),
                        difficulty.getEnemyAttributeMultiplier());
                enemyAircrafts.add(enemy);
                bombManager.registerObserver(enemy);
            }
        }

        shootAction();
        bulletsMoveAction();
        aircraftsMoveAction();
        propsMoveAction();
        crashCheckAction();
        postProcessAction();

        // 游戏结束判定
        if (heroAircraft.getHp() <= 0) {
            running = false;
            soundManager.stopAllMusic();
            soundManager.playGameOverSound();
            long gameTime = System.currentTimeMillis() - gameStartTime;
            scoreManager.saveGameScore(score, gameTime, "Player", difficultyName);
            HeroAircraft.resetInstance();
            if (callback != null) {
                post(() -> callback.onGameOver(score, difficultyName, gameTime));
            }
        }
    }

    // ======================== Shoot / Move / Crash ========================

    private boolean isBossAlive() {
        for (AbstractAircraft a : enemyAircrafts) {
            if (a instanceof BossEnemy && !a.notValid()) return true;
        }
        if (bossSpawned) {
            bossSpawned = false;
            soundManager.switchToBackgroundMusicAfterBoss();
        }
        return false;
    }

    private void shootAction() {
        for (AbstractAircraft enemy : enemyAircrafts) {
            List<BaseBullet> newBullets = null;
            if (enemy instanceof BossEnemy && ((BossEnemy) enemy).canShoot(timeInterval)) {
                newBullets = enemy.shoot();
            } else if (enemy instanceof SuperEliteEnemy && ((SuperEliteEnemy) enemy).canShoot(timeInterval)) {
                newBullets = enemy.shoot();
            } else if (enemy instanceof EliteEnemy && ((EliteEnemy) enemy).canShoot(timeInterval)) {
                newBullets = enemy.shoot();
            }
            if (newBullets != null) {
                enemyBullets.addAll(newBullets);
                for (BaseBullet b : newBullets) bombManager.registerObserver(b);
            }
        }
        heroShootCycle += timeInterval;
        if (heroShootCycle >= heroShootCycleDuration) {
            heroShootCycle %= heroShootCycleDuration;
            heroBullets.addAll(heroAircraft.shoot());
            soundManager.playBulletShootSound();
        }
    }

    private void bulletsMoveAction() {
        for (BaseBullet b : heroBullets)  b.forward();
        for (BaseBullet b : enemyBullets) b.forward();
    }

    private void aircraftsMoveAction() {
        for (AbstractAircraft a : enemyAircrafts) a.forward();
    }

    private void propsMoveAction() {
        for (AbstractProp p : props) p.forward();
    }

    private void crashCheckAction() {
        // 敌机子弹 -> 英雄机
        for (BaseBullet b : enemyBullets) {
            if (b.notValid()) continue;
            if (heroAircraft.crash(b)) {
                heroAircraft.decreaseHp(b.getPower());
                b.vanish();
            }
        }
        // 英雄子弹 -> 敌机
        for (BaseBullet b : heroBullets) {
            if (b.notValid()) continue;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) continue;
                if (enemy.crash(b)) {
                    enemy.decreaseHp(b.getPower());
                    b.vanish();
                    soundManager.playBulletHitSound();
                    if (enemy.notValid()) {
                        soundManager.playBombExplosionSound();
                        if (enemy instanceof BossEnemy) {
                            score += 100;
                            for (int i = 0; i < 3; i++) {
                                if (Math.random() < 0.7) generateProp(enemy.getLocationX(), enemy.getLocationY());
                            }
                        } else if (enemy instanceof SuperEliteEnemy) {
                            score += 50;
                            if (Math.random() < 0.8) generateProp(enemy.getLocationX(), enemy.getLocationY());
                        } else if (enemy instanceof EliteEnemy) {
                            score += 20;
                            if (Math.random() < 0.3) generateProp(enemy.getLocationX(), enemy.getLocationY());
                        } else {
                            score += 10;
                        }
                    }
                }
                if (enemy.crash(heroAircraft) || heroAircraft.crash(enemy)) {
                    enemy.vanish();
                    heroAircraft.decreaseHp(Integer.MAX_VALUE);
                }
            }
        }
        // 道具 -> 英雄机
        for (AbstractProp prop : props) {
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                if (prop instanceof BombProp) {
                    score += calcBombScore();
                }
                prop.activate();
                soundManager.playGetSupplySound();
                if (prop instanceof BloodProp) {
                    int healed = Math.min(heroAircraft.getHp() + ((BloodProp) prop).getHealAmount(),
                            heroAircraft.getMaxHp());
                    heroAircraft.setHp(healed);
                }
                prop.vanish();
            }
        }
    }

    private void generateProp(int x, int y) {
        props.add(PropFactory.createRandomProp(x, y));
    }

    private int calcBombScore() {
        int s = 0;
        for (AbstractAircraft a : enemyAircrafts) {
            if (a instanceof MobEnemy) s += 10;
            else if (a instanceof EliteEnemy) s += 20;
            else if (a instanceof SuperEliteEnemy) s += 50;
        }
        return s;
    }

    private void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
    }

    // ======================== Drawing ========================

    private void draw() {
        SurfaceHolder holder = getHolder();
        if (!holder.getSurface().isValid()) return;
        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;
        try {
            canvas.save();
            canvas.scale(scaleX, scaleY);
            drawBackground(canvas);
            drawObjects(canvas, enemyBullets);
            drawObjects(canvas, heroBullets);
            drawObjects(canvas, enemyAircrafts);
            drawObjects(canvas, props);
            drawHero(canvas);
            drawHUD(canvas);
            canvas.restore();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    // 背景绘制优化 - 使用双缓冲思路
    private Bitmap bgBuffer1;
    private Bitmap bgBuffer2;
    private int lastBgOffset = -1;

    private void drawBackground(Canvas canvas) {
        if (bgImage == null) {
            canvas.drawColor(Color.BLACK);
            return;
        }
        // 计算背景滚动位置
        int bgHeight = bgImage.getHeight();
        int offset = backGroundTop % bgHeight;

        // 只有当offset变化时才重新计算位置
        if (offset != lastBgOffset) {
            lastBgOffset = offset;
        }

        // 绘制两张背景图实现无缝滚动
        canvas.drawBitmap(bgImage, 0, offset - bgHeight, null);
        canvas.drawBitmap(bgImage, 0, offset, null);
    }

    /**
     * 更新背景滚动位置，在update中调用保证流畅性
     */
    private void updateBackground() {
        backGroundTop += 2; // 每帧滚动2像素
    }

    /** 所有游戏精灵统一缩放比例（相对于原图）*/
    private static final float SPRITE_SCALE = 0.45f;  // 飞机贴图缩放比例（稍微调小）

    private void drawObjects(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (AbstractFlyingObject obj : objects) {
            Bitmap bmp = obj.getImage();
            if (bmp == null) continue;
            drawScaled(canvas, bmp, obj.getLocationX(), obj.getLocationY());
        }
    }

    private void drawHero(Canvas canvas) {
        Bitmap bmp = ImageManager.HERO_IMAGE;
        if (bmp == null) return;
        drawScaled(canvas, bmp, heroAircraft.getLocationX(), heroAircraft.getLocationY());
    }

    /** 以逻辑坐标 (cx, cy) 为中心，按 SPRITE_SCALE 缩放绘制 Bitmap */
    private void drawScaled(Canvas canvas, Bitmap bmp, float cx, float cy) {
        float w = bmp.getWidth()  * SPRITE_SCALE;
        float h = bmp.getHeight() * SPRITE_SCALE;
        android.graphics.RectF dst = new android.graphics.RectF(
                cx - w / 2f, cy - h / 2f, cx + w / 2f, cy + h / 2f);
        canvas.drawBitmap(bmp, null, dst, paint);
    }

    private void drawHUD(Canvas canvas) {
        paint.setColor(Color.RED);
        paint.setTextSize(28);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("SCORE: " + score, 10, 35, paint);
        canvas.drawText("LIFE:  " + heroAircraft.getHp(), 10, 65, paint);
    }

    // ======================== Touch ========================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float lx = event.getX() / scaleX;
        float ly = event.getY() / scaleY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                heroAircraft.setLocation(lx, ly);
                break;
        }
        return true;
    }
}
