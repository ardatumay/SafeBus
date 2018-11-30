package com.bros.safebus.safebus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ParentInterface extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_interface);
        Button addChild = (Button) findViewById(R.id.add_child);
        addChild.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GoToChildrenRegister();
            }
        });
    }
    void GoToChildrenRegister(){
        Intent intent = getIntent();
        String parentKey = intent.getStringExtra("userKey");
        Intent i = new Intent(this, registerChild.class);
        i.putExtra("parentKey", parentKey);
        startActivity(i);
    }


}
