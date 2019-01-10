/******************************************************************************
 *  Class Name: AnimationActivity
 *  Author: Can
 *
 * This class playing an animation for UI.
 *
 ******************************************************************************/

package com.bros.safebus.safebus.Animation;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;

import com.bros.safebus.safebus.MainActivity;
import com.bros.safebus.safebus.R;


public class AnimationActivity extends AppCompatActivity {

    private static int SPLASH_TIME_OUT = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.animation_layout);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do any action here. Now we are moving to next page
                Intent myIntent = new Intent(AnimationActivity.this, MainActivity.class);
                startActivity(myIntent);
                /* This 'finish()' is for exiting the app when back button pressed
                 *  from Home page which is ActivityHome
                 */
                finish();
            }
        }, SPLASH_TIME_OUT);


    }
}


