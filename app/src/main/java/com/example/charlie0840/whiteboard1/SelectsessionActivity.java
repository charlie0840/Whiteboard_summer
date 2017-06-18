package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class SelectsessionActivity extends Activity {

    private String role, class_name, session_name = "", user_id, email, type, session_id = "",
            sessionselect_url = "https://moviphones.com/whiteboard/class/session_select.php",
            sessioncreate_url = "https://moviphones.com/whiteboard/class/session_create.php",
            sessionget_url = "https://moviphones.com/whiteboard/class/session_get.php",
            sessionupdate_url = "https://moviphones.com/whiteboard/class/session_classupdate.php";
    private String[] classArray;
    private ArrayList<String> sessionList = new ArrayList<String>();
    private Spinner sessionSpinner;
    private EditText sessiontimeText, sessiontypeText;
    private Button confirm, cancel, addorjoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectsession);

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.widthPixels;

        getWindow().setLayout((int)(width*0.9), (int)(height*1.05));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        Intent intent = getIntent();
        role = intent.getStringExtra("role");
        class_name = intent.getStringExtra("class_name");
        user_id = intent.getStringExtra("user_id");
        email = intent.getStringExtra("email");

        classArray = class_name.split(":");

        System.out.println("class id is " + classArray[0] + " and class name is " + classArray[1]);

        sessiontimeText = (EditText)findViewById(R.id.sessiondate_text);
        sessiontypeText = (EditText)findViewById(R.id.sessiontype_text);

        confirm = (Button)findViewById(R.id.confirmselectsesson_btn);
        cancel = (Button)findViewById(R.id.cancelselectsession_btn);
        addorjoin = (Button)findViewById(R.id.addorjoinsession_btn);

        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
        addorjoin.setOnClickListener(onClickListener);

        addorjoin.setVisibility(View.INVISIBLE);
        sessiontimeText.setVisibility(View.INVISIBLE);
        sessiontypeText.setVisibility(View.INVISIBLE);

        if(role.equals("instructor")) {
            addorjoin.setText("add");
            addorjoin.setVisibility(View.VISIBLE);
            sessiontimeText.setVisibility(View.VISIBLE);
            sessiontypeText.setVisibility(View.VISIBLE);
        }

        sessionSpinner = (Spinner)findViewById(R.id.selectsession_spinner);

        type = "sessionget";
        ArrayList<String> tempList = new ArrayList<>();

        GetSessionList getSessionList = new GetSessionList(getApplicationContext());
        getSessionList.execute(type, classArray[0]);

        try {
            tempList = getSessionList.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        sessionList = tempList;


        ArrayAdapter<String> spinnerAdapter;// = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item );
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sessionList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionSpinner.setAdapter(spinnerAdapter);

        sessionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                session_name = sessionSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.confirmselectsesson_btn:
                    if(session_name.equals("")) {
                        Toast.makeText(getApplicationContext(), "please select a session to enter", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    String[] temp = session_name.split(":");
                    session_id = temp[0];
                    Intent intent1 = new Intent(getApplicationContext(), MyclasslistActivity.class);
                    intent1.putExtra("session_name", session_name);
                    intent1.putExtra("session_id", session_id);
                    SelectsessionActivity.this.setResult(0, intent1);
                    SelectsessionActivity.this.finish();
                    break;


                case R.id.cancelselectsession_btn:
                    SelectsessionActivity.this.setResult(1);
                    SelectsessionActivity.this.finish();
                    break;

                case R.id.addorjoinsession_btn:
                    String sessionTime = "";
                    String sessionType = "";
                    type = "sessioncreate";
                    ArrayList<String> tempList = new ArrayList<>();

                    sessionTime = sessiontimeText.getText().toString();
                    sessionType = sessiontypeText.getText().toString();

                    if(sessionTime.length() == 0 || sessionType.length() == 0) {
                        Toast.makeText(getApplicationContext(), "please enter session time and session type to continue", Toast.LENGTH_SHORT).show();
                        break;
                    }

                    GetSessionList sessionCreate = new GetSessionList(getApplicationContext());
                    sessionCreate.execute(type, classArray[0], sessionTime, sessionType, user_id);

                    try {
                        tempList = sessionCreate.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                    String succ = "succeed";
                    boolean enrolled = false;

                    if (tempList.get(0).toLowerCase().indexOf(succ.toLowerCase()) != -1) {
                        enrolled = true;
                    } else
                        enrolled = false;

                    if(enrolled) {
                        session_id = tempList.get(1);
                        type = "updateclass";

                        GetSessionList updateclass = new GetSessionList(getApplicationContext());
                        updateclass.execute(type, session_id, classArray[1]);

                        updateGroupListSpinner(session_id + ":" + sessionTime + "    " + sessionType);
                    }
                    else
                        Toast.makeText(getApplicationContext(), "failed to create session", Toast.LENGTH_SHORT).show();
                    //put the session into database
                    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                    break;

            }
        }
    };

    public void updateGroupListSpinner(String newSession) {
        // enable and disable buttons based on listed groups

        // Combine list of hosted and joined groups

        sessionList.add(newSession);

        String noRow = "No rows found";

        if(sessionList.size() > 1 && sessionList.contains(noRow)) {
            sessionList.remove(0);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, sessionList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sessionSpinner.setAdapter(spinnerAdapter);
        sessionSpinner.setSelection(sessionList.size() - 1);
        spinnerAdapter.notifyDataSetChanged();
    }




    private class GetSessionList extends AsyncTask<String, Void, ArrayList<String>> {
        Context context;

        ArrayList<String> retVal = new ArrayList<>();

        GetSessionList(Context ctx) {
            context = ctx;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            retVal = new ArrayList<>();
            type = params[0];
            if (type.equals("sessionselect")) {
                try {
                    String session_date = params[1];
                    String session_type = params[2];
                    URL url = new URL(sessionselect_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("session_date", "UTF-8") + "=" + URLEncoder.encode(session_date, "UTF-8") + "&" + URLEncoder.encode("session_type", "UTF-8") + "=" + URLEncoder.encode(session_type, "UTF-8");
                    bufferWriter.write(post_data);
                    bufferWriter.flush();
                    bufferWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
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
            else if (type.equals("sessioncreate")) {
                try {
                    String class_id = params[1];
                    String session_date = params[2];
                    String session_type = params[3];
                    String user_id = params[4];
                    System.out.println("class id is " + class_id + " and session_date is " + session_date + " and session_type is " + session_type + " and user_id is " + user_id);
                    URL url = new URL(sessioncreate_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("class_id", "UTF-8") + "=" + URLEncoder.encode(class_id, "UTF-8") + "&"
                            + URLEncoder.encode("session_date", "UTF-8") + "=" + URLEncoder.encode(session_date, "UTF-8") + "&"
                            + URLEncoder.encode("session_type", "UTF-8") + "=" + URLEncoder.encode(session_type, "UTF-8") + "&"
                            + URLEncoder.encode("user_id", "UTF-8") + "=" + URLEncoder.encode(user_id, "UTF-8");
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
                        System.out.println("session create line " + line);
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

            else if (type.equals("sessionget")) {
                try {
                    String class_id = params[1];
                    URL url = new URL(sessionget_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("class_id", "UTF-8") + "=" + URLEncoder.encode(class_id, "UTF-8");
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
                        System.out.println("session get line " + line);
                    }

                    bufferedReader.close();
                    inputStream.close();
                    httpURLConnection.disconnect();

                    String[] temp = result.split(",");

                    for (int i = 0; i < temp.length; i++) {
                        if(temp[i].length() > 0)
                            retVal.add(temp[i]);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (type.equals("updateclass")) {
                try {
                    String session_id = params[1];
                    String class_name = params[2];
                    URL url = new URL(sessionupdate_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);
                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("session_id", "UTF-8") + "=" + URLEncoder.encode(session_id, "UTF-8")  + "&" + URLEncoder.encode("class_name", "UTF-8") + "=" + URLEncoder.encode(class_name, "UTF-8");
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
                        System.out.println("session update class line " + line);
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





}
