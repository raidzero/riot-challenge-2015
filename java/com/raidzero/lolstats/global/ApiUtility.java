package com.raidzero.lolstats.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
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

    private int mGoBackInTime = 0;
    // list of running threads
    private ArrayList<Thread> mRunningThreads = new ArrayList<>();

    private String mRegion;

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
    private void startThreadAndWait(Runnable r) throws InterruptedException
    {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);

        mRunningThreads.add(t);

        t.start();
        t.join();
    }


    private void startThread(Runnable r) throws InterruptedException
    {
        Thread t = new Thread(r);
        t.setPriority(Thread.MIN_PRIORITY);

        mRunningThreads.add(t);

        t.start();
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
            e.printStackTrace();
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
            mGoBackInTime += 5; // go back another five minutes
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

            if (mGoBackInTime > 0) {
                Log.d(tag, "Going back in time (" + mGoBackInTime + " minutes)");
            }

            timestamp -= mGoBackInTime * 60; // minutes in seconds

            Log.d(tag, "timestamp: " + timestamp);

            String requestUrlStr =
                    String.format(Common.API_PREFIX, mRegion) + "/" + mRegion + Common.RANDOM_MATCH_PATH + "?beginDate=" + timestamp +
                            "&api_key=" + Common.getApiKey();

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
            } else {
                mCallback.onError();
                shutDown();
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
                    String.format(Common.API_PREFIX, mRegion) + "/" + mRegion +
                            Common.MATCH_PATH + mMatchId + "?api_key=" + Common.getApiKey();

            String response = restRequest(requestUrlStr);

            if (!response.isEmpty()) {
                try {
                    MatchParser parser = new MatchParser(response);
                    Match m = parser.getMatchFromParser();

                    // process champions in match
                    for (int i = 0; i < m.participants.length; i++) {
                        int championId = m.participants[i].champion.id;
                        String champReponse =
                                restRequest(String.format(Common.API_PREFIX, "na")
                                        + Common.CHAMPION_PATH + championId +
                                "?api_key=" + Common.getApiKey());
                        ChampionParser champParser = new ChampionParser(champReponse);

                        Champion c = champParser.getChampionFromParser();

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
            } catch (Exception e) {

            }

            if (!mMatchIds.isEmpty()) {
                Log.d(tag, "Processing matches.");
                Log.d(tag, "got " + mMatchIds.size() + " matches");

                if (!mMatchIds.isEmpty()) {
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
                }

                if (mMatches.size() == 1) {
                    // only one returned. get some more just to be on the safe side
                    mGoBackInTime += 5;
                    mHandler.post(this);
                }
            } else {
                // no matches. go back in time, after waiting a second so as not to exceed the rate limit
                mCallback.onGoBackInTime();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {

                }
                Log.d(tag, "No matches found.");
                mGoBackInTime += 5;
                mHandler.post(this);
            }
        }
    };

    // starts the whole shebang
    public void startProcessing() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mRegion = prefs.getString("pref_region", "na");

        try {
            startThread(startProcessingRunnable);
        } catch (InterruptedException e) {
            shutDown();
        }
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
        void onGoBackInTime();
        void onError();
        void onFirstMatchProcessed();
        void onAllMatchesProcessed();
    }
}
