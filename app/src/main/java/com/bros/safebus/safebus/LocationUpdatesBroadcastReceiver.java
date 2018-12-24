package com.bros.safebus.safebus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";
    public static final String USER_KEY = "userKey";
    public static final String USER_TYPE = "userType";
    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";
    private static String userKey;
    private static String userType;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                /*userKey= intent.getStringExtra(USER_KEY);
                userType = intent.getStringExtra(USER_TYPE);*/
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    Log.d("LOC", "LOC UPDATE" + locations);

                    HashMap<String, Double> locationDetails = new HashMap<String, Double>();
                    locationDetails.put("latitude", locations.get(locations.size() - 1).getLatitude());
                    locationDetails.put("longitude", locations.get(locations.size() - 1).getLongitude());

                    HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
                    currentLocation.put("currentLocation", locationDetails);

                    final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child(userType).child(userKey).child("location");
                    databaseref.setValue(currentLocation);

                }
            }
        }
    }

    public static void setUserKey(String UserKey){
        userKey = UserKey;
    }

    public static void setUserType(String UserType){
        userType = UserType;
    }
}
