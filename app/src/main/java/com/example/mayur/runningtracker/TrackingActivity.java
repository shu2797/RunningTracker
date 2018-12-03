package com.example.mayur.runningtracker;

import android.annotation.SuppressLint;
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

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView tv_Distance;

    Boolean isBound;

    MyService ms;

    Location location;
    SupportMapFragment supportMapFragment;
    GoogleMap gmap;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        tv_Distance = findViewById(R.id.tv_Distance);
        supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        Log.d("RunningTracker", "onReceive");
                        Float dis = intent.getExtras().getFloat("dist");
                        String distance = String.format("%.2f", dis);
                        location = intent.getExtras().getParcelable("loc");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        moveCamera(latLng, 15f);
                        Log.d("RunningTracker", "Distance: " + distance);
                        tv_Distance.setText(distance + "m");
                    }
                }
                , new IntentFilter("LocationBroadcastService"));
    }

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(this, MyService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        Log.d("RunningTracker", "starting service");
        startService(intent);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("RunningTracker", "Stop service");
        stopService(intent);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("RunningTracker", "onMapReady");
        gmap = googleMap;
        gmap.setMyLocationEnabled(true);
    }

    public void moveCamera(LatLng latLng, float zoom){
        //gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        gmap.animateCamera(cameraUpdate);
    }
}
