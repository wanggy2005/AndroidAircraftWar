package edu.hitsz.application;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import edu.hitsz.R;
import edu.hitsz.network.ApiClient;

/**
 * 登录/注册页面
 */
public class LoginActivity extends AppCompatActivity {

    public static final String PREF_NAME = "aircraft_war_prefs";
    public static final String KEY_PLAYER_ID = "playerId";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_SERVER_URL = "serverUrl";

    private EditText etServerUrl, etNickname, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etServerUrl = findViewById(R.id.etServerUrl);
        etNickname = findViewById(R.id.etNickname);
        etPassword = findViewById(R.id.etPassword);

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String savedUrl = prefs.getString(KEY_SERVER_URL, "http://10.0.2.2:8888");
        etServerUrl.setText(savedUrl);

        String savedNickname = prefs.getString(KEY_NICKNAME, "");
        if (!savedNickname.isEmpty()) {
            etNickname.setText(savedNickname);
        }

        findViewById(R.id.btnLogin).setOnClickListener(v -> doLogin());
        findViewById(R.id.btnRegister).setOnClickListener(v -> doRegister());
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void doLogin() {
        String serverUrl = etServerUrl.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nickname.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入昵称和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.setBaseUrl(serverUrl);

        new Thread(() -> {
            try {
                String body = "{\"nickname\":\"" + nickname + "\",\"password\":\"" + password + "\"}";
                String response = ApiClient.post("/api/account/login", body);

                if (ApiClient.isSuccess(response)) {
                    String playerId = extractPlayerId(response);
                    String playerNickname = extractPlayerNickname(response, nickname);
                    if (playerId == null || playerId.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this,
                                "登录失败: 服务器返回的玩家信息不完整", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    saveLoginInfo(serverUrl, playerId, playerNickname);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                        launchOnlineLobby();
                    });
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() ->
                            Toast.makeText(this, "登录失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void doRegister() {
        String serverUrl = etServerUrl.getText().toString().trim();
        String nickname = etNickname.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (nickname.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入昵称和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiClient.setBaseUrl(serverUrl);

        new Thread(() -> {
            try {
                String body = "{\"nickname\":\"" + nickname + "\",\"password\":\"" + password + "\"}";
                String response = ApiClient.post("/api/account/register", body);

                if (ApiClient.isSuccess(response)) {
                    String playerId = extractPlayerId(response);
                    String playerNickname = extractPlayerNickname(response, nickname);
                    if (playerId == null || playerId.isEmpty()) {
                        runOnUiThread(() -> Toast.makeText(this,
                                "注册失败: 服务器返回的玩家信息不完整", Toast.LENGTH_SHORT).show());
                        return;
                    }

                    saveLoginInfo(serverUrl, playerId, playerNickname);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "注册成功，已自动登录", Toast.LENGTH_SHORT).show();
                        launchOnlineLobby();
                    });
                } else {
                    String error = ApiClient.extractString(response, "error");
                    runOnUiThread(() ->
                            Toast.makeText(this, "注册失败: " + error, Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "网络错误: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private String extractPlayerId(String response) throws Exception {
        JSONObject root = new JSONObject(response);
        JSONObject player = root.optJSONObject("player");
        if (player == null) {
            return null;
        }
        return player.optString("id", null);
    }

    private String extractPlayerNickname(String response, String fallbackNickname) throws Exception {
        JSONObject root = new JSONObject(response);
        JSONObject player = root.optJSONObject("player");
        if (player == null) {
            return fallbackNickname;
        }
        String playerNickname = player.optString("nickname", fallbackNickname);
        return playerNickname == null || playerNickname.isEmpty() ? fallbackNickname : playerNickname;
    }

    private void launchOnlineLobby() {
        Intent intent = new Intent(this, OnlineLobbyActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveLoginInfo(String serverUrl, String playerId, String nickname) {
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        editor.putString(KEY_PLAYER_ID, playerId);
        editor.putString(KEY_NICKNAME, nickname);
        editor.apply();
    }
}
