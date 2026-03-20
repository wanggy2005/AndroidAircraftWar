package edu.hitsz.difficulty;

public class EasyDifficulty extends AbstractDifficulty {

    @Override protected void setDifficultyName() { this.difficultyName = "简单模式"; }
    @Override protected void setEnemyMaxNumber() { this.enemyMaxNumber = 4; }
    @Override protected void setCycleDuration() { this.cycleDuration = 800; }
    @Override protected void setHeroShootCycleDuration() { this.heroShootCycleDuration = 300; }
    @Override protected void setBossThreshold() { this.bossThreshold = 600; }
    @Override protected void setEliteEnemyProbability() { this.eliteEnemyProbability = 0.15; }
    @Override protected void setEnemyAttributeMultiplier() { this.enemyAttributeMultiplier = 1.0; }
    @Override protected void doIncreaseDifficulty() { /* 简单模式不提升难度 */ }
    @Override protected int getBossThresholdIncrement() { return 600; }
}
