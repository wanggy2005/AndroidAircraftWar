package edu.hitsz.score;

import java.util.List;

public interface ScoreDao {
    boolean saveScore(ScoreRecord record);
    List<ScoreRecord> getAllScores();
    List<ScoreRecord> getLeaderboard(int limit);
    List<ScoreRecord> getLeaderboard();
    List<ScoreRecord> getLeaderboardByDifficulty(String difficulty);
    boolean deleteScoreRecord(String playerName, int score, String difficulty);
    boolean clearAllScores();
    int getScoreCount();
}
