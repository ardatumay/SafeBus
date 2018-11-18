package com.bros.safebus.safebus;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bros.safebus.safebus.Animation.AnimationActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
private static int SPLASH_TIME_OUT = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent animation = new Intent(MainActivity.this, AnimationActivity.class);
                startActivity(animation);
                finish();
            }
        },SPLASH_TIME_OUT);
       /* FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference();

        myRef.setValue("Hello, Worlddddd!");*/

        Button mapsButton = (Button) findViewById(R.id.o_map);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public  void onClick(View v){
                goMaps();
            }
        });

    }

    void goMaps (){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

}
