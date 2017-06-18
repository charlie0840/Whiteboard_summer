package com.example.charlie0840.whiteboard1;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Message;
import android.support.v4.util.LruCache;

import java.lang.ref.SoftReference;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * Created by WMS_12 on 8/3/2016.
 */
public class UpdateCanvasThread extends Thread {


// If you're running on Honeycomb or newer, create a
// synchronized HashSet of references to reusable bitmaps.

    private static final String LOG_TAG = "MyServerThread";

    private Bitmap bitmap;

    private UIHandler mUIHandler;

    private NetworkActivity  m_activityMain;

    public UpdateCanvasThread(NetworkActivity activityMain, UIHandler handler)
    {
        super();
        // Save the activity
        m_activityMain = activityMain;
        mUIHandler = handler;
    }

    @Override
    public void run()
    {
        while (true) {

            if(bitmap != null) {
                //bitmap.recycle();
                bitmap = null;
            }

            ClientReceive bkw = new ClientReceive();
            bkw.execute(m_activityMain.session_id);
            bitmap = m_activityMain.drawView.getDrawingCache();
            try {
                bitmap = bkw.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(bitmap != null) {

                m_activityMain.curr = Bitmap.createBitmap(bitmap);
                m_activityMain.drawView.updateBitmap(m_activityMain.curr, m_activityMain);
                m_activityMain.breakCount = m_activityMain.breakCount + 1;

                Message msg = mUIHandler.obtainMessage(UIHandler.RESET_CANVAS);

                mUIHandler.sendMessage(msg);


                System.out.println(m_activityMain.breakCount + " at updatecanvas thread<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (m_activityMain.breakOut) {
                    break;
                }
            }
        }
    }

}