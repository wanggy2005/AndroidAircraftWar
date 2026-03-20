package edu.hitsz.aircraft;

import android.graphics.Bitmap;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class SuperEliteEnemy extends AbstractAircraft {

    private final int shootNum = 3;
    private final int power = 15;
    private final int direction = 1;
    private final double spreadAngle = 40;
    private int shootCycle = 0;
    private final int shootCycleDuration = 800;

    public SuperEliteEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        locationX += speedX;
        locationY += speedY;
        Bitmap img = ImageManager.ELITE_ENEMY_IMAGE;
        int imgW = (img != null) ? img.getWidth() : 32;
        if (locationX <= 0 || locationX >= AbstractFlyingObject.WINDOW_WIDTH - imgW) {
            speedX = -speedX;
        }
        if (locationY >= AbstractFlyingObject.WINDOW_HEIGHT) vanish();
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int x = getLocationX();
        int y = getLocationY() + direction * 2;
        double[] angles = {
            -spreadAngle * Math.PI / 180,
            0,
            spreadAngle * Math.PI / 180
        };
        for (int i = 0; i < shootNum; i++) {
            int sx = (int) (Math.sin(angles[i]) * 8);
            int sy = getSpeedY() + direction * 8;
            res.add(new EnemyBullet(x, y, sx, sy, power));
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
    public Bitmap getImage() { return ImageManager.SUPERELITE_ENEMY_IMAGE; }

    @Override
    public void onBombExplode() { this.decreaseHp(50); }
}
