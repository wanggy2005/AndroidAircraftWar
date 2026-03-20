package edu.hitsz.score;

public class ScoreRecord implements Comparable<ScoreRecord> {

    private int score;
    private long gameTime;
    private long createTimeMs;
    private String playerName;
    private String difficulty;

    public ScoreRecord(int score, long gameTime, String playerName, String difficulty) {
        this.score = score;
        this.gameTime = gameTime;
        this.createTimeMs = System.currentTimeMillis();
        this.playerName = playerName;
        this.difficulty = difficulty;
    }

    // Used when loading from storage
    public ScoreRecord(int score, long gameTime, String playerName, String difficulty, long createTimeMs) {
        this.score = score;
        this.gameTime = gameTime;
        this.createTimeMs = createTimeMs;
        this.playerName = playerName;
        this.difficulty = difficulty;
    }

    public int getScore() { return score; }
    public long getGameTime() { return gameTime; }
    public long getCreateTimeMs() { return createTimeMs; }
    public String getPlayerName() { return playerName; }
    public String getDifficulty() { return difficulty; }

    public String getFormattedGameTime() {
        long seconds = gameTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public int compareTo(ScoreRecord other) {
        int c = Integer.compare(other.score, this.score);
        if (c != 0) return c;
        return Long.compare(this.gameTime, other.gameTime);
    }

    @Override
    public String toString() {
        return playerName + " - " + score + "分 - " + getFormattedGameTime();
    }
}
