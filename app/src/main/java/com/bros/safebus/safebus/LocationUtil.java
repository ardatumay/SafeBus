/******************************************************************************
 *  Class Name: LocatinUtil
 *  Author: Arda
 *
 * helper class for createing location request
 ******************************************************************************/
package com.bros.safebus.safebus;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationUtil {


    /******************************************************************************
     * This method creates a location request and builds it to set required location paremeters for location tracking
     * priority of locaiton and intervals are set here
     * Author: Arda
     ******************************************************************************/
    public static LocationRequest CreateLocationRequest() { //create a location request for the location updates
        LocationRequest mLocationRequest = new LocationRequest(); // location request
        mLocationRequest.setInterval(1500);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        builder.build();
        Log.w("location", "LOCATION REQUEST SENT");
        return mLocationRequest;
    }
}
