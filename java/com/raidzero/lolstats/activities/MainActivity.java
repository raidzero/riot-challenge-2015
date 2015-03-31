package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.interfaces.RestRequestListener;
import com.raidzero.lolstats.tasks.DownloadRunnable;
import com.raidzero.lolstats.tasks.RestRequest;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements RestRequestListener {
    private final String tag = "MainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        Handler uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Log.d(tag, "Message received");
            }
        };

        // simulate receiving 5 match ID's
        for (int matchId : Common.MATCH_IDS) {
            RestRequest request = new RestRequest(this,
                    Common.API_PREFIX + Common.MATCH_PATH + matchId);
            request.startOperation();

        }

        DownloadRunnable d = new DownloadRunnable(this, uiHandler, Common.CHAMPION_SKIN_URL_PREFIX + "Malzahar_0.jpg");
        Thread downloadThread = new Thread(d);
        downloadThread.start();
    }

    @Override
    public void onRestRequestComplete(String jsonData) {
        Log.d(tag, "HEY I GOT STUFF");
        Log.d(tag, jsonData);


    }
}
