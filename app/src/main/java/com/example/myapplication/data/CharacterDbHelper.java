// app/src/main/java/com/example/myapplication/data/CharacterDbHelper.java
package com.example.myapplication.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.myapplication.data.CharacterContract.CharacterEntry;

import java.util.ArrayList;
import java.util.List;

public class CharacterDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "rpg_chars.db";
    // bumped version to 2 to add hp columns
    private static final int DATABASE_VERSION = 2;

    public CharacterDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create table with HP columns (new installs)
    private static final String SQL_CREATE =
            "CREATE TABLE " + CharacterEntry.TABLE_NAME + " (" +
                    CharacterEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    CharacterEntry.COLUMN_NAME + " TEXT," +
                    CharacterEntry.COLUMN_RACE + " TEXT," +
                    CharacterEntry.COLUMN_SUBRACE + " TEXT," +
                    CharacterEntry.COLUMN_CLASS + " TEXT," +
                    CharacterEntry.COLUMN_BACKGROUND + " TEXT," +
                    CharacterEntry.COLUMN_LEVEL + " INTEGER DEFAULT 1," +
                    CharacterEntry.COLUMN_STR + " INTEGER," +
                    CharacterEntry.COLUMN_DEX + " INTEGER," +
                    CharacterEntry.COLUMN_CON + " INTEGER," +
                    CharacterEntry.COLUMN_INT + " INTEGER," +
                    CharacterEntry.COLUMN_WIS + " INTEGER," +
                    CharacterEntry.COLUMN_CHA + " INTEGER," +
                    CharacterEntry.COLUMN_SKILLS + " TEXT," +
                    CharacterEntry.COLUMN_EQUIP_ARMOR + " TEXT," +
                    CharacterEntry.COLUMN_EQUIP_WEAPON1 + " TEXT," +
                    CharacterEntry.COLUMN_EQUIP_WEAPON2 + " TEXT," +
                    CharacterEntry.COLUMN_EQUIP_PACK + " TEXT," +
                    CharacterEntry.COLUMN_EQUIP_TEXT + " TEXT," +
                    CharacterEntry.COLUMN_CURRENT_HP + " INTEGER DEFAULT 0," +
                    CharacterEntry.COLUMN_MAX_HP + " INTEGER DEFAULT 0," +
                    CharacterEntry.COLUMN_CREATED_TS + " INTEGER" +
                    ");";

    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + CharacterEntry.TABLE_NAME;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    // If upgrading from version 1 -> 2, add columns
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // add hp columns, default 0
            db.execSQL("ALTER TABLE " + CharacterEntry.TABLE_NAME + " ADD COLUMN " +
                    CharacterEntry.COLUMN_CURRENT_HP + " INTEGER DEFAULT 0;");
            db.execSQL("ALTER TABLE " + CharacterEntry.TABLE_NAME + " ADD COLUMN " +
                    CharacterEntry.COLUMN_MAX_HP + " INTEGER DEFAULT 0;");
        }
        // future migrations go here
    }

    public long insertCharacter(ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(CharacterEntry.TABLE_NAME, null, values);
    }

    public int updateCharacter(long id, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        String where = CharacterEntry._ID + "=?";
        String[] args = new String[]{ String.valueOf(id) };
        return db.update(CharacterEntry.TABLE_NAME, values, where, args);
    }

    public int deleteCharacter(long id) {
        SQLiteDatabase db = getWritableDatabase();
        String where = CharacterEntry._ID + "=?";
        String[] args = new String[]{ String.valueOf(id) };
        return db.delete(CharacterEntry.TABLE_NAME, where, args);
    }

    public Cursor queryAll() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(CharacterEntry.TABLE_NAME, null, null, null, null, null, CharacterEntry.COLUMN_CREATED_TS + " DESC");
    }

    public Cursor queryById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String where = CharacterEntry._ID + "=?";
        String[] args = new String[]{ String.valueOf(id) };
        return db.query(CharacterEntry.TABLE_NAME, null, where, args, null, null, null);
    }

    public List<IdName> getAllIdNames() {
        List<IdName> out = new ArrayList<>();
        Cursor c = queryAll();
        try {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(CharacterEntry._ID));
                String name = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_NAME));
                out.add(new IdName(id, name));
            }
        } finally {
            if (c != null) c.close();
        }
        return out;
    }

    // small helper class to return id+name
    public static class IdName {
        public final long id;
        public final String name;
        public IdName(long id, String name) { this.id = id; this.name = name; }
    }

    // в CharacterDbHelper.java
    public void initializeHpDefaultsForExisting() {
        SQLiteDatabase db = getWritableDatabase();
        String[] cols = new String[] { CharacterEntry._ID, CharacterEntry.COLUMN_CLASS, CharacterEntry.COLUMN_CON, CharacterEntry.COLUMN_SUBRACE, CharacterEntry.COLUMN_CURRENT_HP, CharacterEntry.COLUMN_MAX_HP };
        String where = CharacterEntry.COLUMN_MAX_HP + " IS NULL OR " + CharacterEntry.COLUMN_MAX_HP + " = 0";
        Cursor c = db.query(CharacterEntry.TABLE_NAME, cols, where, null, null, null, null);
        try {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow(CharacterEntry._ID));
                String cls = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CLASS));
                int con = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CON));
                String sub = c.getString(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_SUBRACE));

                int conMod = (int) Math.floor((con - 10) / 2.0);
                int baseHp = (cls != null && cls.equalsIgnoreCase("Воин")) ? 10 : 8;
                int maxHp = baseHp + conMod;
                if (sub != null && sub.equalsIgnoreCase("Холмовой")) maxHp += 1;
                if (maxHp < 1) maxHp = 1;

                ContentValues cv = new ContentValues();
                cv.put(CharacterEntry.COLUMN_MAX_HP, maxHp);

                // если current_hp пуст или 0 — сделаем его равным max
                int cur = c.getInt(c.getColumnIndexOrThrow(CharacterEntry.COLUMN_CURRENT_HP));
                if (cur <= 0) cv.put(CharacterEntry.COLUMN_CURRENT_HP, maxHp);

                db.update(CharacterEntry.TABLE_NAME, cv, CharacterEntry._ID + "=?", new String[] { String.valueOf(id) } );
            }
        } finally {
            if (c != null) c.close();
        }
    }


}
