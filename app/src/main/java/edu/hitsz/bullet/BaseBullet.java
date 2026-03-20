package edu.hitsz.bullet;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.observer.BombObserver;

public abstract class BaseBullet extends AbstractFlyingObject implements BombObserver {

    private final int power;

    public BaseBullet(int locationX, int locationY, int speedX, int speedY, int power) {
        super(locationX, locationY, speedX, speedY);
        this.power = power;
    }

    @Override
    public void forward() {
        super.forward();
        if (locationX <= 0 || locationX >= WINDOW_WIDTH) vanish();
        if (speedY > 0 && locationY >= WINDOW_HEIGHT) vanish();
        else if (locationY <= 0) vanish();
    }

    public int getPower() { return power; }

    @Override
    public void onBombExplode() {}
}
