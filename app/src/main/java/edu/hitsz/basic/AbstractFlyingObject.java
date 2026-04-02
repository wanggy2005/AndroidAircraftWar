package edu.hitsz.basic;

import android.graphics.Bitmap;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.application.ImageManager;

/**
 * 可飞行对象的父类
 */
public abstract class AbstractFlyingObject {

    protected int locationX;
    protected int locationY;
    protected int speedX;
    protected int speedY;

    protected Bitmap image = null;
    protected int width = -1;
    protected int height = -1;
    protected boolean isValid = true;

    // 游戏逻辑区域宽高（像素，逻辑坐标系）
    public static int WINDOW_WIDTH = 512;
    public static int WINDOW_HEIGHT = 768;

    public AbstractFlyingObject() {}

    public AbstractFlyingObject(int locationX, int locationY, int speedX, int speedY) {
        this.locationX = locationX;
        this.locationY = locationY;
        this.speedX = speedX;
        this.speedY = speedY;
    }

    public void forward() {
        locationX += speedX;
        locationY += speedY;
        if (locationX <= 0 || locationX >= WINDOW_WIDTH) {
            speedX = -speedX;
        }
    }

    // 碰撞范围缩放系数（0.0-1.0，越小碰撞范围越小）
    private static final float COLLISION_SCALE_X = 0.40f;  // 水平方向（稍微调小）
    private static final float COLLISION_SCALE_Y = 0.25f;  // 垂直方向（更小）

    public boolean crash(AbstractFlyingObject flyingObject) {
        // 计算两个物体的碰撞框（使用缩放后的尺寸，X和Y方向可以不同）
        int thisWidth = (int) (this.getWidth() * COLLISION_SCALE_X);
        int thisHeight = (int) (this.getHeight() * COLLISION_SCALE_Y);
        int otherWidth = (int) (flyingObject.getWidth() * COLLISION_SCALE_X);
        int otherHeight = (int) (flyingObject.getHeight() * COLLISION_SCALE_Y);

        int otherX = flyingObject.getLocationX();
        int otherY = flyingObject.getLocationY();

        // 计算碰撞边界（基于中心点）
        int thisLeft = this.locationX - thisWidth / 2;
        int thisRight = this.locationX + thisWidth / 2;
        int thisTop = this.locationY - thisHeight / 2;
        int thisBottom = this.locationY + thisHeight / 2;

        int otherLeft = otherX - otherWidth / 2;
        int otherRight = otherX + otherWidth / 2;
        int otherTop = otherY - otherHeight / 2;
        int otherBottom = otherY + otherHeight / 2;

        // AABB碰撞检测
        return thisLeft < otherRight &&
               thisRight > otherLeft &&
               thisTop < otherBottom &&
               thisBottom > otherTop;
    }

    public int getLocationX() { return locationX; }
    public int getLocationY() { return locationY; }

    public void setLocation(double locationX, double locationY) {
        this.locationX = (int) locationX;
        this.locationY = (int) locationY;
    }

    public int getSpeedY() { return speedY; }

    public Bitmap getImage() {
        if (image == null) {
            image = ImageManager.get(this);
        }
        return image;
    }

    public int getWidth() {
        if (width == -1) {
            Bitmap bmp = ImageManager.get(this);
            width = (bmp != null) ? bmp.getWidth() : 32;
        }
        return width;
    }

    public int getHeight() {
        if (height == -1) {
            Bitmap bmp = ImageManager.get(this);
            height = (bmp != null) ? bmp.getHeight() : 32;
        }
        return height;
    }

    public boolean notValid() { return !this.isValid; }

    public void vanish() { isValid = false; }
}
