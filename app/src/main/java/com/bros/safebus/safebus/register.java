package com.bros.safebus.safebus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText name = (EditText) findViewById(R.id.name);
        final EditText surname = (EditText) findViewById(R.id.surname);
        final EditText homeAddress = (EditText) findViewById(R.id.home_address);
        final EditText phoneNumber = (EditText) findViewById(R.id.phone_number);
        final Button register = (Button) findViewById(R.id.register_button);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String email_Address = email.getText().toString();
                final String pass = password.getText().toString();
                final String Name = name.getText().toString();
                final String Surname = surname.getText().toString();
                final String Address = homeAddress.getText().toString();
                //final Editable number = phoneNumber.getText();
                final int number = Integer.parseInt(phoneNumber.getText().toString());
                //final String number = phoneNumber.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(email_Address, pass)
                        .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //firstTextView.setText("Signing up");

                                    databaseReference = firebaseDatabase.getReference("parents/");

                                    final Parent newParent = new Parent(Name, Surname, email_Address, pass, Address, number);
                                    databaseReference.child("0").setValue(newParent);
                                   // Map<String, String> newUser = new HashMap<String, String>();
                                    //newUser.put("email", email_Address);
                                    //newUser.put("password", pass);



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

}
