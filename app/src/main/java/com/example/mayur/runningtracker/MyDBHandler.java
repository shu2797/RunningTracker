package com.example.mayur.runningtracker;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mayur.runningtracker.provider.MyContentProvider;

import java.util.ArrayList;
import java.util.List;

public class MyDBHandler extends SQLiteOpenHelper {

    private ContentResolver myCR;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "runLogsDB.db";
    public static final String TABLE_RUNLOGS = "runlogs";

    public static final String COLUMN_DATETIME = "datetime";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_TIME = "time";

    public MyDBHandler( Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        myCR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("RunningTrackerDB", "onCreate");
        String CREATE_RUNLOGS_TABLE = "CREATE TABLE " +
                TABLE_RUNLOGS + "("
                + COLUMN_DATETIME + " TEXT PRIMARY KEY," +
                COLUMN_DISTANCE
                + " TEXT," + COLUMN_TIME + " TEXT" + ")";
        db.execSQL(CREATE_RUNLOGS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RUNLOGS);
        onCreate(db);
    }

    public void addLog(RunLog runLog){
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, runLog.getDateTime());
        values.put(COLUMN_DISTANCE, runLog.getDistance());
        values.put(COLUMN_TIME, runLog.getTime());
        Log.d("RunningTrackerDB", "cr inserting");
        myCR.insert(MyContentProvider.CONTENT_URI, values);
        Log.d("RunningTrackerDB", "cr  inserted");
    }

    public List<RunLog> listAllRunLogs(){
        List<RunLog> runLogList = new ArrayList<>();

        String[] projection = {COLUMN_DATETIME, COLUMN_DISTANCE, COLUMN_TIME};
        String selection = null;
        Cursor c = myCR.query(MyContentProvider.CONTENT_URI, projection, selection, null, null);

        String datetime, distance, time;

        if(c != null){
            if(c.moveToFirst()){
                do {
                    datetime = c.getString(c.getColumnIndex(COLUMN_DATETIME));
                    distance = c.getString(c.getColumnIndex(COLUMN_DISTANCE));
                    time = c.getString(c.getColumnIndex(COLUMN_TIME));

                    RunLog runLog = new RunLog(datetime, distance, time);
                    runLogList.add(runLog);

                } while (c.moveToNext());
            }
        }

        return runLogList;
    }
}
