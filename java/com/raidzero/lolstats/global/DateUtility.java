package com.raidzero.lolstats.global;

import android.util.Log;

import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.interfaces.RequestCommandListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by raidzero on 4/10/15.
 */
public class DateUtility implements RequestCommandListener {
    private static final String tag = "DateUtility";

    private MatchIDsListener mListener;
    public DateUtility(MatchIDsListener listener) {
        mListener = listener;
    }

    /**
     * this gets a timestamp 15 minutes ago.
     * I think 15 minutes is a good range for games to be played
     * @return long unix epoch mins ago
     */
    private long getTimestamp() {
        Calendar c = Calendar.getInstance();
        int minute = c.get(Calendar.MINUTE);

        // is it divisible by 5?
        int minsPast = minute % 5;
        if (minsPast == 0) {
            // already a 5 minute interval. subtract 15 mins
            minute -= 15;
        } else {
            // get last valid minute and then subtract 15 from that
            minute -= (minsPast + 15);
        }

        // force minutes & seconds back
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return (c.getTime().getTime()) / 1000; // dont care about milliseconds
    }

    public void getMatchIds() {
        long timestamp = getTimestamp();

        RequestCommand command = new RequestCommand();
        command.requestUrl = Common.RANDOM_MATCH_PATH +
                "?beginDate=" + timestamp;
        command.listener = this;
        command.requestId = 0;

        RequestProcessor.addRequest(command);
    }

    @Override
    public void onProcessStart(RequestCommand command) {
        Log.d(tag, "starting match ID request for " + command.requestUrl);
    }

    @Override
    public void onProcessComplete(RequestCommand command) {
        Log.d(tag, "got response: " + command.restResponse);

        try {
            List<Long> results = new ArrayList<Long>();

            JSONArray jsonArray = new JSONArray(command.restResponse);

            for (int i = 0; i < jsonArray.length(); i++) {
                results.add(jsonArray.getLong(i));
            }

            mListener.onMatchesReceived(results);

        } catch (JSONException e) {
            e.printStackTrace();
            mListener.onMatchesReceived(null);
        }
    }

    public interface MatchIDsListener {
        void onMatchesReceived(List<Long> matchesReceived);
    }
}
