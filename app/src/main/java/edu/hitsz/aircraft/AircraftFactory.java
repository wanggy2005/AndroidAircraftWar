package edu.hitsz.aircraft;

import android.graphics.Bitmap;
import edu.hitsz.application.ImageManager;
import edu.hitsz.basic.AbstractFlyingObject;

public class AircraftFactory {

    public static AbstractAircraft createRandomAircraft(double eliteProbability, double attributeMultiplier) {
        int imgW = (ImageManager.MOB_ENEMY_IMAGE != null) ? ImageManager.MOB_ENEMY_IMAGE.getWidth() : 32;
        int locationX = (int) (Math.random() * (AbstractFlyingObject.WINDOW_WIDTH - imgW));
        int locationY = (int) (Math.random() * AbstractFlyingObject.WINDOW_HEIGHT * 0.05);
        double rand = Math.random();
        if (rand < eliteProbability) {
            if (rand < eliteProbability * 0.7) {
                return new EliteEnemy(locationX, locationY, 0,
                        (int)(5 * attributeMultiplier), (int)(60 * attributeMultiplier));
            } else {
                return new SuperEliteEnemy(locationX, locationY,
                        (int)(3 * attributeMultiplier), (int)(3 * attributeMultiplier),
                        (int)(100 * attributeMultiplier));
            }
        } else {
            return new MobEnemy(locationX, locationY, 0,
                    (int)(8 * attributeMultiplier), (int)(30 * attributeMultiplier));
        }
    }

    public static AbstractAircraft createBossAircraft() {
        // Boss水平居中，垂直位置在屏幕最上方
        // 考虑贴图缩放比例0.45，计算实际显示尺寸
        Bitmap img = ImageManager.BOSS_ENEMY_IMAGE;
        int imgW = (img != null) ? img.getWidth() : 200;
        int imgH = (img != null) ? img.getHeight() : 150;
        int displayW = (int) (imgW * 0.45f);
        int displayH = (int) (imgH * 0.45f);

        // 水平居中：屏幕宽度一半
        int locationX = AbstractFlyingObject.WINDOW_WIDTH / 2;
        // 垂直位置：让贴图完整显示，顶部留出一点边距
        // locationY是中心点，要让贴图顶部在屏幕内，中心点需要是 displayH/2 + 边距
        int locationY = displayH / 2 + 10;
        return new BossEnemy(locationX, locationY, 2, 0, 999);
    }
}
