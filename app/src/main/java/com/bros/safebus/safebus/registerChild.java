/******************************************************************************
 *  Class Name: registerChild
 *  Author: Efe
 *
 *  This class provides parent to sign up for a child
 *
 *  Revisions: Can: Toast messages
 *             Arda: Extract characters that we don't want from sources
 ******************************************************************************/

package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bros.safebus.safebus.models.Child;
import com.bros.safebus.safebus.models.parentChild;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class registerChild extends Activity {
    final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_child);

        final RelativeLayout r = (RelativeLayout)findViewById(R.id.register_child);

        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }
        });
        /******************************************************************************
         * Defining TextViews and register button
         * Author: Efe
         ******************************************************************************/
        final TextView email = (TextView) findViewById(R.id.email);
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView surname = (TextView) findViewById(R.id.surname);
        final TextView password = (TextView) findViewById(R.id.password);
        final TextView phoneNumber = (TextView) findViewById(R.id.phone_Number);
        final TextView schoolName = (TextView) findViewById(R.id.school_Name);
        Button register = (Button) findViewById(R.id.registerChild);

        /******************************************************************************
         * After clicked to register it gets the inputs and put them in to the variables
         * Author: Efe
         ******************************************************************************/
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email_Address = email.getText().toString();
                final String pass = password.getText().toString();
                final long phone = Long.parseLong(phoneNumber.getText().toString());
                final String Name = name.getText().toString();
                final String Surname = surname.getText().toString();
                final String SchoolName = schoolName.getText().toString();


                /******************************************************************************
                 * Proper toast messages if inputs are empty
                 * Author: Can
                 ******************************************************************************/
                if (TextUtils.isEmpty(email_Address)) {
                    Toast.makeText(registerChild.this, "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(registerChild.this, "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Name)) {
                    Toast.makeText(registerChild.this, "Enter name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(Surname)) {
                    Toast.makeText(registerChild.this, "Enter surname!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone == 0) {
                    Toast.makeText(registerChild.this, "Enter phone number!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(SchoolName)) {
                    Toast.makeText(registerChild.this, "Enter school name", Toast.LENGTH_SHORT).show();
                    return;
                }



                /******************************************************************************
                 * Creates the child with firebase's method with proper constructor
                 * Author: Efe
                 ******************************************************************************/
                firebaseAuth.createUserWithEmailAndPassword(email_Address, pass)
                        .addOnCompleteListener(registerChild.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Intent intent = getIntent();
                                    String parentKey = intent.getStringExtra("parentKey");

                                    //set value for children
                                    String type = "Children";
                                    databaseReference = firebaseDatabase.getReference();
                                    String childrenKey = firebaseAuth.getCurrentUser().getUid();
                                    boolean trackLocation = true;
                                    String homeAddress = "some address";
                                    String schoolAddress= "some address";
                                    final Child newChild = new Child(Name, Surname, SchoolName, email_Address, pass, schoolAddress, phone, childrenKey, parentKey ,type, trackLocation, homeAddress);
                                    databaseReference.child("children").child(childrenKey)
                                            .setValue(newChild);



                                    String childName = Name + " " + Surname;
                                    final parentChild childForParent = new parentChild(childName, childrenKey,false,false,false);
                                    String key = databaseReference.child("parents").child(parentKey).child("children").push().getKey();

                                    databaseReference.child("parents").child(parentKey).child("children").child(key)
                                            .setValue(childForParent);


                                    String mailAsUsername = CreateUsernameFromEmail(email_Address);
                                    HashMap<String, String> userRecord = new HashMap<String, String>();
                                    userRecord.put("key",childrenKey );
                                    userRecord.put("type", "Children");

                                    databaseReference.child("users").child(mailAsUsername)
                                            .setValue(userRecord);


                                    returnHomePage();


                                } else {
                                    Log.e("New User Error", task.getException().getMessage());
                                }

                            }
                        });
            }
        });


    }
    /******************************************************************************
     * Extracting characters that we don't want
     * Author: Arda
     ******************************************************************************/
    String CreateUsernameFromEmail(String email){
        String src1 = ExtractCharFromString(email, "@");
        String src2 = ExtractCharFromString(src1, ".");
        return src2;
    }

    String  ExtractCharFromString(String src, String trgt){
        String newSrc = src.replace(trgt, "");
        return newSrc;

    }

    void returnHomePage()
    {
        Intent intent = new Intent(this, ParentInterface.class);

        startActivity(intent);
    }
}
