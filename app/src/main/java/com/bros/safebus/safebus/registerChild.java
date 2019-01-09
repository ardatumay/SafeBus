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

        final TextView email = (TextView) findViewById(R.id.email);
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView surname = (TextView) findViewById(R.id.surname);
        final TextView password = (TextView) findViewById(R.id.password);
        final TextView phoneNumber = (TextView) findViewById(R.id.phone_Number);
        final TextView schoolName = (TextView) findViewById(R.id.school_Name);

        Button register = (Button) findViewById(R.id.registerChild);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email_Address = email.getText().toString();
                final String pass = password.getText().toString();
                final long phone = Long.parseLong(phoneNumber.getText().toString());
                final String Name = name.getText().toString();
                final String Surname = surname.getText().toString();
                final String SchoolName = schoolName.getText().toString();

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



                //final String number = phoneNumber.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(email_Address, pass)
                        .addOnCompleteListener(registerChild.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //firstTextView.setText("Signing up");
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


                                    //Set problemi yeni parentChild modeli oluturularak çözüldü !
                                    //set value for parent
                                    //HashMap<String, Object> hashchildForparent = new HashMap<String, Object>();
                                    String childName = Name + " " + Surname;
                                    final parentChild childForParent = new parentChild(childName, childrenKey,false,false,false);
                                    String key = databaseReference.child("parents").child(parentKey).child("children").push().getKey();
                                   /* hashchildForparent.put("childName",(String)childName);
                                    hashchildForparent.put("childrenKey",(String)childrenKey);
                                    hashchildForparent.put("notify",(boolean)false);
                                    hashchildForparent.put("childName",(boolean)false);
                                    hashchildForparent.put("childName",(boolean)false);*/
                                    databaseReference.child("parents").child(parentKey).child("children").child(key)
                                            .setValue(childForParent);

                                    //set value for users
                                    //extract @ and . character from mail and pur it as a key
                                    String mailAsUsername = CreateUsernameFromEmail(email_Address);
                                    HashMap<String, String> userRecord = new HashMap<String, String>();
                                    userRecord.put("key",childrenKey );
                                    userRecord.put("type", "Children");
                                    //String keyForUsers = databaseReference.child("users").child(mailAsUsername).child("children").push().getKey();
                                    databaseReference.child("users").child(mailAsUsername)
                                            .setValue(userRecord);

                                    // Map<String, String> newUser = new HashMap<String, String>();
                                    //newUser.put("email", email_Address);
                                    //newUser.put("password", pass);
                                    returnHomePage();

                                    // .child(firebaseAuth.getCurrentUser().getUid())
                                    // .setValue(newParent);
                                    // kullaniciGuncelle();
                                } else {
                                    Log.e("New User Error", task.getException().getMessage());
                                }

                            }
                        });
            }
        });


    }

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
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
    }
}
