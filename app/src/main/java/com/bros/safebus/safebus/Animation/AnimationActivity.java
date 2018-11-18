package com.bros.safebus.safebus.Animation;


import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;

import com.bros.safebus.safebus.R;


public class AnimationActivity extends AppCompatActivity {

    @Override
    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        setContentView(R.layout.animation_layout);
    }



}


