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

        Intent i = getIntent();
        String childKey = i.getStringExtra("childKey");
        Log.w("CHILD KEY", "CHILDKEY" + childKey);

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
                if(listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0){
                    LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                    LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                    double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);
                    Log.w("DISTANCE", "DISTANCE" + distance);
                    TextView distanceView = (TextView) findViewById(R.id.distance);
                    distanceView.setText(String.valueOf(distance));
                    MarkMap();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference databaserefForDriver = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
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

                            if(listPointsDriverLoc.size() > 0 && listPointsChildLoc.size() > 0){
                                LatLng driverLoc = listPointsDriverLoc.get(listPointsDriverLoc.size() - 1);
                                LatLng childLoc = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                                double distance = CalculationByDistance(childLoc.latitude, childLoc.longitude, driverLoc.latitude, driverLoc.longitude);
                                Log.w("DISTANCE", "DISTANCE" + distance);
                                TextView distanceView = (TextView) findViewById(R.id.distance);
                                distanceView.setText(String.valueOf(distance)+ "KM");
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


    }


    // Add a marker in Sydney and move the camera
        /*LatLng kralCanınEvi = new LatLng(39.892967, 32.855078, 39.892311, 32.854128);
        mMap.addMarker(new MarkerOptions().position(kralCanınEvi).title("KRAL CAN"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kralCanınEvi, 18));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(kralCanınEvi));
        mMap.addMarker(new MarkerOptions()
        .position(kralCanınEvi)
        .title("king")
        );*/

        public void MarkMap(){
            mMap.clear();
            if(listPointsChildLoc.size() >= 1){
                LatLng childrenLocation = listPointsChildLoc.get(listPointsChildLoc.size() - 1);
                MarkerOptions markerOptionsChild = new MarkerOptions();
                markerOptionsChild.position(childrenLocation);
                markerOptionsChild.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                mMap.addMarker(markerOptionsChild);
            }
            if(listPointsDriverLoc.size() >= 1){
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
                //Reset marker when already 2
            /*   if (listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }*/
                //Save first point select
                listPoints.add(latLng);
                //Create marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if (listPoints.size() == 1) {
                    //Add first marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    //Add second marker to the map
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if (listPoints.size() >= 2) {
                    //Create the URL to get request from first marker to second marker
                    String url = getRequestUrl(listPoints.get(i), listPoints.get(i + 1));
                    i++;
                    TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
                    taskRequestDirections.execute(url);
                }
            }
        });

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
        //key for access
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
                                        double finalLat, double finalLong){
        int R = 6371; // km (Earth radius)
        double dLat = toRadians(finalLat-initialLat);
        double dLon = toRadians(finalLong-initialLong);
        initialLat = toRadians(initialLat);
        finalLat = toRadians(finalLat);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(initialLat) * Math.cos(finalLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public double toRadians(double deg) {
        return deg * (Math.PI/180);
    }
}
