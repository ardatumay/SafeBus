/******************************************************************************
 *  Class Name: DriverInterface
 *  Author: Arda
 *
 * This is home page of the driver where several buttons are placed to interact with
 * Each button povides differet features
 * Driver can open map and mark the route
 * In addition driver can see the registered childrens' location in the map during marking route
 * Driver can add children to his service
 *
 *  Revisions: Efe: Added database checking for notifications for all children about school and home
 *             Can: Added intent switches, updated markers.
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DriverInterface extends Activity {
    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;
    private FusedLocationProviderClient mFusedLocationClient; //location provider client
    LocationRequest mLocationRequest; // location request
    Location currentLoc = null;
    private LocationCallback mLocationCallback;
    boolean mRequestingLocationUpdates = false;
    private GeofencingClient mGeofencingClient;
    String DriverKey;
    ArrayList<LatLng> listPoints;
    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.driver_interface);
        listPoints = new ArrayList<>();
        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /******************************************************************************
                 * if logout clicked delete the permanent user data from phone and logout the user
                 * shared resource structure provides key-value pair data structure which is a permanent storage
                 * It can be user as a soft database where there is no requirement for sqlite
                 * Author: Arda
                 ******************************************************************************/
                //empty shared preference for next login
                preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                preferenceEditor = preferences.edit();
                preferenceEditor.putString("userMail", "");
                preferenceEditor.putString("userPass", "");
                preferenceEditor.putString("userKey", "");
                preferenceEditor.putString("userType", "");
                preferenceEditor.commit();
                preferenceEditor.apply();

                //disable the broadcast receiver
                DisableBroadcastReceiver();

                //logout firebase auth

                firebaseAuth.signOut();
                GoToHome();
                finish();
            }
        });

        final RelativeLayout r = (RelativeLayout) findViewById(R.id.driver_interface);
        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }
        });
        //Get the driver key from intent - Arda
        DriverKey = getIntent().getStringExtra("userKey");
        /******************************************************************************
         * Get the locaton client and create location rewuest and start listening location updates via pendingt intent
         * that is registered to broadcast receiver
         * Author: Arda
         ******************************************************************************/
        checkLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //get the location provider client
        mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());

        /*Intent intent = new Intent(this, LocationListener.class);
        intent.putExtra(LocationListener.USER_KEY, DriverKey);
        intent.putExtra(LocationListener.USER_TYPE, "drivers");
        startService(intent);*/
        /******************************************************************************
         * If location track is disabled, disable the service location tracking
         * Author: Arda
         ******************************************************************************/
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("drivers").child(DriverKey).child("trackLocation");
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!(boolean) dataSnapshot.getValue()) {

                    DisableBroadcastReceiver();//disable boradcast receiver
                    mFusedLocationClient.removeLocationUpdates(getPendingIntent()); //remove pending intent from location client
                } else {
                    enableBroadcastReceiver();//enable broadcast receiver
                    checkLocationPermission();///check location permission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());//add pending intent from location client
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        /******************************************************************************
         * This adds List path into DB
         * Author: Can
         ******************************************************************************/
        listPoints = getIntent().getParcelableArrayListExtra("pathList");
        if (listPoints != null) {
            Log.v("listPath", listPoints.toString());
            final DatabaseReference databasePath = FirebaseDatabase.getInstance().getReference().child("drivers").child(DriverKey);
            databasePath.child("pathList").setValue(listPoints);
        }



        final Button addChild = (Button) findViewById(R.id.add_child);
        addChild.setOnClickListener(new View.OnClickListener() {
            boolean clicked = false;

            @Override
            public void onClick(View view) {
                EditText childMail = (EditText) findViewById(R.id.child_email);
                if (childMail.getText().toString().length() == 0) {
                    Toast.makeText(DriverInterface.this, "Please enter a valid input.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    AddChildToDriver(childMail.getText().toString());
                }
            }
        });

        Button addRoute = (Button) findViewById(R.id.add_route);

        addRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DriverInterface.this, MapsActivity.class);
                intent.putExtra("driverControl", true);
                intent.putExtra("driverKey", DriverKey);
                startActivity(intent);
            }
        });
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
        preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        preferenceEditor = preferences.edit();
        preferenceEditor.putString("userKey", DriverKey);
        preferenceEditor.putString("userType", "drivers");
        preferenceEditor.commit();
        preferenceEditor.apply();
        //Intent intent = new Intent();
        Intent intent = new Intent ();

        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        //intent.setPackage(getApplicationContext().getPackageName());
        intent.setClass(getApplicationContext(), LocationUpdatesBroadcastReceiver.class);

        /*intent.putExtra(LocationUpdatesBroadcastReceiver.USER_KEY, DriverKey);
        intent.putExtra(LocationUpdatesBroadcastReceiver.USER_TYPE, "drivers");
        sendBroadcast(intent);*/

        return PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    /******************************************************************************
     * Disable back press in home page
     * Author: Arda
     ******************************************************************************/
    @Override
    public void onBackPressed() {

    }
    /******************************************************************************
     * When add child button is pressed system get the entered mail and search it in the firebase db
     * if any child found, add it to driver but if not, give alert
     * Author: Arda
     ******************************************************************************/
    void AddChildToDriver(String userMail) {
        String actualUsername = CreateUsernameFromEmail(userMail);
        final TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
        Task dbTask = dbSource.getTask();

        final DatabaseReference databaseref = firebaseDatabase.getReference().child("users").child(actualUsername);
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() != 0) {
                    dbSource.setResult(dataSnapshot);
                } else {
                    Toast.makeText(DriverInterface.this, "Child not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
                Toast.makeText(DriverInterface.this, "Child not found.", Toast.LENGTH_SHORT).show();
            }
        });

        dbTask.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    DataSnapshot result = task.getResult();

                    Log.w("user mail", "USERMAIL:" + result.toString());
                    HashMap<String, String> child = new HashMap<String, String>();
                    String childKey = result.child("key").getValue(String.class);
                    child.put("childKey", childKey);
                    //bound child to driver
                    databaseReference = firebaseDatabase.getReference();
                    String keyForDriver = databaseReference.child("drivers").child(DriverKey).child("children").push().getKey();
                    databaseReference.child("drivers").child(DriverKey).child("children").child(keyForDriver).setValue(child);

                    //bound driver to child
                    databaseReference = firebaseDatabase.getReference();
                    databaseReference.child("children").child(childKey).child("driverKey").setValue(DriverKey);
                }
            }
        });
    }
    /******************************************************************************
     * Helper method to search child in DB
     * Author: Arda
     ******************************************************************************/
    String CreateUsernameFromEmail(String email) {
        String src1 = ExtractCharFromString(email, "@");
        String src2 = ExtractCharFromString(src1, ".");
        return src2;
    }
    /******************************************************************************
     * Helper method to search child in DB
     * Author: Arda
     ******************************************************************************/
    String ExtractCharFromString(String src, String trgt) {
        String newSrc = src.replace(trgt, "");
        return newSrc;

    }

    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    @Override
    protected void onResume() {
        super.onResume();
        boolean mRequestingLocationUpdates = true;
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
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }

    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    @Override
    protected void onPause() {
        super.onPause();
        // stopLocationUpdates();
    }
    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /******************************************************************************
     * Not working since we use broadcast receiver tyo get the location
     * Author: Arda
     ******************************************************************************/
    void GetLoc() {//get the last location of the user and put the location value to the currentLoc variable
        checkLocationPermission();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
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
    protected void onDestroy() {
        super.onDestroy();


    }
    /******************************************************************************
     * go to login page
     * Author: Arda
     ******************************************************************************/
    void GoToHome() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
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
                                ActivityCompat.requestPermissions(DriverInterface.this,
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
