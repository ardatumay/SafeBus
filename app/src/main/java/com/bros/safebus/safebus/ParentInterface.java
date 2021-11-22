/******************************************************************************
 *  Class Name: ParentInterface
 *  Author: Arda
 *
 * This is home page of the parent where several buttons are placed to interact with
 * Each button povides differet features
 *  This class provides you to sign up screen for the child
 *  Also it sends you to MapsActivty class to see the map
 *  Also it deals with notifications
 *
 *  Revisions: Efe: Added database checking for notifications for all children about school and home
 *             Can: Added intent switches, updated markers.
 ******************************************************************************/


package com.bros.safebus.safebus;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

    public static int NOTIFICATION_ID = 3131;
    NotificationManager mNotificationManager;
    NotificationChannel mChannel;
    String DriverKey;


    private SharedPreferences preferences;
    private SharedPreferences.Editor preferenceEditor;
    /******************************************************************************
     * Tasks are created to wait result of the firebase db calls before running an process that depends on the result
     * Author: Arda
     ******************************************************************************/
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
        Button logout = (Button) findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /******************************************************************************
                 * if logout clicked delete the permanent user data from phone and logout the user
                 * shared resource structure provides key-value pair data structure which is a permanent storage
                 * It can be user as a soft database where there is no requirement for sqlite
                 * Author: Arda
                 ******************************************************************************/
                preferences = getSharedPreferences("credentials", Context.MODE_PRIVATE);
                preferenceEditor = preferences.edit();
                preferenceEditor.putString("userMail", "");
                preferenceEditor.putString("userPass", "");
                preferenceEditor.putString("userKey", "");
                preferenceEditor.putString("userType", "");
                preferenceEditor.commit();
                preferenceEditor.apply();


                firebaseAuth.signOut();
                GoToHome();

                finish();
            }
        });
        /******************************************************************************
         * create notification channel for devices whose api level greater than or equal to 26
         * for other devices does not create notification channel becase other devices has only one channel
         * Author: Arda
         ******************************************************************************/
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 26) {
            CreateNotifChannel();
        }

        /******************************************************************************
         * Get registered children to the parent from firebase to gent children data in other pages
         * it gets children name to be put on buttons and children key to find related children from firebase
         * Author: Arda
         ******************************************************************************/
        FirebaseUser currentUser = firebaseAuth.getInstance().getCurrentUser();//get the unique id of parent
        final String RegisteredUserID = currentUser.getUid();
        final DatabaseReference databaserefChild = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID).child("children");
        databaserefChild.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
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
                        children.put(ds.child("name").getValue(String.class) + "UpperKey", ds.getKey());
                        childrenNames.add(ds.child("name").getValue(String.class));

                        Log.w("TAG", "child keyss in parent" + ds.getKey());
                       



                        /******************************************************************************
                         * Listen notify variable of each child so that it can show notifications based on each child
                         * Author: Arda
                         ******************************************************************************/
                        final DatabaseReference databaserefNotify = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID).child("children").child(ds.getKey()).child("notify");
                        databaserefNotify.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((boolean) dataSnapshot.getValue()) {
                                    databaserefNotify.getParent().child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            SentNotif(dataSnapshot.getValue(String.class), " is far away from bus.");
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dbSource.setException(databaseError.toException());
                            }
                        });
                        /******************************************************************************
                         * Listen notify variable of each child so that it can show notifications based on each child
                         * Author: Efe
                         ******************************************************************************/
                        final DatabaseReference databaserefNotifyHome = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID).child("children").child(ds.getKey()).child("notifyHome");
                        databaserefNotifyHome.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((boolean) dataSnapshot.getValue()) {
                                    databaserefNotifyHome.getParent().child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            SentNotif(dataSnapshot.getValue(String.class), " came to home.");

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dbSource.setException(databaseError.toException());
                            }
                        });
                        /******************************************************************************
                         * Listen notify variable of each child so that it can show notifications based on each child
                         * Author: Efe
                         ******************************************************************************/
                        final DatabaseReference databaserefNotifySchool = FirebaseDatabase.getInstance().getReference().child("parents").child(RegisteredUserID).child("children").child(ds.getKey()).child("notifySchool");
                        databaserefNotifySchool.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if ((boolean) dataSnapshot.getValue()) {
                                    databaserefNotifySchool.getParent().child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            SentNotif(dataSnapshot.getValue(String.class), " came to school.");

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                } else {
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                dbSource.setException(databaseError.toException());
                            }
                        });

                    }

                    if (childrenNames.size() != 0) {
                        CreateButtons(childrenNames);
                    }
                }
            }
        });
        Log.d("Child name", "Child Names: " + childrenNames);
    }

    /******************************************************************************
     * Create notificaiton channel
     * Author: Arda
     ******************************************************************************/
    void CreateNotifChannel() {
        // The id of the channel.
        String id = "my_channel_01";
        // The user-visible name of the channel.
        CharSequence name = "Channel 1";
        // The user-visible description of the channel.
        String description = "Notif channel";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        if (Build.VERSION.SDK_INT >= 26) {
            mChannel = new NotificationChannel(id, name, importance);
            // Configure the notification channel.
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.BLUE);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }

    }


    /******************************************************************************
     * Create notification and show it in the device based on api level
     * api level 26 or higher requires notif. channel
     * Author: Arda
     ******************************************************************************/
    void SentNotif(String name, String content) {

        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "my_channel_01";
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = new Notification.Builder(this,CHANNEL_ID )
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.safebuslogo)
                    .setContentText(name + content)
                    .setChannelId(CHANNEL_ID)
                    .build();
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        } else {
           /* notification = new Notification.Builder(this)
                    //.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.safebuslogo)
                    .setContentText(name + " is far away from bus")
                    .build();*/
            NotificationCompat.Builder notificationCompatBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            notificationCompatBuilder.setContentTitle(getString(R.string.app_name));
            notificationCompatBuilder.setContentText(name + content);
            notificationCompatBuilder.setSmallIcon(R.drawable.safebuslogo);
            notificationCompatBuilder.setAutoCancel(true);
            notificationCompatBuilder.setContentIntent(pendingIntent);
            notificationCompatBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);

           // mNotificationManager.notify(NOTIFICATION_ID, notificationCompatBuilder.build());

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(0, notificationCompatBuilder.build());
        }


    }

    /******************************************************************************
     * Not used, sentNotif used instead
     * Author: Efe
     ******************************************************************************/
    void SentNotifHome(String name) {

        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "my_channel_01";
        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.safebuslogo)
                .setContentText(name + " came to Home")
                .setChannelId(CHANNEL_ID)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /******************************************************************************
     * Not used, sentNotif used instead
     * Author: Efe
     ******************************************************************************/
    void SentNotifSchool(String name) {

        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        String CHANNEL_ID = "my_channel_01";
        Notification notification = new Notification.Builder(this)
                //.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.safebuslogo)
                .setContentText(name + " came to School")
                .setChannelId(CHANNEL_ID)
                .build();

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    /******************************************************************************
     * To disable back button in home page
     * Author: Arda
     ******************************************************************************/
    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    void GoToHome() {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    /******************************************************************************
     * Not used, onCreate method is used instead
     * Author: Arda
     ******************************************************************************/
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


    /******************************************************************************
     * Creating for dynamic buttons
     * Author: Arda
     ******************************************************************************/
    void CreateButtons(List<String> names) {
        for (int i = 0; i < childrenNames.size(); i++) {
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

    View.OnClickListener OnClikChild = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button b = (Button) view;
            String buttonText = b.getText().toString();
            String childKey = children.get(buttonText);
            String childUpperKey = children.get(buttonText + "UpperKey");
            /*final DatabaseReference databaseref = FirebaseDatabase.getInstance().getReference().child("children").child(childKey).child("driverKey");
            databaseref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.w("DRIVER KEY" , "KEY: "+dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    dbSource.setException(databaseError.toException());
                }
            });*/
            GoToChildInterface(childKey, childUpperKey, buttonText);
        }
    };

    /******************************************************************************
     * Goes to middle page for showing children related buttons and features
     * parent reaches map from this page
     * Author: Arda
     ******************************************************************************/
    void GoToChildInterface(String childKey, String childUpperKey, String childFullName) {
        Intent i = new Intent(this, ParentChildInterface.class);
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        i.putExtra("parentKey", parentKey);
        i.putExtra("childKey", childKey);
        i.putExtra("childUpperKey", childUpperKey);
        i.putExtra("childFullName", childFullName);
        startActivity(i);
    }

    /******************************************************************************
     * goes to registration page of the children
     * Author: Arda
     ******************************************************************************/

    void GoToChildrenRegister() {
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}

