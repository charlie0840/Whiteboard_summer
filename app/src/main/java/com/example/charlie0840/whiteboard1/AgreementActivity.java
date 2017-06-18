package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AgreementActivity extends Activity {

    private Button confirm, cancel;
    private String user_name, college_name, role, school_id, email, user_id;
    private String[] classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agreement);
        Intent intent = getIntent();
        user_name = intent.getStringExtra("user_name");
        college_name = intent.getStringExtra("college_name");
        role = intent.getStringExtra("role");
        classList = intent.getStringArrayExtra("classlist");
        school_id = intent.getStringExtra("school_id");
        email = intent.getStringExtra("email");
        user_id = intent.getStringExtra("id");

        confirm = (Button)findViewById(R.id.confirmagreement_btn);
        cancel = (Button)findViewById(R.id.cancelagreement_btn);

        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.confirmagreement_btn:
                    Intent intent = new Intent(getApplicationContext(), NetworkActivity.class);
                    intent.putExtra("user_name", user_name);
                    intent.putExtra("college_name", college_name);
                    intent.putExtra("role", role);
                    intent.putExtra("classlist", classList);
                    intent.putExtra("school_id", school_id);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("email", email);
                    startActivity(intent);
                    break;
                case R.id.cancelagreement_btn:
                    Intent intent2 = new Intent(getApplicationContext(),CollegeloginActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };
}
