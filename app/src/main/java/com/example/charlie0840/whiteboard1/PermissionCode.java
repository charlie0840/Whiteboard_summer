package com.example.charlie0840.whiteboard1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Permission;
import java.util.concurrent.ExecutionException;

/**
 * Created by WMS_12 on 7/1/2016.
 */
public class PermissionCode extends Activity {

    EditText permission;
    Button confirm, confirmReturn;
    int who;
    Bundle b;
    Context ctx = this;
    String type, group_name, result, retval, class_name, college_name;
    boolean finished = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissioncode_input);

        b = getIntent().getExtras();
        group_name = b.getString("group_name");
        class_name = b.getString("class_name");
        college_name = b.getString("college_name");
        who = b.getInt("who");

        permission = (EditText)findViewById(R.id.permission_input);
        confirm = (Button)findViewById(R.id.permissionconfirm_btn);
        confirmReturn = (Button)findViewById(R.id.confirmcode_btn);


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String permissionCode = permission.getText().toString();
                BackgroundWorker backgroundWorker = new BackgroundWorker(ctx);
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
                    if (who == 1) {
                        type = "createsession";
                        backgroundWorker.execute(type, group_name, permissionCode);//, host);
                    } else if (who == 2) {
                        type = "joinsession";
                        backgroundWorker.execute(type, group_name, permissionCode);
                    } else {
                        type = "deletesession";
                        backgroundWorker.execute(type, group_name, permissionCode);
                    }
                    try {
                        result = backgroundWorker.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    String succ = "succeed";
                    String fail = "fail";

                    if (result.toLowerCase().indexOf(succ.toLowerCase()) != -1) {
                        retval = succ;
                    } else
                        retval = fail;

                    Intent intent = new Intent();

                    if (retval.equals("succeed")) {
                        intent.putExtra("create", true);
                        intent.putExtra("joined", true);
                        intent.putExtra("delete", true);
                    } else {
                        intent.putExtra("create", false);
                        intent.putExtra("joined", false);
                        intent.putExtra("delete", false);
                    }
                    intent.putExtra("permissioncode", permissionCode);
                    intent.putExtra("group_name", group_name);
                    finished = true;
                    PermissionCode.this.setResult(who, intent);
                    //PermissionCode.this.finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please connect to internet first", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("create", false);
                    intent.putExtra("joined", false);
                    intent.putExtra("delete", false);
                    intent.putExtra("permissioncode", permissionCode);
                    intent.putExtra("group_name", group_name);
                    PermissionCode.this.setResult(who, intent);

                }
            }
        });

        confirmReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!finished) {
                    Intent intent = new Intent();
                    intent.putExtra("joined", false);
                    intent.putExtra("permissioncode", "wtf");
                    intent.putExtra("group_name", group_name);
                    PermissionCode.this.setResult(who, intent);
                }
                PermissionCode.this.finish();
            }
        });

    }

    private class NetCheck extends AsyncTask<String,String,Boolean> {
        private ProgressDialog nDialog;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(PermissionCode.this);
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
//				nDialog.show();
                Handler handler = null;
                handler = new Handler();
                handler.postDelayed(new Runnable(){
                    public void run(){
                        nDialog.cancel();
                        nDialog.dismiss();
                    }
                }, 1000);
                //loginErrorMsg.setText("Error in Network Connection");
            }
        }
    }

}
