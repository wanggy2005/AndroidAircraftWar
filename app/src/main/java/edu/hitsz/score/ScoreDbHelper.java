package edu.hitsz.score;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import edu.hitsz.application.LoginActivity;

public class ScoreDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "score_rank.db";
    private static final int DB_VERSION = 2;

    public static final String TABLE_SCORE = "score_table";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_SCORE = "score";
    public static final String COL_DIFFICULTY = "difficulty";

    private final Context context;

    public ScoreDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createSql = "CREATE TABLE " + TABLE_SCORE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_SCORE + " INTEGER NOT NULL, "
                + COL_DIFFICULTY + " TEXT NOT NULL"
                + ")";
        db.execSQL(createSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCORE);
        onCreate(db);
    }

    public long addScore(String name, int score, String difficulty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_SCORE, score);
        values.put(COL_DIFFICULTY, difficulty);
        return db.insert(TABLE_SCORE, null, values);
    }

    public List<ScoreItem> getScoresByDifficulty(String difficulty) {
        List<ScoreItem> result = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_SCORE,
                new String[]{COL_ID, COL_NAME, COL_SCORE, COL_DIFFICULTY},
                COL_DIFFICULTY + "=?",
                new String[]{difficulty},
                null,
                null,
                COL_SCORE + " DESC"
        );

        String realNickname = context.getSharedPreferences(LoginActivity.PREF_NAME, Context.MODE_PRIVATE)
                .getString(LoginActivity.KEY_NICKNAME, nameFallback());

        if (cursor != null) {
            try {
                int idIndex = cursor.getColumnIndexOrThrow(COL_ID);
                int nameIndex = cursor.getColumnIndexOrThrow(COL_NAME);
                int scoreIndex = cursor.getColumnIndexOrThrow(COL_SCORE);
                int difficultyIndex = cursor.getColumnIndexOrThrow(COL_DIFFICULTY);

                while (cursor.moveToNext()) {
                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    int score = cursor.getInt(scoreIndex);
                    String itemDifficulty = cursor.getString(difficultyIndex);
                    if (name != null && "Player".equalsIgnoreCase(name)) {
                        name = realNickname;
                    }
                    result.add(new ScoreItem(id, name, score, itemDifficulty));
                }
            } finally {
                cursor.close();
            }
        }

        return result;
    }

    public int deleteScore(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_SCORE, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    private String nameFallback() {
        return "player";
    }
}
