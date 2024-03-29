/******************************************************************************
 *  Class Name: ChildrenInterface
 *  Author:Arda
 *
 *  After child logs in, it gets the permission to get location and it gets
 *  the child's location
 *
 *  There is no any button seen to children since it has not required to take any action
 *  the only responsibility of the child is sending his location
 *
 ******************************************************************************/

package com.bros.safebus.safebus;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
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

public class ChildrenInterface extends Activity {
    /******************************************************************************
     *  Create location variables and childkey
     * Author: Arda
     ******************************************************************************/
    private FusedLocationProviderClient mFusedLocationClient; //location provider client
    LocationRequest mLocationRequest; // location request
    Location currentLoc = null;
    private LocationCallback mLocationCallback;
    boolean mRequestingLocationUpdates = false;
    private GeofencingClient mGeofencingClient;
    String childKey;
    Intent i;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.children_interface);
        Intent intent = getIntent();
        childKey = intent.getStringExtra("userKey");
        i = new Intent(getApplicationContext(), LocationListener.class);

        /******************************************************************************
         *Add the user key and user type to shared resource for permanent storage
         * then the broadcast receiver gets this information and sends the location info to the firebase
         * Author: Arda
         ******************************************************************************/
        preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        preferenceEditor = preferences.edit();
        preferenceEditor.putString("userKey", childKey);
        preferenceEditor.putString("userType", "children");
        preferenceEditor.commit();
        preferenceEditor.apply();

        /******************************************************************************
         *create location request, check permissions, register to the updates via pending intent
         * Author: Arda
         ******************************************************************************/
        checkLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //get the location provider client
        mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
/******************************************************************************
 * If location track is disabled, disable the service location tracking
 * Author: Arda
 ******************************************************************************/
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("trackLocation");
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(boolean) dataSnapshot.getValue()) {

                    DisableBroadcastReceiver();
                    mFusedLocationClient.removeLocationUpdates(getPendingIntent());
                } else {
                    enableBroadcastReceiver();
                    checkLocationPermission();
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //startService(new Intent(getApplicationContext(), LocationListener.class));

    }
    /******************************************************************************
     * If location tracking is disabled, disable the broadcast receiver in the device
     * Author: Arda
     ******************************************************************************/
    private void DisableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, LocationUpdatesBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    /******************************************************************************
     * If location tracking is enabled, enable the broadcast receiver in the device
     * Author: Arda
     ******************************************************************************/
    public void enableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, LocationUpdatesBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    /******************************************************************************
     * Create intent and register to broadcast receiver with pending intent
     * Author: Arda
     ******************************************************************************/
    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        /*intent.putExtra(LocationUpdatesBroadcastReceiver.USER_KEY, DriverKey);
        intent.putExtra(LocationUpdatesBroadcastReceiver.USER_TYPE, "drivers");
        sendBroadcast(intent);*/
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /******************************************************************************
     * Disable the back press in the home page
     * Author: Arda
     ******************************************************************************/
    @Override
    public void onBackPressed() {

    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    public void StartLocationService() {
        i.putExtra(LocationListener.USER_KEY, childKey);
        i.putExtra(LocationListener.USER_TYPE, "children");
        startService(i);
    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    public void StopLocationService() {
        //stopService(i);
        stopService(new Intent(this, LocationListener.class));
    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        boolean mRequestingLocationUpdates = true;
        checkLocationPermission();

        if (mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    private void startLocationUpdates() {
        checkLocationPermission();
       /* mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                LocationCallback(),
                null );*/
    }

    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    void setTextView(TextView targetTextView, String targetString) {//set textView content
        targetTextView.setText(targetString);
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    /******************************************************************************
     * Location permission is checked with this method
     * Author: Arda
     ******************************************************************************/
    public boolean checkLocationPermission() { //check whether the app has enough permissions for location services
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(ChildrenInterface.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);


                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            }

            return false;
        } else {
            return true;
        }

    }

    void GoToHome() {
        Intent in = new Intent(this, MainActivity.class);
        startActivity(in);
    }

    /******************************************************************************
     * Set the flag true for service if user gives permission
     * Author: Arda
     ******************************************************************************/
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    mRequestingLocationUpdates = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    mRequestingLocationUpdates = false;
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

}
