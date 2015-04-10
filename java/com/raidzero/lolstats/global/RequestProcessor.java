package com.raidzero.lolstats.global;


import android.content.Context;
import android.util.Log;

import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.interfaces.RequestCommandListener;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Stack;

/**
 * Created by raidzero on 4/6/15.
 */
public class RequestProcessor {
    private static final String tag = "RequestProcessor";
    private Stack<RequestCommand> mCommands = null;

    protected static RequestProcessor mInstance = null;
    private ProcessorThread mProcessorThread;
    private boolean mRunning;
    private int mNumWorkersRunning;
    protected RequestCommandListener mListener;

    private static RequestProcessor getInstance() {
        if (mInstance == null) {
            mInstance = new RequestProcessor();
        }

        return mInstance;
    }


    protected RequestProcessor() {
        // initialize command stack
        mCommands = new Stack<>();

        // start command processor thread
        mProcessorThread = new ProcessorThread();
        mProcessorThread.start();

        mRunning = true;
    }

    /**
     * adds a request to the command processor
     *
     * @param command (RequestCommand)
     */
    public static void addRequest(RequestCommand command) {
        RequestProcessor instance = RequestProcessor.getInstance();
        instance.iAddRequest(command);
    }

    // internally add a command to the stack
    private void iAddRequest(RequestCommand command) {
        if (!mCommands.contains(command)) {
            mCommands.push(command);
        }
    }

    public void stopProcessor() {
        mRunning = false;
        mProcessorThread.interrupt();
    }

    class ProcessorThread extends Thread {

        public ProcessorThread() {
            super(tag);
        }

        public void run() {
            Log.d(tag, "Started running!");

            while (mRunning) {
                // pop first command off the stack and start running it
                if (!mCommands.empty()) {
                    RequestCommand command = mCommands.pop();

                    // start rest thread on this command
                    RestRunnable restRunnable = new RestRunnable();
                    restRunnable.setCommand(command);
                    Thread restThread = new Thread(restRunnable);
                    restThread.start();
                    mNumWorkersRunning++;
                }
            }
        }

        /**
         * base command runnable
         */
        private abstract class CommandRunnable {
            protected RequestCommand mCommand;

            protected URL requestUrl = null;
            protected String mRequestUrl = "";
            protected RequestCommandListener mListener;

            public CommandRunnable() {
                // default constructor
            }
        }

        private class RestRunnable extends CommandRunnable implements Runnable {

            public void setCommand(RequestCommand command) {
                mCommand = command;
                mRequestUrl = command.requestUrl;
                mListener = command.listener;
            }

            @Override
            public void run() {
                mListener.onProcessStart(mCommand);

                // set up API call
                String requestString = Common.API_PREFIX;

                // add in actual request path
                requestString += mRequestUrl;

                // stick API key on the end
                String separator = "?";

                if (requestString.contains("?")) {
                    separator = "&";
                }

                requestString += separator + "api_key=" + Common.API_KEY;

                Log.d(tag, "requestString: " + requestString);

                try {
                    requestUrl = new URL(requestString);
                } catch (MalformedURLException e) {
                    Log.e(tag, "Malformed URL!" + e.getMessage());
                }

                try {
                    URLConnection urlConnection = requestUrl.openConnection();
                    urlConnection.setUseCaches(true);
                    urlConnection.connect();

                    InputStream is = urlConnection.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);

                    byte[] buffer = new byte[1024]; // read 1024 bytes at a time

                    int bytesRead = 0;

                    String response = "";
                    while ((bytesRead = bis.read(buffer)) != -1) {
                        response += new String(buffer, 0, bytesRead);
                    }

                    mCommand.restResponse = response;

                    Log.d(tag, "obj: " + response);
                    mListener.onProcessComplete(mCommand);
                    mNumWorkersRunning--;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mListener.onProcessComplete(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
