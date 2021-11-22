/******************************************************************************
 *  Class Name: MainActivity
 *  Author: Efe
 *
 *  This is the MainActivity of the app,
 *  When the application starts it shows a login texts and sign up option to user
 *  It takes the parameters from the database and logins the user successfully
 *
 *
 ******************************************************************************/



package com.bros.safebus.safebus;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String userType = "";
    private ProgressBar progressBar;

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        /******************************************************************************
         * Defining EditText's and Buttons and radio groups
         * Author: Efe
         ******************************************************************************/
        final EditText email = (EditText) findViewById(R.id.email);
        final EditText password = (EditText) findViewById(R.id.password);
        final String sa ;
        // final Button register = (Button) findViewById(R.id.register);
        final Button login = (Button) findViewById(R.id.login_Button);
        // final TextView firstTextView = (TextView) findViewById(R.id.textView);
        final Button sign_up = (Button) findViewById(R.id.signup_Button);

        hideKeyboard(email);
        hideKeyboard(password);

        SharedPreferences preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
        String USERMAIL = preferences.getString("userMail", "");
        String USERPASS = preferences.getString("userPass", "");

        if(USERMAIL != "" && USERPASS != "" ){
            loginUser(USERMAIL, USERPASS);
            progressBar.setVisibility(View.VISIBLE);
            login.setVisibility(View.INVISIBLE);
            sign_up.setVisibility(View.INVISIBLE);
            email.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
        }


        /******************************************************************************
         * If the user clicks on sign up button it shows the sigh up screen
         * Author: Efe
         ******************************************************************************/
        sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRegisterPage();
            }
        });


        /******************************************************************************
         * If the user clicks to login, it takes the needed inputs (email and password)
         * and logins for the user
         * Author: Efe
         ******************************************************************************/
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userLoginEmail = email.getText().toString();
                String userLoginPassword = password.getText().toString();

                if(!TextUtils.isEmpty(userLoginEmail)&& !TextUtils.isEmpty(userLoginPassword)) {
                    Log.d("USER LOGIN MAIL","CALL LOGIN" );

                    loginUser(userLoginEmail, userLoginPassword);
                }else{
                    Log.d("USER LOGIN TYPE", "LOGIN EMPTY");
                    Toast.makeText(MainActivity.this, "Failed Login: Empty Inputs are not allowed", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    void SetUserType(String val){
        Log.d("SET USERTYPE", "NEW VAL" + val );
        userType = val;
    }
    /******************************************************************************
     * By using firebase, it logins the user and check for the user's type and
     * send the user proper page for it's type after login.
     * Author: Efe
     ******************************************************************************/
    private void loginUser(final String userLoginEmail, final String userLoginPassword) {
        firebaseAuth.signInWithEmailAndPassword(userLoginEmail, userLoginPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();
                            final String RegisteredUserID = currentUser.getUid();

                            String user = userLoginEmail;
                            user = user.replace("@","");
                            user = user.replace(".","");
                            Log.v("deneme", user);

                            preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                            preferenceEditor = preferences.edit();
                            preferenceEditor.putString("userMail", userLoginEmail);
                            preferenceEditor.putString("userPass",userLoginPassword);
                            preferenceEditor.commit();
                            preferenceEditor.apply();

                            final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("users").child(user);

                            databaseref.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String userType = dataSnapshot.child("type").getValue().toString().toLowerCase();
                                    String key = dataSnapshot.child("key").getValue().toString();

                                    if(userType.equals("parents")){

                                        GoToParentHome(key);
                                    }else if (userType.equals("drivers")){

                                        GoToDriverHome(key);
                                    }else if(userType.equals("children")) {
                                        GoToChildrenHome(key);
                                    }
                                    else{
                                        Toast.makeText(MainActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }


                            });
                        }else{
                            Toast.makeText(MainActivity.this, "Failed Login. Please Try Again", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }



    /******************************************************************************
     * Switch intent to ParenntInterface by taking the key
     * Author: Efe
     ******************************************************************************/
    void GoToParentHome(String userKey){
        Intent i = new Intent(this, ParentInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
    /******************************************************************************
     * Switch intent to ChildrenInterface by taking the key
     * Author: Efe
     ******************************************************************************/
    void GoToChildrenHome(String userKey){
        Intent i = new Intent(this, ChildrenInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
    /******************************************************************************
     * Switch intent to DriverInterface by taking the key
     * Author: Efe
     ******************************************************************************/
    void GoToDriverHome(String userKey){
        Intent i = new Intent(this, DriverInterface.class);
        i.putExtra("userKey", userKey);
        startActivity(i);
    }
    /******************************************************************************
     * Switch intent to sign up page
     * Author: Efe
     ******************************************************************************/
    void changeRegisterPage()
    {
        Intent register_intent = new Intent(this, register.class);
        startActivity(register_intent);
    }



    public void hideKeyboard(EditText editText){
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

    }

}
