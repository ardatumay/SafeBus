package com.bros.safebus.safebus;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.directions.route.Routing;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double latitude,longitude;


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Log.i("Here", "onreceived");
            if (bundle != null) {

                latitude = bundle.getDouble("lati");
                Log.i("Tag", latitude + "");

                longitude = bundle.getDouble("longi");
                Log.i("tag", longitude + "");
               // drawmap(latitude, longitude);
            }
        }
    };

    public void drawPath(double plati, double plongi, double cLati, double clongi) {
        // draw on map here
        // draw line from intial to final location and draw tracker location map

        Log.i("Tag", "map");

        // add line b/w current and prev location.
        LatLng prev = new LatLng(plati, plongi);
        LatLng my = new LatLng(cLati, clongi);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(my, 15));
        // map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        Polyline line = mMap.addPolyline(new PolylineOptions().add(prev, my)
                .width(5).color(Color.BLUE));
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        /*LatLng kralCanınEvi = new LatLng(39.892967, 32.855078, 39.892311, 32.854128);
        mMap.addMarker(new MarkerOptions().position(kralCanınEvi).title("KRAL CAN"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kralCanınEvi, 18));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(kralCanınEvi));
        mMap.addMarker(new MarkerOptions()
        .position(kralCanınEvi)
        .title("king")
        );*/

        drawPath(39.892967, 32.855078,39.892311, 32.854128 );


       /* Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .color(Color.RED)
                .add(
                        new LatLng(39.892967, 32.855078),
                        new LatLng(39.892311, 32.854128),
                        new LatLng(39.892122, 32.852465),
                        new LatLng(39.894560, 32.851845),
                        new LatLng(39.895729, 32.847135)));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);*/


    }


    public void mapTypeSelector(String mType) {
        GoogleMap googleMap = getmMap();
        setmMap(googleMap);
        switch(mType){
            case "Normal": googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); break;
            case "Hybrid": googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); break;
            case "Setellite": googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE); break;
            case "Terrain": googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN); break;
        }

    }
    public void roadBuilder(){
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.RED);
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public void setmMap(GoogleMap mMap) {
        this.mMap = mMap;
    }

}
