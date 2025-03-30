package com.example.sqltp.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ecole";
    private static final int DATABASE_VERSION = 3; // Incremented from 1 to 3

    // Table and column names
    private static final String TABLE_ETUDIANT = "etudiant";
    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE = "date_naissance";
    private static final String KEY_PHOTO = "photo";

    // SQL to create table
    private static final String CREATE_TABLE_ETUDIANT =
            "CREATE TABLE " + TABLE_ETUDIANT + "(" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_NOM + " TEXT NOT NULL, " +
                    KEY_PRENOM + " TEXT NOT NULL, " +
                    KEY_DATE_NAISSANCE + " TEXT, " +  // Date in format "dd/MM/yyyy"
                    KEY_PHOTO + " BLOB)";  // For storing photo as byte array

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ETUDIANT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle incremental upgrades
        if (oldVersion < 2) {
            // Add date_naissance column for version 2
            db.execSQL("ALTER TABLE " + TABLE_ETUDIANT +
                    " ADD COLUMN " + KEY_DATE_NAISSANCE + " TEXT");
        }

        if (oldVersion < 3) {
            // Add photo column for version 3
            db.execSQL("ALTER TABLE " + TABLE_ETUDIANT +
                    " ADD COLUMN " + KEY_PHOTO + " BLOB");
        }

        // For major version jumps, recreate table
        if (oldVersion > 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ETUDIANT);
            onCreate(db);
        }
    }
}