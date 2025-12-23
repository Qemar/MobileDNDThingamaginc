package com.example.myapplication.data;

import android.provider.BaseColumns;

public final class CharacterContract {
    private CharacterContract() {}

    public static final class CharacterEntry implements BaseColumns {
        public static final String TABLE_NAME = "characters";

        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_RACE = "race";
        public static final String COLUMN_SUBRACE = "subrace";
        public static final String COLUMN_CLASS = "class";
        public static final String COLUMN_BACKGROUND = "background";
        public static final String COLUMN_LEVEL = "level";

        // stats
        public static final String COLUMN_STR = "str";
        public static final String COLUMN_DEX = "dex";
        public static final String COLUMN_CON = "con";
        public static final String COLUMN_INT = "intelligence";
        public static final String COLUMN_WIS = "wis";
        public static final String COLUMN_CHA = "cha";

        // skills — храню как CSV ключей (например "Athletics,Intimidation")
        public static final String COLUMN_SKILLS = "skills";

        // equipment choices: radio selections — можно хранить как короткие ключи
        public static final String COLUMN_EQUIP_ARMOR = "equip_armor";   // "A" or "B"
        public static final String COLUMN_EQUIP_WEAPON1 = "equip_weapon1";
        public static final String COLUMN_EQUIP_WEAPON2 = "equip_weapon2";
        public static final String COLUMN_EQUIP_PACK = "equip_pack";

        // full equipment text (class + background summary)
        public static final String COLUMN_EQUIP_TEXT = "equip_text";

        // New: hit points
        public static final String COLUMN_CURRENT_HP = "current_hp";
        public static final String COLUMN_MAX_HP = "max_hp";


        // timestamp
        public static final String COLUMN_CREATED_TS = "created_ts";
    }
}
