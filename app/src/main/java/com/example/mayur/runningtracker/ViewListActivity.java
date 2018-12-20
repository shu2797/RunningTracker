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
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ViewListActivity extends AppCompatActivity {

    Context cont = this;

    public static final String TAG = "RunningTracker";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);
        Context context = this;

        Log.d(TAG, "ViewListActivity");

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

        Log.d(TAG, "Populating list");
        try {
            //get log with best time
            String bestQuery = "SELECT time FROM " + MyDBHandler.TABLE_RUNLOGS + " ORDER BY time ASC LIMIT 1";
            Cursor c = db.rawQuery(bestQuery, null);

            //get log with best time for current month
            SimpleDateFormat SDF = new SimpleDateFormat("MMM-yyyy");
            String date = SDF.format(new Date()); //format date and save as string
            String monthQuery = "SELECT * FROM " + MyDBHandler.TABLE_RUNLOGS + " WHERE datetime LIKE '%" + date +
                    "%' ORDER BY time ASC LIMIT 1";
            Cursor cMonth = db.rawQuery(monthQuery, null);

            //display best time for current month
            if(cMonth.getCount() > 0){
                cMonth.moveToNext();
                String runDateTime = cMonth.getString(cMonth.getColumnIndex("datetime"));
                String runDistance = cMonth.getString(cMonth.getColumnIndex("distance"));
                long runTime = cMonth.getLong(cMonth.getColumnIndex("time"));

                long Seconds = (int) (runTime / 1000);
                long Minutes = Seconds / 60;
                Seconds = Seconds % 60;
                long MilliSeconds = (int) (runTime % 1000);
                String runTimetxt = "" + Minutes + ":" + String.format("%02d", Seconds) + ":" + String.format("%03d", MilliSeconds);

                new AlertDialog.Builder(cont)
                        .setTitle("Best Time For This Month")
                        .setMessage("Date: " + runDateTime + " Distance: " + runDistance + " Time: " + runTimetxt)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create()
                        .show();
            } else {
                //if no records are found for current month, display alert
                new AlertDialog.Builder(cont)
                        .setTitle("Best Time For This Month")
                        .setMessage("No records found for this month")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .create()
                        .show();
            }

            //get all logs
            String selectQuery = "SELECT * FROM " + MyDBHandler.TABLE_RUNLOGS;
            Cursor cursor = db.rawQuery(selectQuery, null);
            if (cursor.getCount() > 0) {
                //get best time
                c.moveToNext();
                long bestTime = c.getLong(c.getColumnIndex("time"));

                while (cursor.moveToNext()) {

                    // Read columns data
                    String runDateTime = cursor.getString(cursor.getColumnIndex("datetime"));
                    String runDistance = cursor.getString(cursor.getColumnIndex("distance"));
                    long runTime = cursor.getLong(cursor.getColumnIndex("time"));

//                    if (cursor.isLast()){
//                        String currentMonth = runDateTime.substring(9);
//                        Log.d(TAG, currentMonth);
//                        String monthQuery = "SELECT * FROM " + MyDBHandler.TABLE_RUNLOGS + " WHERE datetime LIKE " + currentMonth +
//                                " ORDER BY time ASC LIMIT 1";
//
//
//                    }

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
