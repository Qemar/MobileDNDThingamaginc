package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.data.CharacterContract.CharacterEntry;
import com.example.myapplication.data.CharacterDbHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaveFragment extends Fragment {

    private CharacterDbHelper dbHelper;
    private ExecutorService dbExecutor;
    private TextView tvSummary;
    private Button btnSave, btnCancel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new CharacterDbHelper(requireContext());
        dbExecutor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_save_character, container, false);

        tvSummary = root.findViewById(R.id.tvSaveSummary);
        btnSave = root.findViewById(R.id.btnDoSave);
        btnCancel = root.findViewById(R.id.btnCancelSave);

        // составим краткий summary из intent extras
        // внутри SaveFragment.java (onCreateView)
        Bundle extras = requireActivity().getIntent().getExtras();
        if (extras != null) {
            String name = extras.getString("NAME", "Безымянный Персонаж");
            String race = extras.getString("RACE", "");
            String subrace = extras.getString("SUBRACE", "");
            String cls = extras.getString("CLASS", "");
            int str = extras.getInt("STR", 8);
            int dex = extras.getInt("DEX", 8);
            int con = extras.getInt("CON", 8);
            int intl = extras.getInt("INT", 8);
            int wis = extras.getInt("WIS", 8);
            int cha = extras.getInt("CHA", 8);
            ArrayList<String> skills = extras.getStringArrayList("SKILLS");
            String equipText = extras.getString("EQUIP_TEXT", "");

            StringBuilder sb = new StringBuilder();
            sb.append(name).append("\n");
            sb.append(race);
            if (subrace != null && !subrace.isEmpty()) sb.append(" (").append(subrace).append(")");
            sb.append("\nClass: ").append(cls).append("\n\n");
            sb.append("STR ").append(str).append(", DEX ").append(dex).append(", CON ").append(con).append("\n");
            sb.append("INT ").append(intl).append(", WIS ").append(wis).append(", CHA ").append(cha).append("\n\n");
            sb.append("Skills: ");
            if (skills != null && !skills.isEmpty()) sb.append(skills.toString());
            else sb.append("—");
            sb.append("\n\nEquipment summary:\n").append(equipText);

            tvSummary.setText(sb.toString());
        }

// УДАЛЕНО: преждевременный переход в MainActivity — он не должен быть здесь!

        btnCancel.setOnClickListener(v -> requireActivity().finish());

        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            // собираем ContentValues и сохраняем в фоне
            final ContentValues cv = buildContentValuesFromIntent();
            dbExecutor.execute(() -> {
                long id = dbHelper.insertCharacter(cv);
                requireActivity().runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    if (id > 0) {
                        Toast.makeText(requireContext(), "Персонаж сохранён (id=" + id + ")", Toast.LENGTH_SHORT).show();

                        // После успешного сохранения — возвращаемся в MainActivity (калькулятор).
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        // флаги, чтобы не дублировать Activity в стеке
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        requireActivity().finish(); // закрываем SaveActivity
                    } else {
                        Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        return root;
    }

    private ContentValues buildContentValuesFromIntent() {
        ContentValues cv = new ContentValues();
        Bundle extras = requireActivity().getIntent().getExtras();
        if (extras == null) return cv;

        String name = extras.getString("NAME", "");
        String race = extras.getString("RACE", "");
        String subrace = extras.getString("SUBRACE", "");
        String cls = extras.getString("CLASS", "");
        int str = extras.getInt("STR", 8);
        int dex = extras.getInt("DEX", 8);
        int con = extras.getInt("CON", 8);
        int intl = extras.getInt("INT", 8);
        int wis = extras.getInt("WIS", 8);
        int cha = extras.getInt("CHA", 8);

        cv.put(CharacterEntry.COLUMN_NAME, name);
        cv.put(CharacterEntry.COLUMN_RACE, race);
        cv.put(CharacterEntry.COLUMN_SUBRACE, subrace);
        cv.put(CharacterEntry.COLUMN_CLASS, cls);
        cv.put(CharacterEntry.COLUMN_BACKGROUND, extras.getString("BACKGROUND", ""));
        cv.put(CharacterEntry.COLUMN_STR, str);
        cv.put(CharacterEntry.COLUMN_DEX, dex);
        cv.put(CharacterEntry.COLUMN_CON, con);
        cv.put(CharacterEntry.COLUMN_INT, intl);
        cv.put(CharacterEntry.COLUMN_WIS, wis);
        cv.put(CharacterEntry.COLUMN_CHA, cha);

        // skills -> CSV
        ArrayList<String> skills = extras.getStringArrayList("SKILLS");
        String skillsCsv = "";
        if (skills != null && !skills.isEmpty()) {
            skillsCsv = android.text.TextUtils.join(",", skills);
        }
        cv.put(CharacterEntry.COLUMN_SKILLS, skillsCsv);

        // equip choices and text
        cv.put(CharacterEntry.COLUMN_EQUIP_ARMOR, extras.getString("EQUIP_ARMOR", "A"));
        cv.put(CharacterEntry.COLUMN_EQUIP_WEAPON1, extras.getString("EQUIP_WEAPON1", "A"));
        cv.put(CharacterEntry.COLUMN_EQUIP_WEAPON2, extras.getString("EQUIP_WEAPON2", "A"));
        cv.put(CharacterEntry.COLUMN_EQUIP_PACK, extras.getString("EQUIP_PACK", "A"));
        cv.put(CharacterEntry.COLUMN_EQUIP_TEXT, extras.getString("EQUIP_TEXT", ""));

        // ------------------ HP logic ------------------
        // Консервативный способ: вычислим модификатор телосложения
        int conMod = (int) Math.floor((con - 10) / 2.0);

        int baseHp;
        if (cls != null && cls.equalsIgnoreCase("Воин")) {
            baseHp = 10; // воин на 1 уровне даёт d10
        } else {
            baseHp = 8; // по умолчанию (например для других классов) используем d8
        }

        int maxHp = baseHp + conMod;
        // учёт Холмового дворфа (+1 к max hp)
        if (subrace != null && subrace.equalsIgnoreCase("Холмовой")) {
            maxHp += 1;
        }
        if (maxHp < 1) maxHp = 1; // защита: минимум 1 HP

        int currentHp = maxHp; // по умолчанию при создании текущие хиты = максимальным

        cv.put(CharacterEntry.COLUMN_MAX_HP, maxHp);
        cv.put(CharacterEntry.COLUMN_CURRENT_HP, currentHp);

        cv.put(CharacterEntry.COLUMN_CREATED_TS, System.currentTimeMillis());
        return cv;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
