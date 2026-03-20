package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;

import java.util.LinkedList;
import java.util.List;

public class MobEnemy extends AbstractAircraft {

    public MobEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= AbstractFlyingObject.WINDOW_HEIGHT) vanish();
    }

    @Override
    public List<BaseBullet> shoot() { return new LinkedList<>(); }

    @Override
    public void onBombExplode() { this.vanish(); }
}
