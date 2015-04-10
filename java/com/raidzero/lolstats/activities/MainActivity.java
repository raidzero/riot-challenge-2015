package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.global.AppHelper;
import com.raidzero.lolstats.global.ChampionInfoDownloader;
import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.global.DateUtility;
import com.raidzero.lolstats.global.RequestProcessor;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.RequestCommandListener;
import com.raidzero.lolstats.parsers.MatchParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements ChampionImageListener, RequestCommandListener, DateUtility.MatchIDsListener {
    private final String tag = "MainActivity";

    private Champion[] mChampions = new Champion[10];

    private AppHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHelper = ((AppHelper) getApplicationContext());

        setContentView(R.layout.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        setLoadingScreenDrawable();

        new DateUtility(this).getMatchIds();
    }

    @Override
    public void onChampionImagesDownloaded() {
        Intent i = new Intent(this, MatchResultsView.class);
        startActivity(i);
    }

    private void setLoadingScreenDrawable() {
        int randomNum = new Random().nextInt((4 - 1) + 1) + 1;
        try {
            Drawable d = Drawable.createFromStream(getAssets().open("loading" + randomNum + ".jpg"), null);
            getWindow().getDecorView().setBackground(d);
        } catch (IOException e) {
            Log.e(tag, e.getMessage());
        }
    }

    @Override
    public void onProcessStart(RequestCommand command) {
        Log.d(tag, "onProcessStart()");
    }

    @Override
    public void onProcessComplete(RequestCommand command) {
        Log.d(tag, "onProcessComplete: " + command.restResponse);

        try {
            String jsonData = command.restResponse;

            MatchParser parser = new MatchParser(jsonData);
            Match m = parser.getMatchFromParser();

            mHelper.setCurrentMatch(m);

            Log.d(tag, "HOORAY! GOT A MATCH OBJECT: " + m.matchMode);

            // start champion processor which downloads all champion images
            mChampions = m.getChampionsInMatch();

            mHelper.setMatchChampions(mChampions);

            ChampionInfoDownloader processor = new ChampionInfoDownloader(this);
            processor.processChampions();

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(tag, e.getMessage());
        }
    }

    @Override
    public void onMatchesReceived(List<Long> matchesReceived) {
        if (matchesReceived != null) {
            Log.d(tag, "Got stuff... " + matchesReceived.size());

            long matchId = matchesReceived.get(new Random().nextInt(matchesReceived.size()));

            // make request command
            RequestCommand command = new RequestCommand();
            command.requestUrl = Common.MATCH_PATH + matchId;
            command.requestId = 0;
            command.listener = this;

            // start request processor
            RequestProcessor.addRequest(command);

        }
    }
}
