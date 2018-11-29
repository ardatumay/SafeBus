package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bros.safebus.safebus.models.Child;
import com.bros.safebus.safebus.models.Parent;
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



        final TextView email = (TextView) findViewById(R.id.email);
        final TextView name = (TextView) findViewById(R.id.name);
        final TextView surname = (TextView) findViewById(R.id.surname);
        final TextView password = (TextView) findViewById(R.id.password);
        final TextView schoolAddress = (TextView) findViewById(R.id.school_Address);
        final TextView phoneNumber = (TextView) findViewById(R.id.phone_Number);

        Button register = (Button) findViewById(R.id.registerChild);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final String email_Address = email.getText().toString();
                final String pass = password.getText().toString();
                final String Address = schoolAddress.getText().toString();
                final long phone = Long.parseLong(phoneNumber.getText().toString());
                final String Name = name.getText().toString();
                final String Surname = surname.getText().toString();

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
                if (TextUtils.isEmpty(Address)) {
                    Toast.makeText(registerChild.this, "Enter school address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (phone == 0) {
                    Toast.makeText(registerChild.this, "Enter phone number!", Toast.LENGTH_SHORT).show();
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

                                    String type = "children";
                                    databaseReference = firebaseDatabase.getReference();
                                    String childrenKey = firebaseAuth.getCurrentUser().getUid();
                                    final Child newChild = new Child(Name, Surname, email_Address, pass, Address, phone, childrenKey, parentKey ,type);
                                    Log.d("child", "onComplete: " + newChild.toString());
                                    databaseReference.child("children").child(childrenKey)
                                            .setValue(newChild);
                                    HashMap<String, String> childForparent = new HashMap<String, String>();
                                    childForparent.put("key",childrenKey );
                                    String key = databaseReference.child("parents").child(parentKey).child("children").push().getKey();

                                    databaseReference.child("parents").child(parentKey).child("children").child(key)
                                            .setValue(childForparent);
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

    void returnHomePage()
    {
        Intent intent = new Intent(this, ParentInterface.class);
        startActivity(intent);
    }
}