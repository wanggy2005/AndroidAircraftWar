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
        int imgW = (img != null) ? img.getWidth() : 200;
        int imgH = (img != null) ? img.getHeight() : 150;
        // 考虑绘制时的缩放比例0.45
        int displayW = (int) (imgW * 0.45f);
        int displayH = (int) (imgH * 0.45f);
        int halfDisplayW = displayW / 2;

        // 水平边界检测 - 基于显示宽度的中心点边界
        int minX = halfDisplayW;
        int maxX = AbstractFlyingObject.WINDOW_WIDTH - halfDisplayW;
        if (locationX <= minX || locationX >= maxX) {
            speedX = -speedX;
            locationX = Math.max(minX, Math.min(locationX, maxX));
        }

        // 垂直边界检测 - Boss只在屏幕最上方小范围移动
        // 初始位置是 displayH/2 + 10，在此基础上上下移动30像素
        int baseY = displayH / 2 + 10;
        if (locationY > baseY + 40) {
            speedY = -Math.abs(speedY);
        } else if (locationY < baseY - 20) {
            speedY = Math.abs(speedY);
        }

        if (hp <= 0) vanish();
    }

    @Override
    public List<BaseBullet> shoot() {
        List<BaseBullet> res = new LinkedList<>();
        int centerX = getLocationX();
        Bitmap img = ImageManager.BOSS_ENEMY_IMAGE;
        int imgH = (img != null) ? img.getHeight() : 150;
        // 子弹从Boss贴图底部中心发射
        // locationY是中心点，贴图向下延伸imgH/2，所以底部是locationY + imgH/2
        // 但绘制时使用了SPRITE_SCALE=0.45缩放，所以实际显示高度是imgH * 0.45
        int displayHeight = (int) (imgH * 0.45f);
        int centerY = getLocationY() + displayHeight / 2 + 10; // +10让子弹从贴图底部稍下方发射
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
