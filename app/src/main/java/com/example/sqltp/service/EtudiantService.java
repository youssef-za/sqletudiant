package com.example.sqltp.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.sqltp.classes.Etudiant;
import com.example.sqltp.util.MySQLiteHelper;

import java.util.ArrayList;
import java.util.List;

public class EtudiantService {
    private static final String TABLE_NAME = "etudiant";

    private static final String KEY_ID = "id";
    private static final String KEY_NOM = "nom";
    private static final String KEY_PRENOM = "prenom";
    private static final String KEY_DATE_NAISSANCE ="date_naissance"; // New column
    private static final String KEY_PHOTO = "photo";

    private static String[] COLUMNS = {KEY_ID, KEY_NOM, KEY_PRENOM, KEY_DATE_NAISSANCE, KEY_PHOTO};

    private MySQLiteHelper helper;

    public EtudiantService(Context context) {
        this.helper = new MySQLiteHelper(context);
    }

    public long create(Etudiant e) {  // Changed return type to long
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());

        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto());
        }

        long id = db.insert(TABLE_NAME, null, values);
        db.close();

        Log.d("EtudiantService", "Created student with ID: " + id);
        return id;  // Return the inserted row ID
    }


    public void update(Etudiant e) {
        SQLiteDatabase db = this.helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ID, e.getId());
        values.put(KEY_NOM, e.getNom());
        values.put(KEY_PRENOM, e.getPrenom());
        values.put(KEY_DATE_NAISSANCE, e.getDateNaissance());
        if (e.getPhoto() != null) {
            values.put(KEY_PHOTO, e.getPhoto());
        }

        db.update(TABLE_NAME,
                values,
                "id = ?",
                new String[]{e.getId() + ""});
        db.close();
    }

    public Etudiant findById(int id) {
        SQLiteDatabase db = this.helper.getReadableDatabase();
        Etudiant e = null;

        try (Cursor c = db.query(TABLE_NAME, COLUMNS,
                "id = ?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                e = new Etudiant();
                e.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                e.setNom(c.getString(c.getColumnIndexOrThrow(KEY_NOM)));
                e.setPrenom(c.getString(c.getColumnIndexOrThrow(KEY_PRENOM)));
                e.setDateNaissance(c.getString(c.getColumnIndexOrThrow(KEY_DATE_NAISSANCE)));
                e.setPhoto(c.getBlob(c.getColumnIndexOrThrow(KEY_PHOTO)));
            }
        } catch (Exception ex) {
            Log.e("EtudiantService", "Error finding student", ex);
        } finally {
            db.close();
        }
        return e;
    }

    public void delete(Etudiant e){
        if (e == null) return;
        delete(e.getId());
    }

    public List<Etudiant> findAll() {
        List<Etudiant> students = new ArrayList<>();
        SQLiteDatabase db = this.helper.getReadableDatabase();

        try (Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (c.moveToFirst()) {
                do {
                    Etudiant e = new Etudiant();
                    e.setId(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
                    e.setNom(c.getString(c.getColumnIndexOrThrow(KEY_NOM)));
                    e.setPrenom(c.getString(c.getColumnIndexOrThrow(KEY_PRENOM)));
                    e.setDateNaissance(c.getString(c.getColumnIndexOrThrow(KEY_DATE_NAISSANCE)));
                    e.setPhoto(c.getBlob(c.getColumnIndexOrThrow(KEY_PHOTO)));
                    students.add(e);
                    Log.d("EtudiantService", "Found student: " + e.getNom());
                } while (c.moveToNext());
            }
        } catch (Exception ex) {
            Log.e("EtudiantService", "Error finding all students", ex);
        } finally {
            db.close();
        }
        return students;
    }
    public void delete(int id) {
        SQLiteDatabase db = null;
        try {
            db = this.helper.getWritableDatabase();
            db.delete(TABLE_NAME,
                    KEY_ID + " = ?",
                    new String[]{String.valueOf(id)});
        } finally {
            if (db != null) db.close();
        }
    }
}