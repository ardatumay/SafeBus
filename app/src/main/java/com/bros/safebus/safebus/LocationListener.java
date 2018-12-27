package com.bros.safebus.safebus;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.concurrent.Executor;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class LocationListener extends Service {

    private FusedLocationProviderClient mFusedLocationClient; //location provider client
    LocationRequest mLocationRequest; // location request
    Location currentLoc = null;
    private LocationCallback mLocationCallback;
    boolean mRequestingLocationUpdates = false;

    public static final String USER_KEY = "userKey";
    public static final String USER_TYPE = "userType";

    private String userKey;
    private String userType;

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.bros.safebus.safebus.action.FOO";
    private static final String ACTION_BAZ = "com.bros.safebus.safebus.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.bros.safebus.safebus.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.bros.safebus.safebus.extra.PARAM2";

    public LocationListener() {
        super();
    }

    public static final int NOTIFICATION_ID = 555;


    @Override
    public void onCreate() {
        //bussiness logic variables
        Log.i("destror", "onCreate() , created service");

        mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //get the location provider client
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
        }

    }

    private void RequestLocationUpdates(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    LocationCallback(),
                    null /* Looper */);
    }

    private void StopLocationUpdates(){
        this.mFusedLocationClient.removeLocationUpdates(LocationCallback());
    }

    void GetLoc() {//get the last location of the user and put the location value to the currentLoc variable
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
            mFusedLocationClient.getLastLocation().addOnSuccessListener((Executor) this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    currentLoc = location;

                }
            }
        });
    }
    @Override
    public void onDestroy() {

        Log.i("destror", "onCreate() , service stopped...");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        userKey= intent.getStringExtra(USER_KEY);
        userType = intent.getStringExtra(USER_TYPE);
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child(userType).child(userKey).child("trackLocation");
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!(boolean)dataSnapshot.getValue()){
                    StopLocationUpdates();
                }else{
                    RequestLocationUpdates();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LocationListener.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, LocationListener.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    //@Override
    protected void onHandleIntent(Intent intent) {
      /*  synchronized (this) {
            try{
                wait(1500);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }*/
       // user_key = intent.getStringExtra(USER_KEY);
      //  user_type = intent.getStringExtra(USER_TYPE);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    LocationCallback(),
                    null /* Looper */);
        }
        /*if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FOO.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionFoo(param1, param2);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }*/
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public LocationCallback LocationCallback(){
        mLocationCallback = new LocationCallback() {//callback function to get location updates
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {//location gives the updated location
                    //Log.d("LOC", location.toString());
                    // Update UI with location data
                    // ...

                    Log.w("LOCATION", "onLocationResult: " + location.toString() );
                    //firebase cannot serialize arrays, it must be put in dictionary like data structure which is hashmap
                    HashMap<String,Double> locationDetails = new HashMap<String, Double>();
                    locationDetails.put("latitude", location.getLatitude());
                    locationDetails.put("longitude", location.getLongitude());

                    HashMap<String,HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
                    currentLocation.put("currentLocation", locationDetails);

                    SendDataFirebase(currentLocation);
                }
            };
        };
        return mLocationCallback;
    }

    void SendDataFirebase(HashMap<String,HashMap<String, Double>> currentLocation){
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child(userType).child(userKey).child("location");
        databaseref.setValue(currentLocation);
    }

}
