package com.raidzero.lolstats.global;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.parsers.ChampionParser;
import com.raidzero.lolstats.parsers.MatchParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by raidzero on 4/10/15.
 * as long as this is running, it will send a parsed match whenever called... infinitely
 * well, not infinitely, but until the number of games has been exhausted, which may as well
 * be infinite :)
 */
public class ApiUtility {
    private static final String tag = "ApiUtility";

    // callback interface
    private static ApiCallback mCallback;
    private static Context mContext;
    private static Handler mHandler;

    // stacks of stuff
    private Stack<Long> mMatchIds = new Stack<>();
    private Stack<Match> mMatches = new Stack<>();

    private boolean mGoBackInTime;
    // list of running threads
    private ArrayList<Thread> mRunningThreads = new ArrayList<>();

    /**
     * singleton stuff
     */
    private static ApiUtility instance;

    protected ApiUtility() {
        // just so it cant be instantiated
    }

    public static ApiUtility getInstance(ApiCallback callback) {
        mCallback = callback;
        mContext = (Context) callback;
        mHandler = new Handler();

        if (instance == null) {
            instance = new ApiUtility();
        }

        return instance;
    }

    /**
     * starts a thread given a runnable, and waits for it to finish
     */
    private void startThreadAndWait(Runnable r) throws InterruptedException {
        Thread t = new Thread(r);

        mRunningThreads.add(t);

        t.start();
        t.join();
    }

    /**
     * performs a rest request
     * @param requestUrlStr URL string
     * @return response string
     */
    private String restRequest(String requestUrlStr) {
        URL requestUrl = null;
        String response = "";

        try {
            requestUrl = new URL(requestUrlStr);
        } catch (MalformedURLException e) {
            return "";
        }

        try {
            URLConnection urlConnection = requestUrl.openConnection();
            urlConnection.setUseCaches(true);
            urlConnection.connect();

            InputStream is = urlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            byte[] buffer = new byte[1024]; // read 1024 bytes at a time

            int bytesRead = 0;

            response = "";
            while ((bytesRead = bis.read(buffer)) != -1) {
                response += new String(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            return "";
        }

        return response;
    }

    /**
     * performs a file download
     * @param requestUrlStr url string
     * @return path to downloaded file
     */
    private String fileRequest(String requestUrlStr) {
        // pull off filename
        String fileName = requestUrlStr.substring(
                requestUrlStr.lastIndexOf('/') + 1, requestUrlStr.length());

        InputStream is;
        File destDir = mContext.getCacheDir();
        File downloadedFile = new File(destDir, fileName);

        try {
            is = (InputStream) new URL(requestUrlStr).getContent();

            FileOutputStream fos = new FileOutputStream(downloadedFile);

            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

        } catch (Exception e) {
            return null;
        }

        return downloadedFile.getAbsolutePath();
    }

    private long getNextMatchId() {
        return mMatchIds.pop();
    }

    public Match getNextMatch() {
        Log.d(tag, "getNextMatch() " + mMatches.size()  + " left");

        if (mMatches.size() == 2) {
            // matches are getting low. better get some more
            mGoBackInTime = true;
            startProcessing();
        }

        return mMatches.pop();
    }

    /**
     * runnable to fill up mMatchIds
     */
    private Runnable getMatchIdsRunnable = new Runnable() {
        @Override
        public void run() {
            long timestamp = DateUtility.getTimestamp();

            if (mGoBackInTime) {
                Log.d(tag, "Going back in time");
                timestamp -= 300; // 5 minutes in seconds
            }

            String requestUrlStr =
                    Common.API_PREFIX + Common.RANDOM_MATCH_PATH + "?beginDate=" + timestamp +
                            "&api_key=" + Common.API_KEY;

            String response = restRequest(requestUrlStr);

            // now that we (hopefully) have a response...
            if (!response.isEmpty()) {
                try {
                    // parse the response and make mMatchIds
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {
                        mMatchIds.push(array.getLong(i));
                    }

                } catch (JSONException e) {
                    return;
                }
            }
        }
    };

    /**
     * runnable to fill up mMatches
     */
    private class GetMatchRunnable implements Runnable {
        private long mMatchId;

        public GetMatchRunnable(long matchId) {
            mMatchId = matchId;
        }

        @Override
        public void run() {
            String requestUrlStr =
                    Common.API_PREFIX + Common.MATCH_PATH + mMatchId + "?api_key=" + Common.API_KEY;

            String response = restRequest(requestUrlStr);

            if (!response.isEmpty()) {
                try {
                    MatchParser parser = new MatchParser(response);
                    Match m = parser.getMatchFromParser();

                    // process champions in match
                    for (int i = 0; i < m.participants.length; i++) {
                        int championId = m.participants[i].champion.id;
                        String champReponse =
                                restRequest(Common.API_PREFIX + Common.CHAMPION_PATH + championId +
                                "?api_key=" + Common.API_KEY);
                        ChampionParser champParser = new ChampionParser(champReponse);

                        Champion c = champParser.getChampionFromParser();

                        // download this champion's portrait
                        /* nevermind - takes too long, let the activity do this
                        String path =
                                fileRequest(Common.CHAMPION_PORTRAIT_URL_PREFIX + c.name + ".png");
                        c.portraitPath = path;
                        */

                        m.participants[i].champion = c;
                    }

                    mMatches.add(m);
                } catch (JSONException e) {
                    return;
                }
            }
        }
    }

    private Runnable startProcessingRunnable = new Runnable() {
        @Override
        public void run() {
            // first, get a list of match ID's from the API
            try {
                startThreadAndWait(getMatchIdsRunnable);
            } catch (InterruptedException e) {
                return;
            }

            Log.d(tag, "Processing matches.");
            if (!mMatchIds.isEmpty()) {
                Log.d(tag, "got " + mMatchIds.size() + " matches");

                for (long matchId = getNextMatchId();
                     mMatchIds.size() > 0; matchId = getNextMatchId()) {

                    try {
                        startThreadAndWait(new GetMatchRunnable(matchId));
                    } catch (InterruptedException e) {
                        return;
                    }

                    if (mMatches.size() == 1) {
                        mCallback.onFirstMatchProcessed();
                    }
                }

                if (mMatches.size() == 1) {
                    mGoBackInTime = true;
                    mHandler.post(this);
                }

            }
        }
    };

    // starts the whole shebang
    public void startProcessing() {
        Thread t = new Thread(startProcessingRunnable);
        mRunningThreads.add(t);
        t.start();
    }

    public void shutDown() {
        Log.d(tag, "shutting down all workers");

        for (Thread t : mRunningThreads) {
            t.interrupt();
        }
    }

    /**
     * callback interface
     */
    public interface ApiCallback {
        void onFirstMatchProcessed();
        void onAllMatchesProcessed();
    }
}
