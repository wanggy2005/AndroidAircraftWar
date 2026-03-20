package edu.hitsz.aircraft;

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
        int imgW = (ImageManager.BOSS_ENEMY_IMAGE != null) ? ImageManager.BOSS_ENEMY_IMAGE.getWidth() : 64;
        int locationX = AbstractFlyingObject.WINDOW_WIDTH / 2 - imgW / 2;
        return new BossEnemy(locationX, 50, 2, 0, 999);
    }
}
