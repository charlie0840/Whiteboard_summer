package com.example.charlie0840.whiteboard1;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ClassendActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classend);
        Intent intent = getIntent();
        String class_name = intent.getStringExtra("class_name");


        TextView textView = (TextView)findViewById(R.id.classend_text);
        textView.setText(class_name + " Ended");

        new CountDownTimer(3000, 1000) {

            public void onTick(long millisUntilFinished) {
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                finish();
            }
        }.start();
    }
}
