package edu.hitsz.difficulty;

/**
 * 游戏难度抽象类（模板模式）
 */
public abstract class AbstractDifficulty {

    protected String difficultyName;
    protected int enemyMaxNumber;
    protected int cycleDuration;
    protected int heroShootCycleDuration;
    protected int bossThreshold;
    protected double eliteEnemyProbability;
    protected double enemyAttributeMultiplier;

    protected long gameStartTime;
    protected int difficultyIncreaseInterval = 15000;
    protected long lastDifficultyIncreaseTime;

    public AbstractDifficulty() {
        this.gameStartTime = System.currentTimeMillis();
        this.lastDifficultyIncreaseTime = gameStartTime;
        initializeDifficulty();
    }

    public final void initializeDifficulty() {
        setDifficultyName();
        setEnemyMaxNumber();
        setCycleDuration();
        setHeroShootCycleDuration();
        setBossThreshold();
        setEliteEnemyProbability();
        setEnemyAttributeMultiplier();
    }

    public final boolean shouldIncreaseDifficulty() {
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastDifficultyIncreaseTime) >= difficultyIncreaseInterval;
    }

    public final void increaseDifficulty() {
        if (shouldIncreaseDifficulty()) {
            doIncreaseDifficulty();
            lastDifficultyIncreaseTime = System.currentTimeMillis();
        }
    }

    public final boolean shouldGenerateBoss(int score) {
        return score >= bossThreshold;
    }

    public final void updateBossThreshold() {
        bossThreshold += getBossThresholdIncrement();
    }

    protected abstract void setDifficultyName();
    protected abstract void setEnemyMaxNumber();
    protected abstract void setCycleDuration();
    protected abstract void setHeroShootCycleDuration();
    protected abstract void setBossThreshold();
    protected abstract void setEliteEnemyProbability();
    protected abstract void setEnemyAttributeMultiplier();
    protected abstract void doIncreaseDifficulty();
    protected abstract int getBossThresholdIncrement();

    public String getDifficultyName() { return difficultyName; }
    public int getEnemyMaxNumber() { return enemyMaxNumber; }
    public int getCycleDuration() { return cycleDuration; }
    public int getHeroShootCycleDuration() { return heroShootCycleDuration; }
    public int getBossThreshold() { return bossThreshold; }
    public double getEliteEnemyProbability() { return eliteEnemyProbability; }
    public double getEnemyAttributeMultiplier() { return enemyAttributeMultiplier; }
}
