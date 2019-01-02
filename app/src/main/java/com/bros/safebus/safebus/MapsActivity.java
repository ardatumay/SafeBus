package com.bros.safebus.safebus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 500;
    ArrayList<LatLng> listPoints;
    ArrayList<LatLng> listPointsChildLoc;
    ArrayList<LatLng> listPointsDriverLoc;
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String userType = "";
    TextView distanceView;
    Button submit;
    //variables for parent to mark home and school address on the map
    Boolean parentMarksMapSchool;
    Boolean parentMarksMapHome;
    LatLng homeAddress;
    LatLng schoolAddress;

    private static boolean driverControl = false;
    String DriverKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        listPoints = new ArrayList<>();
        listPointsChildLoc = new ArrayList<>();
        listPointsDriverLoc = new ArrayList<>();
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

        String childKey = GetChildKey();
        Log.w("CHILD KEY", "CHILDKEY" + childKey);

        listPoints = new ArrayList<>();
        driverControl = GetDriverControl();
        //DriverKey = GetDriverKey();
//        Log.v("DriverKey", DriverKey);

        if (driverControl == false) {
            final DatabaseReference databaserefForDriver = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
            databaserefForDriver.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w("Driver key", "DRIVERKEY" + dataSnapshot.toString());
                    if (dataSnapshot.getValue() != null) {
                        final String driverKey = (String) dataSnapshot.getValue();
                        final DatabaseReference databaserefForDriverPathlist = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey).child("pathList");
                        databaserefForDriverPathlist.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                int i = 0;
                                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                    LatLng point = new LatLng(dataSnapshot.child(String.valueOf(i)).child("latitude").getValue(Double.class), dataSnapshot.child(String.valueOf(i)).child("longitude").getValue(Double.class));

                                   // LatLng point = postSnapshot.child(String.valueOf(i++)).getValue(LatLng.class);
                                    Log.v("pointList", "pointlists" + point.toString());


                                    //Use the dataType you are using and also use the reference of those childs inside arrays\\

                                    // Putting Data into Getter Setter \\

                                    listPoints.add(point);
                                    Log.v("pointList", "pointlists" + listPoints.toString());
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
            });/*
            //Get route from the DB
            final DatabaseReference pathList = FirebaseDatabase.getInstance().getReference().child("driver").child(DriverKey).child("pathList");
            pathList.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        int i = 1;
                        LatLng point = postSnapshot.child(String.valueOf(i++)).getValue(LatLng.class);
                        //Use the dataType you are using and also use the reference of those childs inside arrays\\

                        // Putting Data into Getter Setter \\

                        listPoints.add(point);
                        Log.v("pointList", listPoints.toString());

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
*/
            Intent incomingIntent = getIntent();
            parentMarksMapHome = incomingIntent.getBooleanExtra("parentMarksMapHome", false);
            parentMarksMapSchool = incomingIntent.getBooleanExtra("parentMarksMapSchool", false);

            Log.w("mark home", "marks home" + parentMarksMapHome);
            Log.w("mark school", "marks school" + parentMarksMapSchool);

            if (!parentMarksMapSchool && !parentMarksMapHome) {
                distanceView.setVisibility(View.VISIBLE);
                Log.w("parent", "parent marks map");
        /*FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();
        final String RegisteredUserID = currentUser.getUid();*/
                final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("location").child("currentLocation");
                databaseref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.w("Child loc", "CHILDLOC" + dataSnapshot.child("latitude").getValue(Double.class));
                        Log.w("Child loc", "CHILDLOC" + dataSnapshot.child("longitude").getValue(Double.class));

                        LatLng ltlng = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                        listPointsChildLoc.add(ltlng);
                        Log.w("Child loc", "CHILDLOCSIZE" + listPointsChildLoc.size());
                        if (listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0) {//eğer bi list in uzunluğu sıfırsa sıkıntı çıkarabilir dikkar et !!
                            LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                            LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                            double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);

                            if (distance > 0.1) {
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
                            Log.w("DISTANCE", "DISTANCE" + distance);
                            distanceView.setText(String.valueOf(distance));
                            MarkMap();

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

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

                                    LatLng ltlng = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                                    listPointsDriverLoc.add(ltlng);
                                    Log.w("Child loc", "DRIVERLOCSIZE" + listPointsDriverLoc.size());

                                    if (listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0) {
                                        LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                                        LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                        double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);
                                        Log.w("DISTANCE", "DISTANCE" + distance);
                                        if (distance > 0.1) {
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
                                        distanceView.setText(String.valueOf(distance) + " KM");
                                        MarkMap();

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
                Log.w("parent", "parent marks map");
                submit.setVisibility(View.VISIBLE);
            }
        }
    }


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


    public void MarkMap() {
        mMap.clear();
        if (listPointsChildLoc.size() >= 1) {
            LatLng childrenLocation = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
            MarkerOptions markerOptionsChild = new MarkerOptions();
            markerOptionsChild.position(childrenLocation);
            markerOptionsChild.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(markerOptionsChild);
        }
        if (listPointsDriverLoc.size() >= 1) {
            LatLng driverLocation = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
            MarkerOptions markerOptionsDriver = new MarkerOptions();
            markerOptionsDriver.position(driverLocation);
            markerOptionsDriver.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(markerOptionsDriver);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
            return;
        }
        mMap.setMyLocationEnabled(true);
        //iki nokta yaratmak için bu method kullanılıyor eğer haritaya uzun tıklarsan tag yartır ve iki tag arasında yol oluşturur.
        /*
        TODO:
        1) kendi locationumu belli aralıklarla al
        2) location değiştiğinde bunu anlaması için call methodu yarat
        3) Değişimi anladığında location arası noktaların arasını google servisi kullanarak bağla
         */


        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            LatLng getlast;
            int i = 0;

            @Override
            public void onMapLongClick(LatLng latLng) {


                if (driverControl == true) {
                    CreateButton("Add Path");
                    //Save first point select
                    listPoints.add(latLng);
                    //Create marker
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    mMap.addMarker(markerOptions);

                    if (listPoints.size() >= 2) {
                        //Create the URL to get request from first marker to second marker
                        String url = getRequestUrl(listPoints.get(i), listPoints.get(i + 1));
                        i++;
                        TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                        taskRequestDirections.execute(url);
                    }
                } else if (driverControl == false) {
                    mMap.clear();
                    //Save first point select
                    if (parentMarksMapHome && !parentMarksMapSchool) {
                        //Create marker
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        mMap.addMarker(markerOptions);
                        homeAddress = latLng;
                    } else if (!parentMarksMapHome && parentMarksMapSchool) {
                        //Create marker
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        mMap.addMarker(markerOptions);
                        schoolAddress = latLng;
                    }
                }
            }
        });
    }

    void CreateButton(String name) {
        Log.d("Button", "Add Button " + name);
        int i = 0;
        Button myButton = new Button(this);
        myButton.setText(name);
        myButton.setId(i);
        myButton.setOnClickListener(addPath);
        LinearLayout ll = (LinearLayout) findViewById(R.id.button_holder);
        ll.setBackground(getDrawable(R.drawable.border));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        myButton.setBackground(getDrawable(R.drawable.border));
        ll.addView(myButton, lp);
    }

    View.OnClickListener addPath = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent goMain = new Intent(MapsActivity.this, DriverInterface.class);
            goMain.putParcelableArrayListExtra("pathList", listPoints);
            Log.v("ListPath", listPoints.toString());
            goMain.putExtra("userKey", DriverKey);
            startActivity(goMain);
        }
    };

    private void cleanMarkers() {

        if (listPoints.size() == 2) {
            listPoints.clear();
            mMap.clear();
        }
    }

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
        return R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI / 180);
    }

    public void onSubmit() {
        if (parentMarksMapHome) {
            HashMap<String, Double> locationDetails = new HashMap<String, Double>();

            locationDetails.put("latitude", homeAddress.latitude);
            locationDetails.put("longitude", homeAddress.longitude);

         /*   HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
            currentLocation.put("currentLocation", locationDetails); */

            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(GetChildKey()).child("homeAddress");
            databaseref.setValue(locationDetails);
        } else if (parentMarksMapSchool) {
            HashMap<String, Double> locationDetails = new HashMap<String, Double>();

            locationDetails.put("latitude", schoolAddress.latitude);
            locationDetails.put("longitude", schoolAddress.longitude);

         /*   HashMap<String, HashMap<String, Double>> currentLocation = new HashMap<String, HashMap<String, Double>>();
            currentLocation.put("currentLocation", locationDetails); */

            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(GetChildKey()).child("schoolAddress");
            databaseref.setValue(locationDetails);
        }
    }
}
