package edu.hitsz.score;

public class ScoreItem {

    private int id;
    private String name;
    private int score;
    private String difficulty;

    public ScoreItem() {
    }

    public ScoreItem(int id, String name, int score, String difficulty) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
