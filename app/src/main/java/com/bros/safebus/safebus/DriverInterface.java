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
        //Get the driver key from intent
        DriverKey = getIntent().getStringExtra("userKey");

        //bussiness logic variables
        /*mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //get the location provider client
        mGeofencingClient = LocationServices.getGeofencingClient(this);
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
                    Log.w("LOCATION", "onLocationResult: " + location.toString());
                    //firebase cannot serialize arrays, it must be put in dictionary like data structure which is hashmap
                    HashMap<String, Double> locationDetails = new HashMap<String, Double>();
                    locationDetails.put("latitude", location.getLatitude());
                    locationDetails.put("longitude", location.getLongitude());

                    HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
                    currentLocation.put("currentLocation", locationDetails);
                    Intent intent = getIntent();
                    String childKey = intent.getStringExtra("userKey");
                    final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("drivers").child(childKey).child("location");
                    databaseref.setValue(currentLocation);
                    //updatedLocation.setText(location.toString());
                }
            }
        };*/

        checkLocationPermission();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this); //get the location provider client
        mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());

        /*Intent intent = new Intent(this, LocationListener.class);
        intent.putExtra(LocationListener.USER_KEY, DriverKey);
        intent.putExtra(LocationListener.USER_TYPE, "drivers");
        startService(intent);*/

        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("drivers").child(DriverKey).child("trackLocation");
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


        //adding List path into DB
        listPoints = getIntent().getParcelableArrayListExtra("pathList");
        if (listPoints != null) {
            Log.v("listPath", listPoints.toString());
            final DatabaseReference databasePath = FirebaseDatabase.getInstance().getReference().child("drivers").child(DriverKey);
            databasePath.child("pathList").setValue(listPoints);
        }


       /*showLocation.setOnClickListener(new View.OnClickListener() {// When show location button is clicked show the location of the currentLoc variable
            @Override
            public void onClick(View view) {
                GetLoc();
                if (currentLoc == null) {
                    setTextView(CurrentLocation, "Location Object Is Null");

                } else {
                    setTextView(CurrentLocation, currentLoc.toString());
                    //setTextView(CurrentLocation, currentLoc.getLatitude() + " " + currentLoc.getAltitude() + " with accuracy " + currentLoc.getAccuracy() );
                }
            }
        });*/

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

              /* if(!clicked){
                   EditText childMail =(EditText) findViewById(R.id.child_email);
                   childMail.setVisibility(View.VISIBLE);
                   TranslateAnimation editTextAnim = new TranslateAnimation(1500.0f,0.0f , 0.0f, 0.0f); // new TranslateAnimation (float fromXDelta,float toXDelta, float fromYDelta, float toYDelta)
                   editTextAnim.setDuration(1500); // animation duration
                   //animation.setRepeatCount(4); // animation repeat count
                   //animation.setRepeatMode(2); // repeat animation (left to right, right to left)
                   //animation.setFillAfter(true);
                   childMail .startAnimation(editTextAnim);//your_view for mine is imageView

                   TranslateAnimation buttonAnim = new TranslateAnimation(0.0f,0.0f , 0.0f, 150.0f); // new TranslateAnimation (float fromXDelta,float toXDelta, float fromYDelta, float toYDelta)
                   buttonAnim.setDuration(1500); // animation duration
                   buttonAnim.setAnimationListener(new Animation.AnimationListener() {
                       @Override
                       public void onAnimationStart(Animation animation) {

                       }

                       @Override
                       public void onAnimationEnd(Animation animation) {
                           /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                           // change the coordinates of the view object itself so that on click listener reacts to new position
                           view.layout(view.getLeft()+200, view.getTop(), view.getRight()+200, view.getBottom());
                           repeatLevelSwitch.clearAnimation();



                           // set new "real" position of wrapper
                           RelativeLayout.LayoutParams lpForAddChild = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                          //lpForAddChild.addRule(RelativeLayout.BELOW, R.id.add_route);
                           addChild.layout(addChild.getLeft(), addChild.getTop() + 1500, addChild.getRight(), addChild.getBottom());
                           addChild.setLayoutParams(lpForAddChild);
                           // clear animation to prevent flicker
                           addChild.clearAnimation();
                       }

                       //@Override
                       public void onAnimationRepeat(Animation animation) {

                       }
                   });
                   buttonAnim.setFillAfter(true);
                   addChild.startAnimation(buttonAnim);//your_view for mine is imageView
                   clicked = true;
               }else{
                   EditText childMail =(EditText) findViewById(R.id.child_email);
                   TranslateAnimation editTextAnim = new TranslateAnimation(0.0f,1500.0f , 0.0f, 0.0f); // new TranslateAnimation (float fromXDelta,float toXDelta, float fromYDelta, float toYDelta)
                   editTextAnim.setDuration(1500); // animation duration
                   //animation.setRepeatCount(4); // animation repeat count
                   //animation.setRepeatMode(2); // repeat animation (left to right, right to left)
                   //animation.setFillAfter(true);
                   childMail .startAnimation(editTextAnim);//your_view for mine is imageView

                   TranslateAnimation buttonAnim = new TranslateAnimation(0.0f,0.0f , 150.0f, 0.0f); // new TranslateAnimation (float fromXDelta,float toXDelta, float fromYDelta, float toYDelta)
                   buttonAnim.setDuration(1500); // animation duration
                   buttonAnim.setAnimationListener(new Animation.AnimationListener() {
                       @Override
                       public void onAnimationStart(Animation animation) {

                       }

                       @Override
                       public void onAnimationEnd(Animation animation) {
                           /*RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
                           // change the coordinates of the view object itself so that on click listener reacts to new position
                           view.layout(view.getLeft()+200, view.getTop(), view.getRight()+200, view.getBottom());
                           repeatLevelSwitch.clearAnimation();

                           // clear animation to prevent flicker
                           addChild.clearAnimation();

                           // set new "real" position of wrapper
                           RelativeLayout.LayoutParams lpForAddChild = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                           lpForAddChild.removeRule(RelativeLayout.BELOW);
                           lpForAddChild.addRule(RelativeLayout.BELOW, R.id.add_route);
                           addChild.setLayoutParams(lpForAddChild);
                       }

                       @Override
                       public void onAnimationRepeat(Animation animation) {

                       }
                   });
                   childMail.setVisibility(View.INVISIBLE);
                   buttonAnim.setFillAfter(true);
                   buttonAnim.setFillEnabled(true);
                   addChild.startAnimation(buttonAnim);//your_view for mine is imageView
                   clicked = false;
               }*/

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

    private void DisableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, LocationUpdatesBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void enableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, LocationUpdatesBroadcastReceiver.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }


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

    @Override
    public void onBackPressed() {

    }

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

    String CreateUsernameFromEmail(String email) {
        String src1 = ExtractCharFromString(email, "@");
        String src2 = ExtractCharFromString(src1, ".");
        return src2;
    }

    String ExtractCharFromString(String src, String trgt) {
        String newSrc = src.replace(trgt, "");
        return newSrc;

    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean mRequestingLocationUpdates = true;
        if (mRequestingLocationUpdates) {
            // startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


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

    void GoToHome() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    void setTextView(TextView targetTextView, String targetString) {//set textView content
        targetTextView.setText(targetString);
    }


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

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
