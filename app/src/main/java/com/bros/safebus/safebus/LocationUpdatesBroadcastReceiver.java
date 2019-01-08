package com.bros.safebus.safebus;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.List;

import static android.provider.Settings.Global.getString;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "LUBroadcastReceiver";
    public static final String USER_KEY = "userKey";
    public static final String USER_TYPE = "userType";
    static final String ACTION_PROCESS_UPDATES =
            "com.google.android.gms.location.sample.backgroundlocationupdates.action" +
                    ".PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String USERKEY = preferences.getString("userKey", "");
        String USERTYPE = preferences.getString("userType", "");

        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> locations = result.getLocations();
                    Log.d("LOC", "LOC UPDATE" + locations);
                    Log.d("LOC", "LOC HAS SPEED" + locations.get(locations.size() - 1).hasSpeed());


                    HashMap<String, Double> locationDetails = new HashMap<String, Double>();
                    locationDetails.put("latitude", locations.get(locations.size() - 1).getLatitude());
                    locationDetails.put("longitude", locations.get(locations.size() - 1).getLongitude());
                    locationDetails.put("speed", (double) locations.get(locations.size() - 1).getSpeed());

                    HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
                    currentLocation.put("currentLocation", locationDetails);

                    final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child(USERTYPE).child(USERKEY).child("location");
                    databaseref.setValue(currentLocation);

                }
            }
        }
    }

}
