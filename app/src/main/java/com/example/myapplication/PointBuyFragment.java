package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Button;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Map;

public class PointBuyFragment extends Fragment {

    private final int POINT_BUDGET = 27;

    private int[] base = {8,8,8,8,8,8}; // STR, DEX, CON, INT, WIS, CHA

    // бонусы: итоговый (raceBase + subraceBonus)
    private int[] racial = {0,0,0,0,0,0};
    private int[] raceBase = {0,0,0,0,0,0};
    private int[] subraceBonus = {0,0,0,0,0,0};

    // UI
    private NumberPicker npStr, npDex, npCon, npInt, npWis, npCha;
    private TextView tvStrRacial, tvDexRacial, tvConRacial, tvIntRacial, tvWisRacial, tvChaRacial;
    private TextView tvStrTotal, tvDexTotal, tvConTotal, tvIntTotal, tvWisTotal, tvChaTotal;
    private TextView tvStrMod, tvDexMod, tvConMod, tvIntMod, tvWisMod, tvChaMod;
    private TextView tvStrCost, tvDexCost, tvConCost, tvIntCost, tvWisCost, tvChaCost;
    private TextView tvUsedPoints;
    private Spinner spinnerRace;
    private Spinner spinnerSubrace;
    private TextView tvSubracePrompt;
    private TextView tvRaceDescription;
    private int defaultUsedColor;

    // data
    private Map<String,int[]> races = new LinkedHashMap<>();
    private Map<String,String[]> subraceOptions = new LinkedHashMap<>();
    private Map<String,int[]> subraceBonuses = new LinkedHashMap<>();
    private Map<String, Integer> raceDescriptions = new LinkedHashMap<>();
    private Map<String, Integer> subraceDescriptions = new LinkedHashMap<>();


    public PointBuyFragment() { }
    public interface OnSelectionChangedListener {
        void onSelectionChanged(boolean isComplete);
    }

    private OnSelectionChangedListener selectionListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectionChangedListener) {
            selectionListener = (OnSelectionChangedListener) context;
        } else {
            selectionListener = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectionListener = null;
    }
    // возвращает true если можно переходить дальше (раса выбрана, и если есть подраса — она выбрана)
    public boolean isSelectionComplete() {
        // view ещё не создана — не готово
        if (spinnerRace == null) return false;

        Object raceObj = spinnerRace.getSelectedItem();
        if (raceObj == null) return false;
        String mainRace = raceObj.toString();
        if (mainRace.equalsIgnoreCase("Выберите")) return false;

        if (subraceOptions.containsKey(mainRace)) {
            if (spinnerSubrace == null || spinnerSubrace.getVisibility() != View.VISIBLE) return false;
            Object subObj = spinnerSubrace.getSelectedItem();
            if (subObj == null) return false;
            String subName = subObj.toString();
            if (subName.equalsIgnoreCase("Выберите подрасу") || subName.trim().isEmpty()) return false;
        }
        return true;
    }


    public Intent buildCharacterIntent(Context ctx) {
        // проверка: выбрана ли основная раса (в нашем списке первый элемент — "Выберите")
        Object raceObj = spinnerRace.getSelectedItem();
        if (raceObj == null) return null;
        String mainRace = raceObj.toString();
        if (mainRace.equalsIgnoreCase("Выберите")) return null;

        // если у этой расы есть подрасы — проверим что пользовател выбрал одну
        if (subraceOptions.containsKey(mainRace)) {
            // spinnerSubrace должен быть видим и иметь выбранный элемент (и не быть placeholder'ом)
            if (spinnerSubrace == null || spinnerSubrace.getVisibility() != View.VISIBLE) return null;
            Object subObj = spinnerSubrace.getSelectedItem();
            if (subObj == null) return null;
            String subName = subObj.toString();
            // если у тебя в списке подрас есть "Выберите подрасу" как placeholder — запрещаем его
            if (subName.equalsIgnoreCase("Выберите подрасу") || subName.trim().isEmpty()) return null;
        }

        // считаем итоговые характеристики (base + racial)
        int[] totals = new int[6];
        for (int i = 0; i < 6; i++) {
            totals[i] = base[i] + ((racial != null && racial.length > i) ? racial[i] : 0);
        }

        // собираем Intent
        Intent intent = new Intent(ctx, CharacterActivity.class);
        intent.putExtra("STR", totals[0]);
        intent.putExtra("DEX", totals[1]);
        intent.putExtra("CON", totals[2]);
        intent.putExtra("INT", totals[3]);
        intent.putExtra("WIS", totals[4]);
        intent.putExtra("CHA", totals[5]);

        intent.putExtra("RACE", mainRace);
        String subRace = "";
        if (spinnerSubrace != null && spinnerSubrace.getVisibility() == View.VISIBLE && spinnerSubrace.getSelectedItem() != null) {
            subRace = spinnerSubrace.getSelectedItem().toString();
            if (subRace.equalsIgnoreCase("Выберите подрасу")) subRace = "";
        }
        intent.putExtra("SUBRACE", subRace);

        return intent;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_point_buy, container, false);

        // find views
        spinnerRace = root.findViewById(R.id.spinnerRace);
        spinnerSubrace = root.findViewById(R.id.spinnerSubrace);
        tvSubracePrompt = root.findViewById(R.id.tvSubracePrompt);
        tvRaceDescription = root.findViewById(R.id.tvRaceDescription);

        npStr = root.findViewById(R.id.npStr);
        npDex = root.findViewById(R.id.npDex);
        npCon = root.findViewById(R.id.npCon);
        npInt = root.findViewById(R.id.npInt);
        npWis = root.findViewById(R.id.npWis);
        npCha = root.findViewById(R.id.npCha);

        tvStrRacial = root.findViewById(R.id.tvStrRacial);
        tvDexRacial = root.findViewById(R.id.tvDexRacial);
        tvConRacial = root.findViewById(R.id.tvConRacial);
        tvIntRacial = root.findViewById(R.id.tvIntRacial);
        tvWisRacial = root.findViewById(R.id.tvWisRacial);
        tvChaRacial = root.findViewById(R.id.tvChaRacial);

        tvStrTotal = root.findViewById(R.id.tvStrTotal);
        tvDexTotal = root.findViewById(R.id.tvDexTotal);
        tvConTotal = root.findViewById(R.id.tvConTotal);
        tvIntTotal = root.findViewById(R.id.tvIntTotal);
        tvWisTotal = root.findViewById(R.id.tvWisTotal);
        tvChaTotal = root.findViewById(R.id.tvChaTotal);

        tvStrMod = root.findViewById(R.id.tvStrMod);
        tvDexMod = root.findViewById(R.id.tvDexMod);
        tvConMod = root.findViewById(R.id.tvConMod);
        tvIntMod = root.findViewById(R.id.tvIntMod);
        tvWisMod = root.findViewById(R.id.tvWisMod);
        tvChaMod = root.findViewById(R.id.tvChaMod);

        tvStrCost = root.findViewById(R.id.tvStrCost);
        tvDexCost = root.findViewById(R.id.tvDexCost);
        tvConCost = root.findViewById(R.id.tvConCost);
        tvIntCost = root.findViewById(R.id.tvIntCost);
        tvWisCost = root.findViewById(R.id.tvWisCost);
        tvChaCost = root.findViewById(R.id.tvChaCost);

        tvUsedPoints = root.findViewById(R.id.tvUsedPoints);
        defaultUsedColor = tvUsedPoints.getCurrentTextColor();

        Button btnReset = root.findViewById(R.id.btnReset);
        btnReset.setOnClickListener(v -> {
            base = new int[]{8,8,8,8,8,8};
            raceBase = new int[]{0,0,0,0,0,0};
            subraceBonus = new int[]{0,0,0,0,0,0};
            racial = new int[]{0,0,0,0,0,0};
            setupPickers();
            spinnerRace.setSelection(0);
            spinnerSubrace.setVisibility(View.GONE);
            tvSubracePrompt.setVisibility(View.GONE);
            tvRaceDescription.setText("");
            updateAll();
        });

//        Button btnToCharacter = root.findViewById(R.id.btnToCharacter);
//        btnToCharacter.setOnClickListener(v -> {
//            Intent intent = buildCharacterIntent(requireContext());
//            if (intent != null) {
//                startActivity(intent);
//            } else {
//                // покажем подсказку, почему не можем перейти
//                Snackbar.make(requireView(), "Выберите расу и подрасу (если требуется) перед продолжением", Snackbar.LENGTH_LONG)
//                        .setAnchorView(R.id.fab) // если в layout есть fab, иначе убери
//                        .show();
//            }
//        });


        // -------- data setup --------
        // races (names only, без скобочек)
        races.put("Выберите", new int[]{0,0,0,0,0,0});
        races.put("Человек", new int[]{1,1,1,1,1,1});
        races.put("Дварф", new int[]{0,0,2,0,0,0});
        races.put("Эльф", new int[]{0,2,0,0,0,0});
        races.put("Полуорк", new int[]{2,0,1,0,0,0}); // +2 STR, +1 CON (твоя правка)
        races.put("Тифлинг", new int[]{0,0,0,1,0,2}); // +1 INT, +2 CHA (твоя правка)

        // subrace options (use keys equal to main race names)
        subraceOptions.put("Эльф", new String[] {"Высший", "Лесной"});
        subraceBonuses.put("Высший", new int[]{0,0,0,1,0,0}); // +1 INT
        subraceBonuses.put("Лесной", new int[]{0,0,0,0,1,0}); // +1 WIS

        subraceOptions.put("Дварф", new String[] {"Горный", "Холмовой"});
        subraceBonuses.put("Горный", new int[]{2,0,0,0,0,0}); // +2 STR
        subraceBonuses.put("Холмовой", new int[]{0,0,0,0,1,0}); // +1 WIS

        // descriptions (основные расы)
        raceDescriptions.put("Дварф", R.string.race_dwarf);
        raceDescriptions.put("Эльф", R.string.race_elf);
        raceDescriptions.put("Полуорк", R.string.race_half_orc);
        raceDescriptions.put("Тифлинг", R.string.race_tiefling);
        raceDescriptions.put("Человек", R.string.race_human);

        subraceDescriptions.put("Высший", R.string.subrace_high_elf);
        subraceDescriptions.put("Лесной", R.string.subrace_wood_elf);
        subraceDescriptions.put("Горный", R.string.subrace_mountain_dwarf);
        subraceDescriptions.put("Холмовой", R.string.subrace_hill_dwarf);


        // ---------------- adapter for main race spinner ----------------
        ArrayList<String> raceList = new ArrayList<>(races.keySet());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, raceList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRace.setAdapter(adapter);
        spinnerRace.setSelection(0);

        spinnerRace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)     {
                String key = raceList.get(position);

                // сохраняем базовый бонус основной расы
                raceBase = races.get(key).clone();

                // если у расы есть подрасы — показать spinnerSubrace + prompt
                if (subraceOptions.containsKey(key)) {
                    String[] opts = subraceOptions.get(key);
                    ArrayAdapter<String> subAdapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            opts
                    );
                    subAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerSubrace.setAdapter(subAdapter);
                    spinnerSubrace.setSelection(0, false);
                    spinnerSubrace.setVisibility(View.VISIBLE);
                    tvSubracePrompt.setVisibility(View.VISIBLE);

                    subraceBonus = new int[]{0, 0, 0, 0, 0, 0};
                } else {
                    spinnerSubrace.setVisibility(View.GONE);
                    tvSubracePrompt.setVisibility(View.GONE);
                    subraceBonus = new int[]{0, 0, 0, 0, 0, 0};
                }

                // итоговый racial = raceBase + subraceBonus
                racial = addArrays(raceBase, subraceBonus);

                // ===== обновление описания =====
                StringBuilder descBuilder = new StringBuilder();

                Integer raceDescRes = raceDescriptions.get(key);
                if (raceDescRes != null) {
                    descBuilder.append(getString(raceDescRes));
                }

                if (spinnerSubrace.getVisibility() == View.VISIBLE) {
                    String subName = (String) spinnerSubrace.getSelectedItem();
                    if (subName != null) {
                        Integer subDescRes = subraceDescriptions.get(subName);
                        if (subDescRes != null) {
                            descBuilder.append("\n\n").append(getString(subDescRes));
                        }
                    }
                }

                tvRaceDescription.setText(descBuilder.toString());

                updateAll();

                // уведомляем Activity, что выбор изменился
                notifySelectionChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        // слушатель подрасы
        spinnerSubrace.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = (String) parent.getItemAtPosition(position);

                if (name != null && subraceBonuses.containsKey(name)) {
                    subraceBonus = subraceBonuses.get(name).clone();
                } else {
                    subraceBonus = new int[]{0, 0, 0, 0, 0, 0};
                }

                racial = addArrays(raceBase, subraceBonus);

                // ===== обновление описания =====
                StringBuilder descBuilder = new StringBuilder();

                String mainRace = (String) spinnerRace.getSelectedItem();
                if (mainRace != null) {
                    Integer raceDescRes = raceDescriptions.get(mainRace);
                    if (raceDescRes != null) {
                        descBuilder.append(getString(raceDescRes));
                    }
                }

                Integer subDescRes = subraceDescriptions.get(name);
                if (subDescRes != null) {
                    descBuilder.append("\n\n").append(getString(subDescRes));
                }

                tvRaceDescription.setText(descBuilder.toString());

                updateAll();

                // уведомляем Activity, что выбор изменился
                notifySelectionChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });


        setupPickers();
        updateAll();

        return root;
    }

    private int[] addArrays(int[] a, int[] b) {
        int[] r = new int[6];
        for (int i=0;i<6;i++) r[i] = (a != null ? a[i] : 0) + (b != null ? b[i] : 0);
        return r;
    }

    private void setupPickers() {
        NumberPicker[] pickers = {npStr, npDex, npCon, npInt, npWis, npCha};
        for (int i = 0; i < pickers.length; i++) {
            NumberPicker p = pickers[i];

            // фантомный диапазон 7..16 с пустыми крайними значениями (если ты ещё хочешь костыль)
            p.setMinValue(7);
            p.setMaxValue(16);
            String[] disp = new String[] {"", "8","9","10","11","12","13","14","15", ""};
            p.setDisplayedValues(disp);
            p.setValue(8); // визуально 8
            p.setWrapSelectorWheel(false);

            final int index = i;
            p.setOnValueChangedListener((picker, oldVal, newVal) -> {
                if (newVal == 7) {
                    picker.setValue(8);
                    base[index] = 8;
                } else if (newVal == 16) {
                    picker.setValue(15);
                    base[index] = 15;
                } else {
                    base[index] = newVal; // нормально 8..15
                }
                updateAll();
            });

            // костыль (разрешать родителю перехватывать/запрещать) — оставляем, раз он стабильно работает у тебя
            p.setOnTouchListener((v, event) -> {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        v.performClick();
                        break;
                }
                return false;
            });

            p.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        }
    }

    private void updateAll() {
        // update racial text (показываем итоговый racial)
        tvStrRacial.setText(formatSigned(racial[0]));
        tvDexRacial.setText(formatSigned(racial[1]));
        tvConRacial.setText(formatSigned(racial[2]));
        tvIntRacial.setText(formatSigned(racial[3]));
        tvWisRacial.setText(formatSigned(racial[4]));
        tvChaRacial.setText(formatSigned(racial[5]));

        // totals, mods, costs
        int[] totals = new int[6];
        int[] mods = new int[6];
        int[] costs = new int[6];

        for (int i=0; i<6; i++) {
            totals[i] = base[i] + racial[i];
            mods[i] = abilityModifier(totals[i]);
            costs[i] = pointCostForBase(base[i]);
        }

        tvStrTotal.setText(String.valueOf(totals[0]));
        tvDexTotal.setText(String.valueOf(totals[1]));
        tvConTotal.setText(String.valueOf(totals[2]));
        tvIntTotal.setText(String.valueOf(totals[3]));
        tvWisTotal.setText(String.valueOf(totals[4]));
        tvChaTotal.setText(String.valueOf(totals[5]));

        tvStrMod.setText(formatSigned(mods[0]));
        tvDexMod.setText(formatSigned(mods[1]));
        tvConMod.setText(formatSigned(mods[2]));
        tvIntMod.setText(formatSigned(mods[3]));
        tvWisMod.setText(formatSigned(mods[4]));
        tvChaMod.setText(formatSigned(mods[5]));

        tvStrCost.setText(String.valueOf(costs[0]));
        tvDexCost.setText(String.valueOf(costs[1]));
        tvConCost.setText(String.valueOf(costs[2]));
        tvIntCost.setText(String.valueOf(costs[3]));
        tvWisCost.setText(String.valueOf(costs[4]));
        tvChaCost.setText(String.valueOf(costs[5]));

        int used = 0;
        for (int c : costs) used += c;
        tvUsedPoints.setText(used + "/" + POINT_BUDGET);

        // highlight when over budget
        if (used > POINT_BUDGET) {
            tvUsedPoints.setTextColor(Color.parseColor("#FF6B6B"));
        } else {
            tvUsedPoints.setTextColor(defaultUsedColor);
        }
        // highlight when over budget
        if (used > POINT_BUDGET) {
            tvUsedPoints.setTextColor(Color.parseColor("#FF6B6B"));
        } else {
            tvUsedPoints.setTextColor(defaultUsedColor);
        }

        // -> уведомляем Activity о текущем состоянии выбора (раса / подраса)
        notifySelectionChanged();

    }
    private void notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(isSelectionComplete());
        }
    }

    private String formatSigned(int v) {
        return v >= 0 ? "+" + v : String.valueOf(v);
    }

    private int abilityModifier(int score) {
        return (int) Math.floor((score - 10) / 2.0);
    }

    private int pointCostForBase(int baseScore) {
        switch (baseScore) {
            case 8: return 0;
            case 9: return 1;
            case 10: return 2;
            case 11: return 3;
            case 12: return 4;
            case 13: return 5;
            case 14: return 7;
            case 15: return 9;
            default: return 0;
        }
    }
}
