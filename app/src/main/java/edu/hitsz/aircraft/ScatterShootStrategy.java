package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class ScatterShootStrategy implements ShootStrategy {

    private final int shootNum = 3;
    private final int power = 30;
    private final int direction = -1;

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int x = aircraft.getLocationX();
        int y = aircraft.getLocationY() + direction * 2;
        int baseSpeedY = aircraft.getSpeedY() + direction * 5;
        for (int i = 0; i < shootNum; i++) {
            int bulletX = x + (i * 2 - shootNum + 1) * 10;
            int speedX = (i - shootNum / 2) * 2;
            res.add(new HeroBullet(bulletX, y, speedX, baseSpeedY, power));
        }
        return res;
    }
}
