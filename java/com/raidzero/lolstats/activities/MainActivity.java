package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.global.AppHelper;
import com.raidzero.lolstats.global.ChampionImageDownloader;
import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.global.RequestProcessor;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.RequestCommandListener;
import com.raidzero.lolstats.parsers.MatchParser;

import org.json.JSONException;

import java.io.IOException;
import java.util.Random;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements ChampionImageListener, RequestCommandListener {
    private final String tag = "MainActivity";

    private Champion[] mChampions = new Champion[10];

    private ImageView portraitView;

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

        portraitView = (ImageView) findViewById(R.id.imgPortraitView);
        int matchId = Common.MATCH_IDS[new Random().nextInt(4)];

        // make request command
        RequestCommand command = new RequestCommand();
        command.requestType = RequestCommand.RequestType.REST;
        command.requestUrl = Common.MATCH_PATH + matchId;
        command.requestId = 0;
        command.listener = this;

        // start request processor
        RequestProcessor.addRequest(command);
    }

    @Override
    public void onChampionImagesDownloaded() {
        Intent i = new Intent(this, MatchResultsView.class);
        startActivity(i);
        /*int r = new Random().nextInt(9);
        String bgPath = getCacheDir() + mChampions[r].getChampionBackgroundPath();
        String fgPath = getCacheDir() + mChampions[r].getChampionPortaitPath();

        Log.d(tag, "bgImg: " + bgPath);

        final Drawable bgImg = Drawable.createFromPath(bgPath);
        final Drawable fgImg = Drawable.createFromPath(fgPath);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                portraitView.setImageDrawable(fgImg);
                getWindow().getDecorView().setBackground(bgImg);
            }
        });*/
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
            switch (command.requestType) {
                case REST:
                    String jsonData = command.restResponse;

                    MatchParser parser = new MatchParser(jsonData);
                    Match m = parser.getMatchFromParser();

                    mHelper.setCurrentMatch(m);

                    Log.d(tag, "HOORAY! GOT A MATCH OBJECT: " + m.matchMode);

                    // start champion processor which downloads all champion images
                    mChampions = m.getChampionsInMatch();

                    mHelper.setMatchChampions(mChampions);

                    ChampionImageDownloader processor = new ChampionImageDownloader(this);
                    processor.processChampions();

                    break;
            }
        } catch (JSONException e) {
            Log.e(tag, e.getMessage());
        }
    }
}
