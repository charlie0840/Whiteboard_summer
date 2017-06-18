package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

/**
 * Created by WMS_12 on 7/14/2016.
 */
public class ClassroomtypeActivity extends Activity {

        Button classroomBtn, publicBtn;
        Bundle b;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_accesstype);
            classroomBtn = (Button)findViewById(R.id.classroom_btn);
            publicBtn = (Button)findViewById(R.id.publicaccess_btn);
            b = getIntent().getExtras();

            classroomBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), CollegeselectActivity.class);
                    startActivity(intent);
                }
            });

            publicBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }
}
