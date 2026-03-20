package edu.hitsz.difficulty;

/**
 * 难度工厂类
 * 根据难度名称创建对应的难度实例
 */
public class DifficultyFactory {
    
    /**
     * 创建难度实例
     * @param difficultyName 难度名称
     * @return 难度实例
     */
    public static AbstractDifficulty createDifficulty(String difficultyName) {
        switch (difficultyName) {
            case "简单模式":
                return new EasyDifficulty();
            case "普通模式":
                return new NormalDifficulty();
            case "困难模式":
                return new HardDifficulty();
            default:
                throw new IllegalArgumentException("未知的难度模式: " + difficultyName);
        }
    }
    
    /**
     * 获取所有可用的难度模式
     * @return 难度模式数组
     */
    public static String[] getAvailableDifficulties() {
        return new String[]{"简单模式", "普通模式", "困难模式"};
    }
}
