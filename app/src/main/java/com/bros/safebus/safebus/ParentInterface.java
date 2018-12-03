package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

import static com.google.android.gms.tasks.Tasks.await;

public class ParentInterface extends Activity {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    HashMap<String, String> children;
    String childFullName;
    List<String> childrenNames;

    TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
    Task dbTask = dbSource.getTask();
    TaskCompletionSource<DataSnapshot> dbSource2 = new TaskCompletionSource<>();
    Task dbTask2 = dbSource2.getTask();
    TaskCompletionSource<String> dbSource3 = new TaskCompletionSource<>();
    Task dbTask3 = dbSource3.getTask();
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


       /* Task task = forestRef.getMetadata();
        task.addOnSuccessListener(this, new OnSuccessListener() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // Metadata now contains the metadata for 'images/forest.jpg'
            }
        });*/
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
                        childrenNames.add(ds.child("name").getValue(String.class));

                        //String childName =  GetChildFullName(ds.child("key").getValue(String.class));
                       /* dbSource3.setResult(GetChildFullName(ds.child("key").getValue(String.class)));
                        dbTask3.addOnCompleteListener(new OnCompleteListener<String>() {
                            @Override
                            public void onComplete(@NonNull Task<String> task) {
                                String name = task.getResult();
                                childrenNames.add(name);
                            }
                        });*/
                        /*final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(ds.child("key").getValue(String.class));
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
                                    DataSnapshot result2 = task.getResult();
                                    Log.d("Child name", "DBRESULT" + result2.toString());

                                    String childName = result2.child("name").getValue().toString();
                                    String childSurname = result2.child("surname").getValue().toString();
                                    childFullName = childName + " " + childSurname;
                                    childrenNames.add(childFullName);
                                    Log.d("Child name", "Child Names: " + childrenNames.toString());

                                }
                            }
                        });*/
                       // Log.d("Child name", "Child Namess: " + childName);

                    }
                    Log.d("Child name", "Child Names: " + childrenNames.size());

                    Log.d("Child name", "ONCREATE cilhdren size" + children.size());
                    Log.d("Child name", "ONCREATE cilhdren size tostring" + children.toString());


                    if(childrenNames.size() != 0) {
                        CreateButtons(childrenNames);
                    }

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
                    //childrenNames.add(childName + " " + childSurname);
                }
            }
        });

        return childFullName;
    }

    void CreateButtons(List<String> names){
        Log.d("CHILDRENFULL", "Child Name: " +childrenNames.size() );
            for (int i = 0 ; i < childrenNames.size() ; i++) {
                //String childName = GetChildFullName(child.getValue());

                //Log.d("CHILDREN", "Child Name of each child: " + childrenNames.get(i));
                Button myButton = new Button(this);
                myButton.setText(childrenNames.get(i));

                myButton.setId(i);
                RelativeLayout ll = (RelativeLayout) findViewById(R.id.parent_interface);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                if ( i > 0 )
                {
                    lp.addRule(RelativeLayout.BELOW, i-1);

                }

                ll.addView(myButton, lp);

                //myButton.setLayoutParams(lp);




            }
    }
    void GoToChildrenRegister() {
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}
