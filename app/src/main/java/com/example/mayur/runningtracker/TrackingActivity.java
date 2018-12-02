package com.example.mayur.runningtracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

public class TrackingActivity extends AppCompatActivity {

    TextView tv_Distance;

    Boolean isBound;

    MyService ms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        tv_Distance = findViewById(R.id.tv_Distance);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("RunningTracker", "onReceive");
                        Float dis = intent.getExtras().getFloat("dist");
                        String distance = String.format("%.2f", dis);
                        //String distance = Float.toString(intent.getExtras().getFloat("dist"));
                        Log.d("RunningTracker", "Distance: " + distance);
                        tv_Distance.setText(distance + "m");

                    }
                }
                , new IntentFilter("LocationBroadcastService"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunningTracker", "starting service");
        startService(intent);


    }

    private ServiceConnection myConnection = new ServiceConnection() {
        //Bind Service
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            MyService.mBinder binder = (MyService.mBinder) service;
            ms = binder.getService();
            isBound = true;
            Log.d("RunningTracker", "isBound = true");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
            Log.d("RunningTracker", "isBound = false");
        }
    };
}
