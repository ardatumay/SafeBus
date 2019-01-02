package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bros.safebus.safebus.MapsActivity;
import com.bros.safebus.safebus.R;

public class ParentChildInterface extends Activity {

    Button seeTheMap;
    Button addHomeAddress;
    Button addSchoolAddress;
    Button stopLocationTracking;

    String childKey;
    String childUpperKey;
    String parentKey;

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
        stopLocationTracking = (Button) findViewById(R.id.stop_location_tracking);

        stopLocationTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

    }

    void GoToMapPage(String childKey, String childUpperKey, String parentKey, boolean marksHome, boolean marksSchool) {

        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("driverControl", false);
        i.putExtra("parentMarksMapSchool", marksSchool);
        i.putExtra("parentMarksMapHome", marksHome);
        i.putExtra("parentKey", parentKey);
        i.putExtra("childKey", childKey);
        i.putExtra("childUpperKey", childUpperKey);
        startActivity(i);
    }

    void StopLocationTracking(){

    }

}
