/******************************************************************************
 *  Class Name: ParentChildrenInterfface
 *  Author: Arda
 *
 *This class shows buttons related to childrean feautures
 * location tracking for children is enabled/disabled here
 * parent sees the map for locations
 * parent assign school and home addreesses here by clicking related buttons
 ******************************************************************************/

package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.bros.safebus.safebus.MapsActivity;
import com.bros.safebus.safebus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentChildInterface extends Activity {

    Button seeTheMap, addHomeAddress, addSchoolAddress ;
    Switch stopLocationTracking;
    public String childKey, childUpperKey, parentKey, childFullName;
    boolean  trackChildLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_child_interface);
        /******************************************************************************
         * required variables are initialized, user keys and buttons
         * Author: Arda
         ******************************************************************************/
        Intent intent = getIntent();
        childKey = intent.getStringExtra("childKey");
        childUpperKey = intent.getStringExtra("childUpperKey");
        parentKey = intent.getStringExtra("parentKey");
        childFullName = intent.getStringExtra("childFullName");
        seeTheMap = (Button) findViewById(R.id.see_the_map);
        addHomeAddress = (Button) findViewById(R.id.add_home_address);
        addSchoolAddress = (Button) findViewById(R.id.add_school_address);
        stopLocationTracking = (Switch) findViewById(R.id.stop_location_tracking);



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

        stopLocationTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    StopLocationTracking();
            }
        });

        /******************************************************************************
         * Firebase db listener for setting track location variable in the app
         * Author: Arda
         ******************************************************************************/
        final DatabaseReference databaserefNotify = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("trackLocation");
        databaserefNotify.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((boolean) dataSnapshot.getValue()) {
                    stopLocationTracking.setChecked(true);
                    trackChildLoc = true;
                } else {
                    stopLocationTracking.setChecked(false);
                    trackChildLoc = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    /******************************************************************************
     * goes to map page with required parameters
     * Author: Arda
     ******************************************************************************/
    void GoToMapPage(String childKey, String childUpperKey, String parentKey, boolean marksHome, boolean marksSchool) {

        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("trackChildLoc", trackChildLoc);
        i.putExtra("driverControl", false);
        i.putExtra("parentMarksMapSchool", marksSchool);
        i.putExtra("parentMarksMapHome", marksHome);
        i.putExtra("parentKey", parentKey);
        i.putExtra("childKey", childKey);
        i.putExtra("childUpperKey", childUpperKey);
        i.putExtra("childFullName", childFullName);
        startActivity(i);
    }

    /******************************************************************************
     * when toggle is pressed track location is off
     * Author: Arda
     ******************************************************************************/
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
