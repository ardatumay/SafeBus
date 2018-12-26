package com.bros.safebus.safebus;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bros.safebus.safebus.models.Driver;
import com.bros.safebus.safebus.models.Parent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class register extends AppCompatActivity {
    private final FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    DatabaseReference databaseReference;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        final RelativeLayout r = (RelativeLayout)findViewById(R.id.register);

        r.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

            }
        });

        final EditText email = (EditText) findViewById(R.id.email);
        final EditText driverEmail = (EditText) findViewById(R.id.email_driver);
        final EditText password = (EditText) findViewById(R.id.password);
        final EditText driverPassword = (EditText) findViewById(R.id.password_driver);
        final EditText name = (EditText) findViewById(R.id.name);
        final EditText driverName = (EditText) findViewById(R.id.name_driver);
        final EditText surname = (EditText) findViewById(R.id.surname);
        final EditText driverSurname = (EditText) findViewById(R.id.surname_driver);
        final EditText homeAddress = (EditText) findViewById(R.id.home_Address);
        final EditText driverSchoolAddress = (EditText) findViewById(R.id.school_Address);
        final EditText phoneNumber = (EditText) findViewById(R.id.phone_Number);
        final EditText driverPhoneNumber = (EditText) findViewById(R.id.phone_Number_driver);
        final EditText plateNumber = (EditText) findViewById(R.id.plate_Number);
        final Button register = (Button) findViewById(R.id.register_Button_Parent);
        final Button driverRegister = (Button) findViewById(R.id.register_Button_Driver);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_Group);
        final RadioButton parentButton = (RadioButton) findViewById(R.id.parent_Button);
        final RadioButton driverButton = (RadioButton) findViewById(R.id.driver_Button);

       /* name.setVisibility(View.VISIBLE);
        deneme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name.setVisibility((View.INVISIBLE));
            }
        });*/
        driverName.setVisibility(View.INVISIBLE);
        driverEmail.setVisibility(View.INVISIBLE);
        driverPassword.setVisibility(View.INVISIBLE);
        driverPhoneNumber.setVisibility(View.INVISIBLE);
        driverSchoolAddress.setVisibility(View.INVISIBLE);
        driverSurname.setVisibility(View.INVISIBLE);
        plateNumber.setVisibility(View.INVISIBLE);
        driverRegister.setVisibility(View.INVISIBLE);
        name.setVisibility(View.INVISIBLE);
        email.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        phoneNumber.setVisibility(View.INVISIBLE);
        homeAddress.setVisibility(View.INVISIBLE);
        surname.setVisibility(View.INVISIBLE);
        register.setVisibility(View.INVISIBLE);


        View.OnClickListener first_radio_listener = new View.OnClickListener() {
            public void onClick(View v) {
                driverName.setVisibility(View.INVISIBLE);
                driverEmail.setVisibility(View.INVISIBLE);
                driverPassword.setVisibility(View.INVISIBLE);
                driverPhoneNumber.setVisibility(View.INVISIBLE);
                driverSchoolAddress.setVisibility(View.INVISIBLE);
                driverSurname.setVisibility(View.INVISIBLE);
                plateNumber.setVisibility(View.INVISIBLE);
                driverRegister.setVisibility(View.INVISIBLE);
                name.setVisibility(View.VISIBLE);
                email.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                phoneNumber.setVisibility(View.VISIBLE);
                homeAddress.setVisibility(View.VISIBLE);
                surname.setVisibility(View.VISIBLE);
                register.setVisibility(View.VISIBLE);
            }
        };
        parentButton.setOnClickListener(first_radio_listener);

        View.OnClickListener second_radio_listener = new View.OnClickListener() {
            public void onClick(View v) {
                name.setVisibility(View.INVISIBLE);
                email.setVisibility(View.INVISIBLE);
                password.setVisibility(View.INVISIBLE);
                phoneNumber.setVisibility(View.INVISIBLE);
                homeAddress.setVisibility(View.INVISIBLE);
                surname.setVisibility(View.INVISIBLE);
                register.setVisibility(View.INVISIBLE);
                driverName.setVisibility(View.VISIBLE);
                driverEmail.setVisibility(View.VISIBLE);
                driverPassword.setVisibility(View.VISIBLE);
                driverPhoneNumber.setVisibility(View.VISIBLE);
                driverSchoolAddress.setVisibility(View.VISIBLE);
                driverSurname.setVisibility(View.VISIBLE);
                plateNumber.setVisibility(View.VISIBLE);
                driverRegister.setVisibility(View.VISIBLE);
            }
        };
        driverButton.setOnClickListener(second_radio_listener);

        /*name.setVisibility(View.VISIBLE);

        if (parentButton.isChecked())
        {
            name.setVisibility(View.INVISIBLE);
        }*/


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String email_Address = email.getText().toString().trim();
                if (TextUtils.isEmpty(email_Address)) {
                    Toast.makeText(register.this, "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String pass = password.getText().toString();
                if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(register.this, "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String Name = name.getText().toString();
                if (TextUtils.isEmpty(Name)) {
                    Toast.makeText(register.this, "Enter name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String Surname = surname.getText().toString();
                if (TextUtils.isEmpty(Surname)) {
                    Toast.makeText(register.this, "Enter surname!", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String Address = homeAddress.getText().toString();
                //final Editable number = phoneNumber.getText();
                final long number = Long.parseLong(phoneNumber.getText().toString());
                final String parentRole = parentButton.getText().toString();


                //final String number = phoneNumber.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(email_Address, pass)
                        .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //firstTextView.setText("Signing up");
                                    String type = "parent";
                                    databaseReference = firebaseDatabase.getReference();
                                    String parentKey = firebaseAuth.getCurrentUser().getUid();
                                    final Parent newParent = new Parent(Name, Surname, email_Address, pass, Address, number, parentKey, parentRole);
                                    databaseReference.child("parents").child(parentKey)
                                            .setValue(newParent);
                                    //set value for users
                                    //extract @ and . character from mail and pur it as a key
                                    String mailAsUsername = CreateUsernameFromEmail(email_Address);
                                    HashMap<String, String> userRecord = new HashMap<String, String>();
                                    userRecord.put("key", parentKey);
                                    userRecord.put("type", "Parents");
                                    //String keyForUsers = databaseReference.child("users").child(mailAsUsername).child("children").push().getKey();
                                    databaseReference.child("users").child(mailAsUsername)
                                            .setValue(userRecord);
                                    returnMainPage();

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


        driverRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final String driver_Email_Address = driverEmail.getText().toString();
                final String driver_Pass = driverPassword.getText().toString();
                final String driver_Name = driverName.getText().toString();
                final String driver_Surname = driverSurname.getText().toString();
                final String driver_Address = driverSchoolAddress.getText().toString();
                //final Editable number = phoneNumber.getText();
                final long driver_phoneNumber = Long.parseLong(driverPhoneNumber.getText().toString());
                final String driver_plateNumber = plateNumber.getText().toString();
                final String driverRole = driverButton.getText().toString();

                //final String number = phoneNumber.getText().toString();
                firebaseAuth.createUserWithEmailAndPassword(driver_Email_Address, driver_Pass)
                        .addOnCompleteListener(register.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    //firstTextView.setText("Signing up");

                                    databaseReference = firebaseDatabase.getReference();
                                    String driverKey = firebaseAuth.getCurrentUser().getUid();
                                    boolean trackLocation = true;
                                    final Driver newDriver = new Driver(driver_Name, driver_Surname, driver_Email_Address, driver_Pass, driver_Address, driver_phoneNumber, driver_plateNumber, driverKey, driverRole, trackLocation);
                                    databaseReference.child("drivers").child(firebaseAuth.getCurrentUser().getUid())
                                            .setValue(newDriver);
                                    //set value for users
                                    //extract @ and . character from mail and pur it as a key
                                    String mailAsUsername = CreateUsernameFromEmail(driver_Email_Address);
                                    HashMap<String, String> userRecord = new HashMap<String, String>();
                                    userRecord.put("key", driverKey);
                                    userRecord.put("type", "Drivers");
                                    //String keyForUsers = databaseReference.child("users").child(mailAsUsername).child("children").push().getKey();
                                    databaseReference.child("users").child(mailAsUsername)
                                            .setValue(userRecord);

                                    returnMainPage();
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

    void returnMainPage() {
        Intent main_Page = new Intent(this, MainActivity.class);
        startActivity(main_Page);
    }

}
