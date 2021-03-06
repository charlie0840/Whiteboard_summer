package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by WMS_12 on 7/1/2016.
 */
public class RegisterActivity extends Activity {
    EditText USER_NAME, USER_PASSWORD, CONFIRM_PASSWORD, EMAIL_ADDRESS, COLLEGE, ADDRESS;
    Button registerButton, cancelButton;
    Spinner spinner;
    String user_name, user_password, confirm_password, type, user_role = "none", email_address, college, address;
    Context ctx = this;
    boolean reg = true;
    List<String> list = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        reg = intent.getBooleanExtra("reg", true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_layout);

        registerButton = (Button)findViewById(R.id.confirm_btn);
        cancelButton = (Button)findViewById(R.id.cancel_btn);
        USER_NAME = (EditText)findViewById(R.id.regusername);
        USER_PASSWORD = (EditText)findViewById(R.id.regpassword);
        CONFIRM_PASSWORD = (EditText)findViewById(R.id.confpassword);
        EMAIL_ADDRESS = (EditText)findViewById(R.id.email_text);
        COLLEGE = (EditText)findViewById(R.id.school_text);
        ADDRESS = (EditText)findViewById(R.id.address_text);
        spinner = (Spinner)findViewById(R.id.userrole_spinner);

        if(!reg)
            registerButton.setText("Unregister");

        registerButton.setOnClickListener(onClickListener);
        cancelButton.setOnClickListener(onClickListener);

        list.add("Student");
        list.add("Instructor");
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(list.get(position).equals("Student"))
                    user_role = "Student";
                else
                    user_role = "Instructor";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Async Task to check whether internet connection is working.
     **/
    private class NetCheck extends AsyncTask<String,String,Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(RegisterActivity.this);
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();
        }
        /**
         * Gets current device state and checks for working internet connection by trying Google.
         **/
        @Override
        protected Boolean doInBackground(String... args){



            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;

        }
        @Override
        protected void onPostExecute(Boolean th){

            if(th == true){
                nDialog.dismiss();
                BackgroundWorker backgroundWorker = new BackgroundWorker(ctx);
                backgroundWorker.execute(type, user_name, user_password);
            }
            else{
                nDialog.dismiss();
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }

//    public void saveInfo(View view) {
        //Context ctx = this;
//        user_name = USER_NAME.getText().toString();
//        user_password = USER_PASSWORD.getText().toString();
//        type = "register";
//        confirm_password = CONFIRM_PASSWORD.getText().toString();
//        if(!user_password.equals(confirm_password)) {
//            Toast.makeText(getApplicationContext(), "Passwordwords do not match", Toast.LENGTH_SHORT).show();
//            USER_NAME.setText("");
//            USER_PASSWORD.setText("");
//            CONFIRM_PASSWORD.setText("");
//        }
//        else {

//            new NetCheck().execute();

//            BackgroundWorker backgroundWorker = new BackgroundWorker(ctx);
//            backgroundWorker.execute(type, user_name, user_password);
//            Intent intent = new Intent(getApplicationContext(), StartActivity.class);
//            startActivity(intent);
//        }
//    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.confirm_btn:
                    user_name = USER_NAME.getText().toString();
                    user_password = USER_PASSWORD.getText().toString();
                    if(user_name.length() == 0) {
                        Toast.makeText(getApplicationContext(), "user name must not be empty", Toast.LENGTH_SHORT).show();
                        USER_PASSWORD.setText("");
                        CONFIRM_PASSWORD.setText("");
                    }
                    else if(user_password.length() < 2) {
                        Toast.makeText(getApplicationContext(), "length of password must be at least 2", Toast.LENGTH_SHORT).show();
                        USER_PASSWORD.setText("");
                        CONFIRM_PASSWORD.setText("");
                    }
                    else if(user_role.equals("none") && type.equals("unregister")) {
                        Toast.makeText(getApplicationContext(), "please select your role", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        if (reg)
                            type = "register";
                        else
                            type = "unregister";
                        confirm_password = CONFIRM_PASSWORD.getText().toString();
                        if (!user_password.equals(confirm_password)) {
                            Toast.makeText(getApplicationContext(), "Passwordwords do not match", Toast.LENGTH_SHORT).show();
                            USER_PASSWORD.setText("");
                            CONFIRM_PASSWORD.setText("");
                        } else {
                            NetCheck netCheck = new NetCheck();
                            netCheck.execute();
                            boolean retVal = false;
                            try {
                                retVal = netCheck.get();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }
                            if(retVal) {
                                BackgroundWorker backgroundWorker = new BackgroundWorker(ctx);
                                backgroundWorker.execute(type, user_name, user_password, user_role);
                                //new NetCheck().execute();

//            BackgroundWorker backgroundWorker = new BackgroundWorker(ctx);
//            backgroundWorker.execute(type, user_name, user_password);
                                //      Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                                //    startActivity(intent);
                            }
                        }
                    }
                    break;

//                    user_name = USER_NAME.getText().toString();
//                    user_password = USER_PASSWORD.getText().toString();
//                    confirm_password = CONFIRM_PASSWORD.getText().toString();
//                    if(!(user_password.equals(confirm_password)))
//                    {
//                        Toast.makeText(getApplicationContext(), "Passwordwords do not match", Toast.LENGTH_SHORT).show();
//                        USER_NAME.setText("");
//                        USER_PASSWORD.setText("");
//                        CONFIRM_PASSWORD.setText("");
//                    }
//                    else
//                    {
//                        DatabaseOperations DB = new DatabaseOperations(ctx);
//                        DB.putInformation(DB, user_name, user_password);
//                        Toast.makeText(getApplicationContext(), "Registration succeed", Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(getApplicationContext(), StartActivity.class);
//                        startActivity(intent);
//                    }
//                    break;
                case R.id.cancel_btn:
                    Intent intent = new Intent(getApplicationContext(), StartActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };
}
