/******************************************************************************
 *  Class Name: MapsActivity
 *  Author: Can
 *
 * This class creates the map, shows in the application, draws the path on the map
 * The class gets the child's and driver's location and shows it to the parent.
 * Parent can see dynamic markers which is showing child's and drivers information.
 * Also Driver can choose home and school from the tag names.
 *
 *  Revisions: Efe: Added calculation of distance between child-home and home-child
 *             Efe: Added proper notification for the home and school
 *             Arda: Added calculation of distance between child and driver
 *             Arda: Added proper notification for the child and school bus
 *             Arda, Efe and Can: Creates required firebase calls for getting data
 ******************************************************************************/

package com.bros.safebus.safebus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> listPoints;
    ArrayList<LatLng> listPointsChildLoc, listPointsDriverLoc, listPointsHomeLoc, listPointsSchoolLoc;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String userType = "";
    TextView distanceView, serviceSpeedInput, servideSpeed;
    Button submit;
    //variables for parent to mark home and school address on the map
    Boolean parentMarksMapSchool, parentMarksMapHome;
    LatLng homeAddress, schoolAddress;
    private int buttonId = 0;
    private static boolean driverControl = false;
    String childKey;
    boolean trackChildLoc;
    Marker HomeMarker, SchoolMarker;
    boolean takeSchool = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        /******************************************************************************
         * Initialize variables required for holding gps
         * Author: Can
         ******************************************************************************/
        mapFragment.getMapAsync(this);
        listPoints = new ArrayList<>();
        listPointsChildLoc = new ArrayList<>();
        listPointsDriverLoc = new ArrayList<>();
        listPointsHomeLoc = new ArrayList<>();
        listPointsSchoolLoc = new ArrayList<>();
        /******************************************************************************
         * Get some user interface components
         * Author: Arda
         ******************************************************************************/
        //Make both distance view and submit button invisible and make each one visible depending on user mode, e.g. showing location of student and service or marking school address
        distanceView = (TextView) findViewById(R.id.distance);
        distanceView.setVisibility(View.INVISIBLE);
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onSubmit();
            }
        });

        submit.setVisibility(View.INVISIBLE);
        serviceSpeedInput = (TextView) findViewById(R.id.service_speed_input);
        serviceSpeedInput.setVisibility(View.INVISIBLE);
        servideSpeed = (TextView) findViewById(R.id.service_speed);
        servideSpeed.setVisibility(View.INVISIBLE);

        childKey = GetChildKey();
        Log.w("CHILD KEY", "CHILDKEY" + childKey);

        listPoints = new ArrayList<>();
        driverControl = GetDriverControl();


        /******************************************************************************
         * If user is not a driver, he is parent. Children does not reach the map
         * Author: Arda
         ******************************************************************************/
        if (driverControl == false) {

            /******************************************************************************
             * Get path list saved in driver in firebase
             * Author: Can
             ******************************************************************************/
            final DatabaseReference databaserefForDriver = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
            databaserefForDriver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    Log.w("Driver key", "DRIVERKEYT" + dataSnapshot.toString());
                    if (dataSnapshot.getValue() != null) {
                        final String driverKey = (String) dataSnapshot.getValue();
                        final DatabaseReference databaserefForDriverPathlist = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey).child("pathList");
                        databaserefForDriverPathlist.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int i = 0, num = 0;

                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    LatLng point = new LatLng(dataSnapshot.child(String.valueOf(i)).child("latitude").getValue(Double.class), dataSnapshot.child(String.valueOf(i)).child("longitude").getValue(Double.class));
                                    //Use the dataType you are using and also use the reference of those childs inside arrays\\
                                    listPoints.add(point);
                                    Log.v("pointList", "pointlists" + listPoints.toString());
                                    if (i >= 1) {
                                        String url = getRequestUrl(listPoints.get(num), listPoints.get(num + 1));
                                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                                        taskRequestDirections.execute(url);
                                        num++;
                                    }
                                    i++;
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


            /******************************************************************************
             * Variables for deciding map mode: marking home, marking school or showing locations
             * if statemtent controls which feature to enable
             * Author: Arda
             ******************************************************************************/
            Intent incomingIntent = getIntent();
            parentMarksMapHome = incomingIntent.getBooleanExtra("parentMarksMapHome", false);
            parentMarksMapSchool = incomingIntent.getBooleanExtra("parentMarksMapSchool", false);
            trackChildLoc = incomingIntent.getBooleanExtra("trackChildLoc", false);


            if (!parentMarksMapSchool && !parentMarksMapHome && trackChildLoc) {
                serviceSpeedInput.setVisibility(View.VISIBLE);
                servideSpeed.setVisibility(View.VISIBLE);
                distanceView.setVisibility(View.VISIBLE);
                Log.w("parent", "parent marks map");
                /******************************************************************************
                 * Adds a value listener to check the changes in children's location
                 * Author: Arda
                 ******************************************************************************/
                final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("location").child("currentLocation");
                databaseref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w("Child loc", "CHILDLOC" + dataSnapshot.child("latitude").getValue(Double.class));
                        Log.w("Child loc", "CHILDLOC" + dataSnapshot.child("longitude").getValue(Double.class));

                        /******************************************************************************
                         * Gets the current location of children and calculate distance between childeren's location
                         * and the driver's location. If it is greater than that we wanted, it sets the notification true
                         * Author: Arda
                         ******************************************************************************/   
                        if (dataSnapshot.child("longitude").getValue(Double.class) != null) {
                            LatLng ltlng = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));

                            listPointsChildLoc.add(ltlng);
                            MarkMap();
                            Log.w("Child loc", "CHILDLOCSIZE" + listPointsChildLoc.size());
                            if (listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0) {//eğer bi list in uzunluğu sıfırsa sıkıntı çıkarabilir dikkar et !!
                                LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                                LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);

                                if (distance > 10) {
                                    distanceView.setTextColor(getResources().getColor(R.color.red));
                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notify");
                                    databaserefForParentNotif.setValue(true);
                                } else {
                                    distanceView.setTextColor(getResources().getColor(R.color.green));
                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notify");
                                    databaserefForParentNotif.setValue(false);
                                }
                                Log.w("DISTANCEchild", "DISTANCE" + distance);


                                String num = String.valueOf(distance);
                                int index = num.indexOf('.');
                                String result = num.substring(0, index + 3);
                                distanceView.setText(result + " M");
                                MarkMap();
                            }

                          

                        /******************************************************************************
                         * Gets the location of home and calculate distance between childeren's location
                         * and the home's location. If it is greater than that we wanted, it sets the notification true
                         * Author: Efe
                         ******************************************************************************/
 

                            databaseref.getParent().getParent().child("homeAddress").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("latitude").getValue(Double.class) != null) {
                                        LatLng ltlnghome = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                                        listPointsHomeLoc.add(ltlnghome);
                                        Log.w("Child loc", "efe" + listPointsHomeLoc.size());

                                    } else {
                                        // Toast.makeText(getApplicationContext(), "Mark home address.", Toast.LENGTH_SHORT).show();
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if (listPointsHomeLoc.size() > 0 && listPointsChildLoc.size() > 0) {//eğer bi list in uzunluğu sıfırsa sıkıntı çıkarabilir dikkar et !!
                                LatLng homeLoc = listPointsHomeLoc.get(listPointsHomeLoc.size() - 1);
                                LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, homeLoc.latitude, homeLoc.longitude);
                                if (distance < 10) {
                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notifyHome");
                                    databaserefForParentNotif.setValue(true);
                                } else {
                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notifyHome");
                                    databaserefForParentNotif.setValue(false);
                                }
                                Log.w("DISTANCEHome", "DISTANCE Home" + distance);


                        }
                        

                        /******************************************************************************
                         * Gets the location of school and calculate distance between childeren's location
                         * and the school's location. If it is greater than that we wanted, it sets the notification true
                         * Author: Efe
                         ******************************************************************************/
                            

                            databaseref.getParent().getParent().child("schoolAddress").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("latitude").getValue(Double.class) != null) {
                                        LatLng ltlngschool = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                                        listPointsSchoolLoc.add(ltlngschool);
                                    } else {
                                        // Toast.makeText(getApplicationContext(), "Mark school address.", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            if (listPointsSchoolLoc.size() > 0 && listPointsChildLoc.size() > 0) {//eğer bi list in uzunluğu sıfırsa sıkıntı çıkarabilir dikkar et !!
                                LatLng schoolLoc = listPointsSchoolLoc.get(listPointsSchoolLoc.size() - 1);
                                LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, schoolLoc.latitude, schoolLoc.longitude);

                                if (distance < 10) {

                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notifySchool");
                                    databaserefForParentNotif.setValue(true);
                                } else {

                                    String parentKey = GetParentKey();
                                    String childUpperKey = GetChildContainerKey();
                                    final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notifySchool");
                                    databaserefForParentNotif.setValue(false);
                                }
                                MarkMap();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Please open the application in the child to get the location information.", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                /******************************************************************************
                 * Adds a value listener to check the changes in driver's location
                 * Author: Arda
                 ******************************************************************************/
                databaserefForDriver.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w("Driver key", "DRIVERKEY" + dataSnapshot.toString());
                        if (dataSnapshot.getValue() != null) {
                            final String driverKey = (String) dataSnapshot.getValue();
                            final DatabaseReference databaserefForDriverLocation = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey).child("location").child("currentLocation");
                            databaserefForDriverLocation.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.w("Child loc", "DRIVERLOC" + dataSnapshot.child("latitude").getValue(Double.class));
                                    Log.w("Child loc", "DRIVERLOC" + dataSnapshot.child("longitude").getValue(Double.class));

                                    /******************************************************************************
                                     * Gets the current location of driver and calculate distance between childeren's location
                                     * and the driver's location. If it is greater than that we wanted, it sets the notification true
                                     * Author: Arda
                                     ******************************************************************************/
                                  
                                    if (dataSnapshot.child("speed").getValue(Integer.class) != null) {
                                        int speed = dataSnapshot.child("speed").getValue(Integer.class);
                                        serviceSpeedInput.setText(String.valueOf(speed) + " KM/H");

                                        LatLng ltlng = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                                        listPointsDriverLoc.add(ltlng);
                                        Log.w("Child loc", "DRIVERLOCSIZE" + listPointsDriverLoc.size());

                                        if (listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0) {
                                            LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                                            LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                            double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);
                                            // Log.w("DISTANCE", "DISTANCE" + distance);
                                            if (distance > 10) {
                                                distanceView.setTextColor(getResources().getColor(R.color.red));
                                                String parentKey = GetParentKey();
                                                String childUpperKey = GetChildContainerKey();
                                                final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notify");
                                                databaserefForParentNotif.setValue(true);
                                            } else {
                                                distanceView.setTextColor(getResources().getColor(R.color.green));
                                                String parentKey = GetParentKey();
                                                String childUpperKey = GetChildContainerKey();
                                                final DatabaseReference databaserefForParentNotif = FirebaseDatabase.getInstance().getReference().child("parents").child(parentKey).child("children").child(childUpperKey).child("notify");
                                                databaserefForParentNotif.setValue(false);
                                            }
                                            String num = String.valueOf(distance);
                                            int index = num.indexOf('.');
                                            String result = num.substring(0, index + 3);
                                            distanceView.setText(result + " M");
                                            MarkMap();

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else if (!trackChildLoc) {

                /******************************************************************************
                 * If parent disables the location tracking give alert
                 * Author: Can
                 ******************************************************************************/
                Toast.makeText(getApplicationContext(), "You need to enable track location to see location information!", Toast.LENGTH_SHORT).show();
            } else {

                /******************************************************************************
                 * if user does not see the locations, he marks the school or home location
                 * Author: Arda
                 ******************************************************************************/
                Log.w("parent", "parent marks map");
                submit.setVisibility(View.VISIBLE);
                distanceView.setVisibility(View.VISIBLE);
                if (parentMarksMapSchool) {
                    serviceSpeedInput.setVisibility(View.INVISIBLE);
                    servideSpeed.setVisibility(View.INVISIBLE);

                    distanceView.setText("Please mark the school address.");
                } else if (parentMarksMapHome) {
                    serviceSpeedInput.setVisibility(View.INVISIBLE);
                    servideSpeed.setVisibility(View.INVISIBLE);

                    distanceView.setText("Please mark the home address.");
                }

            }
        }
    }



    /******************************************************************************
     * Required methods for getting keys from intent
     * Author: Arda, Can
     ******************************************************************************/
    String GetChildContainerKey() {
        Intent i = getIntent();
        String childUpperKey = i.getStringExtra("childUpperKey");
        return childUpperKey;
    }

    String GetParentKey() {
        Intent i = getIntent();
        String parentKey = i.getStringExtra("parentKey");
        return parentKey;

    }

    String GetChildKey() {
        Intent i = getIntent();
        String childKey = i.getStringExtra("childKey");
        return childKey;
    }

    String GetDriverKey() {
        Intent i = getIntent();
        String driverKey = i.getStringExtra("driverKey");
        return driverKey;
    }

    boolean GetDriverControl() {
        boolean cont;
        Intent i = getIntent();
        cont = i.getBooleanExtra("driverControl", false);
        return cont;
    }

    String GetChildFullName() {
        Intent i = getIntent();
        String childFullName = i.getStringExtra("childFullName");
        return childFullName;
    }


    /******************************************************************************
     * After getting locations from the firebase, this method is used to mark the locations in the map and shows the related info on the markers
     * Author: Can
     ******************************************************************************/
    public void MarkMap() {
        mMap.clear();

        String childFullName = GetChildFullName();
        if (listPointsChildLoc.size() >= 1) {
            mMap.addMarker(new MarkerOptions()
                    .position(listPointsChildLoc.get(listPointsChildLoc.size() - 1))
                    .title("Student")
                    .snippet("Name: " + childFullName)
                    .icon(BitmapDescriptorFactory.fromResource(R.raw.student)));
        }
        if (listPointsDriverLoc.size() >= 1) {

            final DatabaseReference databaserefForDriver = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
            databaserefForDriver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w("Driver key", "DRIVERKEYNA" + dataSnapshot.toString());
                    if (dataSnapshot.getValue() != null) {
                        final String driverKey = (String) dataSnapshot.getValue();
                        final DatabaseReference databaserefForDriverName = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey).child("name");
                        databaserefForDriverName.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()) {
                                    final String driverName = dataSnapshot.getValue(String.class);
                                    mMap.addMarker(new MarkerOptions()
                                            .position(listPointsDriverLoc.get(listPointsDriverLoc.size() - 1))
                                            .title("Driver")
                                            .snippet("Name: " + driverName)
                                            .icon(BitmapDescriptorFactory.fromResource(R.raw.bustag)));

                                } else {
                                    Toast.makeText(getApplicationContext(), "Driver doesn't exists!", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        } else {
          //  Toast.makeText(getApplicationContext(), "Driver is needed to register your child.", Toast.LENGTH_SHORT).show();

        }
    }



    /******************************************************************************
     * Requid method to interact with the map when it is ready
     * Author: Can
     ******************************************************************************/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        /******************************************************************************
         * initialize google map and enable driver buttons if the user driver. if the user is not driver then show the school and home locations for the parent
         * Author: Can
         ******************************************************************************/
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (driverControl == true) {
            CreateButton("Add Path", addPath, R.raw.ok);
            CreateButton("Return", deleteLastPoint, R.raw.red2);
            CreateButton("Remove", deletePoints, R.raw.del2);
            CreateButton("Traffic Light", trafficLights, R.raw.trafficlight);
            getHomeTag();
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            boolean create = true;

            @Override
            public void onMapLongClick(LatLng latLng) {


                if (driverControl == true) {
                    mMap.setTrafficEnabled(true);

                    //Save first point select
                    listPoints.add(latLng);
                    //Create marker
                    mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            //.title("name:")
                            //.snippet("no2: 12312312 mV")
                            .icon(BitmapDescriptorFactory.fromResource(R.raw.bustag))
                    );

                    if (listPoints.size() >= 2) {
                        //Create the URL to get request from first marker to second marker
                        String url = getRequestUrl(listPoints.get(listPoints.size() - 1), listPoints.get(listPoints.size() - 2));
                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                        taskRequestDirections.execute(url);
                    }


                } else if (driverControl == false) {

                    //Save first point select
                    if (parentMarksMapHome && !parentMarksMapSchool) {
                        serviceSpeedInput.setVisibility(View.INVISIBLE);
                        //Create marker
                        if (HomeMarker != null) {
                            HomeMarker.remove();
                        }
                        HomeMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        );
                        homeAddress = latLng;

                    } else if (!parentMarksMapHome && parentMarksMapSchool) {
                        serviceSpeedInput.setVisibility(View.INVISIBLE);
                        if (SchoolMarker != null) {
                            SchoolMarker.remove();
                        }
                        //Create marker
                        SchoolMarker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        );
                        schoolAddress = latLng;
                    }
                }
            }
        });
        // Do other setup activities here too, as described elsewhere in this tutorial.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
    }

    /******************************************************************************
     * Method for initializing buttons in the map
     * Author: Can
     ******************************************************************************/
    void CreateButton(String name, View.OnClickListener listener, int icon) {
        Log.d("Button", "Add Button " + name);
        ImageButton myButton = new ImageButton(this);
        myButton.setId(buttonId);
        myButton.setOnClickListener(listener);
        LinearLayout ll = (LinearLayout) findViewById(R.id.button_holder);
        myButton.setBackgroundResource(R.drawable.border2);
        myButton.setImageResource(icon);
        ll.addView(myButton);
        buttonId++;
    }

    View.OnClickListener trafficLights = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mMap.isTrafficEnabled()) {
                mMap.setTrafficEnabled(false);
            } else {
                mMap.setTrafficEnabled(true);
            }
        }
    };
    View.OnClickListener deleteLastPoint = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (listPoints.size() > 1) {
                mMap.clear();
                getHomeTag();
                MarkerOptions markerOptions = new MarkerOptions();
                listPoints.remove(listPoints.size() - 1);
                for (int i = 0; i < listPoints.size() - 1; i++) {
                    //Create the URL to get request from first marker to second marker
                    mMap.addMarker(new MarkerOptions()
                            .position(listPoints.get(i))
                            .icon(BitmapDescriptorFactory.fromResource(R.raw.bustag)));
                    String url = getRequestUrl(listPoints.get(i + 1), listPoints.get(i));
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
                mMap.addMarker(new MarkerOptions()
                        .position(listPoints.get(listPoints.size() - 1))
                        .icon(BitmapDescriptorFactory.fromResource(R.raw.bustag)));
            } else {
                Toast.makeText(getApplicationContext(), "You need to choose first!", Toast.LENGTH_SHORT).show();
            }
        }
    };

    View.OnClickListener deletePoints = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mMap.clear();
            listPoints.clear();
            getHomeTag();
        }
    };

    View.OnClickListener addPath = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (listPoints.size() < 2) {
                Toast.makeText(getApplicationContext(), "You need to choose 2 point", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();//get the unique id of parent
                final String RegisteredUserID = currentUser.getUid();
                Intent goMain = new Intent(MapsActivity.this, DriverInterface.class);
                goMain.putParcelableArrayListExtra("pathList", listPoints);
                goMain.putExtra("userKey", RegisteredUserID);
                Toast.makeText(getApplicationContext(), "Path added", Toast.LENGTH_SHORT).show();
                startActivity(goMain);
            }
        }
    };

    private void cleanMarkers() {

        if (listPoints.size() == 2) {
            listPoints.clear();
            mMap.clear();
        }
    }

    LatLng point = null;
    private void getHomeTag() {
        String driverKey = GetDriverKey();

        final DatabaseReference databaserefForDriver = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey).child("children");
        databaserefForDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    dataSnapshot.getChildren().forEach(new Consumer<DataSnapshot>() {
                        @Override
                        public void accept(DataSnapshot dataSnapshot) {

                            HashMap<String, String> child = (HashMap<String, String>) dataSnapshot.getValue();
                            Log.w("driver child", "driver child " + child.get("childKey"));
                            if (takeSchool) {
                                getSchoolTag(child.get("childKey"));
                                takeSchool = false;
                            }
                            final DatabaseReference databaserefChild = FirebaseDatabase.getInstance().getReference().child("children").child(child.get("childKey"));
                            databaserefChild.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("name").exists() && dataSnapshot.child("homeAddress").hasChildren()) {
                                        String cName = dataSnapshot.child("name").getValue(String.class);
                                        Long cPhone = dataSnapshot.child("phone").getValue(Long.class);
                                        String cSurname = dataSnapshot.child("surname").getValue(String.class);
                                        if (dataSnapshot.child("homeAddress").child("latitude").exists() && dataSnapshot.child("homeAddress").child("longitude").exists()) {
                                            point = new LatLng(dataSnapshot.child("homeAddress").child("latitude").getValue(Double.class), dataSnapshot.child("homeAddress").child("longitude").getValue(Double.class));
                                            mMap.addMarker(new MarkerOptions()
                                                    .position(point)
                                                    .title("name:" + cName + " " + cSurname)
                                                    .snippet("Child Phone: " + cPhone)
                                                    .icon(BitmapDescriptorFactory.fromResource(R.raw.hometag)));
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Add Child information!", Toast.LENGTH_SHORT).show();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//şu an çocuktan alınyor ancak parent istediği isimde okul ismi yazabilir bir scroll ile tek bir isim haline getirilmeli. Bir ikincisi  okul yerleri static olmalı
// 1. Parent map üstünde bulunan bir noktayı seçicek
// 2. Driver çocuğu eklerken okulunu scroll ile ekliyecek


    /******************************************************************************
     * Method for get dynamic school Tag
     * Author: Can
     ******************************************************************************/
    private void getSchoolTag(String childkey) {
        final DatabaseReference databaserefChild = FirebaseDatabase.getInstance().getReference().child("children").child(childkey);
        databaserefChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("schoolName").exists() && dataSnapshot.child("schoolAddress").hasChildren()) {
                    String sName = dataSnapshot.child("schoolName").getValue(String.class);
                    // String sAddress = postSnapshot.child("schoolAddress").getValue(String.class);
                    if (dataSnapshot.child("schoolAddress").child("latitude").exists() && dataSnapshot.child("schoolAddress").child("longitude").exists()) {
                        point = new LatLng(dataSnapshot.child("schoolAddress").child("latitude").getValue(Double.class), dataSnapshot.child("schoolAddress").child("longitude").getValue(Double.class));
                        mMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title("School Name:" + sName)
                                //             .snippet("School Address" + sAddress)
                                .icon(BitmapDescriptorFactory.fromResource(R.raw.schooltag)));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Add school information for children!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    /* public String getDriverInfo(String driverKey){
          String driverName;
          String driverPhone;
         final DatabaseReference databaserefChild = FirebaseDatabase.getInstance().getReference().child("Drivers").child(driverKey);
         databaserefChild.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 driverName = dataSnapshot.child("name").toString();
                 driverPhone = dataSnapshot.child("phoneNumber").toString();
             }
             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
             }
         });
         return  driverName;
     }*/

    /******************************************************************************
     * This method creates URL string of diretion
     * Author: Can
     ******************************************************************************/
    private String getRequestUrl(LatLng origin, LatLng dest) {
        //Value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //key for accesspathString
        String key = "key=AIzaSyAaap7ntmwelL70dRB-rrsHbrLuAgeG4_8";

        String param = str_org + "&" + str_dest + "&" + sensor + "&" + mode + "&" + key;
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    /******************************************************************************
     * This method uses URL to creating connection. Then it returns response of service
     * Author: Can
     ******************************************************************************/
    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
                break;
        }
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    /******************************************************************************
     * This method uses directionParser class to parsing JSON object to direction.
     * Author: Can
     ******************************************************************************/
    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        /******************************************************************************
         * This method creates poly lines on map after JSON object parsed in TaskParser
         * Author: Can
         ******************************************************************************/
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /******************************************************************************
     * Requid method to calculate distance between two geolocations on the earth. ,
     * Method implements harversine methodology to calculate the distance on a earth shape
     *
     * Author: Arda
     ******************************************************************************/
    public double CalculationByDistance(double initialLat, double initialLong,
                                        double finalLat, double finalLong) {
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat - initialLat);
        double dLon = toRadians(finalLong - initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        Log.w("hesap", "hesap getir  " + R * c);
        return (R * c) * 1000;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }
    /******************************************************************************
     * When user marks the home or school location in the map, he need to press submit button to save location.
     * This method is called when submit button is clicked.
     * it simply send the home or school location to firebase realtime db
     * Author: Arda
     ******************************************************************************/
    public void onSubmit() {
        if (parentMarksMapHome) {
            HashMap<String, Double> locationDetails = new HashMap<String, Double>();

            locationDetails.put("latitude", homeAddress.latitude);
            locationDetails.put("longitude", homeAddress.longitude);

         /*   HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
            currentLocation.put("currentLocation", locationDetails); */

            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(GetChildKey()).child("homeAddress");
            databaseref.setValue(locationDetails);
            //Success toast
            Toast.makeText(getApplicationContext(), "Home Submited", Toast.LENGTH_SHORT).show();

        } else if (parentMarksMapSchool) {
            HashMap<String, Double> locationDetails = new HashMap<String, Double>();

            locationDetails.put("latitude", schoolAddress.latitude);
            locationDetails.put("longitude", schoolAddress.longitude);

         /*   HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
            currentLocation.put("currentLocation", locationDetails); */

            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(GetChildKey()).child("schoolAddress");
            databaseref.setValue(locationDetails);
            //Success toast
            Toast.makeText(getApplicationContext(), "School Submited", Toast.LENGTH_SHORT).show();

        }
    }
}
