package edu.hitsz.prop;

import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.ScatterShootStrategy;

public class FireProp extends AbstractProp {

    public FireProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() {
        HeroAircraft hero = HeroAircraft.getInstance(0, 0, 0, 0, 0);
        hero.setTemporaryShootStrategy(new ScatterShootStrategy(), 3000);
    }
}
