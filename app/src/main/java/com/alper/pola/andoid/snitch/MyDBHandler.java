package com.alper.pola.andoid.snitch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.alper.pola.andoid.snitch.categories.business;
import com.alper.pola.andoid.snitch.categories.entertaiment;
import com.alper.pola.andoid.snitch.categories.sport;
import com.alper.pola.andoid.snitch.categories.technology;

import java.util.ArrayList;

// class that works specifically with the database
public class MyDBHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "finaldb.db";
    private SQLiteDatabase database;
    private Context context;

    public MyDBHandler(bookmark context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public MyDBHandler(page context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public MyDBHandler(business context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public MyDBHandler(entertaiment context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public MyDBHandler(sport context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public MyDBHandler(technology context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }
    public static final String TABLE_NAME = "EVENTS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_TITLE = "EVENT_TITLE";
    public static final String COLUMN_DATE = "EVENT_DATE";
    public static final String COLUMN_Author = "EVENT_Author";
    public static final String COLUMN_DESCRIPTION = "EVENT_DESCRIPTION";
    public static final String COLUMN_URL = "EVENT_URL";
    public static final String COLUMN_IMG = "EVENT_IMG";




    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " ( " + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TITLE + " VARCHAR, " + COLUMN_DATE + " VARCHAR, " + COLUMN_Author + " VARCHAR, "
                + COLUMN_DESCRIPTION + " VARCHAR, "+ COLUMN_URL +" VARCHAR, " + COLUMN_IMG + " VARCHAR);");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
    public void deleteProduct(String productName){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = \"" + productName + "\";");
    }

    public void insertRecord(bookmarklist event) {
        database = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, event.getTitle());
        contentValues.put(COLUMN_DATE, event.getDate());
        contentValues.put(COLUMN_Author, event.getAuthor());
        contentValues.put(COLUMN_DESCRIPTION, event.getDescription());
        contentValues.put(COLUMN_URL, event.getUrl());
        contentValues.put(COLUMN_IMG, event.getImg());
        long id = database.insert(TABLE_NAME, null, contentValues);

        if (id != -1) {
            event.setId("" + id);
            Toast.makeText(context, "bookmark added", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error inserting new record", Toast.LENGTH_SHORT).show();
        }

        database.close();
    }


    public ArrayList<bookmarklist> getAllRecords() {
        database = this.getReadableDatabase();
        Cursor cursor = database.query(TABLE_NAME, null, null, null, null, null, null, null);

        ArrayList<bookmarklist> events = new ArrayList<bookmarklist>();
        bookmarklist event;
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToNext();

                event = new bookmarklist();
                event.setId(cursor.getString(0));
                event.setTitle(cursor.getString(1));
                event.setDate(cursor.getString(2));
                event.setAuthor(cursor.getString(3));
                event.setDescription(cursor.getString(4));
                event.setUrl(cursor.getString(5));
                event.setImg(cursor.getString(6));

                events.add(event);



            }
        }
        cursor.close();
        database.close();

        return events;
    }
}
