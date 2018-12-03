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
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyService extends Service implements LocationListener {

    private final Binder mBind = new mBinder();

    static Location oLocation;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("RunningTracker", "onBind");
        return mBind;
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        LocationManager locationManager =
                (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        MyService locationListener = new MyService();

        //Log.d("RunningTracker", "get old location");

//        try{
//            oLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            Log.d("RunningTracker", "oLocation: " + oLocation.toString());
//        } catch (Exception e){
//            Log.d("RunningTracker", e.toString());
//        }



        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5, // minimum time interval between updates
                    5, // minimum distance between updates, in metres
                    locationListener);
        } catch (SecurityException e) {
            Log.d("g53mdp", e.toString());
        }
        return START_STICKY;
    }

    public class mBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        float distance;
        try {
            distance = oLocation.distanceTo(location)/1000;
        } catch (Exception e){
            Log.d("RunningTracker", e.toString());
            oLocation = location;
            distance = oLocation.distanceTo(location);
        }

        Intent i = new Intent("LocationBroadcastService");
        i.putExtra("dist", distance);
        i.putExtra("loc", location);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        Log.d("RunningTracker", "Location changed: " + location.toString());
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
}
