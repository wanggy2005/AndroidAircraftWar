package edu.hitsz.difficulty;

public class NormalDifficulty extends AbstractDifficulty {

    @Override protected void setDifficultyName() { this.difficultyName = "普通模式"; }
    @Override protected void setEnemyMaxNumber() { this.enemyMaxNumber = 6; }
    @Override protected void setCycleDuration() { this.cycleDuration = 600; }
    @Override protected void setHeroShootCycleDuration() { this.heroShootCycleDuration = 400; }
    @Override protected void setBossThreshold() { this.bossThreshold = 800; }
    @Override protected void setEliteEnemyProbability() { this.eliteEnemyProbability = 0.20; }
    @Override protected void setEnemyAttributeMultiplier() { this.enemyAttributeMultiplier = 1.0; }

    @Override
    protected void doIncreaseDifficulty() {
        if (eliteEnemyProbability < 0.35) eliteEnemyProbability += 0.01;
        if (cycleDuration > 400) cycleDuration -= 30;
        if (enemyAttributeMultiplier < 1.5) enemyAttributeMultiplier += 0.02;
    }

    @Override protected int getBossThresholdIncrement() { return 800; }
}
