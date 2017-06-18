package com.example.charlie0840.whiteboard1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class CollegeloginActivity extends AppCompatActivity {

    private Button confirm, cancel;
    private EditText emailText, passwordText;
    private TextView collegeView;
    private String college_name, username = "charlie", role, school_id, id, email, login_email, password,
            collegelogin_url = "https://moviphones.com/whiteboard/college/schoollogin.php";
    private ArrayList<String> tempList = new ArrayList<>(), classes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collegelogin);
        Intent intent = getIntent();
        college_name = intent.getStringExtra("college_name");
        email = intent.getStringExtra("email");
        school_id = intent.getStringExtra("school_id");

        collegeView = (TextView)findViewById(R.id.collegeloginname_text);
        collegeView.setText(college_name);

        emailText = (EditText)findViewById(R.id.username_college);
        passwordText = (EditText)findViewById(R.id.password_college);

        confirm = (Button)findViewById(R.id.confirmcollegelogin_btn);
        cancel = (Button)findViewById(R.id.cancelcollegelogin_btn);

        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.confirmcollegelogin_btn:
                    login_email = emailText.getText().toString();
                    password = passwordText.getText().toString();

                    login_email = login_email + email;

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
                        CollegeLogin collegeLogin = new CollegeLogin(getApplicationContext());
                        collegeLogin.execute(login_email, password);
                        try {
                            tempList = collegeLogin.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        id = tempList.get(0);

                        String fail = "fail";

                        if (id.toLowerCase().indexOf(fail.toLowerCase()) != -1) {
                            Toast.makeText(getApplicationContext(), "Login fail", Toast.LENGTH_SHORT).show();
                        } else {
                            role = tempList.get(1);
                            school_id = tempList.get(2);
                            String[] tempUsername = login_email.split("@");
                            username = tempUsername[0];

                            Intent intent = new Intent(getApplicationContext(), AgreementActivity.class);

                            intent.putExtra("college_name", college_name);
                            intent.putExtra("user_name", username);
                            intent.putExtra("role", role);
                            intent.putExtra("id", id);
                            intent.putExtra("school_id", school_id);
                            intent.putExtra("email", email);

                            String[] classList = new String[classes.size()];

                            for (int i = 0; i < classes.size(); i++) {
                                classList[i] = classes.get(i);
                            }
                            intent.putExtra("classlist", classList);
                            startActivity(intent);
                        }
                    }
                    break;
                case R.id.cancelcollegelogin_btn:
                    Intent intent2 = new Intent(getApplicationContext(), CollegeselectActivity.class);
                    startActivity(intent2);
                    break;
            }
        }
    };

    private class CollegeLogin extends AsyncTask<String, Void, ArrayList<String>> {
        Context context;

        ArrayList<String> retVal = new ArrayList<>();

        CollegeLogin(Context ctx) {
            context = ctx;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            retVal = new ArrayList<>();
            try {
                String user = params[0];
                String pwd = params[1];
                URL url = new URL(collegelogin_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String post_data = URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8") + "&" + URLEncoder.encode("user_password","UTF-8")+"="+URLEncoder.encode(pwd,"UTF-8");
                bufferWriter.write(post_data);
                bufferWriter.flush();
                bufferWriter.close();
                outputStream.close();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                String result = "";
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();
                inputStream.close();
                httpURLConnection.disconnect();
                String[] temp = result.split(";");
                for (int i = 0; i < temp.length; i++) {
                    if( i == 1) {
                        String[] tempClasses = temp[i].split(",");
                        for(int j = 0; j < tempClasses.length; j ++) {
                            classes.add(tempClasses[j]);
                        }
                    }
                    else {
                        retVal.add(temp[i]);
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return retVal;

        }

        @Override
        protected void onPreExecute () {
        }

        @Override
        protected void onPostExecute (ArrayList<String> result){
        }

        @Override
        protected void onProgressUpdate (Void...values){
            super.onProgressUpdate(values);
        }
    }

    private class NetCheck extends AsyncTask<String,String,Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(CollegeloginActivity.this);
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
            }
            else{
                nDialog.setMessage("not connected to netword");
                Handler handler = null;
                handler = new Handler();
                handler.postDelayed(new Runnable(){
                    public void run(){
                        nDialog.cancel();
                        nDialog.dismiss();
                    }
                }, 1000);
            }
        }
    }
}
