package com.bros.safebus.safebus;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);
        final String sa ;
       // final Button register = (Button) findViewById(R.id.register);
        final Button login = (Button) findViewById(R.id.login_Button);
       // final TextView firstTextView = (TextView) findViewById(R.id.textView);
        final Button sign_up = (Button) findViewById(R.id.signup_Button);

        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRegisterPage();
            }
        });


       /* sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String emailAddr = email.getText().toString();
                String pass = password.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(emailAddr, pass)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    firstTextView.setText("Signing up");
                                    kullaniciOlustur();
                                    kullaniciGuncelle();
                                } else {
                                    Log.e("Yeni Kullanıcı Hatası", task.getException().getMessage());
                                }

                            }
                        });
            }
        });*/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userLoginEmail = email.getText().toString();
                String userLoginPassword = password.getText().toString();

                if(!TextUtils.isEmpty(userLoginEmail)&& !TextUtils.isEmpty(userLoginPassword)) {
                    loginUser(userLoginEmail, userLoginPassword);
                }else{
                    Toast.makeText(MainActivity.this, "Failed Login: Empty Inputs are not allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });
       // login.setOnClickListener(new View.OnClickListener() {
          //  @Override
          //  public void onClick(View v) {
             //   firebaseAuth.signInWithEmailAndPassword(
             //            email.getText().toString(),
             //           password.getText().toString())
              //          .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
              //              @Override
               //             public void onComplete(@NonNull Task<AuthResult> task) {
               //                 if (task.isSuccessful()) {
               //                     loginUser(email, password);
                                   // firstTextView.setText("Logging in");
                                   // openActivity();
                                   // startActivity(new Intent(getApplication(), User.class));
                  //              } else {
                  //                  Log.e("Log in error", task.getException().toString());
                 //               }
                 //           }
                 //       });
           // }
       // });
    }
    private void openActivity() {
        Intent intent = new Intent (MainActivity.this, User.class);
        startActivity(intent);
    }

    private void loginUser(final String userLoginEmail, final String userLoginPassword) {
        firebaseAuth.signInWithEmailAndPassword(userLoginEmail, userLoginPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();
                            String RegisteredUserID = currentUser.getUid();

                            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID);

                            databaseref.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String userType = dataSnapshot.child("type").getValue().toString();


                                    if(userType.equals("Parent")){
                                        Intent intentResident = new Intent(MainActivity.this, register.class);
                                        intentResident.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intentResident);
                                        finish();
                                    }else if (userType.equals("Driver")){
                                        Intent intentMain = new Intent(MainActivity.this, register.class);
                                        intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intentMain);
                                        finish();
                                    }else{
                                        Toast.makeText(MainActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }


                            });
                        }
                    }
                });
    }
   /* private void kullaniciOlustur() {
        Map<String, String> yeniUser = new HashMap<String, String>();
        yeniUser.put("name", "name");
        yeniUser.put("surname", "surname");

        DatabaseReference databaseReference = firebaseDatabase.getReference();
        databaseReference.child("users")
                .child(firebaseAuth.getCurrentUser().getUid())
                .setValue(yeniUser);
    }
    private void kullaniciGuncelle() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName("nick ver")
                .setPhotoUri(null)
                .build();

        firebaseUser.updateProfile(userProfileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Log.e("update error", task.getException().getMessage());
                        }
                       // startActivity(new Intent(MainActivity.this, User.class));
                    }
                });

    }*/
    void changeRegisterPage()
    {
        Intent register_intent = new Intent(this, register.class);
        startActivity(register_intent);
    }

}
