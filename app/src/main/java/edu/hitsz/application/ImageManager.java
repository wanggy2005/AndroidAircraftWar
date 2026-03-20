package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.aircraft.SuperEliteEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.SuperFireProp;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片管理器 - Android 版本
 */
public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BACKGROUND_EASY_IMAGE;
    public static Bitmap BACKGROUND_NORMAL_IMAGE;
    public static Bitmap BACKGROUND_HARD_IMAGE;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap SUPERELITE_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap BLOOD_PROP_IMAGE;
    public static Bitmap FIRE_PROP_IMAGE;
    public static Bitmap SUPER_FIRE_PROP_IMAGE;
    public static Bitmap BOMB_PROP_IMAGE;

    private static boolean initialized = false;

    /**
     * 初始化图片资源，必须在使用前调用一次（通常在 Application 或首个 Activity 中调用）
     */
    public static void init(Context context) {
        if (initialized) return;

        BACKGROUND_IMAGE        = load(context, "bg");
        BACKGROUND_EASY_IMAGE   = load(context, "bg2");
        BACKGROUND_NORMAL_IMAGE = load(context, "bg3");
        BACKGROUND_HARD_IMAGE   = load(context, "bg4");

        HERO_IMAGE              = load(context, "hero");
        MOB_ENEMY_IMAGE         = load(context, "mob");
        ELITE_ENEMY_IMAGE       = load(context, "elite");
        SUPERELITE_ENEMY_IMAGE  = load(context, "eliteplus");
        BOSS_ENEMY_IMAGE        = load(context, "boss");
        HERO_BULLET_IMAGE       = load(context, "bullet_hero");
        ENEMY_BULLET_IMAGE      = load(context, "bullet_enemy");
        BLOOD_PROP_IMAGE        = load(context, "prop_blood");
        FIRE_PROP_IMAGE         = load(context, "prop_bullet");
        SUPER_FIRE_PROP_IMAGE   = load(context, "prop_bulletplus");
        BOMB_PROP_IMAGE         = load(context, "prop_bomb");

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(),    HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(),        MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(),      ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperEliteEnemy.class.getName(), SUPERELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(),       BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(),      HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(),     ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(),       BLOOD_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(FireProp.class.getName(),        FIRE_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperFireProp.class.getName(),   SUPER_FIRE_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(),        BOMB_PROP_IMAGE);

        initialized = true;
    }

    /** 从 res/drawable 加载图片（文件名不含扩展名） */
    private static Bitmap load(Context context, String name) {
        int resId = context.getResources().getIdentifier(
                name, "drawable", context.getPackageName());
        if (resId == 0) return null;
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    public static Bitmap get(String className) {
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj) {
        if (obj == null) return null;
        return get(obj.getClass().getName());
    }

    public static Bitmap getBackgroundByDifficulty(String difficulty) {
        switch (difficulty) {
            case "简单模式": return BACKGROUND_EASY_IMAGE;
            case "普通模式": return BACKGROUND_NORMAL_IMAGE;
            case "困难模式": return BACKGROUND_HARD_IMAGE;
            default:        return BACKGROUND_IMAGE;
        }
    }
}
