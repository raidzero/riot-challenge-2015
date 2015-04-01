package com.raidzero.lolstats.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.raidzero.lolstats.R;
import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.Match;
import com.raidzero.lolstats.data.Participant;
import com.raidzero.lolstats.global.ChampionImageDownloader;
import com.raidzero.lolstats.global.Common;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.FileRequestListener;
import com.raidzero.lolstats.interfaces.RestRequestListener;
import com.raidzero.lolstats.parsers.MatchParser;

import org.json.JSONException;

import com.raidzero.lolstats.global.Common.REQUEST_TYPE;
import com.raidzero.lolstats.tasks.RestRequest;

import java.io.IOException;
import java.util.Random;

/**
 * Created by posborn on 3/31/15.
 */
public class MainActivity extends Activity implements RestRequestListener, ChampionImageListener, FileRequestListener {
    private final String tag = "MainActivity";

    private Champion[] mChampions = new Champion[10];

    private ImageView portraitView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
    }

    @Override
    public void onResume() {
        super.onResume();

        setLoadingScreenDrawable();

        portraitView = (ImageView) findViewById(R.id.imgPortraitView);
        int matchId = Common.MATCH_IDS[new Random().nextInt(4)];

        RestRequest matchRequest = new RestRequest(this, REQUEST_TYPE.MATCH, Common.REQUEST_CODE_MATCH, Common.MATCH_PATH + matchId);
        matchRequest.startOperation();
    }

    @Override
    public void onRestRequestComplete(REQUEST_TYPE reqType, int requestCode, String jsonData) {
        Log.d(tag, "HEY I GOT STUFF: " + jsonData.length() + " bytes. " + reqType);

        try {
            switch (reqType) {
                case MATCH:
                    MatchParser parser = new MatchParser(jsonData);
                    Match m = parser.getMatchFromParser();

                    Log.d(tag, "HOORAY! GOT A MATCH OBJECT: " + m.matchMode);

                    // start champion processor which downloads all champion images
                    mChampions = m.getChampionsInMatch();
                    ChampionImageDownloader processor = new ChampionImageDownloader(this);
                    processor.processChampions(mChampions);

                    break;
            }
        } catch (JSONException e) {
            Log.e(tag, e.getMessage());
        }
    }

    @Override
    public void onChampionImagesDownloaded() {
        int r = new Random().nextInt(9);
        String bgPath = getCacheDir() + mChampions[r].getChampionBackgroundPath();
        String fgPath = getCacheDir() + mChampions[r].getChampionPortaitPath();

        Log.d(tag, "bgImg: " + bgPath);

        Drawable bgImg = Drawable.createFromPath(bgPath);
        Drawable fgImg = Drawable.createFromPath(fgPath);

        portraitView.setImageDrawable(fgImg);
        getWindow().getDecorView().setBackground(bgImg);
    }

    @Override
    public void onFileDownloadStart(int requestCode) {

    }

    @Override
    public void onFileComplete(int requestCode, Uri fileUri) {
        String path = fileUri.getEncodedPath();
        Drawable d = Drawable.createFromPath(path);
        getWindow().getDecorView().setBackground(d);
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
}
