package edu.hitsz.prop;

public class PropFactory {

    public static AbstractProp createRandomProp(int locationX, int locationY) {
        double r = Math.random();
        if (r < 0.25)      return new BloodProp(locationX, locationY, 0, 3);
        else if (r < 0.5)  return new FireProp(locationX, locationY, 0, 3);
        else if (r < 0.75) return new SuperFireProp(locationX, locationY, 0, 3);
        else               return new BombProp(locationX, locationY, 0, 3);
    }
}
