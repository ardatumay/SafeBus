package com.bros.safebus.safebus;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnSuccessListener;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bros.safebus.safebus.models.Child;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;






import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String userType = "";

 private FusedLocationProviderClient mFusedLocationClient; //location provider client
    LocationRequest mLocationRequest; // location request
    Location currentLoc = null;
    private LocationCallback mLocationCallback;
    boolean mRequestingLocationUpdates = false;
    private GeofencingClient mGeofencingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //user interface variables
        final TextView updatedLocation = (TextView) findViewById(R.id.updated_Location);
        final TextView CurrentLocation = (TextView) findViewById(R.id.current_Location);
        Button showLocation = (Button) findViewById(R.id.show_Location);
        //////////////////////FIREBASE RELATED////////////////////
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("/children/0/busPlate");
        //myRef.setValue("Hello, Worlddddd!");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                java.lang.Object value = dataSnapshot.getValue();
                setTextView(CurrentLocation, String.valueOf(value));
                Log.d("VALUE", "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("VALUE", "Failed to read value.", error.toException());
            }
        });
        //////////////////////FIREBASE RELATED////////////////////

        //bussiness logic variables
        mLocationRequest = LocationUtil.CreateLocationRequest();  //create the location request
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
                    updatedLocation.setText(location.toString());
                }
            };
        };
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);
        final String sa ;
       // final Button register = (Button) findViewById(R.id.register);
        final Button login = (Button) findViewById(R.id.login_Button);
       // final TextView firstTextView = (TextView) findViewById(R.id.textView);
        final Button sign_up = (Button) findViewById(R.id.signup_Button);


        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRegisterPage();
            }
        });


       /* sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddr = email.getText().toString();
                String pass = password.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(emailAddr, pass)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firstTextView.setText("Signing up");
                                    kullaniciOlustur();
                                    kullaniciGuncelle();
                                } else {
                                    Log.e("Yeni Kullanıcı Hatası", task.getException().getMessage());
                                }


        showLocation.setOnClickListener(new View.OnClickListener() {// When show location button is clicked show the location of the currentLoc variable
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
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean mRequestingLocationUpdates = true;
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    private void startLocationUpdates() {
        checkLocationPermission();
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }




    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }




    void GetLoc(){//get the last location of the user and put the location value to the currentLoc variable
        checkLocationPermission();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            // Logic to handle location object
                            currentLoc = location;

                            }
                        });
            }
        });*/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userLoginEmail = email.getText().toString();
                String userLoginPassword = password.getText().toString();

                if(!TextUtils.isEmpty(userLoginEmail)&& !TextUtils.isEmpty(userLoginPassword) && userType != "") {
                    Log.d("USER LOGIN MAIL","CALL LOGIN" );

                    loginUser(userLoginEmail, userLoginPassword);
                }else{
                    Log.d("USER LOGIN TYPE", "LOGIN EMPTY");
                    Toast.makeText(MainActivity.this, "Failed Login: Empty Inputs are not allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });
       // login.setOnClickListener(new View.OnClickListener() {
          //  @Override
          //  public void onClick(View v) {
             //   firebaseAuth.signInWithEmailAndPassword(
             //            email.getText().toString(),
             //           password.getText().toString())
              //          .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
              //              @Override
               //             public void onComplete(@NonNull Task<AuthResult> task) {
               //                 if (task.isSuccessful()) {
               //                     loginUser(email, password);
                                   // firstTextView.setText("Logging in");
                                   // openActivity();
                                   // startActivity(new Intent(getApplication(), User.class));
                  //              } else {
                  //                  Log.e("Log in error", task.getException().toString());
                 //               }
                 //           }
                 //       });
           // }
       // });
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.userTypeRadio);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedButton = (RadioButton) findViewById(checkedId);
                Log.d("DRIVER", "NEW RADIO CLICK" + checkedButton.getText().toString().toLowerCase() );
                   /* Log.d("DRIVER", "onCheckedChanged: " +checkedButton.getText().toString().toLowerCase() );
                    SetUserType("drivers");
                    Log.d("CHILDREN", "onCheckedChanged: " +checkedButton.getText().toString().toLowerCase() );
                    SetUserType("children");
                    Log.d("PARENT", "onCheckedChanged: " +checkedButton.getText().toString().toLowerCase() );*/

                    SetUserType(checkedButton.getText().toString().toLowerCase());

            }
        });
    }
    private void openActivity() {
        Intent intent = new Intent (MainActivity.this, User.class);
        startActivity(intent);
    }

    void SetUserType(String val){
        Log.d("SET USERTYPE", "NEW VAL" + val );
        userType = val;
    }

    private void loginUser(final String userLoginEmail, final String userLoginPassword) {
        firebaseAuth.signInWithEmailAndPassword(userLoginEmail, userLoginPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();
                            final String RegisteredUserID = currentUser.getUid();

                            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child(userType).child(RegisteredUserID);

                            databaseref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String userType = dataSnapshot.child("type").getValue().toString().toLowerCase();
                                    String key = dataSnapshot.child("key").getValue().toString();

                                    if(userType.equals("parents")){
                                        /*Intent intentResident = new Intent(MainActivity.this, register.class);
                                        intentResident.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intentResident);
                                        finish();*/
                                        GoToParentHome(key);
                                    }else if (userType.equals("drivers")){
                                        /*Intent intentMain = new Intent(MainActivity.this, register.class);
                                        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intentMain);
                                        finish();*/
                                        GoToDriverHome(key);

                                    }else if(userType.equals("children")) {
                                        GoToChildrenHome(key);
                                    }
                                    else{
                                            Toast.makeText(MainActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                    }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }


                            });
                        }
                    }
                });
    }




    void setTextView(TextView targetTextView, String targetString){//set textView content
        targetTextView.setText(targetString);
    }




    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() { //check whether the app has enough permissions for location services
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

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
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
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


    void GoToParentHome(String userKey){
        Intent i = new Intent(this, ParentInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
    void GoToChildrenHome(String userKey){
        Intent i = new Intent(this, ChildrenInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
    void GoToDriverHome(String userKey){
        Intent i = new Intent(this, DriverInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
   /* private void kullaniciOlustur() {
        Map<String, String> yeniUser = new HashMap<String, String>();
        yeniUser.put("name", "name");
        yeniUser.put("surname", "surname");

        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.child("users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .setValue(yeniUser);
    }
    private void kullaniciGuncelle() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName("nick ver")
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(userProfileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e("update error", task.getException().getMessage());
                        }
                       // startActivity(new Intent(MainActivity.this, User.class));
                    }
                });

    }*/
    void changeRegisterPage()
    {
        Intent register_intent = new Intent(this, register.class);
        startActivity(register_intent);
    }

}
