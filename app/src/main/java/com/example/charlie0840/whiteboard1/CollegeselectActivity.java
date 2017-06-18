package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class CollegeselectActivity extends Activity {

    private Spinner collegeSpinner;
    private Button confirm, cancel;
    private Intent intent;
    private boolean selected = false, hasList = false;
    private String college_name, type = "classinfo", school_id, email;
    private String login_url = "https://moviphones.com/whiteboard/college/schools.php", schoolinfo_url = "https://moviphones.com/whiteboard/college/schoolinfo.php";
    private ArrayList<String> collegeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collegeselect);

        collegeSpinner = (Spinner) findViewById(R.id.collegeselect_spinner);
        confirm = (Button) findViewById(R.id.confirmcollegeselect_btn);
        cancel = (Button) findViewById(R.id.cancelcollegeselect_btn);

        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);

        NetCheck netCheck = new NetCheck();
        netCheck.execute();
        boolean retVal = false;
        try {
            retVal = netCheck.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (retVal) {

            hasList = false;

            GetCollegeList getCollegeList = new GetCollegeList(this);
            getCollegeList.execute("getList");

            try {
                collegeList = getCollegeList.get();
                hasList = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            if (hasList) {
                List<String> spinnerArray = collegeList;

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                        this, android.R.layout.simple_spinner_item, spinnerArray);

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                collegeSpinner.setAdapter(adapter);

                collegeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        college_name = (String) collegeSpinner.getSelectedItem();
                        if(!college_name.equals(""))
                            selected = true;
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.confirmcollegeselect_btn:
                    ArrayList<String> tempList = new ArrayList<>();
                    GetCollegeList getCollegeList = new GetCollegeList(getApplicationContext());
                    getCollegeList.execute("getschoolinfo", college_name);

                    try {
                        tempList = getCollegeList.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    if(tempList.size() == 0)
                       break;
                    school_id = tempList.get(0);
                    email = tempList.get(1);

                    if (selected) {
                        intent = new Intent(getApplicationContext(), CollegeloginActivity.class);
                        intent.putExtra("college_name", college_name);
                        intent.putExtra("email", email);
                        intent.putExtra("school_id", school_id);
                        startActivity(intent);
                    } else {

                        if(collegeList.size() == 0) {
                            GetCollegeList getCollegeList1 = new GetCollegeList(getApplicationContext());
                            getCollegeList1.execute("getList");
                            try {
                                collegeList = getCollegeList.get();
                                hasList = true;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            }

                            if (hasList) {
                                List<String> spinnerArray = collegeList;

                                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, spinnerArray);

                                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                collegeSpinner.setAdapter(adapter);
                            }
                        }
                        Toast.makeText(getApplicationContext(), "please select the college", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.cancelcollegeselect_btn:
                    intent = new Intent(getApplicationContext(), ClassroomtypeActivity.class);
                    startActivity(intent);
                    break;
            }
        }
    };

    private class GetCollegeList extends AsyncTask<String, Void, ArrayList<String>> {
        Context context;

        ArrayList<String> retVal = new ArrayList<>();

        GetCollegeList(Context ctx) {
            context = ctx;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            retVal = new ArrayList<>();
            type = params[0];
            if (type.equals("getList")) {
                try {
                    URL url = new URL(login_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println(line);
                        String[] parts = line.split(",");

                        for (int i = 0; i < parts.length; i++) {
                            retVal.add(parts[i]);
                        }
                    }
                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (type.equals("getschoolinfo")) {
                try {
                    String college_name = params[1];
                    URL url = new URL(schoolinfo_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("school_name", "UTF-8") + "=" + URLEncoder.encode(college_name, "UTF-8");
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

                    String[] temp = result.split(",");

                    for (int i = 0; i < temp.length; i++) {
                        retVal.add(temp[i]);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
            nDialog = new ProgressDialog(CollegeselectActivity.this);
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
                Handler handler;
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
