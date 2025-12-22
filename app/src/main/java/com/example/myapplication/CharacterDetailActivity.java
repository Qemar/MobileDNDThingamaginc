// app/src/main/java/com/example/myapplication/CharacterDetailActivity.java
package com.example.myapplication;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.data.CharacterContract.CharacterEntry;
import com.example.myapplication.data.CharacterDbHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CharacterDetailActivity extends AppCompatActivity {

    private long charId;
    private CharacterDbHelper dbHelper;
    private ExecutorService exec;

    private TextView tvSummary;
    private EditText etCurrentHp, etMaxHp;
    private Button btnSaveHp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_detail);

        dbHelper = new CharacterDbHelper(this);
        exec = Executors.newSingleThreadExecutor();

        tvSummary = findViewById(R.id.tvDetailSummary);
        etCurrentHp = findViewById(R.id.etCurrentHp);
        etMaxHp = findViewById(R.id.etMaxHp);
        btnSaveHp = findViewById(R.id.btnSaveHp);

        charId = getIntent().getLongExtra("CHAR_ID", -1);
        if (charId == -1) {
            finish();
            return;
        }

        loadCharacter(charId);

        btnSaveHp.setOnClickListener(v -> {
            int cur = parseIntSafe(etCurrentHp.getText().toString(), 0);
            int max = parseIntSafe(etMaxHp.getText().toString(), 0);
            ContentValues cv = new ContentValues();
            cv.put(CharacterEntry.COLUMN_CURRENT_HP, cur);
            cv.put(CharacterEntry.COLUMN_MAX_HP, max);
            exec.execute(() -> {
                int rows = dbHelper.updateCharacter(charId, cv);
                runOnUiThread(() ->
                        Toast.makeText(this, rows > 0 ? "HP сохранены" : "Ошибка сохранения", Toast.LENGTH_SHORT).show()
                );
            });
        });
    }

    private void loadCharacter(long id) {
        exec.execute(() -> {
            Cursor c = dbHelper.queryById(id);
            if (c != null && c.moveToFirst()) {
                final String name = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_NAME));
                final String race = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_RACE));
                final String subrace = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_SUBRACE));
                final String cls = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CLASS));
                final int str = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_STR));
                final int dex = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_DEX));
                final int con = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CON));
                final int intl = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_INT));
                final int wis = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_WIS));
                final int cha = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CHA));
                final String skillsCsv = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_SKILLS));
                final String equipText = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_EQUIP_TEXT));
                final int currentHp = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CURRENT_HP));
                final int maxHp = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_MAX_HP));
                c.close();

                runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(name).append("\n");
                    sb.append(race);
                    if (subrace != null && !subrace.isEmpty()) sb.append(" (").append(subrace).append(")");
                    sb.append("\nClass: ").append(cls).append("\n\n");
                    sb.append("STR ").append(str).append(", DEX ").append(dex).append(", CON ").append(con).append("\n");
                    sb.append("INT ").append(intl).append(", WIS ").append(wis).append(", CHA ").append(cha).append("\n\n");
                    sb.append("Skills: ").append(skillsCsv == null ? "—" : skillsCsv).append("\n\n");
                    sb.append("Equipment:\n").append(equipText);

                    tvSummary.setText(sb.toString());
                    etCurrentHp.setText(String.valueOf(currentHp));
                    etMaxHp.setText(String.valueOf(maxHp));
                });
            }
            if (c != null && !c.isClosed()) c.close();
        });
    }

    private int parseIntSafe(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        exec.shutdown();
    }
}
