package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.observer.BombObserver;

import java.util.List;

/**
 * 所有飞机的抽象父类
 */
public abstract class AbstractAircraft extends AbstractFlyingObject implements BombObserver {

    protected int maxHp;
    protected int hp;

    public AbstractAircraft(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY);
        this.hp = hp;
        this.maxHp = hp;
    }

    public void decreaseHp(int decrease) {
        hp -= decrease;
        if (hp <= 0) {
            hp = 0;
            vanish();
        }
    }

    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public void setHp(int hp) { this.hp = hp; }

    public abstract List<BaseBullet> shoot();

    @Override
    public void onBombExplode() {}
}
