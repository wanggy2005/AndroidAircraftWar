package edu.hitsz.prop;

import edu.hitsz.observer.BombManager;

public class BombProp extends AbstractProp {

    private static BombManager bombManager;

    public BombProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        if (bombManager != null) bombManager.triggerBombExplosion();
    }

    public static void setBombManager(BombManager manager) {
        bombManager = manager;
    }
}
