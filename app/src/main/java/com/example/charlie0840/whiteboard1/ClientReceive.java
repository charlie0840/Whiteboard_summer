package com.example.charlie0840.whiteboard1;

/**
 * Created by WMS_12 on 7/29/2016.
 */

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class ClientReceive extends AsyncTask<String,Void,Bitmap> {
    ProgressDialog loading;

    SoftReference<Bitmap> ref;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Bitmap b) {
        super.onPostExecute(b);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String add = "https://moviphones.com/whiteboard/class/z_receiveBitmap.php";
        URL url = null;
        Bitmap image = null;
//        try {
//            url = new URL(add);
//            image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        try {
            String session_id = params[0];
            url = new URL(add);
            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("session_id", "UTF-8")+"="+URLEncoder.encode(session_id,"UTF-8");
            bufferWriter.write(post_data);
            bufferWriter.flush();
            bufferWriter.close();
            outputStream.close();
            InputStream inputStream = httpURLConnection.getInputStream();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream,"iso-8859-1"));
            String result = "";
            String line = "";
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            bufferedReader.close();
            inputStream.close();
            httpURLConnection.disconnect();

            Bitmap bitmap = null;
            String[] retVal = result.split(">>>>>");
            System.out.println("testing clientReceive retVal[1]:" + retVal[1]);
            if(retVal[1].equals("0"))
                return null;

            if(result != null) {
                byte[] encodeByte = Base64.decode(retVal[0], Base64.DEFAULT);
                bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                ref = new SoftReference<Bitmap>(bitmap);
            }
            return bitmap;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }
}