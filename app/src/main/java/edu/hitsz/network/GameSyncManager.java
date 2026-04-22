package edu.hitsz.network;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 游戏状态同步管理器
 * 在后台线程中定期上报本方状态并拉取对手状态
 */
public class GameSyncManager {

    private static final String TAG = "GameSyncManager";
    private static final long SYNC_INTERVAL_MS = 100; // 100ms 轮询间隔

    private final String playerId;
    private final String roomId;
    private Thread syncThread;
    private final AtomicBoolean running = new AtomicBoolean(false);

    // 本方待上报的状态
    private volatile int myScore = 0;
    private volatile int myHp = 1000;
    private volatile float myX = 256;
    private volatile float myY = 688;
    private volatile boolean myAlive = true;

    // 对手状态
    private final OnlineGameData opponentData = new OnlineGameData();

    // 快捷消息队列
    private final CopyOnWriteArrayList<String> pendingMessages = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<String> receivedMessages = new CopyOnWriteArrayList<>();

    public GameSyncManager(String playerId, String roomId) {
        this.playerId = playerId;
        this.roomId = roomId;
    }

    /**
     * 启动同步线程
     */
    public void start() {
        if (running.get()) return;
        running.set(true);
        syncThread = new Thread(this::syncLoop, "GameSyncThread");
        syncThread.setDaemon(true);
        syncThread.start();
    }

    /**
     * 停止同步线程
     */
    public void stop() {
        running.set(false);
        if (syncThread != null) {
            syncThread.interrupt();
            try { syncThread.join(500); } catch (InterruptedException ignored) {}
        }
    }

    /**
     * 更新本方状态（由 GameView 调用）
     */
    public void updateMyState(int score, int hp, float x, float y, boolean alive) {
        this.myScore = score;
        this.myHp = hp;
        this.myX = x;
        this.myY = y;
        this.myAlive = alive;
    }

    /**
     * 获取对手状态（由 GameView 调用）
     */
    public OnlineGameData getOpponentState() {
        return opponentData;
    }

    /**
     * 发送快捷消息
     */
    public void sendQuickMessage(String message) {
        pendingMessages.add(message);
    }

    /**
     * 获取并清空收到的快捷消息
     */
    public List<String> pollReceivedMessages() {
        List<String> msgs = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return msgs;
    }

    public String getPlayerId() { return playerId; }
    public String getRoomId() { return roomId; }

    private void syncLoop() {
        while (running.get()) {
            try {
                // 1. 上报本方状态
                String uploadBody = "{\"playerId\":\"" + playerId +
                        "\",\"roomId\":\"" + roomId +
                        "\",\"score\":" + myScore +
                        ",\"hp\":" + myHp +
                        ",\"x\":" + myX +
                        ",\"y\":" + myY +
                        ",\"alive\":" + myAlive + "}";
                ApiClient.post("/api/game/update", uploadBody);

                // 2. 拉取对手状态
                String stateJson = ApiClient.get("/api/game/state?playerId=" + playerId + "&roomId=" + roomId);
                opponentData.updateFromJson(stateJson);

                // 3. 发送待发快捷消息
                for (String msg : pendingMessages) {
                    String msgBody = "{\"playerId\":\"" + playerId +
                            "\",\"roomId\":\"" + roomId +
                            "\",\"message\":\"" + msg + "\"}";
                    ApiClient.post("/api/msg/message", msgBody);
                }
                pendingMessages.clear();

                // 4. 拉取快捷消息
                String msgsJson = ApiClient.get("/api/msg/messages?playerId=" + playerId + "&roomId=" + roomId);
                parseMessages(msgsJson);

                Thread.sleep(SYNC_INTERVAL_MS);
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                Log.w(TAG, "Sync error: " + e.getMessage());
                try { Thread.sleep(500); } catch (InterruptedException ex) { break; }
            }
        }
    }

    private void parseMessages(String json) {
        // 简单解析 {"messages":[{"message":"xxx"}, ...]}
        String messagesArray = json;
        int idx = messagesArray.indexOf("[");
        if (idx < 0) return;
        // 寻找每个 "message":"xxx"
        String search = "\"message\":\"";
        int pos = 0;
        while (true) {
            int found = messagesArray.indexOf(search, pos);
            if (found < 0) break;
            int start = found + search.length();
            int end = messagesArray.indexOf("\"", start);
            if (end < 0) break;
            receivedMessages.add(messagesArray.substring(start, end));
            pos = end + 1;
        }
    }
}
