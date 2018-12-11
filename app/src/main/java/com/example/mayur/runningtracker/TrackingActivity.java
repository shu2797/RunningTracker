/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
TrackingActivity.java
The application launches into this activity. It contains a Google Map view which show the user location, allows the user to start logging, pause and stop.
It displays distance covered and time spent in real time. It also allows the user to launch ViewListActivity which allows the user to look
at all logs.
 */

package com.example.mayur.runningtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Activity components
    TextView tv_Distance, tv_Time;
    FloatingActionButton startFAB, stopFAB, pauseFAB, listFAB;

    //booleans
    Boolean isBound;
    Boolean tracking, active = false;

    //service
    MyService ms;
    //map variables
    Location location, oLocation;
    SupportMapFragment supportMapFragment;
    GoogleMap gmap;
    Intent intent;
    Context cont = this;
    //time recording variables
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    float dis, tot_dist = (float) 0.00;
    Handler handler;
    int Seconds, Minutes, MilliSeconds;
    //time recording runnable process
    public Runnable runnable = new Runnable() {

        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime; //update time
            UpdateTime = TimeBuff + MillisecondTime; //update total time
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 1000);

            //update time textView
            tv_Time.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };
    String date;
    private NotificationManager notificationManager;
    //ServiceConnection for service
    private ServiceConnection myConnection = new ServiceConnection() {
        //Bind Service
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MyService.mBinder binder = (MyService.mBinder) service;
            ms = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        //connect to UI components
        tv_Distance = findViewById(R.id.tv_Distance);
        tv_Time = findViewById(R.id.tv_Time);
        stopFAB = findViewById(R.id.tracking_stopFAB);
        startFAB = findViewById(R.id.tracking_startFAB);
        pauseFAB = findViewById(R.id.tracking_pauseFAB);
        listFAB = findViewById(R.id.tracking_listFAB);

        //set tooltips for the buttons
        TooltipCompat.setTooltipText(stopFAB, "Stop tracking and save log");
        TooltipCompat.setTooltipText(startFAB, "Start tracking");
        TooltipCompat.setTooltipText(pauseFAB, "Pause tracking");
        TooltipCompat.setTooltipText(listFAB, "View all logs and best time");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class); //assign notification manager to system
        }

        handler = new Handler();

        //show only start button
        startFAB.show();
        pauseFAB.hide();
        stopFAB.hide();

        //start button listener
        startFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(intent); //start the service
                StartTime = SystemClock.uptimeMillis(); //get current system uptime
                handler.postDelayed(runnable, 0);
                tracking = true; //set status to TRACKING

                //show pause and stop buttons
                startFAB.hide();
                pauseFAB.show();
                stopFAB.show();

                //if not paused
                if (!active) {
                    SimpleDateFormat SDF = new SimpleDateFormat("HH:mm dd-MMM-yyyy");
                    date = SDF.format(new Date()); //format date and save as string
                    tv_Distance.setText("0.00m"); //if starting new log, reset distance textView
                    active = true; //set status to ACTIVE (currently tracking)
                }
                sendNotif(); //send notification
                oLocation = location; //set start location to current location
            }
        });

        //pause button listener
        pauseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeBuff += MillisecondTime; //save time buffer
                handler.removeCallbacks(runnable); //stop runnable

                tracking = false; //set status to NOT TRACKING

                sendNotif(); //send notification

                startFAB.show(); //show start button
                pauseFAB.hide();
            }
        });

        //stop button listener
        stopFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(runnable); //stop runnable

                MyDBHandler dbHandler = new MyDBHandler(getBaseContext(), null, null, 1); //call database helper
                RunLog runLog = new RunLog(date, tv_Distance.getText().toString(), UpdateTime); //create new record runLog
                dbHandler.addLog(runLog); //add new log to database

                //get log details for best time in database
                SQLiteDatabase db = dbHandler.getReadableDatabase();
                String bestQuery = "SELECT * FROM " + MyDBHandler.TABLE_RUNLOGS + " ORDER BY time ASC LIMIT 1";
                Cursor c = db.rawQuery(bestQuery, null);
                c.moveToNext();
                String bestDateTime = c.getString(c.getColumnIndex("datetime"));
                String dis = c.getString(c.getColumnIndex("distance"));
                long time = c.getLong(c.getColumnIndex("time"));

                //if the current log is the new best time, show a dialog box
                if (date.equals(bestDateTime) && dis.equals(tv_Distance.getText().toString()) && (time == UpdateTime)) {
                    new AlertDialog.Builder(cont)
                            .setTitle("New Best Time")
                            .setMessage("Congratulations. You set a new record! Distance: " + tv_Distance.getText().toString() + " Time: " + tv_Time.getText().toString())
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create()
                            .show();
                }

                //reset UI
                stopFAB.hide();
                pauseFAB.hide();
                startFAB.show();

                notificationManager.cancel(1); //clear notification

                tv_Distance.setText("");
                tv_Time.setText("");

                tot_dist = (float) 0.00;
                MillisecondTime = 0L;
                StartTime = 0L;
                TimeBuff = 0L;
                UpdateTime = 0L;
                Seconds = 0;
                Minutes = 0;
                MilliSeconds = 0;

                //set status to NOT TRACKING and NOT ACTIVE
                tracking = false;
                active = false;

            }
        });

        //list button listener
        listFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //launch ViewListActivity
                Intent i = new Intent(getApplicationContext(), ViewListActivity.class);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //bind service
        intent = new Intent(this, MyService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

        //check if location permission is granted
        if (checkLocationPermission()) {
            //if granted, update Google Map view
            supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            supportMapFragment.getMapAsync(this);

            //Broadcast receiver everytime location is updated
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            location = intent.getExtras().getParcelable("loc");
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            moveCamera(latLng, 18.5f);
                            try {
                                //if status is TRACKING, calculate new distance and display
                                if (tracking) {
                                    dis = oLocation.distanceTo(location);
                                    oLocation = location;
                                    tot_dist = dis + tot_dist;
                                    String distance = String.format("%.2f", tot_dist);
                                    tv_Distance.setText(distance + "m");
                                }
                            } catch (Exception e) {
                            }
                        }
                    }
                    , new IntentFilter("LocationBroadcastService"));
            startService(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //when app is destroyed, stop service
        handler.removeCallbacks(runnable);
        stopService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //do not destroy app when back button is pressed
        moveTaskToBack(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //configure Google Map view
        gmap = googleMap;
        gmap.setMyLocationEnabled(true);
        gmap.getUiSettings().setZoomControlsEnabled(true);
        stopService(intent);
    }

    public void moveCamera(LatLng latLng, float zoom) {
        //Google Map view animation when location is changed
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        gmap.animateCamera(cameraUpdate);
    }

    //function to send notification
    public void sendNotif() {
        Intent intent = getIntent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //notification channel created to support android SDK 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("runningTracker", "RunningTracker", notificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Notification notif = new NotificationCompat.Builder(this, "runningTracker")
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle("RunningTracker")
                .setContentText(tracking ? "Tracking" : "Paused")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        notificationManager.notify(1, notif); //send notification

    }

    //check and ask location permission function
    public boolean checkLocationPermission() {
        //if permission not granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //if user already denied permission once before
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //show explanation to user and ask permission again
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("Location services is needed for this app to work properly. Please allow it")
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(TrackingActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }
                        })
                        .create()
                        .show();
                Log.d("RunningTracker", "request permission");
            } else {
                // If first time user is being asked permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            return false;
        } else {
            //permission already granted
            return true;
        }
    }

    //after permission has been asked
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();

                        //Update map
                        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                        supportMapFragment.getMapAsync(this);

                        //Broadcast receiver everytime location is updated
                        LocalBroadcastManager.getInstance(this).registerReceiver(
                                new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        location = intent.getExtras().getParcelable("loc");
                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        moveCamera(latLng, 18.5f);
                                        try {
                                            //if status is TRACKING, calculate new distance and display
                                            if (tracking) {
                                                dis = oLocation.distanceTo(location);
                                                oLocation = location;
                                                tot_dist = dis + tot_dist;
                                                String distance = String.format("%.2f", tot_dist);
                                                tv_Distance.setText(distance + "m");
                                            }
                                        } catch (Exception e) {
                                        }
                                    }
                                }
                                , new IntentFilter("LocationBroadcastService"));
                        startService(intent);
                    }

                } else {
                    // permission denied
                    Log.d("RunningTracker", "Permission denied");

                    //permission will continue to be asked until accepted

                    //explain to user and ask permission again
                    new AlertDialog.Builder(this)
                            .setTitle("Location Permission Needed")
                            .setMessage("Location services is needed for this app to work properly. Please allow it")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Prompt the user once explanation has been shown
                                    ActivityCompat.requestPermissions(TrackingActivity.this,
                                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                                }
                            })
                            .create()
                            .show();
                }
                return;
            }
        }
    }
}
