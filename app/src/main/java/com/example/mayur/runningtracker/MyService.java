/*
Name: Mayur Gunputh
Date: 11 Dec 2018
Project: G53MDP Coursework 2
MyService.java
Service to continuously detect location change and broadcast the new location
 */

package com.example.mayur.runningtracker;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyService extends Service implements LocationListener {

    public static final String TAG = "RunningTrackerService";

    private final Binder mBind = new mBinder();


    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mBind;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //initialise location manager and listener
        LocationManager locationManager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        MyService locationListener = new MyService();


        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        } catch (SecurityException e) {
        }
        return START_STICKY;
    }

    //when location change is detected
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");

        //broadcast new location
        Intent i = new Intent("LocationBroadcastService");
        i.putExtra("loc", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public class mBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}
