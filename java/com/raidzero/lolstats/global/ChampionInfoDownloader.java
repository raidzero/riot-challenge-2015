package com.raidzero.lolstats.global;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.raidzero.lolstats.data.Champion;
import com.raidzero.lolstats.data.RequestCommand;
import com.raidzero.lolstats.interfaces.ChampionImageListener;
import com.raidzero.lolstats.interfaces.RequestCommandListener;
import com.raidzero.lolstats.parsers.ChampionParser;

import org.json.JSONException;

import java.util.Collections;

/**
 * Created by posborn on 4/1/15.
 */
public class ChampionInfoDownloader implements RequestCommandListener {
    private String tag = "ChampionUtils";

    private Context mContext;
    private Champion[] mChampions = new Champion[10];
    private ChampionImageListener mListener;
    private AppHelper mHelper;

    private int mNumChampInfo;

    public ChampionInfoDownloader(Context context) {
        mContext = context;
        mListener = (ChampionImageListener) context;
        mHelper = ((AppHelper) context.getApplicationContext());
    }

    public void processChampions() {
        Champion[] champs = mHelper.getMatchChampions();

        for (int i = 0; i < champs.length; i++) {
            // get names and stuff
            RequestCommand command = new RequestCommand();
            command.listener = this;
            command.requestId = i;
            command.requestUrl = Common.CHAMPION_PATH + champs[i].id;

            RequestProcessor.addRequest(command);
        }
    }


    @Override
    public void onProcessStart(RequestCommand command) {
        Log.d(tag, "Download started: " + command.requestUrl);
    }

    @Override
    public void onProcessComplete(RequestCommand command) {
        Log.d(tag, "onProcessComplete: " + command.requestId);


        try {
            ChampionParser parser = new ChampionParser(command.restResponse);
            mChampions[command.requestId] = parser.getChampionFromParser();

            mNumChampInfo++;

            if (mNumChampInfo == 10) {
                // if all done, send this back to helper
                mHelper.setMatchChampions(mChampions);
                mListener.onChampionImagesDownloaded();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
