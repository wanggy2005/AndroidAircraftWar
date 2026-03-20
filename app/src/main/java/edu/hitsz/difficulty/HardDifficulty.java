package edu.hitsz.difficulty;

public class HardDifficulty extends AbstractDifficulty {

    @Override protected void setDifficultyName() { this.difficultyName = "困难模式"; }
    @Override protected void setEnemyMaxNumber() { this.enemyMaxNumber = 8; }
    @Override protected void setCycleDuration() { this.cycleDuration = 400; }
    @Override protected void setHeroShootCycleDuration() { this.heroShootCycleDuration = 500; }
    @Override protected void setBossThreshold() { this.bossThreshold = 1000; }
    @Override protected void setEliteEnemyProbability() { this.eliteEnemyProbability = 0.30; }
    @Override protected void setEnemyAttributeMultiplier() { this.enemyAttributeMultiplier = 1.2; }

    @Override
    protected void doIncreaseDifficulty() {
        if (eliteEnemyProbability < 0.50) eliteEnemyProbability += 0.015;
        if (cycleDuration > 200) cycleDuration -= 40;
        if (enemyAttributeMultiplier < 2.0) enemyAttributeMultiplier += 0.03;
    }

    @Override protected int getBossThresholdIncrement() { return 1000; }
}
