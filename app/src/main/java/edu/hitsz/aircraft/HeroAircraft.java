package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 英雄飞机 - 单例模式 + 策略模式
 */
public class HeroAircraft extends AbstractAircraft {

    private ShootStrategy shootStrategy;
    private final ShootStrategy defaultShootStrategy;
    private Timer strategyTimer;

    private static HeroAircraft instance = null;

    private HeroAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
        this.defaultShootStrategy = new StraightShootStrategy();
        this.shootStrategy = defaultShootStrategy;
        this.strategyTimer = new Timer();
    }

    public static synchronized HeroAircraft getInstance(
            int locationX, int locationY, int speedX, int speedY, int hp) {
        if (instance == null) {
            instance = new HeroAircraft(locationX, locationY, speedX, speedY, hp);
        }
        return instance;
    }

    public static void resetInstance() {
        if (instance != null && instance.strategyTimer != null) {
            instance.strategyTimer.cancel();
        }
        instance = null;
    }

    @Override
    public void forward() {
        // 由触摸事件控制，不自动移动
    }

    @Override
    public List<BaseBullet> shoot() {
        return shootStrategy.shoot(this);
    }

    public ShootStrategy getShootStrategy() { return shootStrategy; }

    public void setTemporaryShootStrategy(ShootStrategy strategy, long durationMs) {
        strategyTimer.cancel();
        strategyTimer = new Timer();
        this.shootStrategy = strategy;
        strategyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                shootStrategy = defaultShootStrategy;
            }
        }, durationMs);
    }
}
