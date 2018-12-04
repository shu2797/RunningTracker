package com.example.mayur.runningtracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    TextView tv_Distance, tv_Time;
    FloatingActionButton startFAB, stopFAB, pauseFAB;

    Boolean isBound;
    Boolean tracking = false;

    MyService ms;

    Location location, oLocation;
    SupportMapFragment supportMapFragment;
    GoogleMap gmap;

    Intent intent;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    float dis, tot_dist = (float) 0.00;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;

    Boolean permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        tv_Distance = findViewById(R.id.tv_Distance);
        tv_Time = findViewById(R.id.tv_Time);
        stopFAB = findViewById(R.id.tracking_stopFAB);
        startFAB = findViewById(R.id.tracking_startFAB);
        pauseFAB = findViewById(R.id.tracking_pauseFAB);


        //permission = checkLocationPermission();

        //checkLocationPermission();

        if(checkLocationPermission()){
            supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
            supportMapFragment.getMapAsync(this);
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            Log.d("RunningTracker", "onReceive");
                            //Float dis = intent.getExtras().getFloat("dist");
                            //Location location1 = intent.getExtras().getParcelable("oloc");
                            location = intent.getExtras().getParcelable("loc");

                            //Float dis = location1.distanceTo(location);
                            //String distance = String.format("%.2f", dis);

                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            moveCamera(latLng, 18.5f);
                            //Log.d("RunningTracker", "Distance: " + distance);
                            //tv_Distance.setText(distance + "m");
                            try{
                                if (tracking) {
                                    dis = oLocation.distanceTo(location)/1000;
                                    float show_dist = dis + tot_dist;
                                    String distance = String.format("%.2f", show_dist);
                                    Log.d("RunningTracker", "Distance: " + distance);
                                    tv_Distance.setText(distance + "m");
                                }
                            } catch (Exception e){}
                        }
                    }
                    , new IntentFilter("LocationBroadcastService"));
        }


        handler = new Handler();

        startFAB.show();
        pauseFAB.hide();
        stopFAB.hide();

        startFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("RunningTracker", "starting service");
                startService(intent);
                StartTime = SystemClock.uptimeMillis();
                handler.postDelayed(runnable, 0);
                tracking = true;
                startFAB.hide();
                pauseFAB.show();
                stopFAB.show();

                tracking = true;
                oLocation = location;

            }
        });

        pauseFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeBuff += MillisecondTime;
                handler.removeCallbacks(runnable);

                tracking = false;
                tot_dist = dis;

                startFAB.show();
            }
        });

        stopFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(runnable);

                stopFAB.hide();
                pauseFAB.hide();
                startFAB.show();

                MillisecondTime = 0L ;
                StartTime = 0L ;
                TimeBuff = 0L ;
                UpdateTime = 0L ;
                Seconds = 0 ;
                Minutes = 0 ;
                MilliSeconds = 0 ;


                tracking = false;


            }
        });
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            Log.d("RunningTime", ""+Minutes+":"+String.format("%02d",Seconds)+":"+String.format("%03d",MilliSeconds));

            tv_Time.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds) + ":"
                    + String.format("%03d", MilliSeconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    protected void onStart() {
        super.onStart();
        intent = new Intent(this, MyService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("RunningTracker", "Stop service");
        handler.removeCallbacks(runnable);
        stopService(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        moveTaskToBack(true);
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
        gmap.getUiSettings().setZoomControlsEnabled(true);
        stopService(intent);
    }

    public void moveCamera(LatLng latLng, float zoom){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,zoom);
        gmap.animateCamera(cameraUpdate);
    }


    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
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
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 0: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                        Toast.makeText(this, "Location permissions granted", Toast.LENGTH_SHORT).show();
                        Log.d("RunningTracker", "Permission granted");
                        supportMapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
                        supportMapFragment.getMapAsync(this);
                        LocalBroadcastManager.getInstance(this).registerReceiver(
                                new BroadcastReceiver() {
                                    @Override
                                    public void onReceive(Context context, Intent intent) {
                                        Log.d("RunningTracker", "onReceive");
                                        //Float dis = intent.getExtras().getFloat("dist");
                                        //Location location1 = intent.getExtras().getParcelable("oloc");
                                        location = intent.getExtras().getParcelable("loc");

                                        //Float dis = location1.distanceTo(location);
                                        //String distance = String.format("%.2f", dis);

                                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                        moveCamera(latLng, 18.5f);
                                        //Log.d("RunningTracker", "Distance: " + distance);
                                        //tv_Distance.setText(distance + "m");
                                        try{
                                            if (tracking) {
                                                dis = oLocation.distanceTo(location)/1000;
                                                float show_dist = dis + tot_dist;
                                                String distance = String.format("%.2f", show_dist);
                                                Log.d("RunningTracker", "Distance: " + distance);
                                                tv_Distance.setText(distance + "m");
                                            }
                                        } catch (Exception e){}
                                    }
                                }
                                , new IntentFilter("LocationBroadcastService"));
                        startService(intent);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(this, "Please restart app and allow location permissions", Toast.LENGTH_SHORT).show();
                    Log.d("RunningTracker", "Permission denied");
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

                }
                return;
            }
        }
    }
}
