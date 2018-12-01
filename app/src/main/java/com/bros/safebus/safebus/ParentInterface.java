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

import com.google.android.gms.tasks.OnCompleteListener;
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
import java.util.List;

public class ParentInterface extends Activity {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    HashMap<String, String> children;
    String childFullName;
    List<String> childrenNames;
    TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
    Task dbTask = dbSource.getTask();
    TaskCompletionSource<DataSnapshot> dbSource2 = new TaskCompletionSource<>();
    Task dbTask2 = dbSource2.getTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_interface);
        children = new HashMap<String, String>();
        childrenNames = new ArrayList<>();
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
                if (dataSnapshot.getChildrenCount() != 0) {
                    dbSource.setResult(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
            }
        });

        dbTask.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    DataSnapshot result = task.getResult();
                    for (DataSnapshot ds : result.getChildren()) {
                        children.put(ds.getKey(), ds.child("key").getValue(String.class));
                        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(ds.child("key").getValue(String.class));
                        databaseref.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                dbSource2.setResult(dataSnapshot);
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dbSource2.setException(databaseError.toException());
                            }
                        });
                        dbTask2.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DataSnapshot> task) {
                                if (task.isSuccessful()) {
                                    // Task completed successfully
                                    DataSnapshot result = task.getResult();
                                    String childName = result.child("name").getValue().toString();
                                    String childSurname = result.child("surname").getValue().toString();
                                    childFullName = childName + " " + childSurname;
                                    childrenNames.add(childFullName);
                                    Log.d("Child name", "Child Names: " + childrenNames.toString());

                                }
                            }
                        });
                    }
                    Log.d("Child name", "Child Names: " + childrenNames.size());

                    Log.d("Child name", "ONCREATE cilhdren size" + children.size());
                    Log.d("Child name", "ONCREATE cilhdren size tostring" + children.toString());


                    /*if (children.size() != 0) {
                        for (HashMap.Entry<String, String> child : children.entrySet()) {
                            String childName = GetChildFullName(child.getValue());
                            Log.d("CHILDREN", "Child Name: " + child.getValue());
                            Log.d("CHILDRENFULLL", "Child Name: " + childFullName);
                            //childrenNames.add(childName);
                            Log.d("Child name", "Child Name: " + childName);
                            /*
                            Button myButton = new Button(this);
                            myButton.setText("Push Me");

                            LinearLayout ll = (LinearLayout) findViewById(R.id.buttonlayout);
                            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                            ll.addView(myButton, lp);
                        }
                    }*/
                }
            }
        });
        Log.d("Child name", "Child Names: " + childrenNames);

    }


    String GetChildFullName(String childKey) {
        final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey);
        databaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dbSource2.setResult(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                dbSource.setException(databaseError.toException());
            }
        });
        dbTask2.addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    // Task completed successfully
                    DataSnapshot result = task.getResult();
                    String childName = result.child("name").getValue().toString();
                    String childSurname = result.child("surname").getValue().toString();
                    Log.d("CHILDRENFULL", "Child Name: " + childName + "    " + childSurname);
                    childFullName = childName + " " + childSurname;
                    childrenNames.add(childName + " " + childSurname);
                }
            }
        });

        return childFullName;
    }

    void GoToChildrenRegister() {
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}
