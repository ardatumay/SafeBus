package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ParentInterface extends Activity {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    HashMap<String, String> children;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_interface);
        children = new HashMap<String, String>();
        Button addChild = (Button) findViewById(R.id.add_child);
        addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToChildrenRegister();
            }
        });

        FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();//get the unique id of parent
        final String RegisteredUserID = currentUser.getUid();

        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID).child("children");

        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    /*Log.d("PARENT", "onDataChange: "+ ds);
                    Log.d("PARENT", "onDataChange: "+ ds.child("key").getValue(String.class));
                    Log.d("PARENT", "onDataChange: "+ ds.getKey());*/
                    children.put(ds.getKey(), ds.child("key").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    void GoToChildrenRegister(){
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}
