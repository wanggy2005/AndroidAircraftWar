package edu.hitsz.network;

/**
 * 对手游戏状态数据类
 */
public class OnlineGameData {
    private int score;
    private int hp;
    private float x;
    private float y;
    private boolean alive;
    private long timestamp;
    private String roomStatus; // WAITING, READY, PLAYING, FINISHED

    public OnlineGameData() {
        this.alive = true;
        this.hp = 1000;
    }

    public void updateFromJson(String json) {
        // 解析 opponent 子对象
        String opponentJson = extractSubObject(json, "opponent");
        if (opponentJson != null) {
            this.score = ApiClient.extractInt(opponentJson, "score", this.score);
            this.hp = ApiClient.extractInt(opponentJson, "hp", this.hp);
            this.x = ApiClient.extractFloat(opponentJson, "x", this.x);
            this.y = ApiClient.extractFloat(opponentJson, "y", this.y);
            this.alive = ApiClient.extractBoolean(opponentJson, "alive", this.alive);
            this.timestamp = Long.parseLong(
                    extractLongStr(opponentJson, "timestamp", String.valueOf(this.timestamp)));
        }
        this.roomStatus = ApiClient.extractString(json, "roomStatus");
    }

    private String extractSubObject(String json, String key) {
        String search = "\"" + key + "\":{";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int start = idx + search.length() - 1; // include '{'
        int braces = 0;
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '{') braces++;
            else if (json.charAt(i) == '}') {
                braces--;
                if (braces == 0) return json.substring(start, i + 1);
            }
        }
        return null;
    }

    private String extractLongStr(String json, String key, String defaultVal) {
        String search = "\"" + key + "\":";
        int idx = json.indexOf(search);
        if (idx < 0) return defaultVal;
        int start = idx + search.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return defaultVal;
        return json.substring(start, end);
    }

    public int getScore() { return score; }
    public int getHp() { return hp; }
    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isAlive() { return alive; }
    public long getTimestamp() { return timestamp; }
    public String getRoomStatus() { return roomStatus; }
    public boolean isGameFinished() { return "FINISHED".equals(roomStatus); }
}
