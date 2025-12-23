package com.example.myapplication;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
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

    private Long editCharId = null;
    private boolean isEditMode = false;
    // поля для хранения загруженных значений
    private String loadedName, loadedRace, loadedSubrace, loadedClassStr, loadedEquipText;
    private int loadedSTR, loadedDEX, loadedCON, loadedINT, loadedWIS, loadedCHA, loadedMaxHp,loadedCurrentHp;
    private String loadedSkillsCsv;

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
        // В onCreateView(): определяем editCharId и при необходимости загружаем запись
        Bundle extras = requireActivity().getIntent().getExtras();
        if (extras != null && extras.containsKey("CHAR_ID")) {
            editCharId = extras.getLong("CHAR_ID", -1);
            if (editCharId != null && editCharId > 0) {
                isEditMode = true;
                // загрузим запись в фоне
                dbExecutor.execute(() -> {
                    Cursor c = dbHelper.queryById(editCharId);
                    if (c != null && c.moveToFirst()) {
                        loadedName = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_NAME));
                        loadedRace = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_RACE));
                        loadedSubrace = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_SUBRACE));
                        loadedClassStr = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CLASS));
                        loadedSTR = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_STR));
                        loadedDEX = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_DEX));
                        loadedCON = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CON));
                        loadedINT = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_INT));
                        loadedWIS = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_WIS));
                        loadedCHA = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CHA));
                        loadedSkillsCsv = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_SKILLS));
                        loadedEquipText = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_EQUIP_TEXT));
                        int mx = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_MAX_HP));
                        loadedMaxHp = (mx > 0) ? mx : null;
                        int cur = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CURRENT_HP));
                        loadedCurrentHp = (cur > 0) ? cur : null;
                        c.close();

                        requireActivity().runOnUiThread(() -> {
                            // покажем summary на UI (пример)
                            StringBuilder sb = new StringBuilder();
                            sb.append(loadedName).append("\n");
                            sb.append(loadedRace);
                            if (loadedSubrace != null && !loadedSubrace.isEmpty()) sb.append(" (").append(loadedSubrace).append(")");
                            sb.append("\nClass: ").append(loadedClassStr).append("\n\n");
                            sb.append("STR ").append(loadedSTR).append(", DEX ").append(loadedDEX).append(", CON ").append(loadedCON).append("\n");
                            sb.append("INT ").append(loadedINT).append(", WIS ").append(loadedWIS).append(", CHA ").append(loadedCHA).append("\n\n");
                            sb.append("Skills: ").append(loadedSkillsCsv == null ? "—" : loadedSkillsCsv).append("\n\n");
                            sb.append("Equipment summary:\n").append(loadedEquipText == null ? "" : loadedEquipText);
                            tvSummary.setText(sb.toString());
                        });
                    }
                });
            }
        }


        btnCancel.setOnClickListener(v -> requireActivity().finish());

        // в btnSave.onClick: используем update или insert
        btnSave.setOnClickListener(v -> {
            btnSave.setEnabled(false);
            final ContentValues cv = buildContentValuesFromIntentOrLoaded(); // изменим метод чуть ниже
            dbExecutor.execute(() -> {
                if (isEditMode && editCharId != null && editCharId > 0) {
                    int rows = dbHelper.updateCharacter(editCharId, cv);
                    requireActivity().runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        if (rows > 0) {
                            Toast.makeText(requireContext(), "Персонаж обновлён", Toast.LENGTH_SHORT).show();
                            // вернёмся в список/калькулятор
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), "Ошибка обновления", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    long id = dbHelper.insertCharacter(cv);
                    requireActivity().runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        if (id > 0) {
                            Toast.makeText(requireContext(), "Персонаж сохранён (id=" + id + ")", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(requireContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(intent);
                            requireActivity().finish();
                        } else {
                            Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });

        return root;
    }

    private ContentValues buildContentValuesFromIntentOrLoaded() {
        ContentValues cv = new ContentValues();
        Bundle extras = requireActivity().getIntent().getExtras();

        // Helper to pick loaded value in edit mode, otherwise from extras (with default)
        // assumes loaded* fields exist in the fragment (may be null / zero)
        String name = (isEditMode && loadedName != null) ? loadedName
                : (extras != null ? extras.getString("NAME", "") : "");
        String race = (isEditMode && loadedRace != null) ? loadedRace
                : (extras != null ? extras.getString("RACE", "") : "");
        String subrace = (isEditMode && loadedSubrace != null) ? loadedSubrace
                : (extras != null ? extras.getString("SUBRACE", "") : "");
        String cls = (isEditMode && loadedClassStr != null) ? loadedClassStr
                : (extras != null ? extras.getString("CLASS", "") : "");

        // stats: when editing, prefer loaded numeric values; otherwise take from extras (or defaults)
        int str = isEditMode ? loadedSTR : (extras != null ? extras.getInt("STR", 8) : 8);
        int dex = isEditMode ? loadedDEX : (extras != null ? extras.getInt("DEX", 8) : 8);
        int con = isEditMode ? loadedCON : (extras != null ? extras.getInt("CON", 8) : 8);
        int intl = isEditMode ? loadedINT : (extras != null ? extras.getInt("INT", 8) : 8);
        int wis = isEditMode ? loadedWIS : (extras != null ? extras.getInt("WIS", 8) : 8);
        int cha = isEditMode ? loadedCHA : (extras != null ? extras.getInt("CHA", 8) : 8);

        cv.put(CharacterEntry.COLUMN_NAME, name);
        cv.put(CharacterEntry.COLUMN_RACE, race);
        cv.put(CharacterEntry.COLUMN_SUBRACE, subrace);
        cv.put(CharacterEntry.COLUMN_CLASS, cls);
        cv.put(CharacterEntry.COLUMN_BACKGROUND, (extras != null ? extras.getString("BACKGROUND", "") : ""));

        cv.put(CharacterEntry.COLUMN_STR, str);
        cv.put(CharacterEntry.COLUMN_DEX, dex);
        cv.put(CharacterEntry.COLUMN_CON, con);
        cv.put(CharacterEntry.COLUMN_INT, intl);
        cv.put(CharacterEntry.COLUMN_WIS, wis);
        cv.put(CharacterEntry.COLUMN_CHA, cha);

        // skills: prefer extras SKILLS (array) if present, otherwise loaded CSV when editing
        String skillsCsv = "";
        if (extras != null && extras.containsKey("SKILLS")) {
            ArrayList<String> skills = extras.getStringArrayList("SKILLS");
            if (skills != null && !skills.isEmpty()) {
                skillsCsv = android.text.TextUtils.join(",", skills);
            }
        } else if (isEditMode && loadedSkillsCsv != null) {
            skillsCsv = loadedSkillsCsv;
        }
        cv.put(CharacterEntry.COLUMN_SKILLS, skillsCsv);

        // equipment: prefer explicit extras for choices; fallback to loaded values if editing
//        String equipArmor = (extras != null && extras.containsKey("EQUIP_ARMOR"))
//                ? extras.getString("EQUIP_ARMOR", "A")
//                : (isEditMode && loadedEquipArmor != null ? loadedEquipArmor : "A");
//
//        String equipWeapon1 = (extras != null && extras.containsKey("EQUIP_WEAPON1"))
//                ? extras.getString("EQUIP_WEAPON1", "A")
//                : (isEditMode && loadedEquipWeapon1 != null ? loadedEquipWeapon1 : "A");
//
//        String equipWeapon2 = (extras != null && extras.containsKey("EQUIP_WEAPON2"))
//                ? extras.getString("EQUIP_WEAPON2", "A")
//                : (isEditMode && loadedEquipWeapon2 != null ? loadedEquipWeapon2 : "A");
//
//        String equipPack = (extras != null && extras.containsKey("EQUIP_PACK"))
//                ? extras.getString("EQUIP_PACK", "A")
//                : (isEditMode && loadedEquipPack != null ? loadedEquipPack : "A");

        String equipText = (extras != null && extras.containsKey("EQUIP_TEXT"))
                ? extras.getString("EQUIP_TEXT", "")
                : (isEditMode && loadedEquipText != null ? loadedEquipText : "");

//        cv.put(CharacterEntry.COLUMN_EQUIP_ARMOR, equipArmor);
//        cv.put(CharacterEntry.COLUMN_EQUIP_WEAPON1, equipWeapon1);
//        cv.put(CharacterEntry.COLUMN_EQUIP_WEAPON2, equipWeapon2);
//        cv.put(CharacterEntry.COLUMN_EQUIP_PACK, equipPack);
        cv.put(CharacterEntry.COLUMN_EQUIP_TEXT, equipText);

        // ------------------ HP logic ------------------
        // Priority:
        // 1) if extras contains MAX_HP/CURRENT_HP -> use them
        // 2) else if edit mode and loaded values exist -> reuse them
        // 3) else compute defaults (base HP by class + con mod; hill dwarf +1), current = max

        Integer maxHpFromExtras = (extras != null && extras.containsKey("MAX_HP")) ? extras.getInt("MAX_HP") : null;
        Integer curHpFromExtras = (extras != null && extras.containsKey("CURRENT_HP")) ? extras.getInt("CURRENT_HP") : null;

        Integer maxHp = null;
        Integer curHp = null;

        if (maxHpFromExtras != null) maxHp = maxHpFromExtras;
        if (curHpFromExtras != null) curHp = curHpFromExtras;

        // try loaded (only if editing and loaded fields are available)
        if (isEditMode) {
            if (maxHp == null ) maxHp = loadedMaxHp;
            if (curHp == null ) curHp = loadedCurrentHp;
        }

        // else compute default
        if (maxHp == null) {
            int conMod = (int) Math.floor((con - 10) / 2.0);
            int baseHp = (cls != null && cls.equalsIgnoreCase("Воин")) ? 10 : 8;
            int computed = baseHp + conMod;
            if (subrace != null && subrace.equalsIgnoreCase("Холмовой")) computed += 1;
            if (computed < 1) computed = 1;
            maxHp = computed;
        }
        if (curHp == null) {
            curHp = maxHp;
        }

        cv.put(CharacterEntry.COLUMN_MAX_HP, maxHp);
        cv.put(CharacterEntry.COLUMN_CURRENT_HP, curHp);

        // created_ts: if creating new record - put now; if editing - don't overwrite existing created_ts
        if (!isEditMode) {
            cv.put(CharacterEntry.COLUMN_CREATED_TS, System.currentTimeMillis());
        }

        return cv;
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdown();
    }
}
