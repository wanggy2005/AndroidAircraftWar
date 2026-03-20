package edu.hitsz.aircraft;

import edu.hitsz.bullet.BaseBullet;
import java.util.List;

public interface ShootStrategy {
    List<BaseBullet> shoot(AbstractAircraft aircraft);
}
