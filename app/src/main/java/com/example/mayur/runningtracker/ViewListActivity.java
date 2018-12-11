/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
ViewListActivity.java
Launched when list button is pressed on TrackingActivity.
Allows user to see all logs in database and highlights the log with the best time in GREEN color.
 */

package com.example.mayur.runningtracker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ViewListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);
        Context context = this;

        // call database helper
        MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);

        // Reference to TableLayout
        TableLayout tableLayout = findViewById(R.id.tablelayout);

        // Add header row
        TableRow rowHeader = new TableRow(context);
        rowHeader.setBackgroundColor(Color.parseColor("#5c6298"));
        rowHeader.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.WRAP_CONTENT));
        String[] headerText = {"Date", "Distance", "Time"};
        for (String c : headerText) {
            TextView tv = new TextView(this);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                    TableRow.LayoutParams.WRAP_CONTENT));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setPadding(5, 8, 5, 8);
            tv.setText(c);
            rowHeader.addView(tv);
        }
        tableLayout.addView(rowHeader);

        // Get data from sqlite database and add them to the table
        // Open the database for reading
        SQLiteDatabase db = dbHandler.getReadableDatabase();
        // Start the transaction.
        db.beginTransaction();

        try {
            //get log with best time
            String bestQuery = "SELECT time FROM " + MyDBHandler.TABLE_RUNLOGS + " ORDER BY time ASC LIMIT 1";
            Cursor c = db.rawQuery(bestQuery, null);

            //get all logs
            String selectQuery = "SELECT * FROM " + MyDBHandler.TABLE_RUNLOGS;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                c.moveToNext();
                long bestTime = c.getLong(c.getColumnIndex("time"));
                while (cursor.moveToNext()) {

                    // Read columns data
                    String runDateTime = cursor.getString(cursor.getColumnIndex("datetime"));
                    String runDistance = cursor.getString(cursor.getColumnIndex("distance"));
                    long runTime = cursor.getLong(cursor.getColumnIndex("time"));

                    //format time to string to be displayed
                    long Seconds = (int) (runTime / 1000);
                    long Minutes = Seconds / 60;
                    Seconds = Seconds % 60;
                    long MilliSeconds = (int) (runTime % 1000);
                    String runTimetxt = "" + Minutes + ":" + String.format("%02d", Seconds) + ":" + String.format("%03d", MilliSeconds);

                    // dara rows
                    TableRow row = new TableRow(context);
                    row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT,
                            TableLayout.LayoutParams.WRAP_CONTENT));

                    //if log contains best time, highlight in green color
                    if (runTime == bestTime) {
                        row.setBackgroundColor(Color.parseColor("#99cc00"));
                    }

                    String[] colText = {runDateTime + "", runDistance, runTimetxt};
                    for (String text : colText) {
                        TextView tv = new TextView(this);
                        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT));
                        tv.setGravity(Gravity.CENTER);
                        tv.setTextSize(16);
                        tv.setPadding(5, 5, 5, 5);
                        tv.setText(text);
                        row.addView(tv);
                    }
                    tableLayout.addView(row);
                }
            }
            db.setTransactionSuccessful();

        } catch (SQLiteException e) {
            e.printStackTrace();

        } finally {
            db.endTransaction();
            // End the transaction.
            db.close();
            // Close database
        }
    }
}
