/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
MyDBHandler.java
database helper
 */

package com.example.mayur.runningtracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.mayur.runningtracker.provider.MyContentProvider;

public class MyDBHandler extends SQLiteOpenHelper {

    public static final String TABLE_RUNLOGS = "runlogs";
    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_TIME = "time";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "runLogsDB.db";
    private ContentResolver myCR;

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_RUNLOGS_TABLE = "CREATE TABLE " +
                TABLE_RUNLOGS + "("
                + COLUMN_DATETIME + " TEXT," +
                COLUMN_DISTANCE
                + " TEXT," + COLUMN_TIME + " REAL" + ")";
        db.execSQL(CREATE_RUNLOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNLOGS);
        onCreate(db);
    }

    public void addLog(RunLog runLog) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, runLog.getDateTime());
        values.put(COLUMN_DISTANCE, runLog.getDistance());
        values.put(COLUMN_TIME, runLog.getTime());
        myCR.insert(MyContentProvider.CONTENT_URI, values);
    }
}
