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
import edu.hitsz.network.GameSyncManager;
import edu.hitsz.network.OnlineGameData;
import edu.hitsz.observer.BombManager;
import edu.hitsz.prop.*;

import java.util.LinkedList;
import java.util.List;

/**
 * 游戏主视图 - 基于 SurfaceView，支持单机和联机模式
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
    private final long gameStartTime;
    private final String difficultyName;

    // ---------- 联机模式 ----------
    private final boolean isOnline;
    private final GameSyncManager syncManager;
    private float opponentX = 256, opponentY = 100;
    private int opponentScore = 0, opponentHp = 1000;
    private boolean opponentAlive = true;
    private boolean myAlive = true;
    private long myDeathTime = 0;
    private long opponentDeathTime = 0;
    // 快捷消息显示
    private String quickMsgDisplay = null;
    private long quickMsgDisplayTime = 0;
    private static final long QUICK_MSG_DURATION = 2000; // 2秒
    // 快捷消息按钮区域
    private boolean showQuickMsgPanel = false;
    private static final String[] QUICK_MESSAGES = {"加油", "厉害", "再来", "小心", "666"};
    private static final float QM_BTN_SIZE = 60;
    private static final float QM_PANEL_X = 440;
    private static final float QM_TOGGLE_Y = 50;
    private static final float QM_TOGGLE_W = 60;
    private static final float QM_TOGGLE_H = 44;

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
        this(context, difficultyName, soundEnabled, callback, false, null);
    }

    public GameView(Context context, String difficultyName, boolean soundEnabled,
                    GameCallback callback, boolean isOnline, GameSyncManager syncManager) {
        super(context);
        this.difficultyName = difficultyName;
        this.callback = callback;
        this.isOnline = isOnline;
        this.syncManager = syncManager;

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
        if (isOnline && syncManager != null) {
            syncManager.start();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        scaleX = (float) w / LOGIC_WIDTH;
        scaleY = (float) h / LOGIC_HEIGHT;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        running = false;
        if (isOnline && syncManager != null) {
            syncManager.stop();
        }
        try { if (gameThread != null) gameThread.join(500); } catch (InterruptedException ignored) {}
    }

    // ======================== Game Loop ========================

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final double nsPerTick = 1_000_000_000.0 / 60.0; // 渲染60 FPS
        final double gameSpeedFactor = 0.5;
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

        if (isOnline && !myAlive) {
            // 玩家死亡，冻结游戏逻辑，只保留联机同步
        } else {
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
        }

        // 游戏结束判定
        if (heroAircraft.getHp() <= 0 && myAlive) {
            myAlive = false;
            myDeathTime = System.currentTimeMillis() - gameStartTime;

            if (!isOnline) {
                // 单机模式：直接结束
                running = false;
                soundManager.stopAllMusic();
                soundManager.playGameOverSound();
                HeroAircraft.resetInstance();
                if (callback != null) {
                    post(() -> callback.onGameOver(score, difficultyName, myDeathTime));
                }
            } else {
                // 联机模式：通知服务器本方死亡，等待对方也死亡
                soundManager.playGameOverSound();
            }
        }

        // 联机模式：同步状态并检测双方死亡
        if (isOnline && syncManager != null) {
            syncManager.updateMyState(score, heroAircraft.getHp(),
                    heroAircraft.getLocationX(), heroAircraft.getLocationY(), myAlive);

            OnlineGameData opData = syncManager.getOpponentState();
            opponentX = opData.getX();
            opponentY = opData.getY();
            opponentScore = opData.getScore();
            opponentHp = opData.getHp();
            opponentAlive = opData.isAlive();

            // 检测对方死亡时间
            if (!opponentAlive && opponentDeathTime == 0) {
                opponentDeathTime = System.currentTimeMillis() - gameStartTime;
            }

            // 获取快捷消息
            java.util.List<String> msgs = syncManager.pollReceivedMessages();
            if (!msgs.isEmpty()) {
                quickMsgDisplay = msgs.get(msgs.size() - 1);
                quickMsgDisplayTime = System.currentTimeMillis();
            }

            // 双方都死亡，游戏结束
            if (!myAlive && !opponentAlive) {
                running = false;
                soundManager.stopAllMusic();
                HeroAircraft.resetInstance();
                if (callback instanceof GameActivity) {
                    GameActivity activity = (GameActivity) callback;
                    post(() -> activity.onOnlineGameOver(
                            score, opponentScore, difficultyName,
                            myDeathTime, opponentDeathTime));
                }
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
        if (heroShootCycle >= heroShootCycleDuration && myAlive) {
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
        // 英雄子弹 -> 敌机（死亡后不再计分）
        for (BaseBullet b : heroBullets) {
            if (b.notValid()) continue;
            for (AbstractAircraft enemy : enemyAircrafts) {
                if (enemy.notValid()) continue;
                if (enemy.crash(b)) {
                    enemy.decreaseHp(b.getPower());
                    b.vanish();
                    if (myAlive) {
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
                }
                if (myAlive && (enemy.crash(heroAircraft) || heroAircraft.crash(enemy))) {
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
            if (isOnline) drawOpponent(canvas);
            drawHero(canvas);
            drawHUD(canvas);
            if (isOnline) {
                drawOnlineHUD(canvas);
                drawQuickMessage(canvas);
                drawQuickMsgToggle(canvas);
                if (showQuickMsgPanel) drawQuickMsgPanel(canvas);
                if (!myAlive && opponentAlive) {
                    drawDeathOverlay(canvas);
                }
            }
            canvas.restore();
        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        if (bgImage == null) {
            canvas.drawColor(Color.BLACK);
            return;
        }
        int bgHeight = bgImage.getHeight();
        int offset = backGroundTop % bgHeight;
        canvas.drawBitmap(bgImage, 0, offset - bgHeight, null);
        canvas.drawBitmap(bgImage, 0, offset, null);
    }

    private void updateBackground() {
        backGroundTop += 2;
    }

    private static final float SPRITE_SCALE = 0.45f;

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
        if (isOnline) {
            // 联机模式：左侧显示自己信息
            canvas.drawText("ME: " + score, 10, 35, paint);
            canvas.drawText("HP: " + heroAircraft.getHp(), 10, 65, paint);
        } else {
            canvas.drawText("SCORE: " + score, 10, 35, paint);
            canvas.drawText("LIFE:  " + heroAircraft.getHp(), 10, 65, paint);
        }
    }

    /**
     * 绘制对手飞机（蓝色色调，半透明）
     */
    private void drawOpponent(Canvas canvas) {
        Bitmap bmp = ImageManager.HERO_IMAGE;
        if (bmp == null || !opponentAlive) return;
        Paint opPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        opPaint.setAlpha(160); // 半透明
        opPaint.setColorFilter(new android.graphics.PorterDuffColorFilter(
                0xFF4488FF, android.graphics.PorterDuff.Mode.MULTIPLY));
        float w = bmp.getWidth() * SPRITE_SCALE;
        float h = bmp.getHeight() * SPRITE_SCALE;
        android.graphics.RectF dst = new android.graphics.RectF(
                opponentX - w / 2f, opponentY - h / 2f,
                opponentX + w / 2f, opponentY + h / 2f);
        canvas.drawBitmap(bmp, null, dst, opPaint);
        paint.setColor(0xFF4488FF);
        paint.setTextSize(16);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("对手", opponentX, opponentY - h / 2f - 8, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * 绘制联机 HUD（右侧显示对手信息）
     */
    private void drawOnlineHUD(Canvas canvas) {
        paint.setColor(0xFF4488FF);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("VS: " + opponentScore, LOGIC_WIDTH - 10, 35, paint);

        paint.setTextSize(24);
        paint.setColor(opponentAlive ? 0xFF4488FF : 0xFFFF4444);
        canvas.drawText(opponentAlive ? "HP: " + opponentHp : "已阵亡", LOGIC_WIDTH - 10, 62, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * 绘制死亡后等待对手的遮罩提示
     */
    private void drawDeathOverlay(Canvas canvas) {
        paint.setColor(0xAA000000);
        canvas.drawRect(0, 0, LOGIC_WIDTH, LOGIC_HEIGHT, paint);

        paint.setColor(0xFFFF6B6B);
        paint.setTextSize(42);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("游戏结束", LOGIC_WIDTH / 2f, LOGIC_HEIGHT / 2f - 40, paint);

        paint.setColor(0xCCFFFFFF);
        paint.setTextSize(24);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText("请等待对手结束游戏...", LOGIC_WIDTH / 2f, LOGIC_HEIGHT / 2f + 10, paint);

        paint.setColor(0xAAFFFFFF);
        paint.setTextSize(20);
        canvas.drawText("我的得分: " + score, LOGIC_WIDTH / 2f, LOGIC_HEIGHT / 2f + 55, paint);

        long dots = (System.currentTimeMillis() / 500) % 4;
        String dotStr = "";
        for (int i = 0; i < dots; i++) dotStr += " ●";
        paint.setColor(0xFF4488FF);
        paint.setTextSize(18);
        canvas.drawText(dotStr, LOGIC_WIDTH / 2f, LOGIC_HEIGHT / 2f + 90, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
    }

    /**
     * 绘制对手发来的快捷消息气泡
     */
    private void drawQuickMessage(Canvas canvas) {
        if (quickMsgDisplay == null) return;
        long elapsed = System.currentTimeMillis() - quickMsgDisplayTime;
        if (elapsed > QUICK_MSG_DURATION) {
            quickMsgDisplay = null;
            return;
        }
        float alpha = 1f - (float) elapsed / QUICK_MSG_DURATION;
        paint.setColor(0xFF4488FF);
        paint.setAlpha((int) (200 * alpha));
        float textWidth = paint.measureText(quickMsgDisplay);
        float bx = (LOGIC_WIDTH - textWidth) / 2f - 12;
        float by = 120;
        canvas.drawRoundRect(bx, by, bx + textWidth + 24, by + 40, 10, 10, paint);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (255 * alpha));
        paint.setTextSize(22);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(quickMsgDisplay, LOGIC_WIDTH / 2f, by + 28, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAlpha(255);
    }

    /**
     * 绘制快捷消息切换按钮
     */
    private void drawQuickMsgToggle(Canvas canvas) {
        paint.setColor(showQuickMsgPanel ? 0xCC4488FF : 0x884488FF);
        canvas.drawRoundRect(QM_PANEL_X, QM_TOGGLE_Y, QM_PANEL_X + QM_TOGGLE_W, QM_TOGGLE_Y + QM_TOGGLE_H, 10, 10, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("💬", QM_PANEL_X + QM_TOGGLE_W / 2f, QM_TOGGLE_Y + QM_TOGGLE_H / 2f + 7, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * 绘制快捷消息选择面板
     */
    private void drawQuickMsgPanel(Canvas canvas) {
        float startY = QM_TOGGLE_Y + 40;
        for (int i = 0; i < QUICK_MESSAGES.length; i++) {
            float y = startY + i * (QM_BTN_SIZE + 4);
            paint.setColor(0xCC1A1A2E);
            canvas.drawRoundRect(QM_PANEL_X - 10, y, LOGIC_WIDTH - 6, y + QM_BTN_SIZE - 4, 8, 8, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(18);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(QUICK_MESSAGES[i], (QM_PANEL_X - 10 + LOGIC_WIDTH - 6) / 2f, y + 35, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }

    // ======================== Touch ========================

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float lx = event.getX() / scaleX;
        float ly = event.getY() / scaleY;

        if (isOnline && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (lx >= QM_PANEL_X && lx <= QM_PANEL_X + QM_TOGGLE_W &&
                    ly >= QM_TOGGLE_Y && ly <= QM_TOGGLE_Y + QM_TOGGLE_H) {
                showQuickMsgPanel = !showQuickMsgPanel;
                return true;
            }
            if (showQuickMsgPanel) {
                float startY = QM_TOGGLE_Y + 40;
                for (int i = 0; i < QUICK_MESSAGES.length; i++) {
                    float y = startY + i * (QM_BTN_SIZE + 4);
                    if (lx >= QM_PANEL_X - 10 && lx <= LOGIC_WIDTH - 6 &&
                            ly >= y && ly <= y + QM_BTN_SIZE - 4) {
                        if (syncManager != null) {
                            syncManager.sendQuickMessage(QUICK_MESSAGES[i]);
                        }
                        showQuickMsgPanel = false;
                        return true;
                    }
                }
                showQuickMsgPanel = false;
                return true;
            }
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (myAlive) {
                    heroAircraft.setLocation(lx, ly);
                }
                break;
        }
        return true;
    }
}