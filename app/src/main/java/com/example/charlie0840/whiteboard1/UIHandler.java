/******************************************************************************
 * Copyright 2013, Qualcomm Innovation Center, Inc.
 *
 *    All rights reserved.
 *    This file is licensed under the 3-clause BSD license in the NOTICE.txt
 *    file for this project. A copy of the 3-clause BSD license is found at:
 *
 *        http://opensource.org/licenses/BSD-3-Clause.
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the license is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the license for the specific language governing permissions and
 *    limitations under the license.
 ******************************************************************************/

package com.example.charlie0840.whiteboard1;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.alljoyn.bus.Status;

import java.io.IOException;
import java.io.StreamCorruptedException;

//import android.util.Log;

public class UIHandler extends Handler {
    
    //private static final String TAG = "GroupManagerApp";

    /* UI Handler Codes */
    public static final int TOAST_MSG = 0;
    public static final int TOGGLE_DISCOVERY_BUTTONS = 1;
    public static final int UPDATE_GROUP_LIST_SPINNER = 2;
    public static final int SEND_MSG = 3;
    public static final int RESET_CANVAS = 4;
    public static final int UPDATE_CANVAS = 5;
    
    private NetworkActivity mActivity;
    
    public UIHandler (NetworkActivity activity) {
        mActivity = activity;
    }
    
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case RESET_CANVAS:
                System.out.println("reset canvas>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                Paint p = mActivity.drawView.drawPaint;
                mActivity.cleanOut();
                mActivity.drawView.drawCanvas.drawBitmap(mActivity.curr, 0, 0, p);
                mActivity.drawView.drawPaint = p;
                break;
            case TOAST_MSG:
            	Toast.makeText(mActivity.getApplicationContext(), (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
            case UPDATE_CANVAS:
                mActivity.cleanOut();
                mActivity.drawView.drawCanvas.drawBitmap(mActivity.curr, 0, 0, mActivity.drawView.drawPaint);
                break;
            default:
                break;
        }
    }
    
    public void logInfo(String msg) {
        //Log.i(TAG, msg);
    }
}
