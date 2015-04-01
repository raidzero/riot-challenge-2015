package com.raidzero.lolstats.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.raidzero.lolstats.data.FileRequestMessage;
import com.raidzero.lolstats.interfaces.FileRequestListener;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.raidzero.lolstats.global.Common.FILE_MESSAGE_TYPE;

/**
 * Created by posborn on 3/31/15.
 */
public class FileRequest {
    private final String tag = "FileRequest";

    private int mReqCode;
    private URL mRequestUrl;
    private FileRequestListener mFileRequestListener;
    private File mDownloadedFile = null;

    // handler to invoke callback method
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FileRequestMessage requestMessage = (FileRequestMessage) msg.obj;
            switch (requestMessage.msgType) {

                case ON_START:
                    mFileRequestListener.onFileDownloadStart(mReqCode);
                    break;

                case ON_COMPLETE:
                    mFileRequestListener.onFileComplete(mReqCode, (Uri) requestMessage.obj);
                    break;
            }
        }
    };

    public FileRequest(Context context, FileRequestListener listener, int requestCode, String requestUrl) {
        mFileRequestListener = listener;
        mReqCode = requestCode;

        try {
            mRequestUrl = new URL(requestUrl);
        } catch (MalformedURLException e) {
            //ignored
        }

        // pull off filename
        String fileName = requestUrl.substring(requestUrl.lastIndexOf('/') + 1, requestUrl.length());

        File destDir = context.getCacheDir();

        mDownloadedFile = new File(destDir, fileName);
    }

    private Runnable restRunnable = new Runnable() {
        @Override
        public void run() {

           // send start message from the factory
            mHandler.sendMessage(buildMessage(
                    FILE_MESSAGE_TYPE.ON_START, null));

            try {
                URLConnection urlConnection = mRequestUrl.openConnection();
                urlConnection.setUseCaches(true);
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayBuffer baf = new ByteArrayBuffer(5000);

                int current;
                while ((current = bis.read()) != -1) {
                    baf.append((byte) current);
                }

                FileOutputStream fos = new FileOutputStream(mDownloadedFile);
                fos.write(baf.toByteArray());
                fos.flush();
                fos.close();

                // send file complete message from factory
                mHandler.sendMessage(buildMessage(
                        FILE_MESSAGE_TYPE.ON_COMPLETE,
                        Uri.fromFile(mDownloadedFile)
                ));

            } catch (IOException e) {
                Log.e(tag, e.getMessage());
            }
        }
    };

    public void startOperation() {
        Thread t = new Thread(restRunnable);
        t.start();
    }

    private Message buildMessage(FILE_MESSAGE_TYPE msgType, Object obj) {
        FileRequestMessage fileRequestMessage = new FileRequestMessage(msgType);
        fileRequestMessage.obj = obj;

        Message message = new Message();
        message.obj = fileRequestMessage;

        return message;
    }
}
