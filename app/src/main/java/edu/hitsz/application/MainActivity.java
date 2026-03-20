package edu.hitsz.application;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import edu.hitsz.R;

/**
 * 游戏开始界面 Activity（替代 Java SE 的 Game_start + JFrame）
 */
public class MainActivity extends AppCompatActivity {

    private Spinner difficultySpinner;
    private Switch  soundSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultySpinner = findViewById(R.id.spinnerDifficulty);
        soundSwitch       = findViewById(R.id.switchSound);
        Button btnStart   = findViewById(R.id.btnStart);
        Button btnExit    = findViewById(R.id.btnExit);

        String[] difficulties = {"简单模式", "普通模式", "困难模式"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficulties);
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
