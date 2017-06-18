package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.support.v7.app.AppCompatActivity;
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

public class MyclasslistActivity extends Activity {

    private Spinner myclassSpinner;
    private EditText classnameText, permissioncodeText;
    private Button confirm, cancel, addorjoin, sessionselect;
    private ArrayList<String> classListt = new ArrayList<String>(),  classnameList, classArray;
    private String[] classList;
    private String class_name = "", role, user_name, session_name = "", type, user_id, classListStr = "", permissioncode, school_id, email, session_id,
            selectclass_url = "https://moviphones.com/whiteboard/class/classes.php",
            classinfo_url = "https://moviphones.com/whiteboard/class/classinfo.php",
            classenroll_url = "https://moviphones.com/whiteboard/class/classenroll.php",
            classcreate_url = "https://moviphones.com/whiteboard/class/classcreate.php",
            updateuser_url = "https://moviphones.com/whiteboard/school_user/schooluserupdate.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myclasslist);

        DisplayMetrics dm = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.widthPixels;

        getWindow().setLayout((int)(width*0.9), (int)(height*1.05));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        Intent intent = getIntent();
        role = intent.getStringExtra("role");
        user_name = intent.getStringExtra("user_name");
        user_id = intent.getStringExtra("user_id");
        classList = intent.getStringArrayExtra("classlist");
        school_id = intent.getStringExtra("school_id");
        email = intent.getStringExtra("email");
        classArray = intent.getStringArrayListExtra("classArray");

        for(int i = 0 ; i < classArray.size() ; i ++) {
            if( i == classArray.size())
                classListStr += classArray.get(i);
            else
                classListStr += classArray.get(i) + ",";
        }

        type = "getclasslist";
        GetClassList getClassList = new GetClassList(this);
        getClassList.execute(type, classListStr);

        try {
            classListt = getClassList.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        myclassSpinner = (Spinner)findViewById(R.id.myclass_spinner);

        classnameText = (EditText)findViewById(R.id.classname_text);
        permissioncodeText = (EditText)findViewById(R.id.permissioncode_text);

        confirm = (Button)findViewById(R.id.confirmmyclass_btn);
        cancel = (Button)findViewById(R.id.cancelmyclass_btn);
        addorjoin = (Button)findViewById(R.id.addorjoinmyclass_btn);
        sessionselect = (Button)findViewById(R.id.sessionselect_btn);

        confirm.setOnClickListener(onClickListener);
        cancel.setOnClickListener(onClickListener);
        addorjoin.setOnClickListener(onClickListener);
        sessionselect.setOnClickListener(onClickListener);

        if(role.equals("student")) {
            addorjoin.setText("Join");
        }

        ArrayAdapter<String> spinnerAdapter;
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, classListt);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myclassSpinner.setAdapter(spinnerAdapter);
        myclassSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(myclassSpinner.getSelectedItem() != null)
                    class_name = myclassSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

		});
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            boolean update = false;
            switch(v.getId()) {
                case R.id.confirmmyclass_btn:
                    if(class_name.equals("") || session_name.length() == 0) {
                        Toast.makeText(getApplicationContext(), "please select a class and session", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    type = "updatesessionuser";
                    BackgroundWorker updatesession = new BackgroundWorker(getApplicationContext());
                    updatesession.execute(type, user_id, session_id);
                    Intent intent1 = new Intent();
                    intent1.putExtra("class_name", class_name);
                    intent1.putExtra("session_name", session_name);
                    intent1.putExtra("permissioncode", permissioncode);
                    intent1.putExtra("session_id", session_id);
                    MyclasslistActivity.this.setResult(0, intent1);
                    MyclasslistActivity.this.finish();
                    break;
                case R.id.cancelmyclass_btn:
                    Intent intent2 = new Intent();
                    MyclasslistActivity.this.setResult(1,intent2);
                    MyclasslistActivity.this.finish();
                    break;
                case R.id.addorjoinmyclass_btn:
                    class_name = classnameText.getText().toString();
                    permissioncode = permissioncodeText.getText().toString();
                    if(role.equals("student")) {
                        type = "enrollclass";
                        GetClassList enrollclass = new GetClassList(getApplicationContext());
                        enrollclass.execute(type, class_name, permissioncode, school_id);

                        ArrayList<String> tempList = new ArrayList<>();

                        try {
                            tempList = enrollclass.get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }

                        String succ = "succeed";
                        boolean enrolled = false;

                        if(tempList.size() == 1) {
                            enrolled = false;
                        }
                        else if (tempList.get(1).toLowerCase().indexOf(succ.toLowerCase()) != -1) {
                            enrolled = true;
                            class_name = tempList.get(0) + ":" + class_name;
                            update = true;
                        } else
                            enrolled = false;

                        if(enrolled) {
                            type = "updateuser";
                            String username = user_name + email;
                            String class_id = tempList.get(0);
                            class_name = class_id + ":" + class_name;
                            GetClassList updateuser = new GetClassList(getApplicationContext());
                            updateuser.execute(type, username, class_id);
                        }
                        else
                            Toast.makeText(getApplicationContext(), "failed to enroll in " + class_name, Toast.LENGTH_SHORT).show();

                    }
                    else {
                        type = "createclass";
                        GetClassList createclass = new GetClassList(getApplicationContext());
                        createclass.execute(type, class_name, permissioncode, school_id);
                        type = "updateuser";
                        String class_id = "";
                        try {
                            ArrayList<String> tempList = createclass.get();
                            if(tempList.size() == 1) {
                                Toast.makeText(getApplicationContext(), "Class already exist", Toast.LENGTH_SHORT).show();
                                break;
                            }
                            else if(tempList.size() == 2) {
                                Toast.makeText(getApplicationContext(), "Failed to create class " + class_name, Toast.LENGTH_SHORT).show();
                                break;
                            }
                            class_id = tempList.get(1);
                            class_name = class_id + ":" + class_name;
                            update = true;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        type = "updateuser";
                        String username = user_name + email;
                        GetClassList updateuser = new GetClassList(getApplicationContext());
                        updateuser.execute(type, username, class_id);
                    }


                    if(!classListt.contains(class_name) && update)
                        updateGroupListSpinner(class_name);

                    break;
                case R.id.sessionselect_btn:
                    Intent intent3 = new Intent(getApplicationContext(), SelectsessionActivity.class);
                    intent3.putExtra("class_name", class_name);
                    intent3.putExtra("role", role);
                    intent3.putExtra("user_id", user_id);
                    intent3.putExtra("email", email);
                    startActivityForResult(intent3, 0);
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(resultCode == 0 && intent != null) {
            session_name = intent.getStringExtra("session_name");
            session_id = intent.getStringExtra("session_id");
        }
        else {
            Toast.makeText(getApplicationContext(), "Failed to start the session", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateGroupListSpinner(String newClass) {
        // enable and disable buttons based on listed groups

        // Combine list of hosted and joined groups

        classListt.add(newClass);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, classListt);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myclassSpinner.setAdapter(spinnerAdapter);
        myclassSpinner.setSelection(classListt.size() - 1);
        spinnerAdapter.notifyDataSetChanged();
    }

    private class GetClassList extends AsyncTask<String, Void, ArrayList<String>> {
        Context context;

        ArrayList<String> retVal = new ArrayList<>();

        GetClassList(Context ctx) {
            context = ctx;
        }

        @Override
        protected ArrayList<String> doInBackground(String... params) {
            retVal = new ArrayList<>();
            type = params[0];
            if (type.equals("getclasslist")) {
                try {
                    String classListString = params[1];
                    URL url = new URL(selectclass_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("class_list", "UTF-8") + "=" + URLEncoder.encode(classListString, "UTF-8");
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

            else if(type.equals("enrollclass")) {
                try {
                    String class_name = params[1];
                    String permissioncode = params[2];
                    String schoolid = params[3];
                    URL url = new URL(classenroll_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("class_name", "UTF-8") + "=" + URLEncoder.encode(class_name, "UTF-8") + "&" + URLEncoder.encode("permission_code", "UTF-8") + "=" + URLEncoder.encode(permissioncode, "UTF-8") + "&" + URLEncoder.encode("school_id", "UTF-8") + "=" + URLEncoder.encode(schoolid, "UTF-8");
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

            else if(type.equals("updateuser")) {
                try {
                    String user_name = params[1];
                    String class_name = params[2];
                    URL url = new URL(updateuser_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("user_name", "UTF-8") + "=" + URLEncoder.encode(user_name, "UTF-8") + "&" + URLEncoder.encode("class_name", "UTF-8") + "=" + URLEncoder.encode(class_name, "UTF-8");
                    bufferWriter.write(post_data);
                    bufferWriter.flush();
                    bufferWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println("updateuser line is " + line + " !!!!!!!!!!!!!!!!!");
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


            else if(type.equals("createclass")) {
                try {
                    System.out.println("create class");
                    String class_name = params[1];
                    String permissioncode = params[2];
                    String schoolid = params[3];
                    URL url = new URL(classcreate_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    httpURLConnection.setDoInput(true);

                    OutputStream outputStream = httpURLConnection.getOutputStream();
                    BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                    String post_data = URLEncoder.encode("class_name", "UTF-8") + "=" + URLEncoder.encode(class_name, "UTF-8")
                            + "&" + URLEncoder.encode("permissioncode", "UTF-8") + "=" + URLEncoder.encode(permissioncode, "UTF-8")
                            + "&" + URLEncoder.encode("school_id", "UTF-8") + "=" + URLEncoder.encode(schoolid, "UTF-8");
                    bufferWriter.write(post_data);
                    bufferWriter.flush();
                    bufferWriter.close();
                    outputStream.close();

                    InputStream inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"));
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println("line is " + line + " !!!!!!!!!!!!!!!!!!!!!!!");
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

            else if (type.equals("getclassinfo")) {
                try {
                    String college_name = params[1];
                    URL url = new URL(classinfo_url);
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
}
