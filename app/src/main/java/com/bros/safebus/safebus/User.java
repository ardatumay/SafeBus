package com.bros.safebus.safebus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class User extends AppCompatActivity {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        final EditText ad = (EditText) findViewById(R.id.name);
        final EditText soyad = (EditText) findViewById(R.id.surname);
        final EditText rumuz = (EditText) findViewById(R.id.nick);
        final Button logout = (Button) findViewById(R.id.logout);

        final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        final DatabaseReference databaseReference = firebaseDatabase.getReference("users").child(
                firebaseUser.getUid());

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            String UserName, UserSurname, nick;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserName = dataSnapshot.child("name").getValue().toString();
                UserSurname = dataSnapshot.child("surname").getValue().toString();
                nick = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                ad.setText(UserName);
                soyad.setText(UserSurname);
                rumuz.setText(nick);

                ad.setEnabled(true);
                soyad.setEnabled(true);
                rumuz.setEnabled(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                finish();
            }
        });
    }
}
