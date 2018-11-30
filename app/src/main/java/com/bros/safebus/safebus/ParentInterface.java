package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
    String childFullName;
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
        Log.d("Child name", "ONCREATE PARENT");
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Child name", "ONCREATE fire PARENT");
                Log.d("Child name", "ONCREATE fire PARENT"+dataSnapshot);
                if(dataSnapshot.getChildrenCount() != 0){
                    Log.d("Child name", "INSIDE IF");
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        Log.d("Child name", "INSIDE FOR");
                    /*Log.d("PARENT", "onDataChange: "+ ds);
                    Log.d("PARENT", "onDataChange: "+ ds.child("key").getValue(String.class));
                    Log.d("PARENT", "onDataChange: "+ ds.getKey());*/
                        Log.d("Child name", "INSIDE FOR"+ " " + "  " + ds.getKey()+ "  " +ds.child("key").getValue(String.class));
                        children.put(ds.getKey(), ds.child("key").getValue(String.class));
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Log.d("Child name", "ONCREATE cilhdren size"+children.size());
        Log.d("Child name", "ONCREATE cilhdren size tostring"+children.toString());
        if(children.size() != 0){
            for (HashMap.Entry<String, String> child : children.entrySet()) {
                String childName = GetChildFullName(child.getValue());
                Log.d("Child name", "Child Name: "+childName);
            /*Button myButton = new Button(this);
            myButton.setText("Push Me");

            LinearLayout ll = (LinearLayout)findViewById(R.id.buttonlayout);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            ll.addView(myButton, lp);*/
            }
        }


    }



    String GetChildFullName(String childKey){
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey);
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String childName = dataSnapshot.child("name").getValue().toString();
                String childSurname = dataSnapshot.child("surname").getValue().toString();
                childFullName =  childName + " " + childSurname;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return childFullName;
    }

    void GoToChildrenRegister(){
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}
