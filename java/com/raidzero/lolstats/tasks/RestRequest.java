package com.raidzero.lolstats.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.interfaces.RestRequestListener;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.raidzero.lolstats.global.Common.REQUEST_TYPE;

/**
 * Created by posborn on 3/31/15.
 */
public class RestRequest {
    private final String tag = "RestRequest";

    private URL mRequestUrl;
    private REQUEST_TYPE mReqType;
    private RestRequestListener mRequestListener;

    // handler to invoke callback method
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mRequestListener.onRestRequestComplete(mReqType, (String) msg.obj);
        }
    };

    public RestRequest(Context context, REQUEST_TYPE reqType, String requestPath) {
        mRequestListener = (RestRequestListener) context;
        mReqType = reqType;

        // always start rest requests with API prefix
        String requestString = Common.API_PREFIX;

        // add in actual request path
        requestString += requestPath;

        // stick API key on the end
        requestString += "?api_key=" + Common.API_KEY;

        try {
            mRequestUrl = new URL(requestString);
        } catch (MalformedURLException e) {
            Log.e(tag, "Malformed URL!" + e.getMessage());
        }
    }

    private Runnable restRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URLConnection urlConnection = mRequestUrl.openConnection();
                urlConnection.setUseCaches(true);
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                byte[] buffer = new byte[1024]; // read 1024 bytes at a time

                int bytesRead = 0;

                String response = "";
                while((bytesRead = bis.read(buffer)) != -1){
                    response += new String(buffer, 0, bytesRead);
                }

                Message completeMsg = new Message();
                completeMsg.obj = response;

                mHandler.sendMessage(completeMsg);
            } catch (IOException e) {
                Log.e(tag, e.getMessage());
            }
        }
    };

    public void startOperation() {
        Thread t = new Thread(restRunnable);
        t.start();
    }
}
