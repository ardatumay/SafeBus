package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bros.safebus.safebus.models.Driver;
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
import java.util.function.Function;

import static com.google.android.gms.tasks.Tasks.await;

public class ParentInterface extends Activity {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    HashMap<String, String> children;
    String childFullName;
    List<String> childrenNames;
    String DriverKey;

    TaskCompletionSource<DataSnapshot> dbSource = new TaskCompletionSource<>();
    Task dbTask = dbSource.getTask();
    TaskCompletionSource<DataSnapshot> dbSource2 = new TaskCompletionSource<>();
    Task dbTask2 = dbSource2.getTask();
    TaskCompletionSource<String> dbSource3 = new TaskCompletionSource<>();
    Task dbTask3 = dbSource3.getTask();


    @Override
    protected void onCreate(Bundle savedInstanceState)  {
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
        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
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
                if(dataSnapshot.hasChildren()) {
                    if (dataSnapshot.getChildrenCount() != 0) {
                        dbSource.trySetResult(dataSnapshot);
                    }
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
                        children.put(ds.child("name").getValue(String.class), ds.child("key").getValue(String.class));
                        childrenNames.add(ds.child("name").getValue(String.class));
                    }

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

    public void GetDriverKey(String childKey){
        final DatabaseReference databaseKey = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
        databaseKey.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DriverKey = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        Log.v("DriverKeyyy",DriverKey);


    }

    void CreateButtons(List<String> names){
        Log.d("CHILDRENFULL", "Child Name: " +childrenNames.size() );
            for (int i = 0 ; i < childrenNames.size() ; i++) {
                //String childName = GetChildFullName(child.getValue());

                //Log.d("CHILDREN", "Child Name of each child: " + childrenNames.get(i));
                Button myButton = new Button(this);
                myButton.setText(childrenNames.get(i));
                myButton.setId(i);
                myButton.setOnClickListener(OnClikChild);

                LinearLayout ll = (LinearLayout) findViewById(R.id.button_holder);
                ll.setBackground(getDrawable(R.drawable.border));
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                myButton.setBackground(getDrawable(R.drawable.border));
                ll.addView(myButton, lp);

            }
    }

    View.OnClickListener OnClikChild =  new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            Button b = (Button)view;
            String buttonText = b.getText().toString();
            String childKey = children.get(buttonText);
            GoToMapPage(childKey);
        }
    };

    void GoToMapPage(String childKey){

        Intent i = new Intent(this, MapsActivity.class);
        i.putExtra("childKey", childKey);
        Log.v("DriverKeyPC",childKey);
        i.putExtra("DriverKey",DriverKey);
        Log.v("DriverKeyP",DriverKey);
        startActivity(i);
    }

    void GoToChildrenRegister() {
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}

