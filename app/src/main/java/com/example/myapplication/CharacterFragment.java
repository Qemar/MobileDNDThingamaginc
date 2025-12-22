package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CharacterFragment extends Fragment {

    private TextView tvName;
    private TextView tvStr, tvDex, tvCon, tvInt, tvWis, tvCha;
    private Spinner spinnerClass;
    private Button btnEditName;

    // class details views
    private TextView tvClassTitle, tvHitDice, tvHPAtFirst, tvProficiencies, tvSaves, tvEquipment;
    private TextView tvSkillPrompt;
    private View skillsContainer;
    private CheckBox cbAcrobatics, cbAthletics, cbPerception, cbSurvival, cbIntimidation, cbHistory, cbInsight, cbAnimalHandling;

    // equipment radio groups
    private RadioGroup rgArmor, rgWeapon1, rgWeapon2, rgPack;
    private RadioButton rbArmorA, rbArmorB, rbWeapon1A, rbWeapon1B, rbWeapon2A, rbWeapon2B, rbPackA, rbPackB;

    // background
    private Spinner spinnerBackground;
    private TextView tvBackgroundEquipment;

    // stats
    private int STR = 8, DEX = 8, CON = 8, INTEL = 8, WIS = 8, CHA = 8;
    private String RACE = "";
    private String SUBRACE = "";
    private String currentClass = null;

    private final int PROF_BONUS = 2; // level 1

    // background-granted skills set (strings equal to checkbox variable names)
    // We'll use skill keys like "Athletics", "Intimidation", etc.
    private final Set<String> backgroundGranted = new HashSet<>();

    public CharacterFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_character, container, false);

        // find views
        tvName = root.findViewById(R.id.tvCharacterName);
        btnEditName = root.findViewById(R.id.btnEditName);

        tvStr = root.findViewById(R.id.tvCharStr);
        tvDex = root.findViewById(R.id.tvCharDex);
        tvCon = root.findViewById(R.id.tvCharCon);
        tvInt = root.findViewById(R.id.tvCharInt);
        tvWis = root.findViewById(R.id.tvCharWis);
        tvCha = root.findViewById(R.id.tvCharCha);

        spinnerClass = root.findViewById(R.id.spinnerClass);

        tvClassTitle = root.findViewById(R.id.tvClassTitle);
        tvHitDice = root.findViewById(R.id.tvHitDice);
        tvHPAtFirst = root.findViewById(R.id.tvHPAtFirst);
        tvProficiencies = root.findViewById(R.id.tvProficiencies);
        tvSaves = root.findViewById(R.id.tvSaves);
        tvEquipment = root.findViewById(R.id.tvEquipment);

        tvSkillPrompt = root.findViewById(R.id.tvSkillPrompt);
        skillsContainer = root.findViewById(R.id.skillsContainer);

        cbAcrobatics = root.findViewById(R.id.cbAcrobatics);
        cbAthletics = root.findViewById(R.id.cbAthletics);
        cbPerception = root.findViewById(R.id.cbPerception);
        cbSurvival = root.findViewById(R.id.cbSurvival);
        cbIntimidation = root.findViewById(R.id.cbIntimidation);
        cbHistory = root.findViewById(R.id.cbHistory);
        cbInsight = root.findViewById(R.id.cbInsight);
        cbAnimalHandling = root.findViewById(R.id.cbAnimalHandling);

        rgArmor = root.findViewById(R.id.rgArmor);
        rgWeapon1 = root.findViewById(R.id.rgWeapon1);
        rgWeapon2 = root.findViewById(R.id.rgWeapon2);
        rgPack = root.findViewById(R.id.rgPack);

        rbArmorA = root.findViewById(R.id.rbArmorA);
        rbArmorB = root.findViewById(R.id.rbArmorB);
        rbWeapon1A = root.findViewById(R.id.rbWeapon1A);
        rbWeapon1B = root.findViewById(R.id.rbWeapon1B);
        rbWeapon2A = root.findViewById(R.id.rbWeapon2A);
        rbWeapon2B = root.findViewById(R.id.rbWeapon2B);
        rbPackA = root.findViewById(R.id.rbPackA);
        rbPackB = root.findViewById(R.id.rbPackB);

        spinnerBackground = root.findViewById(R.id.spinnerBackground);
        tvBackgroundEquipment = root.findViewById(R.id.tvBackgroundEquipment);

        // default name
        tvName.setText(getString(R.string.unnamed_character));
        Button btnOpenSave = root.findViewById(R.id.btnOpenSave);
        btnOpenSave.setOnClickListener(v -> {
            Intent i = new Intent(requireContext(), SaveActivity.class);

            // передаём простые данные
            i.putExtra("NAME", tvName.getText().toString());
            i.putExtra("RACE", RACE);
            i.putExtra("SUBRACE", SUBRACE);
            i.putExtra("CLASS", currentClass != null ? currentClass : "");
            i.putExtra("BACKGROUND", spinnerBackground.getSelectedItem() != null ? spinnerBackground.getSelectedItem().toString() : "");
            i.putExtra("STR", STR);
            i.putExtra("DEX", DEX);
            i.putExtra("CON", CON);
            i.putExtra("INT", INTEL);
            i.putExtra("WIS", WIS);
            i.putExtra("CHA", CHA);

            // skills -> как массив строк ключей
            ArrayList<String> skills = new ArrayList<>();
            CheckBox[] all = {cbAcrobatics, cbAthletics, cbPerception, cbSurvival, cbIntimidation, cbHistory, cbInsight, cbAnimalHandling};
            for (CheckBox cb : all) {
                if (cb.isChecked()) skills.add(checkboxKey(cb));
            }
            i.putStringArrayListExtra("SKILLS", skills);

            // экипировка - сохраняем варианты A/B и итоговый текст
            i.putExtra("EQUIP_ARMOR", rbArmorA.isChecked() ? "A" : "B");
            i.putExtra("EQUIP_WEAPON1", rbWeapon1A.isChecked() ? "A" : "B");
            i.putExtra("EQUIP_WEAPON2", rbWeapon2A.isChecked() ? "A" : "B");
            i.putExtra("EQUIP_PACK", rbPackA.isChecked() ? "A" : "B");
            i.putExtra("EQUIP_TEXT", tvEquipment.getText().toString() + "\n\n" + tvBackgroundEquipment.getText().toString());

            startActivity(i);
        });
        // read extras
        Bundle extras = getActivity() != null ? getActivity().getIntent().getExtras() : null;
        if (extras != null) {
            STR = extras.getInt("STR", STR);
            DEX = extras.getInt("DEX", DEX);
            CON = extras.getInt("CON", CON);
            INTEL = extras.getInt("INT", INTEL);
            WIS = extras.getInt("WIS", WIS);
            CHA = extras.getInt("CHA", CHA);
            RACE = extras.getString("RACE", "");
            SUBRACE = extras.getString("SUBRACE", "");
        }

        // initial render of stats (will include save modifiers after class selection)
        refreshStatsView();

        // classes: используем строковый массив из ресурсов
        ArrayAdapter<CharSequence> classAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.classes_array,
                android.R.layout.simple_spinner_item
        );
        classAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClass.setAdapter(classAdapter);
        spinnerClass.setSelection(0);


        spinnerClass.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String cls = (String) parent.getItemAtPosition(position);
                onClassSelected(cls);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // background: используем строковый массив из ресурсов
        ArrayAdapter<CharSequence> bgAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.backgrounds_array,
                android.R.layout.simple_spinner_item
        );
        bgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBackground.setAdapter(bgAdapter);
        spinnerBackground.setSelection(0);

        spinnerBackground.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String bg = (String) parent.getItemAtPosition(position);
                applyBackground(bg);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });

        // edit name
        btnEditName.setOnClickListener(v -> {
            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Имя персонажа");

            new AlertDialog.Builder(requireContext())
                    .setTitle("Введите имя персонажа")
                    .setView(input)
                    .setPositiveButton("OK", (dialog, which) -> {
                        String name = input.getText().toString().trim();
                        if (name.isEmpty()) name = "Безымянный Персонаж";
                        tvName.setText(name);
                    })
                    .setNegativeButton("Отмена", null)
                    .show();
        });

        // skill checkboxes limiter and label updater
        CompoundButton.OnCheckedChangeListener skillLimiter = (buttonView, isChecked) -> {
            recalcSkillLimits();
            updateSkillLabels();
        };

        cbAcrobatics.setOnCheckedChangeListener(skillLimiter);
        cbAthletics.setOnCheckedChangeListener(skillLimiter);
        cbPerception.setOnCheckedChangeListener(skillLimiter);
        cbSurvival.setOnCheckedChangeListener(skillLimiter);
        cbIntimidation.setOnCheckedChangeListener(skillLimiter);
        cbHistory.setOnCheckedChangeListener(skillLimiter);
        cbInsight.setOnCheckedChangeListener(skillLimiter);
        cbAnimalHandling.setOnCheckedChangeListener(skillLimiter);

        // radio groups -> update equipment text
        RadioGroup.OnCheckedChangeListener rgListener = (group, checkedId) -> updateEquipmentText();
        rgArmor.setOnCheckedChangeListener(rgListener);
        rgWeapon1.setOnCheckedChangeListener(rgListener);
        rgWeapon2.setOnCheckedChangeListener(rgListener);
        rgPack.setOnCheckedChangeListener(rgListener);

        // initially hide class details until class selected
        setClassDetailsVisible(false);

        return root;
    }

    private void setClassDetailsVisible(boolean visible) {
        int v = visible ? View.VISIBLE : View.GONE;
        tvClassTitle.setVisibility(v);
        tvHitDice.setVisibility(v);
        tvHPAtFirst.setVisibility(v);
        tvProficiencies.setVisibility(v);
        tvSaves.setVisibility(v);
        tvEquipment.setVisibility(v);
        tvSkillPrompt.setVisibility(v);
        skillsContainer.setVisibility(v);
        rgArmor.setVisibility(v);
        rgWeapon1.setVisibility(v);
        rgWeapon2.setVisibility(v);
        rgPack.setVisibility(v);
    }

    // Refresh top stat displays — now includes saving-throw bonus for each stat
    private void refreshStatsView() {
        tvStr.setText(formatStatWithSave(STR, isSaveProficientFor("STR")));
        tvDex.setText(formatStatWithSave(DEX, isSaveProficientFor("DEX")));
        tvCon.setText(formatStatWithSave(CON, isSaveProficientFor("CON")));
        tvInt.setText(formatStatWithSave(INTEL, isSaveProficientFor("INT")));
        tvWis.setText(formatStatWithSave(WIS, isSaveProficientFor("WIS")));
        tvCha.setText(formatStatWithSave(CHA, isSaveProficientFor("CHA")));
    }

    private String formatStatWithSave(int val, boolean saveProf) {
        int mod = abilityModifier(val);
        int saveTotal = mod + (saveProf ? PROF_BONUS : 0);
        String modStr = mod >= 0 ? "+" + mod : String.valueOf(mod);
        String saveStr = saveTotal >= 0 ? "+" + saveTotal : String.valueOf(saveTotal);
        String profTag = saveProf ? "" : "";
        return val + " (" + modStr + ") — " + saveStr + profTag;
    }

    private int abilityModifier(int score) {
        return (int) Math.floor((score - 10) / 2.0);
    }

    private void onClassSelected(String cls) {
        currentClass = (cls != null && !cls.equals("Выберите класс")) ? cls : null;

        if (currentClass == null) {
            setClassDetailsVisible(false);
            recalcSkillLimits();
            refreshStatsView();
            return;
        }

        if ("Воин".equalsIgnoreCase(currentClass)) {
            setClassDetailsVisible(true);
            tvClassTitle.setText(getString(R.string.fighter_title)); // добавь строку в resources, см. ниже
            tvHitDice.setText(getString(R.string.hit_dice_label) + " d10");
            int conMod = abilityModifier(CON);
            int hpAt1 = 10 + conMod;
            if ("Холмовой".equalsIgnoreCase(SUBRACE)) hpAt1 += 1;
            tvHPAtFirst.setText(getString(R.string.health_1st) + hpAt1);

            tvProficiencies.setText(getString(R.string.fighter_proficiencies)); // ресурс
            tvSaves.setText(getString(R.string.saves_label) + (getString(R.string.fighter_saves)));

            // do not clear existing skill selections — class can add up to 2 non-background skills
            recalcSkillLimits();

            // initial equipment defaults
            rgArmor.check(rbArmorA.getId());
            rgWeapon1.check(rbWeapon1A.getId());
            rgWeapon2.check(rbWeapon2A.getId());
            rgPack.check(rbPackA.getId());

            updateEquipmentText();

            // update stats so top stat save displays reflect class proficiencies
            refreshStatsView();
        }
    }

    // recalc how many skills can still be chosen by class (classLimit = 2)
    // Important: background-granted skills are NOT counted against classLimit.
    private void recalcSkillLimits() {
        CheckBox[] all = {cbAcrobatics, cbAthletics, cbPerception, cbSurvival, cbIntimidation, cbHistory, cbInsight, cbAnimalHandling};

        // count how many class-selected skills (checked but NOT background-granted)
        int classSelected = 0;
        for (CheckBox cb : all) {
            String key = checkboxKey(cb);
            boolean isBg = backgroundGranted.contains(key);
            if (cb.isChecked() && !isBg) classSelected++;
        }

        int classLimit = 2;
        int remainingForClass = Math.max(0, classLimit - classSelected);

        // enable unchecked boxes only if they are not background-granted and there is remaining slot
        for (CheckBox cb : all) {
            String key = checkboxKey(cb);
            boolean isBg = backgroundGranted.contains(key);
            if (isBg) {
                // background-granted skills stay checked and disabled (cannot uncheck)
                cb.setChecked(true);
                cb.setEnabled(false);
            } else {
                // if not checked -> enable only if there is remainingForClass
                if (!cb.isChecked()) {
                    cb.setEnabled(remainingForClass > 0);
                } else {
                    // checked by class -> keep enabled so user can uncheck to free slot
                    cb.setEnabled(true);
                }
            }
        }
    }

    // update skill labels (show attribute and total bonus)
    private void updateSkillLabels() {
        setCbText(cbAthletics, "Атлетика", "Сила", abilityModifier(STR), cbAthletics.isChecked());
        setCbText(cbAcrobatics, "Акробатика", "Ловкость", abilityModifier(DEX), cbAcrobatics.isChecked());
        setCbText(cbPerception, "Восприятие", "Мудрость", abilityModifier(WIS), cbPerception.isChecked());
        setCbText(cbSurvival, "Выживание", "Мудрость", abilityModifier(WIS), cbSurvival.isChecked());
        setCbText(cbIntimidation, "Запугивание", "Харизма", abilityModifier(CHA), cbIntimidation.isChecked());
        setCbText(cbHistory, "История", "Интеллект", abilityModifier(INTEL), cbHistory.isChecked());
        setCbText(cbInsight, "Проницательность", "Мудрость", abilityModifier(WIS), cbInsight.isChecked());
        setCbText(cbAnimalHandling, "Уход за животными", "Мудрость", abilityModifier(WIS), cbAnimalHandling.isChecked());
    }

    private void setCbText(CheckBox cb, String skillRu, String attrRu, int attrMod, boolean proficient) {
        int total = attrMod + (proficient ? PROF_BONUS : 0);
        String sign = total >= 0 ? "+" + total : String.valueOf(total);
        cb.setText(skillRu + " (" + attrRu + ") - " + sign);
    }

    // Applies background (предыстория) bonuses and equipment
    // IMPORTANT CHANGE: do NOT clear user's current selections. Instead mark background skills as granted,
    // check and lock them, and leave other checkboxes intact so class can add its 2 skills.
    private void applyBackground(String bg) {
        // --- Сброс предыдущих фоновых грантов (только "фоновые", не пользовательские отметки) ---
        // Сначала восстановим все чекбоксы в enabled = true (они могут быть заблокированы предыдущим фоном)
        CheckBox[] all = {cbAcrobatics, cbAthletics, cbPerception, cbSurvival, cbIntimidation, cbHistory, cbInsight, cbAnimalHandling};
        for (CheckBox cb : all) {
            cb.setEnabled(true);
        }

        // Удаляем старые записи о фоновых бонусах
        backgroundGranted.clear();

        tvBackgroundEquipment.setText("");

        // Защита: если spinnerBackground ещё не готов (null) — выйдем
        if (spinnerBackground == null) return;

        // Определим позицию выбранной опции — это надёжнее, чем сравнивать строки
        int pos = spinnerBackground.getSelectedItemPosition();
        // у нас в массиве backgrounds_array позиция 0 = "Выберите предысторию", 1 = "Солдат"
        if (pos == 1) { // выбран "Солдат"
            // добавляем фоновые гранты (ключи должны совпадать с checkboxKey(...))
            backgroundGranted.add("Athletics");
            backgroundGranted.add("Intimidation");

            // отмечаем их (если пользователь ещё не отметил) и блокируем
            cbAthletics.setChecked(true);
            cbAthletics.setEnabled(false);

            cbIntimidation.setChecked(true);
            cbIntimidation.setEnabled(false);

            // текст предыстории и инструменты берем из ресурсов, если есть
            String tools = getString(R.string.bg_soldier_tools);
            String equip = getString(R.string.bg_soldier_equipment);
            tvBackgroundEquipment.setText(tools + "\n\n" + equip);
        } else {
            // если выбрали "Выберите предысторию" или другую — ничего не даём
            tvBackgroundEquipment.setText("");
        }

        // после любых изменений пересчитаем доступные слоты класса и подписи
        recalcSkillLimits();
        updateSkillLabels();
    }


    private void updateEquipmentText() {
        StringBuilder sb = new StringBuilder();
        sb.append("Экипировка класса (выбранные варианты):\n");

        // armor
        if (rbArmorA.isChecked()) {
            sb.append("- Кольчуга\n");
        } else if (rbArmorB.isChecked()) {
            sb.append("- Кожаный доспех, длинный лук и 20 стрел\n");
        }

        // weapon1
        if (rbWeapon1A.isChecked()) {
            sb.append("- Воинское оружие и щит\n");
        } else if (rbWeapon1B.isChecked()) {
            sb.append("- Два воинских оружия\n");
        }

        // weapon2
        if (rbWeapon2A.isChecked()) {
            sb.append("- Лёгкий арбалет и 20 болтов\n");
        } else if (rbWeapon2B.isChecked()) {
            sb.append("- Два ручных топора\n");
        }

        // pack
        if (rbPackA.isChecked()) {
            sb.append("- Набор исследователя подземелий\n");
        } else if (rbPackB.isChecked()) {
            sb.append("- Набор путешественника\n");
        }

        tvEquipment.setText(sb.toString());
    }

    // Helper: returns skill key string for the checkbox
    private String checkboxKey(CheckBox cb) {
        if (cb == cbAthletics) return "Athletics";
        if (cb == cbAcrobatics) return "Acrobatics";
        if (cb == cbPerception) return "Perception";
        if (cb == cbSurvival) return "Survival";
        if (cb == cbIntimidation) return "Intimidation";
        if (cb == cbHistory) return "History";
        if (cb == cbInsight) return "Insight";
        if (cb == cbAnimalHandling) return "AnimalHandling";
        return "";
    }

    // Helper: checks whether a given ability has save proficiency (depends on currentClass)
    private boolean isSaveProficientFor(String abilityKey) {
        if (currentClass == null) return false;
        if ("Воин".equalsIgnoreCase(currentClass)) {
            return abilityKey.equalsIgnoreCase("STR") || abilityKey.equalsIgnoreCase("CON");
        }
        return false;
    }

    private String formatSigned(int v) {
        return v >= 0 ? "+" + v : String.valueOf(v);
    }
}
