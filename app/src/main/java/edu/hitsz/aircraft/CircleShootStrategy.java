package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.bullet.HeroBullet;

import java.util.LinkedList;
import java.util.List;

public class CircleShootStrategy implements ShootStrategy {

    private final int shootNum = 16;
    private final int power = 30;
    private final int direction = -1;
    private final int bulletSpeed = 8;

    @Override
    public List<BaseBullet> shoot(AbstractAircraft aircraft) {
        List<BaseBullet> res = new LinkedList<>();
        int centerX = aircraft.getLocationX();
        int centerY = aircraft.getLocationY() + direction * 2;
        double angleStep = 2 * Math.PI / shootNum;
        for (int i = 0; i < shootNum; i++) {
            double angle = i * angleStep;
            int sx = (int) (Math.cos(angle) * bulletSpeed);
            int sy = (int) (Math.sin(angle) * bulletSpeed);
            res.add(new HeroBullet(centerX, centerY, sx, sy, power));
        }
        return res;
    }
}
