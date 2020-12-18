package com.interviewtest.techTask;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.util.ArrayList;


public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "MyDBName.db";
    public static final String CONTACTS_TABLE_NAME = "contacts";
    public static final String CONTACTS_COLUMN_ID = "id";
    public static final String CONTACTS_COLUMN_EMAIL = "email";
    public static final String CONTACTS_COLUMN_FIRSTNAME = "first_name";
    public static final String CONTACTS_COLUMN_LASTNAME = "last_name";
    public static final String CONTACTS_COLUMN_AVATAR = "avatar";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       /* db.execSQL(
                "CREATE TABLE CONTACTS_TABLE_NAME " +
                        "(id integer primary key, CONTACTS_COLUMN_FIRSTNAME text,CONTACTS_COLUMN_LASTNAME text," +
                        "CONTACTS_COLUMN_EMAIL text, CONTACTS_COLUMN_AVATAR text)"
        );
        */

        db.execSQL("create table " + CONTACTS_TABLE_NAME + "(" + CONTACTS_COLUMN_ID + " integer primary key," +
                CONTACTS_COLUMN_FIRSTNAME + " text," + CONTACTS_COLUMN_LASTNAME + " text," + CONTACTS_COLUMN_EMAIL + " text," + CONTACTS_COLUMN_AVATAR + " text" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS contacts");
        onCreate(db);
    }

    public boolean insertContact(String fName, String sName, String email, String avatar, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CONTACTS_COLUMN_EMAIL, email);
        contentValues.put(CONTACTS_COLUMN_FIRSTNAME, fName);
        contentValues.put(CONTACTS_COLUMN_LASTNAME, sName);
        contentValues.put(CONTACTS_COLUMN_AVATAR, avatar);
        contentValues.put(CONTACTS_COLUMN_ID, id);
        db.insert(CONTACTS_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getData(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts where id=" + id + "", null);
        return res;
    }

    public int numberOfRows() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME);
        return numRows;
    }

    public boolean updateContact(Integer id, String name, String phone, String email, String street, String place) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);
        contentValues.put("street", street);
        contentValues.put("place", place);
        db.update(CONTACTS_TABLE_NAME, contentValues, "id = ? ", new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteContact(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[]{Integer.toString(id)});
    }

    public ArrayList<Model.Data> getAllCotacts(Context context) {
        ArrayList<Model.Data> array_list = new ArrayList<Model.Data>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("select * from contacts", null);
        res.moveToFirst();
        while (!res.isAfterLast()) {
            Model.Data values = new Model.Data(res.getInt(res.getColumnIndex(CONTACTS_COLUMN_ID)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_EMAIL)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_FIRSTNAME)),
                    res.getString(res.getColumnIndex(CONTACTS_COLUMN_LASTNAME)), res.getString(res.getColumnIndex(CONTACTS_COLUMN_AVATAR))
            );
            array_list.add(values);
            res.moveToNext();
        }
        return array_list;
    }
}