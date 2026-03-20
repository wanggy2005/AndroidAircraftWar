package edu.hitsz.aircraft;

import android.graphics.Bitmap;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.EnemyBullet;

import java.util.LinkedList;
import java.util.List;

public class BossEnemy extends AbstractAircraft {

    private final int shootNum = 20;
    private final int power = 10;
    private final int direction = 1;
    private final int bulletSpeed = 5;
    private int shootCycle = 0;
    private final int shootCycleDuration = 800;

    public BossEnemy(int locationX, int locationY, int speedX, int speedY, int hp) {
        super(locationX, locationY, speedX, speedY, hp);
    }

    @Override
    public void forward() {
        locationX += speedX;
        locationY += speedY;
        Bitmap img = ImageManager.BOSS_ENEMY_IMAGE;
        int imgW = (img != null) ? img.getWidth() : 64;
        if (locationX <= 0 || locationX >= AbstractFlyingObject.WINDOW_WIDTH - imgW / 2) {
            speedX = -speedX;
        }
        if (locationY > AbstractFlyingObject.WINDOW_HEIGHT * 0.3) {
            speedY = -Math.abs(speedY);
        }
        if (hp <= 0) vanish();
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int centerX = getLocationX();
        Bitmap img = ImageManager.BOSS_ENEMY_IMAGE;
        int imgH = (img != null) ? img.getHeight() : 64;
        int centerY = getLocationY() + imgH / 2 + 20;
        double angleStep = 2 * Math.PI / shootNum;
        for (int i = 0; i < shootNum; i++) {
            double angle = i * angleStep;
            int sx = (int) (Math.cos(angle) * bulletSpeed);
            int sy = (int) (Math.sin(angle) * bulletSpeed) + direction * 2;
            res.add(new EnemyBullet(centerX, centerY, sx, sy, power));
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
    public Bitmap getImage() { return ImageManager.BOSS_ENEMY_IMAGE; }

    @Override
    public void onBombExplode() { /* Boss不受炸弹影响 */ }
}
