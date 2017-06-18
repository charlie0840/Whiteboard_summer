package com.example.charlie0840.whiteboard1;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ClassstartActivity extends AppCompatActivity {

    private TextView mTextField, classnameText;

    private String classname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classstart);

        Intent intent = getIntent();
        classname = intent.getStringExtra("class_name");


        mTextField = (TextView)findViewById(R.id.countdown);
        classnameText = (TextView)findViewById(R.id.classname_countdown);

        classnameText.setText(classname);

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextField.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                mTextField.setText("Start!");
                finish();
            }

        }.start();
    }
}
