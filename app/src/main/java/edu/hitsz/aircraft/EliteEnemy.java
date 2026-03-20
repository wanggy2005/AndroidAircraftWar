package edu.hitsz.aircraft;

import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class EliteEnemy extends AbstractAircraft {

    private final int shootNum = 1;
    private final int power = 20;
    private final int direction = 1;
    private int shootCycle = 0;
    private final int shootCycleDuration = 800;

    public EliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        super.forward();
        if (locationY >= AbstractFlyingObject.WINDOW_HEIGHT) vanish();
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = getLocationX();
        int y = getLocationY() + direction * 2;
        int sx = 0;
        int sy = getSpeedY() + direction * 5;
        for (int i = 0; i < shootNum; i++) {
            res.add(new EnemyBullet(x + (i * 2 - shootNum + 1) * 10, y, sx, sy, power));
        }
        return res;
    }

    public boolean canShoot(int timeInterval) {
        shootCycle += timeInterval;
        if (shootCycle >= shootCycleDuration) {
            shootCycle %= shootCycleDuration;
            return true;
        }
        return false;
    }

    @Override
    public void onBombExplode() { this.vanish(); }
}
