package edu.hitsz.prop;

public class BloodProp extends AbstractProp {

    private final int healAmount = 200;

    public BloodProp(int locationX, int locationY, int speedX, int speedY) {
        super(locationX, locationY, speedX, speedY);
    }

    @Override
    public void activate() { /* 加血由 Game 处理 */ }

    public int getHealAmount() { return healAmount; }
}
