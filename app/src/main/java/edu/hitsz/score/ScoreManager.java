package edu.hitsz.score;

import java.util.List;

public class ScoreManager {

    private final ScoreDao scoreDao;

    public ScoreManager(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public boolean saveGameScore(int score, long gameTime, String playerName, String difficulty) {
        return scoreDao.saveScore(new ScoreRecord(score, gameTime, playerName, difficulty));
    }

    public List<ScoreRecord> getLeaderboard(String difficulty) {
        return scoreDao.getLeaderboardByDifficulty(difficulty);
    }

    public List<ScoreRecord> getLeaderboard() {
        return scoreDao.getLeaderboard();
    }

    public boolean deleteScoreRecord(String playerName, int score, String difficulty) {
        return scoreDao.deleteScoreRecord(playerName, score, difficulty);
    }

    public boolean clearAllScores() {
        return scoreDao.clearAllScores();
    }

    public int getScoreCount() {
        return scoreDao.getScoreCount();
    }
}
