package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.widget.SwitchCompat;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

/**
 * 游戏开始界面 Activity（替代 Java SE 的 Game_start + JFrame）
 */
public class MainActivity extends AppCompatActivity {

    private Spinner difficultySpinner;
    private SwitchCompat soundSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultySpinner = findViewById(R.id.spinnerDifficulty);
        soundSwitch = findViewById(R.id.switchSound);
        Button btnStart   = findViewById(R.id.btnStart);
        Button btnExit    = findViewById(R.id.btnExit);

        String[] difficulties = {"简单模式", "普通模式", "困难模式"};

        // 自定义适配器，设置白色文字
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, difficulties) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView view = (android.widget.TextView) super.getView(position, convertView, parent);
                view.setTextColor(android.graphics.Color.WHITE);
                view.setTextSize(16);
                return view;
            }

            @Override
            public android.view.View getDropDownView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.widget.TextView view = (android.widget.TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(android.graphics.Color.WHITE);
                view.setTextSize(16);
                view.setPadding(20, 16, 20, 16);
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        difficultySpinner.setAdapter(adapter);
        difficultySpinner.setSelection(1); // 默认普通模式

        btnStart.setOnClickListener(v -> startGame());
        btnExit.setOnClickListener(v -> finish());
    }

    private void startGame() {
        String difficulty  = (String) difficultySpinner.getSelectedItem();
        boolean soundOn    = soundSwitch.isChecked();
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_DIFFICULTY, difficulty);
        intent.putExtra(GameActivity.EXTRA_SOUND, soundOn);
        startActivity(intent);
    }
}
