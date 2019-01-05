package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;

import com.bros.safebus.safebus.MapsActivity;
import com.bros.safebus.safebus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentChildInterface extends Activity {

    Button seeTheMap;
    Button addHomeAddress;
    Button addSchoolAddress;
    Button stopLocationTracking;

    String childKey;
    String childUpperKey;
    String parentKey;

    boolean  trackChildLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_child_interface);
        Intent intent = getIntent();
        childKey = intent.getStringExtra("childKey");
        childUpperKey = intent.getStringExtra("childUpperKey");
        parentKey = intent.getStringExtra("parentKey");

        seeTheMap = (Button) findViewById(R.id.see_the_map);
        addHomeAddress = (Button) findViewById(R.id.add_home_address);
        addSchoolAddress = (Button) findViewById(R.id.add_school_address);
        stopLocationTracking = (Button) findViewById(R.id.stop_location_tracking);

        stopLocationTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StopLocationTracking();
            }
        });

        seeTheMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToMapPage(childKey, childUpperKey, parentKey, false, false);
            }
        });

        addHomeAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToMapPage(childKey, childUpperKey, parentKey, true, false);

            }
        });

        addSchoolAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToMapPage(childKey, childUpperKey, parentKey, false, true);

            }
        });

        final DatabaseReference databaserefNotify = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("trackLocation");
        databaserefNotify.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()) {
                    stopLocationTracking.setText("STOP LOCATION TRACKING");
                    trackChildLoc = true;
                } else {
                    stopLocationTracking.setText("START LOCATION TRACKING");
                    trackChildLoc = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    void GoToMapPage(String childKey, String childUpperKey, String parentKey, boolean marksHome, boolean marksSchool) {

        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("trackChildLoc", trackChildLoc);
        i.putExtra("driverControl", false);
        i.putExtra("parentMarksMapSchool", marksSchool);
        i.putExtra("parentMarksMapHome", marksHome);
        i.putExtra("parentKey", parentKey);
        i.putExtra("childKey", childKey);
        i.putExtra("childUpperKey", childUpperKey);
        startActivity(i);
    }

    void StopLocationTracking() {
        final DatabaseReference databaserefNotify = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("trackLocation");
        databaserefNotify.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()) {
                    databaserefNotify.setValue(false);

                } else {
                    databaserefNotify.setValue(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

}
