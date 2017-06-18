package com.example.charlie0840.whiteboard1;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by WMS_12 on 7/7/2016.
 */
public class Client extends AsyncTask<Object,Void,String>{

    String updateImage_url = "https://moviphones.com/whiteboard/class/update_image.php";

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("this is client reporting: " + s + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }

    @Override
    protected String doInBackground(Object... params) {
        try {
            String session_id = (String)params[1];
            Bitmap bitmap = Bitmap.createBitmap((Bitmap)params[2]);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            URL url = new URL(updateImage_url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
            String post_data = URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(image, "UTF-8") +
                    "&" + URLEncoder.encode("session_id", "UTF-8") + "=" + URLEncoder.encode(session_id, "UTF-8");
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

            return result;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}