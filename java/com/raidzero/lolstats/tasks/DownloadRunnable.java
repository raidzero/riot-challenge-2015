package com.raidzero.lolstats.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by posborn on 3/31/15.
 */
public class DownloadRunnable implements Runnable {
    private final String tag = "DownloadTask";

    private Handler mUiHandler;
    private URL mUrl = null;
    private File mDownloadedFile = null;

    public DownloadRunnable(Context context, Handler uiHandler, String url) {
        mUiHandler = uiHandler;

        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            //ignored
        }

        String fileName = url.substring(url.lastIndexOf('/') + 1, url.length());

        File destDir = context.getCacheDir();

        mDownloadedFile = new File(destDir, fileName);
    }


    @Override
    public void run() {
        try {
            URLConnection urlConnection = mUrl.openConnection();
            urlConnection.setUseCaches(true);
            urlConnection.connect();

            InputStream is = urlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            ByteArrayBuffer baf = new ByteArrayBuffer(5000);

            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            FileOutputStream fos = new FileOutputStream(mDownloadedFile);
            fos.write(baf.toByteArray());
            fos.flush();
            fos.close();

            Message completeMsg = new Message();
            completeMsg.obj = Uri.fromFile(mDownloadedFile);

            mUiHandler.sendMessage(completeMsg);

        } catch (IOException e) {
            Log.e(tag, e.getLocalizedMessage());
        }
    }
}
